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
import nme.display.Sprite;
import nme.events.Event;

class DialogBox extends Sprite {
	/**
	 * Special Characters:
	 * /000 - /007: Change the current color.
	 * /200 - /277: Special characters.
	 * /300: Display all text until next /300 instantly.
	 * /301 - /375: Wait for x frames (where x is how much larger than /200 it is).
	 * /376: Automatically end the message at this point.
	 * /377: Automatically end the message at this point after any later modal messages are gone.
	 */
	private static inline var ARROW_CT:Int=8;
	private static var colors:Array<Int> = [Status.GRAY, Status.RED, Status.BLUE, Status.GREEN, Status.YELLOW, Frame.TEXT];
	private var text:String; private var cur:Int; private var line:Int; private var col:Int; private var wait:Int; private var lw:Int;
	public var onClose:DialogBox->Bool; private var arrow:Text; private var arrowCt:Int; private var pause:Int; private var align:Int;
	private var num_lines:Int; private var alignh:Int;
	public function new(t:String=null, func:DialogBox->Bool=null, modal:Bool=true, _align:Int=0, lineWidth:Int=24, rows:Int=4, _alignh:Int=0, dialog:Bool=true) {
		super(); lw = lineWidth*16; num_lines = rows; Frame.drawFrame(graphics, lw+16, num_lines*20+12, dialog); onClose = func;
		if(modal) pause = Main.pause(); else pause = 0; align = _align; alignh = _alignh; x = (Main.width-lw-16)*0.5;
		for(i in 0...num_lines){var s:Sprite = new Sprite(); s.x = 8; s.y = 8+20*i; addChild(s);}
		arrow = new Text(Status.YELLOW, 16, lw+16, 1, "\177"); arrow.visible = false; arrow.y = num_lines*20+2; addChild(arrow);
		if(t != null) setText(t); y = (align == 0)?-num_lines*20+4:Main.height-16; Main.safeEnterFrame(this, enter_frame);
	}
	public function setText(t:String):Void {
		text = t; cur = 0; line = 0; col = colors[0]; wait = 0; arrow.visible = false; arrowCt = ARROW_CT;
		for(i in 0...num_lines) Main.removeAllChildren(cast(getChildAt(i),Sprite)); nextChar();
	}
	public function getFontSz(l:Int):Int {return 16;}
	private function addText(l:Sprite):Void {
		var sz:Int = getFontSz(line); if(l.numChildren == 0) l.addChild(new Text(col, sz, lw, 0)); var t:Text = cast(l.getChildAt(l.numChildren-1), Text);
		var t2:Text = new Text(col, sz, lw, 0); t2.x = t.x+t.length()*sz; l.addChild(t2);
	}
	private inline function beginClose(){wait = -1; if(pause > 0){Main.unpause(); pause = 0;}}
	private inline function callFunc():Bool {var f:DialogBox->Bool = onClose; onClose = null; return f == null || f(this);}
	public inline function close(modal:Bool=false):Void {arrow.visible = false; if(callFunc()){if(modal) wait = -2; else beginClose();}}
	private function nextChar():Void {
		var show:Bool = false, next:Bool = false; do{next = false; var c:Int = text.charCodeAt(cur);
		if(c == 10) line++; else if(c <= colors.length){
			next = true; var color:Int; if(c == colors.length) color = (text.charCodeAt(++cur)<<16)|(text.charCodeAt(++cur)<<8)|text.charCodeAt(++cur); else color = colors[c];
			if(color != col){col = color; addText(cast(getChildAt(line), Sprite));}
		} else if(c == 254) close(); else if(c == 255) close(true); else if(c == 192) show = !show; else if(c > 192) wait = c-192; else {
			var l:Sprite = cast(getChildAt(line), Sprite), sz:Int=getFontSz(line); if(l.numChildren == 0){
				l.addChild(new Text(col, sz, lw, 0, String.fromCharCode(c))); if(alignh != 0) l.x = 8+(lw-16)*((alignh==1)?0.5:1.0);
			} else {
				var t:Text = cast(l.getChildAt(l.numChildren-1), Text); t.appendChar(c); var len:Float = t.x+t.length()*sz;
				if(len >= lw){if(cur >= text.length-1 || text.charCodeAt(cur+1) != 10) line++;}
				if(alignh != 0) l.x = 8+(lw-len)*((alignh==1)?0.5:1.0);
			}
		} cur++;} while(show || next);
	}
	public function enter_frame(e:Event):Void {
		if(pause != 0) Main.resetPressed(pause); if(wait == -2){if(Main.pauseLevel() == pause) beginClose(); else return;}
		var dh:Float; if(align == 0){
			dh = num_lines*5; if(wait == -1){y -= dh; if(y <= -num_lines*20+4){callFunc(); parent.removeChild(this);} return;} if(y < 4){y += dh; return;}
		} else {
			var h:Float = Main.height-num_lines*20; h = ((align == 1)?(h-12)*0.5:h-12); dh = (align == 2)?num_lines*5:((Main.height-16)-h)*0.25;
			if(wait == -1){y += dh; if(y >= (Main.height-16)){callFunc(); parent.removeChild(this);} return;} if(y > h){y -= dh; return;}
		} var esc:Bool = Main.isHeld(Main.ESCAPE, pause); if(esc) wait = 0; else if(wait > 0){wait--; return;} if(text == null || cur >= text.length || line >= num_lines){
			if(text != null){
				arrowCt--; if(arrowCt <= 0){arrowCt = ARROW_CT; arrow.visible = !arrow.visible;}
				if(Main.isHeld(Main.ENTER, pause) || Main.isHeld(Main.DOWN, pause) || esc) close();
			} return;
		} nextChar(); if(esc){if(cur < text.length && line < num_lines) nextChar(); if(cur < text.length && line < num_lines) nextChar();}
	}
	public static function list(msgs:Array<String>, func:DialogBox->Bool=null):DialogBox->Bool {
		var i:Int = 0; var ret:DialogBox->Bool = null; function _func(d:DialogBox):Bool {
			if(i < msgs.length){d.setText(msgs[i++]); d.onClose = ret; return false;} else if(func != null) return func(d); else return true;
		} ret = _func; return ret;
	}
}