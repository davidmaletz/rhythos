package mrpg.export;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import mrpg.editor.resource.Image;
import mrpg.editor.resource.Map;
import mrpg.editor.resource.Media;
import mrpg.editor.resource.TileResource;
import mrpg.world.BasicTilemap;

public abstract class Export {
	private WorldIO world_io; private ByteArrayOutputStream out;
	public Export(){world_io = new WorldIO(); out = new ByteArrayOutputStream();}
	public void addImage(Image i) throws Exception {addImage(i.getGraphic(), i.getId(), i.getFile().lastModified());}
	public abstract void addImage(Graphic b, long i, long modified) throws Exception ;
	public void addMedia(Media m) throws Exception {addSound(m.getSound(), m.getId(), m.getFile().lastModified());}
	public abstract void addSound(Sound s, long i, long modified) throws Exception ;
	public void addTilemap(TileResource t) throws Exception {
		out.reset(); DataOutputStream d = new DataOutputStream(out);
		BasicTilemap b = (BasicTilemap)t.getTilemap(); //TODO: handle auto tiles and other tilemap types!
		d.writeLong(t.getImage().getId()); b.write(d);
		addData(out, "t", t.getId(), t.getFile().lastModified());
	}
	public void addMap(Map m) throws Exception {
		out.reset(); world_io.resetBuffer(); m.getWorld().write(world_io); world_io.write(new DataOutputStream(out));
		addData(out, "m", m.getId(), m.getFile().lastModified());
	}
	public abstract void addData(ByteArrayOutputStream ar, String t, long i, long modified) throws Exception ;
	public void finish() throws Exception {}
}
