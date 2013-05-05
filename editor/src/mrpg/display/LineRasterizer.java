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
package mrpg.display;

public class LineRasterizer {
	private int x, y, d, incXL, incXH, incYL, incYH, incDL, incDH, i, maxi;
	public LineRasterizer(int x1, int y1, int x2, int y2, int _dx, int _dy){
		x = x1; y = y1;
		int dx = (x2-x1)/_dx, dy = (y2-y1)/_dy;
		if(dx >= 0) incXL = incXH = _dx;
		else {dx = -dx; incXL = incXH = -_dx;}
		if(dy >= 0) incYL = incYH = _dy;
		else {dy = -dy; incYL = incYH = -_dy;}
		int longD, shortD;
		if(dx >= dy){longD = dx; shortD = dy; incYL = 0;}
		else {longD = dy; shortD = dx; incXL = 0;}
		d = 2*shortD-longD; incDL = 2*shortD; incDH = incDL-2*longD;
		i = 0; maxi = longD;
	}
	public int getX(){return x;}
	public int getY(){return y;}
	public boolean hasNext(){return i <= maxi;}
	public void next(){
		if(d >= 0){x += incXH; y += incYH; d += incDH;}
		else {x += incXL; y += incYL; d += incDL;}
		i++;
	}
}
