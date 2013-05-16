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
import com.davidmaletz.mrpg.Character;
import com.davidmaletz.mrpg.EnemyType;
import com.davidmaletz.mrpg.ui.DialogBox;
import com.davidmaletz.mrpg.ui.GameMenu;
import nme.display.Sprite;
import nme.events.Event;

/**
 * ...
 * @author David Maletz
 */

class Game extends Sprite {
	private var player:Character; private var playerX:Int; private var playerY:Int; private var map:Map;
	private var enemy:Character; //TODO: event handling, collision, it shouldn't be done with a static variable!
	public function new() {
		super(); map = Map.get(0); addChild(map); player = Main.getPlayer(); playerX = 0; playerY = 1; resetPlayer(); Main.safeEnterFrame(this, enter_frame);
		enemy = EnemyType.ENEMIES[0].getChar(); enemy.scaleX = enemy.scaleY = 1; var f:Int = Character.WALK_ST+Character.WALK_LEN*Character.DOWN; enemy.setFrame(f);
		enemy.queueFrame(f, moveEnemy); enemy.setPos(8,14); map.add(enemy); enemy.enter_frame(null);
		//TODO: read what layer the player should render on in the map, then add the player to that layer (sorting all events by y)
	}
	public function resetPlayer():Void { Main.playBGM(Main.getBGM("menu"));
		player.reset(); player.scaleX = player.scaleY = 1; var f:Int = Character.WALK_ST+Character.WALK_LEN*Character.DOWN; player.setFrame(f);
		player.queueFrame(f, checkMove); player.setPos(playerX,playerY); map.add(player); player.enter_frame(null); map.centerChar(player);
	}
	private function moveEnemy(char:Character):Void {
		var dir:Int = -1; for(i in 0...10){
			dir = Std.int(Math.random()*4);
			switch(dir){
				case Character.DOWN: if(map.canMoveDown(char)) break;
				case Character.UP: if(map.canMoveUp(char)) break;
				case Character.RIGHT: if(map.canMoveRight(char)) break;
				case Character.LEFT: if(map.canMoveLeft(char)) break;
			} dir = -1;
		} if(dir != -1){
			var f:Int = queueWalk(char, dir); var func = char.getQueuedFunc(f+3);
			char.setFunction(f+3, function(c:Character):Void {func(c); moveEnemy(c);});
		} else char.queueFrame(char.getFrame(), moveEnemy);
	}
	private function bump(x:Int, y:Int):Void {
		if(x == 17 && y == 2) Main.instance.addChild(new DialogBox("\004\310\300*knock* \300\310\300*knock* \300\310\300*knock*\300\320\n\000Hmm\302.\302.\302.\302 No one seems\nto be home!",null,true,0,24));
	}
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
	private function doneMove():Void {
		var x:Int = player.getX(), y:Int = player.getY(); if(x == 2 && y == 11) map.setLayer(player,3);
		else if(x == 2 && y == 12) map.setLayer(player,2);
	}
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
		if(esc) Main.instance.addChild(new GameMenu());
		if(enemy.stage != null && player.hitTestObject(enemy)){
			playerX = player.getX(); playerY = player.getY(); Main.instance.startBattle(0);
		}
	}
}