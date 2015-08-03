/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields

import app.utils.IDGenerator
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import supler._

import scala.scalajs.js
import scala.util.Try


case class IntegerField(name: String, onChange: (Field => Unit), label: String = "", validate: Validation[Int] = Validation[Int](), path: String = "",
                        var value: Option[Int] = None, emptyValue: Option[Int] = None,
                        renderHint: Option[RenderHint] = None, enabled: Boolean = true,
                        globalOptions: GlobalOptions = GlobalOptions.default) extends Field {
  val `type` = IntegerField.NAME

  def tryToInt(s: String) = Try(s.toInt).toOption

  def updateValue(e: ReactEventI) = {
    this.value = tryToInt(e.currentTarget.value)
    onChange(this)
  }

  def extractValue: Option[(String, js.Any)] = this.value match {
    case Some(v) => Some(name -> v)
    case _ => null
  }


  def render(parentRenderHint: Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {
    val id = idGenerator.getId
    var properties = List(^.`type` := "number", ^.id := id, ^.name := this.name, ^.className := "form-control",
      ^.onChange ==> this.updateValue)
    //    println(s"Integer Render $this")
    value match {
      case Some(vAl) =>
        properties ::= (^.value := vAl.toString)
      case None =>
        if (emptyValue.isDefined)
          properties ::= (^.value := emptyValue.get.toString)
    }

    <.div(^.className := "form-group" + this.getErrorClass,
      renderInputBody(id, parentRenderHint,
        <.input(properties: _*)
      )
    )

  }
}


object IntegerField {

  val NAME = "integer"

  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): IntegerField = {
    //    println(NAME)
    var field = new IntegerField(name = "", onChange = onChange, globalOptions = globalOptions)
    for ((fname, value) <- json) {
      fname match {
        case "label" =>
          field = field.copy(label = value.asInstanceOf[String])
        case "name" =>
          field = field.copy(name = value.asInstanceOf[String])
        case "type" =>

        case "path" =>
          field = field.copy(path = value.asInstanceOf[String])
        case "enabled" =>
          field = field.copy(enabled = value.asInstanceOf[Boolean])
        case "value" =>
          field = field.copy(value = Some(value.asInstanceOf[Int]))
        case "empty_value" =>
          field = field.copy(emptyValue = Some(value.asInstanceOf[Int]))
        case "validate" =>
          field = field.copy(validate = Validation.fromJson[Int](value.asInstanceOf[js.Dictionary[Any]]))
        case "render_hint" =>
          field = field.copy(renderHint = Some(RenderHint.fromJson(value.asInstanceOf[js.Dictionary[Any]])))
      }
    }
    field
  }

}