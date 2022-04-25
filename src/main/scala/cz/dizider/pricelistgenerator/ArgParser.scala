package cz.dizider.pricelistgenerator

import fs2.io.file.Path

import java.io.File
import scopt.{OParser, OParserBuilder}

object ArgParser {
  val builder: OParserBuilder[GeneratorContext] = OParser.builder[GeneratorContext]
  val parser1: OParser[Unit, GeneratorContext] = {
    import builder._
    OParser.sequence(
      programName("pricelist-generator"),
      head("Pricelist generator", "0.1.0"),
      opt[File]('o', "output")
        .required()
        .valueName("<file>")
        .action((x, c) => c.copy(output = Path.fromNioPath(x.toPath)))
        .text("output file"),
      opt[File]('i', "input")
        .required()
        .valueName("<file>")
        .action((x, c) => c.copy(input = Path.fromNioPath(x.toPath)))
        .text("input file"),
      opt[File]('m', "mapping")
        .valueName("<file>")
        .action((x, c) => c.copy(mapping = Path.fromNioPath(x.toPath)))
        .text("mapping of entry ID's to codes"),
      opt[Unit]('p', "pretty")
        .action((x, c) => c.copy(prettyPrint = true))
        .text("pretty printed output"),
      opt[Unit]('r', "random")
        .action((x, c) => c.copy(random = true))
        .text("random values of constraints")
    )
  }
}
