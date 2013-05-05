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
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import mrpg.export.Graphic;


public class Image extends Resource {
	private static final long serialVersionUID = -5394199071824545816L;
	private static final Icon icon = MapEditor.getIcon(WorkspaceBrowser.IMAGE_ICON);
	public static final BufferedImage DEFAULT_THUMB = new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);
	private BufferedImage thumb; private final Properties properties; private Graphic graphic;
	public Image(String name, Graphic g, MapEditor editor){super(name, editor); graphic = g; properties = new Properties(this);}
	public Image(Image i){super(i.getName(), i.editor); graphic = i.graphic; copyChildren(i); properties = new Properties(this);}
	private void loadThumb(){
		try{
			BufferedImage image = getImage();
			thumb = new BufferedImage(128,128,image.getType());
			thumb.getGraphics().drawImage(image, 0, 0, 128, 128, editor);
		} catch(Exception e){}
	}
	public BufferedImage getImage(){try{return graphic.getBufferedImage();}catch(Exception e){} return null;}
	public BufferedImage getThumb(){if(thumb == null) loadThumb(); return thumb;}
	public void contextMenu(JPopupMenu menu){
		menu.add(editor.getBrowser().properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	public boolean edit(){properties(); return true;}
	public void properties(){properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public byte getType(){return Type.IMAGE;}
	public Icon getIcon(){return icon;}
	public Resource copy(){return new Image(this);}
	
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String OK = "ok", CANCEL = "cancel";
		private final Image image; private final JTextField name; private final JLabel dim, thumb;
		public Properties(Image i){
			super(JOptionPane.getFrameForComponent(i.editor), "Image Properties", true); image = i;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField(image.getName(), 20); name.setActionCommand(OK); name.addActionListener(this);
			inner.add(name);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Dimensions"));
			dim = new JLabel("0 x 0"); inner.add(dim);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Thumbnail"));
			thumb = new JLabel(new ImageIcon(Image.DEFAULT_THUMB));
			thumb.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(thumb);
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
				name.setText(image.getName()); name.requestFocus(); name.selectAll();
				BufferedImage im = image.getImage();
				dim.setText(im.getWidth()+" x "+im.getHeight());
				thumb.setIcon(new ImageIcon(image.getThumb()));
			}
			super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == OK){
				image.setName(name.getText());
				((DefaultTreeModel)image.editor.getBrowser().getModel()).nodeChanged(image);
			}
			setVisible(false);
		}
	}
}
