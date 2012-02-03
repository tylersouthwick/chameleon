package net.northfuse.chameleon

import javax.servlet.http.HttpServletRequestWrapper
import xml.{Null, Text, Attribute, NodeSeq}

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

	def checkbox(onclick : => Unit, checked : Boolean) = {
		val input = <input type="checkbox" onclick={ajax(onclick)} />
		if (checked) {
			input % Attribute(None, "checked", Text("true"), Null)
		} else {
			input
		}
	}
}
