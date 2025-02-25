package com.roeschter.pdfbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;


import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.json.JSONArray;
import org.json.JSONObject;


public class CheatsheetFormatter {

	/*
	 * OK - Config object with inheritance
	 * .Reference style in content
	 * .Override of style in content
	 *
	 *
	 * Lane width adjustment - allow for explicit specification
	 *
	 * Organize content into folder prefixed with cs_
	 * CLI cheatsheet - start with nats cheat
	 *
	 * Create Readme.md
	 *
	 */

	Config cStyle;
	//JSONObject style;
	Config content;
	String output;

	RenderContext ctx;

	public CheatsheetFormatter( String _style, String _content )
	{
		String currentFile = null;
		try {
			currentFile = _content;
			content = new Config( new JSONObject( Files.readString(Path.of(currentFile)) ) , null );

			currentFile = content.get("style", _style);
			JSONObject style = new JSONObject( Files.readString(Path.of(currentFile)) );
			JSONObject override = new JSONObject();

			if ( !content.isNull("styleoverride") )
				override = content.getJSONObject("styleoverride");

			Config root = new Config( style, null );
			cStyle = new Config( override, root);

		} catch (Exception e) {
			System.out.println( "Error Parsing: " + currentFile );
			e.printStackTrace();
			System.exit(1);
		}

		output = _content + ".pdf";
	}



	static boolean trace = false;
	static boolean info = true;

	static public void trace(float x, float y, String text) {
		if (trace)
			System.out.println("("+x+","+y+"):"+text);
	}

	static public void info( String text) {
		if (info)
			System.out.println(text);
	}


	public static float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
	    float widthInUnits = font.getStringWidth(text);
	    return (widthInUnits / 1000) * fontSize; // Convert to user space
	}

	public TextItem makeItem( String text, float inset, float paragraphWidth) {
		TextItem item = new TextItem();
		float scale = ctx.bulletSizeRel;
		item.bullet = new TextFormatted( ctx.bullet , ctx.body.bold, ctx.body.size*scale);
		item.bullet.yOffset = ctx.body.size*ctx.bulletOffetRel;
		item.bulletSpacing = ctx.body.size*ctx.bulletSpacingRel;
		item.inset = inset;
		item.layoutBullet();

		item.paragraph = new TextParagraph( text, ctx.body, paragraphWidth - item.bulletWidth, ctx.body.size*ctx.bodyLineSpacingRel,  ctx.body.size*ctx.bodyParagraphSpacingRel );
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

		String title = content.get(json, "title", null );
		info( title);
		block.name = title;
		if ( title != null) {
			 block.add( new TextParagraph( title, ctx.title, ctx.lanewidth, ctx.title.size*ctx.titleLineSpacingRel, ctx.title.size*ctx.titleParagraphSpacingRel  ) );

			 //Add underline graphics
			 TextGraphics underline = new TextGraphics( "underline.png", ctx.document );
			 underline.yBorder =  ctx.title.size * ctx.underlineBorderRel;
			 underline.gWidth = ctx.lanewidth;
			 underline.gHeight = ctx.title.size * ctx.underlineHeightRel;

			 block.add(underline);
		}

		//Iterate Items
		JSONArray items = json.getJSONArray("items");
		addItems( block, items, 0, ctx.lanewidth );

		return block;
	}

	public void format() throws Exception {

		//Define render context from style
		ctx = new RenderContext( cStyle );
		PDPage page = ctx.addPAge();
		ctx.setupStyle();
		ctx.makeFonts();

		//General TODO
		/*
		    - Insert content
		 */

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

		//We are done preparing the context now layout into lanes

		//Layout block into lanes
		ArrayList<TextBlock> lanes = new ArrayList<TextBlock>();
		TextBlock currentLane = null;;
		float laneHeightRemaining = 0;;
		for ( Text block: textBlocks.texts )
		{
			if ( block.height > laneHeightRemaining ) {
				currentLane = null;
			}
			if ( currentLane==null ) {
				currentLane = new TextBlock();
				lanes.add(currentLane);
				laneHeightRemaining = ctx.laneHeight;
			}

			info( "Adding to lane: " + lanes.size() + " : "  + block.name );
			currentLane.add(block);
			laneHeightRemaining -= block.height;

		}

		////Done with layout, now render lanes onto üages

		while( lanes.size() > 0) {
			if (page == null)
				page = ctx.addPAge();
			//Render header
			TextBlock header = new TextBlock(false, 0);
			header.alignCenter = true;
			TextGraphics headerLogo = new TextGraphics(ctx.headerLogo, ctx.document);
			headerLogo.gHeight = ctx.headerLogoHeight;
			headerLogo.yBorder = (ctx.headerHeight-ctx.headerLogoHeight)/2;
			headerLogo.xBorder = 5;
			TextFormatted headerText = new TextFormatted( content.get( "pageHeader", ""), ctx.header.bold, ctx.header.size );
			headerText.yOffset = ctx.headerYOffset;

			header.add( headerLogo );
			header.add( headerText );

			header.layout();

			ctx.yPos = ctx.rectangle.getHeight() - ctx.borderTop;
			ctx.xPos = (ctx.rectangle.getWidth() - header.width)/2;
			header.render(ctx);

			//Render background
			TextGraphics backgroundTL = new TextGraphics(ctx.backgroundTLLogo, ctx.document);
			TextGraphics backgroundBR = new TextGraphics(ctx.backgroundBRLogo, ctx.document);
			backgroundTL.gWidth = ctx.backgroundTLWidth;
			backgroundTL.gHeight = ctx.backgroundTLHeight;
			backgroundBR.gWidth = ctx.backgroundBRWidth;
			backgroundBR.gHeight = ctx.backgroundBRHeight;

			backgroundTL.layout();
			backgroundBR.layout();

			ctx.xPos = 0;
			ctx.yPos = ctx.rectangle.getHeight() ;
			backgroundTL.render(ctx);

			ctx.xPos = ctx.rectangle.getWidth()-backgroundBR.width;
			ctx.yPos = backgroundBR.height;
			backgroundBR.render(ctx);

			//Render lanes
			for ( int i=0; i<ctx.lanes; i++) {
				ctx.xPos = ctx.borderleft + (ctx.lanewidth + ctx.laneborder) * i;
				ctx.yPos = ctx.laneTop;
				if ( lanes.size() > 0) {
					currentLane = lanes.remove(0);
					currentLane.render(ctx);
				}
			}

			ctx.contentStream.close();
			page = null;
		}

		ctx.document.save(output);
		ctx.document.close();
		System.out.println("Done writing to: " + output);
	}


	public static void main(String[] arg) throws Exception {
		int i=0;

		String _style = null;
		String _content = null;
		String output = null;
		boolean view = false;


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
			} else if ( arg[i].equals("-trace") )
			{
				i++;
				trace = true;
			} else if ( arg[i].equals("-view") || arg[i].equals("-v"))
			{
				i++;
				view = true;
			} else if (arg[i].equals("--")) { //Terminate parameter processing
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

		if (view ) {
			ProcessBuilder processBuilder = new ProcessBuilder(
					"C:\\Program Files\\IrfanView\\i_view64.exe",
					csf.output,
					"/fs",
					"/one"
		        );
		        Process process = processBuilder.start();
		}


		System.out.println("Exiting");

	}

}
