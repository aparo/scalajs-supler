/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields

import app.utils.IDGenerator
import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.vdom.prefix_<^._
import app.components.Panel
import supler._
import japgolly.scalajs.react._

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import app.utils.ExtJSON._

case class SubFormValue(fields: List[Field] = Nil, fieldOrder: List[List[String]] = Nil,
                        id: String = "", globalOptions: GlobalOptions) extends GridSystem{

  def render(renderHint: Option[RenderHint] = None)(implicit idGenerator: IDGenerator): TagMod = {

    def renderTable: TagMod = {
      <.tr(
        fields.map(v => <.td(v.render(renderHint)))
      )
    }

    def renderList: TagMod = {
      fieldOrder.map(fo =>
        fo.map(field =>
          <.li(^.cls := getClassGridSystem(12, fo.length),
            fields.find(_.name == field).map(_.render(renderHint))
          )
        )
      )
    }

    def renderDefault: TagMod = {
      <.div(^.cls := "form-group",
        fields.map(_.render(renderHint))
      )
    }

    renderHint match {
      case Some(rh) =>
        rh.name match {
          case "table" =>
            renderTable
          case "list" =>
            renderList
          case _ =>
            println(s"SubFormValue unknown $renderHint using default")
            renderDefault

        }
      case None =>
        renderDefault
    }
  }

  def extractValue: js.Any = js.Dictionary(fields.flatMap(_.extractValue).toList: _*)

}

object SubFormValue {
  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): SubFormValue = {
    var field = new SubFormValue(globalOptions = globalOptions)

    for ((fname, value) <- json) {
      fname match {
        case "fields" =>

          val fieldsJson = value.asInstanceOf[js.Array[js.Dictionary[Any]]]
          val fields = fieldsJson.map(f => Field.fromJson(f, onChange = onChange, globalOptions = globalOptions))
          field = field.copy(fields = fields.toList)
        case "fieldOrder" =>
          field = field.copy(fieldOrder = value.asInstanceOf[js.Array[js.Array[String]]].map(f => f.toList).toList)

        case "id" =>
          field = field.copy(id = value.asInstanceOf[String])
        case "options" =>

      }
    }
    field
  }

}

case class SubFormField(name: String, onChange: (Field => Unit), label: String = "", value: List[SubFormValue] = Nil, path: String = "",
                        multiple: Boolean = false, renderHint: Option[RenderHint] = None,
                        enabled: Boolean = true, globalOptions: GlobalOptions = GlobalOptions.default) extends Field {
  val `type`: String = SubFormField.NAME

  def subRenderTable(implicit idGenerator: IDGenerator): TagMod = {
    <.table(^.cls := "table",
      <.tbody(
        <.tr(value.headOption match {
          case Some(sv) =>
            sv.fields.filter(_.enabled).map(f => <.th(f.label))
          case _ =>
            <.th("")
        }),
        value.map(_.render(renderHint))
      )
    )
  }

  def subRenderList(implicit idGenerator: IDGenerator): TagMod = {
    if (value.nonEmpty) {
      val items = value.init.flatMap(v => List(v.render(renderHint), <.hr())) :+ value.last.render(renderHint)
      <.ul(^.cls := "list-unstyled",
        items
      )
    } else
      EmptyTag
  }

  def subRenderDefault(implicit idGenerator: IDGenerator): TagMod = {
    <.div(^.cls := "well",
      value.map(_.render(this.renderHint))
    )
  }

  def render(parentRenderHint: Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {
    val id = idGenerator.getId

    val items = renderHint match {
      case Some(rh) =>
        rh.name match {
          case "table" => subRenderTable
          case "list" => subRenderList
          case _ =>
            println(s"SubFormField unknown $renderHint using default")
            subRenderDefault
        }
      case None =>
        subRenderDefault
    }

    renderHint match {
      case Some(rh) =>
        rh.collapsible match {
          case Some(coll) if coll =>
            Panel(collapsable = true)(label)(
              items
            )
          case _ =>
            <.fieldset(^.id := id,
              <.legend(label,
                items
              )
            )
        }
      case None =>
        <.fieldset(^.id := id,
          <.legend(label,
            items
          )
        )
    }
  }

  def extractValue: Option[(String, js.Any)] = {
    val value = this.value.map(_.extractValue)
    if(this.multiple)
      Some(name -> value.toJsArray)
    else {
      Some(name -> value.head)
    }

  }

}

object SubFormField {
  val NAME = "subform"

  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): SubFormField = {
    var field = new SubFormField("", onChange = onChange, globalOptions = globalOptions)
    for ((fname, value) <- json) {
      fname match {
        case "label" =>
          field = field.copy(label = value.asInstanceOf[String])
        case "name" =>
          field = field.copy(name = value.asInstanceOf[String])
        case "enabled" =>
          field = field.copy(enabled = value.asInstanceOf[Boolean])
        case "type" =>

        case "path" =>
          field = field.copy(path = value.asInstanceOf[String])
        case "value" =>
          val valueList = value match {
            case elem:js.Array[_] =>
              elem.map {
                v =>
                  SubFormValue.fromJson(v.asInstanceOf[js.Dictionary[Any]], onChange = onChange, globalOptions = globalOptions)
              }.toList
            case obj:js.Object =>
              List(SubFormValue.fromJson(obj.asInstanceOf[js.Dictionary[Any]], onChange = onChange, globalOptions = globalOptions))
          }


          //          println(s"valueList $valueList")
          field = field.copy(value = valueList)
        case "multiple" =>
          field = field.copy(multiple = value.asInstanceOf[Boolean])
        case "render_hint" =>
          field = field.copy(renderHint = Some(RenderHint.fromJson(value.asInstanceOf[js.Dictionary[Any]])))
      }
    }
    field
  }

}
