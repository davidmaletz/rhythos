package mrpg.editor.resource;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import mrpg.editor.MapEditor;
import mrpg.editor.Menu;

public class Folder extends Resource {
	private static final long serialVersionUID = 4352438677363086255L;
	public static final String FOLDER = "folder", ADD_FOLDER = "add_folder";
	public static final Menu new_options = new Menu("Create New", MapEditor.NEW),
		import_options = new Menu("Import", MapEditor.IMPORT);
	private static final Icon icon = MapEditor.getIcon(FOLDER);
	protected Folder(File f, MapEditor editor){super(f, editor);}
	public boolean canAddChildren(){return true;}
	public void contextMenu(JPopupMenu menu){
		menu.add(new_options.getMenuItem(false));
		menu.add(import_options.getMenuItem(false));
		menu.addSeparator();
	}
	public long getId(){return 0;}
	public Icon getIcon(){return icon;}
	public static String OUT_DIR = "__haxe"; 
	protected void read(File f) throws Exception {
		for(File file : f.listFiles())
			if(!file.getName().equals(OUT_DIR)) try{add(Resource.readFile(file, editor));}catch(Exception e){}
	}
	public File copy(File dir, Project p) throws Exception {
		if(!dir.isDirectory()) throw new Exception(); File f = changeDirectory(dir, p, true);
		if(!f.mkdir()) throw new Exception(); for(int i=0; i<getChildCount(); i++) getChild(i).copy(f, p);
		return f;
	}
	public static Folder create(File f, MapEditor e) throws Exception {
		if(f.mkdir()) return new Folder(f,e); else throw new Exception();
	}
	public String getExt(){return null;}
	public static void register(){
		new_options.addItem("Folder", ADD_FOLDER, KeyEvent.VK_F, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, new CreateFolderAction());
	}
	private static class CreateFolderAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addFolder();
		}
	}
}