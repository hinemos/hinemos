/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobRpaInfoResponse.RpaAbnormalExitNotifyPriorityEnum;
import org.openapitools.client.model.JobRpaInfoResponse.RpaAlreadyRunningNotifyPriorityEnum;
import org.openapitools.client.model.JobRpaInfoResponse.RpaNotLoginNotifyPriorityEnum;
import org.openapitools.client.model.JobRpaInfoResponse.RpaScreenshotEndValueConditionEnum;
import org.openapitools.client.model.JobRpaLoginResolutionResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobRpaReturnCodeConditionMessage;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.rpa.util.ReturnCodeConditionChecker;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオ 直接実行 制御タブ用のコンポジットクラスです
 */
public class RpaDirectControlComposite extends Composite {
	/** ロガー */
	private static final Log m_log = LogFactory.getLog(RpaDirectControlComposite.class);
	/** シナリオ実行前後でOSへログイン・ログアウトするチェックボタン */
	private Button m_loginButton = null;
	/** ユーザIDテキスト */
	private Text m_loginUserIdText = null;
	/** パスワードテキスト */
	private Text m_loginPasswordText = null;
	/** 解像度コンボボックス */
	private ComboViewer m_resolutionComboViewer = null;
	/** ログインできない場合 リトライ回数テキスト */
	private Text m_loginFailureRetryText = null;
	/** ログインできない場合 終了値テキスト */
	private Text m_loginFailureEndValueText = null;
	/** 異常発生時もログアウトする */
	private Button m_logoutOnErrorButton = null;
	/** ログインされていない場合 通知チェックボタン */
	private Button m_notLoginNotifyButton = null;
	/** ログインされていない場合 重要度コンボボックス */
	private ComboViewer m_notLoginNotifyPriorityComboViewer = null;
	/** ログインされていない場合 終了値テキスト */
	private Text m_notLoginEndValueText = null;
	/** 起動前にRPAツールがすでに動作している場合 通知チェックボタン */
	private Button m_rpaRunningNotifyButton = null;
	/** 起動前にRPAツールがすでに動作している場合 重要度コンボボックス */
	private ComboViewer m_rpaRunningNotifyPriorityComboViewer = null;
	/** 起動前にRPAツールがすでに動作している場合 終了値テキスト */
	private Text m_rpaRunningEndValueText = null;
	/** RPAツールが異常終了した場合 通知チェックボタン */
	private Button m_rpaAbnormalExitNotifyButton = null;
	/** RPAツールが異常終了した場合 重要度コンボボックス */
	private ComboViewer m_rpaAbnormalExitNotifyPriorityComboViewer = null;
	/** RPAツールが異常終了した場合 終了値テキスト */
	private Text m_rpaAbnormalExitEndValueText = null;
	/** 終了遅延発生時、スクリーンショットを取得するチェックボタン */
	private Button m_screenshotEndDelayButton = null;
	/** 以下の終了値の場合、スクリーンショットを取得するチェックボタン */
	private Button m_screenshotEndValueButton = null;
	/** 以下の終了値の場合、スクリーンショットを取得する 終了値テキスト */
	private Text m_screenshotEndValueText = null;
	/** 以下の終了値の場合、スクリーンショットを取得する 判定条件コンボボックス */
	private ComboViewer m_screenshotEndValueConditionComboViewer = null;
	/** RPAシナリオジョブ実行情報 */
	private JobRpaInfoResponse m_rpa = null;
	/** マネージャ名 */
	private String m_managerName = null;
	/** 読み取り専用モードのフラグ */
	private boolean m_enabled = false;
	/** RPAシナリオジョブ種別 */
	private Integer m_rpaJobType = null;

