package net.northfuse.chameleon

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request, HttpServlet}

/**
 * @author tylers2
 */
trait ScontServlet extends HttpServlet {

	import ScontSession.ScontCallback

	override final def doGet(request: Request, response: Response) {
		ScontSession(request, response, homePage(request, response), {
			case infe: IdentifierNotFoundException => handleNotFound(infe.identifier, request, response)
		})
	}

	def homePage: ScontCallback

	private def handleNotFound(identifier: String, request: Request, response: Response) {
		identifier match {
			case "sessions" => listSessions(response)
			case _ => {
				ScontServlet.LOG.warn("IDENTIFIER NOT FOUND: " + identifier)
				response.sendError(Response.SC_NOT_FOUND)
			}
		}
	}

	private def listSessions(response: Response) {
		HTMLView(response) {
			<body>
				<h1>Open Sessions</h1>
				{
				val list = ScontSession.session.all
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
}

object ScontServlet {
	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[ScontServlet])
}
