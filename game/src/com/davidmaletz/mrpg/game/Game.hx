package com.davidmaletz.mrpg.game;
import nme.display.Sprite;

/**
 * ...
 * @author David Maletz
 */

class Game extends Sprite {
	public function new() {
		super(); addChild(Map.get(0));
	}
	
}