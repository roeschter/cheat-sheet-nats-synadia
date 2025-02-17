package com.roeschter.pdfbox;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class TextGraphics extends Text {

	PDImageXObject image;

	float gWidth;
	float gHeight;

	float xBorder = 0;
	float yBorder = 0;

	public TextGraphics( String imagePath, PDDocument document) {
		try {
			image = PDImageXObject.createFromFile(imagePath, document);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void layout() {
		if ( width != 0 )
			return;

		if ( gWidth != 0 )
			width = gWidth;
		else
			width = gWidth = image.getWidth();

		if ( gHeight != 0)
			height =  gHeight;
		else
			height = gHeight = image.getHeight();

		width += 2*xBorder;
		height += 2*yBorder;

	}



	@Override
	public void render( RenderContext ctx ) throws Exception {
		PDPageContentStream contentStream = ctx.contentStream;
		contentStream.drawImage(image, ctx.xPos + xBorder, ctx.yPos - yBorder - gHeight , gWidth, gHeight);

		ctx.yPos -= height;
		ctx.xPos += width;
	}
}
