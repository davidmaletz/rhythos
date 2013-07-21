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
package com.jhlabs.image;

import java.awt.Color;

public class Glow {
	public Color color; public int blurX, blurY; public float strength; public int quality;
	public Glow(){color = Color.black; blurX = 4; blurY = 4; strength = 0; quality = 0;}
	public Glow(Glow g){color = g.color; blurX = g.blurX; blurY = g.blurY; strength = g.strength; quality = g.quality;}
}
