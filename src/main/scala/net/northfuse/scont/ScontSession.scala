package net.northfuse.scont

import javax.servlet.http.{HttpSession => Session, HttpServletResponse => Response, HttpServletRequest => Request}
import java.lang.ThreadLocal


/**
 * @author tylers2
 */
class ScontSession(session : Session, request : Request) {

	import ScontSession.ScontCallback
  
	def add(callback : ScontCallback) = {
		val identifier = newIdentifier
		addToSession(identifier, callback)
		identifier
	}
	
  def map = {
    val m = session.getAttribute("scont_session")

    if (m == null) {
      val newM = new collection.mutable.HashMap[String, ScontCallback]
      session.setAttribute("scont_session", newM)
      newM
    } else {
      m.asInstanceOf[collection.mutable.HashMap[String, ScontCallback]]
    }
  }

	def addToSession(identifier : String,  callback : ScontCallback) {
    map.put(identifier, callback)
	}
	
	def getFromSession(identifier : String) = {
		map.get(identifier) match {
      case Some(callback) => callback
      case None => throw new IdentifierNotFoundException(identifier)
    }
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
	
	def all = map.keys
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
				case None => {
					println("No identifier found... invoking start page")
					start
				}
				case Some(callback) => {
					println("found identifier... invoking callback [" + callback + "]")
					callback(request, response)
				}
			}
		} catch errorHandle
		finally {
			holder.remove()
		}
	}
}

class IdentifierNotFoundException(val identifier : String) extends RuntimeException(identifier + " not found")