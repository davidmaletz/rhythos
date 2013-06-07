package mrpg.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class NekoExport extends Export {
	private File dir;
	public NekoExport(File f){dir = f; if(!dir.exists()) dir.mkdir();}
	public void addImage(Graphic b, long i, long modified) throws Exception {
		File f = new File(dir, "Ai"+Long.toHexString(i)); if(f.exists() && f.lastModified() >= modified) return;
		b.write(f);
	}
	public void addSound(Sound s, long i, long modified) throws Exception {
		File f = new File(dir, "As"+Long.toHexString(i)); if(f.exists() && f.lastModified() >= modified) return;
		s.write(f);
	}
	public void addData(ByteArrayOutputStream ar, String t, long i, long modified) throws Exception {
		File f = new File(dir, "A"+t+Long.toHexString(i)); if(f.exists() && f.lastModified() >= modified) return;
		FileOutputStream out = new FileOutputStream(f); out.write(ar.toByteArray()); out.flush(); out.close();
	}
}
