package net.northfuse.scont

import org.mortbay.jetty.servlet.{ServletHolder, Context}
import org.mortbay.jetty.Server
import javax.servlet.http.HttpServlet

/**
 * @author tylers2
 */
trait JettyRunner extends HttpServlet {

	def main(args: Array[String]) {
		val server = new Server(8080)
		val root = new Context(server, "/", Context.SESSIONS)
		root.addServlet(new ServletHolder(this), "/*")
		server.start()
	}
}
