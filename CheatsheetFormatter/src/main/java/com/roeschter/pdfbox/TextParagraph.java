package com.roeschter.pdfbox;

import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class TextParagraph extends Text {
	ArrayList<TextLine> texts = new ArrayList<TextLine>();
	TextLine line;

	float maxWidth;
	float spacing;

	public TextParagraph( TextLine _line ) {
		line = _line;
	}

	public TextParagraph( String text, FontContext ctx, float _maxWidth, float lineSpacing, float _paragraphSpacing) {
		this( new TextLine( text, ctx, lineSpacing ) );
		maxWidth = _maxWidth;
		spacing = _paragraphSpacing;
	}

	@Override
	public void layout() {
		if ( width != 0)
			return;

		if ( line != null ) {
			do {
				TextLine nline = line.takeSubTextline(maxWidth) ;
				texts.add(nline);
			} while (line.texts.size() > 0);
			line = null;
		}
		width = 0;
		height = 0;
		for( TextLine text: texts ) {
			text.layout();
			width = Math.max(width, text.width);
			height += text.height;
		}
		height += spacing;
	}

	@Override
	public void render( RenderContext ctx ) throws Exception {
		float xPos = ctx.xPos;
		for( TextLine text: texts ) {
			text.render(ctx);
			//Reset horizontal after line
			ctx.xPos = xPos;
		}
		ctx.yPos -= spacing;
	}
}
