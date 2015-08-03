/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org

package object supler extends IdentityType {
  type ValuesProvider[T, U] = T => List[U]
}

trait IdentityType {
  type Id[T] = T
}