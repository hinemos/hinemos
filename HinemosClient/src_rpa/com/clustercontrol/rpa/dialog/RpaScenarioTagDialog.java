/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.dialog;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddRpaScenarioTagRequest;
import org.openapitools.client.model.ModifyRpaScenarioTagRequest;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.rpa.action.AddRpaScenarioTag;
import com.clustercontrol.rpa.action.GetRpaScenarioTag;
import com.clustercontrol.rpa.action.ModifyRpaScenarioTag;
import com.clustercontrol.rpa.composite.RpaScenarioTagPathListComposite;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

/**
 * シナリオタグ設定作成・変更ダイアログクラス
 */
public class RpaScenarioTagDialog extends CommonDialog{

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioTagDialog.class );
	/** タグID */
	private String tagId = "";
	/** 変更用ダイアログ判別フラグ */
	private int mode;
	/** タグID */
	private Text tagIdText = null;
	/** タグ名 */
	private Text tagNameText = null;
	/** 入力値を保持するオブジェクト */
	private RpaScenarioTagResponse inputData = null;
	/** オーナーロールIDコンボボックス用コンポジット */
	private RoleIdListComposite scenarioTagRoleIdListComposite = null;
	/** マネージャ名 */
	private String managerName = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;
	/** コンボボックス用コンポジット */
	private RpaScenarioTagPathListComposite scenarioTagPathListComposite = null;

	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;
	private Text scenarioTagDescription = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaScenarioTagDialog(Shell parent, String managerName, String id, int mode) {
		super(parent);
		this.managerName = managerName;
		this.tagId = id;
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
		shell.setText(Messages.getString("dialog.rpa.tag.create.modify"));
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
					scenarioTagRoleIdListComposite.createRoleIdList(managerName);
				}
			});
		}
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
					scenarioTagRoleIdListComposite.createRoleIdList(managerName);
				}
			});
		}

		/*
		 * 親タグ
		 */
		Label labelScenarioTagPath = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelScenarioTagPath.setLayoutData(gridData);
		labelScenarioTagPath.setText(Messages.getString("rpa.tag.path") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.scenarioTagPathListComposite = new RpaScenarioTagPathListComposite(parent, SWT.NONE, this.managerName, true);
		} else {
			this.scenarioTagPathListComposite = new RpaScenarioTagPathListComposite(parent, SWT.NONE, this.managerName, false);
		}
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		scenarioTagPathListComposite.setLayoutData(gridData);
		
		/*
		 * タグID
		 */
		//ラベル
		Label lblScenarioTagID = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblScenarioTagID.setLayoutData(gridData);
		lblScenarioTagID.setText(Messages.getString("rpa.tag.id") + " : ");
		//テキスト
		tagIdText = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		tagIdText.setLayoutData(gridData);
		tagIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * タグ名
		 */
		//ラベル
		Label lblScenarioTagName = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblScenarioTagName.setLayoutData(gridData);
		lblScenarioTagName.setText(Messages.getString("rpa.tag.name") + " : ");
		//テキスト
		tagNameText = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		tagNameText.setLayoutData(gridData);
		tagNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * 説明
		 */
		//ラベル
		Label lblScenarioTagDescription = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblScenarioTagDescription.setLayoutData(gridData);
		lblScenarioTagDescription.setText(Messages.getString("description") + " : ");
		//テキスト
		scenarioTagDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		scenarioTagDescription.setLayoutData(gridData);
		scenarioTagDescription.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.scenarioTagRoleIdListComposite = new RoleIdListComposite(parent,
					SWT.NONE, this.managerName, true, Mode.OWNER_ROLE);
		} else {
			this.scenarioTagRoleIdListComposite = new RoleIdListComposite(parent, SWT.NONE, this.managerName, false, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		scenarioTagRoleIdListComposite.setLayoutData(gridData);
		if(scenarioTagRoleIdListComposite.getComboRoleId() != null){
			scenarioTagRoleIdListComposite.getComboRoleId().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// オーナーロール変更時の動作
						scenarioTagPathListComposite.setOwnerRoleID(scenarioTagRoleIdListComposite.getText());
					}
				}
			);
		}

		//オーナーロールに併せて選択可能な親タグの一覧を調整
		scenarioTagPathListComposite.setOwnerRoleID(scenarioTagRoleIdListComposite.getText());
		
		Display calDisplay = shell.getDisplay();
		shell.setLocation((calDisplay.getBounds().width - shell.getSize().x) / 2,
				(calDisplay.getBounds().height - shell.getSize().y) / 2);
		
		
		// ダイアログを調整
		this.adjustDialog();
		//ダイアログにテンプレートセット詳細情報反映
		this.reflectScenarioTag();
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

		// タグIDのインデックス：9
		if("".equals(this.tagIdText.getText())){
			this.tagIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.tagIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// タグ名のインデックス：9
		if("".equals(this.tagNameText.getText())){
			this.tagNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.tagNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	/**
	 * ダイアログの情報からシナリオタグ情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see
	 */
	private ValidateResult createScenarioTag() {
		ValidateResult result = null;

		this.inputData = new RpaScenarioTagResponse();

		//親タグ取得
		if(scenarioTagPathListComposite.getText().length() > 0){
			inputData.setTagPath(scenarioTagPathListComposite.getTagPath(scenarioTagPathListComposite.getText()));
		} else {
			inputData.setTagPath("");
		}
		//タグID取得
		if(tagIdText.getText().length() > 0){
			inputData.setTagId(tagIdText.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("rpa.tag.id"));
			return result;
		}
		log.trace("createScenarioTag cal name = " + tagNameText.getText());
		//タグ名取得
		if(tagNameText.getText().length() > 0 && !"".equals(tagNameText.getText())) {
			log.trace("createScenarioTag22 scenario tag name = " + tagNameText.getText());
			inputData.setTagName(tagNameText.getText());
			log.trace("input template set name = " + inputData.getTagName());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("rpa.tag.name"));
			return result;
		}
		//説明取得
		if(scenarioTagDescription.getText().length() > 0){
			inputData.setDescription(scenarioTagDescription.getText());
		} else {
			inputData.setDescription("");
		}

		//オーナーロールID
		if (scenarioTagRoleIdListComposite.getText().length() > 0) {
			inputData.setOwnerRoleId(scenarioTagRoleIdListComposite.getText());
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
	 * ダイアログにシナリオタグ情報を反映します。
	 *
	 * @param detailList
	 */
	private void reflectScenarioTag() {
		// 初期表示
		RpaScenarioTagResponse scenariTagInfo = null;
		if(mode == PropertyDefineConstant.MODE_MODIFY
				|| mode == PropertyDefineConstant.MODE_COPY
				|| mode == PropertyDefineConstant.MODE_SHOW){
			// 変更、コピーの場合、情報取得
			scenariTagInfo = new GetRpaScenarioTag().getRpaScenarioTag(this.managerName, this.tagId);
		}else{
			// 作成の場合
			scenariTagInfo = new RpaScenarioTagResponse();
		}
		this.inputData = scenariTagInfo;
		//シナリオタグ情報取得
		if(scenariTagInfo != null){
			if (scenariTagInfo.getTagPath() != null) {
				this.scenarioTagPathListComposite.setText(scenarioTagPathListComposite.getParentTagId(scenariTagInfo.getTagPath()));
			}
			if (scenariTagInfo.getTagId() != null) {
				this.tagId = scenariTagInfo.getTagId();
				this.tagIdText.setText(scenariTagInfo.getTagId());
				//シナリオタグ情報変更の際にはタグIDは変更不可
				if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
					this.tagIdText.setEnabled(false);
				}
			}
			if(scenariTagInfo.getTagName() != null){
				this.tagNameText.setText(HinemosMessage.replace(scenariTagInfo.getTagName()));
			}
			if(scenariTagInfo.getDescription() != null){
				this.scenarioTagDescription.setText((HinemosMessage.replace(scenariTagInfo.getDescription())));
			}

			// オーナーロールID取得
			if (scenariTagInfo.getOwnerRoleId() != null) {
				this.scenarioTagRoleIdListComposite.setText(scenariTagInfo.getOwnerRoleId());
			}
		}
		
		// 入力制御
		if(this.mode == PropertyDefineConstant.MODE_SHOW){
			this.scenarioTagPathListComposite.setEnabled(false);
			this.tagIdText.setEnabled(false);
			this.tagNameText.setEnabled(false);
			this.scenarioTagDescription.setEnabled(false);
			this.scenarioTagRoleIdListComposite.setEnabled(false);
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
		createScenarioTag();
		RpaScenarioTagResponse info = this.inputData;
		String managerName = this.m_managerComposite.getText();
		if(info != null){
			try {
				if(mode == PropertyDefineConstant.MODE_ADD){
					// 作成の場合+
					AddRpaScenarioTagRequest addInfoReq = new AddRpaScenarioTagRequest();
					RestClientBeanUtil.convertBean(info, addInfoReq);
					result = new AddRpaScenarioTag().add(managerName, addInfoReq);
				} else if (mode == PropertyDefineConstant.MODE_MODIFY){
					// 変更の場合
					ModifyRpaScenarioTagRequest modifyInfoReq = new ModifyRpaScenarioTagRequest();
					RestClientBeanUtil.convertBean(info, modifyInfoReq);
					result = new ModifyRpaScenarioTag().modify(managerName, tagIdText.getText(), modifyInfoReq);
				} else if (mode == PropertyDefineConstant.MODE_COPY){
					// コピーの場合
					info.setTagId(tagIdText.getText());
					AddRpaScenarioTagRequest addInfoReq = new AddRpaScenarioTagRequest();
					RestClientBeanUtil.convertBean(info, addInfoReq);
					result = new AddRpaScenarioTag().add(managerName, addInfoReq);
				}
			}  catch (HinemosUnknown e) {
				log.error("action() Failed to convert RpaScenarioTag");
			}
		} else {
			log.error("action() RpaScenarioTag is null");
		}
		return result;
	}

	@Override
	protected ValidateResult validate() {
		return validateEndpoint(this.m_managerComposite.getText());
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		// RpaRestEndpointsにはcheckPublishが存在しない
		// どのEndpointでも内容は同じなのでUtilityを使用する
		return UtilityRestClientWrapper.getWrapper(managerName);
	}
}
