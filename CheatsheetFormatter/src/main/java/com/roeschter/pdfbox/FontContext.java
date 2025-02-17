package com.roeschter.pdfbox;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class FontContext {

	PDFont regular;
	PDFont bold;
	PDFont cursive;
	PDFont cursiveBold;

	float size;

	FontContext() {
	}

	FontContext( FontContext ctx, float _size) {
		regular = ctx.regular;
		bold = ctx.bold;
		cursive = ctx.cursive;
		cursiveBold = ctx.cursiveBold;
		size = _size;
	}

}
