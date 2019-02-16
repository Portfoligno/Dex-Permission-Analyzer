package io.github.portfoligno.dex.permission.settings

import cats.FlatMap
import cats.instances.all._
import cats.syntax.compose._
import io.github.portfoligno.dex.permission.data.{ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.settings.MappingParser.{axplorer, pScout}
import io.github.portfoligno.dex.permission.utility.{->, Table, flip}

case class MappingSource(
  raw: List[String -> MappingParser] = List(
    "https://raw.githubusercontent.com/reddr/axplorer/master/permissions/api-25/framework-map-25.txt" -> axplorer,
    "https://raw.githubusercontent.com/reddr/axplorer/master/permissions/api-25/sdk-map-25.txt" -> axplorer,
    //"https://raw.githubusercontent.com/zyrikby/PScout/master/results/API_22/allmappings" -> pScout
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
  private[permission]
  def fetch[F[_]]: F[Table[MethodIdentity, ClassName, Set[String]]] =
    ???
}
