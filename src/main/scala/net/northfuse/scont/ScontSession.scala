package net.northfuse.scont

import javax.servlet.http.{HttpSession => Session, HttpServletResponse => Response, HttpServletRequest => Request}
import java.lang.ThreadLocal


/**
 * @author tylers2
 */
class ScontSession(session : Session) {
	def add(callback : (Request, Response) => Unit) = {
		val identifier = newIdentifier
		addToSession(identifier, callback)
		identifier
	}
	
	def addToSession(identifier : String,  callback : (Request, Response) => Unit) {
		session.setAttribute("scont_" + identifier, callback)
	}
	
	def getFromSession(identifier : String) = session.getAttribute("scont_" + identifier).asInstanceOf[(Request,  Response) => Unit]
	
	def newIdentifier = java.util.UUID.randomUUID().toString
}

object ScontSession {
	private val holder = new ThreadLocal[ScontSession]
	
	def apply() = holder.get()
	
	def apply(session : ScontSession, callback : => Unit) {
		holder.set(session)
		try {
			callback
		} catch {
			holder.remove
		}
	}
	
	def apply(session : Session, callback : => Unit) {
		apply(new ScontSession(session), callback)
	}
}