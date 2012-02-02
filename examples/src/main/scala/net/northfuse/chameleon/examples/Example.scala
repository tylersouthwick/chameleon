package net.northfuse.chameleon.examples

import net.northfuse.chameleon._
import javax.servlet.http.{HttpServletRequest => Request}

/**
 * @author tylers2
 */
object Example extends ChameleonServlet with HTMLView with JettyRunner {

	val LOG = org.slf4j.LoggerFactory.getLogger("net.northfuse.chameleon.examples.Example")

	def homePage = home1

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
	def showAnswer(answer: String) = {
		LOG.info("showing answer..." + answer)
		<body>
			<h1>Your answer was
				{answer}
			</h1>
		</body>
	}

	def home1 : ChameleonSession.ChameleonCallback = {
		<body>
			<div>home1</div>
			{form(processAnswer, {
				<input type="text" name="name" />
				<input type="submit" value="Save" />
			})}
		</body>
	}

	trait FormAnswer {
		def name : String
	}

	implicit object FormAnswer extends RequestParser[FormAnswer] {
		def apply(request: Request) = new FormAnswer {
			val name = request.getParameter("name")
		}
	}
	
	def processAnswer = parser((answer : FormAnswer) => {
		<body>
			<div>Answered Question!</div>
			<p>Your name is {answer.name}</p>
			{link(home1, "Return to home1")}
		</body>
	})

	def home2 : ChameleonSession.ChameleonCallback = {
		<body>
			<div>home2</div>
		</body>
	}

	val links = Seq(("Home1", home1), ("Home2", home2))

	override def filters = Seq(ClarityTheme(links))
}
