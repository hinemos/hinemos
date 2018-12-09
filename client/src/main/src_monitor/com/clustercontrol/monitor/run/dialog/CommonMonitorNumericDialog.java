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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.bean.MonitorPredictionMethodConstant;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite;
import com.clustercontrol.monitor.run.composite.MonitorRuleComposite;
import com.clustercontrol.monitor.run.composite.NumericValueInfoComposite;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;

/**
 * 数値系監視設定共通ダイアログクラス<BR>
 *
 */
public class CommonMonitorNumericDialog extends CommonMonitorDialog {

	private Group groupCollect = null;			// 収集グループ

	/** 収集を有効にする */
	private Button confirmCollectValid = null;

	/** 収集値表示名 */
	protected Text itemName = null;

	/** 収集値単位 */
	protected Text measure = null;


	/** 数値監視判定情報 */
	protected NumericValueInfoComposite m_numericValueInfo = null;

	/** 閾値の上限・下限を示す文字列**/
	protected String item1 = null;
	protected String item2 = null;

	/** 判定基準を示す文字列 **/
	protected String criterion1 = Messages.getString("greater");
	protected String criterion2 = Messages.getString("less");

	/** 基本グループ */
	private Group m_groupBase = null;

	/** 将来予測グループ */
	private Group m_groupPrediction = null;
	/** 将来予測を有効にする */
	private Button m_confirmPredictionValid = null;
	/** 将来予測－予測方法 */
	private Combo m_comboPredictionMethod = null;
	/** 将来予測－対象収集期間 */
	private Text m_predictionAnalysysRange = null;
	/** 将来予測－予測先 */
	private Text m_predictionTarget = null;
	/** 将来予測－通知情報 */
	private NotifyInfoComposite m_predictionNotifyInfo = null;

