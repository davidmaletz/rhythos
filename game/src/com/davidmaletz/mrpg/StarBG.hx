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
package com.davidmaletz.mrpg;
import nme.display.BitmapData;
import nme.display.Sprite;
import nme.display.TriangleCulling;
import nme.events.Event;
import nme.geom.Rectangle;
import nme.Vector;

 @:bitmap("assets/star.png") class STAR extends BitmapData {}
 
class StarBG extends Sprite {
	private static var N_STARS:Int = 200; private static var SZ:Int = 16; private static var SIZE:Int = 60; private static var SPEED:Float = 0.03;
	private static var star:BitmapData; private var buffer:BitmapData; private var frame_ct:Int; private var stars:Sprite;
	private var pos:Array<Float>; private var vertices:Vector<Float>; private var idx:Vector<Int>; private var uv:Vector<Float>;
	public function new(){
		super(); if(star == null) star = new STAR(14,14,true,0); frame_ct = 1; stars = new Sprite(); var n:Int;
		pos = new Array<Float>(); vertices = new Vector<Float>(); idx = new Vector<Int>(); uv = new Vector<Float>(); for(i in 0...N_STARS){
			var r:Float = (1-Math.sqrt(Math.random()))*(100-SIZE*0.5)+SIZE*0.5, a:Float = Math.random()*2*Math.PI; var x:Float = r*Math.cos(a)+100, y:Float = r*Math.sin(a)+75;
			pos.push(x); pos.push(y); vertices.push(0); vertices.push(0);
			vertices.push(0); vertices.push(0); vertices.push(0); vertices.push(0); vertices.push(0); vertices.push(0);
			n = i*4; idx.push(n); idx.push(n+1); idx.push(n+2); idx.push(n); idx.push(n+2); idx.push(n+3);
			uv.push(0); uv.push(0); uv.push(1); uv.push(0); uv.push(1); uv.push(1); uv.push(0); uv.push(1);
		}
		
		vertices.push(100-SZ); vertices.push(75-SZ); vertices.push(100+SZ); vertices.push(75-SZ);
		vertices.push(100+SZ); vertices.push(75+SZ); vertices.push(100-SZ); vertices.push(75+SZ);
		n = N_STARS*4; idx.push(n); idx.push(n+1); idx.push(n+2); idx.push(n); idx.push(n+2); idx.push(n+3);
		uv.push(0); uv.push(0); uv.push(1); uv.push(0); uv.push(1); uv.push(1); uv.push(0); uv.push(1);
		
		buffer = new BitmapData(200,150,false,0); Main.safeEnterFrame(this, enter_frame); enter_frame(null);
	}
	private inline function put(x:Float, y:Float, ct:Int):Int {
		var dx:Float = (x-100)/100, dy:Float = (y-75)/100, dist:Float = dx*dx+dy*dy; dist *= Math.sqrt(dist);
		vertices[ct++] = (x-100)*dist+100; vertices[ct++] = (y-75)*dist+75; return ct;
	}
	private function enter_frame(e:Event):Void {
		frame_ct--; if(frame_ct == 0){
			var ct:Int = 0; for(i in 0...N_STARS){
				var x:Float = pos[2*i]-100, y:Float = pos[2*i+1]-75; x += x*SPEED+100; y += y*SPEED+75;
				if(x < -6 || y < -56 || x > 406 || y > 356){
					var a:Float = Math.random()*2*Math.PI; x = SIZE*Math.cos(a)+100; y = SIZE*Math.sin(a)+75;
				} pos[2*i] = x; pos[2*i+1] = y;
				ct = put(x-3,y-3,ct); ct = put(x+3,y-3,ct); ct = put(x+3,y+3,ct); ct = put(x-3,y+3,ct);
			} var g=stars.graphics; g.clear(); g.beginBitmapFill(star,null,false,true);
			g.drawTriangles(vertices, idx, uv, TriangleCulling.NONE); g.endFill();
			buffer.fillRect(new Rectangle(0,0,200,150),0); buffer.draw(stars);
			graphics.clear(); graphics.beginBitmapFill(buffer); graphics.drawRect(0,0,200,150); graphics.endFill(); frame_ct = 2;
		}
	}
}