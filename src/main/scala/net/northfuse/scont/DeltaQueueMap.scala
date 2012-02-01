package net.northfuse.scont

import collection.mutable._


/**
 * @author Tyler Southwick
 */
class DeltaQueueMap[A, B](count: Int) extends Map[A, B] {
	private val map = new HashMap[A, B]

	private var evictQueue = Seq[(Seq[A], Int)]()

	def evictSeq = evictQueue

	def get(key: A) = {
		map.get(key) match {
			case Some(value) => {
				updateCounts()
				Some(value)
			}
			case None => None
		}
	}

	def iterator = {
		updateCounts()
		map.iterator
	}

	def +=(kv: (A, B)) = {
		map += kv
		val key = kv._1
		evictQueue = evictQueue.lastOption match {
			case Some(x) => {
				//if the last entry has the same count
				if (x._2 == count) {
					//remove the last element in the list and append a new tuple with the old list + the new key, and the old count
					evictQueue.reverse.tail.reverse ++ Seq((Seq(key) ++ x._1, x._2))
				} else {
					//append the new one to the end of the list
					evictQueue ++ Seq((Seq(key), count - x._2))
				}
			}
			case None => Seq((Seq(key), count))
		}
		println("added: " + evictQueue)
		this
	}

	private def updateCounts() {
		println("updating counts: " + evictQueue)
		println("keys: " + keys)
		evictQueue = evictQueue.headOption match {
			case Some(x) => {
				val keys = x._1
				val count = x._2
				if (count > 1) {
					Seq((keys, count - 1)) ++ evictQueue.tail
				} else {
					keys.foreach(map.remove)
					evictQueue.tail
				}
			}
			case None => evictQueue
		}
	}

	def -=(key: A) = {
		map -= key
		this
	}

	override def keys = map.keys
}