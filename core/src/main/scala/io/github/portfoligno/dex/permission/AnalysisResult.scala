package io.github.portfoligno.dex.permission

import io.github.portfoligno.dex.permission.data.{ClassMethod, ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.utility.->
import org.jf.dexlib2.Opcode

case class AnalysisResult(
  method: MethodIdentity,
  callers: Set[Opcode -> ClassMethod],
  permissions: Map[String, Set[ClassName]]
)
