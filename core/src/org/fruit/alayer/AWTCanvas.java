/******************************************************************************************
 * COPYRIGHT:                                                                             *
 * Universitat Politecnica de Valencia 2013                                               *
 * Camino de Vera, s/n                                                                    *
 * 46022 Valencia, Spain                                                                  *
 * www.upv.es                                                                             *
 *                                                                                        * 
 * D I S C L A I M E R:                                                                   *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)      *
 * in the context of the european funded FITTEST project (contract number ICT257574)      *
 * of which the UPV is the coordinator. As the sole developer of this source code,        *
 * following the signed FITTEST Consortium Agreement, the UPV should decide upon an       *
 * appropriate license under which the source code will be distributed after termination  *
 * of the project. Until this time, this code can be used by the partners of the          *
 * FITTEST project for executing the tasks that are outlined in the Description of Work   *
 * (DoW) that is annexed to the contract with the EU.                                     *
 *                                                                                        * 
 * Although it has already been decided that this code will be distributed under an open  *
 * source license, the exact license has not been decided upon and will be announced      *
 * before the end of the project. Beware of any restrictions regarding the use of this    *
 * work that might arise from the open source license it might fall under! It is the      *
 * UPV's intention to make this work accessible, free of any charge.                      *
 *****************************************************************************************/

/**
 *  @author Sebastian Bauersfeld
 */
package org.fruit.alayer;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.fruit.Assert;
import org.fruit.Pair;

public class AWTCanvas implements Image, Canvas {

	public static enum StorageFormat{ JPEG, PNG, BMP; }

	public static void saveAsJpeg(BufferedImage image, OutputStream os, double quality) throws IOException{
		if(quality == 1){
			if(!ImageIO.write(image, "jpeg", os))
				throw new IOException("Unable to write image as JPEG!");
			return;
		}

		Iterator<ImageWriter> writerIter = ImageIO.getImageWritersByFormatName("jpeg");

		if(!writerIter.hasNext())
			throw new IOException();

		ImageWriter iw = writerIter.next();
		ImageOutputStream ios = ImageIO.createImageOutputStream(os);
		iw.setOutput(ios);
		ImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality((float)quality);
		iw.write(null, new IIOImage(image, null, null), param);
		iw.dispose();
		ios.close();
	}

	public static void saveAsPng(BufferedImage image, OutputStream os) 
			throws IOException{
		if(!ImageIO.write(image, "png", os))
			throw new IOException("Unable to write image as PNG!");
	}

	public static void saveAsBmp(BufferedImage image, OutputStream os) throws IOException{
		BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		target.getGraphics().drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		if(!ImageIO.write(target, "bmp", os))
			throw new IOException("Unable to write image as BMP!");
	}

	public static AWTCanvas fromScreenshot(Rect r){
		return fromScreenshot(r, StorageFormat.PNG, 1);
	}

	public static AWTCanvas fromScreenshot(Rect r, 
			StorageFormat format, double quality){
		Assert.notNull(r, format);
		Assert.isTrue(quality > 0 && quality <= 1);
		try{
			// the rectangle may capture multiple screens!
			Rectangle rect = new Rectangle((int)r.x(), (int)r.y(), 
					(int)r.width(), (int)r.height());
			return new AWTCanvas(r.x(), r.y(), new Robot().createScreenCapture(rect), format, quality);
		} catch (AWTException awte) {
			throw new RuntimeException(awte);
		}
	}

