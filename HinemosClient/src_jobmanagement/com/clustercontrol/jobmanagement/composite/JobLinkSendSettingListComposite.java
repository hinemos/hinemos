/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

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
import org.openapitools.client.model.JobLinkSendSettingResponse;

import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.GetJobLinkSendSettingTableDefine;
import com.clustercontrol.jobmanagement.composite.action.JobLinkSendSettingDoubleClickListener;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ設定[ジョブ連携送信設定一覧]ビュー用のコンポジットクラスです。
 *
 */
public class JobLinkSendSettingListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( JobLinkSendSettingListComposite.class );

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;

	/** 合計ラベル */
	private Label m_totalLabel = null;

	/** 検索条件 */
	private Property m_condition = null;

	/** マネージャ名 */
	private String m_managerName = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public JobLinkSendSettingListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		final Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		m_viewer = new CommonTableViewer(table);

		m_viewer.createTableColumn(GetJobLinkSendSettingTableDefine.get(),
				GetJobLinkSendSettingTableDefine.SORT_COLUMN_INDEX1,
				GetJobLinkSendSettingTableDefine.SORT_COLUMN_INDEX2,
				GetJobLinkSendSettingTableDefine.SORT_ORDER);
		// 列移動が可能に設定（ビューのみ列移動可能で、各設定から選択可能な一覧は列移動不可）
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new JobLinkSendSettingDoubleClickListener(this));

		// 合計ラベルの作成
		m_totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_totalLabel.setLayoutData(gridData);
	}

	/**
	 * このコンポジットが利用するテーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * ジョブ設定[ジョブ連携送信設定]一覧情報を取得し、共通テーブルビューアーにセットします。
	 * 
	 */
	@Override
	public void update() {

		// 一覧をマネージャから取得
		ArrayList<ArrayList<Object>> listInput = new ArrayList<ArrayList<Object>>();
		Map<String, List<JobLinkSendSettingResponse>> dispDataMap = getJobLinkSendSettingList(m_managerName);

		for(Map.Entry<String, List<JobLinkSendSettingResponse>> entrySet : dispDataMap.entrySet()) {
			List<JobLinkSendSettingResponse> list = null;
			list = entrySet.getValue();
			if(list == null) {
				// ジョブ連携送信設定一覧に設定される情報がない場合
				list = new ArrayList<JobLinkSendSettingResponse>();
			}
			for(JobLinkSendSettingResponse info : list){
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(entrySet.getKey());
				a.add(info.getJoblinkSendSettingId());
				a.add(info.getDescription());
				a.add(info.getFacilityId());
				a.add(HinemosMessage.replace(info.getScope()));
				a.add(info.getProtocol().getValue());
				a.add(info.getPort());
				a.add(info.getOwnerRoleId());
				a.add(info.getRegUser());
				if(info.getRegDate() == null){
					a.add(null);
				}
				else{
					a.add(info.getRegDate());
				}
				a.add(info.getUpdateUser());
				if(info.getUpdateDate() == null){
					a.add(null);
				}
				else{
					a.add(info.getUpdateDate());
				}
				listInput.add(a);
			}

			// 合計欄更新
			String[] args = { String.valueOf(listInput.size()) };
			String message = null;
			if (m_condition == null) {
				message = Messages.getString("records", args);
			} else {
				message = Messages.getString("filtered.records", args);
			}
			m_totalLabel.setText(message);
		}

		// テーブル更新
		m_viewer.setInput(listInput);
	}

	/**
	 * ジョブ連携送信設定一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param マネージャ名
	 * @return ジョブ連携送信設定一覧
	 */
	private Map<String, List<JobLinkSendSettingResponse>> getJobLinkSendSettingList(String managerName){

		Map<String, List<JobLinkSendSettingResponse>> dispDataMap= new ConcurrentHashMap<>();
		List<JobLinkSendSettingResponse> records = null;
		Map<String, String> errMsgs = new ConcurrentHashMap<>();

		if(managerName == null) {
			for (String tmpManagerName : RestConnectManager.getActiveManagerSet()) {
				try {
					JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(tmpManagerName);
					records = wrapper.getJobLinkSendSettingList(null);
					dispDataMap.put(tmpManagerName, records);
				} catch (InvalidRole e) {
					MessageDialog.openInformation(
							null, 
							Messages.getString("message"), 
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					m_log.warn("getNotifyList(), " + HinemosMessage.replace(e.getMessage()), e);
					MessageDialog.openError(
							null, 
							Messages.getString("error"), 
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}
			}
		} else {
			try {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
				records = wrapper.getJobLinkSendSettingList(null);
				dispDataMap.put(managerName, records);
			} catch (InvalidRole e) {
				errMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("getNotifyList(), " + HinemosMessage.replace(e.getMessage()), e);
				errMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			//メッセージ表示
			if( 0 < errMsgs.size() ){
				UIManager.showMessageBox(errMsgs, true);
			}
		}
		return dispDataMap;
	}

	/**
	 * @return the managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param managerName the managerName to set
	 */
	public void setManagerName(String managerName) {
		this.m_managerName = managerName;
	}
}
