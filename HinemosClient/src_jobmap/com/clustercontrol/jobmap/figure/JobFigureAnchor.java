/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.figure;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class JobFigureAnchor extends AbstractConnectionAnchor {

	private int type;
	private boolean xyChange;
	
	public static final int TYPE_SOURCE = 0;
	public static final int TYPE_TARGET = 1;
	
	public JobFigureAnchor(JobFigure figure, int type, boolean xyChange) {
		super(figure);
		this.type = type;
		this.xyChange = xyChange;
	}
	
	@Override
	public Point getLocation(Point reference) {
		Rectangle r = new Rectangle();
		r.setBounds(getBox().getBounds());
		r.translate(-1, -1);
		r.resize(1, 1);

		getOwner().translateToAbsolute(r);

		int resultX;
		int resultY;
		
		if (xyChange) {
			if (type == TYPE_TARGET) {
				resultX = r.x;
			} else {
				resultX = r.x + r.width;
			}
			
			resultY = r.y + (r.height / 2);
		} else {
			resultX = r.x + (r.width / 2);
			
			if (type == TYPE_TARGET) {
				resultY = r.y;
			} else {
				resultY = r.y + r.height;
			}
		}
		

		return new Point(resultX, resultY);
	}
	
	@Override
	public Point getReferencePoint() {
		return getLocation(null);
	}
	
	/**
	 * Returns the bounds of this ChopboxAnchor's owner. Subclasses can override
	 * this method to adjust the box the anchor can be placed on. For instance,
	 * the owner figure may have a drop shadow that should not be included in
	 * the box.
	 * 
	 * @return The bounds of this ChopboxAnchor's owner
	 * @since 2.0
	 */
	protected GRoundedRectangle getBox() {
		return ((JobFigure)getOwner()).getBackground();
	}

}
