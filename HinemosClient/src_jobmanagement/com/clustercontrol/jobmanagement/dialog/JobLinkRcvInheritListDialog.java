/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.JobLinkInheritInfoResponse;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetJobLinkRcvInheritTableDefine;
import com.clustercontrol.jobmanagement.bean.JobLinkInheritKeyInfoConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableNotSortViewer;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ連携の引継ぎ設定コンポジットを表示するダイアログクラスです。
 *
 */
public class JobLinkRcvInheritListDialog extends CommonDialog {

	/** ジョブ変数用テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ジョブ変数 追加用ボタン */
	private Button m_btnAdd = null;
	/** ジョブ変数 削除用ボタン */
	private Button m_btnDelete = null;

	/** シェル */
	private Shell m_shell = null;

	/** 引継ぎ設定情報 */
	private List<JobLinkInheritInfoResponse> m_jobLinkInheritList = new ArrayList<>();

	private boolean m_readOnly = false;

	/** コンストラクタ
	 * 
	 * @param parent
	 * @param readOnly
	 */
	public JobLinkRcvInheritListDialog(Shell parent, boolean readOnly) {
		super(parent);
		this.m_readOnly = readOnly;
		m_shell = this.getShell();
	}
	
	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see com.clustercontrol.monitor.action.GetEventFilterProperty#getProperty()
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		parent.getShell().setText(Messages.getString("joblink.inherit.setting"));
		
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		composite.setLayout(layout);

		// 引継ぎ設定（テーブル）
		Table table = new Table(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(300, 75));

