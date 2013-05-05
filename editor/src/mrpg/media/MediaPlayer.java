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
package mrpg.media;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalSliderUI;


public class MediaPlayer extends JPanel implements ActionListener, ChangeListener, FrameListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -2645984272456152640L;
	private static final Icon PLAY_ICON = new PlayIcon(), PAUSE_ICON = new PauseIcon(), STOP_ICON = new StopIcon();
	private final Audio.Player player = new Audio.Player(); private Audio.Clip clip;
	private final JButton play, stop; private final JSlider pos; private final JProgressBar vol; private long len = 1; private int posVal = 0;
	private FrameListener listener;
	public MediaPlayer(){
		player.setFrameListener(this);
		play = new JButton(PLAY_ICON); play.addActionListener(this); play.setEnabled(false); add(play);
		stop = new JButton(STOP_ICON); stop.addActionListener(this); stop.setEnabled(false); add(stop);
		vol = new JProgressBar(JProgressBar.VERTICAL, 0, 1000); vol.addChangeListener(this); vol.setEnabled(false); vol.setFont(vol.getFont().deriveFont(8.f));
		vol.setPreferredSize(new Dimension(vol.getPreferredSize().width, 20)); vol.setValue(1000); add(vol);
		vol.addMouseListener(this); vol.addMouseMotionListener(this); SliderUI posui = new SliderUI();
		pos = new JSlider(0, 1000, 0); pos.addChangeListener(this); pos.addMouseListener(posui); pos.setEnabled(false);
		pos.setPreferredSize(new Dimension(150, pos.getPreferredSize().height)); pos.setUI(posui); add(pos);
	}
	public void setFrameListener(FrameListener l){synchronized(player){listener = l;}}
	public Audio.Clip getClip(){return clip;}
	public void setClip(Audio.Clip c){
		stop(); boolean b = c != null; clip = c;
		if(b){player.setClip(c); player.setVolume(vol.getValue()*0.001f); len = c.length();}
		play.setEnabled(b); stop.setEnabled(b); vol.setEnabled(b); pos.setEnabled(b);
	}
	public void play(){player.play(); play.setIcon(PAUSE_ICON);}
	public void setFrame(long f){player.setFrame(f);}
	public void pause(){player.pause(); play.setIcon(PLAY_ICON);}
	public boolean isRunning(){return player.isRunning();}
	public void stop(){player.stop(); play.setIcon(PLAY_ICON);}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == play){if(player.isRunning()) pause(); else play();}
		else stop();
	}
	public void playFrame(long f) {posVal = (int)((f%len)*1000.f/len); pos.setValue(posVal); if(listener != null) listener.playFrame(f);}
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == pos){
			synchronized(player){
				if(pos.getValue() != posVal){posVal = pos.getValue(); player.setFrame((long)(posVal*0.001f*len));}
			}
		} else {if(vol.isEnabled()) player.setVolume(vol.getValue()*0.001f);}
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {	}
	public void mousePressed(MouseEvent e) {vol.setValue((18-e.getY())*56);}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {mousePressed(e);}
	public void mouseMoved(MouseEvent e) {}

	private static class SliderUI extends MetalSliderUI implements MouseListener {
		private int x = 0;
		protected void scrollDueToClickInTrack(int dir){slider.setValue(valueForXPosition(x));}
		
		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {	}
		public void mousePressed(MouseEvent e) {x = e.getX();}
		public void mouseReleased(MouseEvent e) {}
	}
	private static class PlayIcon implements Icon {
		public int getIconHeight() {return 12;}
		public int getIconWidth() {return 12;}
		public void paintIcon(Component c, Graphics g, int x, int y) {g.setColor(c.getForeground()); g.fillPolygon(new int[]{x, x+12, x}, new int[]{y, y+6, y+12}, 3);}
	}
	private static class PauseIcon implements Icon {
		public int getIconHeight() {return 12;}
		public int getIconWidth() {return 12;}
		public void paintIcon(Component c, Graphics g, int x, int y) {g.setColor(c.getForeground()); g.fillRect(x, y, 3, 12); g.fillRect(x+9, y, 3, 12);}
	}
	private static class StopIcon implements Icon {
		public int getIconHeight() {return 12;}
		public int getIconWidth() {return 12;}
		public void paintIcon(Component c, Graphics g, int x, int y) {g.setColor(c.getForeground()); g.fillRect(x, y, 12, 12);}
	}
}
