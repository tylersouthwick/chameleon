package net.northfuse.chameleon

import org.junit.{Assert, Test}


/**
 * @author Tyler Southwick
 */
class ApplicationTest {

	@Test
	def convertOneToJson() {
		val json = Application.convertToJson(Map("test" -> <div id="test">hello world</div>))

		Assert.assertEquals("{\"test\": \"<div id=\\\"test\\\">hello world</div>\"}", json)
	}

	@Test
	def convertMultipleToJson() {
		val json = Application.convertToJson(Map(
			"test" -> <div id="test">hello world</div>,
			"test2" -> <div id="test2">something</div>
		))

		Assert.assertEquals("{\"test\": \"<div id=\\\"test\\\">hello world</div>\",\"test2\": \"<div id=\\\"test2\\\">something</div>\"}", json)
	}
}