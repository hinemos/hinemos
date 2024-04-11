/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.TemplateSetInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.reporting.action.GetTemplateSetListTableDefine;
import com.clustercontrol.reporting.composite.action.TemplateSetDoubleClickListener;
import com.clustercontrol.reporting.util.ReportingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * テンプレートセット一覧コンポジットクラス<BR>
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateSetListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( TemplateSetListComposite.class );
	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** テーブル */
	private Table templateSetListTable = null;
	/** ラベル */
	private Label m_labelCount = null;
	/** テンプレートセットID */
	private String m_templateSetId = null;

	/**
	 * このコンポジットが利用するテーブルビューアを取得します。<BR>
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}
	/**
	 * このコンポジットが利用するテーブルを取得します。<BR>
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}
	/**
	 * テンプレートセットID
	 * @return m_templateSetId
	 */
	public String getTemplateSetId() {
		return m_templateSetId;
	}

	/**
	 * テンプレートセットID
	 * @param templateSetId
	 */
	public void setTemplateSetId(String templateSetId) {
		m_templateSetId = templateSetId;
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 * @since 2.0.0
	 */
	public TemplateSetListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理<BR>
	 *
	 * @since 2.0.0
	 */
	private void initialize() {
		
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//テンプレートセット一覧テーブル作成
		templateSetListTable = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, templateSetListTable);
		templateSetListTable.setHeaderVisible(true);
		templateSetListTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		templateSetListTable.setLayoutData(gridData);
		
		m_labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "count", m_labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(templateSetListTable);
		m_viewer.createTableColumn(GetTemplateSetListTableDefine.get(),
				GetTemplateSetListTableDefine.SORT_COLUMN_INDEX1,
				GetTemplateSetListTableDefine.SORT_COLUMN_INDEX2,
				GetTemplateSetListTableDefine.SORT_ORDER);
		
		for (int i = 0; i < templateSetListTable.getColumnCount(); i++){
			templateSetListTable.getColumn(i).setMoveable(true);
		}
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new TemplateSetDoubleClickListener(this));
		
	}

	/**
	 * 更新処理<BR>
	 *
	 * @since 2.0.0
	 */
	@Override
	public void update() {
		List<TemplateSetInfoResponse> list = null;

		//カレンダ一覧情報取得
		Map<String, List<TemplateSetInfoResponse>> dispDataMap= new ConcurrentHashMap<String, List<TemplateSetInfoResponse>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			ReportingRestClientWrapper wrapper = ReportingRestClientWrapper.getWrapper(managerName);
			try {
				boolean isPublish = wrapper.checkPublish().getPublish();
				if (!isPublish) {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.expiration.term.invalid"));
				}
			} catch (HinemosUnknown e) {
				// エンタープライズ機能が無効の場合は HinemosUnknownでラップしたUrlNotFoundとなる。
				// この場合は無視する
				if(UrlNotFound.class.equals(e.getCause().getClass())) {
					continue;
				}
				String errMsg = HinemosMessage.replace(e.getMessage());
				m_log.warn("checkPublish Error, " + errMsg);
				errorMsgs.put(managerName, e.getMessage());
				continue;
			} catch (Exception e) {
				String errMsg = HinemosMessage.replace(e.getMessage());
				m_log.warn("checkPublish Error, " + errMsg);
				errorMsgs.put(managerName, e.getMessage());
				continue;
			}

			try {
				list = wrapper.getTemplateSetList(null);
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				// 上記以外の例外
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("update(), " + errMessage, e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
			}
			if(list == null){
				list = new ArrayList<TemplateSetInfoResponse>();
			}

			dispDataMap.put(managerName, list);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<TemplateSetInfoResponse>> map: dispDataMap.entrySet()) {
			for (TemplateSetInfoResponse info : map.getValue()) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(info.getTemplateSetId());
				obj.add(HinemosMessage.replace(info.getTemplateSetName()));
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
		m_viewer.setInput(listInput);

		Object[] args = { Integer.valueOf(listInput.size()) };
		m_labelCount.setText(Messages.getString("records", args));
	}
}
