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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.JobLinkExpInfoResponse;
import org.openapitools.client.model.JobLinkInheritInfoResponse;
import org.openapitools.client.model.JobLinkRcvInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetJobLinkExpTableDefine;
import com.clustercontrol.jobmanagement.bean.JobLinkConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.dialog.JobLinkExpAddDialog;
import com.clustercontrol.jobmanagement.dialog.JobLinkRcvInheritListDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ連携待機タブ用のコンポジットクラスです。
 *
 */
public class JobLinkRcvComposite extends Composite {

	/** スコープ用テキスト */
	private Text m_scopeFixedValueText = null;
	/** スコープ（ジョブ変数）用テキスト */
	private Text m_scopeJobParamText = null;
	/** ジョブ変数用ラジオボタン */
	private Button m_scopeJobParamRadio = null;
	/** 固定値用ラジオボタン */
	private Button m_scopeFixedValueRadio = null;
	/** スコープ参照用ボタン */
	private Button m_scopeFixedValueSelect = null;

	/** 過去に発生したジョブ連携メッセージを確認する用チェックボックス */
	private Button m_past = null;
	/** 対象期間（分）用チェックボックス */
	private Text m_pastMin = null;

	/** ジョブ連携メッセージID */
	private Text m_joblinkMessageId = null;

	/** 重要度（情報）有効/無効 */
	private Button m_infoValid = null;
	/** 重要度（警告）有効/無効 */
	private Button m_warnValid = null;
	/** 重要度（危険）有効/無効 */
	private Button m_criticalValid = null;
	/** 重要度（不明）有効/無効 */
	private Button m_unknownValid = null;

	/** アプリケーションフラグ */
	private Button m_application = null;
	/** アプリケーション */
	private Text m_applicationText = null;

	/** 監視詳細フラグ */
	private Button m_monitorDetailId = null;
	/** 監視詳細 */
	private Text m_monitorDetailIdText = null;

	/** メッセージフラグ */
	private Button m_message = null;
	/** メッセージ */
	private Text m_messageText = null;

	/** 拡張情報フラグ */
	private Button m_exp = null;
	/** 拡張情報 */
	private CommonTableViewer m_expTableViewer = null;
	/** 拡張情報：追加用ボタン */
	private Button m_expCreateButton = null;
	/** 拡張情報：削除用ボタン */
	private Button m_expDeleteButton = null;

	/** メッセージが得られたら常に用チェックボックス */
	private Button m_allEnd = null;
	/** メッセージが得られたら常に：終了値用テキスト */
	private Text m_allEndValue = null;
	/** 終了値（情報）用テキスト */
	private Text m_infoEndValue = null;
	/** 終了値（警告）用テキスト */
	private Text m_warnEndValue = null;
	/** 終了値（危険）用テキスト */
	private Text m_criticalEndValue = null;
	/** 終了値（不明）用テキスト */
	private Text m_unknownEndValue = null;
	/** メッセージが得られない場合用チェックボックス */
	private Button m_failureEndFlg = null;
	/** メッセージが得られない場合のタイムアウト用テキスト */
	private Text m_waitTime = null;
	/** メッセージが得られない場合の終了値用テキスト */
	private Text m_waitEndValue = null;

	/** メッセージ情報の引継ぎ用ボタン */
	private Button m_inheritButton = null;

	/** シェル */
	private Shell m_shell = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;
	/** ファシリティID */
	private String m_facilityId = null;
	/** スコープ */
	private String m_facilityPath = null;
	/** ファシリティID（固定値用） */
	private String m_facilityIdFixed = null;

	/** ジョブ連携待機情報 */
	private JobLinkRcvInfoResponse m_info;
	/** 選択アイテム(拡張情報) */
	private ArrayList<Object> m_expSelectItem = null;

	/** 引継ぎ情報 */
	private List<JobLinkInheritInfoResponse> m_inheritList = new ArrayList<>();

	/** 読み取り専用フラグ */
	private boolean m_readOnly = false;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public JobLinkRcvComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		Label label = null;

		this.setLayout(JobDialogUtil.getParentLayout());

		// スコープ（グループ）
		Group cmdScopeGroup = new Group(this, SWT.NONE);
		cmdScopeGroup.setText(Messages.getString("source.scope"));
		cmdScopeGroup.setLayout(new GridLayout(3, false));

