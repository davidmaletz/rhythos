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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import mrpg.editor.MapEditor;
import mrpg.editor.TilesetEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.world.BasicTilemap;


public class Tileset extends Resource implements ActionListener {
	private static final long serialVersionUID = 3981925226292874481L;
	private static final Icon icon = MapEditor.getIcon(WorkspaceBrowser.TILESET);
	public static final String DEFAULT_NAME = "New Tilemap", SET_TILESET="set-tileset";
	private BasicTilemap tilemap; private final Properties properties; private Image image;
	private JMenuItem set_tileset = MapEditor.createMenuItemIcon("Set Tileset", SET_TILESET, this);
	private Tileset(MapEditor e){
		super(DEFAULT_NAME, e); properties = new Properties(this);
	}
	public Tileset(Tileset m){super(m.getName(), m.editor); properties = new Properties(this); copyChildren(m);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(set_tileset); menu.add(browser.properties); menu.addSeparator();
	}
	public boolean edit(){if(tilemap != null) editor.getTilesetViewer().setTilemap(tilemap); return true;}
	public void properties(){properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public byte getType(){return Type.TILESET;}
	public Icon getIcon(){return icon;}
	public Resource copy(){return new Tileset(this);}
	public BasicTilemap getTilemap(){return tilemap;}
	public void actionPerformed(ActionEvent e) {edit();}
	
	public static Tileset createTileset(MapEditor e){
		Tileset ret = new Tileset(e); ret.properties();
		return (ret.properties.updated)?ret:null;
	}
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public boolean updated;
		private final Tileset tileset; private final JTextField name;
		private final TilesetEditor editor; private BasicTilemap tilemap; private Image image;
		public Properties(Tileset t){
			super(JOptionPane.getFrameForComponent(t.editor), "Tileset Properties", true); tileset = t;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField(t.getName(), 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name);
			settings.add(inner);
			inner = new JPanel(new BorderLayout()); inner.setBorder(BorderFactory.createTitledBorder("Tileset"));
			editor = new TilesetEditor();
			inner.add(new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner.add(set, BorderLayout.SOUTH);
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void setVisible(boolean b){
			if(b == true){
				updated = false;
				name.setText(tileset.getName()); name.requestFocus(); name.selectAll();
				image = tileset.image; tilemap = tileset.tilemap;
				editor.setTilemap(tilemap);
			}
			super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				updated = true;
				tileset.setName(name.getText());
				tileset.image = image; tileset.tilemap = tilemap;
				((DefaultTreeModel)tileset.editor.getBrowser().getModel()).nodeChanged(tileset);
				setVisible(false);
			} else if(command == MapEditor.SET){
				Project p = WorkspaceBrowser.getProject(tileset);
				if(p == null){
					WorkspaceBrowser b = tileset.editor.getBrowser();
					TreePath path; if(b.isSelectionEmpty() && b.getRowCount() == 0){path = null;}
					else if(b.isSelectionEmpty()) path = b.getPathForRow(0);
					else path = b.getSelectionPath();
					if(path != null && path.getPathCount() > 1) p = (Project)path.getPathComponent(1);
				}
				if(p == null){JOptionPane.showMessageDialog(this, "Tileset is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageChooser c = new ImageChooser(p.getChild(2).getChild(0), image);
				c.setVisible(true);
				Image im = c.getSelectedImage();
				if(im != null){
					try{tilemap = new BasicTilemap(im.getImage()); editor.setTilemap(tilemap); image = im;}
					catch(Exception ex){JOptionPane.showMessageDialog(tileset.editor, "Unable to create Tileset: "+ex.getMessage(), "Tileset Creation Error", JOptionPane.ERROR_MESSAGE); updated = false; tileset.tilemap = null;}
				}
			}
			else setVisible(false);
		}
	}
}
