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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.script.ScriptEditor;

public class Script extends Modifiable implements ActionListener {
	private static final long serialVersionUID = 3981925226292874481L;
	private static final Icon icon = MapEditor.getIcon("script");
	public static final String EXT = "hx";
	private String script;
	public Script(File f, MapEditor e){super(f, e);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(browser.edit); menu.addSeparator();
		super.contextMenu(menu);
	}
	public long getId(){return 0;}
	public String getScript(){return script;}
	public void setScript(String s){script = s;}
	public boolean edit(){
		ScriptEditor.show(this); return true;
	}
	public Icon getIcon(){return icon;}
	public void actionPerformed(ActionEvent e) {edit();}

	public void save() throws Exception {
		ScriptEditor.loadScript(this);
		File f = getFile(); BufferedWriter out = new BufferedWriter(new FileWriter(f));
		try{
			out.write(script); out.flush(); out.close(); setModified(false); ScriptEditor.onSave(this);
		}catch(Exception e){out.close(); throw e;}
	}
	private static char[] buffer = new char[4096];
	protected void read(File f) throws Exception {
		FileReader in = new FileReader(f);
		try{
			StringWriter out = new StringWriter();
			int n = 0; while(-1 != (n = in.read(buffer))) out.write(buffer, 0, n);
			script = out.toString(); in.close(); setModified(false); ScriptEditor.update(this);
		}catch(Exception e){in.close(); throw e;}
	}
	public static Script createScript(File f, MapEditor e, Project p) throws Exception {
		Script ret = new Script(f, e); f.createNewFile(); return ret;
	}
	
	public static void register(){
		Resource.register("Script", Script.EXT, Script.class);
		Folder.new_options.addItem("Script", "script", KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, new CreateScriptAction());
	}
	private static class CreateScriptAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addScript();
		}
	}
}
