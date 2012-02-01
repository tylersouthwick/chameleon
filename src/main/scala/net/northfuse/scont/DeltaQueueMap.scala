package net.northfuse.scont

import collection.mutable.{Map, HashMap, PriorityQueue}


/**
 * @author Tyler Southwick
 */
class DeltaQueueMap[A, B](count : Int) extends Map[A, B] {
  private val map = new HashMap[A, B]

  implicit object EvictionOrder extends Ordering[(Seq[A], Int)] {
    def compare(x: (Seq[A], Int), y: (Seq[A], Int)) = x._2.compareTo(y._2)
  }

  private var evictQueue = new PriorityQueue[(Seq[A], Int)]

  def get(key: A) = {
    updateCounts()
    map.get(key)
  }

  def iterator = {
    updateCounts()
    map.iterator
  }

  def +=(kv: (A, B)) = {
    map += kv
    val key = kv._1
    val queueItem = evictQueue.lastOption match {
      case Some(x) => {
        if (x._2 == count) {
          evictQueue = evictQueue.dropRight(1)
          (x._1 ++ Seq(key), count)
        } else {
          (Seq(key), count)
        }
      }
      case None => (Seq(key), count)
    }
    evictQueue += queueItem
    println("added: " + evictQueue)
    this
  }
  
  private def updateCounts() {
    println("updating counts: " + evictQueue)
    evictQueue.headOption match {
      case Some(x) => {
        val keys = x._1
        val count = x._2
        evictQueue.dequeue()
        if (count > 0) {
          evictQueue += ((keys, count - 1))
        } else {
          keys.foreach(map.remove)
        }
      }
      case None =>
    }
  }

  def -=(key: A) = {
    map -= key
    this
  }
}