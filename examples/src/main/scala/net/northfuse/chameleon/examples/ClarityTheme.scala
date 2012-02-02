package net.northfuse.chameleon.examples

import net.northfuse.chameleon.{ChameleonSession, HTMLView}


/**
 * @author Tyler Southwick
 */
object ClarityTheme extends HTMLView {

	val LOG = org.slf4j.LoggerFactory.getLogger("net.northfuse.chameleon.examples.ClarityTheme")
	val subTitle = "Example Application"
	val title = "mytitle"

	import ChameleonSession.ChameleonCallback

	type ClarityLinks = Seq[(String, ChameleonCallback)]
	def apply(links: ClarityLinks): HTMLFilter = (head, body) => {
		LOG.debug("Applying Theme")
		<html>
			<head>
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
						{subTitle}
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