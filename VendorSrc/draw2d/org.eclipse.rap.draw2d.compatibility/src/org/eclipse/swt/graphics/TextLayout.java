/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.graphics;

import intrinsic.flash.text.TextField;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.internal.Compatibility;
import org.eclipse.swt.widgets.Display;

//UNSUPPORTED - cannot use package private super type constructor
//public final class TextLayout extends Resource {
public final class TextLayout {
  
	Font font;
	String text;
	int lineSpacing;
	int ascent, descent;
	int alignment;
	int wrapWidth;
	int orientation;
	int indent;
	boolean justify;
	int[] tabs;
	int[] segments;
	StyleItem[] styles;

	TextField textField;
	
	static final int BORDER = 2; 
	static final int TAB_COUNT = 32;
	
	static class StyleItem {
		TextStyle style;
		int start;
		
		public String toString () {
			return "StyleItem {" + start + ", " + style + "}";
		}
	}	
	
	//BEGIN Resource API
	final Device device;
	private boolean disposed;
	  
	/**
	 * Returns the <code>Device</code> where this resource was
	 * created.
	 *
	 * @return <code>Device</code> the device of the receiver
	 * 
	 * @since 1.3
	 */
	public Device getDevice() {
	  if( disposed ) {
	    SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
	  }
	  Device result = device;
	  // Currently, factory-managed resources (device == null) return the current 
	  // display. This is done under the assumption that resource methods are
	  // only called from the UI thread. This way also shared resources appear to 
	  // belong to the current session.
	  // Note that this is still under investigation.
	  if( result == null ) {
	    result = Display.getCurrent();
	  }
	  return result;
	}
	
	/**
	 * Disposes of the resource. Applications must dispose of all resources
	 * which they allocate.
	 * This method does nothing if the resource is already disposed.
	 * 
	 * @since 1.3
	 */
	public void dispose() {
	  if( device == null ) {
	    String msg = "A factory-created resource cannot be disposed.";
	    throw new IllegalStateException( msg );
	  }
	  destroy();
	  disposed = true;
	}
	
