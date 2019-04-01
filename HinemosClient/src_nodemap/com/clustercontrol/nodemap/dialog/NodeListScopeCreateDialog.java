/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.nodemap.util.NodeMapEndpointWrapper;
import com.clustercontrol.nodemap.util.NodemapUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.nodemap.FacilityDuplicate_Exception;
import com.clustercontrol.ws.nodemap.InvalidRole_Exception;
import com.clustercontrol.ws.repository.NodeInfo;
import com.clustercontrol.ws.repository.ScopeInfo;

/**
 * 検索結果ノード割当て用スコープの作成ダイアログクラス<BR>
 *
 * @version 6.2.0
 */
public class NodeListScopeCreateDialog extends CommonDialog {

	// ----- instance フィールド ----- //

	/** 生成するスコープのファシリティID用テキスト */
	private Text m_scopeFacilityIdText = null;

	/** 生成するスコープのファシリティ名用テキスト */
	private Text m_scopeFacilityNameText = null;

	/** 生成するスコープの説明用テキスト */
	private Text m_descriptionText = null;

	/** オーナーロールID用Composite */
	private RoleIdListComposite m_ownerRoleId = null;

	/** ファシリティIDリスト */
	private List<String> m_facilityIdList = new ArrayList<>();

	/** マネージャ名 */
	private String m_managerName = "";

	/** 生成するスコープのファシリティID */
	private String m_scopeFacilityId = "";

	/** 生成するスコープのイメージ */
	private Text m_imageText = null;

	/** フィルタ条件 */
	private NodeInfo m_nodeFilterInfo = null;

	// ----- コンストラクタ ----- //

	/**
	 * 指定した形式のダイアログのインスタンスを返します。
	 *
	 * @param parent 親シェル
	 * @param managerName マネージャ名
	 * @param scopeFacilityId スコープのファシリティID
	 * @param facilityIdList スコープに割り当てるファシリティIDリスト
	 * @param nodeFilterInfo 検索条件
	 */
	public NodeListScopeCreateDialog(
			Shell parent, String managerName, String scopeFacilityId, List<String> facilityIdList, NodeInfo nodeFilterInfo) {
		super(parent);
		this.m_managerName = managerName;
		this.m_scopeFacilityId = scopeFacilityId;
		this.m_facilityIdList = facilityIdList;
		this.m_nodeFilterInfo = nodeFilterInfo;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 350);
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
		shell.setText(Messages.getString("dialog.nodemap.scope.create"));

		// レイアウト
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		GridData gridData = new GridData();

		/*
		 * マネージャ名
		 */
		Label label = new Label(parent, SWT.LEFT);
		gridData = new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT);
		gridData.horizontalAlignment = GridData.FILL;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.managername") + " : ");

		Text text = new Text(parent, SWT.BORDER);
		text.setText(this.m_managerName);
		text.setEditable(false);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalAlignment = GridData.FILL;
		text.setLayoutData(gridData);
	
		/*
		 * オーナーロールID
		 */
		label = new Label(parent, SWT.LEFT);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("owner.role.id") + " : ");

		this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, this.m_managerName, true, Mode.OWNER_ROLE);
		gridData = new GridData(200, SizeConstant.SIZE_COMBO_HEIGHT);
		gridData.horizontalAlignment = GridData.FILL;
		m_ownerRoleId.setLayoutData(gridData);

		/*
		 * スコープのファシリティID
		 */
		label = new Label(parent, SWT.LEFT);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("facility.id") + " : ");

		m_scopeFacilityIdText = new Text(parent, SWT.BORDER);
		m_scopeFacilityIdText.setText(this.m_scopeFacilityId);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalAlignment = GridData.FILL;
		m_scopeFacilityIdText.setLayoutData(gridData);
		this.m_scopeFacilityIdText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * スコープのファシリティ名
		 */
		label = new Label(parent, SWT.LEFT);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("facility.name") + " : ");

		m_scopeFacilityNameText = new Text(parent, SWT.BORDER);
		m_scopeFacilityNameText.setText(this.m_scopeFacilityId);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalAlignment = GridData.FILL;
		m_scopeFacilityNameText.setLayoutData(gridData);
		this.m_scopeFacilityNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * スコープの説明
		 */
		label = new Label(parent, SWT.LEFT);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("description") + " : ");

		m_descriptionText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		gridData = new GridData(250, 100);
		gridData.horizontalAlignment = GridData.FILL;
		m_descriptionText.setLayoutData(gridData);

		/*
		 * スコープのイメージ
		 */
		label = new Label(parent, SWT.LEFT);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("icon.image") + " : ");

		m_imageText = new Text(parent, SWT.BORDER);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalAlignment = GridData.FILL;
		m_imageText.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// 説明に検索条件を設定する
		m_descriptionText.setText(NodemapUtil.createConditionString(m_nodeFilterInfo));

		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		if(this.m_scopeFacilityIdText.getEnabled() && "".equals(this.m_scopeFacilityIdText.getText())){
			this.m_scopeFacilityIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scopeFacilityIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_scopeFacilityNameText.getEnabled() && "".equals(this.m_scopeFacilityNameText.getText())){
			this.m_scopeFacilityNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scopeFacilityNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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
		String errMessage = "";

		NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(this.m_managerName);
		Object[] arg = {this.m_managerName};

		// 作成の場合
		try {
			ScopeInfo scopeInfo = new ScopeInfo();
			scopeInfo.setFacilityId(this.m_scopeFacilityIdText.getText());
			scopeInfo.setFacilityName(this.m_scopeFacilityNameText.getText());
			scopeInfo.setDescription(this.m_descriptionText.getText());
			scopeInfo.setOwnerRoleId(this.m_ownerRoleId.getText());
			scopeInfo.setIconImage(this.m_imageText.getText());
			wrapper.addFilterScope(scopeInfo, m_facilityIdList);

			// リポジトリキャッシュの更新
			ClientSession.doCheck();

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.repository.14", arg));

			result = true;

		} catch (FacilityDuplicate_Exception e) {
			String args[] = {m_scopeFacilityId};

			// ファシリティIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.repository.63", args));

		} catch (Exception e) {
			if (e instanceof InvalidRole_Exception) {
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

		return result;
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
}
