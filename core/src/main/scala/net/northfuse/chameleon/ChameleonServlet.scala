package net.northfuse.chameleon

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}


/**
 * @author tylers2
 */
trait ChameleonServlet extends HttpServlet with Application {

	override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
		handle(request, response)
	}

	final def findIdentifier(request: HttpServletRequest) = {
		val id = {
			val pathInfo = request.getPathInfo
			if (pathInfo != null && pathInfo.startsWith("/")) {
				pathInfo.substring(1)
			} else pathInfo
		}
		if (id == null || id.trim().isEmpty) None else Some(id)
	}

	final def buildUrl(identifier: String, request: HttpServletRequest) = request.getContextPath + "/" + identifier
}
