package io.github.portfoligno.dex

import scala.reflect.ClassTag

package object permission {
  @inline
  def classTagOf[A](implicit A: ClassTag[A]): ClassTag[A] = A
}
