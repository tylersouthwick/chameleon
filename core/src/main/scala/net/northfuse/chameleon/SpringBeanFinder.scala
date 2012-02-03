package net.northfuse.chameleon

import org.springframework.context.ApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils
import javax.servlet.{Servlet, ServletConfig, ServletContext}

/**
 * @author Tyler Southwick
 */
trait SpringBeanFinder {
	def findBean[T](klass : Class[T]) = SpringBeanFinder.applicationContext.getBean(klass)
}

trait SpringServlet extends Servlet {
	abstract override def init(config: ServletConfig) {
		super.init(config)
		SpringBeanFinder.context_=(config.getServletContext)
	}
}

object SpringBeanFinder {
	private var applicationContext : ApplicationContext = null

	def context_=(servletContext : ServletContext) {
		applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
	}

	def findBean[T](implicit klass : Class[T]) = applicationContext.getBean(klass)

}