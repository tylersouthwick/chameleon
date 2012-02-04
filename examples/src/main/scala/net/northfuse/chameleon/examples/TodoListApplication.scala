package net.northfuse.chameleon.examples

import net.northfuse.chameleon._
import forms.Form
import themes._
import javax.servlet.http.{HttpServletRequest => Request}
import collection.JavaConversions._
import xml.NodeSeq

/**
 * @author tylers2
 */
object TodoListApplication extends ChameleonServlet with HTMLApplication with JettyRunner {

	private val LOG = org.slf4j.LoggerFactory.getLogger("net.northfuse.chameleon.examples.TodoListApplication")

	def homePage = listItems

	val items = new java.util.LinkedList[Item]

	import Application.ChameleonCallback
	import HTMLForm._

	def itemCountPanel = <div id="itemCountPanel">
		<p>You have {items.filter(_.finished).size} finished items and {items.filter(!_.finished).size} not finished!</p>
	</div>

	def listItems : ChameleonCallback = "Todo List" -> {
		<body>
		{
		if (items.isEmpty) {
			<p>You have no items</p>
		} else {
			<div>
			{itemCountPanel}
			<table>
				<thead>
					<tr>
						<th>Item</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					{
					items.map{item => 
						<tr>
							<td>{
								textbox.onchange(
									callback = { newName =>
										println("updateing name from [" + item.name + "] -> [" + newName + "]")
										item.name = newName
										Map()
									},
									value = item.name
								)}
							</td>
							<td>
								{checkbox.onclick(
									callback = { status =>
										item.finished = !item.finished
										Map (
											"itemCountPanel" -> itemCountPanel
										)
									},
									checked = item.finished
								)}
							</td>
						</tr>
					}
					}
				</tbody>
			</table>
				</div>
		}
		}
		<p>You can {link(callback = addItem, body ="add")} some items</p>
		</body>
	}
	
	def addItem = "Add Item" -> Form(saveItem) {
		<input name="itemName" />
		<input type="submit" value="Add Item" />
	}
	
	class Item {
		var name : String = null
		var finished = false
	}
	
	implicit object ItemParser extends RequestParser[Item] {
		def apply(request: Request) = new Item {
			name = request.getParameter("itemName")
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

	override def mappings = Map(
		"items" -> listItems,
		"newItem" -> addItem
	)

	override def filters = super.filters ++ Seq(theme)

}
