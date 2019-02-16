package io.github.portfoligno.dex.permission

import io.github.portfoligno.dex.permission.data.{ClassMethod, ClassName, MethodIdentity}

case class AnalysisResult(
  method: MethodIdentity,
  callers: Set[ClassMethod],
  permissions: Map[ClassName, Set[String]]
)
