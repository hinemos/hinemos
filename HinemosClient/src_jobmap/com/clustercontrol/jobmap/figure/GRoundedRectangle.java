/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.figure;

import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * 上下のグラデーションのRoundedRectangle
 */
public class GRoundedRectangle extends RoundedRectangle {
	private Color upColor = ColorConstantsWrapper.white();
	private Color downColor = ColorConstantsWrapper.white();

	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle shapeRectangle = getShapeRectangle();

		graphics.setForegroundColor(upColor);
		graphics.fillGradient(shapeRectangle, true);
		graphics.setForegroundColor(downColor);
	}

	public Color getUpColor() {
		return upColor;
	}

	public Color getDownColor() {
		return downColor;
	}

	public void setDownColor(Color color) {
		if (color != null) {
			/*
			 * upColorはdownColorと白の中間色とする。
			 * upColorを白にしてしまうと微妙なので。
			 */
			upColor = new Color(null,
					(downColor.getRed() + 255) / 2,
					(downColor.getGreen() + 255) / 2,
					(downColor.getBlue() + 255) / 2);

			downColor = color;
		}
	}

	private Rectangle getShapeRectangle() {
		Rectangle shapeRectangle = new Rectangle();
		Rectangle bounds = getBounds();
		shapeRectangle.x = bounds.x + getLineWidth() / 2;
		shapeRectangle.y = bounds.y + getLineWidth() / 2;
		shapeRectangle.width = bounds.width - getLineWidth();
		shapeRectangle.height = bounds.height - getLineWidth();

		return shapeRectangle;
	}
}