	  static Device checkDevice( final Device device ) {
	    Device result = device;
	    if( result == null ) {
	      result = Display.getCurrent();
	    }
	    if( result == null ) {
	      SWT.error( SWT.ERROR_NULL_ARGUMENT );
	    }
	    return result;
	  }
	   //END Resource API
	
public TextLayout (Device device) {
    this.device = device;
	ascent = descent = wrapWidth = -1;
	text = "";
	styles = new StyleItem[2];
	styles[0] = new StyleItem();
	styles[1] = new StyleItem();
}

void checkLayout () {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
}

void destroy () {
	freeRuns();
	font = null;	
	text = null;
	tabs = null;
	styles = null;
}

void computeRuns () {
	computeRuns (null);
}

int [] computePolyline(int left, int top, int right, int bottom) {
	int height = bottom - top; // can be any number
	int width = 2 * height; // must be even
	int peaks = Compatibility.ceil(right - left, width);
	if (peaks == 0 && right - left > 2) {
		peaks = 1;
	}
	int length = ((2 * peaks) + 1) * 2;
	if (length < 0) return new int[0];
	
	int[] coordinates = new int[length];
	for (int i = 0; i < peaks; i++) {
		int index = 4 * i;
		coordinates[index] = left + (width * i);
		coordinates[index+1] = bottom;
		coordinates[index+2] = coordinates[index] + width / 2;
		coordinates[index+3] = top;
	}
	coordinates[length-2] = left + (width * peaks);
	coordinates[length-1] = bottom;
	return coordinates;
}

void computeRuns (GCData data) {
//	if (textField != null) {
//		if (data == null) return;
//		if (textField.parent == null) {
//			intrinsic.Number currentFg = (intrinsic.Number)textField.defaultTextFormat.color;
//			intrinsic.Number newFg = new intrinsic.Number(data.foreground.handle);
//			if (newFg.equals(currentFg)) return;
//		}
//	}
//	int length = text.length();
//	textField = new TextField();
//	textField.text = length == 0 ? " " : text;
//	if (length == 0) length = 1;
//	textField.autoSize = TextFieldAutoSize.LEFT;
//	textField.multiline = true;
//	textField.selectable = false;
//	textField.mouseEnabled = false;
//	textField.alwaysShowSelection = true;
//	textField.background = false;
//	TextFormat format = new TextFormat();
//	textField.defaultTextFormat = format;
//	Font defaultFont = this.font != null ? this.font : device.systemFont;
//	format.font = defaultFont.fontFamily;
//	format.size = new intrinsic.Number(defaultFont.fontSize);
//	if (data != null) {
//		format.color = new intrinsic.Number(data.foreground.handle);
//	}
//	if (defaultFont.fontStyle.equals("italic")) format.italic = defaultFont.fontStyle;
//	if (defaultFont.fontWeight.equals("bold")) format.bold = defaultFont.fontWeight;
//	if (wrapWidth != -1) {
//		String align = TextFormatAlign.LEFT;
//		if (justify) {
//			align = TextFormatAlign.JUSTIFY;
//		} else {
//			switch (alignment) {
//				case SWT.CENTER:
//					align = TextFormatAlign.CENTER;
//					break;
//				case SWT.RIGHT:
//					align = TextFormatAlign.RIGHT;
//			}
//		}
//		format.align = align;
//		textField.width = wrapWidth;
//		textField.wordWrap = true;
//	}
//	format.leading = new intrinsic.Number(lineSpacing);
//	format.indent = new intrinsic.Number(indent);	
//	int tabWidth = 0;
//	int tabX = 0;
//	Array tabArray = new Array();
//	int tabCount = TAB_COUNT;
//	if (tabs != null) {
//		tabCount = Math.max(TAB_COUNT, tabs.length);
//		if (tabs.length == 1) {
//			tabWidth = tabs[0];
//		}
//		if (tabs.length > 1) {
//			tabWidth = tabs[tabs.length - 1] - tabs[tabs.length - 2];
//		}
//		for (int i = 0; i < tabs.length; i++) {
//			tabX += tabs[i];
//			tabArray.push(tabX);
//			tabCount--;
//		}
//	}
//	while (tabCount > 0) {
//		tabX += tabWidth;
//		tabArray.push(tabX);
//		tabCount--;
//	}
//	format.tabStops = tabArray;
//	textField.setTextFormat(format, 0, length);
//	//TODO lineSpacing, ascent, descent, orientation, segments
//	int start, end;
//	for (int i = 0; i < styles.length - 1; i++) {
//		StyleItem run = styles[i];
//		if (run.style == null) continue;
//		TextStyle style = run.style;
//		start = translateOffset(run.start);
//		end = translateOffset(styles[i + 1].start);
//		format = new TextFormat();
//		Font font = style.font;
//		if (font != null) {
//			format.font = font.fontFamily;
//			format.size = new intrinsic.Number(font.fontSize);
//			if (font.fontStyle.equals("italic")) format.italic = font.fontStyle;
//			if (font.fontWeight.equals("bold")) format.bold = font.fontWeight;
//		}
//		Color foreground = style.foreground;
//		if (foreground != null) {
//			format.color = new intrinsic.Number(foreground.handle);
//		}
//		if (style.rise != 0) {
//			//TODO rise
//		}
//		if (style.metrics != null) {
//			//TODO metrics
//		}
//		textField.setTextFormat(format, start, end);
//	}
}

public void draw (GC gc, int x, int y) {
	draw(gc, x, y, -1, -1, null, null);
}

public void draw (GC gc, int x, int y, int selectionStart, int selectionEnd, Color selectionForeground, Color selectionBackground) {
	draw(gc, x, y, selectionStart, selectionEnd, selectionForeground, selectionBackground, 0);
}

public void draw (GC gc, int x, int y, int selectionStart, int selectionEnd, Color selectionForeground, Color selectionBackground, int flags) {
	checkLayout();
	if (gc == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (gc.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	
	gc.drawText( text, x, y, true );
	
//	GCData data = gc.data;
//	computeRuns(data);
//	if (selectionForeground != null && selectionForeground.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
//	if (selectionBackground != null && selectionBackground.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
//	int length = text.length();
//	if (length == 0 && flags == 0) return;
//	//TODO flags, selection
//	boolean hasSelection = selectionStart <= selectionEnd && selectionStart != -1 && selectionEnd != -1;
//	if (hasSelection) {
//		textField.setSelection(selectionStart, selectionEnd + 1);
//	} else {
//		textField.setSelection(-1, -1);
//	}
//	y -= BORDER;
//	x -= BORDER;
//	if (data.matrix != null) {
//		intrinsic.flash.geom.Point point = new intrinsic.flash.geom.Point(x, y);
//		point = data.matrix.transformPoint(point);
//		textField.x = point.x;
//		textField.y = point.y;		
//	} else {
//		textField.x = x;
//		textField.y = y;
//	}
//	if (data.matrix != null) textField.transform.matrix = data.matrix;
//	if (data.clip != null) textField.mask = data.clip;
//
//	/* Draw background */
//	for (int i = 0; i < styles.length - 1; i++) {
//		StyleItem run = styles[i];
//		TextStyle style = run.style;
//		if (style == null || style.background == null) continue;
//		int start = translateOffset(run.start);
//		int end = translateOffset(styles[i + 1].start);
//		double lineY = y + BORDER;
//		for (int lineIndex = 0; lineIndex < textField.numLines; lineIndex++) {
//			int lineStart = textField.getLineOffset(lineIndex);
//			int lineEnd = lineStart + textField.getLineLength(lineIndex);
//			TextLineMetrics metrics = textField.getLineMetrics(lineIndex);
//			if (!(start > lineEnd || end < lineStart)) {
//				int highStart = Math.max(lineStart, start);
//				int highEnd = Math.min(lineEnd, end);
//				if (highStart != highEnd) {
//					intrinsic.flash.geom.Rectangle startRect = textField.getCharBoundaries(highStart);
//					intrinsic.flash.geom.Rectangle endRect = textField.getCharBoundaries(highEnd - 1);
//					Shape shape = new Shape();
//					if (data.matrix != null) shape.transform.matrix = data.matrix;
//					if (data.clip != null) shape.mask = data.clip;
//					Graphics graphics = shape.graphics;
//					graphics.beginFill(style.background.handle, data.alpha / 255f);
//					graphics.drawRect(x + startRect.x, lineY, endRect.right - startRect.left, metrics.height);
//					graphics.endFill();
//					data.sprite.addChild(shape);
//				}
//			}
//			if (lineEnd > end) break;
//			lineY += metrics.height;
//		}
//	}
//	
//	/* Draw Text */
//	data.sprite.addChild(textField);
//
//	/* Draw underline and border */
//	for (int j = 0; j < styles.length - 1; j++) {
//		StyleItem run = styles[j];
//		TextStyle style = run.style;
//		if (style == null) continue;
//		boolean drawUnderline = style.underline && (j + 1 >= styles.length || !style.isAdherentUnderline(styles[j + 1].style));
//		boolean drawBorder = style.borderStyle != SWT.NONE && (j + 1 >= styles.length || !style.isAdherentBorder(styles[j + 1].style));
//		boolean drawStrikeout = style.strikeout;
//		if (!drawUnderline && !drawBorder && !drawStrikeout) continue;
//		
//		int end = translateOffset(styles[j + 1].start);
//		double lineY = y + BORDER;
//		for (int lineIndex = 0; lineIndex < textField.numLines; lineIndex++) {
//			int lineStart = textField.getLineOffset(lineIndex);
//			int lineEnd = lineStart + textField.getLineLength(lineIndex);
//			TextLineMetrics metrics = textField.getLineMetrics(lineIndex);
//			if (drawUnderline) {
//				int start = run.start;
//				for (int k = j; k > 0 && style.isAdherentUnderline(styles[k - 1].style); k--) {
//					start = styles[k - 1].start;
//				}
//				start = translateOffset(start);
//				if (!(start > lineEnd || end < lineStart)) {
//					int highStart = Math.max(lineStart, start);
//					int highEnd = Math.min(lineEnd, end);
//					if (highStart != highEnd) {
//						/* Draw Underline */
//						intrinsic.flash.geom.Rectangle startRect = textField.getCharBoundaries(highStart);
//						intrinsic.flash.geom.Rectangle endRect = textField.getCharBoundaries(highEnd - 1);
//						Shape shape = new Shape();
//						if (data.matrix != null) shape.transform.matrix = data.matrix;
//						if (data.clip != null) shape.mask = data.clip;
//						Graphics graphics = shape.graphics;
//						int color = data.foreground.handle;
//						if (style.foreground != null) color = style.foreground.handle;
//						if (style.underlineColor != null) color = style.underlineColor.handle;
//						double underlineX = x + startRect.x; 
//						double underlineY = lineY + metrics.ascent + 1;
//						double underlineThickness = 0.5;
//						graphics.lineStyle(0, color, data.alpha / 255f, false, "normal", CapsStyle.ROUND, JointStyle.MITER, data.lineMiterLimit);
//						switch (style.underlineStyle) {
//							case SWT.UNDERLINE_ERROR:
//							case SWT.UNDERLINE_SQUIGGLE: 
//								int squigglyThickness = 1;
//								double squigglyHeight = 2 * squigglyThickness;
//								double lineBottom = lineY + metrics.height;
//								double squigglyY = Math.min(underlineY - squigglyHeight / 2, lineBottom - squigglyHeight - 1);
//								int[] points = computePolyline((int)underlineX, (int)squigglyY, (int)(underlineX + endRect.right - startRect.left), (int)(squigglyY + squigglyHeight));	
//								graphics.moveTo(points[0] + 0.5, points[1] + 0.5);
//								for (int i = 2; i < points.length; i+= 2) {
//									graphics.lineTo(points[i] + 0.5, points[i+1] + 0.5);
//								}
//								break;
//							case SWT.UNDERLINE_DOUBLE:
//								graphics.drawRect(underlineX, underlineY + underlineThickness * 4, endRect.right - startRect.left, underlineThickness);
//								//FALLTHROUGH
//							case SWT.UNDERLINE_SINGLE:
//								graphics.drawRect(underlineX, underlineY, endRect.right - startRect.left, underlineThickness);
//								break;
//						}
//						data.sprite.addChild(shape);
//					}
//				}
//			}
//			
//			if (drawBorder) {
//				int start = run.start;
//				for (int k = j; k > 0 && style.isAdherentBorder(styles[k - 1].style); k--) {
//					start = styles[k - 1].start;
//				}
//				start = translateOffset(start);
//				if (!(start > lineEnd || end < lineStart)) {
//					int highStart = Math.max(lineStart, start);
//					int highEnd = Math.min(lineEnd, end);
//					if (highStart != highEnd) {
//						/* Draw Border */
//						intrinsic.flash.geom.Rectangle startRect = textField.getCharBoundaries(highStart);
//						intrinsic.flash.geom.Rectangle endRect = textField.getCharBoundaries(highEnd - 1);
//						Shape shape = new Shape();
//						if (data.matrix != null) shape.transform.matrix = data.matrix;
//						if (data.clip != null) shape.mask = data.clip;
//						int color = data.foreground.handle;
//						if (style.foreground != null) color = style.foreground.handle;
//						if (style.borderColor != null) color = style.borderColor.handle;
//						//TODO border style
//						Graphics graphics = shape.graphics;
//						graphics.lineStyle(0, color, data.alpha / 255f, false, "normal", CapsStyle.ROUND, JointStyle.MITER, data.lineMiterLimit);
//						graphics.drawRect(x + startRect.x, lineY, endRect.right - startRect.left - 1, metrics.height - 1);
//						data.sprite.addChild(shape);
//					}
//				}
//			}
//			
//			if (drawStrikeout) {
//				int start = translateOffset(run.start);
//				if (!(start > lineEnd || end < lineStart)) {
//					int highStart = Math.max(lineStart, start);
//					int highEnd = Math.min(lineEnd, end);
//					if (highStart != highEnd) {
//						/* Draw Strikeout */
//						intrinsic.flash.geom.Rectangle startRect = textField.getCharBoundaries(highStart);
//						intrinsic.flash.geom.Rectangle endRect = textField.getCharBoundaries(highEnd - 1);
//						Shape shape = new Shape();
//						if (data.matrix != null) shape.transform.matrix = data.matrix;
//						if (data.clip != null) shape.mask = data.clip;
//						int color = data.foreground.handle;
//						if (style.foreground != null) color = style.foreground.handle;
//						if (style.strikeoutColor != null) color = style.strikeoutColor.handle;
//						Graphics graphics = shape.graphics;
//						graphics.lineStyle(0, color, data.alpha / 255f, false, "normal", CapsStyle.ROUND, JointStyle.MITER, data.lineMiterLimit);
//						double striteoutY = lineY + metrics.height / 3 * 2;
//						graphics.moveTo(x + startRect.x, striteoutY);
//						graphics.lineTo(x + startRect.x + endRect.right - startRect.left, striteoutY);
//						data.sprite.addChild(shape);
//					}
//				}
//			}
//			if (lineEnd > end) break;
//			lineY += metrics.height;
//		}
//	}			
}

void freeRuns () {
	textField = new TextField();
}

public int getAlignment () {
	checkLayout();
	return alignment;
}

public int getAscent () {
	checkLayout();
	return ascent;
}

public Rectangle getBounds () {
	checkLayout();
	computeRuns();
	int width = text.length() > 0 ? (int)textField.textWidth : 0;
	int height = (int)textField.textHeight;
	if (wrapWidth != -1) width = wrapWidth;
	if (ascent != -1 && descent != -1) {
		height = Math.max (height, ascent + descent);
	}
	return new Rectangle(0, 0, width, height);
}

public Rectangle getBounds (int start, int end) {
	checkLayout();
	computeRuns();
	int length = text.length();
	if (length == 0) return new Rectangle(0, 0, 0, 0);
	if (start > end) return new Rectangle(0, 0, 0, 0);
	start = Math.min(Math.max(0, start), length - 1);
	end = Math.min(Math.max(0, end), length - 1);
	start = translateOffset(start);
	end = translateOffset(end);
//	intrinsic.flash.geom.Rectangle startRect = textField.getCharBoundaries(start);
//	intrinsic.flash.geom.Rectangle endRect = textField.getCharBoundaries(end);
//	intrinsic.flash.geom.Rectangle rect = startRect.union(endRect);
//	if (textField.getLineIndexOfChar(start) != textField.getLineIndexOfChar(end)) {
//		rect.x = BORDER;
//		rect.width = textField.width;
//	}
//	return new Rectangle((int)rect.x - BORDER, (int)rect.y - BORDER, (int)Math.ceil(rect.width), (int)Math.ceil(rect.height));
	return new Rectangle(0,0,10,10);
}

public int getDescent () {
	checkLayout();
	return descent;
}

public Font getFont () {
	checkLayout();
	return font;
}

public int getIndent () {
	checkLayout();
	return indent;
}

public boolean getJustify () {
	checkLayout();
	return justify;
}

public int getLevel (int offset) {
	checkLayout();
	return 0;
}

public Rectangle getLineBounds (int lineIndex) {
	checkLayout();
	computeRuns();
	if (!(0 <= lineIndex && lineIndex < textField.numLines)) SWT.error(SWT.ERROR_INVALID_RANGE);
	if (text.length() == 0) {
		return getBounds();
	}
	int start = textField.getLineOffset(lineIndex);
//	intrinsic.flash.geom.Rectangle rect = textField.getCharBoundaries(start);
//	TextLineMetrics metrics = textField.getLineMetrics(lineIndex);
//	int height = (int)metrics.height;
//	if (ascent != -1 && descent != -1) {
//		height = Math.max (height, ascent + descent);
//	}
//	return new Rectangle((int)rect.x - BORDER, (int)rect.y - BORDER, (int)metrics.width, height);
	return new Rectangle(0,0,10,10);
}

public int getLineCount () {
	checkLayout ();
	computeRuns();	
	return textField.numLines;
}

public int getLineIndex (int offset) {
	checkLayout ();
	computeRuns();
	int length = text.length();
	if (!(0 <= offset && offset <= length)) SWT.error(SWT.ERROR_INVALID_RANGE);
	if (offset == length) return textField.numLines - 1;
	offset = translateOffset(offset);
	return textField.getLineIndexOfChar(offset);
}

public FontMetrics getLineMetrics (int lineIndex) {
	checkLayout ();
	computeRuns();
	int lineCount = getLineCount();
	if (!(0 <= lineIndex && lineIndex < lineCount)) SWT.error(SWT.ERROR_INVALID_RANGE);
//	TextLineMetrics metrics = textField.getLineMetrics(lineIndex);
//	int ascent = Math.max(this.ascent, (int)metrics.ascent);
//	int descent = Math.max(this.descent, (int)metrics.descent);	
//	return FontMetrics.internal_new(ascent, descent, ascent + descent, 0);
	return null;
}

public int[] getLineOffsets () {
	checkLayout ();
	computeRuns();
	int[] offsets = new int[textField.numLines + 1];
	for (int i = 0; i < offsets.length - 1; i++) {
		offsets[i] = untranslateOffset(textField.getLineOffset(i));
	}
	offsets[offsets.length - 1] = text.length();
	return offsets;
}

public Point getLocation (int offset, boolean trailing) {
	checkLayout();
	computeRuns();
	int length = text.length();
	if (!(0 <= offset && offset <= length)) SWT.error(SWT.ERROR_INVALID_RANGE);
	if (length == 0) return new Point(0, 0);
	offset = translateOffset(offset);
//	intrinsic.flash.geom.Rectangle rect = textField.getCharBoundaries(offset);
//	int x = (int)rect.x;
//	if (trailing) x += rect.width;
//	return new Point(x - BORDER, (int)rect.y - BORDER);
	return new Point(0,0);
}

public int getNextOffset (int offset, int movement) {
	checkLayout();
	return _getOffset (offset, movement, true);
}

int _getOffset(int offset, int movement, boolean forward) {
	computeRuns();
	int length = text.length();
	if (!(0 <= offset && offset <= length)) SWT.error(SWT.ERROR_INVALID_RANGE);
	if (forward && offset == length) return length;
	if (!forward && offset == 0) return 0;
	int step = forward ? 1 : -1;
	if ((movement & org.eclipse.draw2d.rap.swt.SWT.MOVEMENT_CHAR) != 0) return offset + step;
	if ((movement & org.eclipse.draw2d.rap.swt.SWT.MOVEMENT_CLUSTER) != 0) return offset + step;
	offset = translateOffset(offset);
	
	int lineIndex = textField.getLineIndexOfChar(Math.max(0, Math.min(length - 1, offset)));
	int lineStart = textField.getLineOffset(lineIndex);
	int lineLength = textField.getLineLength(lineIndex);
	int lineBreak = 0;//TODO
	while (lineStart <= offset && offset <= lineStart + lineLength) {
		int newOffset = offset + step; 
		int trailing = 0;
		if (forward) {
			if (newOffset + trailing >= lineStart + lineLength - lineBreak) {
				int lineEnd = lineStart + lineLength;
				if (trailing != 0) lineEnd -= lineBreak;
				return untranslateOffset(Math.min(length, lineEnd)); 
			}
		} else {
			if (newOffset + trailing == lineStart) {
				if (lineIndex == 0) return 0;
				int lineEnd = 0;
				//if (newOffset + trailing == offset) lineEnd = OS.TextLine_NewlineLength(lines[lineIndex - 1]);
				return untranslateOffset(Math.max(0, newOffset + trailing - lineEnd)); 
			}
		}
		offset = newOffset + trailing;

		switch (movement) {
			case org.eclipse.draw2d.rap.swt.SWT.MOVEMENT_CLUSTER:
				return untranslateOffset(offset);
			case org.eclipse.draw2d.rap.swt.SWT.MOVEMENT_WORD:
			case org.eclipse.draw2d.rap.swt.SWT.MOVEMENT_WORD_START: {
				if (offset > 0) {
					boolean letterOrDigit = Character.isLetterOrDigit(text.charAt(offset));
					boolean previousLetterOrDigit = Character.isLetterOrDigit(text.charAt(offset - 1));
					if (letterOrDigit != previousLetterOrDigit || !letterOrDigit) {
						if (!Character.isWhitespace(text.charAt(offset))) {
							return untranslateOffset(offset);
						}
					}
				}
				break;
			}
			case org.eclipse.draw2d.rap.swt.SWT.MOVEMENT_WORD_END: {
				if (offset > 0) {
					boolean isLetterOrDigit = Character.isLetterOrDigit(text.charAt(offset));
					boolean previousLetterOrDigit = Character.isLetterOrDigit(text.charAt(offset - 1));
					if (!isLetterOrDigit && previousLetterOrDigit) {
						return untranslateOffset(offset);
					}
				}
				break;
			}
		}
	}
	return forward ? length : 0;
}

public int getOffset (Point point, int[] trailing) {
	checkLayout();
	computeRuns();
	if (point == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	return getOffset(point.x, point.y, trailing);
}

public int getOffset (int x, int y, int[] trailing) {
	checkLayout();
	computeRuns();
	if (trailing != null && trailing.length < 1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	int length = text.length();
	if (length == 0) return 0;
	double nx = Math.max(BORDER, Math.min(x + BORDER, textField.textWidth + BORDER - 1));
	double ny = Math.max(BORDER, Math.min(y + BORDER, textField.textHeight + BORDER - 1));
	int offset = textField.getCharIndexAtPoint(nx, ny);
	
	/*
	*  Bug in Flex. For some reason getCharIndexAtPoint() fails 
	*  for a valid input. The fix is to test char by char.
	*/
//	if (offset == -1) {
//		intrinsic.flash.geom.Rectangle rect;
//		for (int i = 0; i < text.length(); i++) {
//			rect = textField.getCharBoundaries(i);
//			if (rect != null && rect.contains(nx, ny)) {
//				offset = i;
//				break;
//			}
//		}
//	}
//	if (offset == -1) return 0;
//	
//	if (trailing != null) {
//		intrinsic.flash.geom.Rectangle rect = textField.getCharBoundaries(offset);
//		trailing[0] = (nx - rect.x) > rect.width / 2 ? 1 : 0;
//	}
	return Math.min(untranslateOffset(offset), length - 1);
}

public int getOrientation () {
	checkLayout();
	return orientation;
}

public int getPreviousOffset (int offset, int movement) {
	checkLayout();
	return _getOffset (offset, movement, false);
}

public int[] getRanges () {
	checkLayout();
	int[] result = new int[styles.length * 2];
	int count = 0;
	for (int i=0; i<styles.length - 1; i++) {
		if (styles[i].style != null) {
			result[count++] = styles[i].start;
			result[count++] = styles[i + 1].start - 1;
		}
	}
	if (count != result.length) {
		int[] newResult = new int[count];
		System.arraycopy(result, 0, newResult, 0, count);
		result = newResult;
	}
	return result;
}

public int[] getSegments () {
	checkLayout();
	return segments;
}

public int getSpacing () {
	checkLayout();	
	return lineSpacing;
}

public TextStyle getStyle (int offset) {
	checkLayout();
	int length = text.length();
	if (!(0 <= offset && offset < length)) SWT.error(SWT.ERROR_INVALID_RANGE);
	for (int i=1; i<styles.length; i++) {
		if (styles[i].start > offset) {
			return styles[i - 1].style;
		}
	}
	return null;
}

public TextStyle[] getStyles () {
	checkLayout();
	TextStyle[] result = new TextStyle[styles.length];
	int count = 0;
	for (int i=0; i<styles.length; i++) {
		if (styles[i].style != null) {
			result[count++] = styles[i].style;
		}
	}
	if (count != result.length) {
		TextStyle[] newResult = new TextStyle[count];
		System.arraycopy(result, 0, newResult, 0, count);
		result = newResult;
	}
	return result;
}

public int[] getTabs () {
	checkLayout();
	return tabs;
}

public String getText () {
	checkLayout();
	return text;
}

public int getWidth () {
	checkLayout();
	return wrapWidth;
}

public boolean isDisposed () {
	return device == null;
}

public void setAlignment (int alignment) {
	checkLayout();
	int mask = SWT.LEFT | SWT.CENTER | SWT.RIGHT;
	alignment &= mask;
	if (alignment == 0) return;
	if ((alignment & SWT.LEFT) != 0) alignment = SWT.LEFT;
	if ((alignment & SWT.RIGHT) != 0) alignment = SWT.RIGHT;
	if (this.alignment == alignment) return;
	freeRuns();
	this.alignment = alignment;
}

public void setAscent (int ascent) {
	checkLayout();
	if (ascent < -1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (this.ascent == ascent) return;
	freeRuns();
	this.ascent = ascent;
}

public void setDescent (int descent) {
	checkLayout();
	if (descent < -1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (this.descent == descent) return;
	freeRuns();
	this.descent = descent;
}

public void setFont (Font font) {
	checkLayout();
	if (font != null && font.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (this.font == font) return;
	if (font != null && font.equals(this.font)) return;
	freeRuns();
	this.font = font;
}

public void setIndent (int indent) {
	checkLayout();
	if (indent < 0) return;	
	if (this.indent == indent) return;
	freeRuns();
	this.indent = indent;
}

public void setJustify (boolean justify) {
	checkLayout();
	if (this.justify == justify) return;
	freeRuns();
	this.justify = justify;
}

public void setOrientation (int orientation) {
	checkLayout();
	int mask = SWT.LEFT_TO_RIGHT | org.eclipse.draw2d.rap.swt.SWT.RIGHT_TO_LEFT;
	orientation &= mask;
	if (orientation == 0) return;
	if ((orientation & SWT.LEFT_TO_RIGHT) != 0) orientation = SWT.LEFT_TO_RIGHT;
	if (this.orientation == orientation) return;
	this.orientation = orientation;
	freeRuns();
}

public void setSegments (int[] segments) {
	checkLayout();
	if (this.segments == null && segments == null) return;
	if (this.segments != null && segments != null) {
		if (this.segments.length == segments.length) {
			int i;
			for (i = 0; i <segments.length; i++) {
				if (this.segments[i] != segments[i]) break;
			}
			if (i == segments.length) return;
		}
	}
	freeRuns();
	this.segments = segments;
}

public void setSpacing (int spacing) {
	checkLayout();
	if (spacing < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (this.lineSpacing == spacing) return;
	freeRuns();
	this.lineSpacing = spacing;
}

public void setStyle (TextStyle style, int start, int end) {
	checkLayout();
	int length = text.length();
	if (length == 0) return;
	if (start > end) return;
	start = Math.min(Math.max(0, start), length - 1);
	end = Math.min(Math.max(0, end), length - 1);
	int low = -1;
	int high = styles.length;
	while (high - low > 1) {
		int index = (high + low) / 2;
		if (styles[index + 1].start > start) {
			high = index;
		} else {
			low = index;
		}
	}
	if (0 <= high && high < styles.length) {
		StyleItem item = styles[high];
		if (item.start == start && styles[high + 1].start - 1 == end) {
			if (style == null) {
				if (item.style == null) return;
			} else {
				if (style.equals(item.style)) return;
			}
		}
	}
	freeRuns();
	int modifyStart = high;
	int modifyEnd = modifyStart;
	while (modifyEnd < styles.length) {
		if (styles[modifyEnd + 1].start > end) break;
		modifyEnd++;
	}
	if (modifyStart == modifyEnd) {
		int styleStart = styles[modifyStart].start; 
		int styleEnd = styles[modifyEnd + 1].start - 1;
		if (styleStart == start && styleEnd == end) {
			styles[modifyStart].style = style;
			return;
		}
		if (styleStart != start && styleEnd != end) {
			StyleItem[] newStyles = new StyleItem[styles.length + 2];
			System.arraycopy(styles, 0, newStyles, 0, modifyStart + 1);
			StyleItem item = new StyleItem();
			item.start = start;
			item.style = style;
			newStyles[modifyStart + 1] = item;	
			item = new StyleItem();
			item.start = end + 1;
			item.style = styles[modifyStart].style;
			newStyles[modifyStart + 2] = item;
			System.arraycopy(styles, modifyEnd + 1, newStyles, modifyEnd + 3, styles.length - modifyEnd - 1);
			styles = newStyles;
			return;
		}
	}
	if (start == styles[modifyStart].start) modifyStart--;
	if (end == styles[modifyEnd + 1].start - 1) modifyEnd++;
	int newLength = styles.length + 1 - (modifyEnd - modifyStart - 1);
	StyleItem[] newStyles = new StyleItem[newLength];
	System.arraycopy(styles, 0, newStyles, 0, modifyStart + 1);	
	StyleItem item = new StyleItem();
	item.start = start;
	item.style = style;
	newStyles[modifyStart + 1] = item;
	styles[modifyEnd].start = end + 1;
	System.arraycopy(styles, modifyEnd, newStyles, modifyStart + 2, styles.length - modifyEnd);
	styles = newStyles;
}

public void setTabs (int[] tabs) {
	checkLayout();
	if (this.tabs == null && tabs == null) return;
	if (this.tabs != null && tabs !=null) {
		if (this.tabs.length == tabs.length) {
			int i;
			for (i = 0; i <tabs.length; i++) {
				if (this.tabs[i] != tabs[i]) break;
			}
			if (i == tabs.length) return;
		}
	}
	freeRuns();
	this.tabs = tabs;
}

public void setText (String text) {
	checkLayout();
	if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (text.equals(this.text)) return;
	freeRuns();
	this.text = text;
	styles = new StyleItem[2];
	styles[0] = new StyleItem();
	styles[1] = new StyleItem();
	styles[1].start = text.length();
}

public void setWidth (int width) {
	checkLayout();
	if (width < -1 || width == 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (this.wrapWidth == width) return;
	freeRuns();
	this.wrapWidth = width;
}

public String toString () {
	if (isDisposed()) return "TextLayout {*DISPOSED*}";
	return "TextLayout {}";
}

/*
 *  Translate a client offset to an internal offset
 */
int translateOffset (int offset) {
	return offset;
}

/*
 *  Translate an internal offset to a client offset
 */
int untranslateOffset (int offset) {
	return offset;
}

}
