package net.northfuse.chameleon

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request, HttpServlet}
import io.Source
import xml.{Elem, NodeSeq}

/**
 * @author tylers2
 */
trait HTMLView {

	import ChameleonSession.ChameleonCallback

	import HTMLView.LOG

	def url(callback: ChameleonCallback) = ChameleonSession(callback)

	def link(callback: ChameleonCallback, body: String) = <a href={url(callback)}>
		{body}
	</a>

	def form(action: ChameleonCallback, body: NodeSeq) = <form action={url(action)} method="GET">
		{body}
	</form>

	/*
	type CssDefinitions = Map[String, Seq[(String, String)]]
	def css(definitions : CssDefinitions) = definitions.map{case (selector, styles) => {
		<style>{selector + "{" + styles.foldLeft("") { (total, style) => total + style._1 + ": " + style._2 + ";" }}</style>
	}}
	*/
	
	def cssClassPath(name : String) : ChameleonCallback = {
		val is = classOf[HTMLView].getResourceAsStream(name)
		val s = Source.fromInputStream(is).mkString
		(request, response) => {
			response.getOutputStream.write(s.getBytes)
		}
	}

	def css(file : String) = <link rel="stylesheet" href={file} type="text/css"/>
	def css(css : ChameleonCallback) = <link rel="stylesheet" href={url(css)} type="text/css" />

	type HTMLFilter = (NodeSeq,  NodeSeq) => NodeSeq

	def filters = Seq[HTMLFilter]()

	import xml._
	import xml.NodeSeq._
	implicit def addToNodeSeq(nodes : NodeSeq) = new {
		def children = nodes match {
			case e : Elem => e.child
			case _ => NodeSeq.Empty
		}
	}
	//todo improve this
	def filter(nodes: NodeSeq) = {
		LOG.debug("applying filters")
		filters.foldLeft(nodes) {
			(nodes, filter) =>
				val head = Seq[Node]()
				val body = nodes.children
				filter(head, body)
		}
	}

	trait RequestParser[T] {
		def apply(request : Request) : T
	}

	implicit def convertXmlToView(nodes:  => NodeSeq): ChameleonCallback = {
		LOG.debug("converting xml to view")
		(request, response) => {
			LOG.debug("calling HTMLView")
			HTMLView(response)(filter(nodes))
		}
	}

	implicit def convertXmlToView(nodes: (Request) => NodeSeq): ChameleonCallback = (request, response) => {
		LOG.debug("calling HTMLView")
		HTMLView(response)(filter(nodes(request)))
	}

	def parser[T] (nodes : T => NodeSeq) (implicit parser : RequestParser[T]) : ChameleonCallback = (request, response) => {
		LOG.debug("calling HTMLView")
		HTMLView(response)(filter(nodes(parser(request))))
	}
}

object HTMLView {
	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[HTMLView])
	
	def apply(response: Response)(body: => NodeSeq) {
		LOG.debug("applying HTML View")
		response.setHeader("Content-type", "text/html")
		val out = new java.io.PrintWriter(response.getOutputStream, true)
		out.println(body)
	}
}

