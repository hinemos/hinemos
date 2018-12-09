/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite;
import com.clustercontrol.monitor.run.composite.MonitorRuleComposite;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;

/**
 * 監視設定共通ダイアログクラス
 *
 */
public class CommonMonitorDialog extends CommonDialog {

	public static final int WIDTH_TITLE_SHORT = 2;
	public static final int WIDTH_TITLE = 4;
	public static final int WIDTH_TITLE_MIDDLE = 6;
	public static final int WIDTH_TITLE_LONG = 8;
	public static final int WIDTH_VALUE = 2;
	public static final int WIDTH_VALUE_LONG = 4;
	public static final int WIDTH_TEXT_SHORT = 4;
	public static final int WIDTH_TEXT = 8;
	public static final int WIDTH_TEXT_LONG = 16;
	public static final int BASIC_UNIT = 30;
	public static final int LONG_UNIT = 20;
	public static final int HALF_UNIT = 15;
	public static final int SHORT_UNIT = 10;
	public static final int SMALL_UNIT = 5;
	public static final int MIN_UNIT = 1;
	public static final int BASIC_MARGIN = 5;
	public static final int HALF_MARGIN = 1;

	// 初期値
	public static final int RUNCOUNT_COUNT = 1;
	public static final int TIMEOUT_SEC = 5000;

	// ----- instance フィールド ----- //

	/** 入力値を保持するオブジェクト */
	protected MonitorInfo inputData = null;

	/** 入力値の正当性を保持するオブジェクト */
	protected ValidateResult validateResult = null;

	/** 変更対象の監視項目ID */
	protected String monitorId = null;

	/** 変更するかどうかのフラグ（true：変更する） modify、copy時に使用　 */
	protected boolean updateFlg = false;

	/** 監視基本情報 */
	protected MonitorBasicScopeComposite m_monitorBasic = null;

	/** 監視条件 共通部分 */
	protected MonitorRuleComposite m_monitorRule = null;

	/** 通知情報 */
	protected NotifyInfoComposite m_notifyInfo = null;

	/** 監視を有効にする */
	protected Button confirmMonitorValid = null;

	/** 入力値から生成する監視情報 **/
	protected MonitorInfo monitorInfo = null;

	/** 未登録ノード スコープを表示するかフラグ*/
	protected boolean m_unregistered = false;

	/** マネージャ名 */
	protected String managerName = null;
	
	/** グラフから閾値を変更した場合の情報 */
	protected List<MonitorNumericValueInfo> m_MonitorNumericValueInfo = null;

	// ----- 共通メンバ変数 ----- //
	protected Shell shell = null;
	protected Group groupRule = null;				// 条件グループ
	protected Group groupMonitor = null;			// 監視グループ
	protected Group groupDetermine = null;			// 判定グループ
	protected Group groupNotifyAttribute = null;	// 通知グループ

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 */
	public CommonMonitorDialog(Shell parent, String managerName) {
		super(parent);
		this.managerName = managerName;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = BASIC_MARGIN;
		layout.marginHeight = BASIC_MARGIN;
		layout.numColumns = BASIC_UNIT;
		parent.setLayout(layout);

		// 監視基本情報
		m_monitorBasic = new MonitorBasicScopeComposite(parent, SWT.NONE, m_unregistered, this);
		WidgetTestUtil.setTestId(this, null, m_monitorBasic);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_monitorBasic.setLayoutData(gridData);
		if(this.managerName != null) {
			m_monitorBasic.getManagerListComposite().setText(this.managerName);
		}

		/*
		 * 条件グループ
		 */
		groupRule = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "rule", groupRule);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupRule.setLayoutData(gridData);
		groupRule.setText(Messages.getString("monitor.rule"));

		m_monitorRule = new MonitorRuleComposite(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "rule", m_monitorRule);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_monitorRule.setLayoutData(gridData);

