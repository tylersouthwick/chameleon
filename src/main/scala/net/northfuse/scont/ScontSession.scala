package net.northfuse.scont

import javax.servlet.http.{HttpSession => Session, HttpServletResponse => Response, HttpServletRequest => Request}
import java.lang.ThreadLocal


/**
 * @author tylers2
 */
class ScontSession(session : Session, request : Request) {

	import ScontSession.ScontCallback
	val scontPrefix = "scont_"
	def add(callback : ScontCallback) = {
		val identifier = newIdentifier
		addToSession(identifier, callback)
		identifier
	}
	
	def addToSession(identifier : String,  callback : ScontCallback) {
		session.setAttribute(scontPrefix + identifier, callback)
	}
	
	def getFromSession(identifier : String) = {
		val callback = session.getAttribute(scontPrefix + identifier)
		if (callback == null) throw new IdentifierNotFoundException(identifier) else callback.asInstanceOf[ScontCallback]
	}
	
	def newIdentifier = java.util.UUID.randomUUID().toString
	
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
		case Some(identifier) => Some(getFromSession(identifier))
	}
	
	import collection.JavaConversions._
	def all = session.getAttributeNames.map(_.asInstanceOf[String]).filter(_.startsWith(scontPrefix)).map(_.substring(scontPrefix.length))
}

object ScontSession {
	type ScontCallback = (Request, Response) => Unit

	private val holder = new ThreadLocal[ScontSession]

	def session = holder.get()
	
	def apply(callback : ScontCallback) = session.add(callback)
	
	def apply(request : Request, response : Response, start : => Unit, errorHandle : PartialFunction[Throwable, Unit]) {
		val session = new ScontSession(request.getSession, request)
		holder.set(session)
		try {
			session.current match {
				case None => start
				case Some(callback) => callback(request, response)
			}
		} catch errorHandle
		finally {
			holder.remove()
		}
	}
}

class IdentifierNotFoundException(val identifier : String) extends RuntimeException(identifier + " not found")