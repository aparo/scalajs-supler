package supler


trait NativeForm[T] {
  def default:T
  def form: org.supler.Form[T]
}
