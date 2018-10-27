/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.figure;

import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.nodemap.util.ImageManager;
import com.clustercontrol.nodemap.util.RelationViewController;

/**
 * 背景画像のクラス
 * @since 1.0.0
 */
public class BgFigure extends FileImageFigure {
	private final String m_scopeFacilityId;
	private final String m_scopeFacilityName;
	private final String m_ownerRoleId;
	private final boolean m_builtin;
	private final String m_managerName;
	private int height;
	private int width;

	public BgFigure(String managerName, String scopeFacilityId, String scopeFacilityName, String ownerRoleId, boolean builtin){
		this.setFocusTraversable(true);
		this.setRequestFocusEnabled(true);

		m_scopeFacilityId = scopeFacilityId;
		m_scopeFacilityName = scopeFacilityName;
		m_ownerRoleId = ownerRoleId;
		m_builtin = builtin;
		m_managerName = managerName;
	}

	@Override
	public void draw(String filename) throws Exception {
		m_filename = filename;
		// 上書きのため、一度全部消す。
		this.removeAll();

		// レイアウトを設定する
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		layout.setStretchMinorAxis(false);
		this.setLayoutManager(layout);

		imageDraw(filename);
	}

	/**
	 * RegistImageの際にimageだけ再描画する必要があるため、
	 * drawメソッドから切り出してimageDrawメソッドを作成する。
	 * @throws Exception
	 */
	@Override
	public void imageDraw(String filename) throws Exception {
		removeImage();

		// 画像を設定する
		m_image = null;
		Image image = ImageManager.loadBg(m_managerName, filename);
		if (image == null) {
			return;
		}
		m_image = new ImageFigure(image);
		
		width = m_image.getPreferredSize().width;
		height = m_image.getPreferredSize().height;
		
		this.add(m_image);
	}

	@Override
	public String getFacilityId() {
		return m_scopeFacilityId;
	}

	@Override
	public String getFacilityName() {
		return m_scopeFacilityName;
	}
	
	@Override
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	};
	
	@Override
	public boolean isBuiltin() {
		return m_builtin;
	}

	@Override
	public void handleFocusGained(FocusEvent fe){
		/*
		 * イベントビューとステータスビューの表示を変更する。
		 */
		RelationViewController.updateScopeTreeView(null, getFacilityId());
		RelationViewController.updateStatusEventView(getFacilityId(), null);
	}

	@Override
	public void handleFocusLost(FocusEvent fe){
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}
