package io.github.portfoligno.dex.permission.settings

case class MappingSource(
  urls: List[String] = List(
    "https://raw.githubusercontent.com/reddr/axplorer/master/permissions/api-25/framework-map-25.txt",
    "https://raw.githubusercontent.com/reddr/axplorer/master/permissions/api-25/sdk-map-25.txt"
  ),
  delimiter: String = "::",
  trim: Boolean = true,
  extraEntries: Map[String, String] = Map(
    // Return types are omitted
    s"$ContentProvider.getCallingPackage()" -> CONTENT_PROVIDER,
    s"$ContentProvider.openAssetFile($Uri,$String,$CancellationSignal)" -> CONTENT_PROVIDER,
    s"$ContentProvider.openAssetFile($Uri,$String)" -> CONTENT_PROVIDER,
    s"$ContentProvider.openFile($Uri,$String,$CancellationSignal)" -> CONTENT_PROVIDER,
    s"$ContentProvider.openFile($Uri,$String)" -> CONTENT_PROVIDER,
    s"$ContentProvider.openTypedAssetFile($Uri,$String,$Bundle)" -> CONTENT_PROVIDER,
    s"$ContentProvider.openTypedAssetFile($Uri,$String,$Bundle,$CancellationSignal)" -> CONTENT_PROVIDER,
  )
)
