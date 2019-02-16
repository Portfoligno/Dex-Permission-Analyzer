package io.github.portfoligno.dex.permission.data

case class MethodIdentity(
  name: String,
  arguments: List[ClassName]
) {
  override
  def toString: String =
    arguments
      .addString(new StringBuilder(name), "(", ",", ")")
      .mkString
}
