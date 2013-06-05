package mrpg.script;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import mrpg.editor.resource.Folder;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;

public class HaxeCompiler {
	private static Process host, test; private static int port = 0; private static File nme_path = null;
	private static boolean isClosed(Process p){
		try{p.exitValue(); return true;} catch(Exception e){return false;}
	}
	private static void launchHost() throws Exception {
		if(host == null || isClosed(host)){
			ServerSocket s = new ServerSocket(0); port = s.getLocalPort(); s.close();
			host = Runtime.getRuntime().exec("haxe --wait "+port);
		}
	}
	public static String[] TARGETS = {"flash", "windows -neko", "mac -neko", "linux -neko", "linux -neko -64", "html5", "android", "ios", "blackberry", "webos"};
	//TODO: Enable (& test) targets Mac, Linux, Linux 64 and HTML5. Also, I *think* it is safe to delete nme.ndll, test this. 
	public static String[] TARGET_NAMES = {"Flash SWF", "Windows", "Mac", "Linux", "Linux 64-bit"/*, "HTML5"*/};
	public static String defaultTarget(){return "flash";}
	private static String getProjectOut(Project p){return p.getName();}
	private static int getProjectWidth(Project p){return 400;}
	private static int getProjectHeight(Project p){return 300;}
	private static String createNMML(Project p){
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		b.append("<project>");
		b.append("<meta title=\""); b.append(p.getName());
		b.append("\" package=\"com.rhythos.core\" version=\"1.0.0\" company=\"Rhythos RPG Builder\" />");
		b.append("<app main=\"com.rhythos.core.Main\" file=\"");
		b.append(getProjectOut(p)); b.append("\" path=\".\" />");
		b.append("<window background=\"#ffffff\" fps=\"24\" />");
		b.append("<window width=\""); b.append(getProjectWidth(p)); b.append("\" height=\"");
		b.append(getProjectHeight(p)); b.append("\" unless=\"mobile\" />");
		b.append("<window orientation=\"landscape\" vsync=\"true\" antialiasing=\"0\" if=\"cpp\" />");
		b.append("<source path=\"../\" />");
		b.append("<haxelib name=\"nme\" />");
		b.append("<ndll name=\"std\" />");
		b.append("<ndll name=\"regexp\" />");
		b.append("<ndll name=\"zlib\" />");
		b.append("<ndll name=\"nme\" haxelib=\"nme\" />");
		b.append("<haxeflag name=\"--dead-code-elimination\" if=\"html5\" />");
		b.append("<haxeflag name=\"--js-modern\" if=\"html5\" />");
		b.append("</project>");
		return b.toString();
	}
	private static byte buf[] = new byte[4096], NEKO[] = {(byte)'N',(byte)'E',(byte)'K',(byte)'O'};
	private static int writeAll(InputStream in, OutputStream out) throws Exception {
		int ret = 0; try{
			int n = 0; while(-1 != (n = in.read(buf))){out.write(buf, 0, n); ret += n;}
		}catch(Exception e){} in.close(); return ret;
	}
	public static class Result {
		public final String target; public final boolean isNeko; public final long time; public final ArrayList<String> messages;
		public Result(String t, boolean n, ArrayList<String> m){
			target = t; isNeko = n; time = System.currentTimeMillis(); messages = m;
		}
	}
	private static Result inner_parse(Project p, File fbase, File fbin) throws Exception {
		if(!fbin.exists()) fbin.mkdir(); String target = p.getTarget();
		File project = new File(fbin, "project.nmml");
		if(!project.exists() || project.lastModified() < new File(fbase, ".project").lastModified()){
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(fbin, "project.nmml")));
			out.write(createNMML(p)); out.flush(); out.close();
			Process _p = Runtime.getRuntime().exec("haxelib run nme update \""+project.getAbsolutePath()+"\" "+target);
			_p.waitFor();
		} //TODO: update assets if needed.
		File hxml; boolean neko = false; int i = target.indexOf(" -neko"); String t = target; if(i != -1){
			neko = true; t = target.substring(0, i); if(target.indexOf("-64") != -1) t += "64";
			hxml = new File(fbin, t); hxml = new File(hxml, "neko");
		} else hxml = new File(fbin, t); hxml = new File(hxml, "haxe"); hxml = new File(hxml, "release.hxml");
		if(!hxml.exists() || !new File(hxml.getParentFile(), "bin").exists()){
			Process _p = Runtime.getRuntime().exec("haxelib run nme update \""+project.getAbsolutePath()+"\" "+target);
			_p.waitFor(); if(!hxml.exists()) throw new Exception();
		} String s = "--cwd \""+fbin.getAbsolutePath()+"\"\n\""+hxml.getAbsolutePath()+"\"\n\000"; byte b[] = s.getBytes("UTF-8");
		Socket socket = new Socket("127.0.0.1", port); OutputStream out = socket.getOutputStream(); out.write(b); out.flush();
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		Result r = new Result(t, neko, new ArrayList<String>());
		String line; while((line = in.readLine()) != null) r.messages.add(line);
		socket.close(); return r;
	}
	public static Result parse(Project p) throws Exception {
		launchHost(); File fbase = p.getFile(), fbin = new File(fbase, Folder.OUT_DIR);
		return inner_parse(p, fbase, fbin);
	}
	public static void compile(Project p) throws Exception {
		launchHost(); File fbase = p.getFile(), fbin = new File(fbase, Folder.OUT_DIR);
		Result r = inner_parse(p, fbase, fbin); p.lastCompile = r; ScriptEditor.compiled(p);
		if(r.messages.size() > 0){/*TODO: Show error log*/ throw new Exception();}
		if(r.isNeko){
			String t = r.target;
			if(nme_path == null){
				Process _p = Runtime.getRuntime().exec("haxelib path nme"); _p.waitFor();
				BufferedReader in = new BufferedReader(new InputStreamReader(_p.getInputStream()));
				String line; while((line = in.readLine()) != null){
					if(line.charAt(0) != '-'){nme_path = new File(line); if(nme_path.exists()) break; else nme_path = null;}
				}
			} File templates = new File(nme_path, "templates"); templates = new File(templates, "default");
			templates = new File(templates, "neko"); File _target = new File(new File(fbin, t), "neko");
			File bindir = new File(_target, "bin"); if(t.equals("mac")){
				bindir = new File(bindir, getProjectOut(p)+".app"); bindir = new File(bindir, "Contents");
				bindir = new File(bindir, "MacOS");
			} Resource.copyDir(new File(new File(templates, "ndll"),t), bindir); boolean windows = t.equals("windows");
			OutputStream exe = new FileOutputStream(new File(bindir, p.getName()+((windows)?".exe":"")));
			int len = writeAll(new FileInputStream(new File(new File(templates, "bin"), "neko-"+t)), exe);
			writeAll(new FileInputStream(new File(new File(_target, "obj"), "ApplicationMain.n")), exe);
			exe.write(NEKO); ByteBuffer bb = ByteBuffer.allocate(4); bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(len); exe.write(bb.array()); exe.flush(); exe.close();
			//TODO: copy/add assets
		}
	}
	public static void run(Project p) throws Exception {
		if(test != null && !isClosed(test)){test.destroy(); test = null;} String t = p.lastCompile.target;
		File fbase = p.getFile(), fbin = new File(fbase, Folder.OUT_DIR); fbin = new File(fbin, t);
		if(p.lastCompile.isNeko){
			fbin = new File(fbin, "neko"); fbin = new File(fbin, "obj"); fbin = new File(fbin, "ApplicationMain.n");
			test = Runtime.getRuntime().exec("neko \""+fbin.getAbsolutePath()+"\"");
			//TODO: open log window showing errors from test
		} else if(t.equals("flash")){
			fbin = new File(fbin, "bin"); fbin = new File(fbin, getProjectOut(p)+".swf");
			Desktop.getDesktop().open(fbin);
		}
	}
	public static void destroy(){
		try{if(host != null){host.destroy(); host = null;}} catch(Exception e){}
		try{if(test != null){test.destroy(); test = null;}} catch(Exception e){}
	}
}
