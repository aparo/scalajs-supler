/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.widok.moment

import scala.scalajs.js

trait Date extends js.Object with Getters with Setters[Date] {
  def fromNow(): String = js.native
  def fromNow(withoutSuffix: Boolean): String = js.native
  def isDate(): Boolean = js.native
  def format(): String = js.native
  def format(pattern: String): String = js.native
  def startOf(unit: String): Date = js.native
  def endOf(unit: String): Date = js.native
  def calendar(): String = js.native
  def isBefore(date: Date): Boolean = js.native
  def isBefore(date: Date, unit: String): Boolean = js.native
  def isAfter(date: Date): Boolean = js.native
  def isAfter(date: Date, unit: String): Boolean = js.native
}
