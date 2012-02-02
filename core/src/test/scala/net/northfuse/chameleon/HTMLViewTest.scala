package net.northfuse.chameleon

import org.junit.{Assert, Test}


/**
 * @author tylers2
 */

class HTMLViewTest {

	import xml._
	implicit def addToNodeSeq(nodes : NodeSeq) = new {
		def nonEmptyNodes = nodes match {
			case e : Elem => Utility.trim(e)
			case _ => NodeSeq.Empty
		}
	}

	@Test
	def extractWithOnlyBody() {
		val (head, body) = HTMLView.extractHeadAndBody({
			<body>
				<div>hello world!</div>
			</body>
		})
		
		Assert.assertTrue(head.nonEmptyNodes.isEmpty)
		assertEquals(<div>hello world!</div>, body)
	}

	@Test
	def extractWithOnlyBodyContent() {
		val (head, body) = HTMLView.extractHeadAndBody({
			<div>hello world!</div>
		})

		Assert.assertTrue(head.nonEmptyNodes.isEmpty)
		assertEquals(<div>hello world!</div>, body)
	}

	@Test
	def extractWithHTML() {
		val (head, body) = HTMLView.extractHeadAndBody({
			<html>
				<head>
					<title>something</title>
				</head>
				<body>
					<div>hello world!</div>
				</body>
			</html>
		})

		assertEquals(<title>something</title>, head)
		assertEquals(<div>hello world!</div>, body)
	}

	@Test
	def multiLineExtractWithHTML() {
		val (head, body) = HTMLView.extractHeadAndBody({
			<html>
				<head>
					<title>something</title>
					<script>something</script>
				</head>
				<body>
					<div>hello world!</div>
					<div>another</div>
				</body>
			</html>
		})

		assertEquals(<title>something</title><script>something</script>, head)
		assertEquals(<div>hello world!</div><div>another</div>, body)
	}

	def assertEquals(expected : NodeSeq, actual : NodeSeq) {
		val actualElem = Utility.trim(<data>{actual}</data>)
		val expectedElem = Utility.trim(<data>{expected}</data>)
		Assert.assertEquals(expectedElem, actualElem)
	}
}
