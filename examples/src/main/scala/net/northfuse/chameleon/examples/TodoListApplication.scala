package net.northfuse.chameleon.examples

import net.northfuse.chameleon._
import HTMLApplication._
import forms.Form
import themes._
import javax.servlet.http.{HttpServletRequest => Request}
import collection.JavaConversions._
import jQueryUI.AlertBlock
import xml.{Elem, NodeSeq}

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

	def notices(body : NodeSeq) = "itemNotices" -> <div id="itemNotices">{body}</div>

	def listItems : ChameleonCallback = page("Todo List") {
		<body>
			{notices(NodeSeq.Empty)._2}
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
										Map(
											notices(AlertBlock("Updated name"))
										)
									},
									value = item.name
								)}
							</td>
							<td>
								{checkbox.onclick(
									callback = { status =>
										item.finished = !item.finished
										Map (
											notices(AlertBlock("You finished something!")),
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
	
	def addItem = page("Add Item") {
		<body>
			{Form(saveItem) {
			<input name="itemName" />
			<input type="submit" value="Add Item" />
			}}
		</body>
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
	
	def saveItem : ChameleonCallback = formHandler ("Item Saved") {(item : Item) => {
		items.add(item)
		<p>Item Saved!</p>
		<p>{link(callback = listItems, body = "Return to List")}</p>
	}}
	
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
