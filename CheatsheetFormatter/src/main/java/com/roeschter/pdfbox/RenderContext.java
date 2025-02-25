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

	JSONObject style;

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



	public RenderContext( JSONObject _style ) {
		style = _style;
		document = new PDDocument();
	}

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


	public PDPage addPAge( ) throws Exception {
		PDPage page;
		rectangle  = PDRectangle.A4;

		if ( get(style, "orientation", "potrait" ).equals("landscape")) {
			rectangle = new PDRectangle( PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth() ) ;
		}
		page = new PDPage( rectangle);
		document.addPage(page);
		contentStream = new PDPageContentStream(document, page);
		return page;
	}

	public void setupStyle( ) throws IOException {

		borderTop = get(style,"bordertop",0.0);
		borderbottom = get(style,"borderbottom",0.0);
		borderleft = get(style,"borderleft",0.0);
		borderright = get(style,"borderright",0.0);

		backgroundTLLogo = get( style, "backgroundTLLogo", null );
		backgroundBRLogo = get( style, "backgroundBRLogo", null );
		backgroundTLWidth =  get(style,"backgroundTLWidth", 150.0);
		backgroundTLHeight = get(style,"backgroundTLHeight", 40.0);
		backgroundBRWidth =  get(style,"backgroundBRWidth", 150.0);
		backgroundBRHeight = get(style,"backgroundBRHeight", 40.0);


		headerFontSize = get(style,"headerFontSize",30);
		headerYOffset = get(style,"headerYOffset",0);
		headerHeight = get(style,"headerHeight",40);
		headerLogoHeight = get(style,"headerLogoHeight",40);
		headerLogo = get(style,"headerLogo", null );

		viewHeight = rectangle.getHeight() - borderTop - borderbottom - headerHeight;
		viewWidth = rectangle.getWidth() - borderleft - borderright;

		lanes = get( style, "lanes", 1 );
		laneborder = get(style,"laneborder",0.0);
		lanewidth = (viewWidth - (lanes-1)*laneborder) /lanes;
		laneHeight = viewHeight;
		laneTop = rectangle.getHeight() - borderTop - headerHeight;


		titleFontSize = get(style,"titleFontSize",24);
		titleLineSpacingRel = get(style,"titleSpacingRel",(float)0);
		titleParagraphSpacingRel = get(style,"titleParagraphSpacingRel",(float)0);
		blockSpacing = get(style,"blockSpacing",4);


		underLineLogo = get( style, "underLineLogo", "underline.png" );
		underlineHeightRel = get(style,"underlineHeightRel",(float)0);
		underlineBorderRel = get(style,"underlineBorderRel",(float)0);


		bullet = get( style, "bullet", "\u2022" );
		bulletSizeRel = get(style,"bulletSizeRel",(float)1.3);
		bulletOffetRel = get(style,"bulletOffetRel",(float)0.5);
		bulletSpacingRel = get(style,"bulletSpacingRel",(float)0.3);


		bodyFontSize = get(style,"bodyFontSize",8);
		bodyLineSpacingRel = get(style,"bodyLineSpacingRel",(float)0);
		bodyParagraphSpacingRel = get(style,"bodyParagraphSpacingRel",(float)0);
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


