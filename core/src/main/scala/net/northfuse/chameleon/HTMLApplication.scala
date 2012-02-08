package net.northfuse.chameleon

import xml._
import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request}

/**
 * @author tylers2
 */
trait HTMLApplication {

	import HTMLApplication._
	import Application.ChameleonCallback

	import HTMLApplication.LOG

	/*
	type CssDefinitions = Map[String, Seq[(String, String)]]
	def css(definitions : CssDefinitions) = definitions.map{case (selector, styles) => {
		<style>{selector + "{" + styles.foldLeft("") { (total, style) => total + style._1 + ": " + style._2 + ";" }}</style>
	}}
	*/
	
	final def staticFileClassPath(name : String) = Application.session.buildUrl("static/" + name)
	/*
		(request, response) => {
			println("rendering file: " + name)
			val is = classOf[HTMLApplication].getResourceAsStream(name)
			val s = Source.fromInputStream(is).mkString
			response.getOutputStream.write(s.getBytes)
		}
	}
	*/

	final def css(css : String) : Elem = <link rel="stylesheet" href={css} type="text/css" />
	final def css(cssCallback : ChameleonCallback) : Elem = css(url(cssCallback))
	final def js(js : String) : Elem = <script type="text/javascript" src={js}></script>
	final def js(jsCallback : ChameleonCallback) : Elem = js(url(jsCallback))

	import HTMLApplication.HTMLFilter
	def filters = Seq[HTMLFilter](JQueryFilter)

	final private def filter(nodes: NodeSeq) = {
		LOG.debug("applying filters")
		filters.foldLeft(nodes) {
			(nodes, filter) =>
				val (head, body) = HTMLApplication.extractHeadAndBody(nodes)
				filter(head, body)
		}
	}

	trait RequestParser[T] {
		def apply(request : Request) : T
	}

	type PageWithTitle = (String, NodeSeq)

	final def page(title : String)(body: => NodeSeq) : ChameleonCallback = (request, response) => {
		HTMLApplication(response) (filter {
			<html>
				<head>
					<title>{title}</title>
				</head>
				{body}
			</html>
		})
	}

	final def formHandler[T] (title : String) (nodes : T => NodeSeq) (implicit parser : RequestParser[T]) : ChameleonCallback = (request, response) => {
		LOG.debug("calling HTMLApplication")
		HTMLApplication(response)(filter{
			<html>
				<head>
					<title>{title}</title>
				</head>
				<body>
					{nodes(parser(request))}
				</body>
			</html>
		})
	}
}

object HTMLApplication {
	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[HTMLApplication])

	type HTMLFilter = (NodeSeq,  NodeSeq) => NodeSeq
	
	def apply(response: Response)(body: => NodeSeq) {
		LOG.debug("applying HTML View")
		response.setHeader("Content-type", "text/html")
		val out = new java.io.PrintWriter(response.getOutputStream, true)
		out.println(body)
	}
	
	implicit private def findNodeSeqChildren(nodes : Seq[Node]) = new {
		val children = nodes match {
			case e : Elem => e.child
			case _ => if (nodes.isEmpty) {
				NodeSeq.Empty
			} else {
				nodes(0).child
			}
		}
	}

	import xml._
	def extractHeadAndBody(nodes : NodeSeq) : (NodeSeq, NodeSeq) = {
		nodes.headOption match {
			case Some(e) => {
				if (e.label == "html") {
					val (head, body) = (e \ "head", e \ "body")
					(head.children, body.children)
				} else if (e.label == "body") {
					(NodeSeq.Empty, e.child)
				} else {
					(NodeSeq.Empty, nodes)
				}
			}
			case None => (NodeSeq.Empty, NodeSeq.Empty)
		}
	}
	/*
	def extractHeadAndBody(nodes : NodeSeq) : (NodeSeq, NodeSeq) = nodes match {
		case node : Node => Utility.trim(node) match {
			case <html><head>{head}</head><body>{body}</body></html> => (head, body)
			case <body>{body}</body> => (NodeSeq.Empty, body)
			case _ => (NodeSeq.Empty, node)
		}
		case _ => (NodeSeq.Empty, nodes)
	}
	*/

	import Application.ChameleonCallback
	
	def url(callback: ChameleonCallback) = Application.url(callback)

	def link(callback: ChameleonCallback, body: String) = <a href={url(callback)}>
		{body}
	</a>
}

