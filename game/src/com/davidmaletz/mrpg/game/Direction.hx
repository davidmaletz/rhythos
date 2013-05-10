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
package com.davidmaletz.mrpg.game;

/**
 * ...
 * @author David Maletz
 */

class Direction {
	public static inline var NONE:Int = 0; public static inline var LEFT:Int = 1; public static inline var RIGHT:Int = 2;
	public static inline var UP:Int = 4; public static inline var DOWN:Int = 8; public static inline var LINEAR:Int = 15;
	public static inline var UPPER_LEFT:Int = 16; public static inline var UPPER_RIGHT:Int = 32; public static inline var LOWER_LEFT:Int = 64;
	public static inline var LOWER_RIGHT:Int = 128; public static inline var ALL:Int = 255;
	public static inline function left(w:Int){return (w & LEFT) != 0;}
	public static inline function right(w:Int){return (w & RIGHT) != 0;}
	public static inline function up(w:Int){return (w & UP) != 0;}
	public static inline function down(w:Int){return (w & DOWN) != 0;}
	public static inline function upper_left(w:Int){return (w & UPPER_LEFT) != 0;}
	public static inline function upper_right(w:Int){return (w & UPPER_RIGHT) != 0;}
	public static inline function lower_left(w:Int){return (w & LOWER_LEFT) != 0;}
	public static inline function lower_right(w:Int){return (w & LOWER_RIGHT) != 0;}
	public static inline function hasLinear(w:Int){return (w & LINEAR) != 0;}
	public static inline function hasMask(w:Int, mask:Int){return (w & mask) != 0;}
	public static inline function isLinear(w:Int){return (w & LINEAR) == LINEAR;}
}