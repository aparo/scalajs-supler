/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.field

import org.supler.validation._
import org.supler.{FieldPath, Form, Id, Util}
import play.api.libs.json._

case class SubformField[T, ContU, U, Cont[_]](
  c: SubformContainer[ContU, U, Cont],
  name: String,
  read: T => Cont[U],
  write: (T, Cont[U]) => T,
  label: Option[String],
  description: Option[String],
  embeddedForm: Form[U],
  // if not specified, `embeddedForm.createEmpty` will be used
  createEmpty: Option[() => U],
  renderHint: RenderHint with SubformFieldCompatible,
  enabledIf: T => Boolean,
  includeIf: T => Boolean) extends Field[T] {
  
  import c._
  
  def label(newLabel: String): SubformField[T, ContU, U, Cont] = this.copy(label = Some(newLabel))
  def description(newDescription: String): SubformField[T, ContU, U, Cont] = this.copy(description = Some(newDescription))

  def renderHint(newRenderHint: RenderHint with SubformFieldCompatible): SubformField[T, ContU, U, Cont] = this.copy(renderHint = newRenderHint)

  def enabledIf(condition: T => Boolean): SubformField[T, ContU, U, Cont] = this.copy(enabledIf = condition)
  def includeIf(condition: T => Boolean): SubformField[T, ContU, U, Cont] = this.copy(includeIf = condition)

  private[supler] def generateFieldJSON(parentPath: FieldPath, obj: T) = {
    val valuesAsJValue = read(obj).zipWithIndex.map { case (v, indexOpt) =>
      embeddedForm.generateJSON(pathWithOptionalIndex(parentPath, indexOpt), v)
    }

    import JSONFieldNames._
    Json.obj(
      Type -> JsString(SpecialFieldTypes.Subform),
      RenderHint -> JsObject(Seq("name" -> JsString(renderHint.name)) ++ renderHint.extraJSON),
      Multiple -> JsBoolean(c.isMultiple),
      Label -> JsString(label.getOrElse("")),
      Path -> JsString(parentPath.append(name).toString),
      Value -> c.combineJValues(valuesAsJValue)
    )
  }

  override private[supler] def applyFieldJSONValues(parentPath: FieldPath, obj: T, jsonFields: Map[String, JsValue]): PartiallyAppliedObj[T] = {
    def valuesWithIndex = c.valuesWithIndexFromJSON(jsonFields.get(name))
    val paos = valuesWithIndex.map { case (formJValue, indexOpt) =>
      embeddedForm.applyJSONValues(pathWithOptionalIndex(parentPath, indexOpt),
        createEmpty.getOrElse(embeddedForm.createEmpty)(), formJValue)
    }

    c.combinePaos(paos).map(write(obj, _))
  }

  override private[supler] def doValidate(parentPath: FieldPath, obj: T, scope: ValidationScope) = {
    val valuesWithIndex = read(obj).zipWithIndex

    val errorLists = valuesWithIndex.map { case (el, indexOpt) =>
      embeddedForm.doValidate(pathWithOptionalIndex(parentPath, indexOpt), el, scope)
    }

    errorLists.toList.flatten
  }

  override private[supler] def findAction(
    parentPath: FieldPath,
    obj: T,
    jsonFields: Map[String, JsValue],
    ctx: RunActionContext) = {

    val values = read(obj)
    val valuesList = read(obj).toList
    val jvaluesWithIndex = c.valuesWithIndexFromJSON(jsonFields.get(name)).toList

    val valuesJValuesIndex = valuesList.zip(jvaluesWithIndex)

    Util
      .findFirstMapped[(U, (JsValue, Option[Int])), Option[RunnableAction]](valuesJValuesIndex, { case (v, (jvalue, indexOpt)) =>
        val i = indexOpt.getOrElse(0)
        val updatedCtx = ctx.push(obj, i, (v: U) => write(obj, values.update(v, i)))
        // assuming that the values matches the json (that is, that the json values were previously applied)
        embeddedForm.findAction(pathWithOptionalIndex(parentPath, indexOpt), valuesList(i), jvalue, updatedCtx)
      },
      _.isDefined).flatten
  }

  private def pathWithOptionalIndex(parentPath: FieldPath, indexOpt: Option[Int]) = indexOpt match {
    case None => parentPath.append(name)
    case Some(i) => parentPath.appendWithIndex(name, i)
  }
}

