/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.action.GetRpaScenarioTagListTableDefine;
import com.clustercontrol.rpa.composite.action.RpaScenarioTagDoubleClickListener;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * RPAシナリオタグ一覧コンポジットクラス
 */
public class RpaScenarioTagListComposite extends Composite {

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioTagListComposite.class );
	/** テーブルビューア */
	private CommonTableViewer viewer = null;
	/** テーブル */
	private Table rpaScenarioTagListTable = null;
	/** ラベル */
	private Label labelCount = null;
	/** タグID */
	private String tagId = null;

	/**
	 * このコンポジットが利用するテーブルビューアを取得します。
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return viewer;
	}
	/**
	 * このコンポジットが利用するテーブルを取得します。
	 * @return テーブル
	 */
	public Table getTable() {
		return viewer.getTable();
	}
	/**
	 * RPAシナリオタグID
	 * @return m_tagId
	 */
	public String getTagId() {
		return this.tagId;
	}

	/**
	 * RPAシナリオタグID
	 * @param tagId
	 */
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 */
	public RpaScenarioTagListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理
	 */
	private void initialize() {
		
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//RPAシナリオタグ一覧テーブル作成
		rpaScenarioTagListTable = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		rpaScenarioTagListTable.setHeaderVisible(true);
		rpaScenarioTagListTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		rpaScenarioTagListTable.setLayoutData(gridData);
		
		labelCount = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		labelCount.setLayoutData(gridData);

		viewer = new CommonTableViewer(rpaScenarioTagListTable);
		viewer.createTableColumn(GetRpaScenarioTagListTableDefine.get(),
				GetRpaScenarioTagListTableDefine.SORT_COLUMN_INDEX1,
				GetRpaScenarioTagListTableDefine.SORT_COLUMN_INDEX2,
				GetRpaScenarioTagListTableDefine.SORT_ORDER);
		
		for (int i = 0; i < rpaScenarioTagListTable.getColumnCount(); i++){
			rpaScenarioTagListTable.getColumn(i).setMoveable(true);
		}
		// ダブルクリックリスナの追加
		viewer.addDoubleClickListener(new RpaScenarioTagDoubleClickListener(this));
		
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update() {
		List<RpaScenarioTagResponse> list = null;

		//RPAシナリオタグ一覧情報取得
		Map<String, List<RpaScenarioTagResponse>> dispDataMap= new ConcurrentHashMap<String, List<RpaScenarioTagResponse>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			try {
				list = wrapper.getRpaScenarioTagList(null);
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				// 上記以外の例外
				String errMessage = HinemosMessage.replace(e.getMessage());
				log.warn("update(), " + errMessage, e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
			}
			if(list == null){
				list = new ArrayList<RpaScenarioTagResponse>();
			}

			dispDataMap.put(managerName, list);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<RpaScenarioTagResponse>> map: dispDataMap.entrySet()) {
			for (RpaScenarioTagResponse info : map.getValue()) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(HinemosMessage.replace(this.getParentTagId(info.getTagPath())));
				obj.add(info.getTagId());
				obj.add(HinemosMessage.replace(info.getTagName()));
				obj.add(HinemosMessage.replace(info.getDescription()));
				obj.add(info.getOwnerRoleId());
				obj.add(info.getRegUser());
				obj.add(info.getRegDate());
				obj.add(info.getUpdateUser());
				obj.add(info.getUpdateDate());
				obj.add(null);
				listInput.add(obj);
			}
		}
		viewer.setInput(listInput);

		Object[] args = { Integer.valueOf(listInput.size()) };
		labelCount.setText(Messages.getString("records", args));
	}
	
	private String getParentTagId(String tagPath) {
		String ret = "";
		if (tagPath != null && !tagPath.equals("")){
			int index = tagPath.lastIndexOf("\\");
			ret = tagPath.substring(index + 1);
		}
		return ret;
	}
}
