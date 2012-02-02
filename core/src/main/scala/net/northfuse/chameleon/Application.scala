package net.northfuse.chameleon

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request}

/**
 * @author tylers2
 */
trait Application extends IdentifierHandler with HTMLView {

	import Application.ChameleonCallback

	def handle(request : Request, response : Response) {
		Application(this, request, response, homePage(request, response), {
			case infe: IdentifierNotFoundException => handleNotFound(infe.identifier, request, response)
			case t : Throwable => handleError(t)(request, response)
		})
	}

	def homePage: ChameleonCallback

	def handleError(t : Throwable) = {
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

	def notFound(identifier : String) = {
		<body>
			<p>Page Not Found</p>
			<p>{link(homePage, "Return to home page")}</p>
		</body>
	}

	final def listSessions = {
		<body>
			<h1>Open Sessions</h1>
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

trait IdentifierHandler {
	def findIdentifier(request : Request) : Option[String]

	def buildUrl(identifier : String, request : Request) : String
}

object Application {
	type ChameleonCallback = (Request, Response) => Unit

	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[ChameleonSession])

	private val holder = new ThreadLocal[ChameleonSession]

	def session = holder.get()

	def url(callback: ChameleonCallback) = session.add(callback)

	def apply(identifierHandler : IdentifierHandler, request: Request, response: Response, start: => Unit, errorHandle: PartialFunction[Throwable, Unit]) {
		val session = new ChameleonSession(identifierHandler, request.getSession, request)
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
		} catch errorHandle
		finally {
			holder.remove()
		}
	}
}