case class DynSubformField[T, ContU, U, Cont[_]](
                                               c: SubformContainer[ContU, U, Cont],
                                               name: String,
                                               read: T => Cont[U],
                                               write: (T, Cont[U]) => T,
                                               label: Option[String],
                                               description: Option[String],
                                               embeddedForm: U => Form[U],
                                               formFromJson: JsValue => Form[U],
                                               // if not specified, `embeddedForm.createEmpty` will be used
                                               createEmpty: Option[() => U],
                                               renderHint: RenderHint with SubformFieldCompatible,
                                               enabledIf: T => Boolean,
                                               includeIf: T => Boolean) extends Field[T] {

  import c._

  def label(newLabel: String): DynSubformField[T, ContU, U, Cont] = this.copy(label = Some(newLabel))
  def description(newDescription: String): DynSubformField[T, ContU, U, Cont] = this.copy(description = Some(newDescription))

  def renderHint(newRenderHint: RenderHint with SubformFieldCompatible): DynSubformField[T, ContU, U, Cont] = this.copy(renderHint = newRenderHint)

  def enabledIf(condition: T => Boolean): DynSubformField[T, ContU, U, Cont] = this.copy(enabledIf = condition)
  def includeIf(condition: T => Boolean): DynSubformField[T, ContU, U, Cont] = this.copy(includeIf = condition)

  private[supler] def generateFieldJSON(parentPath: FieldPath, obj: T) = {
    val valuesAsJValue = read(obj).zipWithIndex.map { case (v, indexOpt) =>
      embeddedForm(v).generateJSON(pathWithOptionalIndex(parentPath, indexOpt), v)
    }

    import JSONFieldNames._
    Json.obj(
      Type -> JsString(SpecialFieldTypes.Subform),
      RenderHint -> JsObject(Seq("name" -> JsString(renderHint.name)) ++ renderHint.extraJSON),
      Multiple -> JsBoolean(c.isMultiple),
      Label -> JsString(label.getOrElse("")),
      Path -> JsString(parentPath.append(name).toString),
      Value -> c.combineJValues(valuesAsJValue)
    )
  }

  override private[supler] def applyFieldJSONValues(parentPath: FieldPath, obj: T, jsonFields: Map[String, JsValue]): PartiallyAppliedObj[T] = {
    def valuesWithIndex = c.valuesWithIndexFromJSON(jsonFields.get(name))
    val paos = valuesWithIndex.map { case (formJValue, indexOpt) =>
      formFromJson(formJValue).applyJSONValues(pathWithOptionalIndex(parentPath, indexOpt),
        createEmpty.getOrElse( formFromJson(formJValue).createEmpty)(), formJValue)
    }

    c.combinePaos(paos).map(write(obj, _))
  }

  override private[supler] def doValidate(parentPath: FieldPath, obj: T, scope: ValidationScope) = {
    val valuesWithIndex = read(obj).zipWithIndex

    val errorLists = valuesWithIndex.map { case (el, indexOpt) =>
      embeddedForm(el).doValidate(pathWithOptionalIndex(parentPath, indexOpt), el, scope)
    }

    errorLists.toList.flatten
  }

  override private[supler] def findAction(
                                           parentPath: FieldPath,
                                           obj: T,
                                           jsonFields: Map[String, JsValue],
                                           ctx: RunActionContext) = {

    val values = read(obj)
    val valuesList = read(obj).toList
    val jvaluesWithIndex = c.valuesWithIndexFromJSON(jsonFields.get(name)).toList

    val valuesJValuesIndex = valuesList.zip(jvaluesWithIndex)

    Util
      .findFirstMapped[(U, (JsValue, Option[Int])), Option[RunnableAction]](valuesJValuesIndex, { case (v, (jvalue, indexOpt)) =>
      val i = indexOpt.getOrElse(0)
      val updatedCtx = ctx.push(obj, i, (v: U) => write(obj, values.update(v, i)))
      // assuming that the values matches the json (that is, that the json values were previously applied)
      embeddedForm(valuesList(i)).findAction(pathWithOptionalIndex(parentPath, indexOpt), valuesList(i), jvalue, updatedCtx)
    },
    _.isDefined).flatten
  }

  private def pathWithOptionalIndex(parentPath: FieldPath, indexOpt: Option[Int]) = indexOpt match {
    case None => parentPath.append(name)
    case Some(i) => parentPath.appendWithIndex(name, i)
  }
}

/**
 * The three type parameters are needed to extract the container type from the value. This is a bit more complicated
 * as we also want to support values without a container, then we need to artificially add the `Id` container.
 * @tparam ContU Container applied to a type. The type only serves as an example and doesn't matter.
 * @tparam U Example type to which the container is applied
 * @tparam Cont Type of the container
 */
