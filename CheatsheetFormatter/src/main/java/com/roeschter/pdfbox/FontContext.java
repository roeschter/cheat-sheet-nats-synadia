package com.roeschter.pdfbox;

import org.apache.pdfbox.pdmodel.font.PDFont;
import java.awt.Color;

public class FontContext {

	Font prop;
	Font fixed;

	FontContext() {
	}

	FontContext( Font _proportional, float sizeProportional, Color cProportional, Font _fixed, float sizeFixed, Color cFixed) {
		prop = new Font( _proportional, sizeProportional, cProportional);
		fixed = new Font( _fixed, sizeFixed, cFixed);
	}

}
