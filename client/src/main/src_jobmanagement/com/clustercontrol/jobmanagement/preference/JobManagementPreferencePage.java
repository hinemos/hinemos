/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.preference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
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
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.AutoUpdateView;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ管理機能の設定ページクラスです。
 *
 * @version 2.1.1
 * @since 1.0.0
 */
public class JobManagementPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	private static Log m_log = LogFactory.getLog(JobManagementPreferencePage.class);

	/** ジョブ[履歴]ビューの自動更新フラグ */
	public static final String P_HISTORY_UPDATE_FLG = "historyUpdateFlg";

	/** ジョブ[履歴]ビューの自動更新周期 */
	public static final String P_HISTORY_UPDATE_CYCLE = "historyUpdateCycle";

	/** ジョブ[履歴]ビューのメッセージ表示 */
	public static final String P_HISTORY_MESSAGE_FLG = "historyMessageFlg";

	/** ジョブ[履歴]ビューの表示履歴数 */
	public static final String P_HISTORY_MAX_HISTORIES = "historyMaxHistories";

	/** ジョブ[スケジュール予定]ビューの表示数*/
	public static final String P_PLAN_MAX_SCHEDULE = "planMaxSchedule";

	public JobManagementPreferencePage() {
		super(GRID);
	}

	/**
	 * 初期値が設定されたインスタンスを返します。
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
	 * @see #initializeDefaults()
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * 設定フィールドを生成します。
	 *
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#addField(org.eclipse.jface.preference.FieldEditor)
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;
		// ジョブ[履歴]ビュー関連
		Group group = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, null, group);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		group.setLayoutData(gridData);
		group.setText(Messages.getString("view.job.history"));

		// フラグ
		this.addField(new BooleanFieldEditor(P_HISTORY_UPDATE_FLG,
				Messages.getString("autoupdate.enable"), group));

		// 周期
		IntegerFieldEditor cycle =
				new IntegerFieldEditor(
						P_HISTORY_UPDATE_CYCLE,
						Messages.getString("autoupdate.cycle") + " : ", group);
		cycle.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] args = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		cycle.setErrorMessage(Messages.getString("message.hinemos.8", args ));
		this.addField(cycle);

		// メッセージ表示
		this.addField(new BooleanFieldEditor(P_HISTORY_MESSAGE_FLG,
				Messages.getString("over.limit.message"), group));

		// 表示履歴数
		IntegerFieldEditor histories =
				new IntegerFieldEditor(
						P_HISTORY_MAX_HISTORIES,
						Messages.getString("number.of.display.histories") + " : ", group);
		histories.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		histories.setErrorMessage(Messages.getString("message.hinemos.8", args ));
		this.addField(histories);

		// ジョブ[スケジュール予定]ビュー関連
		Group planGroup = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, "plan", planGroup);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;

		planGroup.setLayoutData(gridData);
		planGroup.setText(Messages.getString("view.job.plan"));

		String[] argsPlan = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };

		// 表示件数
		IntegerFieldEditor plan =
				new IntegerFieldEditor(
						P_PLAN_MAX_SCHEDULE,
						Messages.getString("number.of.display.list") + " :            ", planGroup);// FIXME レイアウトを整えるための空白
		plan.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		plan.setErrorMessage(Messages.getString("message.hinemos.8", argsPlan ));
		this.addField(plan);
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
	 * 設定内容をジョブ[履歴]ビューに反映します。
	 */
	private void applySetting() {
		IPreferenceStore store = this.getPreferenceStore();

		// 存在するビュー全てに設定を適応する。

		IWorkbench workbench = ClusterControlPlugin.getDefault()
				.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();

		int windowCount = windows.length;

		for (int i = 0; i < windowCount; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			int pageCount = pages.length;

			for (int j = 0; j < pageCount; j++) {
				int cycle = store.getInt(P_HISTORY_UPDATE_CYCLE);
				boolean flag = store.getBoolean(P_HISTORY_UPDATE_FLG);

				// 本体のジョブ[履歴]ビュー
				setUpdateSetting(
						(AutoUpdateView) pages[j].findView(JobHistoryView.ID),
						cycle, flag);
				// ジョブマップのジョブ[履歴]ビュー
				setUpdateSetting(
						(AutoUpdateView) pages[j].findView("com.clustercontrol.jobmap.view.JobHistoryViewM"),
						cycle, flag);
				// ジョブマップのジョブマップ[履歴]ビュー
				setUpdateSetting(
						(AutoUpdateView) pages[j].findView("com.clustercontrol.jobmap.view.JobMapHistoryView"),
						cycle, flag);
			}
		}
	}

	private void setUpdateSetting(AutoUpdateView view, int cycle, boolean flag) {
		if (view != null) {
			m_log.info("setUpdateSetting " + view.getTitle() + ", cycle=" + cycle + ", flag=" + flag);
			view.setInterval(cycle);
			if (flag) {
				view.startAutoReload();
			} else {
				view.stopAutoReload();
			}
		}
	}
}
