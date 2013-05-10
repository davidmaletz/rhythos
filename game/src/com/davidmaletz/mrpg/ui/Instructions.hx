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

class Instructions extends DialogBox {
	private var func:Void->Void;
	public function new(f:DialogBox->Bool=null) {
		super("\300\005BATTLE CONTROLS\000\300\n\310\001\300\201 LEFT:\300\000\310 ATTACK\n\310\002\300\202 RIGHT:\300\000\310 CAST SPELL\n\310\004\300\200 UP:\300\000\310 EVADE\n\310\003\300\177 DOWN:\300\000\310 DEFEND", f, true, 1, 22, 5, 1, false);
		Main.instructions = false;
	}
}