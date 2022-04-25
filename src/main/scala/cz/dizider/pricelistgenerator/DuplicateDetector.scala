package cz.dizider.pricelistgenerator

class DuplicateDetector {
  var entries: Map[Int, PriceListEntry] = Map.empty

  val isDuplicated: PriceListEntry => Boolean = (entry: PriceListEntry) => {
    entry.code.flatMap(entries.get) match {
      case Some(e) =>
        entries = entries + (e.code.get -> entry)
        true
      case None => false
    }
  }
}
