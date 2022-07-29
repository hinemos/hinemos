/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.GetRpaScenarioListRequest;
import org.openapitools.client.model.GetRpaScenarioListResponse;

import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.rpa.action.GetRpaScenarioListTableDefine;
import com.clustercontrol.rpa.composite.action.RpaScenarioDoubleClickListener;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.util.RpaScenarioFilterPropertyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * RPAシナリオ実績[シナリオ一覧]のコンポジットクラス<BR>
 *
 * RPAシナリオ実績[シナリオ一覧]部分のテーブルのコンポジット
 */
public class RpaScenarioListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( RpaScenarioListComposite.class );

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 表示内容ラベル */
	private Label statuslabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	/** 検索条件 */
	private Property condition = null;

	// ----- コンストラクタ ----- //

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public RpaScenarioListComposite(Composite parent, int style) {
		super(parent, style);

		// 初期化
		initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットの初期化
	 */
	private void initialize() {

		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// 表示内容ラベルの作成
		this.statuslabel = new Label(this, SWT.LEFT);
		this.statuslabel.setText("");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.statuslabel.setLayoutData(gridData);

		// テーブルの作成
		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
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
		this.tableViewer.createTableColumn(GetRpaScenarioListTableDefine.get(),
				GetRpaScenarioListTableDefine.SORT_COLUMN_INDEX1,
				GetRpaScenarioListTableDefine.SORT_COLUMN_INDEX2,
				GetRpaScenarioListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new RpaScenarioDoubleClickListener(this));

		// 合計ラベルの作成
		this.totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットするシナリオ実績の一覧を表示します <br>
	 * 検索条件が設定されていない場合は、全シナリオ実績を表示します。
	 */
	@Override
	public void update() {
		// Message collecting
		Map<String, String> errMsgs = new ConcurrentHashMap<>();

		// データ取得
		Map<String, List<GetRpaScenarioListResponse>> dispDataMap= new ConcurrentHashMap<>();

		String conditionManager = null;
		if(this.condition == null) {
			this.statuslabel.setText("");
			
			for (String managerName : RestConnectManager.getActiveManagerSet()) {
				getRpaScenarioList(managerName, dispDataMap, errMsgs);
			}
		} else {
			this.statuslabel.setText(Messages.getString("filtered.list"));
			
			conditionManager = JobPropertyUtil.getManagerName(this.condition);
			PropertyUtil.deletePropertyDefine(this.condition);
			GetRpaScenarioListRequest filter = RpaScenarioFilterPropertyUtil.property2dto(this.condition);
			Set<String> managerSet = null;
			if (conditionManager == null || conditionManager.equals("")) {
				managerSet = RestConnectManager.getActiveManagerSet();
			
			} else {
				managerSet = new HashSet<String>() ;
				managerSet.add(conditionManager);
			}
			
			for (String managerName : managerSet) {
				getRpaScenarioListWithCondition(managerName, filter, dispDataMap, errMsgs);
			}
		}

		// Show message box
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}

		// レスポンスデータ を tableViewer にセットするための詰め替え
		ArrayList<Object> listInput = new ArrayList<Object>();

		for( Map.Entry<String, List<GetRpaScenarioListResponse>> entry: dispDataMap.entrySet() ){
			for (GetRpaScenarioListResponse response : entry.getValue()) {
				ArrayList<Object> arrayList = new ArrayList<Object>();
				arrayList.add(entry.getKey());
				arrayList.add(response.getScenarioOperationResultCreateSettingId());
				arrayList.add(response.getScenarioId());
				arrayList.add(response.getRpaToolId());
				arrayList.add(response.getScenarioName());
				arrayList.add(response.getScenarioIdentifyString());
				arrayList.add(response.getDescription());
				arrayList.add(response.getOwnerRoleId());
				arrayList.add(response.getRegUser());
				arrayList.add(response.getRegDate());
				arrayList.add(response.getUpdateUser());
				arrayList.add(response.getUpdateDate());
				arrayList.add(null);

				listInput.add(arrayList);
			}
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);

		// 合計欄更新
		String[] args = { String.valueOf(listInput.size()) };
		String message = null;
		if (this.condition == null) {
			message = Messages.getString("records", args);
		} else {
			message = Messages.getString("filtered.records", args);
		}
		this.totalLabel.setText(message);
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
	 * 検索条件が事前に設定されている場合、その条件にヒットするシナリオ実績の一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全シナリオ実績を表示します。
	 */
	public void update(Property condition) {
		this.condition = condition;

		this.update();
	}

	private void getRpaScenarioList(String managerName,
			Map<String, List<GetRpaScenarioListResponse>> dispDataMap,
			Map<String, String> errorMsgs) {
		GetRpaScenarioListRequest getRpaScenarioListRequest = new GetRpaScenarioListRequest();
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			List<GetRpaScenarioListResponse> list = wrapper.getRpaScenarioList(getRpaScenarioListRequest);
			
			if( null != list ){
				dispDataMap.put(managerName, list);
			}
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (HinemosUnknown e) {
			// エンタープライズ機能が無効の場合は無視する
			if(UrlNotFound.class.equals(e.getCause().getClass())) {
				return;
			}
			// 上記以外の例外
			errorMsgs.put( managerName, Messages.getString("message.rpa.scenario.list.1") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("update() getRpaScenarioList, " + HinemosMessage.replace(e.getMessage()), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}

	private void getRpaScenarioListWithCondition(String managerName, GetRpaScenarioListRequest filter,
			Map<String, List<GetRpaScenarioListResponse>> dispDataMap,
			Map<String, String> errorMsgs) {
		try {
			// マネージャにアクセス
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			List<GetRpaScenarioListResponse> list = wrapper.getRpaScenarioList(filter);
			if( null != list ){
				dispDataMap.put(managerName, list);
			}
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (HinemosUnknown e) {			errorMsgs.put( managerName, Messages.getString("message.rpa.scenario.list.1") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("update() getMonitorListByCondition, " + e.getMessage(), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}
}
