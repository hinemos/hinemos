/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.winevent.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.run.dialog.CommonMonitorStringDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.winevent.util.WinEventUtil;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.WinEventCheckInfo;

/**
 * Windowsイベント監視の設定ダイアログクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class WinEventDialog extends CommonMonitorStringDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( WinEventDialog.class );

	// ----- instance フィールド ----- //
	private Button levelCritical = null;
	private Button levelWarning = null;
	private Button levelVerbose = null;
	private Button levelError = null;
	private Button levelInformational = null;
	private Text logName = null;
	private Text source = null;
	private Text eventId = null;
	private Text category = null;
	private Text keywords = null;
	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public WinEventDialog(Shell parent) {
		super(parent, null);
		logLineFlag = true;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public WinEventDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		logLineFlag = true;
		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.winevent.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));

		/*
		 * イベントレベル
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "wineventlevel", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("winevent.level") + " : ");

		// ボタン(重大)
		this.levelCritical = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "criticalcheck", levelCritical);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.levelCritical.setLayoutData(gridData);
		this.levelCritical.setText(Messages.getString("winevent.level.critical"));

		// ボタン(警告)
		this.levelWarning = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "warningcheck", levelWarning);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.levelWarning.setLayoutData(gridData);
		this.levelWarning.setText(Messages.getString("winevent.level.warning"));

		// ボタン(詳細)
		this.levelVerbose = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "verbosecheck", levelVerbose);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.levelVerbose.setLayoutData(gridData);
		this.levelVerbose.setText(Messages.getString("winevent.level.verbose"));

		// ボタン(エラー)
		this.levelError = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "errorcheck", levelError);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.levelError.setLayoutData(gridData);
		this.levelError.setText(Messages.getString("winevent.level.error"));

		// ボタン(情報)
		this.levelInformational = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "infomationalcheck", levelInformational);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.levelInformational.setLayoutData(gridData);
		this.levelInformational.setText(Messages.getString("winevent.level.informational"));

		/*
		 * イベント ログ
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "wineveltlog", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("winevent.log") + " : ");
		// テキスト
		this.logName = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "logname", logName);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.logName.setLayoutData(gridData);
		this.logName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * イベント ソース
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "wineventsource", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("winevent.source") + " : ");
		// テキスト
		this.source = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "source", source);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.source.setLayoutData(gridData);
		this.source.setMessage(Messages.getString("message.winevent.1"));

		/*
		 * イベントID
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "wineventid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("winevent.id") + " : ");
		// テキスト
		this.eventId = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "eventid", eventId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.eventId.setLayoutData(gridData);
		this.eventId.setMessage(Messages.getString("message.winevent.2"));

		/*
		 * タスクのカテゴリ
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "wineventcategory", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("winevent.category") + " : ");
		// テキスト
		this.category = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "category", category);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.category.setLayoutData(gridData);
		this.category.setMessage(Messages.getString("message.winevent.3"));

		/*
		 * キーワード
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "keywords", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("winevent.keywords") + " : ");
		// テキスト
		this.keywords = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "keywords", keywords);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.keywords.setLayoutData(gridData);
		this.keywords.setMessage(Messages.getString("message.winevent.4"));

		// 監視間隔の設定を利用不可とする
		this.m_monitorRule.setRunIntervalEnabled(false);

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfo info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(this.monitorId);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				throw new InternalError(e.getMessage());
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("customizeDialog(), " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				throw new InternalError(e.getMessage());
			}
		}
		this.setInputData(info);
		this.update();
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		super.update();

		// 各項目が必須項目であることを明示
		if("".equals(this.logName.getText())){
			this.logName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.logName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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
		this.inputData = monitor;

		// 監視条件 Windowsイベント監視情報
		WinEventCheckInfo checkInfo = monitor.getWinEventCheckInfo();
		if(checkInfo == null){
			checkInfo = new WinEventCheckInfo();

			// デフォルト設定
			// イベントレベル：重大、エラー、警告
			checkInfo.setLevelCritical(true);
			checkInfo.setLevelWarning(true);
			checkInfo.setLevelVerbose(false);
			checkInfo.setLevelError(true);
			checkInfo.setLevelInformational(false);

			// イベントログ：Application, System
			checkInfo.getLogName().add("Application");
			checkInfo.getLogName().add("System");
		}
		this.levelCritical.setSelection(checkInfo.isLevelCritical());
		this.levelWarning.setSelection(checkInfo.isLevelWarning());
		this.levelVerbose.setSelection(checkInfo.isLevelVerbose());
		this.levelError.setSelection(checkInfo.isLevelError());
		this.levelInformational.setSelection(checkInfo.isLevelInformational());
		if(checkInfo.getLogName() != null) this.logName.setText(listToCommaSeparatedString(checkInfo.getLogName()));
		if(checkInfo.getSource() != null) this.source.setText(listToCommaSeparatedString(checkInfo.getSource()));
		if(checkInfo.getEventId() != null) this.eventId.setText(listToCommaSeparatedString(checkInfo.getEventId()));
		if(checkInfo.getCategory() != null) this.category.setText(listToCommaSeparatedString(checkInfo.getCategory()));
		if(checkInfo.getKeywords() != null) this.keywords.setText(keywordLongToString(checkInfo.getKeywords()));

		m_stringValueInfo.setInputData(monitor);
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

		// Windowsイベント監視（文字列）固有情報を設定
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_WINEVENT);

		// 監視条件 Windowsイベント監視情報
		WinEventCheckInfo winEventInfo = new WinEventCheckInfo();
		winEventInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_WINEVENT);
		winEventInfo.setMonitorId(monitorInfo.getMonitorId());
		winEventInfo.setLevelCritical(this.levelCritical.getSelection());
		winEventInfo.setLevelWarning(this.levelWarning.getSelection());
		winEventInfo.setLevelVerbose(this.levelVerbose.getSelection());
		winEventInfo.setLevelError(this.levelError.getSelection());
		winEventInfo.setLevelInformational(this.levelInformational.getSelection());

		for(String logName : commaSeparatedStringToStringList(this.logName.getText())){
			winEventInfo.getLogName().add(logName);
		}

		for(String source : commaSeparatedStringToStringList(this.source.getText())){
			winEventInfo.getSource().add(source);
		}

		try {
			for(Integer id : commaSeparatedStringToIntegerList(this.eventId.getText())){
				winEventInfo.getEventId().add(id);
			}
		} catch (HinemosUnknown e) {
			setValidateResult(Messages.getString("message.hinemos.1"), HinemosMessage.replace(e.getMessage()) + "\n"+Messages.getString("winevent.id"));
		}

		try {
			for(Integer category : commaSeparatedStringToIntegerList(this.category.getText())){
				winEventInfo.getCategory().add(category);
			}
		} catch (HinemosUnknown e) {
			setValidateResult(Messages.getString("message.hinemos.1"), HinemosMessage.replace(e.getMessage()) + "\n"+Messages.getString("winevent.category"));
		}

		if(this.keywords.getText() != null && ! "".equals(this.keywords.getText())) {
			for(Long keyword : keywordStringToLongList(this.keywords.getText())){
				winEventInfo.getKeywords().add(keyword);
			}
		}

		if(validateResult != null){
			return null;
		}

		monitorInfo.setWinEventCheckInfo(winEventInfo);

		// 結果判定の定義
		validateResult = m_stringValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 通知関連情報とアプリケーションの設定
		validateResult = m_notifyInfo.createInputData(monitorInfo);
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

		if(info != null){
			String[] args = { info.getMonitorId(), managerName };
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
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
						MessageDialog.openInformation(null, Messages.getString("message"),
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

	private static String listToCommaSeparatedString(List<?> list){
		if(list != null){
			String string = list.toString();
			string = string.replace("[", "");
			string = string.replace("]", "");
			return string;
		}else{
			return null;
		}
	}

	private static List<Integer> commaSeparatedStringToIntegerList(String string) throws HinemosUnknown{
		String[] numbers = string.split("\\s*,\\s*");
		ArrayList<Integer> list = new ArrayList<Integer>();
		try{
			for(int i=0; i<numbers.length; i++){
				if(numbers[i] != null && !"".equals(numbers[i])){
					list.add(Integer.parseInt(numbers[i]));
				}
			}
		} catch (NumberFormatException e){
			throw new HinemosUnknown(Messages.getString("message.winevent.6"), e);
		}
		return list;
	}

	private static List<Long> keywordStringToLongList(String keywords){
		ArrayList<Long> longList = new ArrayList<Long>();

		List<String> keywordList = commaSeparatedStringToStringList(keywords);
		for(String keyword : keywordList){
			// 定義済みキーワードの場合
			if(WinEventUtil.containsKeywordString(keyword)){
				longList.add(WinEventUtil.getKeywordLong(keyword));
			}
			else{
				// 数値の場合
				try{
					longList.add(Long.parseLong(keyword));
				}
				// それ以外
				catch (NumberFormatException e){
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.winevent.7"));

					m_log.error("unknown keyword : " + keyword);
					throw e;
				}
			}

		}
		return longList;
	}

	private static String keywordLongToString(List<Long> keywordList){
		ArrayList<String> list = new ArrayList<String>();
		for(Long keyword : keywordList){
			String keywordStr = WinEventUtil.getKeywordString(keyword);

			// Keywordのlong値に対応するString値が見つかった場合、変換した文字列を格納する
			if(keywordStr != null){
				list.add(keywordStr);
			}
			// Keywordのlong値に対応するString値が見つからない場合、long値をそのまま文字列として格納する
			else{
				list.add(keyword.toString());
			}
		}
		return listToCommaSeparatedString(list);
	}

	private static List<String> commaSeparatedStringToStringList(String string){
		String[] strings = string.split("\\s*,\\s*");
		ArrayList<String> list = new ArrayList<String>();
		for(int i=0; i<strings.length; i++){
			if(strings[i] != null && !"".equals(strings[i])){
				list.add(strings[i]);
			}
		}
		return list;
	}
}
