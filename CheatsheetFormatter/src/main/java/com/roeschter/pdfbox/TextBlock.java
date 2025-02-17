package com.roeschter.pdfbox;

import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class TextBlock extends Text {
	public ArrayList<Text> texts = new ArrayList<Text>();

	float spacing = 0;
	public void add( Text text ) {
		texts.add(text);
	}

	@Override
	public void layout() {
		width = 0;
		height = -spacing;
		for( Text text: texts ) {
			text.layout();
			width = Math.max(width, text.width);
			height += text.height;
			height += spacing;
		}
	}

	@Override
	public void render( RenderContext ctx ) throws Exception {
		//Render vertically
		float xPos = ctx.xPos;
		for( Text text: texts ) {
			//Reset horizontally before each text
			ctx.xPos = xPos;
			text.render(ctx);
			ctx.yPos -= spacing;
		}
		ctx.yPos += spacing;
	}

}



