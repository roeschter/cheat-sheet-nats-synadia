package com.roeschter.pdfbox;

import org.apache.pdfbox.pdmodel.font.PDFont;
import java.awt.Color;

public class FontContext {

	Font regular;
	Font fixed;

	FontContext() {
	}

	FontContext( Font _regular, float sizeRegular, Color cRegular, Font _fixed, float sizeFixed, Color cFixed) {
		regular = new Font( _regular, sizeRegular, cRegular);
		fixed = new Font( _fixed, sizeFixed, cFixed);
	}

}
