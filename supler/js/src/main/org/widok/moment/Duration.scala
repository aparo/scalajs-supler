/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.widok.moment

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

trait Duration extends js.Object with Getters with Setters[Duration] {
  @JSName("humanize")
  def humanise(): String = js.native

  @JSName("humanize")
  def humanise(withSuffix: Boolean): String = js.native
}
