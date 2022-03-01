/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
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
import org.openapitools.client.model.LogFormatResponse;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.hub.util.HubRestClientWrapper;
import com.clustercontrol.monitor.run.action.GetStringFilterTableDefine;
import com.clustercontrol.monitor.run.composite.StringValueInfoComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 文字列系監視設定共通ダイアログクラス<BR>
 *
 */
public class CommonMonitorStringDialog extends CommonMonitorDialog {

	/** 文字列監視判定情報 */
	protected StringValueInfoComposite m_stringValueInfo = null;

	/** メッセージにデフォルトを入れるフラグ */
	protected boolean logLineFlag = false;
	
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
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 */
	public CommonMonitorStringDialog(Shell parent, String managerName) {
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

		super.customizeDialog(parent);

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// 文字列判定定義情報
		ArrayList<TableColumnInfo> tableDefine = GetStringFilterTableDefine.get();
		this.m_stringValueInfo = new StringValueInfoComposite(groupDetermine, SWT.NONE, tableDefine, logLineFlag);
		WidgetTestUtil.setTestId(this, null, m_stringValueInfo);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 220;
		m_stringValueInfo.setLayoutData(gridData);
		
		/*
		 * 収集グループ
		 */
		groupCollect = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collect", groupCollect);
		GridLayout layout = new GridLayout(1, true);
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

		// ラベル（ログフォーマット）
		Label label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "logFormat", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("hub.log.format.id") + " : ");

		// コンボボックス（ログフォーマット）
		this.logFormat = new Combo(groupCollect, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "logFormat", logFormat);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT - SMALL_UNIT - WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.logFormat.setLayoutData(gridData);
		this.logFormat.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		super.createInputData();

		if(validateResult != null){
			return null;
		}

		// 収集
		monitorInfo.setCollectorFlg(this.confirmCollectValid.getSelection());

		if(this.logFormat.getText() != null && !this.logFormat.getText().equals("")){
			monitorInfo.setLogFormatId(this.logFormat.getText());
		}

		// 監視種別を文字列に設定する
		monitorInfo.setMonitorType(MonitorInfoResponse.MonitorTypeEnum.STRING);

		return monitorInfo;
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
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor 設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {
		super.setInputData(monitor);

		// 収集
		if (monitor.getCollectorFlg()) {
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
	 * オーナーロールを設定する
	 * @return
	 */
	@Override
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		
		logFormat.setText("");
		logFormat.removeAll();
		
		String managerName = m_monitorBasic.getManagerListComposite().getText();
		if (managerName == null)
			return;
		
		//ログフォーマット一覧情報取得
		List<LogFormatResponse> list = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
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
		for (LogFormatResponse format:list){
			logFormat.add(format.getLogFormatId());
		}
	}
}