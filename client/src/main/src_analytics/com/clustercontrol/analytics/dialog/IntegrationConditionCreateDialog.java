/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.dialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.analytics.bean.IntegrationComparisonMethod;
import com.clustercontrol.analytics.util.AnalyticsUtil;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.collect.CollectKeyInfo;
import com.clustercontrol.ws.monitor.IntegrationConditionInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 判定条件[作成・変更]ダイアログクラス<BR>
 *
 * @version 6.1.0
 */
public class IntegrationConditionCreateDialog extends CommonDialog {

	/** 入力値を保持するオブジェクト。 */
	private IntegrationConditionInfo m_inputData = null;

	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult m_validateResult = null;

	/** 説明 */
	private Text m_textDescription = null;

	/** 対象ノード_監視スコープ（ラジオボタン） */
	private Button m_radioMonitorNode = null;

	/** 対象ノード_値保持用 */
	private boolean m_isRadioMonitorNode = true;

	/** 対象ノード_監視スコープ_ファシリティID */
	private String m_monitorFacilityId = null;

	/** 対象ノード_設定（ラジオボタン） */
	private Button m_radioSettingNode = null;

	/** 対象ノード_設定_ファシリティID */
	private String m_settingFacilityId = null;

	/** 対象ノード_設定_ノード名(テキスト) */
	private Text m_textSettingNode = null;

	/** 対象ノード_設定_参照ボタン(ボタン) */
	private Button m_buttonSettingNode = null;

	/** 対象ノード */
	private String m_targetFacilityId = null;

	/** 収集値種別_数値（ラジオボタン） */
	private Button m_radioNumeric = null;

	/** 収集値種別_値保持用 */
	private boolean m_isRadioNumeric = true;

	/** 収集値種別_文字列（ラジオボタン） */
	private Button m_radioString = null;

	/** 収集値表示名(コンボボックス) */
	private Combo m_comboItemDisplayName = null;

	/** 対象収集値表示名マップ_数値（表示名, キー) */
	private Map<String, CollectKeyInfo> m_itemDisplayNameNumericMap = new HashMap<>();

	/** 対象収集値表示名マップ_文字列（表示名, 監視設定情報) */
	private Map<String, MonitorInfo> m_itemDisplayNameStringMap = new HashMap<>();

	/** 比較方法(コンボボックス) */
	private Combo m_comboComparisonMethod = null;

	/** 比較値(テキスト) */
	private Text m_textComparisonValue = null;

	/** 文字列条件_AND（ラジオボタン） */
	private Button m_radioAnd = null;

	/** 文字列条件_OR（ラジオボタン） */
	private Button m_radioOr = null;

	/** マネージャ名 */
	private String m_managerName = null;

