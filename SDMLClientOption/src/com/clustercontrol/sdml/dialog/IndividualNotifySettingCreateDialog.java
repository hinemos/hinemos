/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.SdmlMonitorNotifyRelationResponse;
import org.openapitools.client.model.SdmlMonitorTypeMasterResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.composite.NotifyIdListComposite;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

public class IndividualNotifySettingCreateDialog extends CommonDialog {
	/** マネージャ */
	private String managerName = null;
	/** オーナーロールID */
	private String ownerRoleId = null;
	/** 入力値を保持するオブジェクト。 */
	private SdmlMonitorNotifyRelationResponse inputData = null;
	/** SDML監視種別 */
	private Combo sdmlMonitorTypeCombo = null;
	/** 選択可能なSDML監視種別のリスト */
	private Map<String, SdmlMonitorTypeMasterResponse> sdmlMonitorTypeMap = null;
	/** 通知ID */
	private NotifyIdListComposite notifyComposite = null;

	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult validateResult = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 * @param sdmlMonitorTypeMap
	 */
	public IndividualNotifySettingCreateDialog(Shell parent,
			Map<String, SdmlMonitorTypeMasterResponse> sdmlMonitorTypeMap, String managerName, String ownerRoleId) {
		super(parent);
		this.sdmlMonitorTypeMap = sdmlMonitorTypeMap;
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
		this.inputData = new SdmlMonitorNotifyRelationResponse();
	}

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 * @param sdmlMonitorTypeMap
	 * @param info
	 */
	public IndividualNotifySettingCreateDialog(Shell parent,
			Map<String, SdmlMonitorTypeMasterResponse> sdmlMonitorTypeMap, SdmlMonitorNotifyRelationResponse info,
			String managerName, String ownerRoleId) {
		super(parent);
		this.sdmlMonitorTypeMap = sdmlMonitorTypeMap;
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
		this.inputData = info;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();
		Label label = null;
		GridData gridData = null;

		// タイトル
		shell.setText(Messages.getString("dialog.sdml.individual.notify.create.modify"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		// SDML監視種別(ラベル)
		label = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.monitor.type") + " : ");
		// SDML監視種別(リスト)
		this.sdmlMonitorTypeCombo = new Combo(parent, SWT.CENTER | SWT.READ_ONLY);
		for (SdmlMonitorTypeMasterResponse type : this.sdmlMonitorTypeMap.values()) {
			String name = HinemosMessage.replace(type.getSdmlMonitorType());
			this.sdmlMonitorTypeCombo.add(name);
			this.sdmlMonitorTypeCombo.setData(name, type);
		}
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.sdmlMonitorTypeCombo.setLayoutData(gridData);
		this.sdmlMonitorTypeCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 通知ID Composite
		this.notifyComposite = new NotifyIdListComposite(parent, SWT.NONE, true);
		this.notifyComposite.setManagerName(this.managerName);
		this.notifyComposite.setOwnerRoleId(this.ownerRoleId, false);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.notifyComposite.setLayoutData(gridData);

		// ダイアログを調整
		this.adjustDialog(shell);

		setInputData();
		update();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(Shell shell) {
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(500, shell.getSize().y));

		// 画面中央に配置
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 */
	public void update() {
		// 必須項目を可視化
		if ("".equals(sdmlMonitorTypeCombo.getText())) {
			sdmlMonitorTypeCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			sdmlMonitorTypeCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * コンストラクタで受け取った情報の値を、各項目に設定します。
	 */
	protected void setInputData() {
		// SDML監視種別
		if (inputData.getSdmlMonitorTypeId() != null) {
			SdmlMonitorTypeMasterResponse type = sdmlMonitorTypeMap.get(inputData.getSdmlMonitorTypeId());
			if (type != null && type.getSdmlMonitorType() != null) {
				this.sdmlMonitorTypeCombo.setText(HinemosMessage.replace(type.getSdmlMonitorType()));
			}
		}

		// 通知ID
		if (inputData.getNotifyRelationList() != null && inputData.getNotifyRelationList().size() > 0) {
			List<NotifyRelationInfoResponse> notifyList = new ArrayList<>();
			for (NotifyRelationInfoResponse src : inputData.getNotifyRelationList()) {
				NotifyRelationInfoResponse dst = new NotifyRelationInfoResponse();
				dst.setNotifyId(src.getNotifyId());
				dst.setNotifyType(src.getNotifyType());
				notifyList.add(dst);
			}
			notifyComposite.setNotify(notifyList);
		}
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
	private SdmlMonitorNotifyRelationResponse createInputData() {
		SdmlMonitorNotifyRelationResponse info = new SdmlMonitorNotifyRelationResponse();
		// アプリケーションIDは登録時に設定する

		// SDML監視種別
		if (sdmlMonitorTypeCombo.getText() != null && !"".equals(sdmlMonitorTypeCombo.getText().trim())) {
			SdmlMonitorTypeMasterResponse type = (SdmlMonitorTypeMasterResponse) sdmlMonitorTypeCombo
					.getData(sdmlMonitorTypeCombo.getText());
			info.setSdmlMonitorTypeId(type.getSdmlMonitorTypeId());
		} else {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", new String[] { Messages.getString("sdml.monitor.type") }));
			return null;
		}

		// 通知ID
		List<NotifyRelationInfoResponse> notifyRelationList = info.getNotifyRelationList();
		if (notifyRelationList != null) {
			notifyRelationList.clear();
		}
		if (notifyComposite.getNotify() != null) {
			List<NotifyRelationInfoResponse> list = new ArrayList<>();
			for (NotifyRelationInfoResponse src : notifyComposite.getNotify()) {
				NotifyRelationInfoResponse dst = new NotifyRelationInfoResponse();
				dst.setNotifyId(src.getNotifyId());
				dst.setNotifyType(src.getNotifyType());
				list.add(dst);
			}
			info.setNotifyRelationList(list);
		}
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
		this.inputData = this.createInputData();

		if (this.inputData != null) {
			return super.validate();
		} else {
			return validateResult;
		}
	}

	public String getSdmlMonitorTypeId() {
		return this.inputData.getSdmlMonitorTypeId();
	}

	public SdmlMonitorNotifyRelationResponse getInputData() {
		return this.inputData;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id
	 *            ID
	 * @param message
	 *            メッセージ
	 */
	protected void setValidateResult(String id, String message) {
		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);
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
	 * 入力値の判定を行います。
	 *
	 * @return true：正常、false：異常
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		if (this.inputData != null) {
			result = true;
		}

		return result;
	}
}
