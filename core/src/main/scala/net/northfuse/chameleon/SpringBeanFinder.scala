package net.northfuse.chameleon

import org.springframework.context.ApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils
import javax.servlet.{Servlet, ServletConfig, ServletContext}

/**
 * @author Tyler Southwick
 */
trait SpringBeanFinder {
	def findBean[T](klass : Class[T]) = SpringServlet.findBean(klass)
}

trait SpringServlet extends Servlet {
	abstract override def init(config: ServletConfig) {
		super.init(config)
		SpringServlet.context_=(config.getServletContext)
	}
}

object SpringServlet {
	private val LOG = org.slf4j.LoggerFactory.getLogger(classOf[SpringServlet])
	private var applicationContext : ApplicationContext = null

	private def context_=(servletContext : ServletContext) {
		LOG.info("Initializing Application Context [" + servletContext.getServletContextName + "]")
		applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
	}

	def findBean[T](klass : Class[T]) = applicationContext.getBean(klass)
}