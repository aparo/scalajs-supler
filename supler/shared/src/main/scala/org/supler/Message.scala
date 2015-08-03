/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler

import org.supler.transformation.{BasicTypeTransformer}

case class Message(key: String, params: Any*)

object Message {
  def apply[U](v: U, params: Any*)(implicit transformer: BasicTypeTransformer[U, String]) =
    new Message(transformer.serialize(v), params: _*)
}