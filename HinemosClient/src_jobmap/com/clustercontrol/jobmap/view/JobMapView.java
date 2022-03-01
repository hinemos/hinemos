/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;

import com.clustercontrol.jobmanagement.view.JobMapViewIF;
import com.clustercontrol.jobmap.composite.JobMapComposite;
import com.clustercontrol.jobmap.composite.MapSearchBarComposite;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.view.AutoUpdateView;

public abstract class JobMapView extends AutoUpdateView implements JobMapViewIF {

	// ログ
	private static Log m_log = LogFactory.getLog( JobMapView.class );

	protected Composite stackComposite;
	protected MapSearchBarComposite m_searchBar;
	protected JobMapComposite m_canvasComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		parent.setLayout(new GridLayout(1, false));
		m_searchBar = new MapSearchBarComposite(parent ,SWT.NONE, true);
		m_searchBar.setLayoutData( new GridData(GridData.GRAB_HORIZONTAL) );

		if (this instanceof JobMapEditorView) {
			m_canvasComposite = new JobMapComposite(parent,
					SWT.BORDER, (JobMapEditorView)this);
		} else if (this instanceof JobMapHistoryView){
			m_canvasComposite = new JobMapComposite(parent,
					SWT.BORDER, (JobMapHistoryView)this);
		}
		m_searchBar.setJobMapComposite(m_canvasComposite);
		//ビューの更新
		m_canvasComposite.update();
		m_canvasComposite.initialMessageDisplay();
		
		// 設定情報反映
		applySetting();

	}


	@Override
	public void setFocus() {
		super.setFocus();
		if (m_canvasComposite != null) {
			m_canvasComposite.setCanvasFocus();
		}
		m_log.debug("setFocus end");
	}


	public abstract void applySetting();

	/**
	 * 自動更新用コールバックメソッド
	 * 定期的に呼び出される
	 */
	@Override
	public void update(boolean refreshFlag) {
		update(null, null, null);
	}

	/**
	 * zoomした後に勝手に再描画する方法が見つからず・・・。
	 * そのため、マネージャにアクセスしない再描画メソッドを作成した。
	 */
	public void updateNotManagerAccess() {
		m_canvasComposite.update();
	}

	@Override
	public void update(String managerName, String sessionId, JobTreeItemWrapper jobTreeItem) {
		try {
			m_canvasComposite.update(managerName, sessionId, jobTreeItem);
		} catch (Exception e) {
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
		}
		m_canvasComposite.setVisible(true);

		// 更新に失敗している場合は自動更新を停止する
		if (!m_canvasComposite.isUpdateSuccess()) {
			this.stopAutoReload();
		}
	}

	public void clear() {
		m_canvasComposite.clearCanvas();
		m_canvasComposite.clearMapData();
		// アイコンイメージキャッシュの更新
		JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
		iconCache.refresh();
		m_canvasComposite.initialMessageDisplay();
	}

	public JobMapComposite getCanvasComposite() {
		return m_canvasComposite;
	}

	public JobFigure getFocusFigure() {
		return m_canvasComposite.getFocusFigure();
	}

	public void zoomIn() {
		m_canvasComposite.zoomIn();
	}
	public void zoomOut() {
		m_canvasComposite.zoomOut();
	}

	public boolean isZoomAdjust() {
		return m_canvasComposite.isZoomAdjust();
	}
	
	public void setZoomAdjust(boolean adjust) {
		m_canvasComposite.setZoomAdjust(adjust);
	}

	public boolean isXyChange() {
		return m_canvasComposite.isXyChange();
	}

	public void setXyChange(boolean xyChange) {
		m_canvasComposite.setXyChange(xyChange);
	}
	
	/**
	 * 更新成功可否を返します。
	 * @return 更新成功可否
	 */
	public boolean isUpdateSuccess() {
		return this.m_canvasComposite.isUpdateSuccess();
	}
}
