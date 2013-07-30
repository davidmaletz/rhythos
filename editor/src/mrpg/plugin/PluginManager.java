package mrpg.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mrpg.editor.MapEditor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.awt.AWTUtilities;

public class PluginManager extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7566025672296138929L;
	public static class PluginData {
		private final File file; private Plugin plugin; private int installed;
		public final String name, desc, author, version; private final String main;
		public PluginData(File f) throws Exception {
			file = f; plugin = null; installed = 0;
			JarFile jar = new JarFile(file); Manifest m = jar.getManifest(); Attributes a = m.getMainAttributes();
			name = get(a, "Plugin-Name"); desc = get(a, "Plugin-Desc"); author = get(a, "Plugin-Author");
			version = get(a, "Version-Version"); main = a.get("Plugin-Main").toString();
		}
		private String get(Attributes a, String s){Object o = a.get(s); return (o == null)?"":o.toString();}
		public Plugin getPlugin() throws Exception {
			if(plugin == null){
				URLClassLoader loader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()}, PluginManager.class.getClassLoader());
				plugin = (Plugin)loader.loadClass(main).newInstance();
			} return plugin;
		}
		public boolean isInstalled(){return installed == 1;}
		public void install(JFrame f){
			if(installed == 0) try{getPlugin().install(); installed = 1;}catch(Exception e){
				if(f != null){installed = 2; JOptionPane.showMessageDialog(f, "The plugin will be installed the next time Rhythos is restarted.", "Plugin Install", JOptionPane.INFORMATION_MESSAGE);}
			}
		}
		public void uninstall(JFrame f){
			if(installed == 1) try{getPlugin().uninstall(); installed = 0;}catch(Exception e){
				installed = -1; JOptionPane.showMessageDialog(f, "The plugin will be uninstalled the next time Rhythos is restarted.", "Plugin Uninstall", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	private static HashMap<String, PluginData> plugins = new HashMap<String, PluginData>();
	private PluginManager(){
		super("Plugin Manager"); setIconImages(MapEditor.getWindowIcon()); setSize(640,480);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); MapEditor.addFrame("plugins", this);
		JTable table = new JTable(new PluginTableModel(this)); table.setFillsViewportHeight(true); JPanel p = new JPanel(new BorderLayout());
		p.add(new JScrollPane(table)); setContentPane(p); JMenuBar bar = new JMenuBar(); JMenu menu = new JMenu("Plugins");
		JMenuItem item = new JMenuItem("Refresh Plugins"); item.addActionListener(this); menu.add(item); bar.add(menu); setJMenuBar(bar);
	}
	public void actionPerformed(ActionEvent e){scanPluginDirectory();}
	public static void openManager(){if(manager == null) manager = new PluginManager(); manager.setVisible(true);}
	private static JFrame initWindow, manager;
	public static void showInitWindow(){
		if(initWindow != null) return; try{
			initWindow = new JFrame(); initWindow.setUndecorated(true); JPanel p = new JPanel(null);
			initWindow.setIconImages(MapEditor.getWindowIcon());
			p.setOpaque(false); JLabel l = new JLabel(new ImageIcon(PluginManager.class.getResource("/data/splash.png")));
			Dimension w = l.getPreferredSize(); l.setBounds(0,0,w.width,w.height); p.add(l);
			l = new JLabel(); l.setBounds(271,334,314,26); l.setHorizontalAlignment(JLabel.CENTER);
			l.setBackground(Color.WHITE); l.setOpaque(true); l.setFont(l.getFont().deriveFont(18.f)); p.add(l);
			initWindow.setBackground(new Color(0,true)); AWTUtilities.setWindowOpaque(initWindow, false);
			initWindow.setContentPane(p); initWindow.setSize(w); Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
			initWindow.setLocation((s.width-w.width)>>1, ((s.height-w.height)>>1)-50); initWindow.setVisible(true);
		} catch(Exception e){e.printStackTrace();}
	}
	public static void setInitStatus(String s){
		if(initWindow != null) ((JLabel)initWindow.getContentPane().getComponent(1)).setText(s);
	}
	public static void closeInitWindow(){if(initWindow != null){initWindow.dispose(); initWindow = null;}}
	public static void scanPluginDirectory(){
		try{
			for(File f : new File("plugins").listFiles()) try{
				String n = f.getName(); if(n.endsWith(".jar")){
				n = n.substring(0, n.length()-4); if(!plugins.containsKey(n)) try{
					plugins.put(n, new PluginData(f));
				} catch(Exception e){}
			}} catch(Exception e){}
		}catch(Exception ex){}
	}
	public static void installPlugins(Document doc, Element root){
		setInitStatus("Loading Plugins..."); scanPluginDirectory(); NodeList list = root.getElementsByTagName("plugin");
		for(int i=0; i<list.getLength(); i++){
			try{plugins.get(((Element)list.item(i)).getTextContent()).install(null);}catch(Exception ex){}
		} closeInitWindow();
	}
	public static void getInstalledPlugins(Document doc, Element root){
		Iterator<Map.Entry<String, PluginData> > i = plugins.entrySet().iterator();
		while(i.hasNext()){ Map.Entry<String, PluginData> e = i.next(); if(e.getValue().installed > 0){
			Element element = doc.createElement("plugin"); element.setTextContent(e.getKey()); root.appendChild(element);
		}}
	}
	
	private static class PluginTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -4382784638118928931L;
		private static Class<?> colClass[] = {String.class, String.class, String.class, String.class, Boolean.class};
		private static String colNames[] = {"Name", "Description", "Author", "Version", "Installed?"};
		private JFrame frame;
		public PluginTableModel(JFrame f){frame = f;}
		public int getRowCount(){return plugins.size();}
		public int getColumnCount(){return colClass.length;}
		public String getColumnName(int col){return colNames[col];}
		public Class<?> getColumnClass(int col){return colClass[col];}
		public boolean isCellEditable(int row, int col){return col == 4;}
		public void setValueAt(Object val, int row, int col){
			if(col != 4) return; for(PluginData p : plugins.values()){
				if(row == 0){
					if(p.installed == 0){
						p.install(frame); fireTableCellUpdated(row, col);
					} else if(p.installed == 1){
						p.uninstall(frame); fireTableCellUpdated(row, col);
					} break;
				} row--;
			}
		}
		public Object getValueAt(int row, int col){
			for(PluginData p : plugins.values()){
				if(row == 0){
					switch(col){
					case 0: return p.name;
					case 1: return p.desc;
					case 2: return p.author;
					case 3: return p.version;
					case 4: return p.isInstalled();
					default: return null;
					}
				} row--;
			} return null;
		}
	}
}
