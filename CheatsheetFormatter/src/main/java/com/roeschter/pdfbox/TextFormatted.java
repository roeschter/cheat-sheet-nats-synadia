package com.roeschter.pdfbox;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import static com.roeschter.pdfbox.CheatsheetFormatter.*;


public class TextFormatted extends Text {

	PDFont font;
	float fontSize;
	Color color;
	public String text;
	public float yOffset;

	public TextFormatted( String _text, PDFont _font, float size, Color _color) {
		font = _font;
		fontSize = size;
		text = _text;
		color = _color;
	}

	@Override
	public TextFormatted clone() {
		return new TextFormatted( text, font, fontSize, color);
	}

	@Override
	public void setWidth(float _width) {
		//Ignore
	}


	@Override
	public void layout() {
		if ( width != 0 )
			return;
		try {
			height = fontSize;
			width = getTextWidth( text, font, fontSize);
		} catch (Exception e ) {
			e.printStackTrace();
		}

	}

	@Override
	public void render( RenderContext ctx ) throws Exception {
		PDPageContentStream contentStream = ctx.contentStream;
		contentStream.beginText();
		contentStream.setNonStrokingColor(color);
		contentStream.setFont( font, fontSize);
		float yPos = ctx.yPos - height - yOffset;
		trace(ctx.xPos, yPos, text );
		contentStream.newLineAtOffset( ctx.xPos , yPos);
		contentStream.showText(text);
		contentStream.endText();
		ctx.xPos += width;
	}
}
