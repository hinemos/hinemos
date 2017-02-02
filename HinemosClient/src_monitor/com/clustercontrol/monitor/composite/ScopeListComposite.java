/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.action.GetScopeListTableDefine;
import com.clustercontrol.monitor.util.ConvertListUtil;
import com.clustercontrol.monitor.util.MonitorEndpointWrapper;
import com.clustercontrol.monitor.util.ScopeSearchRunUtil;
import com.clustercontrol.monitor.view.action.ScopeSpecifiedShowAction;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.monitor.FacilityNotFound_Exception;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.ScopeDataInfo;
import com.sun.xml.internal.ws.client.ClientTransportException;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * スコープ情報一覧のコンポジットクラス<BR>
 *
 * スコープ情報一覧部分のテーブルのコンポジット
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScopeListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( ScopeListComposite.class );

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 危険ラベル */
	private Label criticalLabel = null;

	/** 警告ラベル */
	private Label warningLabel = null;

	/** 通知ラベル */
	private Label infoLabel = null;

	/** 不明ラベル */
	private Label unknownLabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public ScopeListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(5, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		table.setLayoutData(gridData);

		// ステータス作成

		// 危険
		this.criticalLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "criticallabel", criticalLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.criticalLabel.setLayoutData(gridData);
		this.criticalLabel.setBackground(this.getDisplay().getSystemColor(
				SWT.COLOR_RED));

		// 警告
		this.warningLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "warninglabel", warningLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.warningLabel.setLayoutData(gridData);
		this.warningLabel.setBackground(this.getDisplay().getSystemColor(
				SWT.COLOR_YELLOW));

		// 通知
		this.infoLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "infolabel", infoLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.infoLabel.setLayoutData(gridData);
		this.infoLabel.setBackground(this.getDisplay().getSystemColor(
				SWT.COLOR_GREEN));

		// 不明
		this.unknownLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "unknownlabel", unknownLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.unknownLabel.setLayoutData(gridData);
		this.unknownLabel.setBackground(new Color(null, 128, 192, 255));

		// 合計
		this.totalLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "totallabel", totalLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(
				//        				GetScopeListTableDefine.get(),
				GetScopeListTableDefine.getScopeListTableDefine(),
				GetScopeListTableDefine.SORT_COLUMN_INDEX1,
				GetScopeListTableDefine.SORT_COLUMN_INDEX2,
				GetScopeListTableDefine.SORT_ORDER);
		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		// ダブルクリックした場合、表示を配下に移動する
		ScopeSpecifiedShowAction listener = new ScopeSpecifiedShowAction();
		this.tableViewer.addDoubleClickListener(listener);
	}

	/**
	 * このコンポジットが利用する共通テーブルビューアーを返します。
	 *
	 * @return 共通テーブルビューアー
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	public void update(String managerName, String facilityId) {
		super.update();

		ArrayList<?> infoList = null;
		if(facilityId == null) {
			facilityId = ReservedFacilityIdConstant.ROOT_SCOPE;
		}
		try {
			m_log.debug("managerName=" + managerName + ", faciiltyID=" + facilityId);
			List<ScopeDataInfo> records = null;
			MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(managerName);
			records = wrapper.getScopeList(facilityId, true, true, false);
			infoList = ConvertListUtil.scopeInfoDataListToArrayList(managerName, records);
		} catch (InvalidRole_Exception e) {
			if(ClientSession.isDialogFree()){
				ClientSession.occupyDialog();
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				ClientSession.freeDialog();
			}
		} catch (MonitorNotFound_Exception e) {
			MessageDialog.openError(null, Messages.getString("message"),
					Messages.getString("message.monitor.64") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (FacilityNotFound_Exception e) {
			MessageDialog.openError(null, Messages.getString("message"),
					Messages.getString("message.monitor.64") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (HinemosUnknown_Exception e) {
			MessageDialog.openError(null, Messages.getString("message"),
					Messages.getString("message.monitor.64") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			if (ClientSession.isDialogFree()) {
				ClientSession.occupyDialog();
				if (e instanceof ClientTransportException) {
					m_log.info("update() getEventList, " + e.getMessage());
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.transfer") + ", " + HinemosMessage.replace(e.getMessage()));
				} else {
					m_log.warn("update() getScopeList, " + e.getMessage(), e);
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}
				ClientSession.freeDialog();
			}
		}

		if (infoList == null) {
			infoList = new ArrayList<Object>();
		}

		//危険・警告・通知・不明の件数カウント
		this.updateStatus(infoList);

		tableViewer.setInput(infoList);
	}

	/**
	 * ビューを更新します。<BR>
	 * 引数で指定されたファシリティの配下全てのファシリティのスコープ情報一覧を取得し、
	 * 共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたファシリティに属するスコープ情報一覧を取得します。</li>
	 * <li>共通テーブルビューアーにスコープ情報一覧をセットします。</li>
	 * </ol>
	 *
	 * @param facilityId 表示対象の親ファシリティID
	 *
	 * @see #updateStatus(ArrayList)
	 */
	public void update(List<String> managerList) {
		super.update();

		ArrayList<?> infoList = null;
		try {
			m_log.debug("Scope selected");
			ScopeSearchRunUtil util = new ScopeSearchRunUtil();
			infoList = util.searchInfo(managerList);
		} catch (Exception e) {
			if (ClientSession.isDialogFree()) {
				ClientSession.occupyDialog();
				if (e instanceof ClientTransportException) {
					m_log.info("update() getEventList, " + e.getMessage());
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.transfer") + ", " + HinemosMessage.replace(e.getMessage()));
				} else {
					m_log.warn("update() getScopeList, " + e.getMessage(), e);
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}
				ClientSession.freeDialog();
			}
		}

		if (infoList == null) {
			infoList = new ArrayList<Object>();
		}

		//危険・警告・通知・不明の件数カウント
		this.updateStatus(infoList);

		tableViewer.setInput(infoList);
	}

	/**
	 * ステータスラベルを更新します。<BR>
	 * 引数で指定されたスコープ情報一覧より、重要度ごとの件数，全件数を取得し、
	 * ステータスラベルを更新します。
	 *
	 * @param list スコープ情報一覧
	 */
	private void updateStatus(ArrayList<?> list) {
		int[] status = new int[4];

		if (list != null) {
			int count = list.size();

			for (int i = 0; i < count; i++) {
				ArrayList<?> data = (ArrayList<?>) list.get(i);
				int value = ((Integer) data.get(GetScopeListTableDefine.PRIORITY)).intValue();

				switch (value) {
				case PriorityConstant.TYPE_CRITICAL:
					status[0]++;
					break;
				case PriorityConstant.TYPE_WARNING:
					status[1]++;
					break;
				case PriorityConstant.TYPE_INFO:
					status[2]++;
					break;
				case PriorityConstant.TYPE_UNKNOWN:
					status[3]++;
					break;
				default: // 既定の対処はスルー。
					break;
				}
			}
		}

		// ラベル更新
		this.criticalLabel.setText(String.valueOf(status[0]));
		this.warningLabel.setText(String.valueOf(status[1]));
		this.infoLabel.setText(String.valueOf(status[2]));
		this.unknownLabel.setText(String.valueOf(status[3]));
		int total = status[0] + status[1] + status[2] + status[3];
		Object[] args = { String.valueOf(total) };
		this.totalLabel.setText(Messages.getString("records", args));
	}

}
