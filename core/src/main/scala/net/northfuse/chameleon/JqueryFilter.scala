package net.northfuse.chameleon

import HTMLView.HTMLFilter
import xml.NodeSeq

/**
 * @author Tyler Southwick
 */
object JQueryFilter extends HTMLFilter with HTMLView {
	def apply(head: NodeSeq, body: NodeSeq) = {
		<html>
			<head>
				{js(jQueryJs)}
				{head}
			</head>
			<body>
				{body}
			</body>
		</html>
	}

	val jQueryJs = staticFileClassPath("/chameleon/js/jquery-1.7.1.min.js")
}