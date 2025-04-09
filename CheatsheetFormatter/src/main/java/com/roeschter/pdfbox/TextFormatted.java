package com.roeschter.pdfbox;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import static com.roeschter.pdfbox.CheatsheetFormatter.*;


public class TextFormatted extends Text {

	PDFont font;
	float fontSize;
	Color color;
	public String text;
	public float yOffset;

	float rotation = (float)0.0;
    float alpha = 0;

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

	public void setRotation( float _rotation ) {
		rotation = (float) Math.toRadians(_rotation);
	}

	public void setAlpha( float _alpha ) {
		alpha = _alpha;
	}


	@Override
	public void layout() {
		try {
			height = fontSize;
			width = getTextWidth( text, font, fontSize);

			if ( alpha != 0 ) {
				float h = height;
				float w = width;
				width = (float) (w*Math.abs(Math.cos(rotation) ) + h*Math.abs(Math.sin(rotation) )) ;
				height = (float) (w*Math.abs(Math.sin(rotation) ) + h*Math.abs(Math.cos(rotation) )) ;
			}
		} catch (Exception e ) {
			e.printStackTrace();
		}

	}

	public static Matrix createRotationMatrix(float radians, float pivotX, float pivotY) {
	    // Create a translation matrix to move to origin
	    Matrix translateToOrigin = Matrix.getTranslateInstance(-pivotX, -pivotY);

	    // Create rotation matrix
	    Matrix rotation = Matrix.getRotateInstance(radians, 0, 0);

	    // Create translation matrix to move back
	    Matrix translateBack = Matrix.getTranslateInstance(pivotX, pivotY);

	    // Combine the matrices: translate to origin, rotate, translate back
	    // Matrix multiplication order is important - it's applied in reverse order
	    return rotation.multiply(translateBack);
	}

	@Override
	public void render( RenderContext ctx ) throws Exception {
		PDPageContentStream contentStream = ctx.contentStream;


		PDExtendedGraphicsState gState = null;
		if ( alpha != 0.0) {
			//contentStream.saveGraphicsState();
			gState = new PDExtendedGraphicsState();
			gState.setNonStrokingAlphaConstant(alpha);
			//gState.setStrokingAlphaConstant(alpha);

		}

		contentStream.beginText();

		contentStream.setNonStrokingColor(color);
		contentStream.setFont( font, fontSize);
		if ( gState != null) {
			contentStream.setGraphicsStateParameters(gState);
		}

		float yPos = ctx.yPos - height - yOffset;
		trace(ctx.xPos, yPos, text );

		if ( rotation != 0.0 ) {
			Matrix m = createRotationMatrix(rotation, ctx.xPos, (float) (ctx.yPos - fontSize*Math.cos(rotation)) );
			contentStream.setTextMatrix(m);
		} else {
			contentStream.newLineAtOffset( ctx.xPos , yPos);
		}

		contentStream.showText(text);
		contentStream.endText();

		if ( gState != null) {
			//contentStream.restoreGraphicsState();
		}
		ctx.xPos += width;
	}
}
