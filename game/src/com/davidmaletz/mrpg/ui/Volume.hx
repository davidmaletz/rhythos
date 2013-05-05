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
import nme.display.Graphics;
import nme.display.Sprite;

class Volume extends Sprite {
	private var bars:Array<Sprite>;
	public function new(v:Int) {
		super(); var b:Sprite = new Sprite(); var g:Graphics = b.graphics; bars = new Array<Sprite>(); g.beginFill(0x2a2a2a);
		for(i in 0...5){
			var s:Sprite = new Sprite(); g = s.graphics; g.beginFill(0xffffff); var h:Int = (i+1)*3; g.drawRect(i*12,16-h,6,h); g.endFill();
			g = b.graphics; g.drawRect(i*12-2,16-h,2,h); g.drawRect(i*12+6,16-h,2,h); g.drawRect(i*12-2,16,10,2); g.drawRect(i*12-2,16-h-2,10,2);
			s.visible = v>=i+1; bars.push(s); addChild(s);
		} g.endFill(); addChild(b);
	}
	public function set(v:Int):Void {
		for(i in 0...5) bars[i].visible = v>=i+1;
	}
}