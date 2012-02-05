package net.northfuse.chameleon

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request}
import xml.NodeSeq

/**
 * @author tylers2
 */
trait Application extends IdentifierHandler with HTMLApplication {

	import Application.ChameleonCallback
	import HTMLApplication._

	def handle(request : Request, response : Response) {
		Application(
			identifierHandler = this,
			request = request,
			response = response,
			start = homePage(request, response),
			mappings = mappings,
			errorHandler = {
				case infe: IdentifierNotFoundException => handleNotFound(infe.identifier, request, response)
				case t : Throwable => handleError(t)(request, response)
			}
		)
	}

	def findPermanentMapping(request : Request) = {
		findIdentifier(request) match {
			case Some(identifier) => mappings.get(identifier)
			case None => None
		}
	}

	def homePage: ChameleonCallback

	def mappings = Map[String,  ChameleonCallback]()

	def handleError(t : Throwable) = page("Unexpected Error") {
		<body>
			<h1>There was an error!</h1>
			{t.getMessage}
			<pre>
				{
				import java.io._
				val baos = new ByteArrayOutputStream
				val writer = new PrintWriter(baos)
				t.printStackTrace(writer)
				writer.close()
				baos.toString
				}
			</pre>
		</body>
	}

	private def handleNotFound(identifier: String, request: Request, response: Response) {
		identifier match {
			case "sessions" => listSessions(request, response)
			case _ => {
				notFound(identifier)(request, response)
			}
		}
	}

	def notFound(identifier : String) = page("Page Not Found") {
		<body>
			<p>Page Not Found</p>
			<p>{link(homePage, "Return to home page")}</p>
		</body>
	}

	final def listSessions = page("Open Sessions") {
		<body>
			{
			val list = Application.session.all
			if (list.isEmpty) {
				<div>There are no sessions</div>
			} else {
				<style>
					tbody tr:hover {"{background-color: lightgray}"}
				</style>
				<table>
					<thead>
					<tr>
						<th>Session Id</th>
						<th>TTL</th>
					</tr>
					</thead>
					<tbody>
					{list.map {case (id, timeout) =>
						<tr>
							<td>
								<a href={"/" + id}>{id}</a>
							</td>
							<td>
								{timeout}
							</td>
						</tr>
					}}
					</tbody>
				</table>
			}}
		</body>
	}
}

object Application {
	type ChameleonCallback = (Request, Response) => Unit

	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[ChameleonSession])

	private val holder = new ThreadLocal[ChameleonSession]

	def session = holder.get()

	def url(callback: ChameleonCallback) = session.add(callback)

	def ajax(callback : ChameleonCallback) = {
		val ajaxUrl = url(callback)
		"jQuery.ajax('" + ajaxUrl + "')"
	}
	
	implicit def convertNodeSeqToTuple(nodes : NodeSeq) = (nodes \ "@id").text -> nodes

	def convertToJson(data : Map[String, NodeSeq]) = "{" + data.map{case (id, nodes) =>
		"\"" + id + "\"" + ": \"" + nodes.toString().replaceAllLiterally ("\"", "\\\"").replaceAllLiterally("\n", "").replaceAllLiterally("\t", "") + "\""
	}.mkString(",") + "}"

	def ajax(name : String, id : String,  callback : String => Map[String, NodeSeq]) = {
		val ajaxUrl = url((request, response) => {
			val divsToUpdate = callback(request.getParameter(name))
			val json = convertToJson(divsToUpdate);
			/*
			val jsonpCallback = request.getParameter("callback")
			val jsonp = jsonpCallback + "(" + json + ");"
			*/
			response.setHeader("Content-Type", "application/json")
			response.getOutputStream.write(json.getBytes);
		})
		"updateDivsFromAjax('" + name + "', '" + id + "', '" + ajaxUrl + "')"
	}

	def apply(identifierHandler : IdentifierHandler, request: Request, response: Response,
	          start: => Unit,
	          errorHandler: PartialFunction[Throwable, Unit],
			  mappings : Map[String,  ChameleonCallback]) {
		val session = new ChameleonSession(identifierHandler, request.getSession, request, mappings)
		holder.set(session)
		try {
			session.current match {
				case None => {
					LOG.debug("No identifier found... invoking start page")
					start
				}
				case Some((identifier, callback)) => {
					LOG.debug("found identifier... invoking callback [" + identifier + "]")
					callback(request, response)
				}
			}
		} catch errorHandler
		finally {
			holder.remove()
		}
	}
}

