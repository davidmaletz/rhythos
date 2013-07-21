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
package com.jhlabs.image;

import java.awt.image.BufferedImage;

public class OuterGlowFilter extends BoxBlurFilter {
	public Glow glow;
	public OuterGlowFilter(Glow g){
		glow = g; setHRadius(g.blurX>>1); setVRadius(g.blurY>>1); setIterations(1);
	}
	public BufferedImage filter(BufferedImage src, BufferedImage dst){
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );

		for (int i = 0; i < getIterations(); i++ ) {
            blur( inPixels, outPixels, width, height, getHRadius() );
            blur( outPixels, inPixels, height, width, getVRadius() );
        } return new AddGlowFilter(inPixels, width, glow.color.getRed(), glow.color.getGreen(), glow.color.getBlue(), glow.strength).filter(src, dst);
	}
	
	private static class AddGlowFilter extends PointFilter {
		private int[] glow; private int width; private int red, green, blue; private float strength;
		public AddGlowFilter(int[] glow, int w, int r, int g, int b, float s){
			this.glow = glow; width = w; red = r; green = g; blue = b; strength = s;
		}
		public int filterRGB(int x, int y, int rgb){
			int a = (rgb >> 24) & 0xff;
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = rgb & 0xff;
			double alpha = a/255.0;
			int blur = PixelUtils.clamp((int)(((glow[y*width+x] >> 24) & 0xff)*strength));
			r = PixelUtils.clamp((int)(r*alpha+red*(1-alpha)));
			g = PixelUtils.clamp((int)(g*alpha+green*(1-alpha)));
			b = PixelUtils.clamp((int)(b*alpha+blue*(1-alpha)));
			a = PixelUtils.clamp((int)(a+blur*(1-alpha)));
			return (a << 24) | (r << 16) | (g << 8) | b;
		}
	}
}
