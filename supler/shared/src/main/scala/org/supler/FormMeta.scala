/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler

import play.api.libs.json._


case class FormMeta(meta: Map[String, String]) {

  def apply(key: String): String = {
    meta(key)
  }

  def +(key: String, value: String) = this.copy(meta = meta + (key -> value))

  def toJSON = (FormMeta.JsonMetaKey -> JsObject(meta.toList.map {case (key, value) => key-> JsString(value)}))
  
  def isEmpty = meta.isEmpty
}

object FormMeta {
  val JsonMetaKey = "supler_meta"

  def fromJSON(json: JsValue) = {
    json match {
      case JsObject(fields) => fields.toMap.get(JsonMetaKey) match {
        case Some(JsObject(entries)) => FormMeta(entries.toMap.collect{case (key: String, value: JsString) => key -> value.value})
        case Some(_) => throw new IllegalArgumentException("Form meta is not well formed")
        case None => FormMeta(Map())
      }
      case _ => FormMeta(Map())
    }
  }
}
