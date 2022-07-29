/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.dialog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.openapitools.client.model.EventCustomCommandInfoDataResponse;
import org.openapitools.client.model.EventCustomCommandInfoResponse;
import org.openapitools.client.model.EventLogInfoRequest;
import org.openapitools.client.model.EventLogInfoResponse;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.action.GetEventCustomCommandListTableDefine;
import com.clustercontrol.monitor.composite.EventCustomCommandComposite;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.util.EventCustomCommandResultPoller;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
/**
 * 監視履歴[イベント・カスタムコマンドの実行]ダイアログクラス<BR>
 */
public class EventCustomCommandRunDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( EventCustomCommandRunDialog.class );
	
	/** マネージャ */
	private String managerName = null;
	/** 選択されたイベント情報 */
	private List<EventLogInfoRequest> selectEventList = null;
	/** イベント表示情報 */
	private MultiManagerEventDisplaySettingInfo eventDspInfo = null;
	/** カスタムコマンド設定情報。 */
	private Map<Integer, EventCustomCommandInfoResponse> customCommandInfoMap = null;
	
	private Shell shell;

	private ManagerListComposite m_managerComposite = null;
	
	/** イベントカスタムコマンド選択コンポジット */
	private EventCustomCommandComposite tableComposite = null;
	
	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName イベントカスタムスクリプトを実行するマネージャ
	 * @param selectEventList 選択されたイベント情報
	 */
	public EventCustomCommandRunDialog(Shell parent, String managerName, List<EventLogInfoRequest> selectEventList, MultiManagerEventDisplaySettingInfo eventDspInfo) {
		super(parent);
		this.managerName = managerName;
		this.selectEventList = selectEventList;
		this.eventDspInfo = eventDspInfo;
		getCustomCommandInfo();
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(540, 330);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		this.shell = this.getShell();
		
		// タイトル
		shell.setText(Messages.getString("dialog.monitor.events.customcommand.run"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		final int NUM_COLUMNS = 4;
		
		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = NUM_COLUMNS;
		parent.setLayout(layout);

		label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.manager") + " : ");
		
		this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		
		WidgetTestUtil.setTestId(this, "managerComposite", m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = NUM_COLUMNS - 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		// 空白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = NUM_COLUMNS;
		label.setLayoutData(gridData);

		/*
		 * 選択中のイベント件数
		 */
		// ラベル
		label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "selectEventLabel", label);
		GridData gridData_event = new GridData();
		gridData_event.horizontalAlignment = GridData.FILL;
		gridData_event.grabExcessHorizontalSpace = true;
		gridData_event.horizontalSpan = 1;
		label.setLayoutData(gridData_event);
		label.setText(Messages.getString("dialog.monitor.events.customcommand.run.label.eventnum") + " : ");

		// 件数
		label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "select.event.count", label);
		gridData_event = new GridData();
		gridData_event.horizontalAlignment = GridData.FILL;
		gridData_event.grabExcessHorizontalSpace = true;
		gridData_event.horizontalSpan = NUM_COLUMNS - 1;
		label.setLayoutData(gridData_event);
		label.setText(String.valueOf(this.selectEventList.size()) + Messages.getString("record"));

		// 改行
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space4", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = NUM_COLUMNS;
		label.setLayoutData(gridData);

		/*
		 * カスタムコマンド一覧
		 */
		// ラベル
		label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "list", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = NUM_COLUMNS;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("dialog.monitor.events.customcommand.run.label.list.title"));

		this.tableComposite = new EventCustomCommandComposite(parent, SWT.NONE);
		
		WidgetTestUtil.setTestId(this, null, tableComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = NUM_COLUMNS;
		this.tableComposite.setLayoutData(gridData);
		
		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = NUM_COLUMNS;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
		
		//パラメータをセット
		updateDisplay();
	}

	private void updateDisplay() {
		this.m_managerComposite.setText(this.managerName);
		this.tableComposite.updateDisp(this.customCommandInfoMap);
	}
	
	/**
	 * 実行ボタン押下時のメイン処理
	 */
	@Override
	protected void okPressed() {
		// 選択行のカスタムコマンド名とイベントカスタムコマンド番号を取得する
		
		TableItem[] items = this.tableComposite.getTable().getItems();
		
		if (items == null || items.length == 0) {
			return;
		}
		
		//選択されたコマンドNo
		int commandNo = -1;
		
		for (int i = 0; i < items.length; i++) {
			List<?> row = (List<?>)items[i].getData();
			Boolean select = (Boolean) row.get(GetEventCustomCommandListTableDefine.SELECT);
			if (Boolean.TRUE.equals(select)) {
				commandNo = (Integer) row.get(GetEventCustomCommandListTableDefine.COMMAND_NO);
			}
		}

		if (commandNo == -1) {
			//コマンドが選択されていない時
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.monitor.event.customcommand.notselect"));
			return;
		}
		
		EventCustomCommandInfoResponse commandInfo = this.customCommandInfoMap.get(commandNo);
		
		if (this.selectEventList.size() > commandInfo.getMaxEventSize() ) {
			//Hinemosプロパティの上限数より多いイベントが選択されているとき
			MessageDialog.openInformation(null, 
					Messages.getString("message"),
					Messages.getString("message.monitor.event.customcommand.eventnum.over",
					new String[]{String.valueOf(commandInfo.getMaxEventSize())}));
			return;
		}
		
		boolean isOkPush = MessageDialog.openQuestion(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.monitor.event.customcommand.execute.question", 
							new String[]{commandInfo.getDisplayName()}));
		if (!isOkPush) {
			return;
		}
		
		//カスタムイベントコマンドの実行
		boolean executeStart = EventCustomCommandResultPoller.getInstance().startEventCustomCommand(
				this.managerName, commandNo, commandInfo, this.selectEventList, eventDspInfo);
		
		if (executeStart) {
			MessageDialog.openInformation(null, 
					Messages.getString("message"),
					Messages.getString("message.monitor.event.customcommand.execute",
							new String[]{commandInfo.getDisplayName()}));
		}
	}

	/**
	 * 実行ボタンと閉じるボタンを作成します。
	 *
	 * @param parent 親のコンポジット（ボタンバー）
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//実行(Run)ボタン
		this.createButton(parent, IDialogConstants.OK_ID, Messages.getString("run"), true);
		// 閉じる(Close)ボタン
		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);

	}

	/**
	 * カスタムコマンド情報取得。
	 */
	private void getCustomCommandInfo() {
		customCommandInfoMap = new LinkedHashMap<>();
		
		EventCustomCommandInfoDataResponse eventInfoData = null;

		// カスタムコマンド設定情報取得
		CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
		try {
			eventInfoData = wrapper.getEventCustomCommandSettingInfo();
			for (Map.Entry<String, EventCustomCommandInfoResponse> entry : eventInfoData.getEvemtCustomCommandMap().entrySet()) {
				customCommandInfoMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
			}
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("run(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") +
					", " + HinemosMessage.replace(e.getMessage()));
		}
	}
}
