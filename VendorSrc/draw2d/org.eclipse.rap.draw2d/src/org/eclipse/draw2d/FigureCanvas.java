/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Austin Riddle (Texas Center for Applied Technology) - initial RAP port
 *******************************************************************************/
package org.eclipse.draw2d;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Slider;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A scrolling Canvas that contains {@link Figure Figures} viewed through a
 * {@link Viewport}. Call {@link #setContents(IFigure)} to specify the root of
 * the tree of <tt>Figures</tt> to be viewed through the <tt>Viewport</tt>.
 * <p>
 * Normal procedure for using a FigureCanvas:
 * <ol>
 * <li>Create a FigureCanvas.
 * <li>Create a Draw2d Figure and call {@link #setContents(IFigure)}. This
 * Figure will be the top-level Figure of the Draw2d application.
 * </ol>
 * <dl>
 * <dt><b>Required Styles (when using certain constructors):</b></dt>
 * <dd>V_SCROLL, H_SCROLL, NO_REDRAW_RESIZE</dd>
 * <dt><b>Optional Styles:</b></dt>
 * <dd>DOUBLE_BUFFERED, RIGHT_TO_LEFT, LEFT_TO_RIGHT, NO_BACKGROUND, BORDER</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles RIGHT_TO_LEFT, LEFT_TO_RIGHT may be specified.
 * </p>
 */
public class FigureCanvas extends Composite {

	private static final int ACCEPTED_STYLES = org.eclipse.draw2d.rap.swt.SWT.RIGHT_TO_LEFT
			| SWT.LEFT_TO_RIGHT
			| SWT.V_SCROLL
			| SWT.H_SCROLL
			| SWT.NO_BACKGROUND
			| SWT.NO_REDRAW_RESIZE
			| SWT.DOUBLE_BUFFERED
			| SWT.BORDER;

	/**
	 * The default styles are mixed in when certain constructors are used. This
	 * constant is a bitwise OR of the following SWT style constants:
	 * <UL>
	 * <LI>{@link SWT#NO_REDRAW_RESIZE}</LI>
	 * <LI>{@link SWT#NO_BACKGROUND}</LI>
	 * <LI>{@link SWT#V_SCROLL}</LI>
	 * <LI>{@link SWT#H_SCROLL}</LI>
	 * </UL>
	 */
	static final int DEFAULT_STYLES = SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND
			| SWT.V_SCROLL | SWT.H_SCROLL;

	private static final int REQUIRED_STYLES = SWT.NO_REDRAW_RESIZE
			| SWT.V_SCROLL | SWT.H_SCROLL;

	/** Never show scrollbar */
	public static int NEVER = 0;
	/** Automatically show scrollbar when needed */
	public static int AUTOMATIC = 1;
	/** Always show scrollbar */
	public static int ALWAYS = 2;

	protected Slider horizontalBar, verticalBar;
	protected Canvas innerCanvas;

	private int vBarVisibility = AUTOMATIC;
	private int hBarVisibility = AUTOMATIC;
	private Viewport viewport;
	private Font font;
	private int hBarOffset;
	private int vBarOffset;

	private PropertyChangeListener horizontalChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			RangeModel model = getViewport().getHorizontalRangeModel();
			hBarOffset = Math.max(0, -model.getMinimum());
			Slider horizontalBar = getHorizontalSlider();
			if (horizontalBar != null) {
				horizontalBar.setValues(model.getValue() + hBarOffset,
						model.getMinimum() + hBarOffset, model.getMaximum()
								+ hBarOffset, model.getExtent(),
						Math.max(1, model.getExtent() / 20),
						Math.max(1, model.getExtent() * 3 / 4));
			}
		}
	};

	private PropertyChangeListener verticalChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			RangeModel model = getViewport().getVerticalRangeModel();
			vBarOffset = Math.max(0, -model.getMinimum());
			Slider verticalBar = getVerticalSlider();
			if (verticalBar != null) {
				verticalBar.setValues(model.getValue() + vBarOffset,
						model.getMinimum() + vBarOffset, model.getMaximum()
								+ vBarOffset, model.getExtent(),
						Math.max(1, model.getExtent() / 20),
						Math.max(1, model.getExtent() * 3 / 4));
			}
		}
	};

	private final LightweightSystem lws;

	/**
	 * Creates a new FigureCanvas with the given parent and the
	 * {@link #DEFAULT_STYLES}.
	 * 
	 * @param parent
	 *            the parent
	 */
	public FigureCanvas(Composite parent) {
		this(parent, SWT.DOUBLE_BUFFERED, new LightweightSystem());
	}

	/**
	 * Constructor which applies the default styles plus any optional styles
	 * indicated.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            see the class javadoc for optional styles
	 * @since 3.1
	 */
	public FigureCanvas(Composite parent, int style) {
		this(parent, style, new LightweightSystem());
	}

	/**
	 * Constructor which uses the given styles verbatim. Certain styles must be
	 * used with this class. Refer to the class javadoc for more details.
	 * 
	 * @param style
	 *            see the class javadoc for <b>required</b> and optional styles
	 * @param parent
	 *            the parent composite
	 * @since 3.4
	 */
	public FigureCanvas(int style, Composite parent) {
		this(style, parent, new LightweightSystem());
	}

	/**
	 * Constructs a new FigureCanvas with the given parent and
	 * LightweightSystem, using the {@link #DEFAULT_STYLES}.
	 * 
	 * @param parent
	 *            the parent
	 * @param lws
	 *            the LightweightSystem
	 */
	public FigureCanvas(Composite parent, LightweightSystem lws) {
		this(parent, SWT.DOUBLE_BUFFERED, lws);
	}

	/**
	 * Constructor taking a lightweight system and SWT style, which is used
	 * verbatim. Certain styles must be used with this class. Refer to the class
	 * javadoc for more details.
	 * 
	 * @param style
	 *            see the class javadoc for <b>required</b> and optional styles
	 * @param parent
	 *            the parent composite
	 * @param lws
	 *            the LightweightSystem
	 * @since 3.4
	 */
	public FigureCanvas(int style, Composite parent, LightweightSystem lws) {
		super(parent, checkStyle(style));
		setLayout(new Layout() {
			protected org.eclipse.swt.graphics.Point computeSize(
					Composite composite, int wHint, int hHint,
					boolean flushCache) {
				return innerCanvas.computeSize(wHint, hHint, flushCache);
			}

			protected void layout(Composite composite, boolean flushCache) {
				org.eclipse.swt.graphics.Point size = composite.getSize();
				int vScrollBarWidth = getVScrollBarWidth();
				int hScrollBarHeight = getHScrollBarHeight();
				if (!getHorizontalSlider().isVisible()) {
					hScrollBarHeight = 0;
				}
				if (!getVerticalSlider().isVisible()) {
					vScrollBarWidth = 0;
				}
				getHorizontalSlider().setBounds(0, size.y - hScrollBarHeight,
						size.x - vScrollBarWidth, hScrollBarHeight);
				getVerticalSlider().setBounds(size.x - vScrollBarWidth, 0,
						vScrollBarWidth, size.y - hScrollBarHeight);
				innerCanvas.setLocation(0, 0);
				innerCanvas.setSize(size.x - vScrollBarWidth, size.y
						- hScrollBarHeight);
			}
		});
		Slider horizontalBar = getHorizontalSlider();
		if (horizontalBar != null) {
			horizontalBar.setVisible(false);
		}
		Slider verticalBar = getVerticalSlider();
		if (verticalBar != null) {
			verticalBar.setVisible(false);
		}
		this.lws = lws;
		lws.setControl(getInnerCanvas());
		hook();
	}

	
	@Override
	public void dispose() {
		super.dispose();
		lws.close();
	};
	
	public org.eclipse.swt.graphics.Rectangle getClientArea() {
		return innerCanvas.getClientArea();
	}

	private Canvas getInnerCanvas() {
		if (innerCanvas == null) {
			innerCanvas = new Canvas(this, getStyle());
			this.setData(Canvas.class.getName(), innerCanvas);
		}
		return innerCanvas;
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            look at class javadoc for valid styles
	 * @param lws
	 *            the lightweight system
	 * @since 3.1
	 */
	public FigureCanvas(Composite parent, int style, LightweightSystem lws) {
		this(style | DEFAULT_STYLES, parent, lws);
	}

	private static int checkStyle(int style) {
		if ((style & REQUIRED_STYLES) != REQUIRED_STYLES)
			throw new IllegalArgumentException(
					"Required style missing on FigureCanvas"); //$NON-NLS-1$
		if ((style & ~ACCEPTED_STYLES) != 0)
			throw new IllegalArgumentException(
					"Invalid style being set on FigureCanvas"); //$NON-NLS-1$
		return style;
	}

	public void addFocusListener(FocusListener listener) {
		super.addFocusListener(listener);
		getInnerCanvas().addFocusListener(listener);
	}

	public void addDragDetectListener(DragDetectListener listener) {
		super.addDragDetectListener(listener);
		getInnerCanvas().addDragDetectListener(listener);
	}

	public void addMenuDetectListener(MenuDetectListener listener) {
		super.addMenuDetectListener(listener);
		getInnerCanvas().addMenuDetectListener(listener);
	}

	public void addKeyListener(KeyListener listener) {
		super.addKeyListener(listener);
		getInnerCanvas().addKeyListener(listener);
	}

	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
		getInnerCanvas().addListener(eventType, listener);
	}

	public void removeFocusListener(FocusListener listener) {
		super.removeFocusListener(listener);
		getInnerCanvas().removeFocusListener(listener);
	}

	public void removeDragDetectListener(DragDetectListener listener) {
		super.removeDragDetectListener(listener);
		getInnerCanvas().removeDragDetectListener(listener);
	}

	public void removeMenuDetectListener(MenuDetectListener listener) {
		super.removeMenuDetectListener(listener);
		getInnerCanvas().removeMenuDetectListener(listener);
	}

	public void removeKeyListener(KeyListener listener) {
		super.removeKeyListener(listener);
		getInnerCanvas().removeKeyListener(listener);
	}

	public void removeListener(int eventType, Listener listener) {
		super.removeListener(eventType, listener);
		getInnerCanvas().removeListener(eventType, listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	public org.eclipse.swt.graphics.Point computeSize(int wHint, int hHint,
			boolean changed) {
		// TODO Still doesn't handle scrollbar cases, such as when a constrained
		// width
		// would require a horizontal scrollbar, and therefore additional
		// height.
		int borderSize = computeTrim(0, 0, 0, 0).x * -2;
		if (wHint >= 0)
			wHint = Math.max(0, wHint - borderSize);
		if (hHint >= 0)
			hHint = Math.max(0, hHint - borderSize);
		Dimension size = getLightweightSystem().getRootFigure()
				.getPreferredSize(wHint, hHint)
				.getExpanded(borderSize, borderSize);
		size.union(new Dimension(wHint, hHint));
		return new org.eclipse.swt.graphics.Point(size.width, size.height);
	}

	/**
	 * @return the contents of the {@link Viewport}.
	 */
	public IFigure getContents() {
		return getViewport().getContents();
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#getFont()
	 */
	public Font getFont() {
		if (font == null)
			font = super.getFont();
		return font;
	}

	/**
	 * @return the horizontal scrollbar visibility.
	 */
	public int getHorizontalScrollBarVisibility() {
		return hBarVisibility;
	}

	int getVScrollBarWidth() {
		return 16;
	}

	int getHScrollBarHeight() {
		return 16;
	}

	/**
	 * @return the LightweightSystem
	 */
	public LightweightSystem getLightweightSystem() {
		return lws;
	}

	/**
	 * @return the vertical scrollbar visibility.
	 */
	public int getVerticalScrollBarVisibility() {
		return vBarVisibility;
	}

	/**
	 * Returns the Viewport. If it's <code>null</code>, a new one is created.
	 * 
	 * @return the viewport
	 */
	public Viewport getViewport() {
		if (viewport == null)
			setViewport(new Viewport(true));
		return viewport;
	}

	/**
	 * Adds listeners for scrolling.
	 */
	private void hook() {
		getLightweightSystem().getUpdateManager().addUpdateListener(
				new UpdateListener() {
					public void notifyPainting(Rectangle damage,
							java.util.Map dirtyRegions) {
					}

					public void notifyValidating() {
						if (!isDisposed())
							layoutViewport();
					}
				});

		final Slider horizontalBar = getHorizontalSlider();
		if (horizontalBar != null) {
			horizontalBar.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					scrollToX(horizontalBar.getSelection() - hBarOffset);
				}
			});
		}

		final Slider verticalBar = getVerticalSlider();
		if (verticalBar != null) {
			verticalBar.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					scrollToY(verticalBar.getSelection() - vBarOffset);
				}
			});
		}
	}

	private void hookViewport() {
		getViewport().getHorizontalRangeModel().addPropertyChangeListener(
				horizontalChangeListener);
		getViewport().getVerticalRangeModel().addPropertyChangeListener(
				verticalChangeListener);
	}

	private void unhookViewport() {
		getViewport().getHorizontalRangeModel().removePropertyChangeListener(
				horizontalChangeListener);
		getViewport().getVerticalRangeModel().removePropertyChangeListener(
				verticalChangeListener);
	}

	private void layoutViewport() {
		ScrollPaneSolver.Result result;
		result = ScrollPaneSolver.solve(
				new Rectangle(innerCanvas.getBounds()).setLocation(0, 0),
				getViewport(), getHorizontalScrollBarVisibility(),
				getVerticalScrollBarVisibility(),
				innerCanvas.computeTrim(0, 0, 0, 0).width,
				innerCanvas.computeTrim(0, 0, 0, 0).height);
		getLightweightSystem().setIgnoreResize(true);
		try {
			Slider horizontalBar = getHorizontalSlider();
			if (horizontalBar != null
					&& horizontalBar.getVisible() != result.showH) {
				horizontalBar.setVisible(result.showH);
				layout(new Control[] { horizontalBar });
			}
			Slider verticalBar = getVerticalSlider();
			if (verticalBar != null && verticalBar.getVisible() != result.showV) {
				verticalBar.setVisible(result.showV);
				layout(new Control[] { verticalBar });
			}
			Rectangle r = new Rectangle(getClientArea());
			r.setLocation(0, 0);
			getLightweightSystem().getRootFigure().setBounds(r);
		} finally {
			getLightweightSystem().setIgnoreResize(false);
		}
	}

	/**
	 * Scrolls in an animated way to the new x and y location.
	 * 
	 * @param x
	 *            the x coordinate to scroll to
	 * @param y
	 *            the y coordinate to scroll to
	 */
	public void scrollSmoothTo(int x, int y) {
		// Ensure newHOffset and newVOffset are within the appropriate ranges
		x = verifyScrollBarOffset(getViewport().getHorizontalRangeModel(), x);
		y = verifyScrollBarOffset(getViewport().getVerticalRangeModel(), y);

		int oldX = getViewport().getViewLocation().x;
		int oldY = getViewport().getViewLocation().y;
		int dx = x - oldX;
		int dy = y - oldY;

		if (dx == 0 && dy == 0)
			return; // Nothing to do.

		Dimension viewingArea = getViewport().getClientArea().getSize();

		int minFrames = 3;
		int maxFrames = 6;
		if (dx == 0 || dy == 0) {
			minFrames = 6;
			maxFrames = 13;
		}
		int frames = (Math.abs(dx) + Math.abs(dy)) / 15;
		frames = Math.max(frames, minFrames);
		frames = Math.min(frames, maxFrames);

		int stepX = Math.min((dx / frames), (viewingArea.width / 3));
		int stepY = Math.min((dy / frames), (viewingArea.height / 3));

		for (int i = 1; i < frames; i++) {
			scrollTo(oldX + i * stepX, oldY + i * stepY);
			getViewport().getUpdateManager().performUpdate();
		}
		scrollTo(x, y);
	}

	/**
	 * Scrolls the contents to the new x and y location. If this scroll
	 * operation only consists of a vertical or horizontal scroll, a call will
	 * be made to {@link #scrollToY(int)} or {@link #scrollToX(int)},
	 * respectively, to increase performance.
	 * 
	 * @param x
	 *            the x coordinate to scroll to
	 * @param y
	 *            the y coordinate to scroll to
	 */
	public void scrollTo(int x, int y) {
		x = verifyScrollBarOffset(getViewport().getHorizontalRangeModel(), x);
		y = verifyScrollBarOffset(getViewport().getVerticalRangeModel(), y);
		if (x == getViewport().getViewLocation().x)
			scrollToY(y);
		else if (y == getViewport().getViewLocation().y)
			scrollToX(x);
		else
			getViewport().setViewLocation(x, y);
	}

	/**
	 * Scrolls the contents horizontally so that they are offset by
	 * <code>hOffset</code>.
	 * 
	 * @param hOffset
	 *            the new horizontal offset
	 */
	public void scrollToX(int hOffset) {
		hOffset = verifyScrollBarOffset(
				getViewport().getHorizontalRangeModel(), hOffset);
		int hOffsetOld = getViewport().getViewLocation().x;
		if (hOffset == hOffsetOld)
			return;
		int dx = -hOffset + hOffsetOld;

		Rectangle clientArea = getViewport().getBounds().getCropped(
				getViewport().getInsets());
		Rectangle blit = clientArea.getResized(-Math.abs(dx), 0);
		Rectangle expose = clientArea.getCopy();
		Point dest = clientArea.getTopLeft();
		expose.width = Math.abs(dx);
		if (dx < 0) { // Moving left?
			blit.translate(-dx, 0); // Move blit area to the right
			expose.x = dest.x + blit.width;
		} else
			// Moving right
			dest.x += dx; // Move expose area to the right

		// fix for bug 41111
		Control[] children = getInnerCanvas().getChildren();
		boolean[] manualMove = new boolean[children.length];
		for (int i = 0; i < children.length; i++) {
			org.eclipse.swt.graphics.Rectangle bounds = children[i].getBounds();
			manualMove[i] = blit.width <= 0 || bounds.x > blit.x + blit.width
					|| bounds.y > blit.y + blit.height
					|| bounds.x + bounds.width < blit.x
					|| bounds.y + bounds.height < blit.y;
		}
		scroll(dest.x, dest.y, blit.x, blit.y, blit.width, blit.height, true);
		for (int i = 0; i < children.length; i++) {
			if (children[i].isDisposed())
				continue;
			org.eclipse.swt.graphics.Rectangle bounds = children[i].getBounds();
			if (manualMove[i])
				children[i].setBounds(bounds.x + dx, bounds.y, bounds.width,
						bounds.height);
		}

		getViewport().setIgnoreScroll(true);
		getViewport().setHorizontalLocation(hOffset);
		getViewport().setIgnoreScroll(false);
		getInnerCanvas().redraw(expose.x, expose.y, expose.width,
				expose.height, true);
	}

	/**
	 * Scrolls the contents vertically so that they are offset by
	 * <code>vOffset</code>.
	 * 
	 * @param vOffset
	 *            the new vertical offset
	 */
	public void scrollToY(int vOffset) {
		vOffset = verifyScrollBarOffset(getViewport().getVerticalRangeModel(),
				vOffset);
		int vOffsetOld = getViewport().getViewLocation().y;
		if (vOffset == vOffsetOld)
			return;
		int dy = -vOffset + vOffsetOld;

		Rectangle clientArea = getViewport().getBounds().getCropped(
				getViewport().getInsets());
		Rectangle blit = clientArea.getResized(0, -Math.abs(dy));
		Rectangle expose = clientArea.getCopy();
		Point dest = clientArea.getTopLeft();
		expose.height = Math.abs(dy);
		if (dy < 0) { // Moving up?
			blit.translate(0, -dy); // Move blit area down
			expose.y = dest.y + blit.height; // Move expose area down
		} else
			// Moving down
			dest.y += dy;

		// fix for bug 41111
		Control[] children = getInnerCanvas().getChildren();
		boolean[] manualMove = new boolean[children.length];
		for (int i = 0; i < children.length; i++) {
			org.eclipse.swt.graphics.Rectangle bounds = children[i].getBounds();
			manualMove[i] = blit.height <= 0 || bounds.x > blit.x + blit.width
					|| bounds.y > blit.y + blit.height
					|| bounds.x + bounds.width < blit.x
					|| bounds.y + bounds.height < blit.y;
		}
		scroll(dest.x, dest.y, blit.x, blit.y, blit.width, blit.height, true);
		for (int i = 0; i < children.length; i++) {
			if (children[i].isDisposed())
				continue;
			org.eclipse.swt.graphics.Rectangle bounds = children[i].getBounds();
			if (manualMove[i])
				children[i].setBounds(bounds.x, bounds.y + dy, bounds.width,
						bounds.height);
		}

		getViewport().setIgnoreScroll(true);
		getViewport().setVerticalLocation(vOffset);
		getViewport().setIgnoreScroll(false);
		getInnerCanvas().redraw(expose.x, expose.y, expose.width,
				expose.height, true);
	}

	public void scroll(int destX, int destY, int x, int y, int width,
			int height, boolean all) {
		checkWidget();
		int deltaX = destX - x, deltaY = destY - y;
		if (all) {
			Control[] children = getInnerCanvas().getChildren();
			for (int i = 0; i < children.length; i++) {
				Control child = children[i];
				org.eclipse.swt.graphics.Rectangle rect = child.getBounds();
				if (Math.min(x + width, rect.x + rect.width) >= Math.max(x,
						rect.x)
						&& Math.min(y + height, rect.y + rect.height) >= Math
								.max(y, rect.y)) {
					child.setLocation(rect.x + deltaX, rect.y + deltaY);
				}
			}
		}
	}

	/**
	 * Sets the given border on the LightweightSystem's root figure.
	 * 
	 * @param border
	 *            The new border
	 */
	public void setBorder(Border border) {
		getLightweightSystem().getRootFigure().setBorder(border);
	}

	/**
	 * Sets the contents of the {@link Viewport}.
	 * 
	 * @param figure
	 *            the new contents
	 */
	public void setContents(IFigure figure) {
		getViewport().setContents(figure);
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font font) {
		this.font = font;
		super.setFont(font);
	}

	public void setMenu(Menu menu) {
		super.setMenu(menu);
		getInnerCanvas().setMenu(menu);
	}

	/**
	 * Sets the horizontal scrollbar visibility. Possible values are
	 * {@link #AUTOMATIC}, {@link #ALWAYS}, and {@link #NEVER}.
	 * 
	 * @param v
	 *            the new visibility
	 */
	public void setHorizontalScrollBarVisibility(int v) {
		hBarVisibility = v;
	}

	/**
	 * Sets both the horizontal and vertical scrollbar visibility to the given
	 * value. Possible values are {@link #AUTOMATIC}, {@link #ALWAYS}, and
	 * {@link #NEVER}.
	 * 
	 * @param both
	 *            the new visibility
	 */
	public void setScrollBarVisibility(int both) {
		setHorizontalScrollBarVisibility(both);
		setVerticalScrollBarVisibility(both);
	}

	/**
	 * Sets the vertical scrollbar visibility. Possible values are
	 * {@link #AUTOMATIC}, {@link #ALWAYS}, and {@link #NEVER}.
	 * 
	 * @param v
	 *            the new visibility
	 */
	public void setVerticalScrollBarVisibility(int v) {
		vBarVisibility = v;
	}

	/**
	 * Sets the Viewport. The given Viewport must use "fake" scrolling. That is,
	 * it must be constructed using <code>new Viewport(true)</code>.
	 * 
	 * @param vp
	 *            the new viewport
	 */
	public void setViewport(Viewport vp) {
		if (viewport != null)
			unhookViewport();
		viewport = vp;
		lws.setContents(viewport);
		hookViewport();
	}

	private int verifyScrollBarOffset(RangeModel model, int value) {
		value = Math.max(model.getMinimum(), value);
		return Math.min(model.getMaximum() - model.getExtent(), value);
	}

	public Slider getHorizontalSlider() {
		checkWidget();
		if (horizontalBar == null) {
			horizontalBar = new Slider(this, SWT.HORIZONTAL);
			horizontalBar.setMaximum(100);
			horizontalBar.setThumb(10);
		}
		return horizontalBar;
	}

	public Slider getVerticalSlider() {
		checkWidget();
		if (verticalBar == null) {
			verticalBar = new Slider(this, SWT.VERTICAL);
			verticalBar.setMaximum(100);
			verticalBar.setThumb(10);
		}
		return verticalBar;
	}

}
