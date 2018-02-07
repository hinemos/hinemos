/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.dialog;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.reporting.action.AddTemplateSet;
import com.clustercontrol.reporting.action.GetTemplateSet;
import com.clustercontrol.reporting.action.GetTemplateSetDetailTableDefine;
import com.clustercontrol.reporting.action.ModifyTemplateSet;
import com.clustercontrol.reporting.composite.TemplateSetDetailInfoComposite;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.reporting.TemplateSetDetailInfo;
import com.clustercontrol.ws.reporting.TemplateSetInfo;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;

/**
 * テンプレートセット設定作成・変更ダイアログクラス<BR>
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateSetDialog extends CommonDialog{

	// ログ
	private static Log m_log = LogFactory.getLog( TemplateSetDialog.class );
	/** テンプレートセットID */
	private String templateSetId = "";
	/** 変更用ダイアログ判別フラグ */
	private int mode;
	/** テンプレートセットID */
	private Text templateSetIdText = null;
	/** テンプレートセット名 */
	private Text templateSetNameText = null;
	/** 入力値を保持するオブジェクト */
	private TemplateSetInfo inputData = null;
	/** テンプレートセット詳細情報 */
	private TemplateSetDetailInfoComposite templateSetDetailInfoComposite = null;
	/** オーナーロールID用テキスト */
	private RoleIdListComposite templateSetRoleIdListComposite = null;
	/** マネージャ名 */
	private String managerName = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;
	private Group templateSetDetailGroup = null;// テンプレートセット詳細グループ
	private Text templateSetDescription = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public TemplateSetDialog(Shell parent, String managerName, String id, int mode) {
		super(parent);
		this.managerName = managerName;
		this.templateSetId = id;
		this.mode = mode;
	}
	// ----- instance メソッド ----- //
	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.reporting.template.set.create.modify"));
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 13;
		parent.setLayout(layout);
		
		/*
		 * マネージャ
		 */
		Label labelManager = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 4;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if(this.mode == PropertyDefineConstant.MODE_MODIFY
				|| this.mode == PropertyDefineConstant.MODE_SHOW){
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					// オーナーロールIDの対処
					templateSetRoleIdListComposite.createRoleIdList(managerName);
				}
			});
		}
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 8;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}
		
		if(this.mode != PropertyDefineConstant.MODE_MODIFY
				&& this.mode != PropertyDefineConstant.MODE_SHOW) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					templateSetRoleIdListComposite.createRoleIdList(managerName);
					templateSetDetailInfoComposite.setManagerName(managerName);
					templateSetDetailInfoComposite.update();
				}
			});
		}

		/*
		 * テンプレートセットID
		 */
		//ラベル
		Label lblTemplateSetID = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "templatesetid", lblTemplateSetID);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblTemplateSetID.setLayoutData(gridData);
		lblTemplateSetID.setText(Messages.getString("template.set.id") + " : ");
		//テキスト
		templateSetIdText = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "templatesetid", templateSetIdText);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		templateSetIdText.setLayoutData(gridData);
		templateSetIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * テンプレートセット名
		 */
		//ラベル
		Label lblTemplateSetName = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "templatesetname", lblTemplateSetName);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblTemplateSetName.setLayoutData(gridData);
		lblTemplateSetName.setText(Messages.getString("template.set.name") + " : ");
		//テキスト
		templateSetNameText = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "templatesetname", templateSetNameText);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		templateSetNameText.setLayoutData(gridData);
		templateSetNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * 説明
		 */
		//ラベル
		Label lblTemplateSetDescription = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "description", lblTemplateSetDescription);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblTemplateSetDescription.setLayoutData(gridData);
		lblTemplateSetDescription.setText(Messages.getString("description") + " : ");
		//テキスト
		templateSetDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "caldescription", templateSetDescription);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		templateSetDescription.setLayoutData(gridData);
		templateSetDescription.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "ownerroleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.templateSetRoleIdListComposite = new RoleIdListComposite(parent,
					SWT.NONE, this.managerName, true, Mode.OWNER_ROLE);
			this.templateSetRoleIdListComposite.getComboRoleId().addSelectionListener(
					new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							if (templateSetDetailInfoComposite.getDetailList() != null
									&& templateSetDetailInfoComposite.getDetailList().size() > 0) {
								templateSetRoleIdListComposite.setText(templateSetRoleIdListComposite.getText());
								return;
							}
							templateSetDetailInfoComposite.changeOwnerRoleId(templateSetRoleIdListComposite.getText());
						}
					});
		} else {
			this.templateSetRoleIdListComposite = new RoleIdListComposite(parent, SWT.NONE, this.managerName, false, Mode.OWNER_ROLE);
		}
		WidgetTestUtil.setTestId(this, "templatesetroleidlist", templateSetRoleIdListComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		templateSetRoleIdListComposite.setLayoutData(gridData);
		
		/*
		 * テンプレートセット詳細グループ
		 *
		 */
		templateSetDetailGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "detail", templateSetDetailGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		templateSetDetailGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 13;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		templateSetDetailGroup.setLayoutData(gridData);
		templateSetDetailGroup.setText(Messages.getString("template.set.detail"));

		/**
		 *  テンプレートセット詳細定義情報
		 */
		//詳細情報テーブルカラム取得
		GetTemplateSetDetailTableDefine.get();
		this.templateSetDetailInfoComposite = new TemplateSetDetailInfoComposite(templateSetDetailGroup, SWT.NONE, this.managerName);
		WidgetTestUtil.setTestId(this, "detail", templateSetDetailInfoComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 220;
		templateSetDetailInfoComposite.setLayoutData(gridData);

		Display calDisplay = shell.getDisplay();
		shell.setLocation((calDisplay.getBounds().width - shell.getSize().x) / 2,
				(calDisplay.getBounds().height - shell.getSize().y) / 2);
		
		
		// ダイアログを調整
		this.adjustDialog();
		//ダイアログにテンプレートセット詳細情報反映
		this.reflectTemplateSet();
		// 必須入力項目を可視化
		this.update();

	}


	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(600, shell.getSize().y));

		// 画面中央に配置
		Display calAdjustDisplay = shell.getDisplay();
		shell.setLocation((calAdjustDisplay.getBounds().width - shell.getSize().x) / 2,
				(calAdjustDisplay.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示

		// テンプレートセットIDのインデックス：9
		if("".equals(this.templateSetIdText.getText())){
			this.templateSetIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.templateSetIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// テンプレートセット名のインデックス：9
		if("".equals(this.templateSetNameText.getText())){
			this.templateSetNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.templateSetNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	/**
	 * ダイアログの情報からテンプレートセット情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see
	 */
	private ValidateResult createTemplateSetInfo() {
		ValidateResult result = null;

		this.inputData = new TemplateSetInfo();

		//テンプレートセットID取得
		if(templateSetIdText.getText().length() > 0){
			inputData.setTemplateSetId(templateSetIdText.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("template.set.id"));
			return result;
		}
		m_log.trace("createTemplateSetInfo cal name = " + templateSetNameText.getText());
		//テンプレートセット名取得
		if(templateSetNameText.getText().length() > 0 && !"".equals(templateSetNameText.getText())) {
			m_log.trace("createTemplateSetInfo22 template set name = " + templateSetNameText.getText());
			inputData.setTemplateSetName(templateSetNameText.getText());
			m_log.trace("input template set name = " + inputData.getTemplateSetName());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("template.set.name"));
			return result;
		}
		//説明取得
		if(templateSetDescription.getText().length() > 0){
			inputData.setDescription(templateSetDescription.getText());
		} else {
			inputData.setDescription("");
		}

		//テンプレートセット詳細情報取得
		if (this.templateSetDetailInfoComposite.getDetailList() != null) {

			m_log.debug("Add TemplateSetDetailInfo : " +
					this.templateSetDetailInfoComposite.getDetailList().size());

			for (TemplateSetDetailInfo detailInfo : this.templateSetDetailInfoComposite.getDetailList()) {
				this.inputData.getTemplateSetDetailInfoList().add(detailInfo);
			}
		}
		//オーナーロールID
		if (templateSetRoleIdListComposite.getText().length() > 0) {
			inputData.setOwnerRoleId(templateSetRoleIdListComposite.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("owner.role.id"));
			return result;
		}
		
		return result;
	}

	/**
	 * ダイアログにテンプレートセット情報を反映します。
	 *
	 * @param detailList
	 */
	private void reflectTemplateSet() {
		// 初期表示
		TemplateSetInfo templateSetInfo = null;
		if(mode == PropertyDefineConstant.MODE_MODIFY
				|| mode == PropertyDefineConstant.MODE_COPY
				|| mode == PropertyDefineConstant.MODE_SHOW){
			// 変更、コピーの場合、情報取得
			templateSetInfo = new GetTemplateSet().getTemplateSetInfo(this.managerName, this.templateSetId);
		}else{
			// 作成の場合
			templateSetInfo = new TemplateSetInfo();
		}
		this.inputData = templateSetInfo;
		//テンプレートセット情報取得
		if(templateSetInfo != null){
			if (templateSetInfo.getTemplateSetId() != null) {
				this.templateSetId = templateSetInfo.getTemplateSetId();
				this.templateSetIdText.setText(templateSetInfo.getTemplateSetId());
				//テンプレートセット定義変更の際にはテンプレートセットIDは変更不可
				if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
					this.templateSetIdText.setEnabled(false);
				}
			}
			if(templateSetInfo.getTemplateSetName() != null){
				this.templateSetNameText.setText(HinemosMessage.replace(templateSetInfo.getTemplateSetName()));
			}
			if(templateSetInfo.getDescription() != null){
				this.templateSetDescription.setText((HinemosMessage.replace(templateSetInfo.getDescription())));
			}
			
			// テンプレートセット詳細情報取得
			templateSetDetailInfoComposite.setDetailList(
					(ArrayList<TemplateSetDetailInfo>) templateSetInfo.getTemplateSetDetailInfoList());

			// オーナーロールID取得
			if (templateSetInfo.getOwnerRoleId() != null) {
				this.templateSetRoleIdListComposite.setText(templateSetInfo.getOwnerRoleId());
			}
			this.templateSetDetailInfoComposite.setOwnerRoleId(this.templateSetRoleIdListComposite.getText());
		}
		
		// 入力制御
		if(this.mode == PropertyDefineConstant.MODE_SHOW){
			this.templateSetIdText.setEnabled(false);
			this.templateSetNameText.setEnabled(false);
			this.templateSetDescription.setEnabled(false);
			this.templateSetDetailInfoComposite.setEnabled(false);
			this.templateSetRoleIdListComposite.setEnabled(false);
			this.m_managerComposite.setEnabled(false);
		}
		

		this.update();
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
		createTemplateSetInfo();
		TemplateSetInfo info = this.inputData;
		String managerName = this.m_managerComposite.getText();
		if(info != null){
			if(mode == PropertyDefineConstant.MODE_ADD){
				// 作成の場合+
				result = new AddTemplateSet().add(managerName, info);
			} else if (mode == PropertyDefineConstant.MODE_MODIFY){
				// 変更の場合
				info.setTemplateSetId(templateSetIdText.getText());
				result = new ModifyTemplateSet().modify(managerName, info);
			} else if (mode == PropertyDefineConstant.MODE_COPY){
				// コピーの場合
				info.setTemplateSetId(templateSetIdText.getText());
				
				for(TemplateSetDetailInfo detailInfo : info.getTemplateSetDetailInfoList()) {
					detailInfo.setTemplateSetId(templateSetIdText.getText());
				}
				result = new AddTemplateSet().add(managerName, info);
			}
		} else {
			m_log.error("action() TemplateSetinfo is null");
		}
		return result;
	}
}
