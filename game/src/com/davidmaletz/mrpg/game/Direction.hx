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