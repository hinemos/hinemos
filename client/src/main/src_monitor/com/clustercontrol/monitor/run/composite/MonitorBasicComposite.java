/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.composite;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.notify.NotifyRelationInfo;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 監視基本情報コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「監視項目ID」 テキストボックス</dd>
 *  <dd>「説明」 テキストボックス</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class MonitorBasicComposite extends Composite {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = CommonMonitorDialog.WIDTH_TITLE;

	/** カラム数（値）。*/
	public static final int WIDTH_VALUE = CommonMonitorDialog.WIDTH_VALUE;

	/** カラム数（テキスト）。*/
	public static final int WIDTH_TEXT = CommonMonitorDialog.WIDTH_TEXT;

	/** マネージャリスト用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/** 監視項目ID テキストボックス。 */
	private Text m_textMonitorId = null;

	/** 説明 テキストボックス。 */
	private Text m_textDescription = null;

	/** オーナーロールID用テキスト */
	protected RoleIdListComposite m_ownerRoleId = null;

	/** 親ダイアログ **/
	private CommonMonitorDialog parentDialog = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public MonitorBasicComposite(Composite parent, int style, CommonMonitorDialog parentDialog) {
		super(parent, style);
		this.parentDialog = parentDialog;
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
		 * マネージャ
		 */
		label = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.manager") + " : ");
		if (this.parentDialog.getUpdateFlg()) {
			this.m_managerComposite = new ManagerListComposite(this, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(this, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					parentDialog.getNotifyInfo().setNotify(new ArrayList<NotifyRelationInfo>());
					updateManagerName();
				}
			});
		}
		WidgetTestUtil.setTestId(this, "managerComposite", m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space0", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 監視項目ID
		 */
		// ラベル
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitorid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.id") + " : ");
		// テキスト
		this.m_textMonitorId = new Text(this, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "monitorid", m_textMonitorId);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textMonitorId.setLayoutData(gridData);
		this.m_textMonitorId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 説明
		 */
		// ラベル
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "description", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.m_textDescription = new Text(this, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "description", m_textDescription);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textDescription.setLayoutData(gridData);
		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "roleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		String managerName = this.m_managerComposite.getText();
		if (parentDialog.getUpdateFlg()) {
			this.m_ownerRoleId = new RoleIdListComposite(this, SWT.NONE, managerName, false, Mode.OWNER_ROLE);
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(this, SWT.NONE, managerName, true, Mode.OWNER_ROLE);
			this.m_ownerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateOwnerRole();
				}
			});
		}
		WidgetTestUtil.setTestId(this, null, m_ownerRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_ownerRoleId.setLayoutData(gridData);

	}


	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 監視項目IDが必須項目であることを明示
		if(this.m_textMonitorId.getEnabled() && "".equals(this.m_textMonitorId.getText())){
			this.m_textMonitorId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMonitorId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	private void updateOwnerRole(){
		parentDialog.updateOwnerRole(m_ownerRoleId.getText());
	}

	protected void updateManagerName(){
		if (parentDialog.getUpdateFlg() == false) {
			String managerName = m_managerComposite.getText();
			this.m_ownerRoleId.createRoleIdList(managerName);
		}
		updateOwnerRole();
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 * @param udpateFlg 更新するか否か（true：更新する）
	 */
	public void setInputData(MonitorInfo info, boolean updateFlg) {

		if(info != null){
			// 監視項目ＩＤ
			if (info.getMonitorId() != null) {
				this.m_textMonitorId.setText(info.getMonitorId());
				if (updateFlg) {
					this.m_textMonitorId.setEnabled(false);
				}
			}
			// 説明
			if (info.getDescription() != null) {
				this.m_textDescription.setText(info.getDescription());
			}
			// オーナーロールID
			if (info.getOwnerRoleId() != null) {
				this.m_ownerRoleId.setText(info.getOwnerRoleId());
			}
		}

		// マネージャに対応するオーナーロールIDの更新
		// オーナーロールID関連コンポジット更新
		updateManagerName();

		// 監視項目IDが必須項目であることを明示
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
	 * @see #setValidateResult(String, String)
	 */
	public ValidateResult createInputData(MonitorInfo info) {

		if(info != null){
			// 監視項目ID
			if (this.m_textMonitorId.getText() != null
					&& !"".equals((this.m_textMonitorId.getText()).trim())) {
				info.setMonitorId(this.m_textMonitorId.getText());
			}
			// 説明
			if (this.m_textDescription.getText() != null
					&& !"".equals((this.m_textDescription.getText()).trim())) {
				info.setDescription(this.m_textDescription.getText());
			}
			// オーナーロールID
			if (this.m_ownerRoleId.getText().length() > 0) {
				info.setOwnerRoleId(m_ownerRoleId.getText());
			}

		}
		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_textMonitorId.setEnabled(enabled);
		this.m_textDescription.setEnabled(enabled);
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	public ManagerListComposite getManagerListComposite() {
		return this.m_managerComposite;
	}
}
