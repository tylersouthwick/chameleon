package net.northfuse.chameleon.themes.jQueryUI

import xml.{NodeSeq, Elem}
import java.util.UUID


/**
 * @author tylers2
 */
abstract class MessageBlock(iconStyle : String) {

	def apply(message : NodeSeq) = {
		<div class="ui-widget">
			<div class="ui-state-highlight ui-corner-all" style="margin-top: 20px; padding: 0 .7em">
				<p>
					<span class={"ui-icon ui-icon-" + iconStyle} style="float: left; margin-right: .3em;"></span>
					{message}
				</p>
			</div>
		</div>
	}

	def apply(message : String) : Elem = apply(<span>{message}</span>)
}

object InfoBlock extends MessageBlock("info")
object AlertBlock extends MessageBlock("alert")

