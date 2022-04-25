package cz.dizider.pricelistgenerator

import cats.effect.IO
import io.circe.Json
import org.http4s.circe._
import io.circe.syntax._
import org.http4s.LiteralSyntaxMacros.uri
import org.http4s._
import org.http4s.ember.client._
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType
import org.http4s.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import org.typelevel.ci.CIString
import notion.api.v1.NotionClient
import notion.api.v1.model.common.ObjectType
import notion.api.v1.model.databases.QueryResults
import notion.api.v1.model.pages.PageParent
import notion.api.v1.request.search.SearchRequest
import notion.api.v1.model.pages.{PageProperty => prop}

class NC {

}

object NC {
  val client = new NotionClient(System.getenv("access-token"))

  def loadDatabase(implicit ctx: GeneratorContext): IO[Json] = {
    EmberClientBuilder.default[IO].withMaxResponseHeaderSize(4096 * 10).build.use { client =>
      val request = POST(
        uri"https://api.notion.com/v1/databases/b9caae2f47ed47c3b1ed534f26d46ce2/query",
        Header.Raw(CIString("Notion-Version"), "2022-02-22"),
        Authorization(Credentials.Token(AuthScheme.Bearer, ctx.accessToken.get)),
        Accept(MediaType.application.json)
      )
      println(request)
      client.expect[Json](request)
    }
  }
}


case class PriceListDatabase()