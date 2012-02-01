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
  }

  @Test
  def checkDequeueMultiple() {
    val map = new DeltaQueueMap[String, String](2)
    map += "hello1" -> "test"
    map += "hello2" -> "test"
    def get1() = map.get("hello1").isDefined
    def get2() = map.get("hello2").isDefined
    Assert.assertTrue(get1())
    Assert.assertTrue(get2())
    Assert.assertFalse(get1())
    Assert.assertFalse(get2())
  }
}