		// ボタン（Composite）
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());

		// dummy
		new Label(buttonComposite, SWT.NONE).setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// 追加（ボタン）
		this.m_btnAdd = new Button(buttonComposite, SWT.NONE);
		this.m_btnAdd.setText(Messages.getString("add"));
		this.m_btnAdd.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobLinkRcvInheritDialog dialog = new JobLinkRcvInheritDialog(m_shell);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobLinkInheritInfoResponse info = dialog.getInputData();
					@SuppressWarnings("unchecked")
					ArrayList<Object> list = (ArrayList<Object>) m_viewer.getInput();
					if (list == null) {
						list = new ArrayList<Object>();
					}
					for (Object one : list) {
						@SuppressWarnings("unchecked")
						String tabParamId = (String)((ArrayList<Object>) one).get(2);
						if (tabParamId.equals(info.getParamId())) {
							// 重複エラー
							MessageDialog.openWarning(null, Messages.getString("warning"),
								Messages.getString("message.common.16", new String[] { Messages.getString("job.parameter.name") }));
							return;
						}
					}
					list.add(converJobLinkInherit(info));
					m_viewer.setInput(list);
				}
			}
		});
		this.m_btnAdd.setEnabled(!m_readOnly);

		// ジョブ変数：削除（ボタン）
		this.m_btnDelete = new Button(buttonComposite, SWT.NONE);
		this.m_btnDelete.setText(Messages.getString("delete"));
		this.m_btnDelete.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_viewer.getTable().getSelectionIndex();
				if (order >= 0) {
					@SuppressWarnings("unchecked")
					ArrayList<Object> objList = (ArrayList<Object>) m_viewer.getTable().getSelection()[0].getData();
					@SuppressWarnings("unchecked")
					ArrayList<Object> list = (ArrayList<Object>) m_viewer.getInput();
					list.remove(objList);
					m_viewer.setInput(list);
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
				
			}
		});
		this.m_btnDelete.setEnabled(!m_readOnly);

		this.m_viewer = new CommonTableNotSortViewer(table);
		this.m_viewer.createTableColumn(GetJobLinkRcvInheritTableDefine.get(),
				GetJobLinkRcvInheritTableDefine.SORT_COLUMN_INDEX,
				GetJobLinkRcvInheritTableDefine.SORT_ORDER);
		// 先頭2列はキー情報のためサイズ変更不可
		this.m_viewer.getTable().getColumn(GetJobLinkRcvInheritTableDefine.KEY_INFO).setResizable(false);
		this.m_viewer.getTable().getColumn(GetJobLinkRcvInheritTableDefine.EXP_KEY).setResizable(false);

		reflectInheritInfo();
	}

	public List<JobLinkInheritInfoResponse> getInput() {
		return m_jobLinkInheritList;
	}

	public void setInput(List<JobLinkInheritInfoResponse> jobLinkInheritList) {
		this.m_jobLinkInheritList = jobLinkInheritList;
	}

	/**
	 * 引継ぎ情報をコンポジットに反映します。
	 */
	private void reflectInheritInfo() {
		if (m_jobLinkInheritList != null) {
			ArrayList<Object> tableData = new ArrayList<Object>();
			for (JobLinkInheritInfoResponse info: m_jobLinkInheritList) {
				tableData.add(converJobLinkInherit(info));
			}
			m_viewer.setInput(tableData);
		}
	}
	
	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
	
	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		createJobLinkInherit();

		return null;
	}

	/**
	 * コンポジットの情報から、ジョブ連携送信情報を作成する。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult createJobLinkInherit() {

		// インスタンスを作成
		m_jobLinkInheritList = new ArrayList<>();
		//パラメータ取得
		ArrayList<?> tableData = (ArrayList<?>) m_viewer.getInput();
		if (tableData != null) {
			for (int i = 0; i < tableData.size(); i++) {
				ArrayList<?> tableLineData = (ArrayList<?>) tableData.get(i);
				JobLinkInheritInfoResponse info = new JobLinkInheritInfoResponse();
				info.setParamId((String)tableLineData.get(GetJobLinkRcvInheritTableDefine.PARAM_ID));

				String strKeyInfo = (String)tableLineData.get(GetJobLinkRcvInheritTableDefine.KEY_INFO);
				info.setKeyInfo(JobLinkInheritInfoResponse.KeyInfoEnum.fromValue(strKeyInfo));

				if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.EXP_INFO) {
					info.setExpKey((String)tableLineData.get(GetJobLinkRcvInheritTableDefine.EXP_KEY));
				} else {
					info.setExpKey("");
				}
				m_jobLinkInheritList.add(info);
			}
		}
		return null;
	}

	/**
	 * 引継ぎ情報をコンポジット向けの情報に変換する。
	 * 
	 * @param info 引継ぎ情報
	 * @return コンポジット向けデータ
	 */
	private ArrayList<Object> converJobLinkInherit(JobLinkInheritInfoResponse info) {
		ArrayList<Object> tableLineData = new ArrayList<Object>();
		tableLineData.add(info.getKeyInfo().getValue());
		if (info.getExpKey() == null || info.getExpKey().equals("")) {
			tableLineData.add("");
		} else {
			tableLineData.add(info.getExpKey());
		}
		tableLineData.add(info.getParamId());
		if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.SOURCE_FACILITY_ID) {
			tableLineData.add(JobLinkInheritKeyInfoConstant.STRING_SOURCE_FACILITY_ID);

		} else if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.SOURCE_IP_ADDRESS) {
			tableLineData.add(JobLinkInheritKeyInfoConstant.STRING_SOURCE_IP_ADDRESS);

		} else if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.JOBLINK_MESSAGE_ID) {
			tableLineData.add(JobLinkInheritKeyInfoConstant.STRING_JOBLINK_MESSAGE_ID);

		} else if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.MONITOR_DETAIL_ID) {
			tableLineData.add(JobLinkInheritKeyInfoConstant.STRING_MONITOR_DETAIL_ID);

		} else if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.PRIORITY) {
			tableLineData.add(JobLinkInheritKeyInfoConstant.STRING_PRIORITY);

		} else if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.APPLICATION) {
			tableLineData.add(JobLinkInheritKeyInfoConstant.STRING_APPLICATION);

		} else if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.MESSAGE) {
			tableLineData.add(JobLinkInheritKeyInfoConstant.STRING_MESSAGE);

		} else if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.MESSAGE_ORG) {
			tableLineData.add(JobLinkInheritKeyInfoConstant.STRING_MESSAGE_ORG);

		} else if (info.getKeyInfo() == JobLinkInheritInfoResponse.KeyInfoEnum.EXP_INFO) {
			tableLineData.add(
				String.format("%s(%s)", JobLinkInheritKeyInfoConstant.STRING_EXP_INFO, info.getExpKey()));
		}
		return tableLineData;
	}
}
