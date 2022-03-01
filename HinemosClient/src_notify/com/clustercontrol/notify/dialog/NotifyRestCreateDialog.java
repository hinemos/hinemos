/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.openapitools.client.model.RestAccessInfoResponse;
import org.openapitools.client.model.RestNotifyDetailInfoResponse;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

public class NotifyRestCreateDialog extends NotifyBasicCreateDialog {

	private static Log m_log = LogFactory.getLog( NotifyRestCreateDialog.class );

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY = 2;

	/** カラム数（チェックボックス）。 */
	private static final int WIDTH_CHECK = 2;

	/** カラム数（RESTアクセスID）。 */
	private static final int WIDTH_REST_ACCESS_ID = 11;

	// ----- instance フィールド ----- //

	/** 通知タイプ
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private final int TYPE_REST = 7;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** REST（重要度：通知） チェックボックス。 */
	private Button m_checkRestNormalInfo = null;
	/** REST（重要度：警告） チェックボックス。 */
	private Button m_checkRestNormalWarning = null;
	/** REST（重要度：危険） チェックボックス。 */
	private Button m_checkRestNormalCritical = null;
	/** REST（重要度：不明） チェックボックス。 */
	private Button m_checkRestNormalUnknown = null;

	/** RESTアクセスID（重要度：通知） テキスト。 */
	private Combo m_comboRestAccessIdInfo = null;
	/** RESTアクセスID（重要度：警告） テキスト。 */
	private Combo m_comboRestAccessIdWarning = null;
	/** RESTアクセスID（重要度：危険） テキスト。 */
	private Combo m_comboRestAccessIdCritical = null;
	/** RESTアクセスID（重要度：不明） テキスト。 */
	private Combo m_comboRestAccessIdUnknown = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyRestCreateDialog(Shell parent) {
		super(parent);
		parentDialog = this;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param managerName マネージャ名
	 * @param parent 親のシェルオブジェクト
	 * @param notifyId 変更する通知情報の通知ID
	 * @param updateFlg 更新フラグ（true:更新する）
	 */
	public NotifyRestCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
		parentDialog = this;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfoInputData)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfoInputData inputData;
		if(this.notifyId != null){
			inputData = new GetNotify().getRestNotify(this.managerName, this.notifyId);
		} else {
			inputData = new NotifyInfoInputData();
		}
		this.setInputData(inputData);
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
		shell.setText(Messages.getString("dialog.notify.rest.create.modify"));

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
		 * REST
		 */
		// RESTグループ
		Group groupRest = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupRest.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupRest.setLayoutData(gridData);
		groupRest.setText(Messages.getString("notifies.rest"));

