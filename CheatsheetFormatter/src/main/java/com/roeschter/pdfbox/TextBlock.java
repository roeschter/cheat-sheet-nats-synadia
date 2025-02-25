package com.roeschter.pdfbox;

import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class TextBlock extends Text {
	public ArrayList<Text> texts = new ArrayList<Text>();

	public boolean renderVertical = true;
	public boolean alignCenter = false;

	public float spacing = 0;

	public TextBlock()
	{
		this(true, 0 );
	}

	public TextBlock( boolean _renderVertical, float _spacing )
	{
		spacing = _spacing;
		renderVertical = _renderVertical;
	}


	public void add( Text text ) {
		texts.add(text);
	}

	@Override
	public void layout() {
		width = 0;
		height = -spacing;
		for( Text text: texts ) {
			text.layout();
			if ( renderVertical ) {
				width = Math.max(width, text.width);
				height += text.height;
				height += spacing;
			} else {
				height = Math.max(height, text.height);
				width += text.width;
				width += spacing;
			}

		}
	}

	@Override
	public void setWidth(float _width) {
		for( Text text: texts ) {
			text.setWidth(_width);
		}
	}

	@Override
	public void render( RenderContext ctx ) throws Exception {

		float xPos = ctx.xPos;
		float yPos = ctx.yPos;
		for( Text text: texts ) {

			if ( renderVertical ) {
				//Reset horizontally
				ctx.xPos = xPos;
				if ( alignCenter )
					ctx.xPos += (width-text.width)/2;
			} else {
				//Reset vertically
				ctx.yPos = yPos;
				if ( alignCenter )
					ctx.yPos -= (height-text.height)/2;
			}
			text.render(ctx);
			ctx.yPos -= spacing;
		}
		ctx.yPos += spacing;
		ctx.yPos -= ctx.blockSpacing;
	}



}



