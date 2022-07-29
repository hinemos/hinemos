/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.dialog;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.openapitools.client.model.AddScopeRequest;
import org.openapitools.client.model.ModifyScopeRequest;
import org.openapitools.client.model.ScopeInfoRequest;
import org.openapitools.client.model.ScopeInfoResponseP1;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.PropertyFieldColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.bean.ScopeConstant;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * スコープの作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class ScopeCreateDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( ScopeCreateDialog.class );

	// ----- instance フィールド ----- //

	/** 初期表示ノードのファシリティID */
	private String facilityId = "";

	/** 親ファシリティID */
	private String parentFacilityId = "";

	/** 変更用ダイアログ判別フラグ */
	private boolean isModifyDialog = false;

	/** ノード属性プロパティシート */
	private PropertySheet propertySheet = null;

	/** ノード属性テーブル **/
	private Tree table = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * 指定した形式のダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param facilityId
	 *            初期表示するノードのファシリティID
	 * @param isModifyDialog
	 *            変更用ダイアログとして利用する場合は、true
	 */
	public ScopeCreateDialog(Shell parent, String managerName, String facilityId,
			boolean isModifyDialog) {
		super(parent);

		this.managerName = managerName;
		this.facilityId = facilityId;
		this.isModifyDialog = isModifyDialog;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 400);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.repository.scope.create.modify"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		GridData gridData = new GridData();

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "roleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if(!this.isModifyDialog()){
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, this.managerName, true, Mode.OWNER_ROLE);
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, this.managerName, false, Mode.OWNER_ROLE);
		}
		WidgetTestUtil.setTestId(this, null, m_ownerRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_ownerRoleId.setLayoutData(gridData);

		/*
		 * 属性プロパティシート
		 */
		// ラベル
		Label label = new Label(parent, SWT.LEFT);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("attribute") + " : ");

		// プロパティシート
		table = new Tree( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
		WidgetTestUtil.setTestId(this, null, table);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);
		table.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		this.propertySheet = new PropertySheet(table);

		// プロパティ取得及び設定
		Property property = null;
		RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(this.managerName);
		if (this.isModifyDialog) {
			try {
				ScopeInfoResponseP1 scopeInfo = wrapper.getScope(this.facilityId);
				if (scopeInfo == null)
					throw new HinemosUnknown("ScopeInfo is null, facilityId : " + this.facilityId);
				
				// オーナーロールID取得
				if (scopeInfo.getOwnerRoleId() != null) {
					m_ownerRoleId.setText(scopeInfo.getOwnerRoleId());
				}
				property = getScopeProperty(scopeInfo, PropertyDefineConstant.MODE_MODIFY);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("getScopeProperty(), " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		} else {
			try {
				ScopeInfoResponseP1 scopeInfo = wrapper.getScopeDefault();
				// オーナーロールID取得
				if (scopeInfo == null)
					throw new HinemosUnknown("ScopeInfo is null");
				
				if (scopeInfo.getOwnerRoleId() != null) {
					m_ownerRoleId.setText(scopeInfo.getOwnerRoleId());
				}
				property = getScopeProperty(scopeInfo, PropertyDefineConstant.MODE_ADD);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("getScopeProperty(), " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		if (property != null)
			this.propertySheet.setInput(property);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * Set required (pink background color) when empty
	 * @param item
	 */
	private void setRequired(TreeItem item) {
		Property element = (Property)item.getData();
		if ("".equals(element.getValueText())) {
			item.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			item.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	
	/**
	 * 入力項目ですの色を薄くする。
	 * @param item
	 */
	private void setForegroundColor(TreeItem item) {
		if (item == null) {
			return;
		}
		Property element = (Property)item.getData();
		if (element != null && "".equals(element.getValueText())) {
			item.setForeground(1, PropertyFieldColorConstant.COLOR_EMPTY);
		} else {
			item.setForeground(1, PropertyFieldColorConstant.COLOR_FILLED);
		}

		for (TreeItem child : item.getItems()) {
			setForegroundColor(child);
		}
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		// ファシリティIDのインデックス：0
		setRequired( table.getItem(0) );
		// ファシリティIDのインデックス：1
		setRequired( table.getItem(1) );

		for (TreeItem item : table.getItems()) {
			setForegroundColor(item);
		}
		
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		return null;
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

		Property property = this.getInputData();
		if(property != null){
			String errMessage = "";
			Property copy = PropertyUtil.copy(property);
			PropertyUtil.deletePropertyDefine(copy);
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(this.managerName);
			Object[] arg = {this.managerName};
			if(!this.isModifyDialog()){
				// 作成の場合
				try {
					ScopeInfoRequest scopeinfo = ScopePropertyUtil.property2scope(copy);
					if (m_ownerRoleId.getText().length() > 0) {
						scopeinfo.setOwnerRoleId(m_ownerRoleId.getText());
					}
					AddScopeRequest requestDto = new AddScopeRequest();
					requestDto.setParentFacilityId(parentFacilityId);
					requestDto.setScopeInfo(scopeinfo);
					wrapper.addScope(requestDto);

					// リポジトリキャッシュの更新
					ClientSession.doCheck();

					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.repository.14", arg));

					result = true;

				} catch (FacilityDuplicate e) {
					//ファシリティID取得
					ArrayList<?> values = PropertyUtil.getPropertyValue(copy, ScopeConstant.FACILITY_ID);
					String args[] = { (String)values.get(0) };

					// ファシリティIDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.repository.26", args));

				} catch (Exception e) {
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.repository.15") + errMessage);
				}
			} else {
				// 変更の場合
				try {
					ScopeInfoRequest scopeInfo = ScopePropertyUtil.property2scope(copy);
					if (m_ownerRoleId.getText().length() > 0) {
						scopeInfo.setOwnerRoleId(m_ownerRoleId.getText());
					}
					ModifyScopeRequest requestDto = new ModifyScopeRequest();
					RestClientBeanUtil.convertBean(scopeInfo, requestDto);
					wrapper.modifyScope(scopeInfo.getFacilityId(), requestDto);

					// リポジトリキャッシュの更新
					ClientSession.doCheck();

					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.repository.18", arg));

					result = true;

				} catch (Exception e) {
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.repository.19") + errMessage);
				}
			}
		}

		return result;
	}

	/**
	 * 変更用ダイアログなのかを返します。
	 *
	 * @return 変更用ダイアログの場合、true
	 */
	public boolean isModifyDialog() {
		return this.isModifyDialog;
	}

	/**
	 * 入力値を保持したデータモデルを生成します。
	 *
	 * @return データモデル
	 */
	public Property getInputData() {
		return (Property) this.propertySheet.getInput();
	}

	/**
	 * 親ファシリティIDを返します。
	 *
	 * @return 親ファシリティID
	 */
	public String getParentFacilityId() {
		return parentFacilityId;
	}

	/**
	 * 親ファシリティIDを設定します。
	 *
	 * @param parentFacilityId 親ファシリティID
	 */
	public void setParentFacilityId(String parentFacilityId) {
		this.parentFacilityId = parentFacilityId;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("register");
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
	 * スコープ属性情報を取得します。<BR>
	 *
	 * @param facilityId
	 * @param property
	 * @return スコープ属性情報
	 */
	private static Property getScopeProperty(ScopeInfoResponseP1 scopeInfo, int mode) {

		Property property = ScopePropertyUtil.scope2property(scopeInfo, mode, Locale.getDefault());
		return property;
	}
}
