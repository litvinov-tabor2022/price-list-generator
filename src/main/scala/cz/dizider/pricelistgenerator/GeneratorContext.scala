package cz.dizider.pricelistgenerator

import fs2.io.file.Path

case class GeneratorContext(
                             input: Seq[Path] = Seq(),
                             output: Path = Path("pricelist.json"),
                             protoOutput: Path = Path("pricelist.proto"),
                             sqlOutput: Path = Path("pricelist.sql"),
                             prettyPrint: Boolean = false,
                             random: Boolean = false,
                             protobufOutput: Boolean = false,
                             accessToken: Option[String] = sys.env.get("access-token"),
                             generator: Generator = new Generator(),
                             mapping: Path = Path("mapping.json")
                           )
