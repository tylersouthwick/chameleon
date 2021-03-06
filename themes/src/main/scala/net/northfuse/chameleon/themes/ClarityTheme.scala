package net.northfuse.chameleon.themes

import net.northfuse.chameleon.{Application, HTMLApplication}
import xml.NodeSeq

import HTMLApplication._

/**
 * @author Tyler Southwick
 */
object ClarityTheme extends HTMLApplication {

	val LOG = org.slf4j.LoggerFactory.getLogger("net.northfuse.chameleon.examples.ClarityTheme")

	import Application.ChameleonCallback

	type ClarityLinks = Seq[(String, ChameleonCallback)]
	def apply(title : String, links: ClarityLinks, footer : => NodeSeq = NodeSeq.Empty): HTMLFilter = (head, body) => {
		val myStyles = staticFileClassPath("styles/clarity.css")
		val jQueryUICSS = staticFileClassPath("css/start/jquery-ui-1.8.17.custom.css")
		val jQueryUI = staticFileClassPath("js/jquery-ui-1.8.17.custom.min.js")
		LOG.debug("Applying Theme")
		//find title
		val pageTitle = (head \\ "title").text
		<html>
			<head>
				<title>{pageTitle}</title>
				{head}
				{css(myStyles)}
				{css(jQueryUICSS)}
				{js(jQueryUI)}
			</head>
			<body>
				<div id="header">
					<h1>
						<span id="pageTitle">
							{pageTitle}
						</span>
					</h1>
					<h3>
						{title}
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
					Template Design by <a href="http://www.sixshootermedia.com/">Six Shooter Media</a>
					{footer}
				</div>
			</body>
		</html>
	}
}