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
import nme.display.Sprite;
import nme.events.Event;

class ChoiceDialog extends Sprite {
	private var sel:Int; private var pause:Int; private var ids:Array<Int>; public var onSelect:Int->Bool; private var align:Int; private var arrow:Sprite;
	public function new(choices:Array<String>, selectable:Array<Bool>=null, func:Int->Bool=null, _align:Int=1, modal:Bool=true, ew:Int=16) {
		super(); var w:Int=0, _x:Float=28+ew*0.5, _y:Float=8, len:Int=choices.length, t:Text; ids = new Array<Int>(); align = _align+1;
		for(i in 0...len){
			var s:Bool = selectable == null || selectable[i]; t = new Text((s)?Status.GRAY:Frame.TEXT, 16, 400, 0, choices[i]);
			if(s) ids.push(i); t.x = _x; t.y = _y; if(t.length()*16 > w) w = t.length()*16; addChild(t); _y += 20;
		} w += ew+36; var h:Int = len*20+12; Frame.drawFrame(graphics,w,h,true); if(modal) pause = Main.pause(); else pause = 0;
		arrow = new Sprite(); t = new Text(Status.YELLOW, 16, 16, 0, "\202"); t.x = 8+ew*0.5; arrow.addChild(t); addChild(arrow);
		x = (400-w)*0.5; y = (align == 1)?-len*20+4:284; onSelect = func; sel = 0; updateSel(); Main.safeEnterFrame(this, handleKey);
	}
	public inline function getSelText():Text {return cast(getChildAt(ids[sel]), Text);}
	private inline function updateSel():Void {var t:Text = getSelText(); t.setColor(Status.YELLOW); arrow.y = t.y;}
	public function changeSel(d:Int):Void {
		getSelText().setColor(Status.GRAY); var len:Int = ids.length; sel += d;
		if(sel >= len) sel = 0; else if(sel < 0) sel = len-1; updateSel(); Main.playClick();
	}
	private inline function beginClose(){align = -align; if(pause > 0){Main.unpause(); pause = 0;}}
	private function select(i:Int):Void {if(onSelect == null || onSelect(i)) beginClose();}
	public function handleKey(e:Event):Void { var a:Int = (align < 0)?-align:align;
		var dh:Float, len:Int = ids.length; if(a == 1){
			dh = len*5; if(align < 0){y -= dh; if(y <= -len*20+4){if(onSelect != null) onSelect(-2); if(parent != null) parent.removeChild(this);} return;} if(y < 4){y += dh; return;}
		} else {
			var h:Float = 300-len*20; h = ((a == 2)?(h-12)*0.5:h-12); dh = (a == 3)?len*5:(284-h)*0.25;
			if(align < 0){y += dh; if(y >= 284){if(onSelect != null) onSelect(-2); if(parent != null) parent.removeChild(this);} return;} if(y > h){y -= dh; return;}
		} if(Main.isPressed(Main.ESCAPE, pause)){Main.resetPressed(pause); select(-1); if(align < 0) Main.playCancel(); return;}
		var u:Bool = Main.isPressed(Main.UP, pause), d:Bool = Main.isPressed(Main.DOWN, pause);
		if(u && !d) changeSel(-1); if(d && !u) changeSel(1); var ent:Bool = Main.isPressed(Main.ENTER, pause);
		Main.resetPressed(pause); if(ent){select(ids[sel]); Main.playSelect();}
	}
}