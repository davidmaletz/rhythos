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
package com.davidmaletz.mrpg.ui;
import nme.display.BitmapData;
import nme.display.Graphics;
import nme.geom.Matrix;

 @:bitmap("assets/frame.gif") class FRAME extends BitmapData {}
 @:bitmap("assets/bg.png") class BG extends BitmapData {}

class Frame {
	public static var MID:Int; public static var MID_DIALOG:Int=0x88000000; public static inline var TEXT:Int = 0xa99fa0;
	private static var F1:Int; private static var F2:Int; private static var F3:Int; private static var FS:Int;
	private static var frame:BitmapData; private static var frame_dialog:BitmapData; public static var background:BitmapData;
	
	public static function drawFrame(g:Graphics, w:Int, h:Int, dialog:Bool=false):Void {
		var f:BitmapData = (dialog)?frame_dialog:frame, m:Int = (dialog)?(MID_DIALOG&0xffffff):MID, a:Float = (dialog)?((MID_DIALOG>>24)&0xFF)/255:1;
		g.beginBitmapFill(f, new Matrix(2,0,0,2,0,0),false); g.drawRect(0,0,12,12); g.endFill();
		g.beginBitmapFill(f, new Matrix(2,0,0,2,w-24,0),false); g.drawRect(w-12,0,12,12); g.endFill();
		g.beginBitmapFill(f, new Matrix(2,0,0,2,w-24,h-24),false); g.drawRect(w-12,h-12,12,12); g.endFill();
		g.beginBitmapFill(f, new Matrix(2,0,0,2,0,h-24),false); g.drawRect(0,h-12,12,12); g.endFill();
		g.beginFill(m,a); g.drawRect(8,12,w-14,h-24); g.drawRect(12,8,w-24,4); g.drawRect(12,h-12,w-24,6); g.endFill();
		g.beginFill(F1); g.drawRect(0,12,2,h-24); g.drawRect(w-6,12,2,h-24); g.drawRect(12,0,w-24,2); g.drawRect(12,h-6,w-24,2); g.endFill();
		g.beginFill(F2); g.drawRect(2,12,2,h-24); g.drawRect(w-4,12,2,h-24); g.drawRect(12,2,w-24,2); g.drawRect(12,h-4,w-24,2); g.endFill();
		g.beginFill(F3); g.drawRect(4,12,2,h-24); g.drawRect(w-2,12,2,h-24); g.drawRect(12,4,w-24,2); g.drawRect(12,h-2,w-24,2); g.endFill();
		g.beginFill((dialog)?m:FS,a); g.drawRect(6,12,2,h-24); g.drawRect(12,6,w-24,2); g.endFill();
	}
	
	public static function drawDivider(g:Graphics, x:Int, y:Int, w:Int, capl:Bool=false, capr:Bool=false):Void {
		g.beginFill(F1); g.drawRect(x,y,w,2); g.endFill(); g.beginFill(F2); var d:Int = ((capl)?-2:2); g.drawRect(x-d,y+2,w+d+((capr)?-2:2),2); g.endFill();
		g.beginFill(F3); g.drawRect(x,y+4,w,2); g.endFill(); g.beginFill(FS); g.drawRect(x+2,y+6,w-2,2); g.endFill();
		if(capl){g.beginFill(F1); g.drawRect(x,y+2,2,2); g.endFill();} if(capr){
			g.beginFill(F1); g.drawRect(x+w-2,y+2,2,2); g.endFill(); g.beginFill(FS); g.drawRect(x+w,y+2,2,6); g.endFill();
		}
	}
	public static function drawDividerV(g:Graphics, x:Int, y:Int, h:Int):Void {
		g.beginFill(F1); g.drawRect(x,y,2,h); g.endFill(); g.beginFill(F2); g.drawRect(x+2,y-2,2,h+4); g.endFill();
		g.beginFill(F3); g.drawRect(x+4,y,2,h); g.endFill(); g.beginFill(FS); g.drawRect(x+6,y,2,h); g.endFill();
	}
	private static function floodFill(x:Int, y:Int):Void {
		var c:Int = frame_dialog.getPixel32(x,y); if(c == MID || c == FS){
			frame_dialog.setPixel32(x,y,MID_DIALOG); floodFill(x-1,y); floodFill(x,y-1); floodFill(x+1,y); floodFill(x,y+1);
		}
	}
	public static function init():Void {
		frame = new FRAME(12,12,true,0);
		background = new BG(200,150,false,0);
		MID = frame.getPixel32(6,6); F1 = frame.getPixel32(6,0); F2 = frame.getPixel32(6,1);
		F3 = frame.getPixel32(6,2); FS = frame.getPixel32(6,3); frame_dialog = frame.clone(); floodFill(6, 6);
	}
}