package net.northfuse.chameleon

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request, HttpServlet}
import xml.NodeSeq

/**
 * @author tylers2
 */
trait HTMLView {
	import ChameleonSession.ChameleonCallback

	def url(callback: ChameleonCallback) = ChameleonSession(callback)

	def link(callback: ChameleonCallback, body: String) = <a href={url(callback)}>
		{body}
	</a>

	def form(action: ChameleonCallback, body: NodeSeq) = <form action={url(action)} method="GET">
		{body}
	</form>

	implicit def convertXmlToView(nodes : => NodeSeq) : ChameleonCallback = (request, response) => HTMLView(response)(nodes)
	implicit def convertXmlToView(nodes : Request => NodeSeq) : ChameleonCallback = (request, response) => HTMLView(response)(nodes(request))
}

object HTMLView {
	def apply(response: Response)(body: NodeSeq) {
		response.setHeader("Content-type", "text/html")
		val out = new java.io.PrintWriter(response.getOutputStream, true)
		out.println(body)
	}
}

