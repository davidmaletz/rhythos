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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;

public abstract class TypedResource extends Resource {
	private static final long serialVersionUID = 1891837498119361513L;
	protected JDialog properties; private long id = 0;
	public TypedResource(File f, MapEditor editor){super(f, editor);}
	public void contextMenu(JPopupMenu menu){
		menu.add(editor.getBrowser().properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	//Sets this resource's id to a new id if it hasn't been assigned an id yet.
	//Returns true if it has a new id, or false if it already had an id.
	//NOTE: This does NOT reserve the id in the project, you should still call addToProject when you wish to add it.
	public boolean newId(Project p){if(id == 0){id = p.newId(this); return true;} return false;}
	public long getId(){return id;}
	public abstract String getType();
	public abstract short getVersion();
	public abstract JDialog getProperties();
	public boolean edit(){properties(); return true;}
	public void properties(){
		if(properties == null) properties = getProperties(); properties.setVisible(true);
	}
	public boolean hasProperties(){return true;}
	public void remove(boolean delete) throws Exception {
		WorkspaceBrowser.getProject(this).removeId(this, id); super.remove(delete);
	}
	public void addToProject(Project p, boolean changeProject) throws Exception {
		long i = p.setId(this, id); if(i != id){id = i; save();}
	}
	public abstract void writeInner(DataOutputStream out) throws Exception;
	public abstract void readInner(DataInputStream in) throws Exception;
	//Returns the amount of bytes at the beginning of the resource only used for editing purposes and should be skipped in game.
	//Default = 10 bytes, 2 byte version, 8 byte id
	public int getHeaderSize(){return 10;}
	public void save() throws Exception {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getFile())));
		try{
			out.writeShort(getVersion()); out.writeLong(id); writeInner(out); out.flush(); out.close();
		}catch(Exception e){out.close(); throw e;}
	}
	public void deferredRead(File f) throws Exception{
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		try{if(in.readShort() != getVersion()) throw new Exception();
			id = in.readLong(); readInner(in); in.close(); addToProject(WorkspaceBrowser.getProject(this), false);
		}catch(Exception e){in.close(); throw e;}
	}
	
	public static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String OK = "ok", CANCEL = "cancel";
		protected final TypedResource resource; private final JTextField name, id; public boolean updated;
		public Properties(TypedResource r, String title){this(r, title, false);}
		public Properties(TypedResource r, String title, boolean resizable){
			super(JOptionPane.getFrameForComponent(r.editor), title, true); resource = r; setResizable(resizable);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			name = new JTextField(resource.getName(), 20); name.setActionCommand(OK); name.addActionListener(this);
			inner.add(name); JPanel p = new JPanel(); p.add(new JLabel("Id: "));
			id = new JTextField("", 15); id.setOpaque(false); id.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			id.setEditable(false); p.add(id); inner.add(p);
			settings.add(inner);
			addControls(settings);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void addControls(JPanel settings){}
		public void updateControls(){}
		public void acceptControls() throws Exception {}
		public boolean saveOnEdit(){return false;}
		public void setVisible(boolean b){
			if(b == true){
				updated = false; id.setText(Long.toHexString(resource.id));
				name.setText(resource.getName()); name.requestFocus(); name.selectAll();
				updateControls();
			} super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == OK){
				try{acceptControls();}catch(Exception ex){return;} try{
					resource.setName(name.getText());
				}catch(Exception ex){name.setText(resource.getName()); return;}
				try{
					if(saveOnEdit()){
						resource.save(); if(resource instanceof Modifiable) resource.editor.updateSaveButtons();
					} updated = true;
				}catch(Exception ex){}
				setVisible(false);
			} else if(command == CANCEL) setVisible(false);
		}
	}
}