	public static AWTCanvas fromFile(String file) throws IOException{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(file)));

		try{
			return fromInputStream(bis);
		}finally{
			bis.close();
		}
	}
	
	public static AWTCanvas fromInputStream(InputStream is) throws IOException{
		ImageInputStream iis = ImageIO.createImageInputStream(is);
		BufferedImage bi = ImageIO.read(iis);    // already closes the ImageInputStream on success !!!

		if(bi == null){
			iis.close();
			throw new IOException("Unable to load image!");
		}

		return new AWTCanvas(0, 0, bi, StorageFormat.PNG, 1);
	}

	private static final long serialVersionUID = -5041497503329308870L;
	private transient BufferedImage img;
	private StorageFormat format;
	private double quality;
	private double x, y;
	transient Graphics2D gr;
	static final Pen defaultPen = Pen.PEN_DEFAULT;
	double fontSize, strokeWidth;
	String font;
	StrokePattern strokePattern;
	FillPattern fillPattern;
	StrokeCaps strokeCaps;
	Color color;

	public AWTCanvas(int width, int height){ this(0, 0, width, height);	}
	
	public AWTCanvas(double x, double y, int width, int height){
		this(x, y, new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), StorageFormat.PNG, 1.0);
	}
	
	public AWTCanvas(double x, double y, BufferedImage image, StorageFormat format, double quality){
		Assert.notNull(image, format);
		Assert.isTrue(quality >= 0 && quality <= 1);

		this.x = x;
		this.y = y;
		
		if(image.getType() != BufferedImage.TYPE_INT_ARGB){
			img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			img.getGraphics().drawImage(image, 0, 0, null);
		}else{
			img = image;
		}
		this.format = format;
		this.quality = quality;
		gr = img.createGraphics();
		//		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		//				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		adjustPen(defaultPen);		
		//gr.setComposite(AlphaComposite.Clear);
	}

	public void begin() {}
	public void end() {}
	public Pen defaultPen(){ return defaultPen; }
	public double width(){ return img.getWidth(); }
	public double height(){ return img.getHeight(); }
	public double x(){ return x; }
	public double y(){ return y; }
	public BufferedImage image(){ return img; }
	
	private void adjustPen(Pen pen){
		Double tstrokeWidth = pen.strokeWidth();
		if(tstrokeWidth == null)
			tstrokeWidth = defaultPen.strokeWidth();
		
		StrokePattern tstrokePattern = pen.strokePattern();
		if(tstrokePattern == null)
			tstrokePattern = defaultPen.strokePattern();
		
		StrokeCaps tstrokeCaps = pen.strokeCaps();
		if(tstrokeCaps == null)
			tstrokeCaps = defaultPen.strokeCaps();

		if(!tstrokeWidth.equals(strokeWidth) || tstrokePattern != strokePattern || tstrokeCaps != strokeCaps){
			strokePattern = tstrokePattern;
			strokeWidth = tstrokeWidth;
			strokeCaps = tstrokeCaps;
			gr.setStroke(new BasicStroke((float)(double)strokeWidth));
		}
		
		Color tcolor = pen.color();
		if(tcolor == null)
			tcolor = defaultPen.color();
		
		if(!tcolor.equals(color)){
			color = tcolor;
			gr.setColor(new java.awt.Color(color.red(), color.green(), color.blue(), color.alpha()));
		}
		
		String tfont = pen.font();
		if(tfont == null)
			tfont = defaultPen.font();
		
		Double tfontSize = pen.fontSize();
		if(tfontSize == null)
			tfontSize = defaultPen.fontSize();
		
		if(!tfont.equals(font) || !tfontSize.equals(fontSize)){
			font = tfont;
			fontSize = tfontSize;
			gr.setFont(new Font(font, Font.PLAIN, (int)(double)fontSize));
		}
		
		FillPattern tfillPattern = pen.fillPattern();
		if(tfillPattern == null)
			tfillPattern = defaultPen.fillPattern();
		
		if(tfillPattern != fillPattern){
			fillPattern = tfillPattern;
		}
	}
	
	
	public void line(Pen pen, double x1, double y1, double x2, double y2) {
		Assert.notNull(pen);
		adjustPen(pen);
		gr.drawLine((int)(x1 - this.x), (int)(y1 - this.y), (int)(x2 - this.x), (int)(y2 - this.y));
	}

	public void text(Pen pen, double x, double y, double angle, String text) {
		Assert.notNull(pen, text);
		adjustPen(pen);

		//if(angle == 0){
		//		gr.drawString(text, (int)x, (int)y);			
		//		}else{
		TextLayout txtl = new TextLayout(text, gr.getFont(), new FontRenderContext(null, true, false));
		AffineTransform at = new AffineTransform();
		at.translate(x - this.x, y - this.y + txtl.getBounds().getHeight());
		at.rotate(angle);
		gr.fill(txtl.getOutline(at));
		//}
	}

	public Pair<Double, Double> textMetrics(Pen pen, String text) {
		Assert.notNull(pen, text);
		adjustPen(pen);
		Rectangle2D r = new TextLayout(text, gr.getFont(), new FontRenderContext(null, true, false)).getBounds();
		return Pair.from(r.getWidth(), r.getHeight());
	}

	public void clear(double x, double y, double width, double height) {
		Assert.isTrue(width >= 0 && height >= 0);
		gr.clearRect((int)(x - this.x), (int)(y - this.y), (int)width, (int)height);
	}

	public void image(Pen pen, double x, double y, double width,
			double height, int[] image, int imageWidth, int imageHeight) {
		Assert.notNull(image, pen);
		Assert.isTrue(imageWidth >= 0 && imageHeight >= 0 && width >= 0 && height >= 0);
		BufferedImage bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		bi.setRGB(0, 0, imageWidth, imageHeight, image, 0, imageWidth);
		gr.drawImage(bi, (int)(x - this.x), (int)(y - this.y), (int)width, (int)height, null);
	}

	public void ellipse(Pen pen, double x, double y, double width,
			double height) {
		Assert.notNull(pen);
		Assert.isTrue(width >= 0 && height >= 0);

		adjustPen(pen);
		if(fillPattern == FillPattern.Solid)
			gr.fillOval((int)(x - this.x), (int)(y - this.y), (int)width, (int)height);
		else
			gr.drawOval((int)(x - this.x), (int)(y - this.y), (int)width, (int)height);
	}

	public void rect(Pen pen, double x, double y, double width, double height) {
		Assert.notNull(pen);
		Assert.isTrue(width >= 0 && height >= 0);
		adjustPen(pen);
		if(fillPattern.equals(FillPattern.Solid))
			gr.fillRect((int)(x - this.x), (int)(y - this.y), (int)width, (int)height);
		else
			gr.drawRect((int)(x - this.x), (int)(y - this.y), (int)width, (int)height);
	}	

	public void saveAsJpeg(OutputStream os, double quality) throws IOException{
		BufferedImage converted = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		converted.getGraphics().drawImage(img, 0, 0, null);
		saveAsJpeg(converted, os, quality);
	}

	public void saveAsJpeg(String file, double quality) throws IOException{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(file)));

		try{
			saveAsJpeg(bos, quality);
		}finally{
			bos.close();
		}
	}

	public void saveAsPng(OutputStream os) throws IOException{
		saveAsPng(img, os);
	}

	public void saveAsPng(String file) throws IOException{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(file)));

		try{
			saveAsPng(bos);
		}finally{
			bos.close();
		}
	}

	private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException{
		is.defaultReadObject();
		img = ImageIO.read(is);
		if(img == null)
			throw new IOException("Unable to read AWTCanvas!");
		BufferedImage converted = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		converted.getGraphics().drawImage(img, 0, 0, null);
		img = converted;
		gr = img.createGraphics();
	}

	private void writeObject(ObjectOutputStream os) throws IOException, ClassNotFoundException{
		os.defaultWriteObject();
		switch(format){
		case JPEG: saveAsJpeg(img, os, quality); break;
		case PNG: saveAsPng(img, os); break;
		case BMP: saveAsBmp(img, os); break;
		}
	}

	public void paint(Canvas canvas, double x, double y, double width,
			double height) {
		Assert.notNull(canvas);

		int data[] = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		canvas.image(canvas.defaultPen(), x, y, width, height,
				data, img.getWidth(), img.getHeight());
	}

	public void paint(Canvas canvas, Rect srcRect, Rect destRect) {
		Assert.notNull(canvas, srcRect, destRect);
		Assert.isTrue(srcRect.x() >= 0 && srcRect.y() >= 0);
		Assert.isTrue(srcRect.x() + srcRect.width() <= img.getWidth() && srcRect.y() + srcRect.height() <= img.getHeight());

		int srcX = (int)srcRect.x();
		int srcY = (int)srcRect.y();
		int srcWidth = (int)srcRect.width();
		int srcHeight = (int)srcRect.height();
		BufferedImage subImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_ARGB);
		subImage.getGraphics().drawImage(img.getSubimage(srcX, srcY, srcWidth, srcHeight), 0, 0, srcWidth, srcHeight, null);
		
		int area[] = ((DataBufferInt)subImage.getRaster().getDataBuffer()).getData();
		canvas.image(canvas.defaultPen(), destRect.x(), destRect.y(), destRect.width(), destRect.height(), area, srcWidth, srcHeight);
	}

	public void triangle(Pen pen, double x1, double y1, double x2, double y2,
			double x3, double y3) {
		Polygon pol = new Polygon(new int[]{(int)(x1 - this.x), (int)(x2 - this.x), (int)(x3 - this.x)}, new int[]{(int)(y1 - this.y), (int)(y2 - this.y), (int)(y3 - this.y)}, 3);
		adjustPen(pen);

		if(fillPattern.equals(FillPattern.Solid))
			gr.fillPolygon(pol);
		else
			gr.drawPolygon(pol);
	}

	public void release() {}
	
	public String toString(){ return "AWTCanvas (width: " + width() + " height: " + height() + ")";	}
}