	/** オーナーロールID */
	private String m_ownerRoleId = null;

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 * @param info 判定条件情報
	 */
	public IntegrationConditionCreateDialog(Shell parent, String managerName, 
			String ownerRoleId, String monitorFacilityId, IntegrationConditionInfo info) {
		super(parent);
		this.m_managerName = managerName;
		this.m_ownerRoleId = ownerRoleId;
		this.m_monitorFacilityId = monitorFacilityId;
		this.m_inputData = info;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.run.create.modify.condition"));

		// 変数として利用されるラベル
		Label label = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		// 説明（ラベル）
		label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
		label.setText(Messages.getString("description") + " : ");
		// 説明（テキスト）
		this.m_textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "description", m_textDescription);
		this.m_textDescription.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));
		this.m_textDescription.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 条件グループ
		Group monitorRuleGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitorrule", monitorRuleGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		monitorRuleGroup.setLayout(layout);
		monitorRuleGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 15, 1));
		monitorRuleGroup.setText(Messages.getString("monitor.rule"));

		// 対象ノード（ラベル）
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "target.node", label);
		label.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1));
		label.setText(Messages.getString("target.node") + " : ");

		// 対象ノード（Composite）
		Composite compTargetNode = new Composite(monitorRuleGroup, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.numColumns = 12;
		compTargetNode.setLayout(layout);
		compTargetNode.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));

		// 対象ノード_監視ノード（ラジオ）
		this.m_radioMonitorNode = new Button(compTargetNode, SWT.RADIO);
		this.m_radioMonitorNode.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));
		this.m_radioMonitorNode.setText(Messages.getString("monitor.integration.monitor.scope"));
		this.m_radioMonitorNode.setSelection(true);
		this.m_radioMonitorNode.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_isRadioMonitorNode == m_radioMonitorNode.getSelection()) {
					return;
				}
				changeNode();
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (m_isRadioMonitorNode == m_radioMonitorNode.getSelection()) {
					return;
				}
				changeNode();
				update();
			}
		});

		// 対象ノード_設定ノード（ラジオ）
		this.m_radioSettingNode = new Button(compTargetNode, SWT.RADIO);
		this.m_radioSettingNode.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_radioSettingNode.setSelection(true);

		// 対象ノード（テキスト）
		this.m_textSettingNode = new Text(compTargetNode, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_textSettingNode);
		this.m_textSettingNode.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 9, 1));
		this.m_textSettingNode.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 対象ノード用参照ボタン（ボタン）
		this.m_buttonSettingNode = new Button(compTargetNode, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_buttonSettingNode);
		m_buttonSettingNode.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		m_buttonSettingNode.setText(Messages.getString("refer"));
		m_buttonSettingNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, m_managerName, m_ownerRoleId, false, false);
				dialog.setSelectNodeOnly(true);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem item = dialog.getSelectItem();
					FacilityInfo info = item.getData();
					m_textSettingNode.setText(info.getFacilityName());
					m_settingFacilityId = info.getFacilityId();
					m_targetFacilityId = m_settingFacilityId;
					// 参照収集値表示名コンボを切り替える
					if (m_radioNumeric.getSelection()) {
						// 数値監視
						AnalyticsUtil.setComboItemNameForNumeric(m_comboItemDisplayName, m_itemDisplayNameNumericMap, 
							m_settingFacilityId, m_managerName, m_ownerRoleId);
					} else {
						// 文字列監視
						AnalyticsUtil.setComboItemNameForString(m_comboItemDisplayName, m_itemDisplayNameStringMap, 
								m_settingFacilityId, m_managerName, m_ownerRoleId);
					}
					update();
				}
			}
		});

		// 収集値種別（ラベル）
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "target.node", label);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
		label.setText(Messages.getString("collection.type") + " : ");

		// 収集値種別（Composite）
		Composite compCollectionType = new Composite(monitorRuleGroup, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.numColumns = 12;
		compCollectionType.setLayout(layout);
		compCollectionType.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));

		// 収集値種別_数値（ラジオ）
		this.m_radioNumeric = new Button(compCollectionType, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "numeric", m_radioNumeric);
		this.m_radioNumeric.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 6, 1));
		this.m_radioNumeric.setText(Messages.getString("numeric"));
		this.m_radioNumeric.setSelection(true);
		this.m_radioNumeric.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_isRadioNumeric == m_radioNumeric.getSelection()) {
					return;
				}
				changeMonitorType();
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (m_isRadioNumeric == m_radioNumeric.getSelection()) {
					return;
				}
				changeMonitorType();
				update();
			}
		});
		
		// 収集値種別_文字列（ラジオ）
		this.m_radioString = new Button(compCollectionType, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "string", m_radioString);
		this.m_radioString.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 6, 1));
		this.m_radioString.setText(Messages.getString("string"));

		// 収集値表示名（ラベル）
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collection.display.name", label);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
		label.setText(Messages.getString("collection.display.name") + " : ");

		// 収集値表示名（コンボボックス）
		this.m_comboItemDisplayName = new Combo(monitorRuleGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "collection.display.name", this.m_comboItemDisplayName);
		this.m_comboItemDisplayName.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));
		this.m_comboItemDisplayName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// 比較方法（ラベル）
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "comparison.method", label);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
		label.setText(Messages.getString("comparison.method") + " : ");

		// 比較方法（コンボボックス）
		this.m_comboComparisonMethod = new Combo(monitorRuleGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "comparison.method", this.m_comboComparisonMethod);
		this.m_comboComparisonMethod.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));
		this.m_comboComparisonMethod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// ラベル（比較値）
		label = new Label(monitorRuleGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "comparison.method", label);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
		label.setText(Messages.getString("comparison.value") + " : ");

		// テキスト（比較値）
		this.m_textComparisonValue = new Text(monitorRuleGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "comparison.value", m_textComparisonValue);
		this.m_textComparisonValue.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));
		this.m_textComparisonValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//dummy
		label = new Label(monitorRuleGroup, SWT.LEFT);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));

		// 文字列条件（Composite）
		Composite compAndOr = new Composite(monitorRuleGroup, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.numColumns = 12;
		compAndOr.setLayout(layout);
		compAndOr.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 12, 1));

		// 文字列条件_AND（ラジオ）
		this.m_radioAnd = new Button(compAndOr, SWT.RADIO);
		this.m_radioAnd.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 6, 1));
		this.m_radioAnd.setText(Messages.getString("and"));
		this.m_radioAnd.setSelection(true);
		
		// 文字列条件_AND（ラジオ）
		this.m_radioOr = new Button(compAndOr, SWT.RADIO);
		this.m_radioOr.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 6, 1));
		this.m_radioOr.setText(Messages.getString("or"));

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		update();
		setInputData();
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を可視化
		// 対象ノード
		if("".equals(this.m_textSettingNode.getText()) && (this.m_radioSettingNode.getSelection())){
			this.m_textSettingNode.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textSettingNode.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 収集値表示名
		if("".equals(this.m_comboItemDisplayName.getText())) {
			this.m_comboItemDisplayName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboItemDisplayName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 判定
		if("".equals(this.m_comboComparisonMethod.getText())){
			this.m_comboComparisonMethod.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboComparisonMethod.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 判定値
		if("".equals(this.m_textComparisonValue.getText())){
			this.m_textComparisonValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textComparisonValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 入力値を保持した文字列監視の判定情報を返します。
	 *
	 * @return 判定情報
	 */
	public IntegrationConditionInfo getInputData() {
		return this.m_inputData;
	}

	/**
	 * 引数で指定された判定情報の値を、各項目に設定します。
	 */
	protected void setInputData() {
		// 説明
		if (m_inputData.getDescription() != null) {
			this.m_textDescription.setText(m_inputData.getDescription());
		}

		// 対象ノード
		if (m_inputData.isMonitorNode() == null) {
			m_inputData.setMonitorNode(true);
		}
		this.m_radioMonitorNode.setSelection(m_inputData.isMonitorNode());
		this.m_radioSettingNode.setSelection(!m_inputData.isMonitorNode());
		this.m_textSettingNode.setEnabled(this.m_radioSettingNode.getSelection());
		this.m_buttonSettingNode.setEnabled(this.m_radioSettingNode.getSelection());

		if (m_inputData.isMonitorNode()) {
			this.m_targetFacilityId = m_monitorFacilityId;
		} else {
			if (m_inputData.getTargetFacilityId() != null) {
				try {
					RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(this.m_managerName);
					this.m_textSettingNode.setText(wrapper.getFacilityPath(m_inputData.getTargetFacilityId(), null));
				} catch (Exception e) {
					// エラー時は何もしない
				}
				if (this.m_textSettingNode.getText() != null && !this.m_textSettingNode.getText().isEmpty()) {
					this.m_settingFacilityId = m_inputData.getTargetFacilityId();
					this.m_targetFacilityId = this.m_settingFacilityId;
				}
			}
		}
		// 収集値種別
		if (m_inputData.getTargetMonitorType() == null || 
				m_inputData.getTargetMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
			this.m_radioNumeric.setSelection(true);
			this.m_radioString.setSelection(false);
		} else if (m_inputData.getTargetMonitorType() == MonitorTypeConstant.TYPE_STRING) {
			this.m_radioNumeric.setSelection(false);
			this.m_radioString.setSelection(true);
		}
		// 文字列条件
		if (m_inputData.isIsAnd() == null) {
			m_inputData.setIsAnd(true);
		}
		this.m_radioAnd.setSelection(m_inputData.isIsAnd());
		this.m_radioOr.setSelection(!m_inputData.isIsAnd());

		// 収集値種別変更に伴う切り替え
		changeMonitorType();

		// 収集値表示名
		if (m_inputData.getTargetMonitorType() == null || 
				m_inputData.getTargetMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
			this.m_comboItemDisplayName.setText(AnalyticsUtil.getComboItemNameForNumeric(
					m_itemDisplayNameNumericMap, m_inputData.getTargetMonitorId(), 
					m_inputData.getTargetDisplayName(), m_inputData.getTargetItemName()));
		} else if (m_inputData.getTargetMonitorType() == MonitorTypeConstant.TYPE_STRING) {
			this.m_comboItemDisplayName.setText(AnalyticsUtil.getComboItemNameForString(
					m_itemDisplayNameStringMap, m_inputData.getTargetMonitorId()));
		}

		// 比較方法
		if (m_inputData.getComparisonMethod() != null) {
			this.m_comboComparisonMethod.setText(m_inputData.getComparisonMethod());
		}
		// 比較値
		if (m_inputData.getComparisonValue() != null) {
			this.m_textComparisonValue.setText(m_inputData.getComparisonValue());
		}

		// 必須項目を可視化
		this.update();

	}

	/**
	 * 引数で指定された判定情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 判定情報
	 *
	 * @see #setValidateResult(String, String)
	 */
	private IntegrationConditionInfo createInputData() {
		IntegrationConditionInfo info = new IntegrationConditionInfo();

		// 説明
		if (this.m_textDescription.getText() != null
				&& !"".equals((this.m_textDescription.getText()).trim())) {
			info.setDescription(this.m_textDescription.getText());
		}

		// 対象ノード
		info.setMonitorNode(this.m_radioMonitorNode.getSelection());
		if (this.m_targetFacilityId == null || this.m_targetFacilityId.isEmpty()) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", new String[]{Messages.getString("target.node")}));
			return null;
		} else {
			info.setTargetFacilityId(this.m_targetFacilityId);
		}

		// 収集値種別による設定
		if (this.m_radioNumeric.getSelection()) {
			// 収集値種別
			info.setTargetMonitorType(MonitorTypeConstant.TYPE_NUMERIC);
			// 収集項目名
			if (this.m_comboItemDisplayName.getText() != null) {
				CollectKeyInfo collectKeyInfo 
					= m_itemDisplayNameNumericMap.get(this.m_comboItemDisplayName.getText());
				if (collectKeyInfo == null) {
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.1", new String[]{Messages.getString("collection.display.name")}));
					return null;
				} else {
					info.setTargetMonitorId(collectKeyInfo.getMonitorId());
					info.setTargetDisplayName(collectKeyInfo.getDisplayName());
					info.setTargetItemName(collectKeyInfo.getItemName());
				}
			}
		} else if (this.m_radioString.getSelection()) {
			// 収集値種別
			info.setTargetMonitorType(MonitorTypeConstant.TYPE_STRING);
			// 収集項目名
			if (this.m_comboItemDisplayName.getText() != null) {
				MonitorInfo monitorInfo
					= m_itemDisplayNameStringMap.get(this.m_comboItemDisplayName.getText());
				if (monitorInfo == null) {
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.1", new String[]{Messages.getString("collection.display.name")}));
					return null;
				} else {
					info.setTargetMonitorId(monitorInfo.getMonitorId());
					info.setTargetDisplayName("");
					info.setTargetItemName("");
				}
			}
		}
		info.setTargetItemDisplayName(this.m_comboItemDisplayName.getText());

		// 比較方法
	    if (this.m_comboComparisonMethod.getText() == null 
	    		|| this.m_comboComparisonMethod.getText().isEmpty()) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", new String[]{Messages.getString("comparison.method")}));
			return null;
	    } else {
	    	info.setComparisonMethod(this.m_comboComparisonMethod.getText());
	    }
		// 比較値
	    if (this.m_textComparisonValue.getText() == null 
	    		|| this.m_textComparisonValue.getText().isEmpty()) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", new String[]{Messages.getString("comparison.value")}));
			return null;
	    } else {
	    	info.setComparisonValue(this.m_textComparisonValue.getText());
	    }

		// 文字列条件
	    info.setIsAnd(this.m_radioAnd.getSelection());

		return info;
	}

	/**
	 * 無効な入力値をチェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see #createInputData()
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.m_inputData = this.createInputData();

		if (this.m_inputData != null) {
			return super.validate();
		} else {
			return m_validateResult;
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
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	protected void setValidateResult(String id, String message) {

		this.m_validateResult = new ValidateResult();
		this.m_validateResult.setValid(false);
		this.m_validateResult.setID(id);
		this.m_validateResult.setMessage(message);
	}

	/**
	 * 入力値の判定を行います。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		IntegrationConditionInfo info = this.m_inputData;
		if(info != null){
			result = true;
		}

		return result;
	}

	/**
	 * 監視種別変更に伴う切り替え
	 */
	private void changeMonitorType() {
		this.m_comboComparisonMethod.removeAll();
		if (this.m_radioNumeric.getSelection()) {
			// 収集値表示名変更
			AnalyticsUtil.setComboItemNameForNumeric(m_comboItemDisplayName, m_itemDisplayNameNumericMap,
					m_targetFacilityId, m_managerName, m_ownerRoleId);
			// 比較方法変更
			for (IntegrationComparisonMethod method : IntegrationComparisonMethod.values()) {
				this.m_comboComparisonMethod.add(method.symbol());
			}
		} else {
			// 収集値表示名変更
			AnalyticsUtil.setComboItemNameForString(m_comboItemDisplayName, m_itemDisplayNameStringMap,
					m_targetFacilityId, m_managerName, m_ownerRoleId);
			// 比較方法変更
			m_comboComparisonMethod.add(IntegrationComparisonMethod.EQ.symbol());
			m_comboComparisonMethod.setText(IntegrationComparisonMethod.EQ.symbol());
		}
		// 文字列条件
		this.m_radioAnd.setEnabled(this.m_radioString.getSelection());
		this.m_radioOr.setEnabled(this.m_radioString.getSelection());
		// フラグ変更
		m_isRadioNumeric = this.m_radioNumeric.getSelection();
	}

	/**
	 * 対象ノードラジオボタン変更に伴う切り替え
	 */
	private void changeNode() {
		if (this.m_radioMonitorNode.getSelection()) {
			m_targetFacilityId = this.m_monitorFacilityId;
		} else {
			m_targetFacilityId = this.m_settingFacilityId;
		}
		m_textSettingNode.setEnabled(this.m_radioSettingNode.getSelection());
		m_buttonSettingNode.setEnabled(this.m_radioSettingNode.getSelection());
		this.m_comboComparisonMethod.removeAll();
		if (this.m_radioNumeric.getSelection()) {
			// 収集値表示名変更
			AnalyticsUtil.setComboItemNameForNumeric(m_comboItemDisplayName, m_itemDisplayNameNumericMap,
					m_targetFacilityId, m_managerName, m_ownerRoleId);
			// 比較方法変更
			for (IntegrationComparisonMethod method : IntegrationComparisonMethod.values()) {
				this.m_comboComparisonMethod.add(method.symbol());
			}
		} else {
			// 収集値表示名変更
			AnalyticsUtil.setComboItemNameForString(m_comboItemDisplayName, m_itemDisplayNameStringMap,
					m_targetFacilityId, m_managerName, m_ownerRoleId);
			// 比較方法変更
			m_comboComparisonMethod.add(IntegrationComparisonMethod.EQ.symbol());
		}
		// フラグ変更
		m_isRadioMonitorNode = this.m_radioMonitorNode.getSelection();
	}
}