		// 空行
		label = new Label(groupRest, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupRest, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（通知）
		label = new Label(groupRest, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（RESTアクセスID）
		label = new Label(groupRest, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_REST_ACCESS_ID;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("restaccess.id"));


		//　RESTアクセス　重要度：情報
		List<String> restIdList  = getRestAccessIdList();
		label = this.getLabelPriority(groupRest, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);
		this.m_checkRestNormalInfo = this.getCheckRest(groupRest);
		this.m_checkRestNormalInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_comboRestAccessIdInfo.setEnabled(m_checkRestNormalInfo.getSelection());
				update();
			}
		});
		this.m_comboRestAccessIdInfo = getComboRestAccessId(groupRest,restIdList);
		this.m_comboRestAccessIdInfo.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//　RESTアクセス　重要度：警告
		label = this.getLabelPriority(groupRest, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);
		this.m_checkRestNormalWarning = this.getCheckRest(groupRest);
		this.m_checkRestNormalWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_comboRestAccessIdWarning.setEnabled(m_checkRestNormalWarning.getSelection());
				update();
			}
		});
		this.m_comboRestAccessIdWarning = getComboRestAccessId(groupRest,restIdList);
		this.m_comboRestAccessIdWarning.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//　RESTアクセス　重要度：危険
		label = this.getLabelPriority(groupRest, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkRestNormalCritical = this.getCheckRest(groupRest);
		this.m_checkRestNormalCritical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_comboRestAccessIdCritical.setEnabled(m_checkRestNormalCritical.getSelection());
				update();
			}
		});
		this.m_comboRestAccessIdCritical =  getComboRestAccessId(groupRest,restIdList);
		this.m_comboRestAccessIdCritical.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//　RESTアクセス　重要度：不明
		label = this.getLabelPriority(groupRest, Messages.getString("unknown"),PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkRestNormalUnknown = this.getCheckRest(groupRest);
		this.m_checkRestNormalUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_comboRestAccessIdUnknown.setEnabled(m_checkRestNormalUnknown.getSelection());
				update();
			}
		});
		this.m_comboRestAccessIdUnknown = getComboRestAccessId(groupRest,restIdList);
		this.m_comboRestAccessIdUnknown.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// テキストボックスの有効/無効を初期化
		this.m_comboRestAccessIdInfo.setEnabled(this.m_checkRestNormalInfo.getSelection());
		this.m_comboRestAccessIdWarning.setEnabled(this.m_checkRestNormalWarning.getSelection());
		this.m_comboRestAccessIdCritical.setEnabled(this.m_checkRestNormalCritical.getSelection());
		this.m_comboRestAccessIdUnknown.setEnabled(this.m_checkRestNormalUnknown.getSelection());

		// 必須項目を明示
		// 情報
		if(this.m_checkRestNormalInfo.getSelection() && "".equals(this.m_comboRestAccessIdInfo.getText())){
			this.m_comboRestAccessIdInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboRestAccessIdInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 警告
		if(this.m_checkRestNormalWarning.getSelection() && "".equals(this.m_comboRestAccessIdWarning.getText())){
			this.m_comboRestAccessIdWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboRestAccessIdWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 危険
		if(this.m_checkRestNormalCritical.getSelection() && "".equals(this.m_comboRestAccessIdCritical.getText())){
			this.m_comboRestAccessIdCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboRestAccessIdCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 不明
		if(this.m_checkRestNormalUnknown.getSelection() && "".equals(this.m_comboRestAccessIdUnknown.getText())){
			this.m_comboRestAccessIdUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboRestAccessIdUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	@Override
	public NotifyInfoInputData getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfoInputData notify) {
		super.setInputData(notify);

		// コマンド情報
		RestNotifyDetailInfoResponse info = notify.getNotifyRestInfo();
		if (info != null) {
			this.setInputData(info);
		}

		// 必須入力項目を可視化
		this.update();
	}

	private void setInputData(RestNotifyDetailInfoResponse info) {
		
		Button[] checkRestNormals = new Button[] {
				this.m_checkRestNormalInfo,
				this.m_checkRestNormalWarning,
				this.m_checkRestNormalCritical,
				this.m_checkRestNormalUnknown
		};
		String[] restAccessIds = new String[] {
				info.getInfoRestAccessId(),
				info.getWarnRestAccessId(),
				info.getCriticalRestAccessId(),
				info.getUnknownRestAccessId()
		};
		Combo[] comboRestAccessIds = new Combo[] {
				this.m_comboRestAccessIdInfo,
				this.m_comboRestAccessIdWarning,
				this.m_comboRestAccessIdCritical,
				this.m_comboRestAccessIdUnknown
		};

		Boolean[] validFlgs = getValidFlgs(info);
		for (int i = 0; i < validFlgs.length; i++) {
			boolean valid = validFlgs[i].booleanValue();
			checkRestNormals[i].setSelection(valid);
			if (restAccessIds[i] != null) {
				comboRestAccessIds[i].setText(restAccessIds[i]);
			}
		}
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 */
	@Override
	protected NotifyInfoInputData createInputData() {
		NotifyInfoInputData info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_REST);

		// イベント情報
		RestNotifyDetailInfoResponse rest = createNotifyInfoDetail();
		info.setNotifyRestInfo(rest);

		return info;
	}

	private RestNotifyDetailInfoResponse createNotifyInfoDetail() {
		RestNotifyDetailInfoResponse info = new RestNotifyDetailInfoResponse();

		// 通知
		info.setInfoValidFlg(m_checkRestNormalInfo.getSelection());
		info.setWarnValidFlg(m_checkRestNormalWarning.getSelection());
		info.setCriticalValidFlg(m_checkRestNormalCritical.getSelection());
		info.setUnknownValidFlg(m_checkRestNormalUnknown.getSelection());

		// RESTアクセスID
		if (isNotNullAndBlank(m_comboRestAccessIdInfo.getText())) {
			info.setInfoRestAccessId(m_comboRestAccessIdInfo.getText());
		}else{
			info.setInfoRestAccessId(null);
		}
		if (isNotNullAndBlank(m_comboRestAccessIdWarning.getText())) {
			info.setWarnRestAccessId(m_comboRestAccessIdWarning.getText());
		}else{
			info.setWarnRestAccessId(null);
		}
		if (isNotNullAndBlank(m_comboRestAccessIdCritical.getText())) {
			info.setCriticalRestAccessId(m_comboRestAccessIdCritical.getText());
		}else{
			info.setCriticalRestAccessId(null);
		}
		if (isNotNullAndBlank(m_comboRestAccessIdUnknown.getText())) {
			info.setUnknownRestAccessId(m_comboRestAccessIdUnknown.getText());
		}else{
			info.setUnknownRestAccessId(null);
		}

		return info;
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

		NotifyInfoInputData info = this.getInputData();
		if(info != null){
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().addRestNotify(managerName, info);
			}
			else{
				// 変更の場合
				result = new ModifyNotify().modifyRestNotify(managerName, info);
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
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
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
	 * REST通知の実行チェックボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckRest(Composite parent) {
		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		return button;
	}

	private Boolean[] getValidFlgs(RestNotifyDetailInfoResponse info) {
		Boolean[] validFlgs = new Boolean[] {
				info.getInfoValidFlg(),
				info.getWarnValidFlg(),
				info.getCriticalValidFlg(),
				info.getUnknownValidFlg()
		};
		return validFlgs;
	}

	/**
	 * RESTアクセスID選択のコンボボックスを返します。
	 *
	 * @param parent 親のインスタンス
	 * @return 生成されたボックス
	 */
	private static Combo getComboRestAccessId(Composite parent , List<String> selectList ) {
		Combo idSelect = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_REST_ACCESS_ID;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		idSelect.setLayoutData(gridData);
		setComboSelectList(idSelect,selectList);
		return idSelect;
	}

	/**
	 * RESTアクセスID選択のコンボボックスに選択肢を設定します
	 *
	 * @param parent 親のインスタンス
	 * @return 生成されたボックス
	 */
	private static void setComboSelectList(Combo target , List<String> selectList ) {
		target.removeAll();
		target.add("");//ブランク
		for (String rec : selectList) {
			target.add(rec);
		}
		return;
	}
	/**
	 * RESTアクセスID選択のID一覧返します。
	 *
	 * @param parent 親のインスタンス
	 * @return 生成されたボックス
	 */
	private List<String> getRestAccessIdList( ) {
		List<String> list = new ArrayList<String>();

		try {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(this.managerName);
			List<RestAccessInfoResponse> listTmp = wrapper.getRestAccessInfoList(this.ownerRoleId );
			for (RestAccessInfoResponse info : listTmp) {
				list.add(info.getRestAccessId());
			}
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("update() getRestAccessIdList, " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return list;
	}

	private void updateComboSelectList(){
		Combo[] comboRestAccessIds = new Combo[] {
				this.m_comboRestAccessIdInfo,
				this.m_comboRestAccessIdWarning,
				this.m_comboRestAccessIdCritical,
				this.m_comboRestAccessIdUnknown
		};
		List<String > list = getRestAccessIdList();
		for(Combo target: comboRestAccessIds){
			setComboSelectList(target,list);
		}
		
	}
	@Override
	public void updateManagerName(String managerName) {
		super.updateManagerName(managerName);
		update();
		// マネージャの選択変更時に RESTアクセスIDの選択枝を同期して変更する。
		updateComboSelectList();
	}

	@Override
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		update();
		// オーナーロールの選択変更時に RESTアクセスIDの選択枝を同期して変更する。
		updateComboSelectList();
	}



	
}
