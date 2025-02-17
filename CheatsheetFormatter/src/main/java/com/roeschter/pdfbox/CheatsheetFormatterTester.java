package com.roeschter.pdfbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class CheatsheetFormatterTester {

	JSONObject style;
	JSONObject content;
	String output;

	public CheatsheetFormatterTester( String _style, String _content ) throws JSONException, IOException
	{
		style = new JSONObject( Files.readString(Path.of(_style)) );
		content = new JSONObject( Files.readString(Path.of(_content)) );
		output = _content + ".pdf";
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

	 public static float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
	        float widthInUnits = font.getStringWidth(text);
	        return (widthInUnits / 1000) * fontSize; // Convert to user space
	 }

	public void format() throws IOException {
		PDDocument document = new PDDocument();
		PDPage page;
		PDRectangle r  = PDRectangle.A4;
		if ( get(style, "orientation", "potrait" ).equals("landscape")) {
			r = new PDRectangle( PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth() ) ;
		}
		page = new PDPage( r);

		document.addPage(page);

		float borderTop = get(style,"bordertop",0.0);
		float borderbottom = get(style,"borderbottom",0.0);
		float borderleft = get(style,"borderleft",0.0);
		float borderright = get(style,"borderright",0.0);
		float titleheight = get(style,"titleheight",0.0);

		float viewHeight = r.getHeight() - borderTop - borderbottom - titleheight;
		float viewWidth = r.getWidth() - borderleft - borderright;

		int lanes = get( style, "lanes", 1 );
		float laneborder = get(style,"laneborder",0.0);
		float lanewidth = (viewWidth - (lanes-1)*laneborder) /lanes;


		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		PDFont fontHeader = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
		PDFont fontBody = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
		PDFont fontBodyBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

		float xPos = borderleft;
		float yPos = r.getHeight() - borderTop - titleheight;
		float headerFontSize = 12;
		float bodyFontSize = 12;


		String text = "Hello World! Be blessed.";
		float width = getTextWidth( text, fontHeader, headerFontSize);
		System.out.println("Lane width: " + lanewidth);
        System.out.println("Text width: " + width);

		yPos -= headerFontSize;
		contentStream.beginText();
		contentStream.setFont( fontHeader, headerFontSize);
		contentStream.newLineAtOffset(xPos,yPos);
		contentStream.showText(text);
		contentStream.endText();

		yPos -= headerFontSize;
		contentStream.beginText();
		contentStream.setFont( fontHeader, headerFontSize);
		contentStream.newLineAtOffset(xPos + lanewidth ,yPos);
		contentStream.showText("World!");
		contentStream.endText();

		contentStream.close();




		document.save(output);
		document.close();
		System.out.println("Done writing to: " + output);
	}


	public static void main(String[] arg) throws Exception {
		int i=0;

		String _style = null;
		String _content = null;
		String output = null;


		while ( arg.length > i)
		{
			if ( arg[i].equals("-style") || arg[i].equals("-s"))
			{
				i++;
				_style = arg[i];
				i++;
			} else if ( arg[i].equals("-content") || arg[i].equals("-c"))
			{
				i++;
				_content = arg[i];
				i++;
			} else if ( arg[i].equals("-output") || arg[i].equals("-o"))
			{
				i++;
				output = arg[i];
				i++;
			}  else if (arg[i].equals("--")) { //Terminate parameter processing
				i = arg.length;
			}  else {
				System.out.println("Unknown command line parameter: " + arg[i]);
				System.exit(1);
			}
		}

		if ( _style == null || _content == null) {
			System.out.println("Options: ");
			System.out.println("-style  <json style template>");
			System.out.println("-content <json content template>");
			System.exit(1);
		}


		CheatsheetFormatterTester csf = new CheatsheetFormatterTester(_style, _content);
		if ( output != null)
			csf.output = output;

		csf.format();

		System.out.println("Exiting");

	}

}
