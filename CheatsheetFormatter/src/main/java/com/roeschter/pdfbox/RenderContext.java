package com.roeschter.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class RenderContext {
	boolean render = false;

	PDPageContentStream contentStream;
	PDDocument document;

	FontContext title;
	FontContext header;
	FontContext body;

	float xPos = 0;
	float yPos = 0;


	float borderTop;
	float borderbottom;
	float borderleft;
	float borderright;
	float titleheight;

	float viewHeight;
	float viewWidth;

	int lanes;
	float laneborder;
	float lanewidth;

	float blocKTitleSpacing = 6;
	float blockSpacing = 4;

	float lineSpacing = (float)0.2;

	public float laneHeight;

	float titleFontSize;
	float headerFontSize;
	float bodyFontSize;


	public void makeFonts() {
		FontContext baseFont = new FontContext();
		baseFont.regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
		baseFont.bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
		baseFont.cursive = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
		baseFont.cursiveBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);

		FontContext courier = new FontContext();
		courier.regular = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);
		courier.bold = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);
		courier.cursive = new PDType1Font(Standard14Fonts.FontName.COURIER_OBLIQUE);
		courier.cursiveBold = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD_OBLIQUE);

		title = new FontContext( baseFont, titleFontSize );
		header = new FontContext( baseFont, headerFontSize );
		body = new FontContext( baseFont, bodyFontSize );

	}

}


