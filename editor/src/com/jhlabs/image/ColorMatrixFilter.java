/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs.image;

public class ColorMatrixFilter extends PointFilter {
	
	public ColorMatrix matrix;

	public ColorMatrixFilter() {this(new ColorMatrix());}

	public ColorMatrixFilter(ColorMatrix c) {
		matrix = c; canFilterIndexColorModel = true;
	}
	
	public int filterRGB(int x, int y, int rgb) {
		int sa = (rgb >> 24) & 0xff;
		int sr = (rgb >> 16) & 0xff;
		int sg = (rgb >> 8) & 0xff;
		int sb = rgb & 0xff;
		double[] m = matrix.matrix;
		int rr = PixelUtils.clamp((int)(sr*m[0]+sg*m[1]+sb*m[2]+sa*m[3]+m[4]));
		int rg = PixelUtils.clamp((int)(sr*m[5]+sg*m[6]+sb*m[7]+sa*m[8]+m[9]));
		int rb = PixelUtils.clamp((int)(sr*m[10]+sg*m[11]+sb*m[12]+sa*m[13]+m[14]));
		int ra = PixelUtils.clamp((int)(sr*m[15]+sg*m[16]+sb*m[17]+sa*m[18]+m[19]));
		return (ra << 24) | (rr << 16) | (rg << 8) | rb;
	}

	public String toString() {
		return "Colors/Matrix...";
	}
}

