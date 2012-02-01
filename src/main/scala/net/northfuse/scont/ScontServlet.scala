package net.northfuse.scont

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

	def homePage : ScontCallback

	private def handleNotFound(identifier: String, request: Request, response: Response) {
		identifier match {
			case "sessions" => listSessions(response)
			case _ => {
				println("NOT FOUND: " + identifier)
				response.sendError(Response.SC_NOT_FOUND)
			}
		}
	}

	private def listSessions(response: Response) {
		HTMLView(response) {
			<body>
				<h1>Open Sessions</h1>{val list = ScontSession.session.all
			if (list.isEmpty) {
				<div>There are no sessions</div>
			} else {
				<ul>
					{list.map {
					id => {
						<li>
							<a href={"/" + id}>{id}</a>
						</li>
					}
				}}
				</ul>
			}}
			</body>
		}
	}
}

