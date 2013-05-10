/*******************************************************************************
 * Rhythos Game is the base game client which runs games exported from the editor.
 * 
 * Copyright (C) 2013  David Maletz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.davidmaletz.mrpg.ui;
import com.davidmaletz.mrpg.Character;
import nme.display.Sprite;

class SaveSlot extends Sprite {
	private var sel:Sprite; private var title:Text; public var char:SavedCharacter;
	public function new(i:Int, sc:SavedCharacter=null) {
		super(); char = sc; Frame.drawFrame(graphics, 304, 88); sel = new Sprite(); sel.visible = false; sel.graphics.beginFill(0xc0af7e);
		sel.graphics.drawRect(6,8,292,72); sel.graphics.drawRect(8,6,288,2); sel.graphics.drawRect(8,80,288,2); sel.graphics.endFill(); addChild(sel);
		title = new Text(Status.GRAY, 16, 0, 0); title.x = 84; title.y = 16; var desc:Text = new Text(Frame.TEXT, 12, 0, 0); desc.x = 84; desc.y = 46;
		var s:Sprite = new Sprite(); Frame.drawFrame(s.graphics, 64, 64, true); if(sc != null){var c = sc.getChar(true);
			c.x = 64; s.addChild(c); s.addChild(c.getTop()); title.setText(c.cname);
			desc.setText("L: "+Std.string(c.level+1)+"; G:"+Battle.format(Std.string(sc.gold)));
			var desc2:Text = new Text(Frame.TEXT, 12, 0, 0, "Progress: "+Std.int(sc.max_type*100/15+0.5)+"%"); desc2.x = 84; desc2.y = 60; addChild(desc2);
		} else{title.setText("Slot "+(i+1)); desc.setText("Empty.");} s.x = 12; s.y = 12; Frame.drawDivider(s.graphics, 64, 24, 222);
		addChild(title); addChild(desc); addChild(s);
	}
	public inline function setSelected(s:Bool):Void {sel.visible = s; title.setColor(s?Status.YELLOW:Status.GRAY);}
}