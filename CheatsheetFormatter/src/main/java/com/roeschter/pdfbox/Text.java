package com.roeschter.pdfbox;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

public abstract class Text {

	public String name;  //Optional for debugging
	public float width = 0;
	public float height = 0;


	public static float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
        float widthInUnits = font.getStringWidth(text);
        return (widthInUnits / 1000) * fontSize; // Convert to user space
	}

	abstract public void render( RenderContext ctx ) throws Exception;
	abstract public void layout();

	//Should only be respected by elements which can adjust their width
	abstract public void setWidth( float _width);

}
