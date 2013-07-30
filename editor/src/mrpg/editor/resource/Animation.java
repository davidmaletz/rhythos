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
package mrpg.editor.resource;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.Timer;

public class Animation {
	public static final String default_ani[] = {"Idle", "Walk", "Slash", "Thrust", "Shoot", "Cast", "Death"},
	dirs[] = {"Left", "Right", "Up", "Down"};
	private final String name; private final byte dir; public final byte speed; private final AnimationSet parent;
	private final ArrayList<Integer> frames = new ArrayList<Integer>();
	public Animation(String n, byte d, byte s, AnimationSet p){name = n; dir = d; speed = s; parent = p;}
	public Animation(String n, byte d, byte s, AnimationSet p, ArrayList<Integer> f){this(n, d, s, p); frames.addAll(f);}
	public Animation(String n, byte d, byte s, AnimationSet p, String frames){
		this(n, d, s, p); String[] params = frames.split(",");
		for(int i=0; i<params.length; i++) try{this.frames.add(Integer.parseInt(params[i].trim())-1);}catch(Exception e){}
	}
	public int getWidth(){return parent.getWidth();}
	public String getName(){return name;}
	public int getDir(){return dir;}
	public int numFrames(){return frames.size();}
	public int getFrame(int i){return frames.get(i);}
	public ArrayList<Integer> getFramesList(){return frames;}
	public String getFrames(){
		StringBuilder ret = new StringBuilder();
		for(int i=0; i<frames.size(); i++){if(i != 0) ret.append(','); ret.append(frames.get(i)+1);}
		return ret.toString();
	}
	public void write(DataOutputStream out) throws Exception {
		out.writeUTF(name); out.write(dir); out.write(speed); out.writeShort(frames.size()); for(Integer i : frames) out.write(i);
	}
	public static Animation read(Project p, DataInputStream in, AnimationSet parent) throws Exception {
		Animation ret = new Animation(in.readUTF(), (byte)in.read(), (byte)in.read(), parent); int len = in.readShort();
		for(int i=0; i<len; i++) ret.frames.add(in.read());
		return ret;
	}
	public String toString(){return name+" "+dirs[dir];}
	
	public static class Icon implements javax.swing.Icon, ActionListener {
		private Animation ani; private BufferedImage img; private int width, height, iwidth, iheight;
		private int frame = 0, ct; private Timer timer; private JLabel label;
		public Icon(JLabel l, Animation a, BufferedImage i, int w, int h){
			label = l; ani = a; img = i; width = w; height = h; iwidth = img.getWidth()/w; iheight = img.getHeight()/h; ct = a.speed;
		}
		public int getIconWidth(){return iwidth;}
		public int getIconHeight(){return iheight;}
		public void actionPerformed(ActionEvent e){
			if(label.getIcon() != this || !label.isShowing()){timer.stop(); timer = null;} else{
				ct--; if(ct == 0){ct = ani.speed; frame++; if(frame >= ani.numFrames()) frame = 0; label.repaint();}
			}
		}
		public void paintIcon(Component c, Graphics g, int x, int y){
			if(timer == null && ani.numFrames() > 1){
				timer = new Timer(83, this); timer.start();
			} int f = ani.frames.get(frame); int fx = f%width, fy = f/width; if(fy >= height) return;
			fx *= iwidth; fy *= iheight; g.drawImage(img, x, y, x+iwidth, y+iheight, fx, fy, fx+iwidth, fy+iheight, c);
		}
	}
}