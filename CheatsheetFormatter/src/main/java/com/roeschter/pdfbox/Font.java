package com.roeschter.pdfbox;

import java.awt.Color;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class Font {

	PDFont regular;
	PDFont bold;
	PDFont cursive;
	PDFont cursiveBold;

	float size;
	Color color;

	Font() {
	}

	Font( Font ctx, float _size, Color _color) {
		regular = ctx.regular;
		bold = ctx.bold;
		cursive = ctx.cursive;
		cursiveBold = ctx.cursiveBold;
		size = _size;
		color = _color;
	}

}
