package mrpg.editor.resource;

import java.io.File;

import javax.swing.JPopupMenu;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;

public abstract class Modifiable extends Resource {
	private static final long serialVersionUID = 8050024073339824076L;
	private boolean modified = false;
	protected Modifiable(File f, MapEditor e){super(f,e);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); browser.save.setEnabled(isModified()); menu.add(browser.save);
		browser.revert.setEnabled(isModified()); menu.add(browser.revert); menu.addSeparator();
	}
	public boolean isModified(){return modified;}
	public void setModified(boolean m){if(m != modified){modified = m; updateName();}}
	public String toString(){if(modified) return "*"+super.toString(); else return super.toString();}
	public void refresh() throws Exception {if(!modified) super.refresh();}
	public void revert() throws  Exception {super.refresh();}
	public void save() throws Exception {MapEditor.instance.updateSaveButtons();}
}
