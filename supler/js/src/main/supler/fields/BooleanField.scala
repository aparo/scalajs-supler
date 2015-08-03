/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields

import app.utils.IDGenerator
import japgolly.scalajs.react._
import supler._

import scala.scalajs.js
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.util.Try

case class BooleanField(name: String, onChange:(Field=>Unit), label: String = "", validate: Validation[Boolean] = Validation[Boolean](), path: String = "",
                        var value: Option[Boolean] = None, emptyValue: Option[Boolean] = None,
                        renderHint: Option[RenderHint] = None, enabled: Boolean = true,
                        globalOptions:GlobalOptions=GlobalOptions.default) extends Field {
  val `type` = BooleanField.NAME

  def updateValueTrue(e: ReactEventI) = {
    this.value = Some(e.currentTarget.checked)
    onChange(this)
  }

  def updateValueFalse(e: ReactEventI) = {
    this.value = Some(!e.currentTarget.checked)
    onChange(this)
  }

  def extractValue: Option[(String, js.Any)] = this.value match {
    case Some(v) => Some(name -> v)
    case _ => None
  }


  def render(parentRenderHint:Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {
    val id = idGenerator.getId
    val currentRenderingHint=renderHint.getOrElse(RenderHint("checkbox"))

    <.div(^.className := "form-group" + this.getErrorClass,
      renderInputBody(id, parentRenderHint,
        currentRenderingHint.name match {
      case "radio" =>
        <.span(^.id := id,
          <.div(^.className := "radio",
            <.label(
              <.input(^.`type` := "radio", ^.value := 1, /*^.id := id, */^.name := name,
                ^.onChange ==> updateValueTrue,
                (value.isDefined && value.get) ?= (^.checked := "checked")
              ), <.label("Yes")
            )
          ),
          <.div(^.className := "radio",
            <.label(
              <.input(^.`type` := "radio", ^.value := 0, /*^.id := id,*/ ^.name := name,
                ^.onChange ==> updateValueFalse,
                (value.isDefined && !value.get) ?= (^.checked := "checked")),
              <.label("No")
            )
          )
        )
      case _ =>
        <.div(
          <.label(
            <.input(^.`type` := "checkbox", ^.value := value.getOrElse(false),
              ^.onChange ==> updateValueTrue,
              ^.name := name, if (value.getOrElse(false)) ^.checked := "checked" else ""),
              <.span(" ", label)
          )

        )

    },
      skipLabel = currentRenderingHint.name=="checkbox"


      )
    )
  }
}


object BooleanField {
  val NAME = "boolean"

  def fromJson(json: js.Dictionary[Any], onChange:(Field=>Unit), globalOptions: GlobalOptions): BooleanField = {

    var field = new BooleanField(name = "", onChange=onChange, globalOptions=globalOptions)
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
          field = field.copy(value = Some(value.asInstanceOf[Boolean]))
        case "empty_value" =>
          field = field.copy(emptyValue = Some(value.asInstanceOf[Boolean]))
        case "validate" =>
          field = field.copy(validate = Validation.fromJson[Boolean](value.asInstanceOf[js.Dictionary[Any]]))
        case "render_hint" =>
          field = field.copy(renderHint = Some(RenderHint.fromJson(value.asInstanceOf[js.Dictionary[Any]])))
      }
    }
    field
  }

}