/*
Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */

package com.clustercontrol.startup.figure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.startup.bean.StartUpItem;
import com.clustercontrol.startup.composite.StartUpComposite;

/**
 * アイコン(ノード)画像のクラス
 * @since 1.0.0
 */
public class StartUpFigure extends Figure {

	// ログ
	private static Log m_log = LogFactory.getLog( StartUpFigure.class );

	// 基本色
	private static final int tmpColor = 128;
	public static final Color red = new Color (null, 255, tmpColor, tmpColor);
	public static final Color green = new Color (null, tmpColor, 255, tmpColor);
	public static final Color blue = new Color (null, tmpColor, tmpColor, 255);
	public static final Color yellow = new Color (null, 255, 255, tmpColor);
	public static final Color lightgray = new Color ( null, 200, 200, 200);
	public static final Color navy = new Color(null, 0, 63, 133);

	public static final int textWidth = 384;
	public static final int textHeight = 96;
	public static final int lineWidth = 2;

	private Color foreColor = null;
	private Color backColor = null;

	// 3つのレイヤーで構成する。
	// layerStackの上にlayerToolbar。その上にlayerXY。
	private Layer m_layerStack; //背景
	private Layer m_layerBorder; //背景
	private Layer m_layerIcon;

	private int nest = 0;

	public GRoundedRectangle m_backGround;

	private final Rectangle zeroRectangle;

	public StartUpFigure(){
		this.setFocusTraversable(true);
		this.setRequestFocusEnabled(true);
		// 背景色を白に設定する
		this.setBackgroundColor(ColorConstantsWrapper.lightGray());

		Dimension dimension = new Dimension(-1, -1);
		Point point = new Point(0, 0);
		zeroRectangle = new Rectangle(point, dimension);
	}

	public void draw(StartUpItem startUpItem, int nest) {
		this.nest = nest;

		// レイアウトを設定する
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		layout.setStretchMinorAxis(false);
		this.setLayoutManager(layout);

		// 背景を作成する
		// m_layerStackは一番下のレイヤー。丸い四角を描画。
		m_layerStack = new Layer();
		m_layerStack.setLayoutManager(new StackLayout());
		m_layerStack.setPreferredSize(startUpItem.getWidth(), startUpItem.getHeight());

		m_backGround = new GRoundedRectangle();
		m_backGround.setSize(startUpItem.getWidth(), startUpItem.getHeight());
		m_layerStack.add(m_backGround);

		// m_layerBorderは真ん中のレイヤー。説明文を描画
		m_layerBorder = new Layer();
		BorderLayout blayout = new BorderLayout();
		blayout.setVerticalSpacing(-10);
		m_layerBorder.setSize(startUpItem.getWidth(), startUpItem.getHeight());
		m_layerBorder.setLayoutManager(blayout);

		Label label = null;
		String msg = startUpItem.getMsgTextTop();
		if(ClusterControlPlugin.isRAP()) {
			msg = "\n" + startUpItem.getMsgTextTop();
		}
		label = new Label(msg);
		label.setLabelAlignment(Label.CENTER);
		label.setFont(StartUpComposite.topLabelFont);
		label.setForegroundColor(navy);
		m_layerBorder.add(label, BorderLayout.TOP);

		label = new Label();
		label.setText(startUpItem.getIconSpace());
		label.setTextAlignment(Label.RIGHT);
		label.setIcon(startUpItem.getImage());
		m_layerBorder.add(label, BorderLayout.RIGHT);

		label = new Label(startUpItem.getMsgTextBottom());
		label.setLabelAlignment(Label.CENTER);
		label.setTextAlignment(Label.LEFT);
		label.setFont(StartUpComposite.bottomLabelFont);
		label.setForegroundColor(new Color(null, 145, 151, 195));
		m_layerBorder.add(label, BorderLayout.CENTER);

		m_layerStack.add(m_layerBorder);

		//矢印アイコン
		this.setNest(nest);
		if(startUpItem.getImageFigure() != null) {
			m_layerIcon = new Layer();
			m_layerIcon.setLayoutManager(new StackLayout());
			m_layerIcon.setPreferredSize(startUpItem.getImageFigure()
					.getPreferredSize().width, startUpItem.getImageFigure()
					.getPreferredSize().height);
			m_layerIcon.add(startUpItem.getImageFigure());
			this.add(m_layerIcon);
			this.setConstraint(m_layerIcon, zeroRectangle);
			this.setMaximumSize(startUpItem.getImageFigure().getSize());
		}

		// 色を設定
		setBgColor(startUpItem.getColor());

		// ツールチップを生成
		this.setToolTip(getTooltip(startUpItem));

		this.setNest(nest);
		this.add(m_layerStack);
		this.setConstraint(m_layerStack, zeroRectangle);
		this.setMaximumSize(new Dimension (textWidth, textHeight));
	}