		/*
		 * 監視グループ
		 */
		groupMonitor = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, groupMonitor);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupMonitor.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupMonitor.setLayoutData(gridData);
		groupMonitor.setText(Messages.getString("monitor.run"));

		// 監視（有効／無効）
		this.confirmMonitorValid = new Button(groupMonitor, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "confirmvalidcheck", confirmMonitorValid);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.confirmMonitorValid.setLayoutData(gridData);
		this.confirmMonitorValid.setText(Messages.getString("monitor.run"));
		this.confirmMonitorValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 判定、通知部分を有効/無効化
				if(confirmMonitorValid.getSelection()){
					setMonitorEnabled(true);
				}else{
					setMonitorEnabled(false);
				}
			}
		});

		/*
		 * 判定グループ（監視グループの子グループ）
		 * なお、判定内容は継承先のクラスにて実装する。
		 */
		groupDetermine = new Group(groupMonitor, SWT.NONE);
		WidgetTestUtil.setTestId(this, "determine", groupDetermine);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = MIN_UNIT;
		groupDetermine.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupDetermine.setLayoutData(gridData);
		groupDetermine.setText(Messages.getString("determine"));

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		groupNotifyAttribute = new Group(groupMonitor, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyattribute", groupNotifyAttribute);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = MIN_UNIT;
		groupNotifyAttribute.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));
		this.m_notifyInfo = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE);
		this.m_notifyInfo.setManagerName(getManagerName());
		WidgetTestUtil.setTestId(this, "notifyinfo", m_notifyInfo);
		gridData = new GridData();
		gridData.horizontalSpan = MIN_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_notifyInfo.setLayoutData(gridData);
	}

	/**
	 * 監視エリアを有効/無効化します。
	 *
	 */
	protected void setMonitorEnabled(boolean enabled){
		m_notifyInfo.setEnabled(enabled);
	}


	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	protected void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(700, shell.getSize().y));

		// 画面中央に配置
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}


	/**
	 * 入力内容を返します。
	 *
	 * @return 入力内容を保持した通知情報
	 */
	public MonitorInfo getInputData() {
		return this.inputData;
	}

	public boolean getUpdateFlg(){
		return this.updateFlg;
	}

	public MonitorRuleComposite getMonitorRule(){
		return this.m_monitorRule;
	}

	public MonitorBasicScopeComposite getMonitorBasicScope(){
		return this.m_monitorBasic;
	}

	public NotifyInfoComposite getNotifyInfo(){
		return this.m_notifyInfo;
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor 設定値として用いる監視情報
	 */
	protected void setInputData(MonitorInfo monitor) {

		// 監視基本情報
		m_monitorBasic.setInputData(monitor, this.updateFlg);

		// 監視条件
		m_monitorRule.setInputData(monitor);

		//通知情報の設定
		if(monitor.getNotifyRelationList() != null
				&& monitor.getNotifyRelationList().size() > 0){
			this.m_notifyInfo.setNotify(monitor.getNotifyRelationList());
		}

		if (monitor.getApplication() != null) {
			this.m_notifyInfo.setApplication(monitor.getApplication());
			this.m_notifyInfo.update();
		}

		// 監視
		if (monitor.isMonitorFlg()) {
			this.confirmMonitorValid.setSelection(true);
		}else{
			this.setMonitorEnabled(false);
		}


	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	protected MonitorInfo createInputData() {
		monitorInfo = new MonitorInfo();
		setInfoInitialValue(monitorInfo);

		// 監視基本情報
		validateResult = m_monitorBasic.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 監視条件 共通部分
		validateResult = m_monitorRule.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 監視
		monitorInfo.setMonitorFlg(this.confirmMonitorValid.getSelection());

		return monitorInfo;
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
		// 入力値生成
		this.inputData = this.createInputData();

		if (this.inputData != null) {
			return super.validate();
		} else {
			return validateResult;
		}
	}


	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 無効な入力値の情報を設定します
	 *
	 */
	protected void setValidateResult(String id, String message){
		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);
	}

	/**
	 * MonitorInfoに初期値を設定します
	 *
	 */
	protected void setInfoInitialValue(MonitorInfo monitor) {

		// 監視判定タイプ(真偽値/数値/文字列)
		monitor.setMonitorType(MonitorTypeConstant.TYPE_TRUTH);
		// 実行間隔（秒）
		monitor.setRunInterval(RunInterval.TYPE_MIN_05.toSec());
		// 値失敗時の重要度
		monitor.setFailurePriority(PriorityConstant.TYPE_UNKNOWN);
		// 監視有効フラグ
		monitor.setMonitorFlg(true);
		// 収集有効フラグ
		monitor.setCollectorFlg(false);
		// 将来予測有効フラグ
		monitor.setPredictionFlg(false);
		// 将来予測－収集値の範囲
		monitor.setPredictionAnalysysRange(60);
		// 将来予測－予測対象時間
		monitor.setPredictionTarget(60);
		// 変化点有効フラグ
		monitor.setChangeFlg(false);
		// 変化点－収集値の範囲
		monitor.setChangeAnalysysRange(60);
	}

	/**
	 * 更新処理
	 */
	protected void update(){
	}

	/**
	 * 入力されたマネージャ名を返します。
	 * @return
	 */
	public String getManagerName() {
		return this.getMonitorBasicScope().getManagerListComposite().getText();
	}
	
	public void setGraphMonitorNumericValueInfo(List<MonitorNumericValueInfo> monitorNumericValueInfoList) {
		this.m_MonitorNumericValueInfo = monitorNumericValueInfoList;
	}

	/**
	 * オーナーロールを設定する
	 * @return
	 */
	public void updateOwnerRole(String ownerRoleId) {
		getMonitorRule().getCalendarId().createCalIdCombo(m_monitorBasic.getManagerListComposite().getText(), ownerRoleId);
		getNotifyInfo().setOwnerRoleId(ownerRoleId, true);
		getMonitorBasicScope().setOwnerRoleId(ownerRoleId);
		getNotifyInfo().setManagerName(m_monitorBasic.getManagerListComposite().getText());
	}
}
