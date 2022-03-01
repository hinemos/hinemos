/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.AgentStatusInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.action.GetAgentListTableDefine;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ノード一覧コンポジットクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class AgentListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( AgentListComposite.class );

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 表示内容ラベル */
	private Label statuslabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;
	
	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public AgentListComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.statuslabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "statuslabel", statuslabel);
		this.statuslabel.setText("");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.statuslabel.setLayoutData(gridData);

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetAgentListTableDefine.get(),
				GetAgentListTableDefine.SORT_COLUMN_INDEX1,
				GetAgentListTableDefine.SORT_COLUMN_INDEX2,
				GetAgentListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		this.totalLabel = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "totallabel", totalLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

	}

	/**
	 * tableViewerを返します。
	 *
	 * @return tableViewer
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットするノードの一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全ノードを表示します。
	 */
	@Override
	public void update() {
		// データ取得
		List<AgentStatusInfoResponse> list = null;
		ArrayList<Object> listInput = new ArrayList<Object>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		this.statuslabel.setText("");
		int cnt = 0;
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
				list = wrapper.getAgentStatusList();
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}

			if(list == null){
				list = new ArrayList<AgentStatusInfoResponse>();
			}

			for (AgentStatusInfoResponse node : list) {
				ArrayList<Object> aList = new ArrayList<Object>();
				aList.add(managerName);
				aList.add(node.getFacilityId());
				aList.add(node.getFacilityName());
				try {
					aList.add(TimezoneUtil.getSimpleDateFormat().parse(node.getStartupTime()));
				} catch (Exception e1) {
				}
				try {
					aList.add(TimezoneUtil.getSimpleDateFormat().parse(node.getLastLogin()));
				} catch (Exception e) {
				}
				aList.add(node.getMultiplicity());

				switch (node.getUpdateStatusCode()) {
				case DONE:
					aList.add(Messages.getString("repository.agent.update.done"));
					break;
				case NOT_YET:
					aList.add(Messages.getString("repository.agent.update.notyet"));
					break;
				case RESTARTING:
					aList.add(Messages.getString("repository.agent.update.restarting"));
					break;
				case UPDATING:
					aList.add(Messages.getString("repository.agent.update.updating"));
					break;
				case UNKNOWN:
					aList.add(Messages.getString("repository.agent.update.unknown"));
					break;
				case UNSUPPORTED:
					aList.add(Messages.getString("repository.agent.update.unsupported"));
					break;
				}

				aList.add(null);
				listInput.add(aList);
				cnt++;
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			StringBuffer msg = new StringBuffer();
			for( Map.Entry<String, String> e : errorMsgs.entrySet() ){
				String eol = System.getProperty("line.separator");
				msg.append("MANAGER[" + e.getKey() + "] : " + eol + "    " + e.getValue() + eol + eol);
			}
			MessageDialog.openInformation(null, Messages.getString("message"), msg.toString());
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);

		// 合計欄更新
		String[] args = { Integer.toString(cnt) };
		String message = null;
		message = Messages.getString("records", args);
		this.totalLabel.setText(message);
	}
}
