/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields

import app.components.AutoCompleteComboBox
import app.utils.IDGenerator
import japgolly.scalajs.react.{ReactEventI, ReactElement}
import japgolly.scalajs.react.vdom.prefix_<^._
import supler._
import web.icons.FontAwesome

import scala.scalajs.js


case class StringField(name: String, onChange: (Field => Unit), label: String = "", validate: Validation[String] = Validation[String](), path: String = "",
                       var value: String = "", emptyValue: Option[String] = None,
                       renderHint: Option[RenderHint] = None,
                       enabled: Boolean = true, globalOptions: GlobalOptions = GlobalOptions.default) extends Field {
  val `type` = StringField.NAME

  def updateValue(e: ReactEventI) = {
    this.value = e.currentTarget.value
    onChange(this)
  }

  def updateTextValue(value:String): Unit ={
    this.value=value
    onChange(this)
  }

  def extractValue: Option[(String, js.Any)] = Some(name -> this.value)


  def render(parentRenderHint: Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {
    val id = idGenerator.getId
    var properties = List(^.id := id, ^.name := this.name,
      ^.className := "form-control", ^.onChange ==> this.updateValue)
    if (value.nonEmpty)
      properties ::= (^.defaultValue := value)
    else if (emptyValue.isDefined)
      properties ::= (^.defaultValue := emptyValue.get)

    <.div(^.className := "form-group" + this.getErrorClass,
      renderInputBody(id, parentRenderHint,
        renderHint match {
          case Some(renderHint) =>
            renderHint.name match {
              case "password" =>
                properties ::= (^.`type` := "password")
                <.input(properties: _*)
              case "textarea" =>
                properties ::= (^.rows := renderHint.rows.getOrElse(3))
                <.textarea(properties: _*)
              case "radio" =>
                properties ::= (^.`type` := "text")
                <.input(properties: _*)
              case "hidden" =>
                properties ::= (^.`type` := "hidden")
                <.input(properties: _*)
              case "icon" =>
//                properties ::= (^.`type` := "text")
                AutoCompleteComboBox(items = FontAwesome.entries.map(v => AutoCompleteComboBox.Entry(v,v, <.div(<.i(^.cls:=v)," ", v))),
                  onInput=this.updateTextValue, onSelect = this.updateValue,  initialValue=this.value)

//                <.select(^.id := id, ^.name := this.name, ^.className := "form-control", ^.onChange ==> this.updateValue,
//                  FontAwesome.entries.map {
//                    pValue =>
//                      <.option(^.value := pValue, (this.value == pValue) ?= (^.selected := "selected"), <.i(^.cls:=pValue), pValue)
//                  }
//                )
            }
          case None =>
            properties ::= (^.`type` := "text")
            <.input(properties: _*)
        }

      )
    )
  }
}


object StringField {
  val NAME = "string"

  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): StringField = {


    var field = new StringField("", onChange = onChange, globalOptions = globalOptions)
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
          field = field.copy(value = value.asInstanceOf[String])
        case "empty_value" =>
          field = field.copy(emptyValue = Some(value.asInstanceOf[String]))
        case "validate" =>
          field = field.copy(validate = Validation.fromJson[String](value.asInstanceOf[js.Dictionary[Any]]))
        case "render_hint" =>
          field = field.copy(renderHint = Some(RenderHint.fromJson(value.asInstanceOf[js.Dictionary[Any]])))
      }
    }
    field
  }

}