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
package com.rhythos.core;

class Dequeue<T> {
	private var array:Array<T>; private var head:Int; private var tail:Int;
	public function new() { array = createArray(8); head = 0; tail = 0; }
	public inline function addFirst(v:T):Void { head = (head-1)&(array.length-1); array[head] = v; if (head == tail) doubleCapacity(); }
	public inline function addLast(v:T):Void { array[tail] = v; tail = (tail+1)&(array.length-1); if (head == tail) doubleCapacity();}
	public inline function clear():Void {head = 0; tail = 0;}
	public inline function isEmpty():Bool {return head == tail;}
	public inline function size():Int {return (tail-head)&(array.length-1);}
	public inline function getFirst():T {return array[head];}
	public inline function getLast():T {return array[(tail-1)&(array.length-1)];}
	public inline function set(i:Int, v:T):Void {array[(head+i)&(array.length-1)] = v;}
	public inline function get(i:Int):T {return array[(head+i)&(array.length-1)];}
    public inline function removeFirst():T {
        var NULL:Null<T> = null; if(isEmpty()) return NULL; else {var ret:T = array[head]; array[head] = NULL; head = (head+1)&(array.length-1); return ret;}
    }
    public inline function removeLast():T {
        var NULL:Null<T> = null; if(isEmpty()) return NULL; else {tail = (tail-1)&(array.length-1); var ret:T = array[tail]; array[tail] = NULL; return ret;}
    }
    private function doubleCapacity():Void {
        var new_array:Array<T> = createArray(array.length << 1);
        var n:Int = array.length-head; var i:Int = 0; while(i < n){new_array[i] = array[head+i]; i++;} i=0; while(i<head){new_array[n+i] = array[i]; i++;}
        head = 0; tail = array.length; array = new_array;
    }
	private function createArray(len:Int):Array<T> {
		var NULL:Null<T> = null, ret:Array<T> = new Array<T>(), i:Int = 0; while(i<len){ret.push(NULL); i++;} return ret;
	}
}