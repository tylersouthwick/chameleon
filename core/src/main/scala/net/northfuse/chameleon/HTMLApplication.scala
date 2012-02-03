package net.northfuse.chameleon

import io.Source
import xml._
import javax.servlet.http.{HttpServletRequestWrapper, HttpServletResponse => Response, HttpServletRequest => Request}

/**
 * @author tylers2
 */
trait HTMLApplication {

	import Application.ChameleonCallback

	import HTMLApplication.LOG

	final def url(callback: ChameleonCallback) = Application.url(callback)

	final def link(callback: ChameleonCallback, body: String) = <a href={url(callback)}>
		{body}
	</a>

	/**
	 * Follows the POST-REDIRECT-GET pattern so that the user will never get a warning to resubmit a form.
	 * @param callback The callback that needs to get handled.
	 * @return A url to the wrapped callback
	 */
	private def formAction(callback : ChameleonCallback) = url((request, response) => {
		val parameters = request.getParameterMap.asInstanceOf[java.util.Map[String, Array[String]]]
		response.sendRedirect(url((request, response) =>
				callback(new HttpServletRequestWrapper(request) {
					override def getParameterMap = parameters
					override def getParameterValues(name : String) = parameters.get(name)
					override def getParameter(name : String) = {
						val value = parameters.get(name)
						if (value.size > 0) {
							value(0)
						} else {
							null
						}
					}
					//override def getParameterNames = parameters.keySet.iterator
				}, response)
		))}
	)

	/**
	 * Builds a form with the specified action.
	 * Follows the POST-REDIRECT-GET pattern so that the user will never get a warning to resubmit a form.
	 *
	 * @param action The action to preform on submition
	 * @param body The form body
	 * @return The xhtml of the form
	 */
	final def form(action: ChameleonCallback, body: NodeSeq) = <form action={formAction(action)} method="POST">
		{body}
	</form>

	/*
	type CssDefinitions = Map[String, Seq[(String, String)]]
	def css(definitions : CssDefinitions) = definitions.map{case (selector, styles) => {
		<style>{selector + "{" + styles.foldLeft("") { (total, style) => total + style._1 + ": " + style._2 + ";" }}</style>
	}}
	*/
	
	final def staticFileClassPath(name : String) : ChameleonCallback = {
		val is = classOf[HTMLApplication].getResourceAsStream(name)
		val s = Source.fromInputStream(is).mkString
		(request, response) => {
			response.getOutputStream.write(s.getBytes)
		}
	}

	final def css(css : ChameleonCallback) = <link rel="stylesheet" href={url(css)} type="text/css" />
	final def js(js : ChameleonCallback) = <script type="text/javascript" src={url(js)}></script>

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

	implicit final def convertPageWithTitle(page : PageWithTitle) = <html>
		<head>
			<title>{page._1}</title>
		</head>
		<body>{page._2}</body>
	</html>

	implicit final def convertXmlToView(nodes:  => NodeSeq): ChameleonCallback = {
		LOG.debug("converting xml to view")
		(request, response) => {
			LOG.debug("calling HTMLApplication")
			HTMLApplication(response)(filter(nodes))
		}
	}

	implicit final def convertPageWithTitleToView(nodes:  => PageWithTitle) = convertXmlToView(nodes)

	implicit final def convertXmlRequestToView(nodes: (Request) => NodeSeq): ChameleonCallback = (request, response) => {
		LOG.debug("calling HTMLApplication")
		HTMLApplication(response)(filter(nodes(request)))
	}

	final def parser[T] (nodes : T => NodeSeq) (implicit parser : RequestParser[T]) : ChameleonCallback = (request, response) => {
		LOG.debug("calling HTMLApplication")
		HTMLApplication(response)(filter(nodes(parser(request))))
	}

	final def parser2[T] (nodes : T => PageWithTitle) (implicit parser : RequestParser[T]) : ChameleonCallback = (request, response) => {
		LOG.debug("calling HTMLApplication")
		HTMLApplication(response)(filter(nodes(parser(request))))
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
}