		// スコープ：ジョブ変数（ラジオ）
		this.m_scopeJobParamRadio = new Button(cmdScopeGroup, SWT.RADIO);
		this.m_scopeJobParamRadio.setText(Messages.getString("job.parameter") + " : ");
		this.m_scopeJobParamRadio.setLayoutData(
				new GridData(120, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeJobParamRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_scopeJobParamText.setEditable(true);
					m_scopeFixedValueRadio.setSelection(false);
					m_scopeFixedValueSelect.setEnabled(false);
					m_facilityId = m_scopeJobParamText.getText();
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// スコープ：ジョブ変数（テキスト）
		this.m_scopeJobParamText = new Text(cmdScopeGroup, SWT.BORDER);
		this.m_scopeJobParamText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scopeJobParamText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
				if (m_scopeJobParamRadio.getSelection()) {
					m_facilityId = m_scopeJobParamText.getText();
				}
			}
		});

		//dummy
		new Label(cmdScopeGroup, SWT.LEFT);

		// スコープ：固定値（ラジオ）
		this.m_scopeFixedValueRadio = new Button(cmdScopeGroup, SWT.RADIO);
		this.m_scopeFixedValueRadio.setText(Messages.getString("fixed.value") + " : ");
		this.m_scopeFixedValueRadio.setLayoutData(new GridData(120,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValueRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_scopeFixedValueSelect.setEnabled(true);
					m_scopeJobParamRadio.setSelection(false);
					m_scopeJobParamText.setEditable(false);
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// スコープ：固定値（テキスト）
		this.m_scopeFixedValueText = new Text(cmdScopeGroup, SWT.BORDER | SWT.READ_ONLY);
		this.m_scopeFixedValueText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scopeFixedValueText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// スコープ：参照
		this.m_scopeFixedValueSelect = new Button(cmdScopeGroup, SWT.NONE);
		this.m_scopeFixedValueSelect.setText(Messages.getString("refer"));
		this.m_scopeFixedValueSelect.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValueSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityInfoResponse info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityIdFixed = info.getFacilityId();
					m_scopeFixedValueText.setText(m_facilityPath);
					update();
				}
			}
		});

		// 過去に発生したジョブ連携メッセージを確認する
		Composite pastComposite = new Composite(this, SWT.NONE);
		pastComposite.setLayout(new GridLayout(4, false));

		// 過去に発生したジョブ連携メッセージを確認する（チェックボックス）
		this.m_past = new Button(pastComposite, SWT.CHECK);
		this.m_past.setText(Messages.getString("joblink.check.past.message"));
		this.m_past.setLayoutData(new GridData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_past.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_pastMin.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 対象期間（ラベル）
		label = new Label(pastComposite, SWT.NONE);
		label.setText(Messages.getString("target.period") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)label.getLayoutData()).horizontalAlignment = SWT.END;

		// 対象期間（テキスト）
		this.m_pastMin = new Text(pastComposite, SWT.BORDER);
		this.m_pastMin.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_pastMin.addVerifyListener(
				new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));
		this.m_pastMin.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 対象期間：時間単位（ラベル）
		label = new Label(pastComposite, SWT.LEFT);
		label.setText(Messages.getString("min"));
		label.setLayoutData(new GridData(30, SizeConstant.SIZE_LABEL_HEIGHT));

		// 条件（グループ）
		Group ruleGroup = new Group(this, SWT.NONE);
		ruleGroup.setText(Messages.getString("monitor.rule"));
		ruleGroup.setLayout(new GridLayout(3, false));

		// ジョブ連携メッセージID(ラベル)
		label = new Label(ruleGroup, SWT.NONE);
		label.setText(Messages.getString("joblink.message.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ連携メッセージID
		this.m_joblinkMessageId = new Text(ruleGroup, SWT.BORDER);
		this.m_joblinkMessageId.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_joblinkMessageId.getLayoutData()).horizontalSpan = 2;
		this.m_joblinkMessageId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 重要度（グループ）
		Group priorityGroup = new Group(ruleGroup, SWT.NONE);
		priorityGroup.setText(Messages.getString("priority"));
		priorityGroup.setLayoutData(new GridData());
		((GridData)priorityGroup.getLayoutData()).horizontalSpan = 3;
		priorityGroup.setLayout(new GridLayout(8, true));

		// 重要度(レイアウト)
		GridData priorityLabelGridData = new GridData(60, SizeConstant.SIZE_BUTTON_HEIGHT);
		priorityLabelGridData.horizontalAlignment = GridData.FILL;
		priorityLabelGridData.grabExcessHorizontalSpace = true;
		GridData priorityCheckGridData = new GridData(30, SizeConstant.SIZE_BUTTON_HEIGHT);
		priorityCheckGridData.horizontalAlignment = GridData.CENTER;
		priorityCheckGridData.grabExcessHorizontalSpace = true;

		// 重要度：情報
		label = new Label(priorityGroup, SWT.NONE);
		label.setLayoutData(priorityLabelGridData);
		label.setText(Messages.getString("info") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_INFO);
		m_infoValid = new Button(priorityGroup, SWT.CHECK);
		m_infoValid.setLayoutData(priorityCheckGridData);

		// 重要度：警告
		label = new Label(priorityGroup, SWT.NONE);
		label.setLayoutData(priorityLabelGridData);
		label.setText(Messages.getString("warning") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_WARNING);
		m_warnValid = new Button(priorityGroup, SWT.CHECK);
		m_warnValid.setLayoutData(priorityCheckGridData);

		// 重要度：危険
		label = new Label(priorityGroup, SWT.NONE);
		label.setLayoutData(priorityLabelGridData);
		label.setText(Messages.getString("critical") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_CRITICAL);
		m_criticalValid = new Button(priorityGroup, SWT.CHECK);
		m_criticalValid.setLayoutData(priorityCheckGridData);

		// 重要度：不明
		label = new Label(priorityGroup, SWT.NONE);
		label.setLayoutData(priorityLabelGridData);
		label.setText(Messages.getString("unknown") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_UNKNOWN);
		m_unknownValid = new Button(priorityGroup, SWT.CHECK);
		m_unknownValid.setLayoutData(priorityCheckGridData);

		// アプリケーション(チェックボックス)
		m_application = new Button(ruleGroup, SWT.CHECK);
		m_application.setText(Messages.getString("application") + " : ");
		m_application.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_application.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		m_application.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_applicationText.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// アプリケーション(テキスト)
		this.m_applicationText = new Text(ruleGroup, SWT.BORDER);
		this.m_applicationText.setLayoutData(new GridData(280, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_applicationText.getLayoutData()).horizontalSpan = 2;
		this.m_applicationText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 監視詳細(チェックボックス)
		m_monitorDetailId = new Button(ruleGroup, SWT.CHECK);
		m_monitorDetailId.setText(Messages.getString("monitor.detail.id") + " : ");
		m_monitorDetailId.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_monitorDetailId.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		m_monitorDetailId.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_monitorDetailIdText.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 監視詳細(テキスト)
		this.m_monitorDetailIdText = new Text(ruleGroup, SWT.BORDER);
		this.m_monitorDetailIdText.setLayoutData(new GridData(280, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_monitorDetailIdText.getLayoutData()).horizontalSpan = 2;
		this.m_monitorDetailIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージ(チェックボックス)
		m_message = new Button(ruleGroup, SWT.CHECK);
		m_message.setText(Messages.getString("message") + " : ");
		m_message.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_message.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		m_message.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_messageText.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// メッセージ(テキスト)
		this.m_messageText = new Text(ruleGroup, SWT.BORDER);
		this.m_messageText.setLayoutData(new GridData(280, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_messageText.getLayoutData()).horizontalSpan = 2;
		this.m_messageText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 拡張情報(チェックボックス)
		m_exp = new Button(ruleGroup, SWT.CHECK);
		m_exp.setText(Messages.getString("extended.info") + " : ");
		m_exp.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_exp.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		((GridData)m_exp.getLayoutData()).verticalSpan = 3;
		((GridData)m_exp.getLayoutData()).verticalAlignment = SWT.TOP;
		m_exp.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_expCreateButton.setEnabled(check.getSelection());
				m_expDeleteButton.setEnabled(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 拡張情報
		Table table = new Table(ruleGroup, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(280, 40));
		((GridData)table.getLayoutData()).verticalSpan = 3;
		((GridData)table.getLayoutData()).verticalAlignment = SWT.TOP;

		// ボタン：追加（ボタン）
		m_expCreateButton = new Button(ruleGroup, SWT.NONE);
		this.m_expCreateButton.setText(Messages.getString("add"));
		this.m_expCreateButton.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_expCreateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobLinkExpAddDialog dialog = new JobLinkExpAddDialog(m_shell);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobLinkExpInfoResponse info = dialog.getInputData();
					@SuppressWarnings("unchecked")
					ArrayList<Object> list = (ArrayList<Object>) m_expTableViewer.getInput();
					
					if (list == null) {
						list = new ArrayList<>();
					}
					String infoKey = info.getKey();
					for (Object obj : list) {
						@SuppressWarnings("unchecked")
						String key = (String) ((ArrayList<Object>)obj).get(0);
						if (infoKey.equals(key)) {
							// キーの重複エラー
							MessageDialog.openWarning(null, Messages.getString("warning"),
									Messages.getString("message.common.16",
											new String[]{Messages.getString("key")}));
							return;
						}
					}
					ArrayList<Object> tableLineData = new ArrayList<Object>();
					tableLineData.add(info.getKey());
					tableLineData.add(info.getValue());
					list.add(tableLineData);
					m_expTableViewer.setInput(list);
				}
			}
		});

		// ボタン：削除（ボタン）
		this.m_expDeleteButton = new Button(ruleGroup, SWT.NONE);
		this.m_expDeleteButton.setText(Messages.getString("delete"));
		this.m_expDeleteButton.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_expDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<?> list = (ArrayList<?>) m_expTableViewer.getInput();
				list.remove(m_expSelectItem);
				m_expSelectItem = null;
				m_expTableViewer.setInput(list);
			}
		});

		// dummy
		label = new Label(ruleGroup, SWT.NONE);
		label.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 終了値（グループ）
		Group endValueGroup = new Group(this, SWT.NONE);
		endValueGroup.setText(Messages.getString("end.value"));
		endValueGroup.setLayout(new GridLayout(8, true));

		// メッセージが得られたら常に（チェックボックス）
		this.m_allEnd = new Button(endValueGroup, SWT.CHECK);
		this.m_allEnd.setText(Messages.getString("joblink.all.end") + " : ");
		this.m_allEnd.setLayoutData(new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_allEnd.getLayoutData()).horizontalSpan = 4;
		this.m_allEnd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_allEndValue.setEditable(true);
					m_infoEndValue.setEditable(false);
					m_warnEndValue.setEditable(false);
					m_criticalEndValue.setEditable(false);
					m_unknownEndValue.setEditable(false);
				} else {
					m_allEndValue.setEditable(false);
					m_infoEndValue.setEditable(true);
					m_warnEndValue.setEditable(true);
					m_criticalEndValue.setEditable(true);
					m_unknownEndValue.setEditable(true);
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// メッセージが得られたら常に：終了値（テキスト）
		this.m_allEndValue = new Text(endValueGroup, SWT.BORDER);
		this.m_allEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_allEndValue.getLayoutData()).horizontalSpan = 2;
		this.m_allEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_allEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(endValueGroup, SWT.NONE);
		label = new Label(endValueGroup, SWT.NONE);

		// 終了値：情報（ラベル）
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("info") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_INFO);
		label.setLayoutData(new GridData(60, SizeConstant.SIZE_TEXT_HEIGHT));

		// 終了値：情報（テキスト）
		this.m_infoEndValue = new Text(endValueGroup, SWT.BORDER | SWT.LEFT);
		this.m_infoEndValue.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_infoEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_infoEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 終了値：警告（ラベル）
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("warning") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_WARNING);
		label.setLayoutData(new GridData(60, SizeConstant.SIZE_TEXT_HEIGHT));

		// 終了値：警告（テキスト）
		this.m_warnEndValue = new Text(endValueGroup, SWT.BORDER | SWT.LEFT);
		this.m_warnEndValue.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_warnEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_warnEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 終了値：危険（ラベル）
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("critical") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_CRITICAL);
		label.setLayoutData(new GridData(60, SizeConstant.SIZE_TEXT_HEIGHT));

		// 終了値：危険（テキスト）
		this.m_criticalEndValue = new Text(endValueGroup, SWT.BORDER | SWT.LEFT);
		this.m_criticalEndValue.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_criticalEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_criticalEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 終了値：不明（ラベル）
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("unknown") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_UNKNOWN);
		label.setLayoutData(new GridData(60, SizeConstant.SIZE_TEXT_HEIGHT));

		// 終了値：不明（テキスト）
		this.m_unknownEndValue = new Text(endValueGroup, SWT.BORDER | SWT.LEFT);
		this.m_unknownEndValue.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_unknownEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_unknownEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 終了値：メッセージが得られない場合
		m_failureEndFlg = new Button(endValueGroup, SWT.CHECK);
		m_failureEndFlg.setText(Messages.getString("joblink.rcv.result.end"));
		m_failureEndFlg.setLayoutData(new GridData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_failureEndFlg.getLayoutData()).horizontalSpan = 8;
		m_failureEndFlg.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_waitTime.setEditable(check.getSelection());
				m_waitEndValue.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// メッセージが得られない場合：時間（ラベル）
		label = new Label(endValueGroup, SWT.LEFT);
		label.setText(String.format("%s (%s) : ", Messages.getString("time.out"), Messages.getString("min")));
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)label.getLayoutData()).horizontalSpan = 2;

		// メッセージが得られない場合：時間（テキスト）
		this.m_waitTime = new Text(endValueGroup, SWT.BORDER);
		this.m_waitTime.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)m_waitTime.getLayoutData()).horizontalSpan = 2;
		this.m_waitTime.addVerifyListener(
				new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));
		this.m_waitTime.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージが得られない場合：終了値（ラベル）
		label = new Label(endValueGroup, SWT.RIGHT);
		label.setText("    " + Messages.getString("end.value") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)label.getLayoutData()).horizontalSpan = 2;

		// メッセージが得られない場合：終了値（テキスト）
		this.m_waitEndValue = new Text(endValueGroup, SWT.BORDER);
		this.m_waitEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)m_waitEndValue.getLayoutData()).horizontalSpan = 2;
		this.m_waitEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_waitEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージ情報の引継ぎ（Composite）
		Composite inheritComposite = new Composite(this, SWT.NONE);
		inheritComposite.setLayout(new GridLayout(2, false));

		// メッセージ情報の引継ぎ（ボタン）
		m_inheritButton = new Button(inheritComposite, SWT.NONE);
		m_inheritButton.setText(Messages.getString("job.joblink.inherit"));
		m_inheritButton.setLayoutData(new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_inheritButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobLinkRcvInheritListDialog dialog = new JobLinkRcvInheritListDialog(m_shell, m_readOnly);
				dialog.setInput(m_inheritList);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_inheritList.clear();
					m_inheritList.addAll(dialog.getInput());
				}
			}
		});

		this.m_expTableViewer = new CommonTableViewer(table);
		this.m_expTableViewer.createTableColumn(GetJobLinkExpTableDefine.get(),
				GetJobLinkExpTableDefine.SORT_COLUMN_INDEX,
				GetJobLinkExpTableDefine.SORT_ORDER);
		this.m_expTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					//選択行を取得
					@SuppressWarnings("unchecked")
					ArrayList<Object> info = (ArrayList<Object>) ((StructuredSelection) event.getSelection()).getFirstElement();
					setExpSelectItem(info);
				} else {
					setExpSelectItem(null);
				}
			}
		});
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if(m_scopeFixedValueRadio.getSelection() && "".equals(this.m_scopeFixedValueText.getText())){
			this.m_scopeFixedValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scopeFixedValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_scopeJobParamRadio.getSelection() && "".equals(this.m_scopeJobParamText.getText())){
			this.m_scopeJobParamText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scopeJobParamText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_pastMin.getEditable() && "".equals(m_pastMin.getText())) {
			this.m_pastMin.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_pastMin.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_joblinkMessageId.getEditable() && "".equals(m_joblinkMessageId.getText())) {
			this.m_joblinkMessageId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_joblinkMessageId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_applicationText.getEditable() && "".equals(m_applicationText.getText())) {
			this.m_applicationText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_applicationText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_monitorDetailIdText.getEditable() && "".equals(m_monitorDetailIdText.getText())) {
			this.m_monitorDetailIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_monitorDetailIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_messageText.getEditable() && "".equals(m_messageText.getText())) {
			this.m_messageText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_messageText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_allEndValue.getEditable() && "".equals(this.m_allEndValue.getText())) {
			this.m_allEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_allEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_infoEndValue.getEditable() && "".equals(this.m_infoEndValue.getText())){
			this.m_infoEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_infoEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_warnEndValue.getEditable() && "".equals(this.m_warnEndValue.getText())){
			this.m_warnEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_warnEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_criticalEndValue.getEditable() && "".equals(this.m_criticalEndValue.getText())){
			this.m_criticalEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_criticalEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_unknownEndValue.getEditable() && "".equals(this.m_unknownEndValue.getText())){
			this.m_unknownEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_unknownEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_waitEndValue.getEditable() && "".equals(this.m_waitEndValue.getText())){
			this.m_waitEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_waitEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_waitTime.getEditable() && "".equals(this.m_waitTime.getText())){
			this.m_waitTime.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_waitTime.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブ連携待機情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobLinkRcvInfo
	 */
	public void reflectJobLinkRcvInfo() {

		// 初期値
		this.m_scopeFixedValueRadio.setSelection(true);
		this.m_scopeFixedValueText.setText("");
		//スコープ（ジョブ変数）の初期値は"#[FACILITY_ID]"とする
		this.m_scopeJobParamRadio.setSelection(false);
		this.m_scopeJobParamText.setText(SystemParameterConstant.getParamText(SystemParameterConstant.FACILITY_ID));
		this.m_past.setSelection(false);
		this.m_pastMin.setText(String.valueOf(JobLinkConstant.RCV_INITIAL_PAST_MIN));
		this.m_joblinkMessageId.setText("");
		this.m_infoValid.setSelection(true);
		this.m_warnValid.setSelection(true);
		this.m_criticalValid.setSelection(true);
		this.m_unknownValid.setSelection(true);
		this.m_application.setSelection(false);
		this.m_applicationText.setText("");
		this.m_monitorDetailId.setSelection(false);
		this.m_monitorDetailIdText.setText("");
		this.m_message.setSelection(false);
		this.m_messageText.setText("");
		this.m_exp.setSelection(false);
		this.m_allEndValue.setText(String.valueOf(JobLinkConstant.RCV_INITIAL_END_VALUE_ALL));
		this.m_infoEndValue.setText(String.valueOf(JobLinkConstant.RCV_INITIAL_END_VALUE_INFO));
		this.m_warnEndValue.setText(String.valueOf(JobLinkConstant.RCV_INITIAL_END_VALUE_WARN));
		this.m_criticalEndValue.setText(String.valueOf(JobLinkConstant.RCV_INITIAL_END_VALUE_CRITICAL));
		this.m_unknownEndValue.setText(String.valueOf(JobLinkConstant.RCV_INITIAL_END_VALUE_UNKNOWN));
		this.m_failureEndFlg.setSelection(true);
		this.m_waitEndValue.setText(String.valueOf(JobLinkConstant.RCV_INITIAL_END_VALUE_TIMEOUT));
		this.m_waitTime.setText(String.valueOf(JobLinkConstant.RCV_INITIAL_TIMEOUT_MIN));

		if (m_info != null) {
			//スコープ設定
			m_facilityPath = HinemosMessage.replace(m_info.getScope());
			m_facilityId = m_info.getFacilityID();
			if(isParamFormat(m_facilityId)){
				//ファシリティIDがジョブ変数の場合
				m_facilityPath = "";
				m_scopeJobParamRadio.setSelection(true);
				m_scopeJobParamText.setText(m_facilityId);
				m_scopeFixedValueRadio.setSelection(false);
				m_scopeFixedValueText.setText("");
			} else{
				if (m_facilityPath != null && m_facilityPath.length() > 0) {
					m_scopeFixedValueText.setText(m_facilityPath);
					m_facilityIdFixed = m_facilityId;
				}
				m_scopeJobParamRadio.setSelection(false);
				m_scopeFixedValueRadio.setSelection(true);
			}

			// 過去に発生したジョブ連携メッセージを確認する
			m_past.setSelection(m_info.getPastFlg());
			if (m_info.getPastMin() != null) {
				m_pastMin.setText(m_info.getPastMin().toString());
			}

			// 条件
			if (m_info.getJoblinkMessageId() != null) {
				this.m_joblinkMessageId.setText(m_info.getJoblinkMessageId());
			}
			this.m_infoValid.setSelection(m_info.getInfoValidFlg());
			this.m_warnValid.setSelection(m_info.getWarnValidFlg());
			this.m_criticalValid.setSelection(m_info.getCriticalValidFlg());
			this.m_unknownValid.setSelection(m_info.getUnknownValidFlg());
			this.m_application.setSelection(m_info.getApplicationFlg());
			if (m_info.getApplication() != null) {
				this.m_applicationText.setText(m_info.getApplication());
			}
			this.m_monitorDetailId.setSelection(m_info.getMonitorDetailIdFlg());
			if (m_info.getMonitorDetailId() != null) {
				this.m_monitorDetailIdText.setText(m_info.getMonitorDetailId());
			}
			this.m_message.setSelection(m_info.getMessageFlg());
			if (m_info.getMessage() != null) {
				this.m_messageText.setText(m_info.getMessage());
			}
			this.m_exp.setSelection(m_info.getExpFlg());

			// 拡張情報
			List<JobLinkExpInfoResponse> list = m_info.getJobLinkExpList();
			if(list != null){
				ArrayList<Object> tableData = new ArrayList<Object>();
				for (int i = 0; i < list.size(); i++) {
					JobLinkExpInfoResponse info = list.get(i);
					ArrayList<Object> tableLineData = new ArrayList<Object>();
					tableLineData.add(info.getKey());
					tableLineData.add(info.getValue());
					tableData.add(tableLineData);
				}
				m_expTableViewer.setInput(tableData);
			}

			// メッセージが得られたら常に
			m_allEnd.setSelection(this.m_info.getMonitorAllEndValueFlg());
			if (this.m_info.getMonitorAllEndValue() != null) {
				this.m_allEndValue.setText(this.m_info.getMonitorAllEndValue().toString());
			}

			// 終了値（情報）
			if (this.m_info.getMonitorInfoEndValue() != null) {
				this.m_infoEndValue.setText(this.m_info.getMonitorInfoEndValue().toString());
			}
	
			// 終了値（警告）
			if (this.m_info.getMonitorWarnEndValue() != null) {
				this.m_warnEndValue.setText(this.m_info.getMonitorWarnEndValue().toString());
			}
	
			// 終了値（危険）
			if (this.m_info.getMonitorCriticalEndValue() != null) {
				this.m_criticalEndValue.setText(this.m_info.getMonitorCriticalEndValue().toString());
			}
	
			// 終了値（不明）
			if (this.m_info.getMonitorUnknownEndValue() != null) {
				this.m_unknownEndValue.setText(this.m_info.getMonitorUnknownEndValue().toString());
			}
	
			// メッセージが得られない場合
			m_failureEndFlg.setSelection(this.m_info.getFailureEndFlg());
			if (this.m_info.getMonitorWaitTime() != null) {
				this.m_waitTime.setText(this.m_info.getMonitorWaitTime().toString());
			}
			if (this.m_info.getMonitorWaitEndValue() != null) {
				this.m_waitEndValue.setText(this.m_info.getMonitorWaitEndValue().toString());
			}
		}
		// オブジェクトの有効/無効設定
		m_pastMin.setEditable(m_past.getSelection());
		m_applicationText.setEditable(m_application.getSelection());
		m_monitorDetailIdText.setEditable(m_monitorDetailId.getSelection());
		m_messageText.setEditable(m_message.getSelection());
		m_expCreateButton.setEnabled(m_exp.getSelection());
		m_expDeleteButton.setEnabled(m_exp.getSelection());
		m_allEndValue.setEditable(m_allEnd.getSelection());
		m_infoEndValue.setEditable(!m_allEnd.getSelection());
		m_waitEndValue.setEditable(!m_allEnd.getSelection());
		m_criticalEndValue.setEditable(!m_allEnd.getSelection());
		m_unknownEndValue.setEditable(!m_allEnd.getSelection());
		m_waitTime.setEditable(m_failureEndFlg.getSelection());
		m_waitEndValue.setEditable(m_failureEndFlg.getSelection());

		update();
	}

	/**
	 * ジョブ連携待機情報を設定します。
	 *
	 * @param info ジョブ連携待機情報
	 */
	public void setJobLinkRcvInfo(JobLinkRcvInfoResponse info) {
		m_info = info;
		if (m_info != null && m_info.getJobLinkInheritList() != null) {
			m_inheritList.clear();
			m_inheritList.addAll(m_info.getJobLinkInheritList());
		}
	}

	/**
	 * ジョブ連携待機情報を返します。
	 *
	 * @return ジョブ連携待機情報
	 */
	public JobLinkRcvInfoResponse getJobLinkRcvInfo() {
		return m_info;
	}

	/**
	 * コンポジットの情報から、ジョブ連携待機情報を作成する。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult createJobLinkRcvInfo() {
		ValidateResult result = null;

		// インスタンスを作成
		m_info = JobTreeItemUtil.createJobLinkRcvInfoResponse();

		//スコープ取得
		if(m_scopeJobParamRadio.getSelection()){
			if (isParamFormat(m_facilityId)) {
				//ジョブ変数の場合
				m_info.setFacilityID(m_facilityId);
				m_info.setScope("");
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.4"));
				return result;
			}
		}
		else{
			//固定値の場合
			if (m_facilityIdFixed != null && m_facilityIdFixed.length() > 0){
				m_info.setFacilityID(m_facilityIdFixed);
				m_info.setScope(m_facilityPath);
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.3"));
				return result;
			}
		}

		// 過去に発生したジョブ連携メッセージを確認する
		m_info.setPastFlg(this.m_past.getSelection());
		if (m_info.getPastFlg()
				|| !m_pastMin.getText().isEmpty()) {
			try {
				m_info.setPastMin(Integer.parseInt(m_pastMin.getText()));
			} catch (NumberFormatException e) {
				if (m_info.getPastFlg()) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.common.1", 
							new String[]{Messages.getString("target.period")}));
					return result;
				}
			}
		}

		// ジョブ連携メッセージID
		if (m_joblinkMessageId.getText().isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("joblink.message.id")}));
			return result;
		}
		m_info.setJoblinkMessageId(m_joblinkMessageId.getText());

		// 重要度
		if (!m_infoValid.getSelection()
				&& !m_warnValid.getSelection()
				&& !m_criticalValid.getSelection()
				&& !m_unknownValid.getSelection()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.17", 
					new String[]{Messages.getString("priority")}));
			return result;
		}
		// 重要度（情報）有効/無効
		m_info.setInfoValidFlg(m_infoValid.getSelection());
		// 重要度（警告）有効/無効
		m_info.setWarnValidFlg(m_warnValid.getSelection());
		// 重要度（危険）有効/無効
		m_info.setCriticalValidFlg(m_criticalValid.getSelection());
		// 重要度（不明）有効/無効
		m_info.setUnknownValidFlg(m_unknownValid.getSelection());

		// アプリケーションフラグ
		m_info.setApplicationFlg(m_application.getSelection());
		// アプリケーション
		if (m_info.getApplicationFlg() && m_applicationText.getText().isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("application")}));
			return result;
		}
		m_info.setApplication(m_applicationText.getText());

		// 監視詳細フラグ
		m_info.setMonitorDetailIdFlg(m_monitorDetailId.getSelection());
		// 監視詳細
		if (m_info.getMonitorDetailIdFlg() && m_monitorDetailIdText.getText().isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("monitor.detail.id")}));
			return result;
		}
		m_info.setMonitorDetailId(m_monitorDetailIdText.getText());

		// メッセージフラグ
		m_info.setMessageFlg(m_message.getSelection());
		// メッセージ
		if (m_info.getMessageFlg() && m_messageText.getText().isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("message")}));
			return result;
		}
		m_info.setMessage(m_messageText.getText());

		// 拡張情報フラグ
		m_info.setExpFlg(m_exp.getSelection());
		// 拡張情報
		ArrayList<JobLinkExpInfoResponse> expList = new ArrayList<>();
		ArrayList<?> tableData = (ArrayList<?>) m_expTableViewer.getInput();
		if (tableData != null) {
			for (int i = 0; i < tableData.size(); i++) {
				ArrayList<?> tableLineData = (ArrayList<?>) tableData.get(i);
				JobLinkExpInfoResponse expInfo = new JobLinkExpInfoResponse();
				String key = (String) tableLineData.get(GetJobLinkExpTableDefine.KEY);
				expInfo.setKey(key);
				expInfo.setValue((String) tableLineData.get(GetJobLinkExpTableDefine.VALUE));
				expList.add(expInfo);
			}
		}
		if (m_info.getExpFlg() && expList.isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("extended.info")}));
			return result;
		}
		m_info.getJobLinkExpList().clear();
		m_info.getJobLinkExpList().addAll(expList);

		// 常に
		m_info.setMonitorAllEndValueFlg(this.m_allEnd.getSelection());

		// 終了値（常に）
		if (m_info.getMonitorAllEndValueFlg()
				|| !m_allEndValue.getText().isEmpty()) {
			try {
				this.m_info.setMonitorAllEndValue(Integer.parseInt(m_allEndValue.getText()));
			} catch (NumberFormatException e) {
				if (m_info.getMonitorAllEndValueFlg()) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.173", 
							new String[]{Messages.getString("joblink.all.end")}));
					return result;
				}
			}
		}

		// 終了値（情報）
		if (!m_info.getMonitorAllEndValueFlg()
				|| !m_infoEndValue.getText().isEmpty()) {
			try {
				this.m_info.setMonitorInfoEndValue(Integer.parseInt(m_infoEndValue.getText()));
			} catch (NumberFormatException e) {
				if (!m_info.getMonitorAllEndValueFlg()) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.173", 
							new String[]{PriorityMessage.typeToString(PriorityConstant.TYPE_INFO)}));
					return result;
				}
			}
		}

		// 終了値（警告）
		if (!m_info.getMonitorAllEndValueFlg()
				|| !m_warnEndValue.getText().isEmpty()) {
			try {
				this.m_info.setMonitorWarnEndValue(Integer.parseInt(m_warnEndValue.getText()));
			} catch (NumberFormatException e) {
				if (!m_info.getMonitorAllEndValueFlg()) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.173", 
							new String[]{PriorityMessage.typeToString(PriorityConstant.TYPE_WARNING)}));
					return result;
				}
			}
		}

		// 終了値（危険）
		if (!m_info.getMonitorAllEndValueFlg()
				|| !m_criticalEndValue.getText().isEmpty()) {
			try {
				this.m_info.setMonitorCriticalEndValue(Integer.parseInt(m_criticalEndValue.getText()));
			} catch (NumberFormatException e) {
				if (!m_info.getMonitorAllEndValueFlg()) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.173", 
							new String[]{PriorityMessage.typeToString(PriorityConstant.TYPE_CRITICAL)}));
					return result;
				}
			}
		}

		// 終了値（不明）
		if (!m_info.getMonitorAllEndValueFlg()
				|| !m_unknownEndValue.getText().isEmpty()) {
			try {
				this.m_info.setMonitorUnknownEndValue(Integer.parseInt(m_unknownEndValue.getText()));
			} catch (NumberFormatException e) {
				if (!m_info.getMonitorAllEndValueFlg()) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.173", 
							new String[]{PriorityMessage.typeToString(PriorityConstant.TYPE_UNKNOWN)}));
					return result;
				}
			}
		}

		// メッセージが得られない場合に終了する
		m_info.setFailureEndFlg(m_failureEndFlg.getSelection());

		// メッセージが得られない場合に終了する（タイムアウト、終了値）
		if (m_info.getFailureEndFlg()
				|| !m_waitTime.getText().isEmpty()) {
			try {
				this.m_info.setMonitorWaitTime(Integer.parseInt(m_waitTime.getText()));
			} catch (NumberFormatException e) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.174"));
				return result;
			}
		}
		if (m_info.getFailureEndFlg()
				|| !m_waitEndValue.getText().isEmpty()) {
			try {
				this.m_info.setMonitorWaitEndValue(Integer.parseInt(m_waitEndValue.getText()));
			} catch (NumberFormatException e) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.175"));
				return result;
			}
		}

		// 引継ぎ情報
		this.m_info.getJobLinkInheritList().clear();
		if (this.m_inheritList != null) {
			this.m_info.getJobLinkInheritList().addAll(m_inheritList);
		}
		return null;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_scopeFixedValueText.setEditable(false);
		this.m_scopeJobParamText.setEditable(this.m_scopeJobParamRadio.getSelection() && enabled);
		this.m_scopeJobParamRadio.setEnabled(enabled);
		this.m_scopeFixedValueRadio.setEnabled(enabled);
		this.m_scopeFixedValueSelect.setEnabled(enabled);
		this.m_past.setEnabled(enabled);
		this.m_pastMin.setEditable(this.m_past.getSelection() && enabled);
		this.m_joblinkMessageId.setEditable(enabled);
		this.m_infoValid.setEnabled(enabled);
		this.m_warnValid.setEnabled(enabled);
		this.m_criticalValid.setEnabled(enabled);
		this.m_unknownValid.setEnabled(enabled);
		this.m_application.setEnabled(enabled);
		this.m_applicationText.setEditable(this.m_application.getSelection() && enabled);
		this.m_monitorDetailId.setEnabled(enabled);
		this.m_monitorDetailIdText.setEditable(this.m_monitorDetailId.getSelection() && enabled);
		this.m_message.setEnabled(enabled);
		this.m_messageText.setEditable(this.m_message.getSelection() && enabled);
		this.m_exp.setEnabled(enabled);
		this.m_expCreateButton.setEnabled(this.m_exp.getSelection() && enabled);
		this.m_expDeleteButton.setEnabled(this.m_exp.getSelection() && enabled);
		this.m_allEnd.setEnabled(enabled);
		this.m_allEndValue.setEditable(this.m_allEnd.getSelection() && enabled);
		this.m_infoEndValue.setEditable(!this.m_allEnd.getSelection() && enabled);
		this.m_warnEndValue.setEditable(!this.m_allEnd.getSelection() && enabled);
		this.m_criticalEndValue.setEditable(!this.m_allEnd.getSelection() && enabled);
		this.m_unknownEndValue.setEditable(!this.m_allEnd.getSelection() && enabled);
		this.m_failureEndFlg.setEnabled(enabled);
		this.m_waitTime.setEditable(this.m_failureEndFlg.getSelection() && enabled);
		this.m_waitEndValue.setEditable(this.m_failureEndFlg.getSelection() && enabled);
		this.m_readOnly = !enabled;
	}

	/**
	 * 拡張情報の選択アイテムを設定します。
	 *
	 * @param expSelectItem 選択アイテム
	 */
	public void setExpSelectItem(ArrayList<Object> expSelectItem) {
		m_expSelectItem = expSelectItem;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_scopeFixedValueText.setText("");
		this.m_facilityId = null;
	}

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
	}

	/**
	 * strがジョブ変数の書式(#[xxx])かどうかを判定する
	 * 
	 * @param str
	 * @return
	 */
	private boolean isParamFormat(String str) {
		if (str == null) {
			return false;
		}
		return str.startsWith(SystemParameterConstant.PREFIX)
				&& str.endsWith(SystemParameterConstant.SUFFIX);
	}

}

