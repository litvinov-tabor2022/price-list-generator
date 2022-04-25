package cz.dizider.pricelistgenerator

import cats.effect._
import io.circe.syntax._
import fs2._
import fs2.io.file.Files
import scopt.OParser

import scala.language.implicitConversions

object Main extends IOApp {
  val list: Pipe[IO, PriceListEntry, List[PriceListEntry]] = _.fold(List.empty[PriceListEntry])((acc, pl) => pl +: acc)

  def program(implicit ctx: GeneratorContext): Stream[IO, Unit] = {
    for {
      enums <- Entries.load(ctx.mapping)
      _ <- Stream.emit(ctx.generator.addEntries(enums))
      _ <- ctx.generator.generatePriceList
        .map(x => if (ctx.random) x.withRandomConstraints() else x)
        .through(list)
        .map(x => if (ctx.prettyPrint) x.asJson.spaces4 else x.asJson.noSpaces)
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(ctx.output)).as()
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(ArgParser.parser1, args, GeneratorContext()) match {
      case Some(ctx) =>
        implicit val context: GeneratorContext = ctx
        (for {
          _ <- program.compile.drain
          _ <- Stream.emit(ctx.generator.preProcessed.asJson.spaces4)
            .through(text.utf8.encode)
            .through(Files[IO].writeAll(ctx.mapping)).compile.drain
        } yield ()).flatTap(_ => IO(println("Bye!"))).as(ExitCode.Success)
    }
  }
}