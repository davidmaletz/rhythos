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
package com.rhythos.core.game;
import com.rhythos.core.Character;
import nme.display.Sprite;
import nme.events.Event;

/**
 * ...
 * @author David Maletz
 */

class Game extends Sprite {
	private var player:Character; private var playerX:Int; private var playerY:Int; private var map:Map;
	//private var enemy:Character; TODO: event handling, collision, it shouldn't be done with a static variable!
	public function new() {
		super(); map = Map.get("50425635ce09bb53"); addChild(map); player = Main.getPlayer(); playerX = 0; playerY = 1; resetPlayer(); Main.safeEnterFrame(this, enter_frame);
		//TODO: read what layer the player should render on in the map, then add the player to that layer (sorting all events by y)
	}
	public function resetPlayer():Void { //TODO: play map bgm Main.playBGM(Main.getBGM("menu"));
		player.reset(); player.scaleX = player.scaleY = 1; var f:Int = Character.WALK_ST+Character.WALK_LEN*Character.DOWN; player.setFrame(f);
		player.queueFrame(f, checkMove); player.setPos(playerX,playerY); map.add(player); player.enter_frame(null); map.centerChar(player);
	}
	private function bump(x:Int, y:Int):Void {}
	private function tryMoveDown():Bool {bump(playerX, playerY+1); return map.canMoveDown(player);}
	private function tryMoveUp():Bool {bump(playerX, playerY-1); return map.canMoveUp(player);}
	private function tryMoveRight():Bool {bump(playerX+1, playerY); return map.canMoveRight(player);}
	private function tryMoveLeft():Bool {bump(playerX-1, playerY); return map.canMoveLeft(player);}
	private function checkMove(c:Character):Void {
		playerX = player.getX(); playerY = player.getY();
		var r:Bool = Main.isHeld(Main.RIGHT), l:Bool = Main.isHeld(Main.LEFT), u:Bool = Main.isHeld(Main.UP), d:Bool = Main.isHeld(Main.DOWN);
		var dir:Int = player.getDir(); if(d && !u){if(tryMoveDown()){queuePlayerWalk(Character.DOWN); return;} dir = Character.DOWN;}
		if(u && !d){if(tryMoveUp()){queuePlayerWalk(Character.UP); return;} dir = Character.UP;}
		if(r && !l){if(tryMoveRight()){queuePlayerWalk(Character.RIGHT); return;} dir = Character.RIGHT;}
		if(l && !r){if(tryMoveLeft()){queuePlayerWalk(Character.LEFT); return;} dir = Character.LEFT;}
		player.queueFrame(Character.WALK_ST+Character.WALK_LEN*dir, checkMove);
	}
	private function doneMove():Void {}
	private function queuePlayerWalk(dir:Int):Void {
		var f:Int = queueWalk(player, dir); var func = player.getQueuedFunc(f+3);
		player.setFunction(f+3, function(c:Character):Void {func(c); doneMove(); checkMove(c);});
	}
	private function queueWalk(char:Character, dir:Int):Int {
		var moveChar:Character->Void = null; var delta:Float = Tile.tile_size/4, frame:Int = char.getFrame();
		switch(dir){
			case Character.UP: moveChar = function(c:Character):Void{char.y -= delta; if(char == player) map.centerChar(char);}
			case Character.DOWN: moveChar = function(c:Character):Void{char.y += delta; if(char == player) map.centerChar(char);}
			case Character.LEFT: moveChar = function(c:Character):Void{char.x -= delta; if(char == player) map.centerChar(char);}
			case Character.RIGHT: moveChar = function(c:Character):Void{char.x += delta; if(char == player) map.centerChar(char);}
		}
		var st:Int = Character.WALK_ST+Character.WALK_LEN*dir+1; var f:Int = char.queueLength();
		var end:Int = st+4; if(frame < end){for(i in st...end) char.queueFrame(i, moveChar);}
		else {st = end; end = st+4; for(i in st...end) char.queueFrame(i, moveChar);} return f;
	}
	private function enter_frame(e:Event):Void {
		var esc:Bool = Main.isPressed(Main.ESCAPE);
		Main.resetPressed(); //TODO: animate map, map repeat
		//TODO: if(esc) Main.instance.addChild(new GameMenu());
	}
}