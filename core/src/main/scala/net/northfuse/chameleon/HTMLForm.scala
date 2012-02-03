package net.northfuse.chameleon

import javax.servlet.http.HttpServletRequestWrapper
import xml._

/**
 * @author Tyler Southwick
 */
object HTMLForm {

	import Application.{url, ajax,  ChameleonCallback}

	/**
	 * Follows the POST-REDIRECT-GET pattern so that the user will never get a warning to resubmit a form.
	 * @param callback The callback that needs to get handled.
	 * @return A url to the wrapped callback
	 */
	private def formAction(callback : ChameleonCallback) = url((request, response) => {
		val parameters = request.getParameterMap.asInstanceOf[java.util.Map[String, Array[String]]]
		response.sendRedirect(url((request, response) =>
			callback(new HttpServletRequestWrapper(request) {
				override def getParameterMap = parameters
				override def getParameterValues(name : String) = parameters.get(name)
				override def getParameter(name : String) = {
					val value = parameters.get(name)
					if (value.size > 0) {
						value(0)
					} else {
						null
					}
				}
				//override def getParameterNames = parameters.keySet.iterator
			}, response)
		))}
	)

	/**
	 * Builds a form with the specified action.
	 * Follows the POST-REDIRECT-GET pattern so that the user will never get a warning to resubmit a form.
	 *
	 * @param action The action to preform on submition
	 * @param body The form body
	 * @return The xhtml of the form
	 */
	def form(action: ChameleonCallback, body: NodeSeq) = <form action={formAction(action)} method="POST">
		{body}
	</form>

	object checkbox extends FormInputElement {
		def inputType = "checkbox"
	}

	object input extends FormInputElement {
		def inputType = "input"
	}

	def textbox = input
}

class Opt[T] private (val option : Option[T])
object Opt {
	implicit def any2opt[T] (t : T) : Opt[T] = new Opt[T](Option(t))
	implicit def option2opt[T](o : Option[T]) : Opt[T] = new Opt[T](o)
	implicit def opt2option[T](o : Opt[T]): Option[T] = o.option
}

trait FormElement {
	/**
	 * Defines a form element tag with the following options.
	 * @param checked
	 * @return
	 */
	def apply(name : Opt[String] = None, id : Opt[String] = None, checked : Boolean = false, value : Opt[String] = None) : Elem

	private def buildElement(name : Opt[String] = None, id : Opt[String] = None, checked : Boolean = false, value : Opt[String] = None) =
		apply(name = name, id = id,  checked = checked, value = value)

	trait JavascriptEvent {
		import Application.ajax
		def eventType : String
		def randomString = java.util.UUID.randomUUID().toString
		def apply(callback : String => Unit, name : Opt[String] = None, id : Opt[String] = None, checked : Boolean = false, value : Opt[String] = None) = {
			val actualName = (name : Option[String]) match {
				case Some(givenName) => givenName
				case None => randomString
			}
			val actualId = (id : Option[String]) match {
				case Some(givenId) => givenId
				case None => randomString
			}
			val attribute = Attribute(None, eventType, Text(ajax(id = actualId, name = actualName, callback = callback)), Null)
			buildElement(name = actualName, id = actualId,  checked = checked, value = value) % attribute
		}
	}
	
	object onclick extends JavascriptEvent {
		def eventType = "onclick"
	}
	object onchange extends JavascriptEvent {
		def eventType = "onchange"
	}

}

trait FormInputElement extends FormElement {

	def inputType : String

	def apply(name : Opt[String] = None, id : Opt[String] = None, checked : Boolean = false, value : Opt[String] = None) = {
		var input = <input type={inputType} />
		
		def addParameter(name : String, value : Option[String]) {
			if (value.isDefined) {
				input = input % Attribute(None, name, Text(value.get), Null)
			}
		}
		addParameter("id", id)
		addParameter("name", name)
		addParameter("value", value)

		if (checked) {
			input = input % Attribute(None, "checked", Text("true"), Null)
		}

		input
	}
}
