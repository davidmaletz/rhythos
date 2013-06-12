package com.jhlabs.image;

import java.awt.Color;

public class Glow {
	public Color color; public int blurX, blurY; public float strength; public int quality;
	public Glow(){color = Color.black; blurX = 4; blurY = 4; strength = 0; quality = 0;}
	public Glow(Glow g){color = g.color; blurX = g.blurX; blurY = g.blurY; strength = g.strength; quality = g.quality;}
}
