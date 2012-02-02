package net.northfuse.chameleon.examples

import net.northfuse.chameleon.{Application, HTMLView}


/**
 * @author Tyler Southwick
 */
object ClarityTheme extends HTMLView {

	val LOG = org.slf4j.LoggerFactory.getLogger("net.northfuse.chameleon.examples.ClarityTheme")

	import Application.ChameleonCallback

	type ClarityLinks = Seq[(String, ChameleonCallback)]
	def apply(applicationName : String, links: ClarityLinks): HTMLFilter = (head, body) => {
		LOG.debug("Applying Theme")
		//find title
		val title = (head \\ "title").text
		<html>
			<head>
				<title>{title}</title>
				{css(myStyles)}{head}
			</head>
			<body>
				<div id="header">
					<h1>
						<span id="pageTitle">
							{title}
						</span>
					</h1>
					<h3>
						{applicationName}
					</h3>
					<ul id="nav">
						{links.map {
						case (name, callback) =>
							<li>
								<a href={url(callback)}>
									{name}
								</a>
							</li>
					}}
					</ul>
				</div>
				<div id="container">
					<div id="content">
						{body}
					</div>
				</div>
				<div id="footer">
					<p>copy 2012 northfuse</p>
				</div>
			</body>
		</html>
	}

	val myStyles = cssClassPath("/styles/clarity.css")
}