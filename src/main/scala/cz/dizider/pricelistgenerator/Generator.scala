package cz.dizider.pricelistgenerator

import cats.effect.IO
import fs2.data.csv.decodeUsingHeaders
import fs2.io.file.Files
import fs2.{Stream, text}

class Generator() {
  var preProcessed: Entries = Entries(Map.empty)
  private val dd = new DuplicateDetector()

  def addEntries(entries: Entries): Unit = {
    preProcessed = entries
  }

  def generatePriceList(implicit ctx: GeneratorContext): Stream[IO, PriceListEntry] =
    Files[IO]
      .readAll(ctx.input)
      .through(text.utf8.decode)
      .through(decodeUsingHeaders[PriceListEntry]())
      .flatMap(validation)
      .flatMap(addCode)

  def generatorState: Stream[IO, Entries] = Stream.emit(preProcessed)

  private val addCode: PriceListEntry => Stream[IO, PriceListEntry] = (x: PriceListEntry) => {
    Stream.emit {
      preProcessed.entries.get(x.id) match {
        case Some(code) => x.withCode(code.code)
        case None =>
          val next = preProcessed.nextCode
          preProcessed = preProcessed.add(Entry(next, x.id))
          x.withCode(next)
      }
    }
  }

  private val validation: PriceListEntry => Stream[IO, PriceListEntry] = (x: PriceListEntry) => {
    if (dd.isDuplicated(x))
      Stream.emit(Stream.raiseError[IO](new Exception(s"Duplicated item $x"))).covary[IO].flatten
    else Stream.emit(x)
  }
}