	/** 変化点グループ */
	private Group m_groupChange = null;
	/** 変化点を有効にする */
	private Button m_confirmChangeValid = null;
	/** 変化点－対象収集期間 */
	private Text m_changeAnalysysRange = null;
	/** 変化点－通知情報 */
	private NotifyInfoComposite m_changeNotifyInfo = null;
	/** 変化点－数値監視判定情報 */
	private NumericValueInfoComposite m_changeNumericValueInfo = null;


	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 */
	public CommonMonitorNumericDialog(Shell parent, String managerName) {
		super(parent, managerName);
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
		// 変数として利用されるラベル
		Label label = null;

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

		// タブ設定
		TabFolder tabFolder = new TabFolder(groupMonitor, SWT.NONE);
		gridData = new GridData(); 
        gridData.horizontalSpan = BASIC_UNIT; 
        gridData.horizontalAlignment = GridData.FILL; 
        gridData.grabExcessHorizontalSpace = true; 
        tabFolder.setLayoutData(gridData);
        
        // 基本
        Composite baseComposite = new Composite(tabFolder, SWT.NONE);
        TabItem baseTabItem = new TabItem(tabFolder, SWT.NONE);
        baseTabItem.setText(Messages.getString("monitor.basic")); 
        baseTabItem.setControl(baseComposite);
        baseComposite.setLayoutData(new GridData());
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		baseComposite.setLayout(layout);

        // 将来予測
        Composite predictionComposite = new Composite(tabFolder, SWT.NONE);
        TabItem predictionTabItem = new TabItem(tabFolder, SWT.NONE);
        predictionTabItem.setText(Messages.getString("prediction.run")); 
        predictionTabItem.setControl(predictionComposite); 
        predictionComposite.setLayoutData(new GridData());
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		predictionComposite.setLayout(layout);

        // 変化点
        Composite changeComposite = new Composite(tabFolder, SWT.NONE);
        TabItem changeTabItem = new TabItem(tabFolder, SWT.NONE);
        changeTabItem.setText(Messages.getString("change.run")); 
        changeTabItem.setControl(changeComposite); 
        changeComposite.setLayoutData(new GridData());
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		changeComposite.setLayout(layout);

		tabFolder.setSelection(0);

		/*
		 * 基本
		 */
		m_groupBase = new Group(baseComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "base", m_groupBase);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		m_groupBase.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_groupBase.setLayoutData(gridData);

		// 監視（有効／無効）
		this.confirmMonitorValid = new Button(m_groupBase, SWT.CHECK);
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
		groupDetermine = new Group(m_groupBase, SWT.NONE);
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

		// 数値判定定義情報
		m_numericValueInfo = new NumericValueInfoComposite(groupDetermine,
				SWT.NONE,
				true,
				item1,
				item2,
				criterion1,
				criterion2,
				NumericValueInfoComposite.INPUT_VERIFICATION_REAL_NUMBER);
		WidgetTestUtil.setTestId(this, "numericvalue", m_numericValueInfo);

		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_numericValueInfo.setLayoutData(gridData);

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		groupNotifyAttribute = new Group(m_groupBase, SWT.NONE);
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

		/*
		 * 将来予測
		 */
		m_groupPrediction = new Group(predictionComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "prediction", m_groupPrediction);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		m_groupPrediction.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_groupPrediction.setLayoutData(gridData);

		// 将来予測（有効／無効）
		this.m_confirmPredictionValid = new Button(m_groupPrediction, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "confirmPredictionValid", m_confirmPredictionValid);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_confirmPredictionValid.setLayoutData(gridData);
		this.m_confirmPredictionValid.setText(Messages.getString("prediction.run"));
		this.m_confirmPredictionValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 通知部分、入力欄を有効/無効化
				if(m_confirmPredictionValid.getSelection()){
					setPredictionEnabled(true);
				}else{
					setPredictionEnabled(false);
				}
			}
		});

		// ラベル（予測方法）
		label = new Label(m_groupPrediction, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitor.prediction.method", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.prediction.method") + " : ");

		// コンボボックス（予測方法）
		this.m_comboPredictionMethod = new Combo(m_groupPrediction, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_comboPredictionMethod", this.m_comboPredictionMethod);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_SHORT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboPredictionMethod.setLayoutData(gridData);
		this.m_comboPredictionMethod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// 空白
		label = new Label(m_groupPrediction, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ラベル（将来予測－対象収集期間）
		label = new Label(m_groupPrediction, SWT.NONE);
		WidgetTestUtil.setTestId(this, "displayname", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("analysys.range") + " : ");

		// テキスト（将来予測－対象収集期間）
		this.m_predictionAnalysysRange = new Text(m_groupPrediction, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "predictionAnalysysRange", m_predictionAnalysysRange);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_predictionAnalysysRange.setLayoutData(gridData);
		this.m_predictionAnalysysRange.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(m_groupPrediction, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 空白
		label = new Label(m_groupPrediction, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ラベル（将来予測－予測先）
		label = new Label(m_groupPrediction, SWT.NONE);
		WidgetTestUtil.setTestId(this, "displayname", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("prediction.target") + " : ");

		// テキスト（将来予測－予測対象時間）
		this.m_predictionTarget = new Text(m_groupPrediction, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "predictionTarget", m_predictionTarget);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_predictionTarget.setLayoutData(gridData);
		this.m_predictionTarget.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		Group groupPredictionNotifyAttribute = new Group(m_groupPrediction, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyattribute", groupPredictionNotifyAttribute);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = MIN_UNIT;
		groupPredictionNotifyAttribute.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupPredictionNotifyAttribute.setLayoutData(gridData);
		groupPredictionNotifyAttribute.setText(Messages.getString("notify.attribute"));
		this.m_predictionNotifyInfo = new NotifyInfoComposite(groupPredictionNotifyAttribute, 
				SWT.NONE, 100, MonitorNumericType.TYPE_PREDICTION.getType());
		this.m_predictionNotifyInfo.setManagerName(getManagerName());
		WidgetTestUtil.setTestId(this, "notifyinfo", m_predictionNotifyInfo);
		gridData = new GridData();
		gridData.horizontalSpan = MIN_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_predictionNotifyInfo.setLayoutData(gridData);

		/*
		 * 変化点
		 */
		m_groupChange = new Group(changeComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "change", m_groupChange);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		m_groupChange.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_groupChange.setLayoutData(gridData);

		// 変化点（有効／無効）
		this.m_confirmChangeValid = new Button(m_groupChange, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_confirmChangeValid", m_confirmChangeValid);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_confirmChangeValid.setLayoutData(gridData);
		this.m_confirmChangeValid.setText(Messages.getString("change.run"));
		this.m_confirmChangeValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 通知部分、入力欄を有効/無効化
				if(m_confirmChangeValid.getSelection()){
					setChangeEnabled(true);
				}else{
					setChangeEnabled(false);
				}
			}
		});

		// ラベル（変化点－収集値の範囲）
		label = new Label(m_groupChange, SWT.NONE);
		WidgetTestUtil.setTestId(this, "displayname", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_MIDDLE + WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("analysys.range") + " : ");

		// テキスト（変化点－収集値の範囲）
		this.m_changeAnalysysRange = new Text(m_groupChange, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "changeAnalysysRange", m_changeAnalysysRange);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_changeAnalysysRange.setLayoutData(gridData);
		this.m_changeAnalysysRange.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(m_groupChange, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - WIDTH_TITLE * 2 - WIDTH_TITLE_MIDDLE - WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// スペース
		Composite spaceComposite = new Composite(m_groupChange, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		spaceComposite.setLayoutData(gridData);

		/*
		 * 判定グループ（監視グループの子グループ）
		 * なお、判定内容は継承先のクラスにて実装する。
		 */
		Group groupChangeDetermine = new Group(m_groupChange, SWT.NONE);
		WidgetTestUtil.setTestId(this, "determine", groupChangeDetermine);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = MIN_UNIT;
		groupChangeDetermine.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupChangeDetermine.setLayoutData(gridData);
		groupChangeDetermine.setText(Messages.getString("determine"));

		// 変化点－数値判定定義情報
		m_changeNumericValueInfo = new NumericValueInfoComposite(groupChangeDetermine,
				SWT.NONE,
				true,
				Messages.getString("response.standard_deviation"),
				Messages.getString("response.standard_deviation"),
				Messages.getString("greater"),
				Messages.getString("less"),
				NumericValueInfoComposite.INPUT_VERIFICATION_REAL_NUMBER,
				MonitorNumericType.TYPE_CHANGE.getType());
		m_changeNumericValueInfo.setInfoWarnText("-1", "1", "-2", "2");
		WidgetTestUtil.setTestId(this, "numericvalue", m_changeNumericValueInfo);

		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_changeNumericValueInfo.setLayoutData(gridData);

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		Group groupChangeNotifyAttribute = new Group(m_groupChange, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyattribute", groupChangeNotifyAttribute);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = MIN_UNIT;
		groupChangeNotifyAttribute.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupChangeNotifyAttribute.setLayoutData(gridData);
		groupChangeNotifyAttribute.setText(Messages.getString("notify.attribute"));
		this.m_changeNotifyInfo = new NotifyInfoComposite(groupChangeNotifyAttribute, SWT.NONE,
				60, MonitorNumericType.TYPE_CHANGE.getType());
		this.m_changeNotifyInfo.setManagerName(getManagerName());
		WidgetTestUtil.setTestId(this, "notifyinfo", m_changeNotifyInfo);
		gridData = new GridData();
		gridData.horizontalSpan = MIN_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_changeNotifyInfo.setLayoutData(gridData);

		/*
		 * 収集グループ
		 */
		groupCollect = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collect", groupCollect);
		layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupCollect.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCollect.setLayoutData(gridData);
		groupCollect.setText(Messages.getString("collection.run"));

		// 収集（有効／無効）
		this.confirmCollectValid = new Button(groupCollect, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "confirmcollectvalid", confirmCollectValid);
		gridData = new GridData();
		gridData.horizontalSpan = SMALL_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.confirmCollectValid.setLayoutData(gridData);
		this.confirmCollectValid.setText(Messages.getString("collection.run"));
		this.confirmCollectValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 収集エリアを有効/無効化
				if(confirmCollectValid.getSelection()){
					setCollectorEnabled(true);
				}else{
					setCollectorEnabled(false);
				}
			}
		});

		// ラベル（収集値表示名）
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "displayname", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_SHORT + SMALL_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("collection.display.name") + " : ");

		// テキスト（収集値表示名）
		this.itemName = new Text(groupCollect, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "itemName", itemName);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - SMALL_UNIT * 2 - WIDTH_TITLE_SHORT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.itemName.setLayoutData(gridData);
		this.itemName.setText(Messages.getString("select.value"));
		this.itemName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = SMALL_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// ラベル（収集値単位）
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collectionunit", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_SHORT + SMALL_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("collection.unit") + " : ");
		// テキスト（収集値単位）
		this.measure = new Text(groupCollect, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "measure", measure);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - SMALL_UNIT * 2 - WIDTH_TITLE_SHORT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.measure.setLayoutData(gridData);
		this.measure.setText(Messages.getString("collection.unit"));
		this.measure.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	/**
	 * 収集エリアを有効/無効化します。
	 *
	 */
	private void setCollectorEnabled(boolean enabled){
		itemName.setEnabled(enabled);
		measure.setEnabled(enabled);

		update();
	}

	/**
	 * 将来予測エリアを有効/無効化します。
	 *
	 */
	private void setPredictionEnabled(boolean enabled){
		m_comboPredictionMethod.setEnabled(enabled);
		m_predictionAnalysysRange.setEnabled(enabled);
		m_predictionTarget.setEnabled(enabled);
		m_predictionNotifyInfo.setEnabled(enabled);
		update();
	}

	/**
	 * 変化点エリアを有効/無効化します。
	 *
	 */
	private void setChangeEnabled(boolean enabled){
		m_changeAnalysysRange.setEnabled(enabled);
		m_changeNotifyInfo.setEnabled(enabled);
		update();
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	protected void update(){
		super.update();

		// 将来予測－予測方法
		if(this.m_comboPredictionMethod.getEnabled() && "".equals(this.m_comboPredictionMethod.getText())){
			this.m_comboPredictionMethod.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboPredictionMethod.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 変化点－対象収集期間
		if(this.m_changeAnalysysRange.getEnabled() && "".equals(this.m_changeAnalysysRange.getText())){
			this.m_changeAnalysysRange.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_changeAnalysysRange.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 将来予測－対象収集期間
		if(this.m_predictionAnalysysRange.getEnabled() && "".equals(this.m_predictionAnalysysRange.getText())){
			this.m_predictionAnalysysRange.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_predictionAnalysysRange.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 将来予測－予測先
		if(this.m_predictionTarget.getEnabled() && "".equals(this.m_predictionTarget.getText())){
			this.m_predictionTarget.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_predictionTarget.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 収集値項目名
		if(this.itemName.getEnabled() && "".equals(this.itemName.getText())){
			this.itemName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.itemName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 収集値単位
		if(this.measure.getEnabled() && "".equals(this.measure.getText())){
			this.measure.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.measure.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor 設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {
		super.setInputData(monitor);

		// 収集
		if (monitor.isCollectorFlg()) {
			this.confirmCollectValid.setSelection(true);
		}else{
			this.setCollectorEnabled(false);
		}

		// 収集値表示名
		if (monitor.getItemName() != null){
			this.itemName.setText(monitor.getItemName());
		}

		// 収集値単位
		if (monitor.getMeasure() != null){
			this.measure.setText(monitor.getMeasure());
		}
		
		// 閾値情報
		if (this.m_MonitorNumericValueInfo != null) {
			List<MonitorNumericValueInfo> monitorNumericValueInfo = monitor.getNumericValueInfo();
			for (MonitorNumericValueInfo valueInfo : monitorNumericValueInfo) {
				if (!MonitorNumericType.TYPE_BASIC.getType().equals(valueInfo.getMonitorNumericType())) {
					continue;
				}
				for (MonitorNumericValueInfo mValueInfo : m_MonitorNumericValueInfo) {
					if (valueInfo.getPriority().equals(mValueInfo.getPriority())) {
						if (monitor.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PING)) {
							valueInfo.setThresholdLowerLimit(mValueInfo.getThresholdUpperLimit());
						} else {
							valueInfo.setThresholdLowerLimit(mValueInfo.getThresholdLowerLimit());
							valueInfo.setThresholdUpperLimit(mValueInfo.getThresholdUpperLimit());
						}
					}
				}
			}
		}

		// 将来予測
		if (monitor.isPredictionFlg()) {
			this.m_confirmPredictionValid.setSelection(true);
		}else{
			this.setPredictionEnabled(false);
		}

		// 将来予測－通知情報
		if(monitor.getPredictionNotifyRelationList() != null
				&& monitor.getPredictionNotifyRelationList().size() > 0){
			this.m_predictionNotifyInfo.setNotify(monitor.getPredictionNotifyRelationList());
		}

		// 将来予測－アプリケーション
		if(monitor.getPredictionApplication() != null){
			this.m_predictionNotifyInfo.setApplication(monitor.getPredictionApplication());
		}

		// 将来予測－予測方法
		for (String type : MonitorPredictionMethodConstant.types()) {
			this.m_comboPredictionMethod.add(HinemosMessage.replace(MonitorPredictionMethodConstant.typeToMessage(type)));
			this.m_comboPredictionMethod.setData(HinemosMessage.replace(MonitorPredictionMethodConstant.typeToMessage(type)), type);
		}
		String predictionMethod = monitor.getPredictionMethod();
		if(predictionMethod == null || predictionMethod.isEmpty()){
			predictionMethod = MonitorPredictionMethodConstant.DEFALUT;
		}
		this.m_comboPredictionMethod.setText(HinemosMessage.replace(MonitorPredictionMethodConstant.typeToMessage(predictionMethod)));

		// 将来予測－対象収集期間
		if (monitor.getPredictionAnalysysRange() != null){
			this.m_predictionAnalysysRange.setText(monitor.getPredictionAnalysysRange().toString());
		}

		// 将来予測－予測先
		if (monitor.getPredictionTarget() != null){
			this.m_predictionTarget.setText(monitor.getPredictionTarget().toString());
		}

		// 変化点
		if (monitor.isChangeFlg()) {
			this.m_confirmChangeValid.setSelection(true);
		}else{
			this.setChangeEnabled(false);
		}

		//　変化点－通知情報
		if(monitor.getChangeNotifyRelationList() != null
				&& monitor.getChangeNotifyRelationList().size() > 0){
			this.m_changeNotifyInfo.setNotify(monitor.getChangeNotifyRelationList());
		}

		//　変化点－アプリケーション
		if(monitor.getChangeApplication() != null){
			this.m_changeNotifyInfo.setApplication(monitor.getChangeApplication());
		}

		// 変化点－対象収集期間
		if (monitor.getChangeAnalysysRange() != null){
			this.m_changeAnalysysRange.setText(monitor.getChangeAnalysysRange().toString());
		}

		// 変化点－閾値情報
		m_changeNumericValueInfo.setInputData(monitor);
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfo createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// 収集
		if (!this.confirmCollectValid.getSelection()) {
			if (this.m_confirmPredictionValid.getSelection()) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.monitor.90"));
				return null;
			} else if (this.m_confirmChangeValid.getSelection()) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.monitor.91"));
				return null;
			}
		}

		// 収集
		monitorInfo.setCollectorFlg(this.confirmCollectValid.getSelection());

		if(this.itemName.getText() != null){
			monitorInfo.setItemName(this.itemName.getText());
		}

		if(this.measure.getText() != null){
			monitorInfo.setMeasure(this.measure.getText());
		}

		// 将来予測
		monitorInfo.setPredictionFlg(this.m_confirmPredictionValid.getSelection());

		// 将来予測－予測方法
		monitorInfo.setPredictionMethod((String)this.m_comboPredictionMethod.getData(this.m_comboPredictionMethod.getText()));

		// 将来予測－対象収集期間
		if(this.m_predictionAnalysysRange.getText() != null && this.m_predictionAnalysysRange.getText().length() > 0){
			try {
				monitorInfo.setPredictionAnalysysRange(Integer.parseInt(this.m_predictionAnalysysRange.getText()));
			}catch (NumberFormatException e) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.monitor.86"));
				return null;
			}
		}else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.monitor.87"));
			return null;
		}
		// 将来予測－予測先
		if(this.m_predictionTarget.getText() != null && this.m_predictionTarget.getText().length() > 0){
			try {
				monitorInfo.setPredictionTarget(Integer.parseInt(this.m_predictionTarget.getText()));
			}catch (NumberFormatException e) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.monitor.88"));
				return null;
			}
		}else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.monitor.89"));
			return null;
		}

		// 将来予測－通知設定の格納
		validateResult = m_predictionNotifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if (validateResult.getID() == null) {
				if (! displayQuestion(validateResult)) {	// 通知IDが選択されていない場合
					validateResult = null;
					return null;
				}
			} else {
				return null;	// アプリケーションが未入力の場合
			}
		}

		// 変化点
		monitorInfo.setChangeFlg(this.m_confirmChangeValid.getSelection());

		// 変化点－対象収集期間
		if(this.m_changeAnalysysRange.getText() != null && this.m_changeAnalysysRange.getText().length() > 0){
			try {
				monitorInfo.setChangeAnalysysRange(Integer.parseInt(this.m_changeAnalysysRange.getText()));
			}catch (NumberFormatException e) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.monitor.92"));
				return null;
			}
		}else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.monitor.93"));
			return null;
		}

		// 変化点－閾値判定の格納
		validateResult = m_changeNumericValueInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			return null;
		}

		// 変化点－通知設定の格納
		validateResult = m_changeNotifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if (validateResult.getID() == null) {
				if (! displayQuestion(validateResult)) {	// 通知IDが選択されていない場合
					validateResult = null;
					return null;
				}
			} else {
				return null;	// アプリケーションが未入力の場合
			}
		}

		// 監視種別を数値に設定する
		monitorInfo.setMonitorType(MonitorTypeConstant.TYPE_NUMERIC);

		return monitorInfo;
	}

	/**
	 * オーナーロールを設定する
	 * @return
	 */
	@Override
	public void updateOwnerRole(String ownerRoleId) {
		getMonitorRule().getCalendarId().createCalIdCombo(m_monitorBasic.getManagerListComposite().getText(), ownerRoleId);
		getNotifyInfo().setOwnerRoleId(ownerRoleId, true);
		m_predictionNotifyInfo.setOwnerRoleId(ownerRoleId, true);
		m_changeNotifyInfo.setOwnerRoleId(ownerRoleId, true);
		getMonitorBasicScope().setOwnerRoleId(ownerRoleId);
		getNotifyInfo().setManagerName(m_monitorBasic.getManagerListComposite().getText());
		m_predictionNotifyInfo.setManagerName(m_monitorBasic.getManagerListComposite().getText());
		m_changeNotifyInfo.setManagerName(m_monitorBasic.getManagerListComposite().getText());
	}
}
