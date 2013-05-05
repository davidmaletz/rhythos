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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeModel;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.export.Sound;
import mrpg.media.Audio;
import mrpg.media.MediaPlayer;


public class Media extends Resource {
	private static final long serialVersionUID = -3381982539985690245L;
	private static final Icon icon = MapEditor.getIcon(WorkspaceBrowser.MEDIA_ICON);
	private final Properties properties; private Sound sound;
	public Media(String name, Sound s, MapEditor editor){super(name, editor); sound = s; properties = new Properties(this);}
	public Media(Media m){super(m.getName(), m.editor); sound = m.sound; copyChildren(m); properties = new Properties(this);}
	public Audio.Clip getClip(){try{return sound.getClip();}catch(Exception e){} return null;}
	public void contextMenu(JPopupMenu menu){
		menu.add(editor.getBrowser().properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	public boolean edit(){properties(); return true;}
	public void properties(){properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public byte getType(){return Type.MEDIA;}
	public Icon getIcon(){return icon;}
	public Resource copy(){return new Media(this);}
	
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String OK = "ok", CANCEL = "cancel";
		private final Media media; private final JTextField name; private final JLabel dur;
		private final MediaPlayer preview;
		public Properties(Media m){
			super(JOptionPane.getFrameForComponent(m.editor), "Media Properties", true); media = m;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField(media.getName(), 20); name.setActionCommand(OK); name.addActionListener(this);
			inner.add(name);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Duration"));
			dur = new JLabel("0:00"); inner.add(dur);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Media Player"));
			preview = new MediaPlayer(); inner.add(preview);
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void setVisible(boolean b){
			if(b == true){
				name.setText(media.getName()); name.requestFocus(); name.selectAll();
				Audio.Clip clip = media.getClip(); long frames = clip.length(); float spf = 1.f/clip.framesPerSecond();
				int m = (int)(frames*spf/60), s = (int)(frames*spf)-m*60;
				dur.setText(m+":"+((s < 10)?"0":"")+s);
				preview.setClip(clip);
			} else preview.stop();
			super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == OK){
				media.setName(name.getText());
				((DefaultTreeModel)media.editor.getBrowser().getModel()).nodeChanged(media);
			}
			setVisible(false);
		}
	}
}
