package net.northfuse.chameleon

import HTMLApplication.HTMLFilter
import xml.NodeSeq

/**
 * @author Tyler Southwick
 */
object JQueryFilter extends HTMLFilter with HTMLApplication {
	def apply(head: NodeSeq, body: NodeSeq) = {
		<html>
			<head>
				{js(jQueryJs)}
				{js(ajax)}
				{head}
			</head>
			<body>
				{body}
			</body>
		</html>
	}

	val jQueryJs = staticFileClassPath("/chameleon/js/jquery-1.7.1.min.js")
	val ajax = staticFileClassPath("/chameleon/js/ajax.js")
}