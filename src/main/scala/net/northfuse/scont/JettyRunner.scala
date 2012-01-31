package net.northfuse.scont

import org.mortbay.jetty.servlet.{ServletHolder, Context}
import org.mortbay.jetty.Server

/**
 * @author tylers2
 */

object JettyRunner {
	def main(args : Array[String]) {
		val server = new Server(8080)
    val root = new Context(server, "/", Context.SESSIONS)
    root.addServlet(new ServletHolder(new ScontServlet), "/*")
    server.start()
	}
}