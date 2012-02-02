package net.northfuse.chameleon

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request, HttpServlet}

/**
 * @author tylers2
 */
trait ChameleonServlet extends HttpServlet with HTMLView {

	import ChameleonSession.ChameleonCallback

	override final def doGet(request: Request, response: Response) {
		ChameleonSession(request, response, homePage(request, response), {
			case infe: IdentifierNotFoundException => handleNotFound(infe.identifier, request, response)
		})
	}

	def homePage: ChameleonCallback

	private def handleNotFound(identifier: String, request: Request, response: Response) {
		identifier match {
			case "sessions" => listSessions(request, response)
			case _ => {
				notFound(identifier)(request, response)
			}
		}
	}

	def notFound(identifier : String) : ChameleonCallback = (request, response) => {
		ChameleonServlet.LOG.debug("IDENTIFIER NOT FOUND: " + identifier)
		response.sendError(Response.SC_NOT_FOUND)
	}

	final def listSessions : ChameleonCallback = {
		<body>
			<h1>Open Sessions</h1>
			{
			val list = ChameleonSession.session.all
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

object ChameleonServlet {
	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[ChameleonServlet])
}
