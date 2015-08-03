/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.widok.moment

import scala.scalajs.js

trait Getters extends js.Object {
  def milliseconds(): Int = js.native
  def seconds(): Double = js.native
  def minutes(): Double = js.native
  def hours(): Double = js.native
  def days(): Double = js.native
  def months(): Double = js.native
  def years(): Double = js.native
}
