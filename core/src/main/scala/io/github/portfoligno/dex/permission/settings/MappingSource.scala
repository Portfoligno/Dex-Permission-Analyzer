package io.github.portfoligno.dex.permission.settings

import cats.FlatMap
import cats.effect.ConcurrentEffect
import cats.instances.all._
import cats.syntax.applicative._
import cats.syntax.compose._
import cats.syntax.functor._
import cats.syntax.traverse._
import fs2.text.utf8Decode
import io.github.portfoligno.dex.permission.data.{ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.settings.MappingParser.{axplorer, pScout}
import io.github.portfoligno.dex.permission.utility.{->, Table, flip}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

case class MappingSource(
  raw: List[String -> MappingParser] = List(
    "https://raw.githubusercontent.com/reddr/axplorer/master/permissions/api-25/framework-map-25.txt" -> axplorer,
    "https://raw.githubusercontent.com/reddr/axplorer/master/permissions/api-25/sdk-map-25.txt" -> axplorer,
    "https://raw.githubusercontent.com/zyrikby/PScout/master/results/API_22/allmappings" -> pScout
  ),
  extra: List[MethodIdentity -> ClassName -> String] = flip(
    FlatMap[List].flatMap[String, MethodIdentity -> ClassName -> String]
  )(
    axplorer >>> (_.map(_.fold(throw _, identity)).toList)
  )(
    List(
      // Return types are omitted
      s"$ContentProvider.getCallingPackage()  ::  $CONTENT_PROVIDER",
      s"$ContentProvider.openAssetFile($Uri,$String,$CancellationSignal)  ::  $CONTENT_PROVIDER",
      s"$ContentProvider.openAssetFile($Uri,$String)  ::  $CONTENT_PROVIDER",
      s"$ContentProvider.openFile($Uri,$String,$CancellationSignal)  ::  $CONTENT_PROVIDER",
      s"$ContentProvider.openFile($Uri,$String)  ::  $CONTENT_PROVIDER",
      s"$ContentProvider.openTypedAssetFile($Uri,$String,$Bundle)  ::  $CONTENT_PROVIDER",
      s"$ContentProvider.openTypedAssetFile($Uri,$String,$Bundle,$CancellationSignal)  ::  $CONTENT_PROVIDER",
    )
  )
) {
  import io.github.portfoligno.dex.permission.utility.implicits._

  private[permission]
  def fetch[F[_] : ConcurrentEffect](
    executionContext: ExecutionContext = ExecutionContext.global
  ): F[Table[MethodIdentity, ClassName, Set[String]]] =
    BlazeClientBuilder[F](executionContext)
      .resource
      .evalMap(client =>
        raw
          .map {
            case uri -> parser =>
              client.get(uri)(
                _
                  .body
                  .through(utf8Decode)
                  .compile
                  .fold(new StringBuilder)((b, s) => b ++= s)
                  .map(b =>
                    parser(b.mkString)
                      .map(_.fold(throw _, identity))
                      .toSeq
                  )
              )
          }
          .sequence
      )
      .use(s =>
        (s.view :+ extra)
          .flatten
          .groupBy(_._1)
          .view
          .map(t =>
            t._1 -> t._2.map(_._2).toSet
          )
          .toTable
          .pure[F]
      )
}
