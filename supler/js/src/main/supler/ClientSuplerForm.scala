/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import app.utils.IDGenerator
import org.scalajs.dom.html
import play.api.libs.json.Json
import web._
import web.icons.FontAwesome

import scala.scalajs.js
import scala.scalajs.js.JSON

object ClientSuplerForm {

  case class State[T](suplerForm: org.supler.FormWithObject[T], form: Option[Form] = None,
                      globalOptions: GlobalOptions = GlobalOptions.default)

  class Backend[T](t: BackendScope[Props[T], State[T]]) {

    def onTextChange(e: SyntheticEvent[html.Input]) = {
      //      t.modState(s=>s.copy(filteredModels = getFilteredModels(e.target.value, s.allModels), offset = 0, filterText=e.target.value))

    }

    private def parseForm(jsonString: String): Option[Form] = {
      val json = JSON.parse(jsonString).asInstanceOf[js.Dictionary[Any]]
      val globalOptions = t.state.globalOptions
      val keys = json.keySet.toList
      if (keys.contains("is_supler_form")) {
        //        keys.filter(_ != "main_form").foreach{
        //          case "errors" =>
        //            globalOptions = globalOptions.updateErrors(json.apply("errors").asInstanceOf[js.Array[Any]])
        //          case "is_supler_form" | "main_form" | "custom_data" =>
        //          case default =>
        //            println(s"SuplerFrom unprocessed field: $default")
        //        }
        val form = Form.fromJson(json.asInstanceOf[js.Dictionary[Any]], onChange = this.onChange, globalOptions = globalOptions)
        t.modState(s => s.copy(form = Some(form)))
        Some(form)
      } else {
        t.modState(s => s.copy(form = None))
        None
      }
    }

    def onChange(changedItem: Field): Unit = {
      if (t.props.onChange != null) {
        t.props.onChange(changedItem)
      }
      //t.modState(s => s.copy(form = s.form))
//      println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
//      println(JSON.stringify(t.state.form.get.extractValue))
//      println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
      val serverUpdate = true //changedItem.`type` == ActionField.NAME
      //      println(s"serverUpdate $serverUpdate")
      if (serverUpdate && t.state.form.isDefined) {
        val validatedForm = t.state.suplerForm.applyJSONValues(Json.parse(JSON.stringify(t.state.form.get.extractValue))).doValidate()
        if (!validatedForm.hasErrors && t.props.onValid != null) {
          t.props.onValid(validatedForm.obj)
        }
        parseForm(Json.stringify(validatedForm.generateJSON))
      }
    }

    def save(): Unit = {
      if (t.props.canSave && t.state.form.isDefined) {
        val validatedForm = t.state.suplerForm.applyJSONValues(Json.parse(JSON.stringify(t.state.form.get.extractValue))).doValidate()
        if (validatedForm.hasErrors) {
          parseForm(Json.stringify(validatedForm.generateJSON))
        } else {
          if (t.props.onSaved != null)
            t.props.onSaved(validatedForm.obj)
        }
      }
    }


    def refresh(form: NativeForm[T], defaultValue: Option[T] = None): Option[Form] = {
      //in future manage also id
      val suplerForm = form.form(defaultValue.getOrElse(form.default))
      parseForm(Json.stringify(suplerForm.generateJSON))
    }
  }

  def component[T] = ReactComponentB[Props[T]]("ClientSuplerForm")
    .initialStateP {
    P =>
      State[T](globalOptions = P.globalOptions, suplerForm = P.form.form(P.defaultValue.getOrElse(P.form.default)))
  }
    .backend(new Backend[T](_))
    .render((P, S, B) => {
    implicit val idGenerator = new IDGenerator("")
    <.div(
      if (S.form.isDefined) S.form.get.render else <.div(s"Loading form")
      , (P.canSave && S.form.isDefined) ?= <.button(^.cls := buttonActionClass, ^.onClick --> B.save(), P.saveIcon, " ", P.saveText)
    )
  }).componentDidMount(scope => {
    scope.backend.refresh(scope.props.form, scope.props.defaultValue)
  }).componentWillReceiveProps((scope, newProps) => {
    if (scope.props != newProps) {
      val form = scope.backend.refresh(newProps.form, newProps.defaultValue)
      scope.modState(_.copy(form = form))
    }
  })
    .build

  case class Props[T](form: NativeForm[T], defaultValue: Option[T] = None, onChange: Any => Boolean,
                      canSave: Boolean, onSaved: (T => Unit), globalOptions: GlobalOptions = GlobalOptions.default, onValid: (T => Unit) = null,
                      saveText: String = "save", saveIcon: Icon)

  def apply[T](form: NativeForm[T], defaultValue: Option[T] = None, onSaved: (T => Unit) = null,
               onChange: Any => Boolean = null, canSave: Boolean = true, onValid: (T => Unit) = null,
               saveText: String = "save", saveIcon: Icon = FontAwesome.save) =
    component(Props[T](form = form, defaultValue = defaultValue,
      onChange = onChange, canSave = canSave, onSaved = onSaved,
      onValid = onValid, saveText = saveText, saveIcon = saveIcon))
}
