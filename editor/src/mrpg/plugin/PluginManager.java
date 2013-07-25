package mrpg.plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.awt.AWTUtilities;

public class PluginManager {
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

	private static JFrame initWindow;
	public static void showInitWindow(){
		if(initWindow != null) return; try{
			initWindow = new JFrame(); initWindow.setUndecorated(true); JPanel p = new JPanel(null);
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
}
