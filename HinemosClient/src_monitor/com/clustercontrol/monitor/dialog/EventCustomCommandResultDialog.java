/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.EventCustomCommandInfoResponse;
import org.openapitools.client.model.EventCustomCommandResultResponse;
import org.openapitools.client.model.EventCustomCommandResultRootResponse;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.bean.EventCustomCommandStatusConstant;
import com.clustercontrol.monitor.composite.EventCustomCommandResultComposite;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.Messages;
/**
 * 監視履歴[イベント・カスタムコマンドの実行結果]ダイアログクラス<BR>
 */
public class EventCustomCommandResultDialog extends CommonDialog {

	private static Log m_log = LogFactory.getLog( EventCustomCommandResultDialog.class );
	
	private Shell shell;
	private String managerName;
	private EventCustomCommandInfoResponse customCommnadInfo;
	private EventCustomCommandResultRootResponse result;
	private MultiManagerEventDisplaySettingInfo eventDspSettingInfo;
	
	/** 実行件数 */
	private Label totalCount = null;
	/** 正常件数 */
	private Label normalCount = null;
	/** 警告件数 */
	private Label warnCount = null;
	/** エラー件数 */
	private Label errorCount = null;
	/** キャンセル件数 */
	private Label cancelCount = null;
	/** コマンド起動日時 */
	private Label commandStartTime = null;
	/** 実行日時 */
	private Label runTime = null;
	
	/** イベントカスタムコマンド選択コンポジット */
	private EventCustomCommandResultComposite tableComposite = null;
	
	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 */
	public EventCustomCommandResultDialog(Shell parent, String managerName, 
			EventCustomCommandInfoResponse customCommnadInfo, EventCustomCommandResultRootResponse result,
			MultiManagerEventDisplaySettingInfo eventDspSettingInfo) {
		super(parent);
		this.managerName = managerName;
		this.customCommnadInfo = customCommnadInfo;
		this.result = result;
		this.eventDspSettingInfo = eventDspSettingInfo;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(580, 400);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		try {
			this.shell = this.getShell();
	
			// タイトル
			shell.setText(Messages.getString("dialog.monitor.events.customcommand.result.title"));
			
			// 変数として利用されるラベル
			Label label = null;
			// 変数として利用されるグリッドデータ
			GridData gridData = null;
	
			// レイアウト
			GridLayout layout = new GridLayout(1, false);
			layout.marginWidth = 10;
			layout.marginHeight = 10;
			layout.numColumns = 5;
			parent.setLayout(layout);
			
			// ヘッダメッセージ
			Label managerText = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.headermsg", managerText);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 5;
			managerText.setLayoutData(gridData);
			managerText.setText(Messages.getString("dialog.monitor.events.customcommand.result.headermsg", new String[]{customCommnadInfo.getDisplayName(), managerName}));
	
			// 空白(行間を開ける)
			label = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "space", label);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 5;
			label.setLayoutData(gridData);
			
			/*
			 * 実行件数
			 */
			// ラベル
			label = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.count", label);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			label.setLayoutData(gridData);
			label.setText(Messages.getString("dialog.monitor.events.customcommand.result.count") + " : ");
	
			//値
			totalCount = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.result", totalCount);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 4;
			totalCount.setLayoutData(gridData);
			
			/*
			 * 実行結果
			 */
			// ラベル
			label = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.result", label);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			label.setLayoutData(gridData);
			label.setText(Messages.getString("dialog.monitor.events.customcommand.result.result") + " : ");
	
			// 正常
			normalCount = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.normal", normalCount);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			normalCount.setLayoutData(gridData);
	
	
			// 警告
			warnCount = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.warn", warnCount);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			warnCount.setLayoutData(gridData);
			
			// エラー
			errorCount = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.error", errorCount);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			errorCount.setLayoutData(gridData);
	
	
			// キャンセル
			cancelCount = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.cancel", cancelCount);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			cancelCount.setLayoutData(gridData);
	
			/*
			 * コマンド起動日時
			 */
			// ラベル
			label = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.starttime", label);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			label.setLayoutData(gridData);
			label.setText(Messages.getString("dialog.monitor.events.customcommand.result.starttime") + " : ");
			
			// 値
			commandStartTime = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.starttime.value", commandStartTime);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 4;
			commandStartTime.setLayoutData(gridData);
			
			/*
			 * 実行日時
			 */
			//ラベル
			label = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.runtime", label);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			label.setLayoutData(gridData);
			label.setText(Messages.getString("dialog.monitor.events.customcommand.result.runtime") + " : ");
			
			// 値
			runTime = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.date.range", runTime);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 4;
			runTime.setLayoutData(gridData);
	
			// 空白
			label = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "space", label);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 5;
			label.setLayoutData(gridData);
			
