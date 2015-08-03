package play.json.extra

import scala.language.experimental.macros
import org.cvogt.scala.constraint.boolean.!
import scala.reflect.macros.blackbox
import play.api.libs.json._
import collection.immutable.ListMap

sealed trait AdtEncoder
object AdtEncoder{
  trait TypeTagAdtEncoder extends AdtEncoder{
    import scala.reflect.runtime.universe.TypeTag
    def extractClassJson[T: TypeTag](json: JsObject): Option[JsObject]
    def encodeObject[T: TypeTag]: JsValue
    def encodeClassType[T: TypeTag](json: JsObject): JsObject
  }

  trait ClassTagAdtEncoder extends AdtEncoder{
    import scala.reflect.ClassTag
    def extractClassJson[T: ClassTag](json: JsObject): Option[JsObject]
    def encodeObject[T: ClassTag]: JsValue
    def encodeClassType[T: ClassTag](json: JsObject): JsObject
  }

  object TypeAsField extends ClassTagAdtEncoder{
    import scala.reflect._
    def extractClassJson[T: ClassTag](json: JsObject) = {
      Some(json).filter(
        _ \ "type" == JsString(classTag[T].runtimeClass.getSimpleName)
      )
    }

    def encodeObject[T: ClassTag] = {
      JsString(classTag[T].runtimeClass.getSimpleName.dropRight(1))
    }
      
    def encodeClassType[T: ClassTag](json: JsObject) = {
      json ++ JsObject(Seq("type" -> JsString(classTag[T].runtimeClass.getSimpleName)))
    }
  }
}

private class Macros(val c: blackbox.Context){
  import c.universe._
  val pkg = q"_root_.play.json.extra"
  val pjson = q"_root_.play.api.libs.json"

  /**
  Generates a list of all known classes and traits in an inheritance tree.
  Includes the given class itself.
  Does not include subclasses of non-sealed classes and traits.
  TODO: move this to scala-extensions
  */
  private def knownTransitiveSubclasses(sym: ClassSymbol): Seq[ClassSymbol] = {
    sym +: (
      if(sym.isModuleClass){
        Seq()
      } else {      
        sym.knownDirectSubclasses.flatMap(s => knownTransitiveSubclasses(s.asClass))
      }
    ).toSeq
  }

  private def caseClassFieldsTypes(tpe: Type): List[(String,String, Type)] = {
    val params = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.head

    params.map{ field =>
      var attrName=field.name.toTermName.decodedName.toString
      if(field.annotations.nonEmpty){
        field.annotations.map{
          annotation =>
            annotation.tree match {
              case Apply(Select(New(tpe), _), List(Literal(Constant(unique)))) =>
                if(tpe.toString().endsWith(".key")) attrName=unique.toString
              case extra =>
            }
        }
      }
      ( field.name.toTermName.decodedName.toString,
        attrName,
        field.typeSignature)
    }
  }

  def formatCaseClass[T: c.WeakTypeTag]/*(ev: Tree)*/: Tree = {
    val T = c.weakTypeOf[T]
    if(!isCaseClass(T))
      c.error(c.enclosingPosition, s"not a case class: $T")
    val (results,mkResults) = caseClassFieldsTypes(T).map{
      case (k,key, t) =>
        val name = TermName(c.freshName)
        if(t.toString.startsWith("Option")){
          val subtype=t.typeArgs.head
          (name, q"""val $name: JsResult[$t] = {
            val path = (JsPath() \ $key)
            val resolved = path.asSingleJsResult(json)
            val result = (json \ $key).validate[$subtype].repath(path)
            if(resolved.isSuccess && result.isSuccess){
              result.map(v=> Some(v))
            } else {
              JsSuccess[$t](None)
            }
          }
          """)

        } else {
          (name, q"""val $name: JsResult[$t] = {
            val path = (JsPath() \ $key)
            val resolved = path.asSingleJsResult(json)
            val result = (json \ $key).validate[$t].repath(path)
            (resolved,result) match {
              case (_,result:JsSuccess[_]) => result
              case _ => resolved.flatMap(_ => result)
            }
          }
          """)
        }
    }.unzip
    val jsonFields = caseClassFieldsTypes(T).map{
      case (k,key, _) => q"""${Constant(key)} -> Json.toJson(obj.${TermName(k)})"""
    }

    val r=q"""
      {
        import $pjson._
        new Format[$T]{ 
          def reads(json: JsValue) = {
            ..$mkResults
            val errors = Seq[JsResult[_]](..$results).collect{
              case JsError(values) => values
            }.flatten
            if(errors.isEmpty){
              JsSuccess(new $T(..${results.map(r => q"$r.get")}))
            } else JsError(errors)
          }
          def writes(obj: $T) = JsObject(Seq(..$jsonFields).filterNot(_._2 == JsNull))
        }
      }
      """
//    println(r)
    r
  }

