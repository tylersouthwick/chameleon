package net.northfuse.scont

/**
 * @author tylers2
 */

object JettyRunner {
	def main(args : Array[String]) {
		val server = new org.mortbay.jetty.Server(8080);
	}
}