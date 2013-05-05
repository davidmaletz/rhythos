/*******************************************************************************
 * Rhythos Editor is a game editor and project management tool for making RPGs on top of the Rhythos Game system.
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
package mrpg.world;

public class Direction {
	public static int NONE = 0, LEFT = 1, RIGHT = 2, UP = 4, DOWN = 8, LINEAR = 15;
	public static int UPPER_LEFT = 16, UPPER_RIGHT = 32, LOWER_LEFT = 64, LOWER_RIGHT = 128, ALL = 255;
	public static boolean left(int w){return (w & LEFT) != 0;}
	public static boolean right(int w){return (w & RIGHT) != 0;}
	public static boolean up(int w){return (w & UP) != 0;}
	public static boolean down(int w){return (w & DOWN) != 0;}
	public static boolean upper_left(int w){return (w & UPPER_LEFT) != 0;}
	public static boolean upper_right(int w){return (w & UPPER_RIGHT) != 0;}
	public static boolean lower_left(int w){return (w & LOWER_LEFT) != 0;}
	public static boolean lower_right(int w){return (w & LOWER_RIGHT) != 0;}
	public static boolean hasLinear(int w){return (w & LINEAR) != 0;}
	public static boolean isLinear(int w){return (w & LINEAR) == LINEAR;}
}
