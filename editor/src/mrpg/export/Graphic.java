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
package mrpg.export;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import com.flagstone.transform.image.DefineImage2;
import com.flagstone.transform.image.ImageTag;

public class Graphic {
	private static byte[] buffer = new byte[4096];
	private int width, height; private byte data[]; private BufferedImage cache;
	public Graphic(BufferedImage im) throws Exception {
		width = im.getWidth(); height = im.getHeight();
		if(im.getType() != BufferedImage.TYPE_INT_ARGB){
			BufferedImage i = im; im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = im.createGraphics(); g.drawImage(i, 0, 0, null); g.dispose();
		} cache = im; int buf[] = ((DataBufferInt)im.getData().getDataBuffer()).getData();
		ByteBuffer byteBuffer = ByteBuffer.allocate(buf.length*4);
		byteBuffer.asIntBuffer().put(buf); Deflater deflater = new Deflater();
		deflater.setInput(byteBuffer.array()); deflater.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while(!deflater.finished()){
			int count = deflater.deflate(buffer); out.write(buffer, 0, count);
		} data = out.toByteArray();
	}
	public Graphic(File f) throws Exception {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		width = in.readInt(); height = in.readInt(); int len = in.readInt(); data = new byte[len];
		in.read(data); in.close();
	}
	public ImageTag defineImage(int i){
		return new DefineImage2(i, width, height, data);
	}
	public BufferedImage getBufferedImage() throws Exception {
		if(cache == null){
			Inflater inflater = new Inflater(); inflater.setInput(data);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while(!inflater.finished()){
				int count = inflater.inflate(buffer); out.write(buffer, 0, count);
			} cache = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			IntBuffer i = ByteBuffer.wrap(out.toByteArray()).asIntBuffer();
			Raster r = cache.getData(); i.get(((DataBufferInt)r.getDataBuffer()).getData());
			cache.setData(r);
		} return cache;
	}
	public void write(File f) throws Exception {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		out.writeInt(width); out.writeInt(height); out.writeInt(data.length); out.write(data);
		out.flush(); out.close();
	}
	
	public static Graphic decode(File f) throws Exception {return new Graphic(ImageIO.read(f));}
}
