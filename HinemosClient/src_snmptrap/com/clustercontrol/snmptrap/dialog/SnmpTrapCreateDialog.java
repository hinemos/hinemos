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

package com.clustercontrol.snmptrap.dialog;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite;
import com.clustercontrol.monitor.run.composite.MonitorRuleComposite;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.snmptrap.composite.TrapDefineCompositeDefine;
import com.clustercontrol.snmptrap.composite.TrapDefineListComposite;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.hub.LogFormat;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.TrapCheckInfo;
import com.clustercontrol.ws.monitor.TrapValueInfo;

/**
 * SNMPTRAP監視作成・変更ダイアログクラス<BR>
 *
 * @version 6.0.0
 * @since 2.1.0
 */
public class SnmpTrapCreateDialog extends CommonMonitorDialog {

	public static final int MAX_COLUMN = 20;
	public static final int MAX_COLUMN_SMALL = 15;
	public static final int WIDTH_TITLE = 6;
	public static final int WIDTH_TITLE_WIDE = 8;
	public static final int WIDTH_TITLE_SMALL = 4;
	public static final int WIDTH_VALUE = 2;



	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 750;
	private static final int sizeY = 760;

	// ----- instance フィールド ----- //

	/** コミュニティチェックボタン **/
	private Button buttonCommunityCheckOn = null;

	/** コミュニティ名 */
	private Text textCommunityName = null;

	/** 文字コード変換ボタン **/
	private Button buttonCharsetConvertOn = null;

	/** 変換文字コード名 */
	private Text textCharsetName = null;

	/** OIDテーブル */
	private TrapDefineListComposite tableDefineListComposite = null;

	/** 未指定のトラップ受信時に通知する */
	//チェックボックス
	private Button buttonNotifyNonSpecifiedTrap = null;
	//重要度
	private Combo comboPriority = null;
	
	/** 収集グループ */
	private Group groupCollect = null;

	/** 収集を有効にする */
	private Button confirmCollectValid = null;

	/** ログフォーマット */
	protected Combo logFormat = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param monitorType 監視判定タイプ
	 */
	public SnmpTrapCreateDialog(Shell parent) {
		super(parent, null);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public SnmpTrapCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);

		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.snmptrap.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = MAX_COLUMN;
		parent.setLayout(layout);

		// 監視基本情報
		//SNMPトラップでは未登録ノードからのトラップを受け付けるようにするので、
		//第３引数をtrueとする。
		m_monitorBasic = new MonitorBasicScopeComposite(parent, SWT.NONE ,true, this);
		gridData = new GridData();
		gridData.horizontalSpan =MAX_COLUMN;
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
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = MAX_COLUMN;
		groupRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupRule.setLayoutData(gridData);
		groupRule.setText(Messages.getString("monitor.rule"));

		m_monitorRule = new MonitorRuleComposite(groupRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_monitorRule.setLayoutData(gridData);

		// 監視間隔の設定を利用不可とする
		this.m_monitorRule.setRunIntervalEnabled(false);

		/*
		 * 監視グループ
		 */
		groupMonitor = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = MAX_COLUMN;
		groupMonitor.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupMonitor.setLayoutData(gridData);
		groupMonitor.setText(Messages.getString("monitor.run"));

		// 監視（有効／無効）
		this.confirmMonitorValid = new Button(groupMonitor, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "confirmmonitorvalid", confirmMonitorValid);

		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
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
		 * トラップ定義グループ
		 */
		// グループ
		Group groupCheckRule = new Group(groupMonitor, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = MAX_COLUMN;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("trap.definition"));

		/*
		 * コミュニティ
		 */
		Group groupCommunity = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "community", groupCommunity);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = MAX_COLUMN;
		groupCommunity.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN / 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		groupCommunity.setLayoutData(gridData);
		groupCommunity.setText(Messages.getString("community"));


