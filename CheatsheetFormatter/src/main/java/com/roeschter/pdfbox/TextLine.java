package com.roeschter.pdfbox;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class TextLine extends Text {

	ArrayList<TextFormatted> texts = new ArrayList<TextFormatted>();
	public float spacing;

	public TextLine() {
	}

	@Override
	public TextLine clone() {
		TextLine line = new TextLine();
		line.spacing = spacing;
		for ( TextFormatted text: texts ) {
			line.add(text);
		}
		return line;
	}

	char[] markup = {'*', '\\', '`'};

	int countChar( String s, int pos, char c) {
		int count = 0;
		while ( pos<s.length() ) {
			if ( s.charAt(pos)==c) {
				count++;
				pos++;
			} else {
				return count;
			}
		}
		return count;
	}

	static int NOMARKUP = 0;
	static int INMARKUP = 1;

	public boolean isMarkup( char cc) {
		for ( char c: markup) {
			if ( cc==c ) {
				return true;
			}
		}
		return false;
	}

	public void parseMarkDown( String text, FontContext ctx) {
		StringBuilder b = new StringBuilder();
		int lpos = 0;
		int spos = -1;
		char markchar = ' ';
		int state = NOMARKUP;
		int markupcount = 0;
		Font currentFont = ctx.regular;

		do {
			int pos = lpos;
			spos = -1;
			//Search for a markup character
			while (pos < text.length() && spos == -1) {
				markchar = text.charAt(pos);
				for ( char c: markup) {
					if ( c==markchar ) {
						spos = pos;
					}
				}
				pos++;
			}
			 //Found markup
			if ( spos!=-1) {
				//Grab text up to markup into buffer
				b.append( text.subSequence(lpos, spos ));
				lpos = spos;
				//Its an escape
				if ( markchar=='\\') {
					//Check if there is another character and if its markup
					if ( spos+1<text.length() ) {
						char c = text.charAt(spos+1);
						if ( isMarkup(c)) {
							b.append(c);
							lpos = spos+2;
						}
					//Otherwise append the escape char
					} else {
						b.append('\\');
						lpos = spos+1;
					}
				}
				//Its asterix - bold/cursive marking
				if ( markchar=='*') {
					markupcount = countChar(text,spos,markchar);
					if ( state==NOMARKUP) {
						//There was no markup so add the buffered text as regular
						if ( b.length() != 0)
							texts.add(new TextFormatted(b.toString(), currentFont.regular, currentFont.size, currentFont.color));
						b.setLength(0);
						lpos += markupcount;
						state = INMARKUP;
					} else {
						PDFont font = null;
						if ( markupcount==1 ) {
							font=currentFont.cursive;
						} else if ( markupcount==2 ) {
							font=currentFont.bold;
						} else  {
							font=currentFont.cursiveBold;
						}
						//Adding the text
						if ( b.length() != 0)
							texts.add(new TextFormatted(b.toString(), font, currentFont.size, currentFont.color));
						b.setLength(0);
						lpos += markupcount;
						state = NOMARKUP;
					}
				}
				//Its an apostrophe
				if ( markchar=='`') {
					markupcount = countChar(text,spos,markchar);
					//Current font is body - start fixed
					if ( currentFont == ctx.regular ) {
						currentFont = ctx.fixed;
					//End of fixed font markup
					} else {
						//Adding text
						if ( b.length() != 0)
							texts.add(new TextFormatted(b.toString(), currentFont.regular, currentFont.size, currentFont.color));
						b.setLength(0);
						currentFont = ctx.regular;
					}
					lpos += markupcount;
				}
			//No markup found - Handle rest of the string
			} else {
				b.append( text.subSequence(lpos, text.length()));
				lpos = text.length();
				if (b.length() != 0)
				texts.add(new TextFormatted(b.toString(), currentFont.regular, currentFont.size, currentFont.color));
			}
		} while ( spos!=-1 );
	}

	public TextLine( String text, FontContext ctx, float _spacing ) {
		spacing = _spacing;
		parseMarkDown( text, ctx);
	}


	char[] delim = {' ', ',', '*', '\\', '/', '-'};
	public TextLine takeSubTextline( float width )  {
		layout();
		TextLine subLine = new TextLine();
		subLine.spacing = spacing;
		float widthRemaining = width;

		boolean canFit = true;
		while ( canFit && texts.size() >0 && widthRemaining>0) {
			canFit = false;
			TextFormatted text = texts.get(0);
			if ( text.width <= widthRemaining )
			{
				widthRemaining -= text.width;
				subLine.texts.add(text);
				texts.remove(0);
				canFit = true;
			} else {
				//Search for a delim character
				int pos = 0;
				int spos = -1;
				char delimchar;
				String s = text.text;
				while (pos < s.length() && spos == -1) {
					delimchar = s.charAt(pos);
					for ( char c: delim) {
						if ( c==delimchar ) {
							spos = pos;
						}
					}
					pos++;
				}
				// We found no delims
				if ( spos == -1) {
					if ( subLine.texts.size() != 0 ) { //Did we have some content already? Then we are done
						widthRemaining = -1;
					} else { //Insert the whole text even if it does not fit
						widthRemaining -= text.width;
						subLine.texts.add(text);
						texts.remove(0);
						canFit = true;
					}

				} else { //OK we have a token
					//Take the token and check if it fits
					String token = s.substring(0,spos+1);
					TextFormatted ntext = text.clone();
					ntext.text = token;
					ntext.layout();
					if ( ntext.width <= widthRemaining ) //It fits insert it
					{
						widthRemaining -= ntext.width;
						subLine.texts.add(ntext);
						canFit = true;
						texts.remove(0);
						//Reinsert the rest if not an empty string
						String rest = s.substring(spos+1);
						if (rest.length() > 0) {
							TextFormatted rtext = text.clone();
							rtext.text = rest;
							rtext.layout();
							texts.add(0,rtext);
						}
					} else { //Does not fit
						if ( subLine.texts.size() != 0 ) { //Did we have some content already? Then we are done
							widthRemaining = -1;
						} else { //Insert the whole token even if it does not fit
							widthRemaining -= ntext.width;
							subLine.texts.add(ntext);
							texts.remove(0);
							canFit = true;
						}
					}
				}
			}

		} while ( canFit && texts.size() >0 && widthRemaining>0);

		subLine.layout();
		return subLine;
	}

	public void add( TextFormatted text ) {
		texts.add(text);
	}

	@Override
	public void layout() {
		width = 0;
		height = 0;
		for( TextFormatted text: texts ) {
			text.layout();
			width += text.width;
			height = Math.max(height, text.height);
		}
		height += spacing;
	}

	@Override
	public void setWidth(float _width) {
		//Ignore
	}


	@Override
	public void render( RenderContext ctx ) throws Exception {
		for( TextFormatted text: texts ) {
			text.render(ctx);
		}
		ctx.yPos -= height;
	}
}
