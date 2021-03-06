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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mrpg.editor.resource.Resource;

public class ResourceChooser extends JDialog implements ActionListener, TreeSelectionListener, MouseListener {
	private static final long serialVersionUID = 6196229623660851300L;
	private Resource selected_resource = null; public final JTree tree; private Filter filter;
	public ResourceChooser(Resource root, Resource selected, Filter f){
		super(JOptionPane.getFrameForComponent(root.editor), "Resource Chooser", true);
		WorkspaceBrowser browser = root.editor.getBrowser();
		setResizable(false); filter = f;
		tree = new JTree(new FilterTreeModel(root, f));
		tree.addTreeSelectionListener(this); tree.addMouseListener(this);
		tree.setCellRenderer(browser.getCellRenderer());
		tree.setFocusable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(true);
		((BasicTreeUI)tree.getUI()).setExpandedIcon(MapEditor.getIcon("expand"));
		((BasicTreeUI)tree.getUI()).setCollapsedIcon(MapEditor.getIcon("collapse"));
		tree.putClientProperty("JTree.lineStyle", "None");
		Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createRaisedBevelBorder()); JScrollPane sp = new JScrollPane(tree);
		sp.setPreferredSize(new Dimension(300, 300)); inner.add(sp);
		inner.add(f.getPreview()); c.add(inner);
		inner = new JPanel();
		JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
		b = new JButton("Cancel");  b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
		c.add(inner);
		if(selected != null) try{tree.setSelectionPath(new TreePath(WorkspaceBrowser.getPathToRoot(selected, root)));}catch(Exception e){}
		pack();
	}
	public Resource getSelectedResource(){return selected_resource;}
	private void okAction(){super.setVisible(false);}
	public void setVisible(boolean v){if(!v){tree.clearSelection(); selected_resource = null;} super.setVisible(v);}
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command == MapEditor.OK) okAction();
		else if(command == MapEditor.CANCEL){setVisible(false);}
	}
	public void valueChanged(TreeSelectionEvent e) {
		if(e.getNewLeadSelectionPath() == null) return;
		Resource r = (Resource)e.getNewLeadSelectionPath().getLastPathComponent();
		if(filter.showPreview(r)) selected_resource = r; else selected_resource = null;
	}
	public void mouseClicked(MouseEvent e) {if(e.getClickCount() == 2 && tree.getRowForLocation(e.getX(), e.getY()) != -1 && selected_resource != null) okAction();}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
}