			/*
			 * 結果詳細
			 */
			// ラベル
			label = new Label(parent, SWT.LEFT);
			WidgetTestUtil.setTestId(this, "dialog.monitor.events.customcommand.result.detail.title", managerText);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 5;
			label.setLayoutData(gridData);
			label.setText(Messages.getString("dialog.monitor.events.customcommand.result.detail.title") + " : ");
	
			this.tableComposite = new EventCustomCommandResultComposite(parent, SWT.NONE);
			
			WidgetTestUtil.setTestId(this, null, tableComposite);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.verticalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			gridData.horizontalSpan = 5;
			this.tableComposite.setLayoutData(gridData);
			
			// 画面中央に表示
			Display display = shell.getDisplay();
			shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
					(display.getBounds().height - shell.getSize().y) / 2);
	
			this.updateDisp();
		} catch (Exception e) {
			m_log.error("customizeDialog", e);
		}
	}
	
	private void updateDisp() {
		//件数をカウント
		int normalCnt = 0;
		int warnCnt = 0;
		int errorCnt = 0;
		int cancelCnt = 0;
		
		for (EventCustomCommandResultResponse res : this.result.getEventResultList()) {
			
			switch (res.getStatus()) {
			case EventCustomCommandStatusConstant.STATUS_NORMAL:
				normalCnt++;
				break;
			case EventCustomCommandStatusConstant.STATUS_WARNING:
				warnCnt++;
				break;
			case EventCustomCommandStatusConstant.STATUS_ERROR:
				errorCnt++;
				break;
			case EventCustomCommandStatusConstant.STATUS_CANCEL:
				cancelCnt++;
				break;
			default:
				break;
			}
		}

		totalCount.setText(Messages.getString("dialog.monitor.events.customcommand.result.record", 
				new String[]{String.valueOf(result.getCount())}));
				
		normalCount.setText(Messages.getString("dialog.monitor.events.customcommand.result.normal",
					new String[] {String.valueOf(normalCnt)}));
					
		warnCount.setText(Messages.getString("dialog.monitor.events.customcommand.result.warn",
				new String[] {String.valueOf(warnCnt)}));
				
		errorCount.setText(Messages.getString("dialog.monitor.events.customcommand.result.error",
				new String[] {String.valueOf(errorCnt)}));
				
		cancelCount.setText(Messages.getString("dialog.monitor.events.customcommand.result.cancel",
				new String[] {String.valueOf(cancelCnt)}));
				
		commandStartTime.setText(result.getCommandKickTime());
		
		runTime.setText(
				Messages.getString("dialog.monitor.events.customcommand.result.date.range" ,
				new String[] {
						result.getCommandStartTime(),
						result.getCommandEndTime()
				}));
		
		this.tableComposite.updateDisp(this.result, this.managerName, this.eventDspSettingInfo);
	}
	
	/**
	 * 実行ボタンと閉じるボタンを作成します。
	 *
	 * @param parent 親のコンポジット（ボタンバー）
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//実行(Run)ボタン
		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), true);

	}
}
