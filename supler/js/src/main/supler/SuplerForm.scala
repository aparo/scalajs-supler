/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler

import app.utils.IDGenerator
import echidna.Client
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import supler.fields.ActionField
import web.icons.FontAwesome

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.concurrent.ExecutionContext.Implicits.global
import app.utils.ExtAjax._
import web._

object SuplerForm {

  case class State(form: Option[Form] = None,
                   globalOptions: GlobalOptions = GlobalOptions.default)

  class Backend(t: BackendScope[Props, State]) {

    def getUrl(moduleName: Option[String] = None, formName: Option[String] = None, id: Option[String] = None): String = {
      var url = s"${Client.baseUrl}/form/${moduleName.getOrElse(t.props.moduleName)}/${formName.getOrElse(t.props.formName)}"
      val currId = id.getOrElse(t.props.id)
      if (currId.nonEmpty)
        url = s"$url/${currId}"
      url
    }

    def onTextChange(e: SyntheticEvent[html.Input]) = {
      //      t.modState(s=>s.copy(filteredModels = getFilteredModels(e.target.value, s.allModels), offset = 0, filterText=e.target.value))

    }

    private def parseForm(jsonString: String): Option[Form] = {
      val json = JSON.parse(jsonString).asInstanceOf[js.Dictionary[Any]]
      var globalOptions = t.state.globalOptions
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
      println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
      println(JSON.stringify(t.state.form.get.extractValue))
      println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
      val serverUpdate = true//changedItem.`type` == ActionField.NAME
//      println(s"serverUpdate $serverUpdate")
      if (serverUpdate && t.state.form.isDefined) {
        val url = getUrl()
        Ajax.putAsJson(url, JSON.stringify(t.state.form.get.extractValue)).foreach {
          xhr =>
            println(xhr.responseText)
            parseForm(xhr.responseText)
        }
      }
    }

    def save(): Unit = {
      if (t.props.canSave && t.state.form.isDefined) {
        val formCopy = t.state.form
        val url = getUrl()
        Ajax.postAsJson(url, JSON.stringify(t.state.form.get.extractValue)).foreach {
          xhr =>
//            println(s"Saved: ${xhr.responseText}")
            parseForm(xhr.responseText)  match {
              case Some(f) => //ok data was not ok
              case None =>
                if(t.props.onSaved!=null)
                  t.props.onSaved(JSON.parse(xhr.responseText))
            }
            //println("after_parsed>>>")
            //println("form>>>"+formCopy)
            if(t.props.keepValues) {
                t.modState(_.copy(form = formCopy))
            }
            //if form is None =>

        }
      }
    }


    def refresh(moduleName: String = "", formName: String = "", id: String = ""): Unit = {
      //in future manage also id
      val url =
        if (moduleName.nonEmpty && formName.nonEmpty) getUrl(Some(moduleName), Some(formName), if (id.isEmpty) None else Some(id))
        else getUrl()
      Ajax.get(url).foreach {
        xhr =>
          parseForm(xhr.responseText)
      }
    }
  }

  val component = ReactComponentB[Props]("SuplerForm")
    .initialStateP(P => State(globalOptions = P.globalOptions))
    .backend(new Backend(_))
    .render((P, S, B) => {
    implicit val idGenerator = new IDGenerator(P.formName)
    <.div(
      if (S.form.isDefined) S.form.get.render else <.div(s"Loading form: ${P.moduleName}/${P.formName}/${P.id}")
      ,(P.canSave && S.form.isDefined) ?= <.button(^.cls := buttonActionClass, ^.onClick --> B.save(), FontAwesome.save, " ", "Save")
    )
  }).componentDidMount(scope => {
    if (scope.isMounted())
      scope.backend.refresh()
  }).componentWillReceiveProps((scope, newProps) => {
    if (scope.props != newProps){
      if(!scope.props.keepValues) {
      scope.backend.refresh(newProps.moduleName, newProps.formName, newProps.id)
      scope.modState(_.copy(form=None))
    }
    }
  })
    .build

  case class Props(moduleName: String, formName: String, id: String, onChange: Any => Boolean,
                   canSave:Boolean, keepValues:Boolean, onSaved:(js.Any =>Unit), globalOptions: GlobalOptions = GlobalOptions.default)

  def apply(moduleName: String, formName: String, id: String = "",
            onChange: Any => Boolean = null, canSave:Boolean=true, keepValues:Boolean=false, onSaved:(js.Any =>Unit)) =
    component(Props(moduleName, formName, id = id, onChange = onChange, canSave=canSave, keepValues=keepValues, onSaved = onSaved))
}

