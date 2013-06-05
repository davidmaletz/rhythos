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
import com.rhythos.core.game.Tile;
import nme.geom.ColorTransform;

class Project {
	public static function load():Void {//TODO: load from database.
		Tile.tile_size = 32; Main.instance.scaleX = Main.instance.scaleY = 1;
		Character.GENDERS = ["Male", "Female"];
		Character.SKIN_COLORS = ["Light", "Tanned", "Dark", "Darker", "Darkest"];
		Character.HAIR_STYLES =  [["Normal", "Short", "Long", "Messy 1", "Messy 2", "Mohawk", "Page", "Parted", "Bald"],
								  ["Normal", "Short", "Long", "Pixie", "Ponytail", "Princess", "Swoop", "Curly", "Bald"]];
		Character.HAIR_COLORS = ["Brown", "Blond", "Red", "Dark Brown", "Black", "White", "Pink", "Blue", "Green"];
		Character.EYE_COLORS = ["Brown", "Blue", "Green", "Gray", "Red"];
		Character.SKIN_MAT = [new ColorTransform(1,1,1,1,0,0,0,0), new ColorTransform(0.9,0.9,0.85,1,-10,-10,-10,0),
			new ColorTransform(0.9,0.9,0.9,1,-35,-35,-35,0), new ColorTransform(0.85,0.85,0.85,1,-52,-52,-52,0), new ColorTransform(0.8,0.8,0.8,1,-80,-70,-70,0)];
		Character.HAIR_MAT = [new ColorTransform(1,0.8,0.7,1,-5,-10,-50,0), new ColorTransform(1,1,0.7,1,0,0,-50,0),
			new ColorTransform(1,0.7,0.6,1,15,-20,-50,0), new ColorTransform(1,0.8,0.7,1,-60,-50,-70,0), new ColorTransform(0.7,0.7,0.7,1,-60,-60,-60,0),
			new ColorTransform(1,1,1,1,20,60,20,0), new ColorTransform(1,0.9,0.8,1,30,10,10,0), new ColorTransform(0.8,0.8,1,1,-40,0,20,0),
			new ColorTransform(0.7,0.95,0.5,1,-40,-20,-60,0)];
		Character.EYES_MAT = [new ColorTransform(0.9,0.6,0.5,1,15,-30,-60,0), new ColorTransform(1,1,1,1,0,0,0,0),
			new ColorTransform(1,0.9,0.6,1,15,15,-50,0), new ColorTransform(1,0.7,0.5,1,50,30,50,0), new ColorTransform(1,0,0,1,128,0,0,0)];
		Character.EYE_WHITE = 0xfff2f8f9;
	}
}