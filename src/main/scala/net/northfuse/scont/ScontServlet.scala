package net.northfuse.scont

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

/**
 * @author tylers2
 */
class ScontServlet extends HttpServlet {
	override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
		ScontSession(req.getSession, {
		})
	}
}