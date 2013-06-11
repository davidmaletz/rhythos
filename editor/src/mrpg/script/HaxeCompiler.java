package mrpg.script;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import mrpg.editor.resource.Folder;
import mrpg.editor.resource.Image;
import mrpg.editor.resource.Map;
import mrpg.editor.resource.Media;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;
import mrpg.editor.resource.TileResource;
import mrpg.export.Export;
import mrpg.export.NekoExport;
import mrpg.export.SWFExport;

public class HaxeCompiler {
	private static Process host, test; private static int port = 0; private static String neko_exe = null;
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
		b.append("<window orientation=\"landscape\" vsync=\"true\" antialiasing=\"0\" if=\"neko\"/>");
		b.append("<window require-shaders=\"true\" if=\"neko\"/>");
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
	public static class Result {
		public final String target; public final boolean isNeko; public final long time; public final ArrayList<String> messages;
		public Result(String t, boolean n, ArrayList<String> m){
			target = t; isNeko = n; time = System.currentTimeMillis(); messages = m;
		}
	}
	private static void updateApplicationMainFl(File f) throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(f, "ApplicationMain.hx"), true));
		out.newLine(); out.write("import AssetList;"); out.newLine(); out.flush(); out.close();
	}
	private static void generateAssetListFl(Project p, File f) throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(f, "AssetList.hx")));
		out.write("class AssetList {}"); out.newLine();
		for(Image i : p.getImages()){
			out.write("class Ai"); out.write(Long.toHexString(i.getId()));
			out.write(" extends nme.display.BitmapData { public function new () { super (0, 0); } }"); out.newLine();
		} for(Media m : p.getMedia()){
			out.write("class As"); out.write(Long.toHexString(m.getId()));
			out.write(" extends nme.media.Sound { }"); out.newLine();
		} for(TileResource tm : p.getTilemaps()){
			out.write("class At"); out.write(Long.toHexString(tm.getId()));
			out.write(" extends nme.utils.ByteArray { }"); out.newLine();
		} for(Map m : p.getMaps()){
			out.write("class Am"); out.write(Long.toHexString(m.getId()));
			out.write(" extends nme.utils.ByteArray { }"); out.newLine();
		} out.flush(); out.close();
	}
	private static Result inner_parse(Project p, File fbase, File fbin) throws Exception {
		if(!fbin.exists()) fbin.mkdir(); String target = p.getTarget(); boolean flash = target.equals("flash");
		File project = new File(fbin, "project.nmml");
		if(!project.exists() || project.lastModified() < new File(fbase, ".project").lastModified()){
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(fbin, "project.nmml")));
			out.write(createNMML(p)); out.flush(); out.close();
			Process _p = Runtime.getRuntime().exec("haxelib run nme update \""+project.getAbsolutePath()+"\" "+target);
			_p.waitFor(); if(flash) updateApplicationMainFl(new File(new File(fbin, target), "haxe"));
		} File hxml; boolean neko = false; int i = target.indexOf(" -neko"); String t = target; if(i != -1){
			neko = true; t = target.substring(0, i); if(target.indexOf("-64") != -1) t += "64";
			hxml = new File(fbin, t); hxml = new File(hxml, "neko");
		} else hxml = new File(fbin, t); hxml = new File(hxml, "haxe"); hxml = new File(hxml, "release.hxml");
		if(!hxml.exists() || !new File(hxml.getParentFile(), "bin").exists()){
			Process _p = Runtime.getRuntime().exec("haxelib run nme update \""+project.getAbsolutePath()+"\" "+target);
			_p.waitFor(); if(!hxml.exists()) throw new Exception(); if(flash) updateApplicationMainFl(hxml.getParentFile());
		} if(flash) generateAssetListFl(p, hxml.getParentFile());
		String s = "--cwd \""+fbin.getAbsolutePath()+"\"\n\""+hxml.getAbsolutePath()+"\"\n\000"; byte b[] = s.getBytes("UTF-8");
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
	private static void deleteAll(File f, boolean subdir) throws Exception {
		for(File c : f.listFiles()) if(subdir || !c.isDirectory()){
			if(c.isDirectory()) deleteAll(c, subdir); c.delete();
		}
	}
	private static byte NEKO[] = {(byte)'N',(byte)'E',(byte)'K',(byte)'O'};
	public static void compile(Project p) throws Exception {
		launchHost(); File fbase = p.getFile(), fbin = new File(fbase, Folder.OUT_DIR);
		Result r = inner_parse(p, fbase, fbin); p.lastCompile = r; ScriptEditor.compiled(p);
		if(r.messages.size() > 0){System.out.println(r.messages); /*TODO: Show error log*/ throw new Exception();}
		Export export; String t = r.target;
		if(r.isNeko){
			File _target = new File(new File(fbin, t), "neko");
			File bindir = new File(_target, "bin"); deleteAll(bindir, false); if(t.equals("mac")){
				File app = new File(bindir, getProjectOut(p)+".app"); deleteAll(app, true); app.delete();
				BufferedWriter command = new BufferedWriter(new FileWriter(new File(bindir, getProjectOut(p)+".command")));
				command.write("#!/bin/bash\ncd \"${0%/*}\"\n./"+getProjectOut(p)+"\n"); command.flush(); command.close();
			}  Resource.copyDir(new File("neko", t), bindir); boolean windows = t.equals("windows"); String ext = ((windows)?".exe":"");
			File out = new File(bindir, p.getName()+ext); new File(bindir, "neko"+ext).renameTo(out);
			FileOutputStream exe = new FileOutputStream(out, true); int len = (int)exe.getChannel().size();
			Export.writeAll(new FileInputStream(new File(new File(_target, "obj"), "ApplicationMain.n")), exe);
			exe.write(NEKO); ByteBuffer bb = ByteBuffer.allocate(4); bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(len); exe.write(bb.array()); exe.flush(); exe.close();
			export = new NekoExport(new File(new File(_target, "bin"), "assets"));
		} else if(t.equals("flash")) export = new SWFExport(new File(new File(new File(fbin, t), "bin"), getProjectOut(p)+".swf"));
		else throw new Exception();
		for(Image i : p.getImages()) export.addImage(i);
		for(Media m : p.getMedia()) export.addMedia(m);
		ByteArrayOutputStream ar = new ByteArrayOutputStream(); DataOutputStream dout = new DataOutputStream(ar);
		for(String type : p.getResourceTypes()){
			if(type.equals(Export.IMAGE) || type.equals(Export.SOUND)) continue;
			boolean tileset = type.equals(Export.TILEMAP);
			for(Resource rs : p.getResources(type)){
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(rs.getFile()));
				in.skip(10); byte[] header = null; if(tileset){
					String name = ((TileResource)rs).getTilemap().getClass().getSimpleName();
					ar.reset(); dout.writeUTF(name); dout.flush(); header = ar.toByteArray();
				} export.addData(header, in, type, rs.getId(), rs.getFile().lastModified());
			}
		} export.finish();
	}
	public static void run(Project p) throws Exception {
		if(test != null && !isClosed(test)){test.destroy(); test = null;} String t = p.lastCompile.target;
		File fbase = p.getFile(), fbin = new File(fbase, Folder.OUT_DIR); fbin = new File(fbin, t);
		if(p.lastCompile.isNeko){
			fbin = new File(fbin, "neko"); File dir = new File(fbin, "bin");
			fbin = new File(fbin, "obj"); fbin = new File(fbin, "ApplicationMain.n");
			if(neko_exe == null){
				String os = System.getProperty("os.name").toLowerCase();
				if(os.contains("windows")) neko_exe = "neko\\windows\\neko.exe";
				else if(os.contains("linux")) neko_exe = "neko/linux/neko";
				else if(os.contains("mac")) neko_exe = "neko/mac/neko";
				else throw new Exception();
				//TODO: check for linux 64?
			}
			test = Runtime.getRuntime().exec(neko_exe+" \""+fbin.getAbsolutePath()+"\"", null, dir);
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
