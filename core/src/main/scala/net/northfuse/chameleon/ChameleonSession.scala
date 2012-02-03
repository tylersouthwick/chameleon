package net.northfuse.chameleon

import javax.servlet.http.{HttpSession => Session, HttpServletRequest => Request}
import java.math.BigInteger
import Application.ChameleonCallback

/**
 * @author tylers2
 */
class ChameleonSession(identifierHandler : IdentifierHandler, session: Session, request: Request, mappings : Map[String,  ChameleonCallback]) {

	val sessionAttribute = "chameleonSession"

	/**
	 * Adds the callback to the session and returns a URL to access it.
	 * @param callback The callback to execute
	 * @return A URL
	 */
	def add(callback: ChameleonCallback) = {
		val identifier = newIdentifier
		addToSession(identifier, callback)
		identifierHandler.buildUrl(identifier, request)
	}

	def map = lock {
		val m = session.getAttribute(sessionAttribute)

		if (m == null) {
			val newM = new DeltaQueueMap[String, ChameleonCallback](10)
			session.setAttribute(sessionAttribute, newM)
			newM
		} else {
			m.asInstanceOf[DeltaQueueMap[String, ChameleonCallback]]
		}
	}

	def addToSession(identifier: String, callback: ChameleonCallback) {
		lock {
			map.put(identifier, callback)
		}
	}

	def getFromSession(identifier: String) = lock {
		map.get(identifier) match {
			case Some(callback) => callback
			case None => throw new IdentifierNotFoundException(identifier)
		}
	}

	def newIdentifier = {
		val guid = java.util.UUID.randomUUID().toString.replaceAllLiterally("-", "")
		new BigInteger(guid, 16).toString(36)
	}

	val currentIdentifier = identifierHandler.findIdentifier(request)

	def current = currentIdentifier match {
		case None => None
		case Some(identifier) => {
			mappings.get(identifier) match {
				case Some(callback) => Some((identifier, callback))
				case None => Some((identifier, getFromSession(identifier)))
			}
		}
	}

	def all = lock {map.keysWithTimeout}

	def lock[T](callback : => T) = {
		session synchronized {
			callback
		}
	}
}

class IdentifierNotFoundException(val identifier: String) extends RuntimeException(identifier + " not found")

trait IdentifierHandler {
	def findIdentifier(request : Request) : Option[String]

	def buildUrl(identifier : String, request : Request) : String
}

