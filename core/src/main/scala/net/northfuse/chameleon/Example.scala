package net.northfuse.scont

import javax.servlet.http.{HttpServletRequest => Request}

/**
 * @author tylers2
 */
object Example extends ScontServlet with HTMLView with JettyRunner {

	val LOG = org.slf4j.LoggerFactory.getLogger("net.northfuse.chameleon.Example")

	def homePage = {
		LOG.info("showing home page....")
		<body>
			<p>Hello world!</p>{link(callback = test1, body = "my test1")}
		</body>
	}

	def test1 = {
		LOG.info("showing test1...")
		<body>
			<p>test1!</p>{form(handleForm, {
				<input type="text" name="data1"/>
						<input type="submit"/>
		})}
		</body>
	}

	def handleForm(request: Request) = {
		LOG.info("handling form...")
		val answer = request.getParameter("data1")
		LOG.info("\tanswer: " + answer)
		<body>
			<p>You Answered the question</p>{link(showAnswer(answer), "See your response")}
		</body>
	}

	/**
	 * Currying example
	 */
	def showAnswer(answer : String) = {
		LOG.info("showing answer..." + answer)
		<body>
			<h1>Your answer was
				{answer}
			</h1>
		</body>
	}

}