		// ボタン
		this.buttonCommunityCheckOn = new Button(groupCommunity, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "communitycheckon", buttonCommunityCheckOn);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.buttonCommunityCheckOn.setLayoutData(gridData);
		this.buttonCommunityCheckOn.setText(Messages.getString("valid"));
		this.buttonCommunityCheckOn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// ラベル
		label = new Label(groupCommunity, SWT.NONE);
		WidgetTestUtil.setTestId(this, "communityname", label);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("community.name") + " : ");

		// テキスト
		this.textCommunityName = new Text(groupCommunity, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "communitiname", textCommunityName);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textCommunityName.setLayoutData(gridData);
		this.textCommunityName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		/*
		 * 文字コード
		 */
		Group groupCharset = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "charset", groupCharset);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = MAX_COLUMN;
		groupCharset.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN / 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		groupCharset.setLayoutData(gridData);
		groupCharset.setText(Messages.getString("charset.convert"));


		// ボタン
		this.buttonCharsetConvertOn = new Button(groupCharset, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "charsetconvert", buttonCharsetConvertOn);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.buttonCharsetConvertOn.setLayoutData(gridData);
		this.buttonCharsetConvertOn.setText(Messages.getString("valid"));
		this.buttonCharsetConvertOn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// ラベル
		label = new Label(groupCharset, SWT.NONE);
		WidgetTestUtil.setTestId(this, "snmptrapcode", label);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("charset.snmptrap.code") + " : ");

		// テキスト
		this.textCharsetName = new Text(groupCharset, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "charsetname", textCharsetName);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textCharsetName.setLayoutData(gridData);
		this.textCharsetName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * OIDテーブル
		 */
		Group groupOid = new Group(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "oid", groupOid);
		layout = new GridLayout(MAX_COLUMN, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		groupOid.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		groupOid.setLayoutData(gridData);
		groupOid.setText("OID");

		this.buttonNotifyNonSpecifiedTrap = new Button(groupOid, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "communitycheckoff", buttonNotifyNonSpecifiedTrap);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.buttonNotifyNonSpecifiedTrap.setLayoutData(gridData);
		this.buttonNotifyNonSpecifiedTrap.setText(Messages.getString("monitor.snmptrap.notify.on.non.specified.trap.receipt"));
		this.buttonNotifyNonSpecifiedTrap.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		this.comboPriority = new Combo(groupOid, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "priority", comboPriority);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.comboPriority.setLayoutData(gridData);
		this.comboPriority.add(PriorityMessage.STRING_CRITICAL);
		this.comboPriority.add(PriorityMessage.STRING_WARNING);
		this.comboPriority.add(PriorityMessage.STRING_INFO);
		this.comboPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.comboPriority.setText(PriorityMessage.STRING_UNKNOWN);
		this.comboPriority.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupOid, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - 13;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// テキスト
		this.tableDefineListComposite = new TrapDefineListComposite(groupOid, SWT.NONE, new TrapDefineCompositeDefine());
		WidgetTestUtil.setTestId(this, "oidlist", tableDefineListComposite);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.tableDefineListComposite.setLayoutData(gridData);

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		groupNotifyAttribute = new Group(groupMonitor, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		groupNotifyAttribute.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));
		this.m_notifyInfo = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.heightHint = 120;
		gridData.grabExcessHorizontalSpace = true;
		this.m_notifyInfo.setLayoutData(gridData);

		// 初期表示
		MonitorInfo info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(getManagerName());
				info = wrapper.getMonitor(this.monitorId);
			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole_Exception) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					// 上記以外の例外
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}

				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.traputil.4") + errMessage);
				throw new InternalError(e.getMessage());
			}
		}
		
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
		gridData.horizontalSpan = WIDTH_TITLE;
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

		// ラベル（ログフォーマット）
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "logFormat", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("hub.log.format.id") + " : ");

		// テキスト（ログフォーマット）
		this.logFormat = new Combo(groupCollect, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "logFormat", logFormat);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - (WIDTH_TITLE * 2);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.logFormat.setLayoutData(gridData);
		this.logFormat.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = MAX_COLUMN;
		line.setLayoutData(gridData);

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(850, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		this.setInputData(info);

	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {

		// 監視基本情報
		super.setInputData(monitor);
		this.inputData = monitor;

		TrapCheckInfo checkInfo = monitor.getTrapCheckInfo();
		if (checkInfo == null) {
			checkInfo = new TrapCheckInfo();
			checkInfo.setCommunityCheck(false);
			checkInfo.setCharsetConvert(false);
			checkInfo.setNotifyofReceivingUnspecifiedFlg(true);
			checkInfo.setPriorityUnspecified(PriorityColorConstant.TYPE_UNKNOWN);
			monitor.setTrapCheckInfo(checkInfo);
		}

		// コミュニティ名
		if(checkInfo.getCommunityName() != null){
			textCommunityName.setText(checkInfo.getCommunityName());
		}
		if (checkInfo.isCommunityCheck().booleanValue()) {
			buttonCommunityCheckOn.setSelection(true);
		}
		textCommunityName.setEnabled(buttonCommunityCheckOn.getSelection());

		// 文字コード
		if(checkInfo.getCharsetName() != null){
			textCharsetName.setText(checkInfo.getCharsetName());
		}
		if (checkInfo.isCharsetConvert().booleanValue()) {
			buttonCharsetConvertOn.setSelection(true);
		}
		textCharsetName.setEnabled(buttonCharsetConvertOn.getSelection());

		//未指定のトラップ受信時の処理
		buttonNotifyNonSpecifiedTrap.setSelection(checkInfo.isNotifyofReceivingUnspecifiedFlg());
		comboPriority.select(comboPriority.indexOf(PriorityMessage.typeToString(checkInfo.getPriorityUnspecified())));
		tableDefineListComposite.setInputData(checkInfo.getTrapValueInfos());

		// 収集
		if (monitor.isCollectorFlg()) {
			this.confirmCollectValid.setSelection(true);
		}else{
			this.setCollectorEnabled(false);
		}

		// ログフォーマット
		if (monitor.getLogFormatId() != null){
			this.logFormat.setText(monitor.getLogFormatId());
		}
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

		//SNMPTRAP監視固有情報を設定
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_SNMPTRAP);
		monitorInfo.setMonitorType(MonitorTypeConstant.TYPE_TRAP);

		// 監視条件 SNMPTRAP監視情報
		TrapCheckInfo trapInfo = new TrapCheckInfo();
		trapInfo.setMonitorId(monitorInfo.getMonitorId());
		trapInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_SNMPTRAP);
		monitorInfo.setTrapCheckInfo(trapInfo);

		// コミュニティ名
		if (this.buttonCommunityCheckOn.getSelection()) {
			(monitorInfo.getTrapCheckInfo()).setCommunityCheck(true);
		} else {
			(monitorInfo.getTrapCheckInfo()).setCommunityCheck(false);
		}
		if (this.buttonCommunityCheckOn.getSelection() && !"".equals((this.textCommunityName.getText()).trim())) {
			(monitorInfo.getTrapCheckInfo()).setCommunityName(textCommunityName.getText());
		}

		// 文字コード
		if (this.buttonCharsetConvertOn.getSelection()) {
			(monitorInfo.getTrapCheckInfo()).setCharsetConvert(true);
		} else {
			(monitorInfo.getTrapCheckInfo()).setCharsetConvert(false);
		}
		if (this.buttonCharsetConvertOn.getSelection() && !"".equals((this.textCharsetName.getText()).trim())) {
			(monitorInfo.getTrapCheckInfo()).setCharsetName(textCharsetName.getText());
		}

		// 未指定のトラップ情報受信時の処理
		trapInfo.setNotifyofReceivingUnspecifiedFlg(buttonNotifyNonSpecifiedTrap.getSelection());
		trapInfo.setPriorityUnspecified(PriorityMessage.stringToType(comboPriority.getText()));
		List<TrapValueInfo> monitorTrapValueInfoList_old = monitorInfo.getTrapCheckInfo().getTrapValueInfos();
		monitorTrapValueInfoList_old.clear();
		if (tableDefineListComposite.getItems() != null) {
			monitorTrapValueInfoList_old.addAll(tableDefineListComposite.getItems());
		}

		// 通知関連情報とアプリケーションの設定
		// 通知グループIDの設定
		validateResult = this.m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if(validateResult.getID() == null){	// 通知ID警告用出力
				if(!displayQuestion(validateResult)){
					validateResult = null;
					return null;
				}
			}
			else{	// アプリケーション未入力チェック
				return null;
			}
		}

		// 監視間隔
		monitorInfo.setRunInterval(0);

		// 監視 有効/無効
		monitorInfo.setMonitorFlg(this.confirmMonitorValid.getSelection());

		// 収集 有効/無効
		monitorInfo.setCollectorFlg(this.confirmCollectValid.getSelection());
		if (this.logFormat.getText() != null && !this.logFormat.getText().equals("")) {
			monitorInfo.setLogFormatId(this.logFormat.getText());
		}

		return monitorInfo;
	}

	/**
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		MonitorInfo info = this.inputData;
		String managerName = this.getManagerName();
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		if(info != null){
			String[] args = { info.getMonitorId(), managerName };
			if(!this.updateFlg){
				// 作成の場合
				try {
					result = wrapper.addMonitor(info);

					if(result){
						MessageDialog.openInformation(
								null,
								Messages.getString("successful"),
								Messages.getString("message.monitor.33", args));
					} else {
						MessageDialog.openError(
								null,
								Messages.getString("failed"),
								Messages.getString("message.monitor.34", args));
					}
				} catch (MonitorDuplicate_Exception e) {
					// 監視項目IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.53", args));

				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole_Exception) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else {
				// 変更の場合
				String errMessage = "";
				try {
					result = wrapper.modifyMonitor(info);
				} catch (InvalidRole_Exception e) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}

				if(result){
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
				}
				else{
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}
		return result;
	}

	/**
	 * 必須項目の描画更新
	 */
	@Override
	protected void update() {
		super.update();

		textCommunityName.setEnabled(buttonCommunityCheckOn.getEnabled() && buttonCommunityCheckOn.getSelection());
		textCharsetName.setEnabled(buttonCharsetConvertOn.getEnabled() &&buttonCharsetConvertOn.getSelection());

		// コミュニティチェックボタンが有効な場合のみ
		if(this.textCommunityName.getEnabled() && "".equals(this.textCommunityName.getText())){
			this.textCommunityName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textCommunityName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 文字コードチェックボタンが有効な場合のみ
		if(this.textCharsetName.getEnabled() && "".equals(this.textCharsetName.getText())){
			this.textCharsetName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textCharsetName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		comboPriority.setEnabled(buttonNotifyNonSpecifiedTrap.getEnabled() && buttonNotifyNonSpecifiedTrap.getSelection());

		// ログフォーマット
//		if(this.logFormat.getEnabled() && "".equals(this.logFormat.getText())){
//			this.logFormat.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
//		}else{
//			this.logFormat.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//		}
	}
	
	
	/**
	 * 収集エリアを有効/無効化します。
	 *
	 */
	private void setCollectorEnabled(boolean enabled){
		logFormat.setEnabled(enabled);

		update();
	}
	
	/**
	 * オーナーロールを設定する
	 * @return
	 */
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		
		logFormat.setText("");
		logFormat.removeAll();
		
		//ログフォーマット一覧情報取得
		List<LogFormat> list = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			try {
				list = wrapper.getLogFormatListByOwnerRole(ownerRoleId);
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
			}
			//一覧が空の場合
			if (list == null) {
				list = Collections.emptyList();
			}

			logFormat.add("");
			for (LogFormat format:list){
				logFormat.add(format.getLogFormatId());
			}
		}
	}
}
