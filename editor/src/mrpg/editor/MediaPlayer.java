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
package mrpg.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mrpg.editor.resource.Media;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;
import mrpg.media.Audio;
import mrpg.media.FrameListener;



public class MediaPlayer extends JFrame implements TreeSelectionListener, FrameListener, ActionListener {
	private static final long serialVersionUID = 6196229623660851300L;
	private final mrpg.media.MediaPlayer player; public final JTree tree;
	private final JCheckBox shuffle, play_all, repeat;
	public MediaPlayer(WorkspaceBrowser browser){
		super("Media Player");
		tree = new JTree(new FilterTreeModel(null, "."+Media.EXT));
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(browser.getCellRenderer());
		tree.setFocusable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
		((BasicTreeUI)tree.getUI()).setExpandedIcon(MapEditor.getIcon("expand"));
		((BasicTreeUI)tree.getUI()).setCollapsedIcon(MapEditor.getIcon("collapse"));
		tree.putClientProperty("JTree.lineStyle", "None");
		Container c = getContentPane(); c.setLayout(new BorderLayout());
		JPanel inner = new JPanel(new BorderLayout());
		player = new mrpg.media.MediaPlayer();
		JButton b = new JButton(new NextIcon()); b.addActionListener(this); player.add(b);
		player.setFrameListener(this);
		inner.add(player, BorderLayout.NORTH);
		JPanel settings = new JPanel();
		shuffle = new JCheckBox("Shuffle"); settings.add(shuffle);
		play_all = new JCheckBox("Play All Folders"); settings.add(play_all);
		repeat = new JCheckBox("Repeat Folders"); settings.add(repeat);
		inner.add(settings, BorderLayout.SOUTH);
		c.add(inner, BorderLayout.NORTH);
		JScrollPane sp = new JScrollPane(tree);
		sp.setPreferredSize(new Dimension(300, 300)); c.add(sp, BorderLayout.CENTER);
		pack();
	}
	public void setProject(Project p){
		DefaultTreeModel m = (DefaultTreeModel)tree.getModel();
		m.setRoot(p); tree.clearSelection(); player.setClip(null);
	}
	public void setVisible(boolean v){if(!v) player.stop(); super.setVisible(v);}
	public void valueChanged(TreeSelectionEvent e) {
		if(e.getNewLeadSelectionPath() == null) return;
		Resource r = (Resource)e.getNewLeadSelectionPath().getLastPathComponent();
		if(r instanceof Media){
			Audio.Clip c = ((Media)r).getClip();
			if(player.getClip() != c){player.setClip(c); player.play();}
		}
	}
	private Resource innerNextSong(Resource r){
		while(r != null){
			if(r instanceof Media) return r;
			else if(play_all.isSelected() && r.getChildCount() > 0){Resource ret = innerNextSong(r.getChild(0)); if(ret != null) return ret;}
			r = (Resource)r.getParent().getChildAfter(r);
		}
		return null;
	}
	private Resource seqNextSong(Resource r){
		Resource ret = innerNextSong((Resource)r.getParent().getChildAfter(r));
		if(ret == null){
			if(play_all.isSelected() && r != tree.getModel().getRoot()){
				if(r.getParent() == null) r = null; else r = seqNextSong(r.getParent());
			}else{
				if(!play_all.isSelected() && !repeat.isSelected()){player.stop(); return null;}
				else r = innerNextSong(r.getParent().getChild(0));
			}
		} else r = ret;
		return r;
	}
	public void nextSong(){
		if(tree.getRowCount() == 0){player.stop(); return;}
		TreePath p = tree.getSelectionPath(); if(p == null){
			if(play_all.isSelected()) p = new TreePath(new Object[]{tree.getModel().getRoot()}); else p = tree.getPathForRow(0);
		}
		Resource r = WorkspaceBrowser.getResource(p);
		if(r.getParent() == null){if(r.getChildCount() == 0){player.stop(); return;} else r = r.getChild(0);}
		if(shuffle.isSelected()){
			Random rnd = new Random();
			if(play_all.isSelected()) r = (Resource)tree.getModel().getRoot();
			Resource ret = null;
			for(int i=0; i<100; i++){
				ret = r.getParent().getChild(rnd.nextInt(r.getParent().getChildCount()));
				if(play_all.isSelected()){
					while(ret.getChildCount() > 0)
						ret = ret.getChild(rnd.nextInt(ret.getChildCount()));
				}
				if(ret instanceof Media) break;
				ret = null;
			}
			r = ret;
		} else r = seqNextSong(r);
		if(r != null){
			boolean b = player.getClip() == ((Media)r).getClip();
			tree.setSelectionPath(new TreePath(WorkspaceBrowser.getPathToRoot(r, tree.getModel().getRoot())));
			if(b){player.setFrame(0); player.play();}
		} else player.stop();
	}
	public void playFrame(long f){if(player.getClip() != null && f >= player.getClip().length()-1) nextSong();}
	public void actionPerformed(ActionEvent e) {nextSong();}
	
	private static class NextIcon implements Icon {
		public int getIconHeight() {return 12;}
		public int getIconWidth() {return 12;}
		public void paintIcon(Component c, Graphics g, int x, int y) {g.setColor(c.getForeground()); g.fillPolygon(new int[]{x, x+9, x}, new int[]{y, y+6, y+12}, 3); g.fillRect(x+10, y, 2, 12);}
	}
}