trait SubformContainer[ContU, U, Cont[_]] {
  // operations on any value type
  def map[R, S](c: Cont[R])(f: R => S): Cont[S]
  def toList[R](c: Cont[R]): List[R]
  def update[R](cont: Cont[R])(v: R, i: Int): Cont[R]
  def zipWithIndex[R](values: Cont[R]): Cont[(R, Option[Int])]

  implicit class ContainerOps[R](c: Cont[R]) {
    def map[S](f: R => S) = SubformContainer.this.map(c)(f)
    def toList = SubformContainer.this.toList(c)
    def update(v: R, i: Int) = SubformContainer.this.update(c)(v, i)
    def zipWithIndex = SubformContainer.this.zipWithIndex(c)
  }

  // operations on specific types
  def valuesWithIndexFromJSON(jvalue: Option[JsValue]): Cont[(JsValue, Option[Int])]
  def combineJValues(jvalues: Cont[JsValue]): JsValue
  def combinePaos[R](paosInCont: Cont[PartiallyAppliedObj[R]]): PartiallyAppliedObj[Cont[R]]

  def isMultiple: Boolean
}

object SubformContainer {
  implicit def singleSubformContainer[U]: SubformContainer[U, U, Id] = new SubformContainer[U, U, Id] {
    def map[R, S](c: R)(f: (R) => S) = f(c)
    def toList[R](c: R) = List(c)
    def update[R](cont: R)(v: R, i: Int) = v
    def zipWithIndex[R](values: R) = (values, None)

    def valuesWithIndexFromJSON(jvalue: Option[JsValue]) = (jvalue.getOrElse(JsNull), None)
    def combineJValues(jvalues: JsValue) = jvalues
    def combinePaos[R](paosInCont: PartiallyAppliedObj[R]) = paosInCont

    def isMultiple = false
  }

  implicit def optionSubformContainer[U]: SubformContainer[Option[U], U, Option] = new SubformContainer[Option[U], U, Option] {
    def map[R, S](c: Option[R])(f: (R) => S) = c.map(f)
    def toList[R](c: Option[R]) = c.toList
    def zipWithIndex[R](values: Option[R]) = values.map((_, None))
    def update[R](cont: Option[R])(v: R, i: Int) = Some(v)

    def valuesWithIndexFromJSON(jvalue: Option[JsValue]) = jvalue.map((_, None))
    def combineJValues(jvalues: Option[JsValue]) = jvalues.getOrElse(JsNull)
    def combinePaos[R](paosInCont: Option[PartiallyAppliedObj[R]]) = paosInCont match {
      case None => PartiallyAppliedObj.full(None)
      case Some(paos) => paos.map(Some(_))
    }

    def isMultiple = false
  }

  implicit def listSubformContainer[U]: SubformContainer[List[U], U, List] = new SubformContainer[List[U], U, List] {
    def map[R, S](c: List[R])(f: (R) => S) = c.map(f)
    def toList[R](c: List[R]) = c
    def zipWithIndex[R](values: List[R]) = values.zipWithIndex.map { case (v, i) => (v, Some(i))}
    def update[R](cont: List[R])(v: R, i: Int) = cont.updated(i, v)

    def valuesWithIndexFromJSON(jvalue: Option[JsValue]) = jvalue match {
      case Some(JsArray(jvalues)) => jvalues.zipWithIndex.map { case (v, i) => (v, Some(i))}.toList
      case _ => Nil
    }
    def combineJValues(jvalues: List[JsValue]) = JsArray(jvalues)
    def combinePaos[R](paosInCont: List[PartiallyAppliedObj[R]]) = PartiallyAppliedObj.flatten(paosInCont)

    def isMultiple = true
  }

  implicit def vectorSubformContainer[U]: SubformContainer[Vector[U], U, Vector] = new SubformContainer[Vector[U], U, Vector] {
    def map[R, S](c: Vector[R])(f: (R) => S) = c.map(f)
    def toList[R](c: Vector[R]) = c.toList
    def zipWithIndex[R](values: Vector[R]) = values.zipWithIndex.map { case (v, i) => (v, Some(i))}
    def update[R](cont: Vector[R])(v: R, i: Int) = cont.updated(i, v)

    def valuesWithIndexFromJSON(jvalue: Option[JsValue]) = jvalue match {
      case Some(JsArray(jvalues)) => jvalues.zipWithIndex.toVector.map { case (v, i) => (v, Some(i))}
      case _ => Vector.empty
    }
    def combineJValues(jvalues: Vector[JsValue]) = JsArray(jvalues.toList)
    def combinePaos[R](paosInCont: Vector[PartiallyAppliedObj[R]]) = PartiallyAppliedObj.flatten(paosInCont.toList).map(_.toVector)

    def isMultiple = true
  }
}