	public void setBgColor(Color color){
		if(m_backGround == null) {
			m_log.debug("setBgColor : m_backGround is null");
			return;
		}

		backColor = color;
		foreColor = navy;

		m_backGround.setLineWidth(lineWidth);
		resetColor();
	}

	/**
	 *  マウスが上に来たら、色を変える。
	 */
	public void changeColor() {
		double rate = 1.2;
		Color fColor = new Color(null,
				(int)(255 < rate * foreColor.getRed() ? 255 : rate * foreColor.getRed()),
				(int)(255 < rate * foreColor.getGreen() ? 255 : rate * foreColor.getGreen()),
				(int)(255 < rate * foreColor.getBlue() ? 255 : rate * foreColor.getBlue()));
		Color bColor = new Color(null,
				(int)(255 < rate * backColor.getRed() ? 255 : rate * backColor.getRed()),
				(int)(255 < rate * backColor.getGreen() ? 255 : rate * backColor.getGreen()),
				(int)(255 < rate * backColor.getBlue() ? 255 : rate * backColor.getBlue()));
		m_backGround.setForegroundColor(fColor); // ここは利用されないが、一応設定しておく。
		m_backGround.setDownColor(fColor);
		m_backGround.setBackgroundColor(bColor); // 枠の色。
	}

	/**
	 * マウスが離れたら、色を戻す。
	 */
	public void resetColor() {
		/*
		 * 枠は細く。
		 * 中身はpriority色。
		 */
		m_backGround.setForegroundColor(foreColor); // ここは利用されないが、一応設定しておく。
		m_backGround.setDownColor(foreColor);
		m_backGround.setBackgroundColor(backColor); // 枠の色。
	}

	@Override
	public void repaint(){
		super.repaint();
	}

	/**
	 *  ツールチップに内容を設定
	 * @return
	 */
	private Panel getTooltip(StartUpItem startUpItem){
		Panel tooltip = new Panel();
		tooltip.setLayoutManager(new FlowLayout(false));

		tooltip.add(new Label(startUpItem.getToolTipText()));

		return tooltip;
	}

	public RoundedRectangle getBackGround() {
		return m_backGround;
	}

	public int getNest() {
		return nest;
	}

	public void setNest(int nest) {
		this.nest = nest;
	}

	/**
	 * 上下のグラデーションのRoundedRectangle
	 */
	private static class GRoundedRectangle extends RoundedRectangle {
		private Color upColor = ColorConstantsWrapper.white();
		private Color downColor = ColorConstantsWrapper.white();

		@Override
		protected void fillShape(Graphics graphics) {
			Rectangle shapeRectangle = getShapeRectangle();

			graphics.setForegroundColor(upColor);
			graphics.fillGradient(shapeRectangle, true);
			graphics.setForegroundColor(downColor);
		}

		public void setDownColor(Color color) {
			if (color != null) {
				upColor = new Color(null, 255, 255, 255);
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
}
