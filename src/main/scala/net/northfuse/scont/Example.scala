package net.northfuse.scont

import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request}

/**
 * @author tylers2
 */
object Example extends ScontServlet with HTMLView with JettyRunner {

	def homePage(request : Request, response : Response) {
		println("rendering home page")
		HTMLView(response) {
			<body>
				<p>Hello world!</p>{link(callback = test1, body = "my test1")}
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
				<p>You Answered the question</p>{link(showAnswer(answer), "See your response")}
			</body>
		}
	}

	/**
	 * Currying example
	 */
	def showAnswer(answer : String) (request : Request,  response : Response) {
		HTMLView(response) {
			<body>
				<h1>Your answer was
					{answer}
				</h1>
			</body>
		}
	}

}
