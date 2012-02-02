package net.northfuse.chameleon.examples

import net.northfuse.chameleon.{ChameleonSession, HTMLView}


/**
 * @author Tyler Southwick
 */
object ClarityTheme extends HTMLView {

	val subTitle = "Example Application"
	val title = "mytitle"

	def apply(links: Seq[(String, ChameleonSession.ChameleonCallback)]): HTMLFilter = (head, body) => {
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

	//def myStyles = "https://denhgi.northfuse.net/resources-0.12.3/css/style.css"
	val myStyles = cssClassPath("/styles/clarity.css")
}