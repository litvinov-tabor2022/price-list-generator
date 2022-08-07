package cz.dizider.pricelistgenerator

import cats.effect.IO
import io.circe.syntax._
import fs2._
import fs2.io.file.Files

trait OutputWriter {
  def write(src: Stream[IO, List[PriceListEntry]])(implicit ctx: GeneratorContext): Stream[IO, Unit]
}

object JsonWriter extends OutputWriter {
  override def write(src: Stream[IO, List[PriceListEntry]])(implicit ctx: GeneratorContext): Stream[IO, Unit] =
    src.map(x => if (ctx.prettyPrint) x.asJson.spaces4 else x.asJson.noSpaces).through(text.utf8.encode).through(Files[IO].writeAll(ctx.output))
}

object ProtobufWriter extends OutputWriter {
  override def write(src: Stream[IO, List[PriceListEntry]])(implicit ctx: GeneratorContext): Stream[IO, Unit] =
    src.map(x =>
      x.map(item => s"${item.altName.replaceAll("\\s", "_")} = ${item.code.getOrElse(throw new InternalError("Item does not have code"))};"
      ).fold("")((acc, item) => acc + s"$item\n"))
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(ctx.protoOutput))
}

object SqlWriter extends OutputWriter {
  override def write(src: Stream[IO, List[PriceListEntry]])(implicit ctx: GeneratorContext): Stream[IO, Unit] =
    src.map(x =>
      x.map(
        item => s"(${item.code.getOrElse(throw new InternalError("Item does not have code"))}," +
          s"\"${item.name}\"," +
          s"${item.constraints.strength}," +
          s"${item.constraints.dexterity}," +
          s"${item.constraints.magic}," +
          s"0),"
      ).fold("INSERT INTO skills VALUES ")((acc, item) => acc + s"$item\n"))
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(ctx.sqlOutput))
}
