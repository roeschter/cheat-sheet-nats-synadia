package com.roeschter.pdfbox;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.json.JSONArray;
import org.json.JSONObject;


public class CheatsheetFormatter {

	/*

	 */

	Config cStyle;
	Config content;
	String output;

	RenderContext ctx;
	String rootDir = ".";

	public CheatsheetFormatter( String _style, String _content )
	{
		String contentFile = null;
		String styleFile = null;


		contentFile = _content;
		rootDir = new File(contentFile).getAbsoluteFile().getParent();
		contentFile = new File(contentFile).getName();

		content = new Config( rootDir, contentFile );

		File f = new File( "text.html");
		System.out.println(f.getAbsolutePath());

		styleFile = content.get("style", _style);
		JSONObject override;

		//Is there a content override section?
		if ( !content.isNull("styleoverride") )
			override = content.getJSONObject("styleoverride");
		else
			override = new JSONObject();

		Config root = new Config( rootDir, styleFile );
		cStyle = new Config( override, root);

		output = rootDir + File.separatorChar + contentFile + ".pdf";
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
			item.bullet = new TextFormatted( ctx.bullet , ctx.body.prop.bold, ctx.body.prop.size*scale, ctx.body.prop.color);
			item.bullet.yOffset = ctx.body.prop.size*ctx.bulletOffetRel;
			item.bulletSpacing = ctx.body.prop.size*ctx.bulletSpacingRel;
		}
		item.inset = inset;
		item.layoutBullet();

		item.paragraph = new TextParagraph( text, ctx.body, paragraphWidth - item.bulletWidth, ctx.body.prop.size*ctx.bodyLineSpacingRel,  ctx.body.prop.size*ctx.bodyParagraphSpacingRel );
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
				float _inset = hasBullets?bulletWidth:(ctx.body.prop.size*ctx.blockIndentationRel);
				addItems( block, item, new Config( item, config), _filter, inset + _inset, paragraphWidth - bulletWidth );
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
			 block.add( new TextParagraph( title, ctx.title, ctx.lanewidth, ctx.title.prop.size*ctx.titleLineSpacingRel, ctx.title.prop.size*ctx.titleParagraphSpacingRel  ) );

			 //Add underline graphics
			 TextGraphics underline = new TextGraphics( ctx.underLineLogo, ctx.document );
			 underline.yBorder =  ctx.title.prop.size * ctx.underlineBorderRel;
			 underline.gWidth = ctx.lanewidth;
			 underline.gHeight = ctx.title.prop.size * ctx.underlineHeightRel;
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

        float lineSpacing = ctx.body.prop.size*(float)0.2;
        float paragraphSpacing = ctx.body.prop.size*(float)0.2;

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
		//The lanes may span multiple pages
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

