package net.northfuse.chameleon.examples

import net.northfuse.chameleon._
import themes._
import javax.servlet.http.{HttpServletRequest => Request}
import collection.JavaConversions._

/**
 * @author tylers2
 */
object TodoListApplication extends ChameleonServlet with HTMLView with JettyRunner {

	private val LOG = org.slf4j.LoggerFactory.getLogger("net.northfuse.chameleon.examples.TodoListApplication")

	def homePage = listItems

	val items = new java.util.LinkedList[Item]

	def listItems : PageWithTitle = "Todo List" -> {
		<body>
		{
		if (items.isEmpty) {
			<p>You have no items</p>
		} else {
			<p>You have {items.size} items</p>
			<table>
				<thead>
					<tr>
						<td>Item</td>
					</tr>
				</thead>
				<tbody>
					{
					items.map{item => 
						<tr><td>{item.name}</td></tr>
					}
					}
				</tbody>
			</table>
		}
		}
		<p>You can {link(callback = addItem, body ="add")} some items</p>
		</body>
	}
	
	def addItem = "Add Item" -> form (saveItem, {
		<input name="itemName" />
		<input type="submit" value="Add Item" />
	})
	
	trait Item {
		def name : String
	}
	
	implicit object ItemParser extends RequestParser[Item] {
		def apply(request: Request) = new Item {
			val name = request.getParameter("itemName")
		}
	}
	
	def saveItem = parser2((item : Item) => "Item Saved" -> {
		items.add(item)
		<p>Item Saved!</p>
		<p>{link(callback = listItems, body = "Return to List")}</p>
	})
	
	val clarityTheme = ClarityTheme(
		title = "Todo List Application",
		footer = <p>Copyright northfuse.net</p>,
		links = Seq(
			"Items" -> listItems,
			"New Item" -> addItem
		)
	)

	def theme = clarityTheme

	override def filters = super.filters ++ Seq(theme)

}
