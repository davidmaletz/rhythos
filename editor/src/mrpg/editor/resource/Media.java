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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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

import mrpg.editor.Filter;
import mrpg.editor.MapEditor;
import mrpg.editor.ResourceChooser;
import mrpg.editor.WorkspaceBrowser;
import mrpg.export.Sound;
import mrpg.media.Audio;
import mrpg.media.MediaPlayer;


public class Media extends Resource {
	private static final long serialVersionUID = -3381982539985690245L;
	public static final String EXT = "msnd"; private static final short VERSION=1;
	private static final Icon icon = MapEditor.getIcon("media");
	private final Properties properties; private Sound sound; private long id;
	public Media(File f, MapEditor editor){super(f, editor); properties = new Properties(this);}
	public Audio.Clip getClip(){try{return sound.getClip();}catch(Exception e){} return null;}
	public void contextMenu(JPopupMenu menu){
		menu.add(editor.getBrowser().properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	public Sound getSound(){return sound;}
	public long getId(){return id;}
	public boolean edit(){properties(); return true;}
	public void properties(){properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public Icon getIcon(){return icon;}
	public void remove(boolean delete) throws Exception {
		WorkspaceBrowser.getProject(this).removeMediaId(this, id); super.remove(delete);
	}
	public void addToProject(Project p) throws Exception {
		long i = p.setMediaId(this, id); if(i != id){id = i; save();}
	}
	public void save() throws Exception {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getFile())));
		try{
			out.writeShort(VERSION); out.writeLong(id); sound.write(out);
		}catch(Exception e){out.close(); throw e;}
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_MEDIA);}
	public void deferredRead(File f) throws Exception{
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		try{if(in.readShort() != VERSION) throw new Exception();
			id = in.readLong(); sound = new Sound(in); in.close(); addToProject(WorkspaceBrowser.getProject(this));
		}catch(Exception e){in.close(); throw e;}
	}
	
	public static Media importMedia(File media, File f, MapEditor e, Project p) throws Exception {
		Sound s = Sound.decode(media); Media ret = new Media(f, e); ret.id = p.newMediaId();
		ret.sound = s; ret.save(); p.setMediaId(ret, ret.id); return ret;
	}
	
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String OK = "ok", CANCEL = "cancel";
		private final Media media; private final JTextField name, id; private final JLabel dur;
		private final MediaPlayer preview;
		public Properties(Media m){
			super(JOptionPane.getFrameForComponent(m.editor), "Media Properties", true); media = m;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			name = new JTextField(media.getName(), 20); name.setActionCommand(OK); name.addActionListener(this);
			inner.add(name); JPanel p = new JPanel(); p.add(new JLabel("Id: "));
			id = new JTextField("", 15); id.setOpaque(false); id.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			id.setEditable(false); p.add(id); inner.add(p);
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
				name.setText(media.getName()); name.requestFocus(); name.selectAll(); id.setText(Long.toHexString(media.id));
				Audio.Clip clip = media.getClip(); if(clip != null){long frames = clip.length(); float spf = 1.f/clip.framesPerSecond();
				int m = (int)(frames*spf/60), s = (int)(frames*spf)-m*60;
				dur.setText(m+":"+((s < 10)?"0":"")+s);
				preview.setClip(clip);} else dur.setText("0:00");
			} super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == OK){
				try{
					media.setName(name.getText());
				}catch(Exception ex){name.setText(media.getName()); return;}
			}
			setVisible(false);
		}
	}
	public String getExt(){return EXT;}
	public static void register(){
		Resource.register("Media Files", Media.EXT, Media.class);
		Folder.import_options.addItem("Audio File", "media", KeyEvent.VK_I, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, new ImportMediaAction());
	}
	private static class ImportMediaAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().importMedia();
		}
	}
	
	public static Media choose(Resource root, Resource selected){
		ResourceChooser c = new ResourceChooser(root, selected, FILTER);
		c.setVisible(true); return (Media)c.getSelectedResource();
	}
	private static class MFilter extends JPanel implements Filter {
		private static final long serialVersionUID = 907354882348925575L;
		private MediaPlayer player; private JLabel dur;
		public MFilter(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); player = new MediaPlayer();
			add(player); JLabel dur = new JLabel("Duration: 0:00"); dur.setPreferredSize(new Dimension(150,20)); add(dur);
		}
		public boolean filter(Resource r){String ext = r.getExt(); return ext == null || ext == EXT;}
		private void reset(){player.setClip(null); dur.setText("Duration: 0:00");}
		public JPanel getPreview(){reset(); return this;}
		public boolean showPreview(Resource r){
			if(r.getExt() == null){reset(); return false;} 
			Audio.Clip clip = ((Media)r).getClip(); player.setClip(clip);
			long frames = clip.length(); float spf = 1.f/clip.framesPerSecond();
			int m = (int)(frames*spf/60), s = (int)(frames*spf)-m*60;
			dur.setText("Duration: "+m+":"+((s < 10)?"0":"")+s);
			return true;
		}
	} public static final Filter FILTER = new MFilter();
}
