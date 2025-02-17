package com.roeschter.pdfbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class CheatsheetFormatter {

	JSONObject style;
	JSONObject content;
	String output;

	RenderContext ctx;

	public CheatsheetFormatter( String _style, String _content )
	{
		String currentFile = null;
		try {
			currentFile = _style;
			style = new JSONObject( Files.readString(Path.of(currentFile)) );
			currentFile = _content;
			content = new JSONObject( Files.readString(Path.of(currentFile)) );
		} catch (Exception e) {
			System.out.println( "Error Parsing: " + currentFile );
			e.printStackTrace();
			System.exit(1);
		}

		output = _content + ".pdf";
	}

	public String get( JSONObject json, String key, String _default) {
		if ( json.isNull(key))
			return _default;
		return json.getString(key);
	}

	public float get( JSONObject json, String key, double _default) {
		if ( json.isNull(key))
			return (float)_default;
		return (float)json.getDouble(key);
	}

	public int get( JSONObject json, String key, int _default) {
		if ( json.isNull(key))
			return _default;
		return (int)json.getLong(key);
	}

	public static float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
	    float widthInUnits = font.getStringWidth(text);
	    return (widthInUnits / 1000) * fontSize; // Convert to user space
	}

	public TextItem makeItem( String text, float inset, float paragraphWidth) {
		TextItem item = new TextItem();
		float scale = (float)1.3;
		item.bullet = new TextFormatted( "\u2022" , ctx.body.bold, ctx.body.size*scale);
		item.bullet.yOffset = -ctx.body.size*(scale-1)*(float)0.5;
		item.bulletSpacing = 3;
		item.spacing = ctx.body.size * (float)0.2;
		item.inset = inset;
		item.layoutBullet();

		item.paragraph = new TextParagraph( text, ctx.body, paragraphWidth - item.bulletWidth, ctx.body.size*ctx.lineSpacing );
		item.layout();
		return item;
	}

	public void addItems(TextBlock block, JSONArray items, float inset, float paragraphWidth  ) {
		float bulletWidth = 0;
		for ( Object _item: items) {
			if (_item instanceof String ) {
				String text = (String)_item;
				TextItem item = makeItem(text, inset, paragraphWidth);
				bulletWidth = item.bulletWidth;
				block.add(item);
			}

			//A nested list of items, adding recursively with indentation
			if (_item instanceof JSONObject ) {
				//Iterate Items
				JSONObject itemList = (JSONObject)_item;
				JSONArray _items = itemList.getJSONArray("items");
				addItems( block, _items, inset + bulletWidth, paragraphWidth - bulletWidth );
			}
		}
	}

	public TextBlock parseBlock( Object _json, RenderContext ctx ) {
		TextBlock block = new TextBlock();
		JSONObject json = (JSONObject)_json;

		String title = get(json, "title", null );
		if ( title != null) {
			 block.add( new TextParagraph( title, ctx.header, ctx.lanewidth,  ctx.header.size*(float)0.1  ) );

			 TextGraphics underline = new TextGraphics( "underline.png", ctx.document );
			 underline.yBorder =  ctx.header.size * (float)0.1;
			 underline.gWidth = ctx.lanewidth;
			 underline.gHeight = ctx.header.size * (float)0.2;

			 block.add(underline);
		}

		//Iterate Items
		JSONArray items = json.getJSONArray("items");
		addItems( block, items, 0, ctx.lanewidth );

		return block;
	}

	public void format() throws Exception {

		//Define render context from style
		ctx = new RenderContext();

		ctx.document = new PDDocument();
		PDPage page;
		PDRectangle r  = PDRectangle.A4;
		if ( get(style, "orientation", "potrait" ).equals("landscape")) {
			r = new PDRectangle( PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth() ) ;
		}
		page = new PDPage( r);

		ctx.document.addPage(page);

		ctx.contentStream = new PDPageContentStream(ctx.document, page);

		ctx.titleFontSize = get(style,"titleFontSize",24);
		ctx.headerFontSize = get(style,"headerFontSize",12);
		ctx.bodyFontSize = get(style,"bodyFontSize",8);

		ctx.borderTop = get(style,"bordertop",0.0);
		ctx.borderbottom = get(style,"borderbottom",0.0);
		ctx.borderleft = get(style,"borderleft",0.0);
		ctx.borderright = get(style,"borderright",0.0);
		ctx.titleheight = get(style,"titleheight",0.0);

		ctx.viewHeight = r.getHeight() - ctx.borderTop - ctx.borderbottom - ctx.titleheight;
		ctx.viewWidth = r.getWidth() - ctx.borderleft - ctx.borderright;

		ctx.lanes = get( style, "lanes", 1 );
		ctx.laneborder = get(style,"laneborder",0.0);
		ctx.lanewidth = (ctx.viewWidth - (ctx.lanes-1)*ctx.laneborder) /ctx.lanes;
		ctx.laneHeight = ctx.viewHeight;

		ctx.makeFonts();

		//General TODO
		/*
		 */

		//Get Header
		//TODO

		//TextBlock of TextBlock
		TextBlock textBlocks = new TextBlock();
		textBlocks.spacing = ctx.blockSpacing;

		//Iterate over content blocks
		JSONArray blocks = content.getJSONArray("blocks");
		for ( Object item: blocks )
		{
			textBlocks.add( parseBlock( item, ctx ) );
		}
		textBlocks.layout();

		//Layout block into lanes
		TextBlock[] lanes = new TextBlock[ctx.lanes];
		for ( int i=0; i<ctx.lanes; i++) {
			lanes[i] = new TextBlock();
		}

		//Distribute blocks over lanes
		int lanePos = 0;
		float laneHeightRemaining = ctx.laneHeight;
		for ( Text block: textBlocks.texts )
		{
			if ( block.height > laneHeightRemaining ) {
				laneHeightRemaining = ctx.laneHeight;
				lanePos = (lanePos+1)%lanes.length;
			}
			lanes[lanePos].add(block);
			laneHeightRemaining -= block.height;
		}

		//Render lanes
		for ( int i=0; i<ctx.lanes; i++) {
			ctx.xPos = ctx.borderleft + (ctx.lanewidth + ctx.laneborder) * i;
			ctx.yPos = r.getHeight() - ctx.borderTop - ctx.titleheight;
			lanes[i].render(ctx);
		}

		ctx.contentStream.close();

		ctx.document.save(output);
		ctx.document.close();
		System.out.println("Done writing to: " + output);
	}


	public static void main(String[] arg) throws Exception {
		int i=0;

		String _style = null;
		String _content = null;
		String output = null;


		while ( arg.length > i)
		{
			if ( arg[i].equals("-style") || arg[i].equals("-s"))
			{
				i++;
				_style = arg[i];
				i++;
			} else if ( arg[i].equals("-content") || arg[i].equals("-c"))
			{
				i++;
				_content = arg[i];
				i++;
			} else if ( arg[i].equals("-output") || arg[i].equals("-o"))
			{
				i++;
				output = arg[i];
				i++;
			}  else if (arg[i].equals("--")) { //Terminate parameter processing
				i = arg.length;
			}  else {
				System.out.println("Unknown command line parameter: " + arg[i]);
				System.exit(1);
			}
		}

		if ( _style == null || _content == null) {
			System.out.println("Options: ");
			System.out.println("-style  <json style template>");
			System.out.println("-content <json content template>");
			System.exit(1);
		}


		CheatsheetFormatter csf = new CheatsheetFormatter(_style, _content);
		if ( output != null)
			csf.output = output;

		csf.format();

		System.out.println("Exiting");

	}

}
