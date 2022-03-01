/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.TemplateIdListResponse;
import org.openapitools.client.model.TemplateSetDetailInfoResponse;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.reporting.util.ReportingRestClientWrapper;

/**
 * テンプレートセット詳細設定ダイアログ作成・変更ダイアログクラス<BR>
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateSetDetailDialog extends CommonDialog{

	// ----- instance フィールド ----- //
	// ログ
	private static Log m_log = LogFactory.getLog( TemplateSetDetailDialog.class );
	/* ----- 変数 ----- */
	/**
	 * ダイアログの最背面レイヤのカラム数
	 * 最背面のレイヤのカラム数のみを変更するとレイアウトがくずれるため、
	 * グループ化されているレイヤは全てこれにあわせる
	 */
	private final int DIALOG_WIDTH = 8;
	/** 入力値を保持するオブジェクト */
	private TemplateSetDetailInfoResponse inputData = null;
	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult m_validateResult = null;
	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;
	private Group templateSettingGroup = null; //テンプレート設定グループ

	/**コンボボックス**/
	private Combo templateIdCombo = null;

	/**テキスト**/
	//説明
	private Text templateSetDetailDescriptionText = null;
	//タイトル
	private Text titleNameText = null;

	// オーナーロールID
	private String ownerRoleId = null;
	/** マネージャ名 */
	private String managerName = null;

	/**
	 *
	 * @return
	 */
	public TemplateSetDetailInfoResponse getInputData() {
		return this.inputData;
	}
	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 */
	public TemplateSetDetailDialog(Shell parent, String managerName, String ownerRoleId) {
		super(parent);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
	}
	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 * @param ownerRoleId オーナーロールID
	 */
	public TemplateSetDetailDialog(Shell parent, String managerName, int order, String ownerRoleId){
		super(parent);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
	}
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param detailInfo カレンダ詳細情報
	 * @param ownerRoleId オーナーロールID
	 */
	public TemplateSetDetailDialog(Shell parent, String managerName, TemplateSetDetailInfoResponse detailInfo, String ownerRoleId){
		super(parent);
		this.managerName = managerName;
		this.inputData = detailInfo;
		this.ownerRoleId = ownerRoleId;
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
		//レポーティング[テンプレートセット詳細の作成・変更]
		shell.setText(Messages.getString("dialog.reporting.template.set.detail.create.modify"));

		// ラベル
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = DIALOG_WIDTH;
		parent.setLayout(layout);
		/*
		 * 説明
		 */
		//ラベル
		Label label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText("  " + Messages.getString("description"));
		//テキスト
		templateSetDetailDescriptionText = new Text(parent, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "description", templateSetDetailDescriptionText);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		templateSetDetailDescriptionText.setLayoutData(gridData);
		templateSetDetailDescriptionText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * テンプレート設定グループ
		 */
		templateSettingGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "templatesetting", templateSettingGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 8;
		templateSettingGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		templateSettingGroup.setLayoutData(gridData);
		//テンプレート設定
		templateSettingGroup.setText(Messages.getString("template.setting"));
		
		//テンプレートIDコンボボックス
		Label lblTemplateID = new Label(templateSettingGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "templateid", lblTemplateID);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblTemplateID.setLayoutData(gridData);
		lblTemplateID.setText(Messages.getString("template.id"));
		// コンボ
		this.templateIdCombo = new Combo(templateSettingGroup, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "templateidcombo", templateIdCombo);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.templateIdCombo.setLayoutData(gridData);
		this.templateIdCombo.add("");
		for(String str : getTemplateIdList(this.managerName, this.ownerRoleId)){
			this.templateIdCombo.add(str);
		}
		this.templateIdCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});
		
		// タイトル
		label = new Label(templateSettingGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "titlename", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		//タイトル名
		label.setText(Messages.getString("title.name") + "");
		//テキスト
		titleNameText = new Text(templateSettingGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "titlename", titleNameText);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		titleNameText.setLayoutData(gridData);
		titleNameText.setText("");
		titleNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		shell.pack();
		// 画面中央に
		Display templateSetDetailDisplay = shell.getDisplay();
		shell.setLocation((templateSetDetailDisplay.getBounds().width - shell.getSize().x) / 2,
				(templateSetDetailDisplay.getBounds().height - shell.getSize().y) / 2);
		
		// 必須入力項目を可視化
		this.update();
		this.reflectTemplateSetDetail();
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		   // 特になし
	}

	/**
	 * ダイアログの情報からカレンダ情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see
	 */
	private TemplateSetDetailInfoResponse createTemplateSetDetailInfo() {

		inputData = new TemplateSetDetailInfoResponse();
		/*
		 * 説明
		 */
		if(templateSetDetailDescriptionText.getText().length() > 0){
			this.inputData.setDescription(templateSetDetailDescriptionText.getText());
		} else {
			this.inputData.setDescription("");
		}

		/*
		 * テンプレートID
		 */
		if(templateIdCombo.getText() != null && templateIdCombo.getText().length() > 0){
			this.inputData.setTemplateId(templateIdCombo.getText());
		} else {
			String[] args = {"[ " +  Messages.getString("template.id") + " ]"};
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.reporting.31",args));
			return null;
		}
		
		/*
		 * タイトル
		 */
		if(titleNameText.getText().length() > 0){
			this.inputData.setTitleName(titleNameText.getText());
		}
		
		return inputData;
	}
	
	/**
	 * ダイアログにテンプレートセット詳細情報を反映します。
	 */
	private void reflectTemplateSetDetail() {
		// 初期表示
		TemplateSetDetailInfoResponse detailInfo = null;
		if(this.inputData != null){
			// 変更の場合、情報取得
			detailInfo = this.inputData;
			//ここで、getSelection() firstElementを取得する
		}
		else{
			// 作成の場合
			detailInfo = new TemplateSetDetailInfoResponse();
		}
		//テンプレートセット詳細情報取得
		if(detailInfo != null){
			//説明
			if(detailInfo.getDescription() != null){
				this.templateSetDetailDescriptionText.setText(HinemosMessage.replace(detailInfo.getDescription()));
			}
			
			//テンプレートID
			if (detailInfo.getTemplateId() != null) {
				this.templateIdCombo.setText(detailInfo.getTemplateId());
			}
			
			//タイトル名
			if (detailInfo.getTitleName() != null) {
				this.titleNameText.setText(HinemosMessage.replace(detailInfo.getTitleName()));
			}
		}
		this.update();
	}
	/**
	 * 入力値をTemplateSetDetailInfoListに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		createTemplateSetDetailInfo();
		return true;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.inputData = this.createTemplateSetDetailInfo();
		if (this.inputData != null) {
			return super.validate();
		} else {
			return m_validateResult;
		}
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
	 * テンプレートセット詳細ダイアログ
	 * その他項目を取得する。
	 *
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 * @return
	 */
	private List<String> getTemplateIdList(String managerNane, String ownerRoleId){
		
		List<String> templateIdList = new ArrayList<String>();
		
		//テンプレートIDの一覧をマネージャより取得
		try {
			ReportingRestClientWrapper wrapper = ReportingRestClientWrapper.getWrapper(managerName);
			TemplateIdListResponse listRes = wrapper.getTemplateIdList(ownerRoleId);
			templateIdList = listRes.getTemplateIdList();
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
				Messages.getString("message.accesscontrol.16"));
		} catch (HinemosUnknown  e) {
			//マルチマネージャ接続時にレポーティングが有効になってないマネージャの混在によりendpoint通信で異常が出る場合あり
			//(エンタープライズ機能が無効の場合は HinemosUnknownでラップしたUrlNotFoundとなる。)
			//この場合、その旨のダイアログを表示
			if(UrlNotFound.class.equals(e.getCause().getClass())) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.expiration.term") + ":"+managerName );
			} else {
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("update() getTemplateSetInfoList, " + errMessage, e);
				MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
			}
		} catch (Exception e) {
			// 上記以外の例外
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("update() getTemplateSetInfoList, " + errMessage, e);
			MessageDialog.openError(
				null,
				Messages.getString("failed"),
				Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}
		
		return templateIdList;
	}
}
