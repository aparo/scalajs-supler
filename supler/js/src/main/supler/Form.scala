/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler

import app.utils.IDGenerator
import japgolly.scalajs.react.ReactElement

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import app.utils.ExtJSON._
import japgolly.scalajs.react.vdom.prefix_<^._

case class Form(id: String, fields: List[Field] = Nil, fieldOrder:List[List[String]]=Nil,
                renderHint: Option[RenderHint] = None, meta:js.Dictionary[Any]=js.Dictionary[Any](),
                isSuplerForm:Boolean=false) extends GridSystem{
  def render(implicit idGenerator: IDGenerator): ReactElement = {
    <.form(id.nonEmpty ?= (^.id := id), //^.cls := "form-horizontal",
      <.div(^.cls := "div-container-fluid",
        fieldOrder.map(fo =>
          <.div(^.cls := "row",
            fo.map(field =>
              <.div(^.cls := getClassGridSystem(12, fo.length),
                fields.find(_.name == field).map(_.render(renderHint))
              )
            )
          )
//          fields.map(_.render(renderHint))
        )
      )
    )
  }

  def extractValue: js.Dictionary[js.Any] = {
    var values = fields.flatMap(_.extractValue)
    values ::= ("id" -> id)
    js.Dictionary(values: _*)
  }
}

object Form {
  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions=GlobalOptions()): Form = {
    var gl=globalOptions.updateErrors(json.asListDict("errors"))

    val mainFormJSON=json.asObj("main_form")

    val fieldsJson = mainFormJSON.as[js.Array[js.Dictionary[Any]]]("fields", js.Array[js.Dictionary[Any]]())
    val fields = fieldsJson.map(f=> Field.fromJson(f, onChange = onChange, globalOptions=gl))

    val fieldOrderJson = mainFormJSON.as[js.Array[js.Array[String]]]("fieldOrder", js.Array[js.Array[String]]())
    val fieldOrder = fieldOrderJson.map(f=> f.toList).toList

    new Form(
      id = mainFormJSON.as[String]("id", ""),
      fields = fields.toList,
      fieldOrder=fieldOrder,
      isSuplerForm=json.as[Boolean]("is_supler_form", false),
      meta=json.asObj("supler_meta")
    )
  }

}


trait Field {
  def name: String

  def `type`: String

  def label: String

  def path: String

  def enabled: Boolean

  def onChange: (Field => Unit)

  def render(parentRenderHint: Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod

  def extractValue: Option[(String, js.Any)]

  def globalOptions:GlobalOptions

  //returns if the field should render the label
  def renderInputBody(id:String, parentRenderHint: Option[RenderHint], body:TagMod, skipLabel:Boolean=false):List[TagMod]={
    val items=new ListBuffer[TagMod]()
    if(!skipLabel && shouldRenderLabel(parentRenderHint))
      items += renderLabel(id, parentRenderHint)

    items += body

    if(globalOptions.hasError(this.path))
      items += getErrorsDiv

    items.toList
  }

  def renderLabel(id:String, renderHint: Option[RenderHint]):ReactElement={
    <.label(^.`for` := id, label)
  }

    //returns if the field should render the label
  def shouldRenderLabel(renderHint: Option[RenderHint]):Boolean={
    renderHint match {
      case Some(rh) =>
        rh.name match {
          case "table"=>false
          case _ => true
        }
      case None =>
        true
    }
  }

  def getErrorClass=if(globalOptions.hasError(this.path)) " has-error" else ""

  def getErrorsDiv:ReactElement = {
    val errors=globalOptions.errors.filter(_.fieldPath==this.path)
    <.div(^.cls := "text-danger", errors.map(_.errorKey))
  }
//  <div class="text-danger" id="id4">Value is required</div>

}

object Field {
  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): Field = {
    //    println(name)
    val kind = json.apply("type").asInstanceOf[String]
    import supler.fields._
    kind match {
      case StringField.NAME => StringField.fromJson(json, onChange, globalOptions=globalOptions)
      case BooleanField.NAME => BooleanField.fromJson(json, onChange, globalOptions=globalOptions)
      case FloatField.NAME => FloatField.fromJson(json, onChange, globalOptions=globalOptions)
      case IntegerField.NAME => IntegerField.fromJson(json, onChange, globalOptions=globalOptions)
      case SelectField.NAME => SelectField.fromJson(json, onChange, globalOptions=globalOptions)
      case MultiEditField.NAME => MultiEditField.fromJson(json, onChange, globalOptions=globalOptions)
      case SubFormField.NAME => SubFormField.fromJson(json, onChange, globalOptions=globalOptions)
      case ActionField.NAME => ActionField.fromJson(json, onChange, globalOptions=globalOptions)
      case StaticField.NAME => StaticField.fromJson(json, onChange, globalOptions=globalOptions)
    }

  }

}

trait GridSystem{
  def getClassGridSystem(numberColumns: Int, numberFieldsRow: Int): String = {
    val baseClass = "col-md-"
    numberFieldsRow match {
      case 0 =>  baseClass + numberColumns
      case _ => {
                  val value = Math floor numberColumns / numberFieldsRow
                  value match {
                    case 0 => baseClass + 1
                    case _ => baseClass + value
                  }
                }
    }
  }
}