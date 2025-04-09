package com.roeschter.pdfbox;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class TextGraphics extends Text {

	PDImageXObject image = null;

	float gWidth;
	float gHeight;

	float xBorder = 0;
	float yBorder = 0;

	boolean fixedHeight = false;

	public TextGraphics( String imagePath, PDDocument document) {
		name = imagePath;
		if ( imagePath == null || imagePath.length()==0) {
			return;
		}

		try {
			image = PDImageXObject.createFromFile(imagePath, document);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void setWidth(float _width) {
		//Scale to new width, respecting borders
		float nWidth = _width - 2*xBorder;
		if (!fixedHeight)
			gHeight = gHeight * nWidth / gWidth ;
		gWidth = nWidth;
	}


	@Override
	public void layout() {

		if ( gWidth==0 && gHeight==0) {
			width = gWidth = image.getWidth();
			height = gHeight = image.getHeight();
		} else if ( gWidth!=0 && gHeight!=0 ) {
			width = gWidth;
			height = gHeight;
		} else if (  gWidth!=0 ) {
			width = gWidth;
			height = image.getHeight();
			height = gHeight = image.getHeight()*width/image.getWidth();
		} else if (  gHeight!=0 ) {
			height =  gHeight;
			width = gWidth = image.getWidth()*height/image.getHeight();
		}

		width += 2*xBorder;
		height += 2*yBorder;
	}



	@Override
	public void render( RenderContext ctx ) throws Exception {
		if (image == null)
			return;

		PDPageContentStream contentStream = ctx.contentStream;

		float x = ctx.xPos + xBorder;
		float y = ctx.yPos - yBorder - gHeight;
		CheatsheetFormatter.info( "image " + name + " at: " + x+"/"+y + " size: " + gWidth + "/" + gHeight );
		contentStream.drawImage(image, x, y, gWidth, gHeight );

		ctx.yPos -= height;
		ctx.xPos += width;
	}
}
