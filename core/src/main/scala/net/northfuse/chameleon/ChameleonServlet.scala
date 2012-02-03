package net.northfuse.chameleon

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request, HttpServlet}


/**
 * @author tylers2
 */
trait ChameleonServlet extends HttpServlet with Application {

	override def doGet(request: Request, response: Response) {
		handle(request, response)
	}

	override def doPost(request: Request, response: Response) {
		handle(request, response)
	}

	final def findIdentifier(request: Request) = {
		val id = {
			val pathInfo = request.getPathInfo
			if (pathInfo != null && pathInfo.startsWith("/")) {
				pathInfo.substring(1)
			} else pathInfo
		}
		if (id == null || id.trim().isEmpty) None else Some(id)
	}

	final def buildUrl(identifier: String, request: Request) = {
		val sb = new StringBuilder
		def strip(path : String) = {
			val path1 = {
				if (path.startsWith("/")) {
					path.substring(1)
				} else {
					path
				}
			}
			if (path1.endsWith("/")) {
				path1.substring(0, path1.length - 1)
			} else {
				path1
			}
		}
		val contextPath = strip(request.getContextPath)
		val servletPath = strip(request.getServletPath)
		
		def empty(path : String) = path.trim().isEmpty
		
		sb.append("/")
		if (!empty(contextPath)) {
			sb.append(contextPath)
			sb.append("/")
		}
		if (!empty(servletPath)) {
			sb.append(servletPath)
		}
		sb.append("/")
		sb.append(identifier)
		sb.toString()
	}
}
