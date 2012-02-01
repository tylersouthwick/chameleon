package net.northfuse.scont

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request, HttpServlet}
import xml.NodeSeq

/**
 * @author tylers2
 */
trait HTMLView {
	import ScontSession.ScontCallback

	def url(callback: ScontCallback) = ScontSession(callback)

	def link(callback: ScontCallback, body: String) = <a href={url(callback)}>
		{body}
	</a>

	def form(action: ScontCallback, body: NodeSeq) = <form action={url(action)} method="GET">
		{body}
	</form>

	implicit def convertXmlToView(nodes : => NodeSeq) : ScontCallback = (request, response) => HTMLView(response)(nodes)
	implicit def convertXmlToView(nodes : Request => NodeSeq) : ScontCallback = (request, response) => HTMLView(response)(nodes(request))
}

object HTMLView {
	def apply(response: Response)(body: NodeSeq) {
		response.setHeader("Content-type", "text/html")
		val out = new java.io.PrintWriter(response.getOutputStream, true)
		out.println(body)
	}
}

