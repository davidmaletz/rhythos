package com.davidmaletz.mrpg.game;
import com.davidmaletz.mrpg.Character;
import nme.display.Sprite;
import nme.events.Event;

/**
 * ...
 * @author David Maletz
 */

class Game extends Sprite {
	private var player:Character;
	public function new() {
		super(); addChild(Map.get(0)); player = Main.getPlayer(); Main.safeEnterFrame(this, enter_frame); player.reset();
		player.scaleX = player.scaleY = 1; player.setFrame(Character.WALK_ST+Character.WALK_LEN*Character.DOWN);
		player.x = -16; player.y = -32; addChild(player); player.enter_frame(null);
		//TODO: read what layer the player should render on in the map, then add the player to that layer (sorting all events by y)
	}
	private function enter_frame(e:Event):Void {
		var frame:Int = player.getFrame(), dir:Int=Math.floor((frame-Character.WALK_ST)/Character.WALK_LEN);
		if(player.queueLength() <= 1){
			var r:Bool = Main.isHeld(Main.RIGHT), l:Bool = Main.isHeld(Main.LEFT), u:Bool = Main.isHeld(Main.UP), d:Bool = Main.isHeld(Main.DOWN);
			if(u && !d) player.queueWalk(Character.UP);
			else if(d && !u) player.queueWalk(Character.DOWN);
			else if(l && !r) player.queueWalk(Character.LEFT);
			else if(r && !l) player.queueWalk(Character.RIGHT);
		} else {
			var delta:Float = Tile.tile_size/(2*(Character.WALK_LEN-1)); switch(dir){
				case Character.UP: player.y -= delta;
				case Character.DOWN: player.y += delta;
				case Character.LEFT: player.x -= delta;
				case Character.RIGHT: player.x += delta;
			}
		} Main.resetPressed();
	}
}