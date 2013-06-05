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
package com.rhythos.core.equipment;

import nme.display.BitmapData;
import nme.geom.Point;
import nme.geom.Rectangle;

class Equipment {
	public static var TYPES:Array<Int>;
	private var id:Int; public var phys_def:Float; public var mag_def:Float; public var allowHair:Bool;
	public function new(i:Int, p:Float, m:Float, a:Bool=true){id = i; phys_def = p; mag_def = m; allowHair = a;}
	public function draw(type:Int, b:BitmapData):Void {if(id >= 0) _draw(b, Main.getBitmap("equip", id, TYPES[type]));}
	public static inline function _draw(b1:BitmapData, b2:BitmapData):Void {
		b1.copyPixels(b2, new Rectangle(0,0,b1.width,b1.height), new Point(0,0), null, null, true);
	}
}
