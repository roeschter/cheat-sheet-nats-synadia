package com.roeschter.pdfbox;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;

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
	FontContext footer;
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
	Color headerFontColor;
	float headerYOffset;
	String headerLogo;
	float headerHeight;
	float headerLogoHeight;

	float footerFontSize;
	Color footerFontColor;
	boolean footerDate;
	Padding footerPadding;

	float titleFontSize;
	Color titleFontColor;
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
	Color bodyFontColor;
	float bodyLineSpacingRel;
	float bodyParagraphSpacingRel;

	float bodyFixedFontSize;
	Color bodyFixedFontColor;

	public RenderContext( Config _style ) {
		style = _style;
		document = new PDDocument();
	}

    HashMap<Integer,Float> laneReservedTop = new HashMap<Integer,Float> ();
    HashMap<Integer,Float> laneReservedBottom = new HashMap<Integer,Float> ();

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

    public void setLaneReservedTop( int lane, float reserved ) {
        laneReservedTop.put(-lane, reserved );
    }
    public void setLaneReservedBottom( int lane, float reserved ) {
        laneReservedBottom.put( lane, reserved );
    }

    public float getLaneReservedTop( int lane) {
        Float reserved = laneReservedTop.get( -lane );
        return (reserved==null)?0:reserved.floatValue();
    }
    public float getLaneReservedBottom( int lane) {
        Float reserved = laneReservedBottom.get( lane );
        return (reserved==null)?0:reserved.floatValue();
    }

	public float getLaneWidthForLane( int n ) {
		if ( laneWidthRel != null) {
			n = Math.min(n, laneWidthRel.length-1 );
			return ( lanewidth *  laneWidthRel[n]);
		} else {
			return lanewidth;
		}
	}

	public float getLaneTotalWidth( int lane, int count ) {
		float width = 0;
		for ( int i=lane; i<lane+count; i++) {
			if ( i != lane)
				width += this.laneborder;
			width += getLaneWidthForLane(i);
		}
		return width;
	}

	public float getLaneXPos( int lane )
	{
		int page =lane / lanes;
		float xPos = borderleft;;
		for ( int i=page*lanes; i< lane; i++ ) {
			xPos += getLaneWidthForLane(i);
			xPos += laneborder;
		}
		return xPos;
	}

	public float getLaneYPos( int lane )
	{
		return laneTop - getLaneReservedTop(lane);
	}

	public float getLaneHeight( int lane )
	{
		return laneHeight - getLaneReservedTop(lane) - getLaneReservedBottom(lane);
	}

    public int[] getFilter(JSONObject json) {
    	Config conf = new Config(json, null);
    	return conf.getIntArray("exclude");
    }

    public boolean isFiltered(int[] filter, int pos) {
        if ( filter == null )
            return false;

        for (int i: filter )
            if ( i==pos ) return true;

        return false;
    }


	public void setupStyle( ) throws IOException {

		borderTop = style.getFloat("bordertop",0.0);
		borderbottom = style.getFloat("borderbottom",0.0);
		borderleft = style.getFloat("borderleft",0.0);
		borderright = style.getFloat("borderright",0.0);

		backgroundTLLogo = style.get( "backgroundTLLogo", null );
		backgroundBRLogo = style.get( "backgroundBRLogo", null );
		backgroundTLWidth =  style.getFloat("backgroundTLWidth", 150.0);
		backgroundTLHeight = style.getFloat("backgroundTLHeight", 40.0);
		backgroundBRWidth =  style.getFloat("backgroundBRWidth", 150.0);
		backgroundBRHeight = style.getFloat("backgroundBRHeight", 40.0);


		headerFontSize = style.getFloat("headerFontSize",30);
		headerFontColor = style.getColor("headerFontColor",Color.black);
		headerYOffset = style.getFloat("headerYOffset",0);
		headerHeight = style.getFloat("headerHeight",40);
		headerLogoHeight = style.getFloat("headerLogoHeight",40);
		headerLogo = style.get("headerLogo", null );

		footerFontSize = style.getFloat("footerFontSize",30);
		footerFontColor = style.getColor("footerFontColor",Color.black);
		footerPadding= new Padding(style, "footerPadding");
		footerDate = style.getBool("footerDate", false);

		viewHeight = rectangle.getHeight() - borderTop - borderbottom - headerHeight;
		viewWidth = rectangle.getWidth() - borderleft - borderright;

		lanes = style.getInt( "lanes", 1 );
		laneborder = style.getFloat("laneborder",0.0);
		laneWidthRel = style.getFloatArray("laneWidthRel");

		lanewidth = (viewWidth - (lanes-1)*laneborder) /lanes;
		laneHeight = viewHeight;
		laneTop = rectangle.getHeight() - borderTop - headerHeight;


		titleFontSize = style.getFloat("titleFontSize",24);
		titleFontColor = style.getColor("titleFontColor",Color.black);
		titleLineSpacingRel = style.getFloat("titleSpacingRel",0.0);
		titleParagraphSpacingRel = style.getFloat("titleParagraphSpacingRel",0.0);
		blockSpacing = style.getFloat("blockSpacing",4);


		underLineLogo = style.get( "underLineLogo", "underline.png" );
		underlineHeightRel = style.getFloat("underlineHeightRel",0.0);
		underlineBorderRel = style.getFloat("underlineBorderRel",0.0);


		bullet = style.get( "bullet", "\u2022" );
		bulletSizeRel = style.getFloat("bulletSizeRel",1.3);
		bulletOffetRel = style.getFloat("bulletOffetRel",0.5);
		bulletSpacingRel = style.getFloat("bulletSpacingRel",0.3);


		bodyFontSize = style.getFloat("bodyFontSize",8);
		bodyFontColor = style.getColor("bodyFontColor",Color.black);
		bodyLineSpacingRel = style.getFloat("bodyLineSpacingRel",0);
		bodyParagraphSpacingRel = style.getFloat("bodyParagraphSpacingRel",0);

		bodyFixedFontSize = style.getFloat("bodyFixedFontSize",8);
		bodyFixedFontColor = style.getColor("bodyFixedFontColor",Color.black);
	}


	HashMap<String,Font> fonts = new HashMap<String,Font>();

	public Font pickFont( String type ) {
		String name =style.get( type+"Font", "times").toLowerCase();
		Font ret =fonts.get(name);

		if ( ret == null)
			throw new RuntimeException("Font not found: " + name);

		return ret;
	}

	public void makeFonts() {
		Font helvetica = new Font();
		helvetica.regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
		helvetica.bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
		helvetica.cursive = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
		helvetica.cursiveBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);
		fonts.put("helvetica", helvetica);

		Font times = new Font();
		times.regular = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
		times.bold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
		times.cursive = new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);
		times.cursiveBold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD_ITALIC);
		fonts.put("times", times);

		Font courier = new Font();
		//Standard14Fonts.FontName.
		courier.regular = new PDType1Font(Standard14Fonts.FontName.COURIER);
		courier.bold = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);
		courier.cursive = new PDType1Font(Standard14Fonts.FontName.COURIER_OBLIQUE);
		courier.cursiveBold = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD_OBLIQUE);
		fonts.put("courier", courier);

		header = new FontContext( pickFont("header"), headerFontSize,  headerFontColor, courier,  headerFontSize, headerFontColor );
		footer = new FontContext( pickFont("footer"), footerFontSize,  footerFontColor, courier,  footerFontSize, footerFontColor );
		title = new FontContext( pickFont("title"), titleFontSize, titleFontColor, courier,  titleFontSize, titleFontColor );
		body = new FontContext( pickFont("body"), bodyFontSize, bodyFontColor, courier,  bodyFixedFontSize, bodyFixedFontColor );
	}


}