	public RpaDirectControlComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		initialize();
	}

	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());

		// ログイン制御（グループ）
		Group controlLoginGroup = new Group(this, SWT.NONE);
		controlLoginGroup.setText(Messages.getString("rpa.login.control"));
		controlLoginGroup.setLayout(new GridLayout(1, false));

		// シナリオ実行前後でOSへログイン・ログアウトするチェック
		this.m_loginButton = new Button(controlLoginGroup, SWT.CHECK);
		this.m_loginButton.setText(Messages.getString("rpa.login.before.logout.after"));
		this.m_loginButton.setLayoutData(new GridData(400, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_loginButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_loginUserIdText.setEditable(check.getSelection());
				m_loginPasswordText.setEditable(check.getSelection());
				m_resolutionComboViewer.getCombo().setEnabled(check.getSelection());
				m_loginFailureRetryText.setEditable(check.getSelection());
				m_loginFailureEndValueText.setEditable(check.getSelection());
				m_logoutOnErrorButton.setEnabled(check.getSelection());
				update();
			}
		});

		// シナリオ実行前後でOSへログイン・ログアウトするComposite
		Composite loginLogoutComposite = new Composite(controlLoginGroup, SWT.NONE);
		loginLogoutComposite.setLayout(new GridLayout(4, false));

		// ユーザID（ラベル）
		Label userIdLabel = new Label(loginLogoutComposite, SWT.LEFT);
		userIdLabel.setText(Messages.getString("user.id") + " : ");
		userIdLabel.setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// ユーザID（テキスト）
		this.m_loginUserIdText = new Text(loginLogoutComposite, SWT.BORDER);
		this.m_loginUserIdText.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_loginUserIdText.addModifyListener(e -> update());

		// パスワード（ラベル）
		Label passwordLabel = new Label(loginLogoutComposite, SWT.RIGHT);
		passwordLabel.setText(Messages.getString("password") + " : ");
		passwordLabel.setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// パスワード（テキスト）
		this.m_loginPasswordText = new Text(loginLogoutComposite, SWT.PASSWORD | SWT.BORDER);
		this.m_loginPasswordText.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_loginPasswordText.addModifyListener(e -> update());

		// 解像度（ラベル）
		Label resolutionLabel = new Label(loginLogoutComposite, SWT.LEFT);
		resolutionLabel.setText(Messages.getString("resolution") + " : ");
		resolutionLabel.setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// 解像度（コンボボックス）
		this.m_resolutionComboViewer = new ComboViewer(loginLogoutComposite, SWT.CENTER | SWT.READ_ONLY);
		this.m_resolutionComboViewer.getCombo()
				.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_resolutionComboViewer.setContentProvider(ArrayContentProvider.getInstance());

		// ログインできない場合(ラベル)
		Label loginFailureLabel = new Label(controlLoginGroup, SWT.LEFT);
		loginFailureLabel.setText(Messages.getString("rpa.login.failure") + " : ");
		loginFailureLabel.setLayoutData(new GridData(400, SizeConstant.SIZE_LABEL_HEIGHT));

		// ログインできない場合Composite
		Composite loginFailureComposite = new Composite(controlLoginGroup, SWT.NONE);
		loginFailureComposite.setLayout(new GridLayout(4, false));

		// リトライ回数（ラベル）
		Label retryLabel = new Label(loginFailureComposite, SWT.LEFT);
		retryLabel.setText(Messages.getString("retry.count") + " : ");
		retryLabel.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// リトライ回数（テキスト）
		this.m_loginFailureRetryText = new Text(loginFailureComposite, SWT.BORDER);
		this.m_loginFailureRetryText.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_loginFailureRetryText.addModifyListener(e -> update());
		this.m_loginFailureRetryText.addVerifyListener(new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));

		// 終了値（ラベル）
		Label endValueLabel = new Label(loginFailureComposite, SWT.RIGHT);
		endValueLabel.setText(Messages.getString("end.value") + " : ");
		endValueLabel.setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// 終了値（テキスト）
		this.m_loginFailureEndValueText = new Text(loginFailureComposite, SWT.BORDER);
		this.m_loginFailureEndValueText.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_loginFailureEndValueText.addModifyListener(e -> update());
		this.m_loginFailureEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));

		// 異常発生時もログアウトする
		this.m_logoutOnErrorButton = new Button(controlLoginGroup, SWT.CHECK);
		this.m_logoutOnErrorButton.setText(Messages.getString("rpa.logout.on.error"));

		// レイアウトのためのComposite
		Composite notifyComposite = new Composite(this, SWT.NONE);
		// ダイアログが縦に長くなるのを防ぐためグループを横に並べる
		notifyComposite.setLayout(new GridLayout(3, false));
		int notifyLayoutWidth = 80;

		// ログインされていない場合（グループ）
		Group notLoginGroup = new Group(notifyComposite, SWT.NONE);
		notLoginGroup.setText(Messages.getString("rpa.not.login"));
		notLoginGroup.setLayout(new GridLayout(2, false));

		// ログインされていない場合 通知チェックボックス
		this.m_notLoginNotifyButton = new Button(notLoginGroup, SWT.CHECK);
		this.m_notLoginNotifyButton.setText(Messages.getString("notify") + " : ");
		this.m_notLoginNotifyButton.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_notLoginNotifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_notLoginNotifyPriorityComboViewer.getCombo().setEnabled(check.getSelection());
			}
		});

		// ログインされていない場合 通知重要度コンボボックス
		this.m_notLoginNotifyPriorityComboViewer = new ComboViewer(notLoginGroup, SWT.CHECK);
		this.m_notLoginNotifyPriorityComboViewer.getCombo()
				.setLayoutData(new GridData(notifyLayoutWidth, SizeConstant.SIZE_BUTTON_HEIGHT));
		// プルダウン項目を設定
		m_notLoginNotifyPriorityComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		m_notLoginNotifyPriorityComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RpaNotLoginNotifyPriorityEnum) {
					RpaNotLoginNotifyPriorityEnum condition = (RpaNotLoginNotifyPriorityEnum) element;
					return PriorityMessage.enumToString(condition, RpaNotLoginNotifyPriorityEnum.class);
				}
				return super.getText(element);
			}
		});
		// NONEは除く
		m_notLoginNotifyPriorityComboViewer.setInput(Arrays.asList(RpaNotLoginNotifyPriorityEnum.CRITICAL,
				RpaNotLoginNotifyPriorityEnum.WARNING, RpaNotLoginNotifyPriorityEnum.INFO));

		// ログインされていない場合 終了（レイアウト用Composite）
		Composite notLoginEndComposite = new Composite(notLoginGroup, SWT.NONE);
		notLoginEndComposite.setLayout(new GridLayout(2, false));
		GridData notLoginEndGrid = new GridData();
		notLoginEndGrid.horizontalSpan = 2;
		notLoginEndComposite.setLayoutData(notLoginEndGrid);

		// ログインされていない場合 終了（ラべル）
		Label notLoginEndLabel = new Label(notLoginEndComposite, SWT.LEFT);
		notLoginEndLabel.setText(Messages.getString("end") + " : ");
		GridData notLoginEndLabelGridData = new GridData();
		notLoginEndLabelGridData.horizontalAlignment = GridData.CENTER;
		notLoginEndLabel.setLayoutData(notLoginEndLabelGridData);

		// ログインされていない場合 終了値Group
		Group notLoginEndValueGroup = new Group(notLoginEndComposite, SWT.NONE);
		notLoginEndValueGroup.setLayout(new RowLayout());

		// ログインされていない場合 終了値（ラべル）
		Label notLoginEndValueLabel = new Label(notLoginEndValueGroup, SWT.LEFT);
		notLoginEndValueLabel.setText(Messages.getString("end.value") + " : ");
		notLoginEndValueLabel.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// ログインされていない場合 終了値（テキスト）
		this.m_notLoginEndValueText = new Text(notLoginEndValueGroup, SWT.BORDER);
		this.m_notLoginEndValueText.setLayoutData(new RowData(notifyLayoutWidth, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_notLoginEndValueText.addModifyListener(e -> update());
		this.m_notLoginEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));

		// 起動前にRPAツールがすでに動作している場合（グループ）
		Group rpaRunningGroup = new Group(notifyComposite, SWT.NONE);
		rpaRunningGroup.setText(Messages.getString("rpa.already.running"));
		rpaRunningGroup.setLayout(new GridLayout(2, false));

		// 起動前にRPAツールがすでに動作している場合 通知チェックボックス
		this.m_rpaRunningNotifyButton = new Button(rpaRunningGroup, SWT.CHECK);
		this.m_rpaRunningNotifyButton.setText(Messages.getString("notify") + " : ");
		this.m_rpaRunningNotifyButton.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_rpaRunningNotifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_rpaRunningNotifyPriorityComboViewer.getCombo().setEnabled(check.getSelection());
			}
		});

		// 起動前にRPAツールがすでに動作している場合 通知重要度コンボボックス
		this.m_rpaRunningNotifyPriorityComboViewer = new ComboViewer(rpaRunningGroup, SWT.CHECK);
		this.m_rpaRunningNotifyPriorityComboViewer.getCombo()
				.setLayoutData(new GridData(notifyLayoutWidth, SizeConstant.SIZE_BUTTON_HEIGHT));
		// プルダウン項目を設定
		m_rpaRunningNotifyPriorityComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		m_rpaRunningNotifyPriorityComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RpaAlreadyRunningNotifyPriorityEnum) {
					RpaAlreadyRunningNotifyPriorityEnum condition = (RpaAlreadyRunningNotifyPriorityEnum) element;
					return PriorityMessage.enumToString(condition, RpaAlreadyRunningNotifyPriorityEnum.class);
				}
				return super.getText(element);
			}
		});
		// NONEは除く
		m_rpaRunningNotifyPriorityComboViewer.setInput(Arrays.asList(RpaAlreadyRunningNotifyPriorityEnum.CRITICAL,
				RpaAlreadyRunningNotifyPriorityEnum.WARNING, RpaAlreadyRunningNotifyPriorityEnum.INFO));

		// 起動前にRPAツールがすでに動作している場合 （レイアウト用Composite）
		Composite rpaRunningEndComposite = new Composite(rpaRunningGroup, SWT.NONE);
		rpaRunningEndComposite.setLayout(new GridLayout(2, false));
		GridData rpaRunningEndGrid = new GridData();
		rpaRunningEndGrid.horizontalSpan = 2;
		rpaRunningEndComposite.setLayoutData(rpaRunningEndGrid);

		// 起動前にRPAツールがすでに動作している場合 終了（ラべル）
		Label rpaRunningEndLabel = new Label(rpaRunningEndComposite, SWT.LEFT);
		rpaRunningEndLabel.setText(Messages.getString("end") + " : ");
		GridData rpaRunningEndLabelGridData = new GridData();
		rpaRunningEndLabelGridData.horizontalAlignment = GridData.CENTER;
		rpaRunningEndLabel.setLayoutData(rpaRunningEndLabelGridData);

		// 起動前にRPAツールがすでに動作している場合 終了値Group
		Group rpaRunningEndValueGroup = new Group(rpaRunningEndComposite, SWT.NONE);
		rpaRunningEndValueGroup.setLayout(new RowLayout());

		// 起動前にRPAツールがすでに動作している場合 終了値（ラべル）
		Label rpaRunningEndValueLabel = new Label(rpaRunningEndValueGroup, SWT.LEFT);
		rpaRunningEndValueLabel.setText(Messages.getString("end.value") + " : ");
		rpaRunningEndValueLabel.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// 起動前にRPAツールがすでに動作している場合 終了値（テキスト）
		this.m_rpaRunningEndValueText = new Text(rpaRunningEndValueGroup, SWT.BORDER);
		this.m_rpaRunningEndValueText.setLayoutData(new RowData(notifyLayoutWidth, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_rpaRunningEndValueText.addModifyListener(e -> update());
		this.m_rpaRunningEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));

		// RPAツールが異常終了した場合（グループ）
		Group rpaAbnormalExitGroup = new Group(notifyComposite, SWT.NONE);
		rpaAbnormalExitGroup.setText(Messages.getString("rpa.exit.abnormally"));
		rpaAbnormalExitGroup.setLayout(new GridLayout(2, false));

		// RPAツールが異常終了した場合 通知チェックボックス
		this.m_rpaAbnormalExitNotifyButton = new Button(rpaAbnormalExitGroup, SWT.CHECK);
		this.m_rpaAbnormalExitNotifyButton.setText(Messages.getString("notify") + " : ");
		this.m_rpaAbnormalExitNotifyButton.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_rpaAbnormalExitNotifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_rpaAbnormalExitNotifyPriorityComboViewer.getCombo().setEnabled(check.getSelection());
			}

		});

		// RPAツールが異常終了した場合 通知重要度コンボボックス
		this.m_rpaAbnormalExitNotifyPriorityComboViewer = new ComboViewer(rpaAbnormalExitGroup, SWT.CHECK);
		this.m_rpaAbnormalExitNotifyPriorityComboViewer.getCombo()
				.setLayoutData(new GridData(notifyLayoutWidth, SizeConstant.SIZE_BUTTON_HEIGHT));
		// プルダウン項目を設定
		m_rpaAbnormalExitNotifyPriorityComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		m_rpaAbnormalExitNotifyPriorityComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RpaAbnormalExitNotifyPriorityEnum) {
					RpaAbnormalExitNotifyPriorityEnum condition = (RpaAbnormalExitNotifyPriorityEnum) element;
					return PriorityMessage.enumToString(condition, RpaAbnormalExitNotifyPriorityEnum.class);
				}
				return super.getText(element);
			}
		});
		// NONEは除く
		m_rpaAbnormalExitNotifyPriorityComboViewer.setInput(Arrays.asList(RpaAbnormalExitNotifyPriorityEnum.CRITICAL,
				RpaAbnormalExitNotifyPriorityEnum.WARNING, RpaAbnormalExitNotifyPriorityEnum.INFO));

		// RPAツールが異常終了した場合 （レイアウト用Composite）
		Composite rpaAbnormalExitEndComposite = new Composite(rpaAbnormalExitGroup, SWT.NONE);
		rpaAbnormalExitEndComposite.setLayout(new GridLayout(2, false));
		GridData rpaAbnormalExitEndGrid = new GridData();
		rpaAbnormalExitEndGrid.horizontalSpan = 2;
		rpaAbnormalExitEndComposite.setLayoutData(rpaAbnormalExitEndGrid);

		// RPAツールが異常終了した場合 終了（ラべル）
		Label rpaAbnormalExitEndLabel = new Label(rpaAbnormalExitEndComposite, SWT.LEFT);
		rpaAbnormalExitEndLabel.setText(Messages.getString("end") + " : ");
		GridData rpaAbnormalExitEndLabelGridData = new GridData();
		rpaAbnormalExitEndLabelGridData.horizontalAlignment = GridData.CENTER;
		rpaAbnormalExitEndLabel.setLayoutData(rpaAbnormalExitEndLabelGridData);

		// RPAツールが異常終了した場合 終了値Group
		Group rpaAbnormalExitEndValueGroup = new Group(rpaAbnormalExitEndComposite, SWT.NONE);
		rpaAbnormalExitEndValueGroup.setLayout(new RowLayout());

		// RPAツールが異常終了した場合 終了値（ラべル）
		Label rpaAbnormalExitValueLabel = new Label(rpaAbnormalExitEndValueGroup, SWT.LEFT);
		rpaAbnormalExitValueLabel.setText(Messages.getString("end.value") + " : ");
		rpaAbnormalExitValueLabel.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_LABEL_HEIGHT));

		// RPAツールが異常終了した場合 終了値（テキスト）
		this.m_rpaAbnormalExitEndValueText = new Text(rpaAbnormalExitEndValueGroup, SWT.BORDER);
		this.m_rpaAbnormalExitEndValueText.setLayoutData(new RowData(notifyLayoutWidth, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_rpaAbnormalExitEndValueText.addModifyListener(e -> update());
		this.m_rpaAbnormalExitEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));

		// スクリーンショット（グループ）
		Group screenshotGroup = new Group(this, SWT.NONE);
		screenshotGroup.setText(Messages.getString("screenshot"));
		screenshotGroup.setLayout(new GridLayout(1, false));

		// 終了遅延発生時、スクリーンショットを取得するチェックボックス
		this.m_screenshotEndDelayButton = new Button(screenshotGroup, SWT.CHECK);
		this.m_screenshotEndDelayButton.setText(Messages.getString("rpa.screenshot.delay"));
		this.m_screenshotEndDelayButton.setLayoutData(new GridData(400, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 以下の終了値の場合、スクリーンショットを取得するチェックボックス
		this.m_screenshotEndValueButton = new Button(screenshotGroup, SWT.CHECK);
		this.m_screenshotEndValueButton.setText(Messages.getString("rpa.screenshot.exit.code"));
		this.m_screenshotEndValueButton.setLayoutData(new GridData(400, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_screenshotEndValueButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_screenshotEndValueText.setEditable(check.getSelection());
				m_screenshotEndValueConditionComboViewer.getCombo().setEnabled(check.getSelection());
				update();
			}
		});

		// スクリーンショットを取得する終了値Composite
		Composite screenshotEndValueComposite = new Composite(screenshotGroup, SWT.NONE);
		screenshotEndValueComposite.setLayout(new GridLayout(4, false));

		// スクリーンショットを取得する終了値（ラべル）
		Label screenshotEndValueLabel = new Label(screenshotEndValueComposite, SWT.RIGHT);
		screenshotEndValueLabel.setText(Messages.getString("end.value") + " : ");
		GridData gd_screenshotEndValueLabel = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd_screenshotEndValueLabel.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		screenshotEndValueLabel.setLayoutData(gd_screenshotEndValueLabel);

		// スクリーンショットを取得する終了値（テキスト）
		this.m_screenshotEndValueText = new Text(screenshotEndValueComposite, SWT.BORDER);
		this.m_screenshotEndValueText.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_screenshotEndValueText.addModifyListener(e -> update());

		// スクリーンショットを取得する判定条件（ラべル）
		Label screenshotEndValueConditionLabel = new Label(screenshotEndValueComposite, SWT.RIGHT);
		screenshotEndValueConditionLabel.setText(Messages.getString("judgment.condition") + " : ");
		GridData gd_screenshotEndValueConditionLabel = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd_screenshotEndValueConditionLabel.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		screenshotEndValueConditionLabel.setLayoutData(gd_screenshotEndValueConditionLabel);

		// スクリーンショットを取得する判定条件（コンボボックス）
		m_screenshotEndValueConditionComboViewer = new ComboViewer(screenshotEndValueComposite, SWT.BORDER);
		m_screenshotEndValueConditionComboViewer.getCombo()
				.setLayoutData(new GridData(50, SizeConstant.SIZE_TEXT_HEIGHT));
		// プルダウン項目を設定
		m_screenshotEndValueConditionComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		m_screenshotEndValueConditionComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RpaScreenshotEndValueConditionEnum) {
					RpaScreenshotEndValueConditionEnum condition = (RpaScreenshotEndValueConditionEnum) element;
					return JobRpaReturnCodeConditionMessage.typeToString(condition.getValue());
				}
				return super.getText(element);
			}
		});
		m_screenshotEndValueConditionComboViewer.setInput(RpaScreenshotEndValueConditionEnum.values());

		// ログイン解像度のプルダウンを生成
		List<JobRpaLoginResolutionResponse> resolutionList;
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(this.m_managerName);
			resolutionList = wrapper.getJobRpaLoginResolution();
			// プルダウン項目を設定
			m_resolutionComboViewer
					.setInput(resolutionList.stream().map(r -> r.getResolution()).collect(Collectors.toList()));
		} catch (Exception e) {
			m_log.warn("initialize() : " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		}
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update() {
		// 必須項目を明示
		if (m_loginButton.getSelection()) {
			if (m_enabled && "".equals(this.m_loginUserIdText.getText())) {
				this.m_loginUserIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_loginUserIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			if (m_enabled && "".equals(this.m_loginPasswordText.getText())) {
				this.m_loginPasswordText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_loginPasswordText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			if (m_enabled && "".equals(this.m_loginFailureRetryText.getText())) {
				this.m_loginFailureRetryText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_loginFailureRetryText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			if (m_enabled && "".equals(this.m_loginFailureEndValueText.getText())) {
				this.m_loginFailureEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_loginFailureEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			this.m_loginUserIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_loginPasswordText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_loginFailureRetryText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_loginFailureEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);

		}
		if (m_enabled && "".equals(this.m_notLoginEndValueText.getText())) {
			this.m_notLoginEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_notLoginEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_enabled && "".equals(this.m_rpaRunningEndValueText.getText())) {
			this.m_rpaRunningEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_rpaRunningEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_enabled && "".equals(this.m_rpaAbnormalExitEndValueText.getText())) {
			this.m_rpaAbnormalExitEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_rpaAbnormalExitEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_enabled && m_screenshotEndValueButton.getSelection()
				&& "".equals(this.m_screenshotEndValueText.getText())) {
			this.m_screenshotEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_screenshotEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	public void reflectRpaJobInfo() {
		if (m_rpa != null) {
			if (m_rpa.getRpaLoginFlg() != null) {
				m_loginButton.setSelection(m_rpa.getRpaLoginFlg());
			}
			if (m_rpa.getRpaLoginUserId() != null) {
				m_loginUserIdText.setText(m_rpa.getRpaLoginUserId());
			}
			if (m_rpa.getRpaLoginPassword() != null) {
				m_loginPasswordText.setText(m_rpa.getRpaLoginPassword());
			}
			if (m_rpa.getRpaLoginResolution() != null) {
				m_resolutionComboViewer.setSelection(new StructuredSelection(m_rpa.getRpaLoginResolution()));
			}
			if (m_rpa.getRpaLoginRetry() != null) {
				m_loginFailureRetryText.setText(String.valueOf(m_rpa.getRpaLoginRetry()));
			}
			if (m_rpa.getRpaLoginEndValue() != null) {
				m_loginFailureEndValueText.setText(String.valueOf(m_rpa.getRpaLoginEndValue()));
			}
			if (m_rpa.getRpaLogoutFlg() != null) {
				m_logoutOnErrorButton.setSelection(m_rpa.getRpaLogoutFlg());
			}
			if (m_rpa.getRpaNotLoginNotify() != null) {
				m_notLoginNotifyButton.setSelection(m_rpa.getRpaNotLoginNotify());
			}
			if (m_rpa.getRpaNotLoginNotifyPriority() != null) {
				m_notLoginNotifyPriorityComboViewer
						.setSelection(new StructuredSelection(m_rpa.getRpaNotLoginNotifyPriority()));
			}
			if (m_rpa.getRpaNotLoginEndValue() != null) {
				m_notLoginEndValueText.setText(String.valueOf(m_rpa.getRpaNotLoginEndValue()));
			}
			if (m_rpa.getRpaAlreadyRunningNotify() != null) {
				m_rpaRunningNotifyButton.setSelection(m_rpa.getRpaAlreadyRunningNotify());
			}
			if (m_rpa.getRpaAlreadyRunningNotifyPriority() != null) {
				m_rpaRunningNotifyPriorityComboViewer
						.setSelection(new StructuredSelection(m_rpa.getRpaAlreadyRunningNotifyPriority()));
			}
			if (m_rpa.getRpaAlreadyRunningEndValue() != null) {
				m_rpaRunningEndValueText.setText(String.valueOf(m_rpa.getRpaAlreadyRunningEndValue()));
			}
			if (m_rpa.getRpaAbnormalExitNotify() != null) {
				m_rpaAbnormalExitNotifyButton.setSelection(m_rpa.getRpaAbnormalExitNotify());
			}
			if (m_rpa.getRpaAbnormalExitNotifyPriority() != null) {
				m_rpaAbnormalExitNotifyPriorityComboViewer
						.setSelection(new StructuredSelection(m_rpa.getRpaAbnormalExitNotifyPriority()));
			}
			if (m_rpa.getRpaAbnormalExitEndValue() != null) {
				m_rpaAbnormalExitEndValueText.setText(String.valueOf(m_rpa.getRpaAbnormalExitEndValue()));
			}
			if (m_rpa.getRpaScreenshotEndDelayFlg() != null) {
				m_screenshotEndDelayButton.setSelection(m_rpa.getRpaScreenshotEndDelayFlg());
			}
			if (m_rpa.getRpaScreenshotEndValueFlg() != null) {
				m_screenshotEndValueButton.setSelection(m_rpa.getRpaScreenshotEndValueFlg());
			}
			if (m_rpa.getRpaScreenshotEndValue() != null) {
				m_screenshotEndValueText.setText(m_rpa.getRpaScreenshotEndValue());
			}
			if (m_rpa.getRpaScreenshotEndValueCondition() != null) {
				m_screenshotEndValueConditionComboViewer
						.setSelection(new StructuredSelection(m_rpa.getRpaScreenshotEndValueCondition()));
			}
			if (m_rpa.getRpaNotLoginNotify() != null) {
				m_notLoginNotifyButton.setSelection(m_rpa.getRpaNotLoginNotify());
			}
		} else {
			// 新規作成の場合はデフォルト値を表示
			m_loginButton.setSelection(true);
			m_loginUserIdText.setText("");
			m_loginPasswordText.setText("");
			m_resolutionComboViewer.setSelection(new StructuredSelection("1920x1080"));
			m_loginFailureRetryText.setText("10");
			m_loginFailureEndValueText.setText("-1");
			m_logoutOnErrorButton.setSelection(false);
			m_notLoginNotifyButton.setSelection(true);
			m_notLoginNotifyPriorityComboViewer
					.setSelection(new StructuredSelection(JobRpaInfoResponse.RpaNotLoginNotifyPriorityEnum.WARNING));
			m_notLoginEndValueText.setText("-1");
			m_rpaRunningNotifyButton.setSelection(true);
			m_rpaRunningNotifyPriorityComboViewer
					.setSelection(new StructuredSelection(JobRpaInfoResponse.RpaAlreadyRunningNotifyPriorityEnum.WARNING));
			m_rpaRunningEndValueText.setText("-1");
			m_rpaAbnormalExitNotifyButton.setSelection(true);
			m_rpaAbnormalExitNotifyPriorityComboViewer
					.setSelection(new StructuredSelection(JobRpaInfoResponse.RpaAbnormalExitNotifyPriorityEnum.WARNING));
			m_rpaAbnormalExitEndValueText.setText("-1");
			m_screenshotEndDelayButton.setSelection(true);
			m_screenshotEndValueButton.setSelection(true);
			m_screenshotEndValueText.setText("-1");
			m_screenshotEndValueConditionComboViewer.setSelection(
					new StructuredSelection(JobRpaInfoResponse.RpaScreenshotEndValueConditionEnum.EQUAL_NUMERIC));
		}
	}

	public ValidateResult validateRpaJobInfo() {
		if (m_rpaJobType == RpaJobTypeConstant.DIRECT) {
			if (m_loginButton.getSelection()) {
				// ログインを行う場合のみ必須項目のチェックを行う
				// ユーザID
				if (!JobDialogUtil.validateText(m_loginUserIdText)) {
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.job.rpa.6"));
				}
				// パスワード
				if (!JobDialogUtil.validateText(m_loginPasswordText)) {
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.job.rpa.7"));
				}
				// ログインできない場合のリトライ回数
				if (!JobDialogUtil.validateNumberText(m_loginFailureRetryText)) {
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.job.rpa.8"));
				}
				// ログインできない場合の終了値
				if (!JobDialogUtil.validateNumberText(m_loginFailureEndValueText)) {
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.job.rpa.9"));
				}
			}
			// ログインされていない場合の終了値
			if (!JobDialogUtil.validateNumberText(m_notLoginEndValueText)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.10"));
			}

			// RPAツールがすでに起動している場合の終了値
			if (!JobDialogUtil.validateNumberText(m_rpaRunningEndValueText)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.11"));
			}

			// RPAツールが異常終了した場合の終了値
			if (!JobDialogUtil.validateNumberText(m_rpaAbnormalExitEndValueText)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.12"));
			}

			if (m_screenshotEndValueButton.getSelection()) {
				// スクリーンショット取得を行う終了値
				// 終了値によるスクリーンショットの取得を行う場合のみ、必須項目のチェックを行う
				if (!JobDialogUtil.validateReturnCodeText(m_screenshotEndValueText)) {
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.job.rpa.13"));
				}
				// スクリーンショット取得を行う終了値 設定値の範囲チェック
				// 区切り文字で分割して分割後の値毎にチェック
				try{
					ReturnCodeConditionChecker.comfirmReturnCodeNumberRange(
							MessageConstant.END_VALUE.getMessage(), m_screenshotEndValueText.getText());
				}catch(InvalidSetting e){
					String[] args = { m_screenshotEndValueText.getText() };
					String errMsg = MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_END_VALUE_INVALID.getMessage(args) + "\n" + e.getMessage();
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),HinemosMessage.replace(errMsg ));
				}
			}
		}

		IStructuredSelection endValueConditionSelection = (StructuredSelection) m_screenshotEndValueConditionComboViewer
				.getSelection();
		RpaScreenshotEndValueConditionEnum selectedCondition = (RpaScreenshotEndValueConditionEnum) endValueConditionSelection
				.getFirstElement();
		// スクリーンショット取得を行う終了値と判定条件の組み合わせチェック
		if (!m_screenshotEndValueButton.getSelection()
				&& (m_screenshotEndValueText.getText() == null || m_screenshotEndValueText.getText().isEmpty())) {
			// 「以下の終了値の場合、スクリーンショットを取得する」がオフ、終了値がnullの場合は、何もしない
		} else if (selectedCondition == RpaScreenshotEndValueConditionEnum.EQUAL_NUMERIC
				|| selectedCondition == RpaScreenshotEndValueConditionEnum.NOT_EQUAL_NUMERIC) {
			// 判定条件が"="か"!="の場合は、複数指定、範囲指定の書式でチェックする。
			if (!m_screenshotEndValueText.getText().matches(ReturnCodeConditionChecker.MULTI_RANGE_CONDITION_REGEX)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.24"));
			}
		} else {
			// 既に書式チェック済みではあるが、それ以外の場合は、念のために数値書式であることを確認する。
			if (!m_screenshotEndValueText.getText().matches(ReturnCodeConditionChecker.NUMBER_REGEX)) {
				return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.job.rpa.24"));
			}
		}

		return null;
	}

	public void createRpaJobInfo() {
		// ログインを行うかどうか
		m_rpa.setRpaLoginFlg(m_loginButton.getSelection());
		// ユーザID
		m_rpa.setRpaLoginUserId(m_loginUserIdText.getText());
		// パスワード
		m_rpa.setRpaLoginPassword(m_loginPasswordText.getText());
		// ログイン解像度
		IStructuredSelection resolutionSelection = (StructuredSelection) m_resolutionComboViewer.getSelection();
		m_rpa.setRpaLoginResolution(String.valueOf(resolutionSelection.getFirstElement()));
		// ログインできない場合のリトライ回数
		if (!m_loginFailureRetryText.getText().isEmpty()) {
			m_rpa.setRpaLoginRetry(Integer.valueOf(m_loginFailureRetryText.getText()));
		}
		// ログインできない場合の終了値
		if (!m_loginFailureEndValueText.getText().isEmpty()) {
			m_rpa.setRpaLoginEndValue(Integer.valueOf(m_loginFailureEndValueText.getText()));
		}
		// 異常終了時もログアウトする
		m_rpa.setRpaLogoutFlg(m_logoutOnErrorButton.getSelection());

		// ログインされていない場合に通知する
		m_rpa.setRpaNotLoginNotify(m_notLoginNotifyButton.getSelection());
		// ログインされていない場合の通知重要度
		IStructuredSelection prioritySelection = (StructuredSelection) m_notLoginNotifyPriorityComboViewer
				.getSelection();
		m_rpa.setRpaNotLoginNotifyPriority((RpaNotLoginNotifyPriorityEnum) prioritySelection.getFirstElement());
		// ログインされていない場合の終了値
		if (!m_notLoginEndValueText.getText().isEmpty()) {
			m_rpa.setRpaNotLoginEndValue(Integer.valueOf(m_notLoginEndValueText.getText()));
		}

		// RPAツールがすでに起動している場合に通知する
		m_rpa.setRpaAlreadyRunningNotify(m_rpaRunningNotifyButton.getSelection());
		// RPAツールがすでに起動している場合の通知重要度
		prioritySelection = (StructuredSelection) m_rpaRunningNotifyPriorityComboViewer.getSelection();
		m_rpa.setRpaAlreadyRunningNotifyPriority(
				(RpaAlreadyRunningNotifyPriorityEnum) prioritySelection.getFirstElement());
		// RPAツールがすでに起動している場合の終了値
		if (!m_rpaRunningEndValueText.getText().isEmpty()) {
			m_rpa.setRpaAlreadyRunningEndValue(Integer.valueOf(m_rpaRunningEndValueText.getText()));
		}

		// RPAツールが異常終了した場合に通知する
		m_rpa.setRpaAbnormalExitNotify(m_rpaAbnormalExitNotifyButton.getSelection());
		// RPAツールが異常終了した場合の通知重要度
		prioritySelection = (StructuredSelection) m_rpaAbnormalExitNotifyPriorityComboViewer.getSelection();
		m_rpa.setRpaAbnormalExitNotifyPriority((RpaAbnormalExitNotifyPriorityEnum) prioritySelection.getFirstElement());
		// RPAツールが異常終了した場合の終了値
		if (!m_rpaAbnormalExitEndValueText.getText().isEmpty()) {
			m_rpa.setRpaAbnormalExitEndValue(Integer.valueOf(m_rpaAbnormalExitEndValueText.getText()));
		}

		// 終了遅延発生時にスクリーンショットを取得する
		m_rpa.setRpaScreenshotEndDelayFlg(m_screenshotEndDelayButton.getSelection());
		// 特定の終了値の場合にスクリーンショットを取得する
		m_rpa.setRpaScreenshotEndValueFlg(m_screenshotEndValueButton.getSelection());
		// スクリーンショットを取得する終了値
		m_rpa.setRpaScreenshotEndValue(m_screenshotEndValueText.getText());
		// スクリーンショットを取得する終了値判定条件
		IStructuredSelection endValueConditionSelection = (StructuredSelection) m_screenshotEndValueConditionComboViewer
				.getSelection();
		RpaScreenshotEndValueConditionEnum selectedCondition = (RpaScreenshotEndValueConditionEnum) endValueConditionSelection
				.getFirstElement();
		m_rpa.setRpaScreenshotEndValueCondition(selectedCondition);

	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_loginButton.setEnabled(enabled);
		this.m_loginUserIdText.setEditable(m_loginButton.getSelection() && enabled);
		this.m_loginPasswordText.setEditable(m_loginButton.getSelection() && enabled);
		this.m_resolutionComboViewer.getCombo().setEnabled(m_loginButton.getSelection() && enabled);
		this.m_loginFailureRetryText.setEditable(m_loginButton.getSelection() && enabled);
		this.m_loginFailureEndValueText.setEditable(m_loginButton.getSelection() && enabled);
		this.m_logoutOnErrorButton.setEnabled(m_loginButton.getSelection() && enabled);
		this.m_notLoginNotifyButton.setEnabled(enabled);
		this.m_notLoginNotifyPriorityComboViewer.getCombo()
				.setEnabled(m_notLoginNotifyButton.getSelection() && enabled);
		this.m_notLoginEndValueText.setEditable(enabled);
		this.m_rpaRunningNotifyButton.setEnabled(enabled);
		this.m_rpaRunningNotifyPriorityComboViewer.getCombo()
				.setEnabled(m_rpaRunningNotifyButton.getSelection() && enabled);
		this.m_rpaRunningEndValueText.setEditable(enabled);
		this.m_rpaAbnormalExitNotifyButton.setEnabled(enabled);
		this.m_rpaAbnormalExitNotifyPriorityComboViewer.getCombo()
				.setEnabled(m_rpaAbnormalExitNotifyButton.getSelection() && enabled);
		this.m_rpaAbnormalExitEndValueText.setEditable(enabled);
		this.m_screenshotEndDelayButton.setEnabled(enabled);
		this.m_screenshotEndValueButton.setEnabled(enabled);
		this.m_screenshotEndValueText.setEditable(m_screenshotEndValueButton.getSelection() && enabled);
		this.m_screenshotEndValueConditionComboViewer.getCombo()
				.setEnabled(m_screenshotEndValueButton.getSelection() && enabled);
		this.m_enabled = enabled;
		update(); // 読み込み専用時は必須項目を明示しない
	}

	/**
	 * @return the m_rpa
	 */
	public JobRpaInfoResponse getRpaJobInfo() {
		return m_rpa;
	}

	/**
	 * @param m_rpa
	 *            the m_rpa to set
	 */
	public void setRpaJobInfo(JobRpaInfoResponse rpa) {
		this.m_rpa = rpa;
	}

	/**
	 * RPAシナリオジョブ種別を設定します。<br>
	 * 必須項目のチェック有無を判断するために使用します。
	 * 
	 * @param rpaJobType
	 */
	public void setRpaJobType(Integer rpaJobType) {
		this.m_rpaJobType = rpaJobType;
	}

}
