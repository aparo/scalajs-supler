/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields

import app.utils.IDGenerator
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLSelectElement
import supler._

import scala.scalajs.js

case class SelectField(name: String, onChange: (Field => Unit), label: String = "", validate: Validation[List[Int]] = Validation[List[Int]](), path: String = "",
                       var value: List[String] = Nil, multiple: Boolean = false, emptyValue: List[String] = Nil,
                       possibleValues: List[PossibleValue] = Nil,
                       renderHint: Option[RenderHint] = None, enabled: Boolean = true,
                       globalOptions: GlobalOptions = GlobalOptions.default) extends Field {
  val `type` = SelectField.NAME

  def updateSelectChange(e: ReactEventI) = {
    this.value = List(e.currentTarget.asInstanceOf[HTMLSelectElement].selectedIndex.toString)
    onChange(this)
  }

  def renderSelect(id: String): TagMod = {
    if (value.isEmpty && possibleValues.nonEmpty) value = List(this.possibleValues.head.id)
    <.select(^.id := id, ^.name := name, ^.className := "form-control", ^.onChange ==> updateSelectChange, ^.value:=value.headOption,
      possibleValues.map {
        pValue =>
          <.option(^.value := pValue.id, this.value.contains(pValue.id) ?= (^.selected := "selected"), pValue.label)
      }
    )
  }

  def extractValue: Option[(String, js.Any)] = {
    println(s"selectfield $name $value")
    if (multiple)
      Some(name -> this.value.toJsArray)
    else
    if (this.value.nonEmpty)
      Some(name -> this.value.head)
    else
      None
  }

  def updateCheckBoxChange(id: String)(e: ReactEventI) = {
    if (e.currentTarget.checked) {
      this.value ::= id
    } else {
      this.value = this.value.filterNot(_ == id)
    }
    onChange(this)
  }

  def renderCheckbox(id: String): TagMod = {
    possibleValues.zipWithIndex.map {
      case (pValue, pos) =>
        <.div(^.className := "form-group" + this.getErrorClass,
          <.label(
            <.input(^.`type` := "checkbox", ^.value := pValue.id, ^.id := "id15." + pValue.id,
              ^.onChange ==> updateCheckBoxChange(pValue.id),
              ^.name := name, if (this.value.contains(pValue.id)) ^.checked := "checked" else ""),
              <.span(" ", pValue.label)
          )
        )
    }

  }

  def render(parentRenderHint: Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {
    val id = idGenerator.getId

    <.div(^.className := "form-group" + this.getErrorClass,
    renderInputBody(id, parentRenderHint,
      renderHint match {
        case Some(renderH) =>
          renderH.name match {
            case "checkbox" =>
              renderCheckbox(id)
            case _ =>
                renderSelect(id)
          }
        case None =>
          renderSelect(id)
      }
    )
    )
  }

}


object SelectField {
  val NAME = "select"

  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): SelectField = {
    //    println(NAME)
    var field = new SelectField(name = "", onChange = onChange, globalOptions = globalOptions)
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