/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * スコープ付き監視基本情報コンポジットクラス<BR>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「監視項目ID」 テキストボックス（親）</dd>
 *  <dd>「説明」 テキストボックス（親）</dd>
 *  <dd>「スコープ」 テキストボックス</dd>
 *  <dd>「参照」 ボタン</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class MonitorBasicScopeComposite extends MonitorBasicComposite {

	/** スコープ テキストボックス。 */
	private Text m_textScope = null;

	/** 参照 ボタン。 */
	private Button m_buttonScope = null;

	/** 選択されたスコープのファシリティID。 */
	private String m_facilityId = null;

	/** 未登録ノード スコープを表示するかフラグ*/
	private boolean m_unregistered = false;

	/** オーナーロールID*/
	private String m_ownerRoleId = null;

	/** スコープラベル */
	private Label m_labelScope = null;


	/**
	 * インスタンスを返します。
	 * <p>
	 * 親クラスのコンストラクタを呼び出し、監視基本情報のコンポジットを配置します。
	 * 初期処理を呼び出し、スコープのコンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see com.clustercontrol.monitor.run.composite.MonitorBasicComposite#MonitorBasicComposite(Composite, int)
	 * @see #initialize()
	 */
	public MonitorBasicScopeComposite(Composite parent, int style ,boolean unregistered, CommonMonitorDialog parentDialog) {
		super(parent, style, parentDialog);
		this.m_unregistered = unregistered;
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * スコープ
		 */
		// ラベル
		m_labelScope = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scope", m_labelScope);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_labelScope.setLayoutData(gridData);
		m_labelScope.setText(Messages.getString("scope") + " : ");
		// テキスト
		this.m_textScope = new Text(this, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_textScope);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope.setLayoutData(gridData);
		this.m_textScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 参照ボタン
		m_buttonScope = new Button(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_buttonScope);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_buttonScope.setLayoutData(gridData);
		m_buttonScope.setText(Messages.getString("refer"));
		m_buttonScope.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				String managerName = getManagerListComposite().getText();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, m_ownerRoleId, false, m_unregistered);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem item = dialog.getSelectItem();
					FacilityInfo info = item.getData();
					m_facilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityConstant.TYPE_NODE) {
						m_textScope.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(
								ClusterControlPlugin.getDefault()
								.getSeparator());
						m_textScope.setText(path.getPath(item));
					}
				}
			}
		});

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		super.update();
		// スコープが必須項目であることを明示
		if("".equals(this.m_textScope.getText())){
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 * @param updateFlg 更新するか否か（true:更新する）
	 *
	 * @see com.clustercontrol.monitor.run.composite.MonitorBasicComposite#setInputData(MonitorInfo)
	 */
	@Override
	public void setInputData(MonitorInfo info, boolean updateFlg) {

		super.setInputData(info, updateFlg);

		if(info != null){
			if (info.getScope() != null) {
				this.m_textScope.setText(HinemosMessage.replace(info.getScope()));
			}
			if (info.getFacilityId() != null) {
				this.m_facilityId = info.getFacilityId();
			}
		}
		// スコープが必須項目であることを明示
		this.update();
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info 入力値を設定する監視情報
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.monitor.run.composite.MonitorBasicComposite#createInputData(MonitorInfo)
	 * @see #setValidateResult(String, String)
	 */
	@Override
	public ValidateResult createInputData(MonitorInfo info) {

		if(info != null){

			ValidateResult validateResult = super.createInputData(info);

			if(validateResult != null){
				return validateResult;
			}
			else{
				if (this.m_textScope.getText() != null
						&& !"".equals((this.m_textScope.getText()).trim())) {
					info.setFacilityId(this.m_facilityId);
				}
			}
		}
		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {

		super.setEnabled(enabled);
		this.m_textScope.setEnabled(enabled);
		this.m_buttonScope.setEnabled(enabled);
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 * @return 認証結果
	 */
	@Override
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	public Button getButtonScope() {
		return m_buttonScope;
	}

	public String getFacilityId() {
		return m_facilityId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_textScope.setText("");
		this.m_facilityId = null;
	}

	public String getOwnerRoleId(){
		return this.m_ownerRoleId;
	}

	public void setScopeLabel(String labelString) {
		m_labelScope.setText(labelString);
	}
}
