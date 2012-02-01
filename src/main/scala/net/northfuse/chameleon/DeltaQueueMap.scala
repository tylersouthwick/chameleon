package net.northfuse.scont

import collection.mutable._


/**
 * @author Tyler Southwick
 */
class DeltaQueueMap[A, B](count: Int) extends Map[A, B] {
	private val map = new HashMap[A, B]

	private var evictQueue = Seq[(Seq[A], Int)]()

	def evictSeq = evictQueue

	import DeltaQueueMap.LOG

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
					val totalValue = evictQueue.map(_._2).foldLeft(0){_+_}
					evictQueue ++ Seq((Seq(key), count - totalValue))
				}
			}
			case None => Seq((Seq(key), count))
		}
		LOG.trace("added: " + evictQueue)
		this
	}

	private def updateCounts() {
		if (LOG.isTraceEnabled) {
			LOG.trace("updating counts: " + evictQueue)
			LOG.trace("keys: " + keys)
		}
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

	def keysWithTimeout = evictQueue.foldLeft(Seq[(A, Int)]()) { case (seq, (keys, timeout)) => seq.lastOption match {
		case Some((lastKey, lastTimeout)) => {
			seq ++ keys.map((_, timeout + lastTimeout))
		}
		case None => seq ++ keys.map((_, timeout))
	}}
}

object DeltaQueueMap {
	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[DeltaQueueMap[_, _]])
}