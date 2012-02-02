package net.northfuse.chameleon

import javax.servlet.http.{HttpSession => Session, HttpServletResponse => Response, HttpServletRequest => Request}
import java.lang.ThreadLocal
import java.math.BigInteger


/**
 * @author tylers2
 */
class ChameleonSession(session: Session, request: Request) {

	import ChameleonSession.ChameleonCallback
	val sessionAttribute = "chameleonSession"

	def add(callback: ChameleonCallback) = {
		val identifier = newIdentifier
		addToSession(identifier, callback)
		identifier
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

	val currentIdentifier = {
		val id = {
			val pathInfo = request.getPathInfo
			if (pathInfo != null && pathInfo.startsWith("/")) {
				pathInfo.substring(1)
			} else pathInfo
		}
		if (id == null || id.trim().isEmpty) None else Some(id)
	}

	def current = currentIdentifier match {
		case None => None
		case Some(identifier) => Some((identifier, getFromSession(identifier)))
	}

	def all = lock {map.keysWithTimeout}

	def lock[T](callback : => T) = {
		session synchronized {
			callback
		}
	}
}

object ChameleonSession {
	type ChameleonCallback = (Request, Response) => Unit
	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[ChameleonSession])

	private val holder = new ThreadLocal[ChameleonSession]

	def session = holder.get()

	def apply(callback: ChameleonCallback) = session.add(callback)

	def apply(request: Request, response: Response, start: => Unit, errorHandle: PartialFunction[Throwable, Unit]) {
		val session = new ChameleonSession(request.getSession, request)
		holder.set(session)
		try {
			session.current match {
				case None => {
					LOG.debug("No identifier found... invoking start page")
					start
				}
				case Some((identifier, callback)) => {
					LOG.debug("found identifier... invoking callback [" + identifier + "]")
					callback(request, response)
				}
			}
		} catch errorHandle
		finally {
			holder.remove()
		}
	}
}

class IdentifierNotFoundException(val identifier: String) extends RuntimeException(identifier + " not found")