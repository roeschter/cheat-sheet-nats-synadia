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
	 + Arial?
	 * Spacin?
	 *
	 * ReTest nats cheatsheet
	 *
	 * Test CLI cheatsheet (courier)
	 */

	Config cStyle;
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
			JSONObject override;

			if ( !content.isNull("styleoverride") )
				override = content.getJSONObject("styleoverride");
			else
				override = new JSONObject();

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

	public TextItem makeItem( String text, float inset, float paragraphWidth, boolean hasBUllets) {
		TextItem item = new TextItem();
		float scale = ctx.bulletSizeRel;

		if ( hasBUllets ) {
			item.bullet = new TextFormatted( ctx.bullet , ctx.body.regular.bold, ctx.body.regular.size*scale, ctx.body.regular.color);
			item.bullet.yOffset = ctx.body.regular.size*ctx.bulletOffetRel;
			item.bulletSpacing = ctx.body.regular.size*ctx.bulletSpacingRel;
		}
		item.inset = inset;
		item.layoutBullet();

		item.paragraph = new TextParagraph( text, ctx.body, paragraphWidth - item.bulletWidth, ctx.body.regular.size*ctx.bodyLineSpacingRel,  ctx.body.regular.size*ctx.bodyParagraphSpacingRel );
		item.layout();
		return item;
	}

	public void addItems(TextBlock block, JSONObject json, Config config , int[] filter, float inset, float paragraphWidth  ) {
		float bulletWidth = 0;
		int count = -1;
		JSONArray items = json.getJSONArray("items");
		boolean hasBullets = config.getBool("bullets", true );

		for ( Object _item: items) {
			count++;
			if ( ctx.isFiltered(filter, count))
				continue;

			if (_item instanceof String ) {
				String text = (String)_item;
				TextItem item = makeItem(text, inset, paragraphWidth, hasBullets);

				bulletWidth = item.bulletWidth;
				block.add(item);
			}

			//A nested list of items, adding recursively with indentation
			if (_item instanceof JSONObject ) {
				//Iterate Items
				JSONObject item = (JSONObject)_item;
				int[] _filter = ctx.getFilter( item );
				addItems( block, item, new Config( item, config), _filter, inset + bulletWidth, paragraphWidth - bulletWidth );
			}

		}
	}

	public TextBlock parseBlock( JSONObject json, RenderContext ctx ) {
		TextBlock block = new TextBlock();
        int[] filter = ctx.getFilter( json );

        if ( ctx.isFiltered(filter,-1) )
            return null;


        //Add Title and underline unless empty
		String title = new Config(json).get( "title", null );
		info( title);
		block.name = title;
		if ( title != null && title.length()>0) {
			 block.add( new TextParagraph( title, ctx.title, ctx.lanewidth, ctx.title.regular.size*ctx.titleLineSpacingRel, ctx.title.regular.size*ctx.titleParagraphSpacingRel  ) );

			 //Add underline graphics
			 TextGraphics underline = new TextGraphics( "underline.png", ctx.document );
			 underline.yBorder =  ctx.title.regular.size * ctx.underlineBorderRel;
			 underline.gWidth = ctx.lanewidth;
			 underline.gHeight = ctx.title.regular.size * ctx.underlineHeightRel;
			 underline.fixedHeight = true;

			 block.add(underline);
		}

		//Iterate Items
		addItems( block, json, new Config(json, cStyle), filter, 0, ctx.lanewidth );

		return block;
	}


    public TextBlock parseImage( JSONObject _json, RenderContext ctx ) {

    	Config image = new Config(_json);
        TextBlock block = new TextBlock();

        int[] filter = ctx.getFilter( _json );
        if ( ctx.isFiltered(filter,-1) )
            return null;

        String title = image.get( "title", null );
        String imageName = image.get("image", null );

        block.padding = new Padding(image);

        float lineSpacing = ctx.body.regular.size*(float)0.2;
        float paragraphSpacing = ctx.body.regular.size*(float)0.2;

        //TODO set padding on block

        if (title != null && title.length()>0 ) {
        	block.add(new TextParagraph( title, ctx.body, 100, lineSpacing, paragraphSpacing ) );
        	block.name = title;
        } else {
        	block.name = imageName;
        }

        TextGraphics imageBlock = new TextGraphics( imageName, ctx.document );
        block.add(imageBlock);

        block.layout();

        return block;
    }

    public TextBlock parseImageFixedPos( JSONObject _json, RenderContext ctx ) {

    	Config image = new Config(_json);
        TextBlock block = parseImage( _json, ctx );

        if ( block==null )
            return null;

        int startlane = image.getInt( "startlane", 0 );
        int lanes = image.getInt("lanes", 0 );
        String position = image.get( "position", "top" );

        //Efective width adjust for spacing and scaling down the image
        float rawSpace = ctx.getLaneTotalWidth( startlane, lanes);

        block.setWidth(rawSpace);
        block.layout();

        //Set block position
        block.fixed = true;
        block.page = startlane / ctx.lanes;
        block.xPos = ctx.getLaneXPos(startlane);
        block.yPos = ctx.getLaneYPos(startlane);

        //Adjust ypos and reserve space in lanes
        block.yPos =ctx.getLaneYPos(startlane);
        if (position.equals("bottom")) {
        	block.yPos -= (ctx.laneHeight - block.height);
        	for ( int i=startlane; i<startlane+lanes; i++) {
        		ctx.setLaneReservedBottom(i, block.height );
        	}
        } else {
        	for ( int i=startlane; i<startlane+lanes; i++) {
        		ctx.setLaneReservedTop(i, block.height );
        	}
        }

        return block;
    }

	public void format() throws Exception {

		//Define render context from style
		ctx = new RenderContext( cStyle );
		PDPage page = ctx.addPAge();
		ctx.setupStyle();
		ctx.makeFonts();

		//Test code
		/*
		ctx.setLaneReservedTop(0, 50);
		ctx.setLaneReservedTop(2, 80);
		ctx.setLaneReservedBottom(1, 150);
		 */

		//Iterate over images
        JSONArray images = content.getJSONArray("images", new JSONArray());
        TextBlock imageBlocks = new TextBlock();
		for ( Object item: images )
		{
			imageBlocks.add( parseImageFixedPos( (JSONObject)item, ctx ) );
		}

		//TextBlock of TextBlock
		TextBlock textBlocks = new TextBlock();

		//Iterate over content blocks
		JSONArray blocks = content.getJSONArray("blocks", new JSONArray());
		for ( Object item: blocks )
		{
			textBlocks.add( parseBlock( (JSONObject)item, ctx ) );
		}

		//We are done preparing the context now layout into lanes

		//Layout blocks into lanes
		ArrayList<TextBlock> lanes = new ArrayList<TextBlock>();

		TextBlock currentLane = null;
        int laneCount = -1;
		float laneHeightRemaining = 0;
		float lanewidth = ctx.lanewidth;
		for ( Text block: textBlocks.texts )
		{
			//Do we fit in the old lane?
			block.setWidth(lanewidth);
			block.layout();
			if ( block.height > laneHeightRemaining ) {
				currentLane = null;
			}
			if ( currentLane==null ) {
                laneCount++;
				currentLane = new TextBlock();
				currentLane.spacing = ctx.blockSpacing;

				lanewidth = ctx.getLaneWidthForLane( lanes.size());

				lanes.add(currentLane);
				laneHeightRemaining = ctx.getLaneHeight(laneCount);

				//New lane new layout
				block.setWidth(lanewidth);
				block.layout();
			}

			info( "Adding to lane: " + lanes.size() + " : "  + block.name );
			currentLane.add(block);
			laneHeightRemaining -= block.height + ctx.blockSpacing;

		}

		System.out.println("Lanes: " + (laneCount+1));
		////Done with layout, now render lanes onto pages
		laneCount = 0;
		int pageCount = (page!=null)?0:-1;  //We have a page alreay
		while( lanes.size() > 0) {
			if (page == null) {
				page = ctx.addPAge();
				pageCount++;
			}

			//Render header
			TextBlock header = new TextBlock(false);
			header.alignCenter = true;
			TextGraphics headerLogo = new TextGraphics(ctx.headerLogo, ctx.document);
			headerLogo.gHeight = ctx.headerLogoHeight;
			headerLogo.yBorder = (ctx.headerHeight-ctx.headerLogoHeight)/2;
			headerLogo.xBorder = 5;

			TextFormatted headerText = new TextFormatted( content.get( "pageHeader", ""), ctx.header.regular.bold, ctx.header.regular.size, ctx.header.regular.color );
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

			//Render images
			for ( Text text: imageBlocks.texts) {
				TextBlock block = (TextBlock)text;
				if ( block.page == pageCount) {
					info( "Adding image to page: " + pageCount + " : " + block.name);
					block.render(ctx);
				}
			}

			//Render lanes
			for ( int i=0; i<ctx.lanes; i++) {

				ctx.xPos = ctx.getLaneXPos(laneCount);
				ctx.yPos = ctx.getLaneYPos(laneCount);
				if ( lanes.size() > 0) {
					currentLane = lanes.remove(0);
					currentLane.render(ctx);
				}

				float lw =ctx.getLaneWidthForLane(laneCount);

				laneCount++;
			}

			ctx.contentStream.close();
			page = null;
		}

		ctx.document.save(output);
		ctx.document.close();
		System.out.println("Pages: " + (pageCount+1));
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

		if ( _content == null) {
			System.out.println("Options: ");
			System.out.println("-style  <json style template>");
			System.out.println("-content <json content template>");
			System.exit(1);
		}


		CheatsheetFormatter csf = new CheatsheetFormatter(_style, _content);
		if ( output != null)
			csf.output = output;

		csf.format();

		if ( view ) {
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
