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

public class ColorTransformFilter extends PointFilter {
	
	public double redMultiplier=1, greenMultiplier=1, blueMultiplier=1, alphaMultiplier=1;
	public double redOffset=0, greenOffset=0, blueOffset=0, alphaOffset=0;

	public ColorTransformFilter() {}

	public int filterRGB(int x, int y, int rgb) {
		int a = (rgb >> 24) & 0xff;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		r = PixelUtils.clamp((int)(r*redMultiplier+redOffset));
		g = PixelUtils.clamp((int)(g*greenMultiplier+greenOffset));
		b = PixelUtils.clamp((int)(b*blueMultiplier+blueOffset));
		a = PixelUtils.clamp((int)(a*alphaMultiplier+alphaOffset));
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public String toString() {
		return "Colors/Matrix...";
	}
}

