package cz.dizider.pricelistgenerator

import fs2.io.file.Path

case class GeneratorContext(
                           input: Path = Path("pricelist.csv"),
                           output: Path = Path("pricelist.json"),
                           prettyPrint: Boolean = false,
                           random: Boolean = false,
                           accessToken: Option[String] = sys.env.get("access-token"),
                           generator: Generator = new Generator(),
                           mapping: Path = Path("mapping.json")
                           )
