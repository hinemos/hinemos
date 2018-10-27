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

import com.clustercontrol.bean.Property;
import com.clustercontrol.repository.action.GetNodeList;
import com.clustercontrol.repository.action.GetNodeListTableDefine;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.NodeInfo;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 複数選択可能なノード一覧コンポジットクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeFilterComposite extends Composite {
	// ログ
	private static Log m_log = LogFactory.getLog( NodeFilterComposite.class );

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 表示内容ラベル */
	private Label statuslabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	/** 検索条件 */
	private Property condition = null;

	/** スコープのファシリティID */
	private String scopeId = null;

	/** 割り当て時はtrue, 解除時はfalse */
	private boolean assignFlag = true;

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 *
	 * 未割り当てノードを表示するインスタンスを返します。(割り当て時 assignFlag = true)
	 * 指定したスコープに属するノードを表示するインスタンスを返します。(解除時 assignFlag = false)
	 * <p>
	 *
	 * このインスタンスは常に固定された内容を表示し、update(NodeFilterProperty) を実行しても表示内容に変更はありません。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 * @param managerName
	 *            マネージャ名
	 * @param facilityId
	 *            スコープのファシリティID
	 */
	public NodeFilterComposite(Composite parent, int style, String managerName, String facilityId, boolean assignFlag) {
		super(parent, style);

		this.managerName = managerName;
		this.assignFlag = assignFlag;
		this.scopeId = facilityId;
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

		Table table = new Table(this, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.widthHint = SWT.MIN;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetNodeListTableDefine.get(), 0, 1);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		this.totalLabel = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "totallabel", totalLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

		this.update();
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
	 * コンポジットを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットするノードの一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全ノードを表示します。
	 */
	@Override
	public void update() {
		// データ取得
		List<NodeInfo> list = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		if (assignFlag) {
			List<NodeInfo> allList = null;
			// 割り当て時

			// 全ノードを取得
			if (this.condition == null) {
				this.statuslabel.setText("");
				allList = new GetNodeList().getAll(this.managerName);
			} else {
				this.statuslabel.setText(Messages.getString("filtered.list"));
				allList = new GetNodeList().get(this.managerName, this.condition);
			}

			// 全ノードから既に割り当て済みのノードを取り除く。
			try {
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(this.managerName);
				List<NodeInfo> assignedList = wrapper.getNodeList(this.scopeId, 1);
				list = new ArrayList<NodeInfo>();
				for (NodeInfo node : allList) {
					boolean flag = true;
					for (NodeInfo assignedNode : assignedList) {
						if (node.getFacilityId().equals(assignedNode.getFacilityId())) {
							flag = false;
							break;
						}
					}
					if (flag) {
						list.add(node);
					}
				}
			} catch (InvalidRole_Exception e) {
				errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
				throw new InternalError(e.getMessage());
			} catch (Exception e) {
				m_log.warn("getAll(), " + e.getMessage(), e);
				errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				throw new InternalError(e.getMessage());
			}
		} else {
			// 割り当て解除時
			this.statuslabel.setText("");
			list = new ArrayList<NodeInfo>();
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(this.managerName);
			try {
				// RepositoryControllerBean.ONE_LEVEL = 1
				list = wrapper.getNodeList(this.scopeId, 1);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
				throw new InternalError(e.getMessage());
			} catch (Exception e) {
				m_log.warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				throw new InternalError(e.getMessage());
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

		ArrayList<Object> listInput = new ArrayList<Object>();
		for (NodeInfo node : list) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(managerName);
			a.add(node.getFacilityId());
			a.add(node.getFacilityName());
			a.add(node.getPlatformFamily());
			if (node.getIpAddressVersion() == 6) {
				a.add(node.getIpAddressV6());
			} else {
				a.add(node.getIpAddressV4());
			}
			a.add(node.getDescription());
			a.add(node.getOwnerRoleId());
			a.add(null);
			listInput.add(a);
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);

		// 合計欄更新
		String[] args = { String.valueOf(list.size()) };
		String message = null;
		if (assignFlag) {
			if (this.condition == null) {
				message = Messages.getString("records", args);
			} else {
				message = Messages.getString("filtered.records", args);
			}
		} else {
			message = Messages.getString("records", args);
		}
		this.totalLabel.setText(message);
	}

	/**
	 * 検索条件にヒットしたノードの一覧を表示します。
	 * <p>
	 *
	 * conditionがnullの場合、全ノードを表示します。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void update(Property condition) {
		this.condition = condition;

		this.update();
	}
}
