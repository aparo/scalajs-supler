/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.widok.moment

import scala.scalajs.js

trait Setters[T] extends js.Object {
  def add(time: Double, unit: String): T = js.native
  def add(millis: Int): T = js.native
  def add(duration: Duration): T = js.native

  def subtract(time: Double, unit: String): T = js.native
  def subtract(millis: Int): T = js.native
  def subtract(duration: Duration): T = js.native
}
