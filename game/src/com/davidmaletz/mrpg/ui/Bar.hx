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

class Bar extends Sprite {
	public static inline var HP:Int = 0; public static inline var MP:Int = 1; public static inline var COMBO:Int = 2;
	private var inner:Sprite;
	public function new(type:Int, flip:Bool){
		super(); inner = new Sprite(); var g:Graphics = inner.graphics, sx:Int=0, b:Sprite = null, c:Int = 0;
		switch(type){
		case HP:
			if(flip){sx = -150; inner.x = 152;} else inner.x = 2;
			g.beginFill(0xff5442); g.drawRect(sx,2,150,4); g.endFill();
			g.beginFill(0xe43c3c); g.drawRect(sx,6,150,4); g.endFill();
			g.beginFill(0xae424a); g.drawRect(sx,10,150,4); g.endFill();
			drawBorder(0x380510,150,12); 
		case MP:
			if(flip){sx = -110; inner.x = 112;} else inner.x = 2;
			g.beginFill(0x61a0ef); g.drawRect(sx,2,110,4); g.endFill();
			g.beginFill(0x5a72dd); g.drawRect(sx,6,110,4); g.endFill();
			drawBorder(0x2f1c40, 110,8);
		case COMBO:
			if(flip){sx = -120; inner.x = 122;} else inner.x = 2;
			g.beginFill(0xffed49); g.drawRect(sx,2,120,4); g.endFill();
			g.beginFill(0xffc63a); g.drawRect(sx,6,120,4); g.endFill();
			g.beginFill(0xff9c42); g.drawRect(sx,10,120,4); g.endFill();
			b = new Sprite(); c = 0x380e05; drawBorder(c,27,12,0,b.graphics); drawBorder(c,27,12,31,b.graphics);
			drawBorder(c,27,12,62,b.graphics); drawBorder(c,27,12,93,b.graphics); b.cacheAsBitmap = true;
		} addChild(inner); if(b != null) addChild(b);
	}
	private inline function drawBorder(col:Int, w:Int, h:Int, x:Int=0, g:Graphics=null):Void {
		if(g == null) g = graphics; graphics.beginFill(0xcccccc); graphics.drawRect(x+2,2,w,4); graphics.endFill();
		graphics.beginFill((h>8)?0xbbbbbb:0x999999); graphics.drawRect(x+2,6,w,4); graphics.endFill();
		if(h > 8){graphics.beginFill(0x999999); graphics.drawRect(x+2,10,w,4); graphics.endFill();}
		g.beginFill(col); g.drawRect(x+2,-2,w,4); g.drawRect(x+2,h+2,w,4); if(x == 0) g.drawRect(-2,2,4,h); g.drawRect(x+w+2,2,4,h);
		g.drawRect(x,0,2,2); g.drawRect(x+w+2,0,2,2); g.drawRect(x,h+2,2,2); g.drawRect(x+w+2,h+2,2,2); g.endFill();
	}
	public inline function getPercent():Float {return inner.scaleX;}
	public inline function setPercent(p:Float):Void {inner.scaleX = p;}
}