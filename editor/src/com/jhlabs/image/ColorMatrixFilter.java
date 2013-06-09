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
		int a = (rgb >> 24) & 0xff;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		double[] m = matrix.matrix;
		r = PixelUtils.clamp((int)(r*m[0]+g*m[1]+b*m[2]+a*m[3]+m[4]));
		g = PixelUtils.clamp((int)(r*m[5]+g*m[6]+b*m[7]+a*m[8]+m[9]));
		b = PixelUtils.clamp((int)(r*m[10]+g*m[11]+b*m[12]+a*m[13]+m[14]));
		a = PixelUtils.clamp((int)(r*m[15]+g*m[16]+b*m[17]+a*m[18]+m[19]));
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public String toString() {
		return "Colors/Matrix...";
	}
}

