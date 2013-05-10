package com.davidmaletz.mrpg.game;
import com.davidmaletz.mrpg.Character;
import nme.display.Sprite;
import nme.events.Event;

/**
 * ...
 * @author David Maletz
 */

class Game extends Sprite {
	private var player:Character; private var map:Map;
	public function new() {
		super(); map = Map.get(0); addChild(map); player = Main.getPlayer(); player.reset();
		player.scaleX = player.scaleY = 1; var f:Int = Character.WALK_ST+Character.WALK_LEN*Character.DOWN; player.setFrame(f);
		player.queueFrame(f, checkMove); player.setPos(1,1); map.add(player); player.enter_frame(null); Main.safeEnterFrame(this, enter_frame);
		//TODO: read what layer the player should render on in the map, then add the player to that layer (sorting all events by y)
	}
	private function checkMove(c:Character):Void {
		var r:Bool = Main.isHeld(Main.RIGHT), l:Bool = Main.isHeld(Main.LEFT), u:Bool = Main.isHeld(Main.UP), d:Bool = Main.isHeld(Main.DOWN);
		var dir:Int = player.getDir(); if(u && !d){if(map.canMoveUp(player)){queueWalk(Character.UP); return;} dir = Character.UP;}
		else if(d && !u){if(map.canMoveDown(player)){queueWalk(Character.DOWN); return;} dir = Character.DOWN;}
		else if(l && !r){if(map.canMoveLeft(player)){queueWalk(Character.LEFT); return;} dir = Character.LEFT;}
		else if(r && !l){if(map.canMoveRight(player)){queueWalk(Character.RIGHT); return;} dir = Character.RIGHT;}
		player.queueFrame(Character.WALK_ST+Character.WALK_LEN*dir, checkMove);
	}
	private function queueWalk(dir:Int):Void {
		var moveChar:Character->Void = null; var delta:Float = Tile.tile_size/4, frame:Int = player.getFrame();
		switch(dir){
			case Character.UP: moveChar = function(c:Character):Void{player.y -= delta; map.centerChar(player);}
			case Character.DOWN: moveChar = function(c:Character):Void{player.y += delta; map.centerChar(player);}
			case Character.LEFT: moveChar = function(c:Character):Void{player.x -= delta; map.centerChar(player);}
			case Character.RIGHT: moveChar = function(c:Character):Void{player.x += delta; map.centerChar(player);}
		}
		var st:Int = Character.WALK_ST+Character.WALK_LEN*dir+1; var f:Int = player.queueLength();
		var end:Int = st+4; if(frame < end){for(i in st...end) player.queueFrame(i, moveChar);}
		else {st = end; end = st+4; for(i in st...end) player.queueFrame(i, moveChar);}
		player.setFunction(f+3, function(c:Character):Void {moveChar(c); checkMove(c);});
	}
	private function enter_frame(e:Event):Void {
		Main.resetPressed(); //TODO: animate map, map repeat
	}
}