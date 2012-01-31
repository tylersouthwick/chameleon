package net.northfuse.scont

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request, HttpServlet}
import xml.NodeSeq

/**
 * @author tylers2
 */
class ScontServlet extends HttpServlet {

	import ScontSession.ScontCallback

	override def doGet(request: Request, response: Response) {
		ScontSession(request, response, HTMLView(response) {
			<body>
				<p>Hello world!</p>{link(callback = test1, body = "my test1")}
			</body>
		}, {
			case infe: IdentifierNotFoundException => handleNotFound(infe.identifier, request, response)
		})
	}

	def handleNotFound(identifier: String, request: Request, response: Response) {
		identifier match {
			case "sessions" => listSessions(response)
			case _ => {
				println("NOT FOUND: " + identifier)
				response.sendError(Response.SC_NOT_FOUND)
			}
		}
	}

	def listSessions(response: Response) {
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

	def test1(request: Request, response: Response) {
		println("rendering test1")

		HTMLView(response) {
			<body>
				<p>test1!</p>{form(handleForm, {
					<input type="text" name="data1"/>
							<input type="submit"/>
			})}
			</body>
		}
	}

	def handleForm(request: Request, response: Response) {
		val answer = request.getParameter("data1")
		HTMLView(response) {
			<body>
				<p>You Answered the question</p>{link({
				(request, response) =>
					HTMLView(response) {
						<body>
							<h1>Your answer was
								{answer}
							</h1>
						</body>
					}
			}, "See your response")}
			</body>
		}
	}

	def url(callback: ScontCallback) = ScontSession(callback)

	def link(callback: ScontCallback, body: String) = <a href={url(callback)}>
		{body}
	</a>

	def form(action: ScontCallback, body: NodeSeq) = <form action={url(action)} method="GET">
		{body}
	</form>
}

object HTMLView {
	def apply(response: Response)(body: NodeSeq) {
		response.setHeader("Content-type", "text/html")
		val out = new java.io.PrintWriter(response.getOutputStream, true)
		out.println(body)
	}
}