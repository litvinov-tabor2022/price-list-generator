package cz.dizider.pricelistgenerator

import cats.effect.IO
import fs2.data.csv._
import fs2.io.file.{Files, Path}
import fs2.{Stream, text}
import io.circe._
import io.circe.fs2._
import io.circe.generic.semiauto._

import java.nio.file.NoSuchFileException

case class PriceListEntry(id: String, name: String, altName: String, description: Option[String], note: Option[String],
                          constraints: Constraints, skill: Int,
                          createdAt: Option[String], code: Option[Int]) {
  def withCode(code: Int): PriceListEntry = {
    PriceListEntry(id, name, altName, description, note, constraints, skill, createdAt, Some(code))
  }

  def withRandomConstraints(): PriceListEntry = {
    val constraints = Constraints(scala.util.Random.between(0, 20), scala.util.Random.between(0, 20), scala.util.Random.between(0, 20))
    PriceListEntry(id, name, altName, description, note, constraints, skill, createdAt, code)
  }
}

object PriceListEntry {
  implicit val priceListEntryJsonEncoder: Encoder[PriceListEntry] =
    (entry: PriceListEntry) => Json.obj(
      ("code", entry.code.map(Json.fromInt).getOrElse(throw new InternalError("Empty 'code'."))),
      ("skill", entry.code.map(Json.fromInt).getOrElse(throw new InternalError("Empty 'skill'."))),
      ("name", Json.fromString(entry.name)),
      ("altName", Json.fromString(entry.altName)),
      ("description", entry.description.map(Json.fromString).getOrElse(Json.Null)),
      ("note", entry.note.map(Json.fromString).getOrElse(Json.Null)),
      ("strength", Json.fromInt(entry.constraints.strength)),
      ("magic", Json.fromInt(entry.constraints.magic)),
      ("dexterity", Json.fromInt(entry.constraints.dexterity)),
    )

  implicit object PriceListEntryCsvDecoder extends CsvRowDecoder[PriceListEntry, String] {
    def apply(row: CsvRow[String]): DecoderResult[PriceListEntry] =
      for {
        name <- row.as[String]("name")
        description <- row.as[String]("description").map(Some(_)).left.flatMap(_ => Right(None))
        note <- row.as[String]("note").map(Some(_)).left.flatMap(_ => Right(None))
        strength <- row.as[Int]("strength").left.flatMap(_ => Right(0))
        magic <- row.as[Int]("magic").left.flatMap(_ => Right(0))
        dexterity <- row.as[Int]("dexterity").left.flatMap(_ => Right(0))
        skill <- row.as[Int]("skill").left.flatMap(_ => Right(0))
        createdAt <- row.as[String]("createdAt").map(Some(_)).left.flatMap(_ => Right(None))
        id <- row.as[String]("id")
      } yield PriceListEntry(id, name, name.toUpperCase, description, note, Constraints(strength, magic, dexterity), skill, createdAt, None)
  }
}

case class Constraints(strength: Int, magic: Int, dexterity: Int)

case class Entries(entries: Map[String, Entry]) {
  lazy val nextCode: Int = if (entries.isEmpty) 0 else (entries.values.max.code + 1)

  def add(entry: Entry): Entries = Entries(entries + (entry.hash -> entry))
}

object Entries {
  val load: Path => Stream[IO, Entries] = (path: Path) =>
    Files[IO]
      .readAll(path)
      .through(text.utf8.decode)
      .through(stringStreamParser)
      .through(decoder[IO, Entries])
      .handleErrorWith {
        case _: NoSuchFileException => Stream.emit(Entries(Map.empty))
        case err => throw err
      }

  implicit val entriesDecoder: Decoder[Entries] = deriveDecoder
  implicit val entriesEncoder: Encoder[Entries] = deriveEncoder
}

case class Entry(code: Int, hash: String)

object Entry {
  implicit def orderingByCode[A <: Entry]: Ordering[Entry] =
    Ordering.by(e => (e.code))

  implicit val enumsEntryDecoder: Decoder[Entry] = deriveDecoder
  implicit val enumsEntryEncoder: Encoder[Entry] = deriveEncoder
}
