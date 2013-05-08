package mrpg.editor.resource;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;

public class Folder extends Resource {
	private static final long serialVersionUID = 4352438677363086255L;
	private static final String FOLDER = "folder";
	private static final Icon icon = MapEditor.getIcon(FOLDER);
	protected Folder(File f, MapEditor editor){super(f, editor);}
	public boolean canAddChildren(){return true;}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser();
		JMenu inner = new JMenu("Create New"); inner.setIcon(MapEditor.getIcon(MapEditor.NEW));
		inner.add(browser.add_folder);
		inner.add(browser.add_map);
		inner.add(browser.add_tileset);
		inner.add(browser.add_autotile);
		inner.add(browser.add_script);
		menu.add(inner);
		inner = new JMenu("Import"); inner.setIcon(MapEditor.getIcon(MapEditor.IMPORT));
		inner.add(browser.add_image);
		inner.add(browser.add_media);
		menu.add(inner);
		menu.addSeparator();
	}
	public long getId(){return 0;}
	public Icon getIcon(){return icon;}
	protected void read(File f) throws Exception {
		for(File file : f.listFiles())
			try{add(Resource.readFile(file, editor));}catch(Exception e){}
	}
	public File copy(File dir) throws Exception {
		if(!dir.isDirectory()) throw new Exception(); File f = changeDirectory(dir, true);
		if(!f.mkdir()) throw new Exception(); for(int i=0; i<getChildCount(); i++) getChild(i).copy(f);
		return f;
	}
	public static Folder create(File f, MapEditor e) throws Exception {
		if(f.mkdir()) return new Folder(f,e); else throw new Exception();
	}
}