			info( "Lane space: " + laneHeightRemaining + " needed: "  + block.height );
			if ( block.height > laneHeightRemaining ) {
				currentLane = null;
			}
			if ( currentLane==null ) {
                laneCount++;
				currentLane = new TextBlock();
				currentLane.spacing = ctx.blockSpacing;

				lanewidth = ctx.getLaneWidthForLane(laneCount);

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

		System.out.println("Lanes needed: " + (laneCount+1));

		//Done with layout, now render lanes onto pages

		laneCount = 0;
		int pageCount = (page!=null)?0:-1;  //We have a page alreay
		while( lanes.size() > 0) {
			if (page == null) {
				page = ctx.addPAge();
				pageCount++;
			}


			//Render background
			TextGraphics backgroundTL = new TextGraphics(ctx.backgroundTLLogo, ctx.document);
			backgroundTL.gWidth = ctx.backgroundTLWidth;
			backgroundTL.gHeight = ctx.backgroundTLHeight;

			TextGraphics backgroundTR = new TextGraphics(ctx.backgroundTRLogo, ctx.document);
			backgroundTR.gWidth = ctx.backgroundTRWidth;
			backgroundTR.gHeight = ctx.backgroundTRHeight;

			TextGraphics backgroundBR = new TextGraphics(ctx.backgroundBRLogo, ctx.document);
			backgroundBR.gWidth = ctx.backgroundBRWidth;
			backgroundBR.gHeight = ctx.backgroundBRHeight;

			backgroundTL.layout();
			backgroundTR.layout();
			backgroundBR.layout();

			ctx.xPos = 0;
			ctx.yPos = ctx.rectangle.getHeight() ;
			backgroundTL.render(ctx);

			ctx.xPos = ctx.rectangle.getWidth()-backgroundTR.width;
			ctx.yPos = ctx.rectangle.getHeight() ;
			backgroundTR.render(ctx);

			ctx.xPos = ctx.rectangle.getWidth()-backgroundBR.width;
			ctx.yPos = backgroundBR.height;
			backgroundBR.render(ctx);


			//Render header
			TextBlock header = new TextBlock(false);
			header.alignCenter = true;
			//Logo
			if ( ctx.headerLogo.length() > 0) {
				TextGraphics headerLogo = new TextGraphics(ctx.headerLogo, ctx.document);
				headerLogo.gHeight = ctx.headerLogoHeight;
				headerLogo.yBorder = (ctx.headerHeight-ctx.headerLogoHeight)/2;
				headerLogo.xBorder = 5;
				header.add( headerLogo );
			}
			//Header Text
			TextFormatted headerText = new TextFormatted( content.get( "pageHeader", ""), ctx.header.prop.bold, ctx.header.prop.size, ctx.header.prop.color );
			headerText.yOffset = ctx.headerYOffset;
			header.add( headerText );
			header.layout();

			ctx.yPos = ctx.rectangle.getHeight() - ctx.borderTop;
			ctx.xPos = (ctx.rectangle.getWidth() - header.width)/2;
			header.render(ctx);

			//footer
			TextBlock footer = new TextBlock();
			String _footerText =  content.get( "pageFooter", "");
			if (ctx.footerDate) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
				_footerText += " - " + sdf.format(new Date());
			}
			//TextFormatted footerText = new TextFormatted( _footerText , ctx.footer.regular.regular, ctx.footer.regular.size, ctx.footer.regular.color );
			TextParagraph footerText = new TextParagraph( _footerText, ctx.footer, ctx.footerWidth, ctx.footerLineSpacingRel, 0 );
			footer.padding = ctx.footerPadding;
			footer.add(footerText);
			footer.layout();

			//Align left bottom
			ctx.yPos = footer.height;
			ctx.xPos = 0;
			footer.render(ctx);



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

			//Render warning if present
			String warning = content.get( "warning", null);
			if ( warning != null ) {
				float fontSize = ctx.header.prop.size;
				TextFormatted warningText = new TextFormatted(  warning, ctx.header.prop.bold, fontSize, Color.gray );
				warningText.setAlpha((float) 0.5);
				warningText.setRotation(-30);
				warningText.layout();
				float w = ctx.rectangle.getWidth();
				warningText.fontSize = (float) (fontSize / warningText.width * w * 0.8);
				warningText.layout();
				ctx.xPos = (float) (ctx.rectangle.getWidth()*0.1);
				ctx.yPos = (float) (ctx.rectangle.getHeight()*0.9);

				warningText.render(ctx);;
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
			} else if (!arg[i].startsWith("-") )
			{
				_content = arg[i];
				i++;
			} else if (arg[i].equals("--")) { //Terminate parameter processing
				i = arg.length;
			}  else {
				System.out.println("Unknown command line parameter: " + arg[i]);
				System.exit(1);
			}
		}

		if ( _content == null) {
			System.out.println("Options: ");
			System.out.println("<json content template>");
			System.exit(1);
		}


		CheatsheetFormatter csf = new CheatsheetFormatter(_style, _content);
		if ( output != null)
		{
			csf.output = output;
		}

		csf.format();

		String pdfViewerCommand = System.getenv("PDF_VIEWER");

		if ( pdfViewerCommand != null )
			view = true;
		else
			view = false;

		if ( view ) {
			info("PDF_VIEWER: " + pdfViewerCommand );
			String finalCommand = pdfViewerCommand.replace("%f", csf.output);
			info( finalCommand );
			ProcessBuilder processBuilder = new ProcessBuilder(
					finalCommand.split(" ")
		        );


		    Process process = processBuilder.start();
		}


		System.out.println("Exiting");

	}

}
