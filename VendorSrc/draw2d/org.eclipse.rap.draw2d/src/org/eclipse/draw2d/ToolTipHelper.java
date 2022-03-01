/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.draw2d;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;

import org.eclipse.draw2d.geometry.Dimension;

/**
 * This class is used by SWTEventDispatcher as support to display Figure
 * tooltips on a mouse hover event. Tooltips are drawn directly below the cursor
 * unless the display does not allow, in which case the tooltip will be drawn
 * directly above the cursor. Tooltips will be displayed with a LineBorder. The
 * background of the tooltips will be the standard SWT tooltipBackground color
 * unless the Figure's tooltip has set its own background.
 */
public class ToolTipHelper extends PopUpHelper {

	private Timer timer;
	private IFigure currentTipSource;
	private IFigure currentTooltip;
	private Layer root;
	private int hideDelay = 5000;
	
	private FigureCanvas canvas;


	/**
	 * Constructs a ToolTipHelper to be associated with Control <i>c</i>.
	 * 
	 * @param c
	 *            the control
	 * @since 2.0
	 */
	public ToolTipHelper(org.eclipse.swt.widgets.Control c) {
		super(c, SWT.TOOL | SWT.ON_TOP);
		getShell().setBackground(ColorConstants.tooltipBackground());
		getShell().setForeground(ColorConstants.tooltipForeground());
	}
	
	
	private Layer getRoot() {
		if (root != null) {
			return root;
		}
		
		getShell().setLayout(new FillLayout(SWT.VERTICAL));
		canvas = new FigureCanvas(getShell(), SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		// スクロールバーをツールチップに表示させない
		canvas.setHorizontalScrollBarVisibility(FigureCanvas.NEVER);
		canvas.setVerticalScrollBarVisibility(FigureCanvas.NEVER);
		root = new Layer();
		canvas.setContents(root);
		return root;
	}

	/*
	 * Calculates the location where the tooltip will be painted. Returns this
	 * as a Point. Tooltip will be painted directly below the cursor if
	 * possible, otherwise it will be painted directly above cursor.
	 */
	private Point computeWindowLocation(IFigure tip, int eventX, int eventY) {
		org.eclipse.swt.graphics.Rectangle clientArea = control.getDisplay()
				.getClientArea();
		Point preferredLocation = new Point(eventX, eventY + 26);

		Dimension tipSize = tip.getPreferredSize().getExpanded(getShellTrimSize());

		// Adjust location if tip is going to fall outside display
		if (preferredLocation.y + tipSize.height > clientArea.height)
			preferredLocation.y = eventY - tipSize.height;

		if (preferredLocation.x + tipSize.width > clientArea.width)
			preferredLocation.x -= (preferredLocation.x + tipSize.width)
					- clientArea.width;

		return preferredLocation;
	}

	/**
	 * Sets the tooltip hide delay, which is the amount in ms, after which the
	 * tooltip will disappear again.
	 * 
	 * @param hideDelay
	 *            The delay after which the tooltip is hidden again, in ms.
	 * @since 3.10
	 */
	public void setHideDelay(int hideDelay) {
		this.hideDelay = hideDelay;
	}

	/**
	 * Sets the LightWeightSystem's contents to the passed tooltip, and displays
	 * the tip. The tip will be displayed only if the tip source is different
	 * than the previously viewed tip source. (i.e. The cursor has moved off of
	 * the previous tooltip source figure.)
	 * <p>
	 * The tooltip will be painted directly below the cursor if possible,
	 * otherwise it will be painted directly above cursor.
	 * 
	 * @param hoverSource
	 *            the figure over which the hover event was fired
	 * @param tip
	 *            the tooltip to be displayed
	 * @param eventX
	 *            the x coordinate of the hover event
	 * @param eventY
	 *            the y coordinate of the hover event
	 * @since 2.0
	 */
	public void displayToolTipNear(IFigure hoverSource, IFigure tip,
			int eventX, int eventY) {
		if (tip != null && hoverSource != currentTipSource) {
			if (currentTooltip != null) {
				getRoot().removeAll();
			}

			currentTooltip = tip;
			currentTooltip.setBorder(new MarginBorder(20));
			getRoot().add(currentTooltip);
			Dimension prefSize = currentTooltip.getPreferredSize();
			currentTooltip.setSize(prefSize); 
			getRoot().setSize(prefSize);
			
			Point displayPoint = computeWindowLocation(currentTooltip, eventX, eventY);
			Dimension shellSize = prefSize.getExpanded(getShellTrimSize());
			setShellBounds(displayPoint.x, displayPoint.y, shellSize.width, shellSize.height);

			show();
			currentTipSource = hoverSource;
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				public void run() {
					control.getDisplay().asyncExec(new Runnable() {
						public void run() {
							hide();
						}
					});
				}
			}, hideDelay);
		}
	}

	/**
	 * Disposes of the tooltip's shell and kills the timer.
	 * 
	 * @see PopUpHelper#dispose()
	 */
	public void dispose() {
		if (isShowing()) {
			hide();
		}
		if (canvas != null) {
			canvas.dispose();
		}
		getShell().dispose();
	}

	protected void hide() {
		if (timer != null) {
			timer.cancel();
		}
		super.hide();
	}

	/**
	 * @see PopUpHelper#hookShellListeners()
	 */
	protected void hookShellListeners() {
		// Close the tooltip window if the mouse enters the tooltip
	    // UNSUPPORTED - api not implemented in RAP
//		getShell().addMouseTrackListener(new MouseTrackAdapter() {
//			public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
//				hide();
//				currentTipSource = null;
//			}
//		});
	}

	/**
	 * Displays the hover source's tooltip if a tooltip of another source is
	 * currently being displayed.
	 * 
	 * @param figureUnderMouse
	 *            the figure over which the cursor was when called
	 * @param tip
	 *            the tooltip to be displayed
	 * @param eventX
	 *            the x coordinate of the cursor
	 * @param eventY
	 *            the y coordinate of the cursor
	 * @since 2.0
	 */
	public void updateToolTip(IFigure figureUnderMouse, IFigure tip,
			int eventX, int eventY) {
		/*
		 * If the cursor is not on any Figures, it has been moved off of the
		 * control. Hide the tool tip.
		 */
		if (figureUnderMouse == null) {
			if (isShowing()) {
				hide();
			}
		}
		// Makes tooltip appear without a hover event if a tip is currently
		// being displayed
		if (isShowing() && figureUnderMouse != currentTipSource) {
			hide();
			displayToolTipNear(figureUnderMouse, tip, eventX, eventY);
		} else if (!isShowing() && figureUnderMouse != currentTipSource)
			currentTipSource = null;
	}

}
