/*******************************************************************************
 * Rhythos Game is the base game client which runs games exported from the editor.
 * 
 * Copyright (C) 2013  David Maletz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.davidmaletz.mrpg.ui;
import nme.events.KeyboardEvent;
import nme.display.Sprite;
import nme.events.Event;
import nme.ui.Keyboard;

class TextField extends Sprite {
	private var max_len:Int; private var chars:Array<Int>; private var len:Int; private var cursor:Int; private var selected:Bool; private var listener:TextField->Void;
	public function new(sz:Int, text:String, l:TextField->Void=null, len:Int=8){
		super(); Text._setColor(this,Frame.TEXT); Text.initTilesheet(); scaleX = scaleY = sz*0.125; max_len = len; chars = new Array<Int>();
		len = text.length; for(i in 0...len) chars.push(text.charCodeAt(i)); for(i in len...max_len) chars.push(32); update(); listener = l;
		selected = false; cursor = len; addEventListener(Event.ADDED_TO_STAGE, init); addEventListener(Event.REMOVED_FROM_STAGE, destroy);
	}
	private function init(e:Event):Void {Main.instance.stage.addEventListener(KeyboardEvent.KEY_DOWN, handleKey);}
	private function destroy(e:Event):Void {Main.instance.stage.removeEventListener(KeyboardEvent.KEY_DOWN, handleKey);}
	private function update(change:Bool=false):Void {
		var data:Array<Float> = new Array<Float>(); len = -1; for(i in 0...max_len){
			var c:Int = chars[i]; if(c != 32){data.push(8*i); data.push(0); data.push(c-32); len = i;}
		} len++; if(selected){data.push(8*cursor); data.push(2); data.push(95-32);} Text.draw(graphics, data, 0);
		if(change && listener != null) listener(this);
	}
	public function setSelected(b:Bool):Void {
		if(b != selected){selected = b; update(); Text._setColor(this,(b)?Status.YELLOW:Frame.TEXT);}
	}
	public function insert(c:Int):Void {if(cursor < max_len && c >= 32 && c <= 126){chars[cursor] = c; Main.playClick(); cursor++; update(true);}}
	public function moveCursor(d:Int):Void {Main.playClick(); cursor += d; if(cursor < 0) cursor = max_len-1; if(cursor > max_len) cursor = 0; update();}
	
	public function getString():String {
		var buf:StringBuf = new StringBuf(); for(i in 0...len) buf.addChar(chars[i]); return buf.toString();
	}
	private function handleKey(e:KeyboardEvent):Void {
		if(selected){switch(e.keyCode){
			case Keyboard.BACKSPACE: if(cursor > 0){Main.playClick(); cursor--; chars[cursor] = 32; update(true);}
			case Keyboard.LEFT: moveCursor(-1);
			case Keyboard.RIGHT: moveCursor(1);
			case Keyboard.DELETE: if(cursor < max_len){Main.playClick(); chars[cursor] = 32; update(true);}
			default: insert(e.charCode);
		}}
	}
}