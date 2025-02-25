package com.roeschter.pdfbox;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.json.JSONObject;

public class RenderContext {
	boolean render = false;

	Config style;

	PDPageContentStream contentStream;
	PDDocument document;
	PDRectangle rectangle;

	FontContext title;
	FontContext header;
	FontContext body;

	float xPos = 0;
	float yPos = 0;


	float borderTop;
	float borderbottom;
	float borderleft;
	float borderright;

	String backgroundTLLogo;
	String backgroundBRLogo;
	float backgroundTLWidth;
	float backgroundTLHeight;
	float backgroundBRWidth;
	float backgroundBRHeight;

	float viewHeight;
	float viewWidth;

	int lanes;
	float laneborder;
	float[] laneWidthRel;
	float lanewidth;
	float laneTop;

	float blockSpacing ;;

	public float laneHeight;

	float headerFontSize;
	float headerYOffset;
	String headerLogo;
	float headerHeight;
	float headerLogoHeight;

	float titleFontSize;
	float titleLineSpacingRel;
	float titleParagraphSpacingRel;

	float underlineHeightRel;
	float underlineBorderRel;
	String underLineLogo;


	String bullet;
	float bulletSizeRel;
	float bulletOffetRel;
	float bulletSpacingRel;

	float bodyFontSize;
	float bodyLineSpacingRel;
	float bodyParagraphSpacingRel;



	public RenderContext( Config _style ) {
		style = _style;
		document = new PDDocument();
	}

	/*
	public String get( JSONObject json, String key, String _default) {
		if ( json.isNull(key))
			return _default;

		return json.getString(key);
	}

	public float get( JSONObject json, String key, double _default) {
		if ( json.isNull(key))
			return (float)_default;

		return (float)json.getDouble(key);
	}

	public int get( JSONObject json, String key, int _default) {
		if ( json.isNull(key))
			return _default;

		return (int)json.getLong(key);
	}
	*/

	public PDPage addPAge( ) throws Exception {
		PDPage page;
		rectangle  = PDRectangle.A4;

		if ( style.get( "orientation", "potrait" ).equals("landscape")) {
			rectangle = new PDRectangle( PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth() ) ;
		}
		page = new PDPage( rectangle);
		document.addPage(page);
		contentStream = new PDPageContentStream(document, page);
		return page;
	}

	public void setupStyle( ) throws IOException {

		borderTop = style.get("bordertop",0.0);
		borderbottom = style.get("borderbottom",0.0);
		borderleft = style.get("borderleft",0.0);
		borderright = style.get("borderright",0.0);

		backgroundTLLogo = style.get( "backgroundTLLogo", null );
		backgroundBRLogo = style.get( "backgroundBRLogo", null );
		backgroundTLWidth =  style.get("backgroundTLWidth", 150.0);
		backgroundTLHeight = style.get("backgroundTLHeight", 40.0);
		backgroundBRWidth =  style.get("backgroundBRWidth", 150.0);
		backgroundBRHeight = style.get("backgroundBRHeight", 40.0);


		headerFontSize = style.get("headerFontSize",30);
		headerYOffset = style.get("headerYOffset",0);
		headerHeight = style.get("headerHeight",40);
		headerLogoHeight = style.get("headerLogoHeight",40);
		headerLogo = style.get("headerLogo", null );

		viewHeight = rectangle.getHeight() - borderTop - borderbottom - headerHeight;
		viewWidth = rectangle.getWidth() - borderleft - borderright;

		lanes = style.get( "lanes", 1 );
		laneborder = style.get("laneborder",0.0);
		laneWidthRel = style.getFloatArray("laneWidthRel");

		lanewidth = (viewWidth - (lanes-1)*laneborder) /lanes;
		laneHeight = viewHeight;
		laneTop = rectangle.getHeight() - borderTop - headerHeight;


		titleFontSize = style.get("titleFontSize",24);
		titleLineSpacingRel = style.get("titleSpacingRel",0.0);
		titleParagraphSpacingRel = style.get("titleParagraphSpacingRel",0.0);
		blockSpacing = style.get("blockSpacing",4);


		underLineLogo = style.get( "underLineLogo", "underline.png" );
		underlineHeightRel = style.get("underlineHeightRel",0.0);
		underlineBorderRel = style.get("underlineBorderRel",0.0);


		bullet = style.get( "bullet", "\u2022" );
		bulletSizeRel = style.get("bulletSizeRel",1.3);
		bulletOffetRel = style.get("bulletOffetRel",0.5);
		bulletSpacingRel = style.get("bulletSpacingRel",0.3);


		bodyFontSize = style.get("bodyFontSize",8);
		bodyLineSpacingRel = style.get("bodyLineSpacingRel",(float)0);
		bodyParagraphSpacingRel = style.get("bodyParagraphSpacingRel",(float)0);
	}

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

		header = new FontContext( baseFont, headerFontSize );
		title = new FontContext( baseFont, titleFontSize );
		body = new FontContext( baseFont, bodyFontSize );
	}

}


