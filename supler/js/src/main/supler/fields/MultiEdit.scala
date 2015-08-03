/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields

import app.components.MultiEdit
import app.utils.IDGenerator
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLSelectElement
import supler.Field
import supler.GlobalOptions
import supler.PossibleValue
import supler.RenderHint
import supler.Validation
import supler._

import scala.scalajs.js

case class MultiEditField(name: String, onChange: (Field => Unit), label: String = "", validate: Validation[List[Int]] = Validation[List[Int]](), path: String = "",
                       var value: List[String] = Nil, multiple: Boolean = false, emptyValue: List[String] = Nil,
                       var possibleValues: List[PossibleValue] = Nil,
                       renderHint: Option[RenderHint] = None, enabled: Boolean = true,
                       globalOptions: GlobalOptions = GlobalOptions.default) extends Field {
  val `type` = MultiEditField.NAME

  def extractValue: Option[(String, js.Any)] = {
    Some(name -> this.value)
  }

  def listChange(items: List[String]):Unit = {
//    this.possibleValues = items.zipWithIndex.map(item => PossibleValue(id = item._2.toString,label = item._1))
    this.value = items
    onChange(this)
  }

  def render(parentRenderHint: Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {
    val id = idGenerator.getId

    <.div(^.className := "form-group" + this.getErrorClass,
      renderInputBody(id, parentRenderHint,
          MultiEdit(value, listChange)
      )
    )
  }

}


object MultiEditField {
  val NAME = "multiedit"

  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): MultiEditField = {
    //    println(NAME)
    var field = new MultiEditField(name = "", onChange = onChange, globalOptions = globalOptions)
    for ((fname, value) <- json) {
      fname match {
        case "label" =>
          field = field.copy(label = value.asInstanceOf[String])
        case "name" =>
          field = field.copy(name = value.asInstanceOf[String])
        case "type" =>

        case "enabled" =>
          field = field.copy(enabled = value.asInstanceOf[Boolean])
        case "path" =>
          field = field.copy(path = value.asInstanceOf[String])
        case "value" =>
          val values = value match {
            case null => Nil
            case ja: js.Array[_] =>
              value.asInstanceOf[js.Array[String]].toList
            case i: String => List(i)

          }
          field = field.copy(value = values)
        case "empty_value" =>
          val values = value match {
            case null => Nil
            case ja: js.Array[_] =>
              value.asInstanceOf[js.Array[String]].toList
            case i: String => List(i)

          }
          field = field.copy(emptyValue = values)
        case "multiple" =>
          field = field.copy(multiple = value.asInstanceOf[Boolean])
        case "possible_values" =>

          field = field.copy(possibleValues = value.asInstanceOf[js.Array[Any]].map(v => PossibleValue.fromJson(v.asInstanceOf[js.Dictionary[Any]])).toList)
        case "validate" =>
          field = field.copy(validate = Validation.fromJson[List[Int]](value.asInstanceOf[js.Dictionary[Any]]))
        case "render_hint" =>
          field = field.copy(renderHint = Some(RenderHint.fromJson(value.asInstanceOf[js.Dictionary[Any]])))

        //        case default=>
        //          println(s"$default $value")
      }
    }
    field
  }

}