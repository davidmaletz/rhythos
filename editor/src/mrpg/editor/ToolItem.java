package mrpg.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import mrpg.display.WorldPanel;
import mrpg.editor.tools.Tool;

public class ToolItem implements MenuItem, ActionListener {
	private Tool tool; private Class<? extends Tool> tool_class; private JMenuItem item; private JComponent toolbar;
	public ToolItem(Class<? extends Tool> tool){tool_class = tool;}
	public Tool getTool(){
		if(tool == null){
			MapEditor e = MapEditor.instance; try{
			tool = tool_class.getConstructor(WorldPanel.class, TilesetViewer.class, History.class).
					newInstance(e.getOverlay().getPanel(), e.getTilesetViewer(), e.getHistory());
			}catch(Exception ex){}
		} return tool;
	}
	public JMenuItem getMenuItem(boolean allowAccelerator){
		if(item == null){
			Tool t = getTool(); item = MapEditor.createMenuItemIcon(t.getName(), t.getIcon(), this);
		} return item;
	}
	public JComponent getToolbarItem(){
		if(toolbar == null){
			Tool t = getTool(); toolbar = MapEditor.createToolbarButton(t.getIcon(), t.getName(), MapEditor.instance.tools, this);
		} return toolbar;
	}
	public void actionPerformed(ActionEvent e){
		Tool t = getTool();
		MapEditor.instance.getOverlay().setTool(t);
	}
}
