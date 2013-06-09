package com.jhlabs.image;

import java.util.Arrays;

/**
* ColorMatrix by Grant Skinner. August 8, 2005
* Updated to AS3 November 19, 2007
* Visit www.gskinner.com/blog for documentation, updates and more free code.
*
* You may distribute this class freely, provided it is not modified in any way (including
* removing this header or changing the package path).
*
* Please contact info@gskinner.com prior to distributing modified versions of this class.
*/
public class ColorMatrix {
	private static final double[] DELTA_INDEX = {
		0,    0.01, 0.02, 0.04, 0.05, 0.06, 0.07, 0.08, 0.1,  0.11,
		0.12, 0.14, 0.15, 0.16, 0.17, 0.18, 0.20, 0.21, 0.22, 0.24,
		0.25, 0.27, 0.28, 0.30, 0.32, 0.34, 0.36, 0.38, 0.40, 0.42,
		0.44, 0.46, 0.48, 0.5,  0.53, 0.56, 0.59, 0.62, 0.65, 0.68, 
		0.71, 0.74, 0.77, 0.80, 0.83, 0.86, 0.89, 0.92, 0.95, 0.98,
		1.0,  1.06, 1.12, 1.18, 1.24, 1.30, 1.36, 1.42, 1.48, 1.54,
		1.60, 1.66, 1.72, 1.78, 1.84, 1.90, 1.96, 2.0,  2.12, 2.25, 
		2.37, 2.50, 2.62, 2.75, 2.87, 3.0,  3.2,  3.4,  3.6,  3.8,
		4.0,  4.3,  4.7,  4.9,  5.0,  5.5,  6.0,  6.5,  6.8,  7.0,
		7.3,  7.5,  7.8,  8.0,  8.4,  8.7,  9.0,  9.4,  9.6,  9.8, 
		10.0
	};

	// identity matrix constant:
	private static final double[] IDENTITY_MATRIX = {
		1,0,0,0,0,
		0,1,0,0,0,
		0,0,1,0,0,
		0,0,0,1,0,
		0,0,0,0,1
	};
	private static final double LENGTH = IDENTITY_MATRIX.length;

	public double[] matrix;
// initialization:
	public ColorMatrix(){this(null);}
	public ColorMatrix(double[] p_matrix) {
		matrix = new double[25];
		copyMatrix(((p_matrix != null && p_matrix.length == LENGTH) ? p_matrix : IDENTITY_MATRIX));
	}
	
	
// public methods:
	public void reset() {
		for(int i=0; i<LENGTH; i++) {
			matrix[i] = IDENTITY_MATRIX[i];
		}
	}

	public void adjustColor(double p_brightness,double p_contrast,double p_saturation,double p_hue) {
		adjustHue(p_hue);
		adjustContrast(p_contrast);
		adjustBrightness(p_brightness);
		adjustSaturation(p_saturation);
	}

	public void adjustBrightness(double p_val) {
		p_val = cleanValue(p_val,100);
		if (p_val == 0 || Double.isNaN(p_val)) { return; }
		multiplyMatrix(new double[]{
			1,0,0,0,p_val,
			0,1,0,0,p_val,
			0,0,1,0,p_val,
			0,0,0,1,0,
			0,0,0,0,1
		});
	}

	public void adjustContrast(double p_val) {
		p_val = cleanValue(p_val,100);
		if (p_val == 0 || Double.isNaN(p_val)) { return; }
		double x;
		if (p_val<0) {
			x = 127+p_val/100*127;
		} else {
			x = p_val%1;
			if (x == 0) {
				x = DELTA_INDEX[(int)Math.floor(p_val)];
			} else {
				//x = DELTA_INDEX[(p_val<<0)]; // this is how the IDE does it.
				x = DELTA_INDEX[(int)Math.floor(p_val)]*(1-x)+DELTA_INDEX[(int)Math.floor(p_val)+1]*x; // use linear interpolation for more granularity.
			}
			x = x*127+127;
		}
		multiplyMatrix(new double[]{
			x/127,0,0,0,0.5*(127-x),
			0,x/127,0,0,0.5*(127-x),
			0,0,x/127,0,0.5*(127-x),
			0,0,0,1,0,
			0,0,0,0,1
		});
	}

	public void adjustSaturation(double p_val) {
		p_val = cleanValue(p_val,100);
		if (p_val == 0 || Double.isNaN(p_val)) { return; }
		double x = 1+((p_val > 0) ? 3*p_val/100 : p_val/100);
		double lumR = 0.3086;
		double lumG = 0.6094;
		double lumB = 0.0820;
		multiplyMatrix(new double[]{
			lumR*(1-x)+x,lumG*(1-x),lumB*(1-x),0,0,
			lumR*(1-x),lumG*(1-x)+x,lumB*(1-x),0,0,
			lumR*(1-x),lumG*(1-x),lumB*(1-x)+x,0,0,
			0,0,0,1,0,
			0,0,0,0,1
		});
	}

	public void adjustHue(double p_val) {
		p_val = cleanValue(p_val,180)/180*Math.PI;
		if (p_val == 0 || Double.isNaN(p_val)) { return; }
		double cosVal = Math.cos(p_val);
		double sinVal = Math.sin(p_val);
		double lumR = 0.213;
		double lumG = 0.715;
		double lumB = 0.072;
		multiplyMatrix(new double[]{
			lumR+cosVal*(1-lumR)+sinVal*(-lumR),lumG+cosVal*(-lumG)+sinVal*(-lumG),lumB+cosVal*(-lumB)+sinVal*(1-lumB),0,0,
			lumR+cosVal*(-lumR)+sinVal*(0.143),lumG+cosVal*(1-lumG)+sinVal*(0.140),lumB+cosVal*(-lumB)+sinVal*(-0.283),0,0,
			lumR+cosVal*(-lumR)+sinVal*(-(1-lumR)),lumG+cosVal*(-lumG)+sinVal*(lumG),lumB+cosVal*(1-lumB)+sinVal*(lumB),0,0,
			0,0,0,1,0,
			0,0,0,0,1
		});
	}

	public void concat(double[] p_matrix) {
		if (p_matrix.length != LENGTH) { return; }
		multiplyMatrix(p_matrix);
	}
	
	public ColorMatrix clone() {
		return new ColorMatrix(matrix);
	}

	public String toString() {
		return "ColorMatrix "+Arrays.toString(matrix);
	}

// private methods:
	// copy the specified matrix's values to this matrix:
	protected void copyMatrix(double[] p_matrix) {
		for (int i=0;i<LENGTH;i++) {
			matrix[i] = p_matrix[i];
		}
	}

	// multiplies one matrix against another:
	protected void multiplyMatrix(double[] p_matrix) {
		double[] col = new double[5];
		
		for (int i=0;i<5;i++) {
			for (int j=0;j<5;j++) {
				col[j] = matrix[j+i*5];
			}
			for (int j=0;j<5;j++) {
				double val=0;
				for (int k=0;k<5;k++) {
					val += p_matrix[j+k*5]*col[k];
				}
				matrix[j+i*5] = val;
			}
		}
	}
	
	// make sure values are within the specified range, hue has a limit of 180, others are 100:
	protected double cleanValue(double p_val,double p_limit) {
		return Math.min(p_limit,Math.max(-p_limit,p_val));
	}
}