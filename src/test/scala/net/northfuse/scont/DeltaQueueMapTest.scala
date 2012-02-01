package net.northfuse.scont

import org.junit.{Assert, Test}


/**
 * @author Tyler Southwick
 */
class DeltaQueueMapTest {

	@Test
	def checkDequeue() {
		val map = new DeltaQueueMap[String, String](2)
		map += "hello" -> "test"
		def get() = map.get("hello").isDefined
		Assert.assertTrue(get())
		Assert.assertTrue(get())
		Assert.assertFalse(get())
		Assert.assertTrue(map.evictSeq.isEmpty)
	}

	@Test
	def checkDequeueMultiple() {
		val map = new DeltaQueueMap[String, String](2)
		map += "hello1" -> "test"
		map += "hello2" -> "test"
		Assert.assertSame(1, map.evictSeq.size)
		def get1() = map.get("hello1").isDefined
		def get2() = map.get("hello2").isDefined
		Assert.assertTrue(get1())
		Assert.assertTrue(get2())
		Assert.assertFalse(get1())
		Assert.assertFalse(get2())
	}

	@Test
	def checkDequeueMultiple2() {
		val map = new DeltaQueueMap[String, String](4)
		map += "hello1" -> "test" //count = 4
		map += "hello2" -> "test" //count = 4
		Assert.assertEquals(Seq((Seq("hello2", "hello1"), 4)), map.evictSeq)
		def get1() = map.get("hello1").isDefined
		def get2() = map.get("hello2").isDefined
		def get3() = map.get("hello3").isDefined
		Assert.assertTrue(get1()) //count = 3
		Assert.assertTrue(get2()) //count = 2
		map += "hello3" -> "test"
		Assert.assertEquals(Seq((Seq("hello2", "hello1"), 2), (Seq("hello3"), 2)), map.evictSeq)
		Assert.assertEquals(Seq(("hello2", 2), ("hello1", 2), ("hello3", 4)), map.keysWithTimeout)
		Assert.assertTrue(get1())
		Assert.assertEquals(Seq((Seq("hello2", "hello1"), 1), (Seq("hello3"), 2)), map.evictSeq)
		Assert.assertEquals(Seq(("hello2", 1), ("hello1", 1), ("hello3", 3)), map.keysWithTimeout)
		Assert.assertTrue(get1())
		Assert.assertEquals(Seq((Seq("hello3"), 2)), map.evictSeq)
		Assert.assertEquals(Seq(("hello3", 2)), map.keysWithTimeout)
		Assert.assertFalse(get1())
		Assert.assertTrue(get3())
		Assert.assertEquals(Seq((Seq("hello3"), 1)), map.evictSeq)
		Assert.assertEquals(Seq(("hello3", 1)), map.keysWithTimeout)
		Assert.assertTrue(get3())
		Assert.assertEquals(Seq(), map.evictSeq)
		Assert.assertEquals(Seq(), map.keysWithTimeout)
		Assert.assertFalse(get3())
	}

	@Test
	def checkEvictionOnlyWhenSomethingIsFound() {
		val map = new DeltaQueueMap[String, String](1)
		map += "hello1" -> "test"
		def get(s : String) = map.get(s).isDefined
		Assert.assertFalse(get("something"))
		Assert.assertFalse(get("something"))
		Assert.assertTrue(get("hello1"))
		Assert.assertFalse(get("hello1"))
	}
	
	@Test
	def checkKeys() {
		val map = new DeltaQueueMap[String, String](1)
		map += "hello1" -> "test"
		def get(s : String) = map.get(s).isDefined
		Assert.assertSame(1, map.keys.size)
		Assert.assertSame(1, map.keys.size)
		Assert.assertSame(1, map.keys.size)
		Assert.assertSame(1, map.keys.size)
		Assert.assertSame(1, map.keys.size)
		Assert.assertTrue(get("hello1"))
		Assert.assertFalse(get("hello1"))
	}
}