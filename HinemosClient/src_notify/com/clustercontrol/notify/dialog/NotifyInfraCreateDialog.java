/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.notify.NotifyInfo;
import com.clustercontrol.ws.notify.NotifyInfraInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 通知（環境構築実行）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyInfraCreateDialog extends NotifyBasicCreateDialog {

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY 		= 2;

	/** カラム数（構築実行）。 */
	private static final int WIDTH_INFRA_RUN	 		= 2;

	/** カラム数（構築ID）。 */
	private static final int WIDTH_INFRA_ID	 		= 7;

	/** カラム数（呼出失敗時）。 */
	private static final int WIDTH_FAILURE_PRIORITY = 4;


	// ----- instance フィールド ----- //

	/** 通知タイプ
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private static final int TYPE_INFRA = 6;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** スコープ用テキスト */
	private Text m_textScope = null;

	/** ファシリティID */
	private String m_facilityId = null;

	/** スコープ */
	private String m_facilityPath = null;

	/** 変数用ラジオボタン */
	private Button m_radioGenerationNodeValue = null;

	/** 固定値用ラジオボタン */
	private Button m_radioFixedValue = null;

	/** スコープ参照用ボタン */
	private Button m_scopeSelect = null;

	/** 実行（通知） チェックボックス。 */
	private Button m_checkInfraRunInfo = null;
	/** 実行（警告） チェックボックス。 */
	private Button m_checkInfraRunWarning = null;
	/** 実行（異常） チェックボックス。 */
	private Button m_checkInfraRunCritical = null;
	/** 実行（不明） チェックボックス。 */
	private Button m_checkInfraRunUnknown = null;

	/** 構築ID（通知） コンボボックス。 */
	private Combo m_comboInfraIdInfo = null;
	/** 構築ID（警告） コンボボックス。 */
	private Combo m_comboInfraIdWarning = null;
	/** 構築ID（異常） コンボボックス。 */
	private Combo m_comboInfraIdCritical = null;
	/** 構築ID（不明） コンボボックス。 */
	private Combo m_comboInfraIdUnknown = null;

	/** 呼出失敗時の重要度（通知） コンボボックス。 */
	private Combo m_comboFailurePriorityInfo = null;
	/** 呼出失敗時の重要度（警告） コンボボックス。 */
	private Combo m_comboFailurePriorityWarning = null;
	/** 呼出失敗時の重要度（異常） コンボボックス。 */
	private Combo m_comboFailurePriorityCritical = null;
	/** 呼出失敗時の重要度（不明） コンボボックス。 */
	private Combo m_comboFailurePriorityUnknown = null;
	
	private List<InfraManagementInfo> m_infraList = new ArrayList<>();
	
	// 表示とIDを紐付けるマップ
	// key=CalendarId
	// value=CalendarName(CalendarId)
	private ConcurrentHashMap<String, String> dispMap = new ConcurrentHashMap<>(); 

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyInfraCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param notifyId 変更する通知情報の通知ID
	 * @param updateFlg 更新フラグ（true:更新する）
	 */
	public NotifyInfraCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfo)
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfo info = null;
		if(this.notifyId != null){
			info = new GetNotify().getNotify(this.managerName, this.notifyId);
		}
		else{
			info = new NotifyInfo();
		}
		this.setInputData(info);
	}

	/**
	 * 親のクラスから呼ばれ、各通知用のダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 */
	@Override
	protected void customizeSettingDialog(Composite parent) {
		final Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.infra.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * 環境構築
		 */
		// 構築グループ
		Group groupInfra = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "infra", groupInfra);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupInfra.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupInfra.setLayoutData(gridData);
		groupInfra.setText(Messages.getString("notifies.infra"));

		/*
		 * スコープグループ
		 */
		Group groupScope = new Group(groupInfra, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scope", groupScope);
		groupScope.setText(Messages.getString("notify.infra.scope"));
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupScope.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupScope.setLayoutData(gridData);

		// 変数 ラジオボタン
		this.m_radioGenerationNodeValue = new Button(groupScope, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "generationnodevalue", m_radioGenerationNodeValue);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioGenerationNodeValue.setLayoutData(gridData);
		this.m_radioGenerationNodeValue.setText(Messages.getString("notify.node.generation") + " : ");
		this.m_radioGenerationNodeValue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_radioFixedValue.setSelection(false);
					m_scopeSelect.setEnabled(false);
				}
			}
		});

		// 固定値 ラジオボタン
		this.m_radioFixedValue = new Button(groupScope, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "fixedvalue", m_radioFixedValue);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioFixedValue.setLayoutData(gridData);
		this.m_radioFixedValue.setText(Messages.getString("notify.node.fix") + " : ");
		this.m_radioFixedValue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_radioGenerationNodeValue.setSelection(false);
					m_scopeSelect.setEnabled(true);
				}
				update();
			}
		});

		this.m_textScope = new Text(groupScope, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "scope", m_textScope);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope.setLayoutData(gridData);
		this.m_textScope.setText("");

		this.m_scopeSelect = new Button(groupScope, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scopeselect", m_scopeSelect);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_scopeSelect.setLayoutData(gridData);
		this.m_scopeSelect.setText(Messages.getString("refer"));
		this.m_scopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell,
						m_notifyBasic.getManagerListComposite().getText(),
						m_notifyBasic.getRoleIdList().getText());
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem selectItem = dialog.getSelectItem();
					FacilityInfo info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityId = info.getFacilityId();
					m_textScope.setText(HinemosMessage.replace(m_facilityPath));
					update();
				}
			}
		});

		label = new Label(groupScope, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 空行
		label = new Label(groupInfra, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupInfra, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（実行する）
		label = new Label(groupInfra, SWT.NONE);
		WidgetTestUtil.setTestId(this, "run", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_INFRA_RUN;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（構築ID）
		label = new Label(groupInfra, SWT.NONE);
		WidgetTestUtil.setTestId(this, "infraid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_INFRA_ID;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("infra.management.id"));

		// ラベル（呼出失敗時）
		label = new Label(groupInfra, SWT.NONE);
		WidgetTestUtil.setTestId(this, "failurecallvalue", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_FAILURE_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("failure.call.value"));

		// 構築 重要度：情報
		label = this.getLabelPriority(groupInfra, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);
		this.m_checkInfraRunInfo = this.getCheckInfraRun(groupInfra);
		WidgetTestUtil.setTestId(this, "infraruninfo", m_checkInfraRunInfo);
		this.m_comboInfraIdInfo = this.getComboInfraId(groupInfra);
		WidgetTestUtil.setTestId(this, "infraidinfo", m_comboInfraIdInfo);
		this.m_comboFailurePriorityInfo = this.getComboPriority(groupInfra);
		WidgetTestUtil.setTestId(this, "failurepriority", m_comboFailurePriorityInfo);
		this.m_checkInfraRunInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkInfraRunInfo.getSelection(),
						m_comboInfraIdInfo,
						m_comboFailurePriorityInfo);
				update();
			}
		});
		this.m_comboInfraIdInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		setEnabled(false,
				m_comboInfraIdInfo,
				m_comboFailurePriorityInfo);
		
		// 構築 重要度：警告
		label = this.getLabelPriority(groupInfra, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);
		this.m_checkInfraRunWarning = this.getCheckInfraRun(groupInfra);
		WidgetTestUtil.setTestId(this, "infrarunwarning", m_checkInfraRunWarning);
		this.m_comboInfraIdWarning = this.getComboInfraId(groupInfra);
		WidgetTestUtil.setTestId(this, "infraidwarnitng", m_comboInfraIdWarning);
		this.m_comboFailurePriorityWarning = this.getComboPriority(groupInfra);
		WidgetTestUtil.setTestId(this, "failureprioritywarning", m_comboFailurePriorityWarning);
		this.m_checkInfraRunWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkInfraRunWarning.getSelection(),
						m_comboInfraIdWarning,
						m_comboFailurePriorityWarning);
				update();
			}
		});
		this.m_comboInfraIdWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		setEnabled(false,
				m_comboInfraIdWarning,
				m_comboFailurePriorityWarning);

		// 構築 重要度：危険
		label = this.getLabelPriority(groupInfra, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkInfraRunCritical = this.getCheckInfraRun(groupInfra);
		WidgetTestUtil.setTestId(this, "criticalcheck", m_checkInfraRunCritical);
		this.m_comboInfraIdCritical = this.getComboInfraId(groupInfra);
		WidgetTestUtil.setTestId(this, "infraidcritical", m_comboInfraIdCritical);
		this.m_comboFailurePriorityCritical = this.getComboPriority(groupInfra);
		WidgetTestUtil.setTestId(this, "failureprioritycritical", m_comboFailurePriorityCritical);
		this.m_checkInfraRunCritical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkInfraRunCritical.getSelection(),
						m_comboInfraIdCritical,
						m_comboFailurePriorityCritical);
				update();
			}
		});
		this.m_comboInfraIdCritical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		setEnabled(false,
				m_comboInfraIdCritical,
				m_comboFailurePriorityCritical);

		// 構築 重要度：不明
		label = this.getLabelPriority(groupInfra, Messages.getString("unknown"),PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkInfraRunUnknown = this.getCheckInfraRun(groupInfra);
		WidgetTestUtil.setTestId(this, "infrarununknown", m_checkInfraRunUnknown);
		this.m_comboInfraIdUnknown = this.getComboInfraId(groupInfra);
		WidgetTestUtil.setTestId(this, "infraidunknown", m_comboInfraIdUnknown);
		this.m_comboFailurePriorityUnknown = this.getComboPriority(groupInfra);
		WidgetTestUtil.setTestId(this, "failurepriorityunknown", m_comboFailurePriorityUnknown);
		this.m_checkInfraRunUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkInfraRunUnknown.getSelection(),
						m_comboInfraIdUnknown,
						m_comboFailurePriorityUnknown);
				update();
			}
		});
		this.m_comboInfraIdUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		setEnabled(false,
				m_comboInfraIdUnknown,
				m_comboFailurePriorityUnknown);
		
		if (!this.updateFlg) {
			// マネージャを変えたときのイベント
			getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerNames = getComboManagerName().getText();
					managerName = managerNames;
					refreshComboInfraId();
					update();
				}
			});
			
			// オーナーロールIDを変えたときのイベント
			getComboOwnerRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshComboInfraId();
					update();
				}
			});
		}
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示

		// 構築実行スコープ
		if(this.m_radioFixedValue.getSelection() && "".equals(this.m_textScope.getText())){
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 情報
		if(this.m_checkInfraRunInfo.getSelection() && "".equals(this.m_comboInfraIdInfo.getText())){
			this.m_comboInfraIdInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboInfraIdInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 警告
		if(this.m_checkInfraRunWarning.getSelection() && "".equals(this.m_comboInfraIdWarning.getText())){
			this.m_comboInfraIdWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboInfraIdWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 危険
		if(this.m_checkInfraRunCritical.getSelection() && "".equals(this.m_comboInfraIdCritical.getText())){
			this.m_comboInfraIdCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboInfraIdCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 不明
		if(this.m_checkInfraRunUnknown.getSelection() && "".equals(this.m_comboInfraIdUnknown.getText())){
			this.m_comboInfraIdUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboInfraIdUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	@Override
	public NotifyInfo getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfo notify) {
		super.setInputData(notify);
		
		// 構築IDのコンボボックスのデータを取得
		refreshComboInfraId();

		// コマンド情報
		NotifyInfraInfo info = notify.getNotifyInfraInfo();
		if (info != null) {
			this.setInputDatal(info);
		} else {
			// 新規追加の場合
			this.m_radioGenerationNodeValue.setSelection(true);
			this.m_scopeSelect.setEnabled(false);
		}
		update();
	}

	private void setInputDatal(NotifyInfraInfo infra) {
		if (infra.getInfraExecFacility() != null) {
			this.m_facilityId = infra.getInfraExecFacility();
			this.m_textScope.setText(HinemosMessage.replace(infra.getInfraExecScope()));
		}
		if (infra.getInfraExecFacilityFlg() != null && infra.getInfraExecFacilityFlg() == ExecFacilityConstant.TYPE_GENERATION) {
			this.m_radioGenerationNodeValue.setSelection(true);
			this.m_scopeSelect.setEnabled(false);
		}
		else {
			this.m_radioFixedValue.setSelection(true);
			this.m_scopeSelect.setEnabled(true);
		}

		Boolean[] validFlgs = getValidFlgs(infra);
		Button[] checkInfraRuns = new Button[] {
				this.m_checkInfraRunInfo,
				this.m_checkInfraRunWarning,
				this.m_checkInfraRunCritical,
				this.m_checkInfraRunUnknown
		};
		String[] infraIds = new String[] {
				infra.getInfoInfraId(),
				infra.getWarnInfraId(),
				infra.getCriticalInfraId(),
				infra.getUnknownInfraId()
		};
		Combo[] comboInfraIds = new Combo[] {
				this.m_comboInfraIdInfo,
				this.m_comboInfraIdWarning,
				this.m_comboInfraIdCritical,
				this.m_comboInfraIdUnknown
		};
		Integer[] infraFailurePriorities = new Integer[] {
				infra.getInfoInfraFailurePriority(),
				infra.getWarnInfraFailurePriority(),
				infra.getCriticalInfraFailurePriority(),
				infra.getUnknownInfraFailurePriority()
		};
		Combo[] comboFailurePriorities = new Combo[] {
				this.m_comboFailurePriorityInfo,
				this.m_comboFailurePriorityWarning,
				this.m_comboFailurePriorityCritical,
				this.m_comboFailurePriorityUnknown
		};


		for (int i = 0; i < validFlgs.length; i++) {
			boolean valid = validFlgs[i].booleanValue();
			checkInfraRuns[i].setSelection(valid);
			WidgetTestUtil.setTestId(this, "checkInfraRuns" + i, checkInfraRuns[i]);
			
			// 構築ID
			if (infraIds[i] != null) {
				setInfraId(comboInfraIds[i], infraIds[i]);
				WidgetTestUtil.setTestId(this, "textInfraIds" + i, comboInfraIds[i]);
			}

			// 構築失敗時の重要度
			if (infraFailurePriorities[i] != null) {
				comboFailurePriorities[i].setText(PriorityMessage.typeToString(infraFailurePriorities[i]));
				WidgetTestUtil.setTestId(this, "comboFailurePriorities" + i, comboFailurePriorities[i]);
			}
			
			setEnabled(valid, comboInfraIds[i], comboFailurePriorities[i]);
		}
	}

	/**
	 * 引数で指定された環境構築通知情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる環境構築通知情報
	 * @param checkInfraRun 通知チェックボックス
	 * @param textInfraId 構築IDテキストボックス
	 * @param checkInhibition 抑制チェックボックス
	 * @param comboFailurePriority 呼出失敗時の重要度
	 */
	protected void setInputDataForInfra(NotifyInfraInfo info,
			Button checkInfraRun,
			Text textInfraunitId,
			Text textInfraId,
			Combo comboFailurePriority
			) {
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForInfra(ArrayList, int, Button, Text, Button, Combo)
	 */
	@Override
	protected NotifyInfo createInputData() {
		NotifyInfo info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_INFRA);

		// コマンド情報
		NotifyInfraInfo notifyInfraInfo = createNotifyInfoDetail();
		info.setNotifyInfraInfo(notifyInfraInfo);
		return info;
	}

	private NotifyInfraInfo createNotifyInfoDetail() {
		// 環境構築情報
		NotifyInfraInfo infra = new NotifyInfraInfo();

		//　実行
		infra.setInfoValidFlg(m_checkInfraRunInfo.getSelection());
		infra.setWarnValidFlg(m_checkInfraRunWarning.getSelection());
		infra.setCriticalValidFlg(m_checkInfraRunCritical.getSelection());
		infra.setUnknownValidFlg(m_checkInfraRunUnknown.getSelection());


		// infraId
		if (isNotNullAndBlank(m_comboInfraIdInfo.getText())) {
			infra.setInfoInfraId(getInfraId(m_comboInfraIdInfo));
		}
		if (isNotNullAndBlank(m_comboInfraIdWarning.getText())) {
			infra.setWarnInfraId(getInfraId(m_comboInfraIdWarning));
		}
		if (isNotNullAndBlank(m_comboInfraIdCritical.getText())) {
			infra.setCriticalInfraId(getInfraId(m_comboInfraIdCritical));
		}
		if (isNotNullAndBlank(m_comboInfraIdUnknown.getText())) {
			infra.setUnknownInfraId(getInfraId(m_comboInfraIdUnknown));
		}

		// 呼出失敗時
		if (isNotNullAndBlank(m_comboFailurePriorityInfo.getText())) {
			infra.setInfoInfraFailurePriority(PriorityMessage.stringToType(m_comboFailurePriorityInfo.getText()));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityWarning.getText())) {
			infra.setWarnInfraFailurePriority(PriorityMessage.stringToType(m_comboFailurePriorityWarning.getText()));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityCritical.getText())) {
			infra.setCriticalInfraFailurePriority(PriorityMessage.stringToType(m_comboFailurePriorityCritical.getText()));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityUnknown.getText())) {
			infra.setUnknownInfraFailurePriority(PriorityMessage.stringToType(m_comboFailurePriorityUnknown.getText()));
		}

		// 共通部分登録
		// 実行ファシリティID
		if (isNotNullAndBlank(this.m_textScope.getText())) {
			infra.setInfraExecFacility(this.m_facilityId);
			infra.setInfraExecScope(this.m_textScope.getText());
		}
		// 実行ファシリティ
		if (this.m_radioGenerationNodeValue.getSelection()) {
			infra.setInfraExecFacilityFlg(ExecFacilityConstant.TYPE_GENERATION);
		}
		else if (this.m_radioFixedValue.getSelection()){
			infra.setInfraExecFacilityFlg(ExecFacilityConstant.TYPE_FIX);
		}

		return infra;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.inputData = this.createInputData();

		return super.validate();
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

		NotifyInfo info = this.getInputData();
		if(info != null){
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().add(this.getInputManagerName(), info);
			}
			else{
				// 変更の場合
				result = new ModifyNotify().modify(this.getInputManagerName(), info);
			}
		}

		return result;
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
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	@Override
	protected void setValidateResult(String id, String message) {

		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);
	}

	/**
	 * ボタンを生成します。<BR>
	 * 参照フラグが<code> true </code>の場合は閉じるボタンを生成し、<code> false </code>の場合は、デフォルトのボタンを生成します。
	 *
	 * @param parent ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if(!this.referenceFlg){
			super.createButtonsForButtonBar(parent);
		}
		else{
			// 閉じるボタン
			// TODO Remove the following hard-code. IDialogConstants.*_LABEL will causes IncompatibleClassChangeError on RAP
			this.createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
		}
	}

	/**
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable 選択可の場合、<code> true </code>
	 */
	@Override
	protected void setEnabled(boolean enable) {
		super.m_notifyBasic.setEnabled(enable);
		super.m_notifyInhibition.setEnabled(enable);
	}
	
	private void setEnabled(boolean enable,
			Combo comboInfraId,
			Combo comboFailurePriority) {
		comboInfraId.setEnabled(enable);
		comboFailurePriority.setEnabled(enable);
	}

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param text ラベルに表示するテキスト
	 * @param background ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label getLabelPriority(Composite parent,
			String text,
			Color background
			) {

		// ラベル（重要度）
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labelpriority", label);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(text + " : ");
		label.setBackground(background);

		return label;
	}

	/**
	 * 構築の実行チェックボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckInfraRun(Composite parent) {

		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, null, button);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_INFRA_RUN;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		return button;
	}

	private String putMap(String infraId, String infraName) {
		String disp = null;
		if (infraId.length() == 0) {
			disp = "";
		} else {
			int maxLength = 32;
			if (maxLength < infraName.length()) {
				infraName = infraName.substring(0, maxLength) + "...";
			}
			disp = infraName + "(" + infraId + ")";
		}
		dispMap.put(infraId, disp);
		m_comboInfraIdInfo.add(disp);
		m_comboInfraIdWarning.add(disp);
		m_comboInfraIdCritical.add(disp);
		m_comboInfraIdUnknown.add(disp);
		return disp;
	}
	
	/** 
	 * 構築IDコンボボックスの表示内容をリセットします
	 * 
	 * ダイアログを開いたときと、オーナーロールIDが変更されたときに呼ばれます
	 * @param comboInfraId
	 */
	private void refreshComboInfraId() {
		dispMap.clear();
		m_comboInfraIdInfo.removeAll();
		m_comboInfraIdWarning.removeAll();
		m_comboInfraIdCritical.removeAll();
		m_comboInfraIdUnknown.removeAll();

		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
			m_infraList = wrapper.getInfraManagementListByOwnerRole(getOwnerRoleId());
		} catch (Exception e) {
			MessageDialog.openError(getShell(), MessageConstant.MESSAGE.getMessage(), e.getMessage());
			return;
		}
		
		putMap("","");
		
		for (InfraManagementInfo infra : m_infraList) {
			putMap(infra.getManagementId(), infra.getName());
		}
	}
	
	private String getInfraId(Combo comboInfraId) {
		String selectedText = comboInfraId.getText();
		if (selectedText == null || selectedText.isEmpty()) {
			return "";
		}
		for (Map.Entry<String, String> tmpId : dispMap.entrySet()) {
			if (tmpId.getValue().equals(selectedText)) {
				return tmpId.getKey();
			}
		}
		return null;
	}
	
	private void setInfraId(Combo comboInfraId, String infraManagementId) {
		if (infraManagementId == null) {
			return;
		}
		// 権限によっては、存在しないカレンダを選択(setText)する場合があるので、
		// ここで追加する。
		if (!dispMap.keySet().contains(infraManagementId)) {
			// カレンダの参照権限がない場合のみ、ここに到達する。
			// 参照権限がないので、IDしかわからない。
			putMap(infraManagementId, "");
		}

		// 選択する
		comboInfraId.setText(dispMap.get(infraManagementId));
	}


	/**
	 * 構築IDコンボボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Combo getComboInfraId(Composite parent) {
		// テキストボックス（構築ID）
		Combo notifyInfraCreateInfraIdCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "infra", notifyInfraCreateInfraIdCombo);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_INFRA_ID;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		notifyInfraCreateInfraIdCombo.setLayoutData(gridData);

		return notifyInfraCreateInfraIdCombo;
	}

	/**
	 * 構築の重要度のコンボボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param horizontalSpan コンボボックスのカラム数
	 * @return コンボボックス
	 */
	private Combo getComboPriority(Composite parent) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, combo);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_FAILURE_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		combo.setLayoutData(gridData);
		combo.add(PriorityMessage.STRING_CRITICAL);
		combo.add(PriorityMessage.STRING_WARNING);
		combo.add(PriorityMessage.STRING_INFO);
		combo.add(PriorityMessage.STRING_UNKNOWN);
		combo.setText(PriorityMessage.STRING_UNKNOWN);

		return combo;
	}

	@Override
	public void setOwnerRoleId(String ownerRoleId) {
		super.setOwnerRoleId(ownerRoleId);
		this.m_facilityPath = "";
		this.m_facilityId = "";
		this.m_textScope.setText(HinemosMessage.replace(m_facilityPath));

		refreshComboInfraId();
	}

}
