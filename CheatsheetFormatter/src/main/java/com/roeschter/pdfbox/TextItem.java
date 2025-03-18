package com.roeschter.pdfbox;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class TextItem extends Text {

	TextFormatted bullet = null;
	float bulletSpacing = 0;
	float bulletWidth;
	float inset;
	float spacing;

	TextParagraph paragraph;


	public void layoutBullet() {
		if ( bullet != null) {
			bullet.layout();
			bulletWidth = 2*bulletSpacing + bullet.width;
		}
	}

	@Override
	public void layout() {
		if ( bullet != null)
			bullet.layout();
		paragraph.layout();
		width = inset + bulletWidth + paragraph.width;
		height = paragraph.height + spacing;
	}

	@Override
	public void setWidth(float _width) {
		paragraph.setWidth(_width - inset - bulletWidth);
	}

	@Override
	public void render( RenderContext ctx ) throws Exception {
		float yPos = ctx.yPos;
		ctx.xPos += inset;
		ctx.xPos += bulletSpacing;

		if ( bullet != null)
			bullet.render(ctx);

		ctx.xPos += bulletSpacing;

		paragraph.render(ctx);
		ctx.yPos -= spacing;
	}
}
