/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler

import scala.scalajs.js

case class GlobalOptions(errors:List[Error]=Nil) {

  def updateErrors(arr:js.Array[Any]): GlobalOptions ={
    this.copy(errors = arr.map(v => Error.fromJson(v.asInstanceOf[js.Dictionary[Any]])).toList)
  }

  def updateErrors(arr:List[js.Dictionary[Any]]): GlobalOptions ={
    this.copy(errors = arr.map(v => Error.fromJson(v)))
  }

  def hasError(path:String)=errors.exists(_.fieldPath==path)
}

object GlobalOptions{
  val default=new GlobalOptions()
}