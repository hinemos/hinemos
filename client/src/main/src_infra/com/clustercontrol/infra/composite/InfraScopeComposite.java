/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ClusterControlPlugin;

/**
 * スコープ基本情報コンポジットクラス<BR>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「スコープ」 テキストボックス</dd>
 *  <dd>「参照」 ボタン</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 5.0.0
 */
public class InfraScopeComposite extends Composite {

	/** 変数用ラジオボタン */
	private Button m_scopeParam = null;
	
	/** 固定値用ラジオボタン */
	private Button m_scopeFixedValue = null;

	/** スコープ テキストボックス。 */
	private Text m_scope = null;

	/** 参照 ボタン。 */
	private Button btnRefer = null;

	/** 選択されたスコープのファシリティID。 */
	private String m_facilityId = null;

	/** 未登録ノード スコープを表示するかフラグ*/
	private boolean m_unregistered = false;

	/** オーナーロールID*/
	private String m_ownerRoleId = null;

	/** マネージャ名 */
	private String m_managerName = null;

//	/** 選択されたスコープのファシリティ名*/
//	private String m_facilityName = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、スコープのコンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #initialize()
	 */
	public InfraScopeComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);

		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		
		m_scopeParam = new Button(this, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_scopeParam", this.m_scopeParam);
		this.m_scopeParam.setText(Messages.getString("infra.parameter") + " : ");
		this.m_scopeParam.setLayoutData(
				new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeParam.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_scopeFixedValue.setSelection(false);
					btnRefer.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		// スコープ：ジョブ変数（ラベル）
		Label scopeJobParamLabel = new Label(this, SWT.LEFT);
		scopeJobParamLabel.setText("#[FACILITY_ID]");
		scopeJobParamLabel.setLayoutData(
				new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		//dummy
		new Label(this, SWT.LEFT);
		
		m_scopeFixedValue = new Button(this, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_scopeFixedValue", this.m_scopeFixedValue);
		this.m_scopeFixedValue.setText(Messages.getString("fixed.value") + " : ");
		this.m_scopeFixedValue.setLayoutData(
				new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValue.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_scopeFixedValue.setSelection(true);
					btnRefer.setEnabled(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		this.m_scope = new Text(this, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "scope", m_scope);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = GridData.FILL;
		this.m_scope.setLayoutData(gridData);
		this.m_scope.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		// 参照ボタン
		btnRefer = new Button(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "refer", btnRefer);
		btnRefer.setText(Messages.getString("refer"));
		btnRefer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(null, m_managerName, m_ownerRoleId, false, m_unregistered);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem item = dialog.getSelectItem();
					FacilityInfo info = item.getData();
					if( !info.getFacilityId().equals(InfraScopeComposite.this.m_facilityId) ){
						InfraScopeComposite.this.m_facilityId = info.getFacilityId();
						if (info.getFacilityType() == FacilityConstant.TYPE_NODE) {
							m_scope.setText(info.getFacilityName());
						} else {
							FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
							m_scope.setText(path.getPath(item));
						}
					}
				}
			}
		});
		btnRefer.setEnabled(false);

		update();
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update(){
		super.update();
		// スコープが必須項目であることを明示
		if(m_scopeFixedValue.getSelection() && "".equals((this.m_scope.getText()).trim())){
			this.m_scope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 引数で指定された情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる情報
	 *
	 * @see com.clustercontrol.infra.composite.MonitorBasicComposite#setInputData(MonitorInfo)
	 */
	public void setInputData(String managerName, InfraManagementInfo info) {
		this.m_managerName = managerName;
		setInputData( info );
	}
	public void setInputData(InfraManagementInfo info) {
		if(info != null){
			this.m_ownerRoleId = info.getOwnerRoleId();
			this.m_facilityId = info.getFacilityId();
			if (m_facilityId == null) {
				// 変数の場合
				m_scopeParam.setSelection(true);
				btnRefer.setEnabled(false);
			} else {
				// 固定値の場合
				m_scopeFixedValue.setSelection(true);
				btnRefer.setEnabled(true);
				this.m_scope.setText(HinemosMessage.replace(info.getScope()));
			}

		} else {
			m_scopeFixedValue.setSelection(true);
			btnRefer.setEnabled(false);
		}
		// スコープが必須項目であることを明示
		this.update();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.m_scope.setEnabled(enabled);
		this.btnRefer.setEnabled(enabled);
	}

	public String getFacilityId() {
		if (m_scopeParam.getSelection()) {
			return null;
		}
		return m_facilityId;
	}

	public void setOwnerRoleId(String managerName, String ownerRoleId) {
		this.m_managerName = managerName;
		setOwnerRoleId( ownerRoleId );
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_scope.setText("");
		this.m_facilityId = "";
	}

	public String getOwnerRoleId(){
		return this.m_ownerRoleId;
	}

	public String getScope(){
		return m_scope.getText();
	}
	
	public void setScopeParam(boolean scopeParam) {
		this.m_scopeParam.setSelection(scopeParam);
	}
}
