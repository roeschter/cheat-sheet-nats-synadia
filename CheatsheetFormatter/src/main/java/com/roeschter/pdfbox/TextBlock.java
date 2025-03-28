package com.roeschter.pdfbox;

import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class TextBlock extends Text {

	static Padding NoPadding = new Padding();

	public ArrayList<Text> texts = new ArrayList<Text>();

	public boolean renderVertical = true;
	public boolean alignCenter = false;

    public boolean fixed = false;
    public float xPos = 0;
    public float yPos = 0;
    public int page = 0;

    public Padding padding = NoPadding;
    public float spacing;

	public TextBlock()
	{
		this(true);
	}

	public TextBlock( boolean _renderVertical )
	{
		renderVertical = _renderVertical;
	}


	public void add( Text text ) {
        if ( text != null )
            texts.add(text);
	}

	@Override
	public void layout() {
		width = 0;
		height = 0;
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
		width += padding.left + padding.right;
		height += padding.top + padding.bottom;
		if (texts.size() != 0) {
			if ( renderVertical )
				height -= spacing;
			else
				width -= spacing;
		}

	}

	@Override
	public void setWidth(float _width) {
		for( Text text: texts ) {
			text.setWidth(_width - padding.left - padding.right);
		}
	}

	@Override
	public void render( RenderContext ctx ) throws Exception {

        if ( fixed) {
        	ctx.xPos = xPos;
            ctx.yPos = yPos;
        }
        ctx.yPos -= padding.top;
        ctx.xPos += padding.left;

        float _xPos = ctx.xPos;
        float _yPos = ctx.yPos;

		for( Text text: texts ) {

			if ( renderVertical ) {
				//Reset horizontally
				ctx.xPos = _xPos;
				if ( alignCenter )
					ctx.xPos += (width-text.width)/2;
			} else {
				//Reset vertically
				ctx.yPos = _yPos;
				if ( alignCenter )
					ctx.yPos -= (height-text.height)/2;
			}
			text.render(ctx);
			ctx.yPos -= spacing;
		}

		if (texts.size() != 0) {
			if ( renderVertical )
				ctx.yPos += spacing;
			else
				ctx.xPos += spacing;
		}

		ctx.yPos -= padding.bottom;
        ctx.xPos += padding.right;

	}



}



