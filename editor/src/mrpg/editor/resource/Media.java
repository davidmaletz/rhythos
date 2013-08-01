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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mrpg.editor.Filter;
import mrpg.editor.MapEditor;
import mrpg.editor.ResourceChooser;
import mrpg.export.Export;
import mrpg.export.Sound;
import mrpg.media.Audio;
import mrpg.media.MediaPlayer;


public class Media extends TypedResource {
	private static final long serialVersionUID = -3381982539985690245L;
	public static final String EXT = "msnd"; private static final short VERSION=1;
	private static final Icon icon = MapEditor.getIcon("media");
	private Sound sound;
	public Media(File f, MapEditor editor){super(f, editor);}
	public String getType(){return Export.SOUND;}
	public short getVersion(){return VERSION;}
	public JDialog getProperties(){return new Properties(this);}
	public Audio.Clip getClip(){try{return sound.getClip();}catch(Exception e){} return null;}
	public Sound getSound(){return sound;}
	public Icon getIcon(){return icon;}
	public void writeInner(DataOutputStream out) throws Exception {sound.write(out);}
	public void readInner(DataInputStream in) throws Exception {sound = new Sound(in);}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_MEDIA);}
	
	public static Media importMedia(File media, File f, MapEditor e, Project p) throws Exception {
		Sound s = Sound.decode(media); Media ret = new Media(f, e); ret.newId(p);
		ret.sound = s; ret.save(); ret.addToProject(p, false); return ret;
	}
	
	private static class Properties extends TypedResource.Properties {
		private static final long serialVersionUID = -4987880557990107307L;
		private JLabel dur; private MediaPlayer preview;
		public Properties(Media m){super(m, "Media Properties");}
		public void addControls(JPanel settings){
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Duration"));
			dur = new JLabel("0:00"); inner.add(dur);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Media Player"));
			preview = new MediaPlayer(); inner.add(preview);
			settings.add(inner);
		}
		public void updateControls(){
			Audio.Clip clip = ((Media)resource).getClip(); if(clip != null){long frames = clip.length();
			float spf = 1.f/clip.framesPerSecond();
			int m = (int)(frames*spf/60), s = (int)(frames*spf)-m*60;
			dur.setText(m+":"+((s < 10)?"0":"")+s);
			preview.setClip(clip);} else dur.setText("0:00");
		}
	}
	public String getExt(){return EXT;}
	public static void register() throws Exception {
		Resource.register("Media Files", Media.EXT, Export.SOUND, Media.class);
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
