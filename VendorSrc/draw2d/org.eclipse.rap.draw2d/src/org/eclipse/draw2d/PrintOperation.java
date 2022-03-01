/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.draw2d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.Printer;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Implementation of draw2d's printing capabilities.
 * 
 * @author Dan Lee
 * @author Eric Bordeau
 */
public abstract class PrintOperation {

	private GC printerGC; // Note: Only one GC instance should be created per
							// print job
	private Insets printMargin = new Insets(0, 0, 0, 0);
	private Printer printer;
	private PrinterGraphics printerGraphics;
	private SWTGraphics g;

	/**
	 * Creates a new PrintOperation
	 */
	public PrintOperation() {
	}

	/**
	 * Creates a new PrintOperation on Printer p
	 * 
	 * @param p
	 *            The printer to print on
	 */
	public PrintOperation(Printer p) {
		setPrinter(p);
	}

	/**
	 * Disposes the PrinterGraphics and GC objects associated with this
	 * PrintOperation.
	 */
	protected void cleanup() {
		if (g != null) {
			printerGraphics.dispose();
			g.dispose();
		}
		if (printerGC != null)
			printerGC.dispose();
	}

	/**
	 * Returns a new PrinterGraphics setup for the Printer associated with this
	 * PrintOperation.
	 * 
	 * @return PrinterGraphics The new PrinterGraphics
	 */
	protected PrinterGraphics getFreshPrinterGraphics() {
		if (printerGraphics != null) {
			printerGraphics.dispose();
			g.dispose();
			printerGraphics = null;
			g = null;
		}
		g = new SWTGraphics(printerGC);
		printerGraphics = new PrinterGraphics(g, printer);
		setupGraphicsForPage(printerGraphics);
		return printerGraphics;
	}

	/**
	 * This method is invoked by the {@link #run(String)} method to determine
	 * the orientation of the GC to be used for printing. This default
	 * implementation always returns SWT.LEFT_TO_RIGHT.
	 * 
	 * @return SWT.LEFT_TO_RIGHT or SWT.RIGHT_TO_LEFT
	 * @since 3.1
	 * @TODO Make protected post-3.1
	 */
	int getGraphicsOrientation() {
		return SWT.LEFT_TO_RIGHT;
	}

	/**
	 * Returns the printer.
	 * 
	 * @return Printer
	 */
	public Printer getPrinter() {
		return printer;
	}

	/**
	 * Returns a Rectangle that represents the region that can be printed to.
	 * The x, y, height, and width values are using the printers coordinates.
	 * 
	 * @return the print region
	 */
	public Rectangle getPrintRegion() {
		org.eclipse.swt.graphics.Rectangle trim = printer.computeTrim(0, 0, 0,
				0);
		org.eclipse.swt.graphics.Point printerDPI = printer.getDPI();
		Insets notAvailable = new Insets(-trim.y, -trim.x,
				trim.height + trim.y, trim.width + trim.x);
		Insets userPreferred = new Insets(
				(printMargin.top * printerDPI.x) / 72,
				(printMargin.left * printerDPI.x) / 72,
				(printMargin.bottom * printerDPI.x) / 72,
				(printMargin.right * printerDPI.x) / 72);
		Rectangle paperBounds = new Rectangle(printer.getBounds());
		Rectangle printRegion = paperBounds.getCropped(notAvailable);
		printRegion.intersect(paperBounds.getCropped(userPreferred));
		printRegion.translate(trim.x, trim.y);
		return printRegion;
	}

	/**
	 * This method contains all operations performed to sourceFigure prior to
	 * being printed.
	 */
	protected void preparePrintSource() {
	}

	/**
	 * This method is responsible for printing pages. (A page is printed by
	 * calling Printer.startPage(), followed by painting to the PrinterGraphics
	 * object, and then calling Printer.endPage()).
	 */
	protected abstract void printPages();

	/**
	 * This method contains all operations performed to sourceFigure after being
	 * printed.
	 */
	protected void restorePrintSource() {
	}

	/**
	 * Sets the print job into motion.
	 * 
	 * @param jobName
	 *            A String representing the name of the print job
	 */
	public void run(String jobName) {
		preparePrintSource();
		if (printer.startJob(jobName)) {
			printerGC = new GC(getPrinter(), getGraphicsOrientation());
			printPages();
			printer.endJob();
		}
		restorePrintSource();
		cleanup();
	}

	/**
	 * Sets the printer.
	 * 
	 * @param printer
	 *            The printer to set
	 */
	public void setPrinter(Printer printer) {
		this.printer = printer;
	}

	/**
	 * Sets the page margin in pels (logical pixels) to the passed Insets.(72
	 * pels == 1 inch)
	 * 
	 * @param margin
	 *            The margin to set on the page
	 */
	public void setPrintMargin(Insets margin) {
		printMargin = margin;
	}

	/**
	 * Manipulates the PrinterGraphics to position it to paint in the desired
	 * region of the page. (Default is the top left corner of the page).
	 * 
	 * @param pg
	 *            The PrinterGraphics to setup
	 */
	protected void setupGraphicsForPage(PrinterGraphics pg) {
		Rectangle printRegion = getPrintRegion();
		pg.clipRect(printRegion);
		pg.translate(printRegion.getTopLeft());
	}

}