  def formatAdt[T: c.WeakTypeTag](encoder: Tree): Tree = {
    val T = c.weakTypeOf[T].typeSymbol.asClass

    val allSubs = knownTransitiveSubclasses(T)
    
    allSubs.foreach{ sym =>
      lazy val modifiers = sym.toString.split(" ").dropRight(1).mkString
      if(!sym.isFinal && !sym.isSealed && !sym.isModuleClass ){
        c.error(c.enclosingPosition, s"required sealed or final, found $modifiers: ${sym.fullName}")
      }
      if(!sym.isCaseClass && !sym.isAbstract){
        c.error(c.enclosingPosition, s"required abstract, trait or case class, found $modifiers: ${sym.fullName}")
      }
    }

    val subs = allSubs.filterNot(_.isAbstract)
    
    val isModuleJson = (sym: ClassSymbol) =>
      q"""
        (json: JsValue) => {
          Some(json).filter{
            _ == $encoder.encodeObject[$sym]
          }
        }
      """

    val writes = subs.map{
      sym => if(sym.isModuleClass){
        cq"`${TermName(sym.name.decodedName.toString)}` => $encoder.encodeObject[$sym]"//
      } else {
        assert(sym.isCaseClass)
        cq"""obj: $sym => {
          $encoder.encodeClassType[$sym](
            Json.toJson[$sym](obj).as[JsObject]
          )
        }
        """
      }
    }

    val (extractors, reads) = subs.map{
      sym =>
        val name = TermName(c.freshName)
        (
          {
            val extractor = if(sym.isModuleClass){
              q"""
                (json: JsValue) =>
                  ${isModuleJson(sym)}(json).map(_ => $pjson.JsSuccess(${sym.asClass.module}))
              """
            } else {
              assert(sym.isCaseClass)
              q"""
                (json: JsValue) =>
                  $encoder.extractClassJson[${sym}](json.as[JsObject])
                          .map($pjson.Json.fromJson[$sym](_))
              """
            }
            q"object $name extends $pkg.Extractor($extractor)"
          },
          cq"""$name(json) => json"""
        )
    }.unzip
    
    val t = q"""
      {
        ..$extractors
        new Format[$T]{ 
          def reads(json: $pjson.JsValue) = json match {case ..$reads}
          def writes(obj: $T) = obj match {case ..$writes}
        }
      }
      """
    //println(t)
    t
  }

  protected def isCaseClass(tpe: Type)
    = tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isCaseClass
}
trait ImplicitCaseClassFormatDefault{
  implicit def formatCaseClass[T]
//    (implicit ev: ![Format[T]])
    : Format[T] = macro Macros.formatCaseClass[T]
}
object implicits extends ImplicitCaseClassFormatDefault

class Extractor[T,R](f: T => Option[R]){
  def unapply(arg: T): Option[R] = f(arg)
}

object Jsonx{
  /**
  Generates a PlayJson Format[T] for a case class T with any number of fields (>22 included)
  */
  def formatCaseClass[T]
//    (implicit ev: ![Format[T]])
    : Format[T]
    = macro Macros.formatCaseClass[T]

  /**
  Generates a PlayJson Format[T] for a sealed trait that only has case object children
  */
  def formatAdt[T](encoder: AdtEncoder): Format[T]
    = macro Macros.formatAdt[T]
}