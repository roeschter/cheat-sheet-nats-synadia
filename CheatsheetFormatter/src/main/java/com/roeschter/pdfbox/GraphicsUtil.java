package com.roeschter.pdfbox;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class GraphicsUtil {

	public static RenderedImage makeColorBand( int width, int height, Color[] colors)  {


		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Get graphics context
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        for ( int i=0; i<colors.length-1; i++) {
        	int _width = width / (colors.length-1);

	        // Create gradient paint
	        GradientPaint gradient = new GradientPaint(
	        	_width*i, 0, colors[i],
	            _width*(i+1), 0, colors[i+1]
	        );
	        // Set the gradient paint
	        g2d.setPaint(gradient);

	        // Fill rectangle with gradient
	        g2d.fillRect( _width*i, 0, _width, height);
        }

        // Dispose graphics
        g2d.dispose();

        return image;
	}

	public static RenderedImage makeColorBandWave( int width, int height, float angleleft, float angleright, boolean upper, Color[] colors)  {


		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Get graphics context
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Set white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);


        Rectangle2D rect = new Rectangle2D.Double(0, 0, width, height);
        // Create spline curve that cuts the rectangle
        Path2D spline = new Path2D.Double();
        spline.moveTo(0, height);  // Start at left bottom

        // Add cubic curve that crosses the rectangle
        spline.curveTo(
            width*0.45, height*0.0,  // First control point
            width*0.75, height*1.0,  // Second control point
            width, 0   // End point
        );

        // Create area for rectangle
        Area rectArea = new Area(rect);

        // Create area for everything below the spline
        Path2D half = new Path2D.Double(spline);
        half.lineTo(width, (upper)?0:height);
        half.lineTo(0, (upper)?0:height);
        half.closePath();
        Area area = new Area(half);

        // Intersect to get only the bottom part of rectangle
        //bottomArea.intersect(rectArea);

        for ( int i=0; i<colors.length-1; i++) {
        //for ( int i=1; i<2; i++) {
        	int _width = width / (colors.length-1);

        	float left = _width*i;
        	float right = _width*(i+1);
	        // Create gradient paint
	        GradientPaint gradient = new GradientPaint(
	        	left, 0, colors[i],
	        	right, 0, colors[i+1]
	        );
	        // Set the gradient paint
	        g2d.setPaint(gradient);

	        // Create area for rectangle
	        Area paintArea = new Area( new Rectangle2D.Double(left, 0, (right-left), height) );
	        paintArea.intersect(area);
	        // Fill rectangle with gradient
	        g2d.fill(paintArea);
        }


        // Dispose graphics
        g2d.dispose();

        return image;
	}



	public static void savePNG( RenderedImage image, String outputPath)  {
		 try {
	            // Save as PNG
	            File outputFile = new File(outputPath);
	            ImageIO.write(image, "png", outputFile);
	            System.out.println("Gradient image saved to: " + outputPath);
	        } catch (IOException e) {
	            System.err.println("Error saving image: " + e.getMessage());
	        }
	}


	public static void main(String[] args) {

		//Nats colorss
		Color c1 = new Color(29, 170, 225);
		Color c2 = new Color(52, 165, 116);
		Color c3 = new Color(55, 92, 147);
		Color c4 = new Color(141, 198, 63);
		//Synadia Colors
		Color c5 = new Color(182,94, 255);
		Color c6 = new Color(79, 70, 229);


		Color[] colors = new Color[] { c1, c2, Color.WHITE, Color.WHITE, Color.WHITE}; // ,Color.WHITE};
		RenderedImage image = makeColorBand( 512, 16, colors);
		savePNG( image, "underline.png"  );


		colors = new Color[] { c5, c6, Color.WHITE, Color.WHITE, Color.WHITE}; // ,Color.WHITE};
		image = makeColorBand( 512, 16, colors);
		savePNG( image, "synadia_underline.png"  );


		colors = new Color[] { c2, c1}; // ,Color.WHITE};
		image = makeColorBandWave( 1024, 256, (float)0.2, (float)0.5, true, colors);
		savePNG( image, "wave_top.png"  );

		colors = new Color[] { c1, c2 }; // ,Color.WHITE};
		image = makeColorBandWave( 1024, 256, (float)0.2, (float)0.5, false, colors);
		savePNG( image, "wave_bottom.png"  );


		colors = new Color[] { c5, c5, c6, Color.black,  Color.black};
		image = makeColorBandWave( 1024, 256, (float)0.2, (float)0.5, true, colors);
		savePNG( image, "synadia_wave_top.png"  );

		colors = new Color[] {  Color.black,  Color.black, c6, c5, c5};
		image = makeColorBandWave( 1024, 256, (float)0.2, (float)0.5, false, colors);
		savePNG( image, "synadia_wave_bottom.png"  );

	}

}
