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
package com.rhythos.core;

class BoolArray {
	private var ar:Int; public function new(){ar = 0;}
	private static function pow2(i:Int):Int {return 1<<i;}
	public inline function get(i:Int):Bool {return (ar&pow2(i))!=0;}
	public inline function set(i:Int, b:Bool):Void {if(b) ar |= pow2(i); else ar &= ~pow2(i);}
	public inline function getAr():Int {return ar;}
	public inline function setAr(a:Int):Void {ar = a;}
	public inline function hasAll(n:Int):Bool {return pow2(n)-1==ar;}
}