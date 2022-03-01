/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.monitor.view.ScopeView;
import com.clustercontrol.monitor.view.StatusView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 監視管理機能用の設定ページクラス<BR>
 *
 * @version 2.1.1
 * @since 1.0.0
 */
public class MonitorPreferencePage extends FieldEditorPreferencePage implements
IWorkbenchPreferencePage {

	/** 監視[スコープ]ビューの自動更新フラグ */
	public static final String P_SCOPE_UPDATE_FLG = "scopeUpdateFlg";

	/** 監視[スコープ]ビューの自動更新周期 */
	public static final String P_SCOPE_UPDATE_CYCLE = "scopeUpdateCycle";

	/** 監視[ステータス]ビューの自動更新フラグ */
	public static final String P_STATUS_UPDATE_FLG = "statusUpdateFlg";

	/** 監視[ステータス]ビューの自動更新周期 */
	public static final String P_STATUS_UPDATE_CYCLE = "stateUpdateCycle";

	/** 監視[ステータス]ビューの新規ステータスフラグ */
	public static final String P_STATUS_NEW_STATE_FLG = "statusNewStateFlg";

	/** 監視[イベント]ビューの自動更新フラグ */
	public static final String P_EVENT_UPDATE_FLG = "eventUpdateFlg";

	/** 監視[イベント]ビューの自動更新周期 */
	public static final String P_EVENT_UPDATE_CYCLE = "eventUpdateCycle";

	/** 監視[イベント]ビューのメッセージ表示 */
	public static final String P_EVENT_MESSAGE_FLG = "eventMessageFlg";

	/** 監視[イベント]ビューの表示イベント数 */
	public static final String P_EVENT_MAX = "eventMaxMessages";
	
	/** 監視[ステータス]ビューの新規イベントフラグ */
	public static final String P_EVENT_NEW_EVENT_FLG = "eventNewEventFlg";
	
	/** 監視[イベント]ビューで確認済/確認中/未確認に変更する際に確認ダイアログを表示するかどうかのフラグ */
	public static final String P_EVENT_CONFIRM_DIALOG_FLG = "eventConfirmDialogtFlg";

	/** SNMPTRAP[作成・変更]ダイアログのtrap_value_infoの表示件数 */
	public static final String P_MAX_TRAP_OID = "maxTrapOid";

	private static final String MSG_ENABLE =
			Messages.getString("autoupdate.enable");

	private static final String MSG_CYCLE =
			Messages.getString("autoupdate.cycle") + " : ";

	private static final String MSG_MESSAGE =
			Messages.getString("over.limit.message");

	private static final String MSG_MAX_EVENTS =
			Messages.getString("number.of.display.events") + " : ";

	private static final String MSG_MAX_TRAP_OIDS =
			Messages.getString("number.of.display.trap.oids") + " : ";

	private static final String MSG_NEW_STATUS_MESSAGE =
			Messages.getString("new.status.message");

	private static final String MSG_NEW_EVENT_MESSAGE =
			Messages.getString("new.event.message");

	private static final String MSG_EVENT_CONFIRM_DIALOG_MESSAGE =
			Messages.getString("event.confirm.dialog.message");
	
	public MonitorPreferencePage() {
		super(GRID);
	}

	/**
	 * 初期値設定
	 *
	 * @param workbench ワークベンチオブジェクト
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * 設定フィールドを生成します。
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;

		// スコープ監視ビュー関連
		Group scopeGroup = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, "scope", scopeGroup);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		scopeGroup.setLayoutData(gridData);
		scopeGroup.setText(Messages.getString("view.monitor.scope"));
		// フラグ
		this.addField(new BooleanFieldEditor(P_SCOPE_UPDATE_FLG, MSG_ENABLE,
				scopeGroup));
		// 周期
		IntegerFieldEditor scopeCycle =
				new IntegerFieldEditor(P_SCOPE_UPDATE_CYCLE, MSG_CYCLE, scopeGroup);
		scopeCycle.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] args = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		scopeCycle.setErrorMessage(Messages.getString("message.hinemos.8", args ));
		this.addField(scopeCycle);

		// ステータス監視ビュー関連
		Group statusGroup = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, "status", statusGroup);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		statusGroup.setLayoutData(gridData);
		statusGroup.setText(Messages.getString("view.monitor.status"));
		// フラグ
		this.addField(new BooleanFieldEditor(P_STATUS_UPDATE_FLG, MSG_ENABLE,
				statusGroup));
		// 周期
		IntegerFieldEditor statusCycle =
				new IntegerFieldEditor(P_STATUS_UPDATE_CYCLE, MSG_CYCLE, statusGroup);
		statusCycle.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		statusCycle.setErrorMessage(Messages.getString("message.hinemos.8", args ));
		this.addField(statusCycle);
		// 新規ステータス発生時にメッセージ表示
		this.addField(new BooleanFieldEditor(P_STATUS_NEW_STATE_FLG, MSG_NEW_STATUS_MESSAGE,
				statusGroup));
		// ステータス監視ビューのレイアウトを調整するため、ダミーのフィールドを作成
		this.addField(new DummyFieldEditor(statusGroup));

		// イベント監視ビュー関連
		Group eventGroup = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, "event", eventGroup);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		eventGroup.setLayoutData(gridData);
		eventGroup.setText(Messages.getString("view.monitor.event"));
		// フラグ
		this.addField(new BooleanFieldEditor(P_EVENT_UPDATE_FLG, MSG_ENABLE,
				eventGroup));
		// 周期
		IntegerFieldEditor eventCycle =
				new IntegerFieldEditor(P_EVENT_UPDATE_CYCLE, MSG_CYCLE, eventGroup);
		eventCycle.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		eventCycle.setErrorMessage(Messages.getString("message.hinemos.8", args ));
		this.addField(eventCycle);
		// 新規イベント発生時にメッセージ表示
		this.addField(new BooleanFieldEditor(P_EVENT_NEW_EVENT_FLG, MSG_NEW_EVENT_MESSAGE,
				eventGroup));
		// メッセージ表示
		this.addField(new BooleanFieldEditor(P_EVENT_MESSAGE_FLG, MSG_MESSAGE,
				eventGroup));

		// 表示イベント数
		IntegerFieldEditor eventMax =
				new IntegerFieldEditor(P_EVENT_MAX, MSG_MAX_EVENTS, eventGroup);
		eventMax.setValidRange(1, DataRangeConstant.MONITOR_EVENT_MAX);
		String[] argsEvent = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.MONITOR_EVENT_MAX) };
		eventMax.setErrorMessage(Messages.getString("message.hinemos.8", argsEvent ));
		this.addField(eventMax);
		// 確認ダイアログ表示フラグ
		BooleanFieldEditor eventConfirmDialogField = new BooleanFieldEditor(P_EVENT_CONFIRM_DIALOG_FLG, MSG_EVENT_CONFIRM_DIALOG_MESSAGE,
				eventGroup);
		String eventConfirmDialogEditable = System.getProperty("event.confirm.dialog.editable");
		if (eventConfirmDialogEditable != null && eventConfirmDialogEditable.equals("false")) {
			// 確認ダイアログを表示するかどうかをユーザが変更できない設定の場合
			eventConfirmDialogField.setEnabled(false, eventGroup);
		}
		this.addField(eventConfirmDialogField);

		// SNMPTRAP監視ビュー関連
		Group trapGroup = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, "trap", trapGroup);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		trapGroup.setLayoutData(gridData);
		trapGroup.setText(Messages.getString("view.monitor.trap"));
		
		// 表示TrapValueInfo件数
		IntegerFieldEditor maxTrapOid =
				new IntegerFieldEditor(P_MAX_TRAP_OID, MSG_MAX_TRAP_OIDS, trapGroup);
		maxTrapOid.setValidRange(1, DataRangeConstant.MONITOR_TRAP_OID_MAX);
		String[] argsTrap = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.MONITOR_TRAP_OID_MAX) };
		maxTrapOid.setErrorMessage(Messages.getString("message.hinemos.8", argsTrap ));
		this.addField(maxTrapOid);
	}

	/**
	 * 設定内容を各ビューに反映します。
	 *
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean result = super.performOk();

		this.applySetting();

		return result;
	}

	/**
	 * 設定内容を反映します。
	 */
	private void applySetting() {
		IPreferenceStore store = this.getPreferenceStore();

		// 存在するビュー全てに設定を適応する。

		IWorkbench workbench = ClusterControlPlugin.getDefault().getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();

		int windowCount = windows.length;

		for (int i = 0; i < windowCount; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			int pageCount = pages.length;

			for (int j = 0; j < pageCount; j++) {
				ScopeView scopeView = (ScopeView) pages[j]
						.findView(ScopeView.ID);
				if (scopeView != null) {
					int cycle = store.getInt(P_SCOPE_UPDATE_CYCLE);
					scopeView.setInterval(cycle);
					if (store.getBoolean(P_SCOPE_UPDATE_FLG) && scopeView.isUpdateSuccess()) {
						scopeView.startAutoReload();
					} else {
						scopeView.stopAutoReload();
					}
				}

				StatusView statusView = (StatusView) pages[j]
						.findView(StatusView.ID);
				if (statusView != null) {
					int cycle = store.getInt(P_STATUS_UPDATE_CYCLE);
					statusView.setInterval(cycle);
					if (store.getBoolean(P_STATUS_UPDATE_FLG) && statusView.isUpdateSuccess()) {
						statusView.startAutoReload();
					} else {
						statusView.stopAutoReload();
					}
				}

				EventView eventView = (EventView) pages[j]
						.findView(EventView.ID);
				if (eventView != null) {
					int cycle = store.getInt(P_EVENT_UPDATE_CYCLE);
					eventView.setInterval(cycle);
					if (store.getBoolean(P_EVENT_UPDATE_FLG) && eventView.isUpdateSuccess()) {
						eventView.startAutoReload();
					} else {
						eventView.stopAutoReload();
					}
				}
			}
		}
	}
	
	/**
	 * レイアウトを調整するためのダミーフィールドクラス。<BR>
	 *
	 */
	static class DummyFieldEditor extends FieldEditor {
		
		private DummyFieldEditor(Composite parent) {
			super("Dummy", "", parent);
		}

		@Override
		protected void adjustForNumColumns(int numColumns) {
			// 未実装
			
		}

		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			// 未実装
			
		}

		@Override
		protected void doLoad() {
			// 未実装
			
		}

		@Override
		protected void doLoadDefault() {
			// 未実装
			
		}

		@Override
		protected void doStore() {
			// 未実装
			
		}

		@Override
		public int getNumberOfControls() {
			// レイアウトを調整するため、2を返却する
			return 2;
		}
		
	}
}
