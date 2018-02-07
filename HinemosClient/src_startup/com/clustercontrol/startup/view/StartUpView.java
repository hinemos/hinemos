/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.startup.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.startup.bean.StartUpItem;
import com.clustercontrol.startup.composite.StartUpComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.CommonViewPart;

public class StartUpView extends CommonViewPart {
	static {
		JFaceResources.getColorRegistry().put("StartUpView_Background", new RGB(224, 226, 237));
	}
	
	/** ビューID */
	public static final String ID = StartUpView.class.getName();

	/** startUpCompositeコンポジット*/
	private StartUpComposite startUpComposite = null;

	// 描画対象のマップの情報を保持したモデル
	private List<StartUpItem> m_startUpItem = null;

	protected String getViewName() {
		return this.getClass().getName();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		setStartUpItem();
		parent.setLayout(new GridLayout());

		// 背景色を合わせておく
		parent.setBackground(JFaceResources.getColorRegistry().get("StartUpView_Background"));
		// SWT.INHERIT_DEFAULTにしないと、RAP版だけ白い枠が表示される
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);

		startUpComposite = new StartUpComposite(parent, SWT.NO_SCROLL, m_startUpItem);
		WidgetTestUtil.setTestId(this, null, startUpComposite);

		// ビューの更新
		startUpComposite.update();
		startUpComposite.setVisible(true);
	}

	/**
	 * 各種基本設定
	 */
	public void setStartUpItem() {
		StartUpItem startUpItem = new StartUpItem();
		m_startUpItem = new ArrayList<StartUpItem>();

		int height = 96;
		int width = 384;
		int xStart = 50;
		int xDiff = 200;
		int yStart = 100;
		int yDiff = 150;
		int posYupper = yStart + yDiff + 20;
		int posYlower = yStart + 2 * yDiff + 40;
		int posXLeft = xStart;
		int posXRight = xStart + 2 * xDiff;
		int posXyajirushiLeft = xStart + 230;
		int posXyajirushiRight = xStart + 500;
		int posYyajirushiUpper = yStart + 106;
		int posYyajirushiLower = yStart + 276;

		startUpItem.setFieldId("fieldid1");
		startUpItem.setTarget(null);
		startUpItem.setPosX(0);
		startUpItem.setPosY(0);
		startUpItem.setMsgTextTop(Messages.getString("startup.title"));
		startUpItem.setMsgTextBottom(null);
		startUpItem.setToolTipText(null);
		startUpItem.setPerspectiveName(null);
		m_startUpItem.add(startUpItem);

		//タイトルライン
		startUpItem = new StartUpItem();
		startUpItem.setFieldId("line");
		Image image = loadImage("line.png");
		if(image != null) {
			ImageFigure imageFigure = new ImageFigure(image);
			startUpItem.setImageFigure(imageFigure);
			startUpItem.setPosX(5);
			startUpItem.setPosY(50);
			m_startUpItem.add(startUpItem);
		}

		startUpItem = new StartUpItem();
		startUpItem.setFieldId("fieldid2");
		startUpItem.setTarget(null);
		startUpItem.setPosX(xStart + 150);
		startUpItem.setPosY(yStart);
		startUpItem.setWidth(500);
		startUpItem.setHeight(height);
		image = loadImage("startup_repository.png");
		startUpItem.setImage(image);
		startUpItem.setIconSpace("     ");
		startUpItem.setMsgTextTop(Messages.getString("startup.toplabel01"));
		startUpItem.setMsgTextBottom(Messages.getString("startup.bottomlabel01"));
		startUpItem.setToolTipText(Messages.getString("startup.tiplabel01"));
		startUpItem.setPerspectiveName(Messages.getString("startup.pname01"));
		startUpItem.setColor(ColorConstantsWrapper.white());
		m_startUpItem.add(startUpItem);

		//キャラクタアイコン
		startUpItem = new StartUpItem();
		startUpItem.setFieldId("charactor");
		image = loadImage("hinemos_01.png");
		if(image != null) {
			ImageFigure imageFigure = new ImageFigure(image);
			startUpItem.setImageFigure(imageFigure);
			startUpItem.setPosX(xStart + 680);
			startUpItem.setPosY(80);
			m_startUpItem.add(startUpItem);
		}

		//矢印アイコン1
		startUpItem = new StartUpItem();
		startUpItem.setFieldId("yajirushi1");
		image = loadImage("yajirushi.png");
		if(image != null) {
			ImageFigure imageFigure = new ImageFigure(image);
			startUpItem.setImageFigure(imageFigure);
			startUpItem.setPosX(posXyajirushiLeft);
			startUpItem.setPosY(posYyajirushiUpper);
			m_startUpItem.add(startUpItem);
		}

		startUpItem = new StartUpItem();
		startUpItem.setFieldId("fieldid3");
		startUpItem.setTarget("fieldid2");
		startUpItem.setPosX(posXLeft);
		startUpItem.setPosY(posYupper);
		startUpItem.setWidth(width);
		startUpItem.setHeight(height);
		image = loadImage("startup_monitor_setting.png");
		startUpItem.setImage(image);
		startUpItem.setIconSpace("    ");
		startUpItem.setMsgTextTop(Messages.getString("startup.toplabel02"));
		startUpItem.setMsgTextBottom(Messages.getString("startup.bottomlabel02"));
		startUpItem.setToolTipText(Messages.getString("startup.tiplabel02"));
		startUpItem.setPerspectiveName(Messages.getString("startup.pname02"));
		startUpItem.setColor(ColorConstantsWrapper.white());
		m_startUpItem.add(startUpItem);

		//矢印アイコン2
		startUpItem = new StartUpItem();
		startUpItem.setFieldId("yajirushi2");
		image = loadImage("yajirushi.png");
		if(image != null) {
			ImageFigure imageFigure = new ImageFigure(image);
			startUpItem.setImageFigure(imageFigure);
			startUpItem.setPosX(posXyajirushiLeft);
			startUpItem.setPosY(posYyajirushiLower);
			m_startUpItem.add(startUpItem);
		}

		startUpItem = new StartUpItem();
		startUpItem.setFieldId("fieldid4");
		startUpItem.setTarget("fieldid3");
		startUpItem.setPosX(posXLeft);
		startUpItem.setPosY(posYlower);
		startUpItem.setWidth(width);
		startUpItem.setHeight(height);
		image = loadImage("startup_monitor_result.png");
		startUpItem.setImage(image);
		startUpItem.setIconSpace("    ");
		startUpItem.setMsgTextTop(Messages.getString("startup.toplabel03"));
		startUpItem.setMsgTextBottom(Messages.getString("startup.bottomlabel03"));
		startUpItem.setToolTipText(Messages.getString("startup.tiplabel03"));
		startUpItem.setPerspectiveName(Messages.getString("startup.pname03"));
		startUpItem.setColor(ColorConstantsWrapper.white());
		m_startUpItem.add(startUpItem);

		//矢印アイコン3
		startUpItem = new StartUpItem();
		startUpItem.setFieldId("yajirushi3");
		image = loadImage("yajirushi.png");
		if(image != null) {
			ImageFigure imageFigure = new ImageFigure(image);
			startUpItem.setImageFigure(imageFigure);
			startUpItem.setPosX(posXyajirushiRight);
			startUpItem.setPosY(posYyajirushiUpper);
			m_startUpItem.add(startUpItem);
		}

		startUpItem = new StartUpItem();
		startUpItem.setFieldId("fieldid5");
		startUpItem.setTarget("fieldid2");
		startUpItem.setPosX(posXRight);
		startUpItem.setPosY(posYupper);
		startUpItem.setWidth(width);
		startUpItem.setHeight(height);
		image = loadImage("startup_job_setting.png");
		startUpItem.setImage(image);
		startUpItem.setIconSpace("    ");
		startUpItem.setMsgTextTop(Messages.getString("startup.toplabel04"));
		startUpItem.setMsgTextBottom(Messages.getString("startup.bottomlabel04"));
		startUpItem.setToolTipText(Messages.getString("startup.tiplabel04"));
		startUpItem.setPerspectiveName(Messages.getString("startup.pname04"));
		startUpItem.setColor(ColorConstantsWrapper.white());
		m_startUpItem.add(startUpItem);

		//矢印アイコン4
		startUpItem = new StartUpItem();
		startUpItem.setFieldId("yajirushi4");
		image = loadImage("yajirushi.png");
		if(image != null) {
			ImageFigure imageFigure = new ImageFigure(image);
			startUpItem.setImageFigure(imageFigure);
			startUpItem.setPosX(posXyajirushiRight);
			startUpItem.setPosY(posYyajirushiLower);
			m_startUpItem.add(startUpItem);
		}

		startUpItem = new StartUpItem();
		startUpItem.setFieldId("fieldid6");
		startUpItem.setTarget("fieldid5");
		startUpItem.setPosX(posXRight);
		startUpItem.setPosY(posYlower);
		startUpItem.setWidth(width);
		startUpItem.setHeight(height);
		image = loadImage("startup_job_result.png");
		startUpItem.setImage(image);
		startUpItem.setIconSpace("    ");
		startUpItem.setMsgTextTop(Messages.getString("startup.toplabel05"));
		startUpItem.setMsgTextBottom(Messages.getString("startup.bottomlabel05"));
		startUpItem.setToolTipText(Messages.getString("startup.tiplabel05"));
		startUpItem.setPerspectiveName(Messages.getString("startup.pname05"));
		startUpItem.setColor(ColorConstantsWrapper.white());
		m_startUpItem.add(startUpItem);
	}

	private static Image loadImage(String fileName) {
		Image image = null;
		ImageDescriptor desc;
		try {
			URL url = new URL(ClusterControlPlugin.getDefault().getBundle().getEntry("/"), "icons/" + fileName);
			desc = ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e1) {
			desc = ImageDescriptor.getMissingImageDescriptor();
		}
		image = desc.createImage();
		return image;
	}
}
