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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.CloudNotifyLinkInfoKeyValueObjectResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.composite.AbstractNotifyCloudLinkInfoComposite;
import com.clustercontrol.notify.composite.NotifyCloudLinkInfoAWSComposite;
import com.clustercontrol.notify.composite.NotifyCloudLinkInfoAzureComposite;
import com.clustercontrol.notify.composite.NotifyCloudLinkInfoGCPComposite;
import com.clustercontrol.util.Messages;

/**
 * 連携情報ダイアログクラス<BR>
 */
public class NotifyCloudLinkInfoDialog extends CommonDialog {

	private static Log m_log = LogFactory.getLog(NotifyCloudLinkInfoDialog.class);
	private boolean isAWS = false;
	private boolean isGCP = false;

	// 連携情報の一時保存用
	private boolean hasSetting = false;
	private int priority;
	private String eventBus;
	private String accessKey;
	private String dataVersion;
	private String detailType;
	private List<CloudNotifyLinkInfoKeyValueObjectResponse> dataList;
	private String source;

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public NotifyCloudLinkInfoDialog(Shell parent, boolean isAWS, boolean isGCP) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.isAWS = isAWS;
		this.isGCP = isGCP;
	}

	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 800;
	private static final int sizeY = 450;

	/** 連携情報 コンポジット*/
	private AbstractNotifyCloudLinkInfoComposite eventGroup = null;

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see com.clustercontrol.notify.composite.NotifyListComposite
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.cloud.link.info"));

		// レイアウト
		GridLayout layout = new GridLayout(8, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 8;
		parent.setLayout(layout);

		/*
		 * クラウド通知
		 */
		if (isAWS) {
			// awsの場合
			eventGroup = new NotifyCloudLinkInfoAWSComposite(parent, SWT.NONE);
		} else if (isGCP) {
			// GCPの場合
			eventGroup = new NotifyCloudLinkInfoGCPComposite(parent, SWT.NONE);
		}else {
			// azureの場合
			eventGroup = new NotifyCloudLinkInfoAzureComposite(parent, SWT.NONE);
		}
		eventGroup.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		eventGroup.setLayoutData(gridData);

		// 既に連携情報が設定されている場合のみコンポジットに反映
		if (hasSetting) {
			refrectLinkInfoData();
		}
		
		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(sizeX, sizeY));

	}

	// コンポジットのテキストを設定に反映
	protected void setInputData() {
		eventGroup.setInputData();
	}

	// 設定済み連携情報を一時保存
	public void setLinkInfoData(int priority, String eventBus, String accessKey, String dataVersion, String detailType,
			List<CloudNotifyLinkInfoKeyValueObjectResponse> dataList, String source) {

		this.priority = priority;
		this.eventBus = eventBus;
		this.accessKey = accessKey;
		this.dataVersion = dataVersion;
		this.detailType = detailType;
		// deep copy
		this.dataList = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>(dataList);
		this.source = source;
		hasSetting = true;

	}

	// 一時保存した連携情報をコンポジットに反映
	private void refrectLinkInfoData() {
		try {
			eventGroup.setLinkInfoData(priority, eventBus, accessKey, dataVersion, detailType, dataList, source);
			eventGroup.reflectInputData();

		} catch (Exception e) {
			m_log.error("refrectLinkInfoData(): ", e);
		}
	}

	public AbstractNotifyCloudLinkInfoComposite getComposite() {
		return eventGroup;
	}

	public boolean isAWS() {
		return this.isAWS;
	}
	
	public boolean isGCP() {
		return this.isGCP;
	}

	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		setInputData();
		//To Validate GCP Ordering Key
		if (eventGroup.isValidate()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.ordering.key"));
			return result;
		}
		return super.validate();

	}

	/**
	 * ボタンを生成します。<BR>
	 * 閉じるボタンを生成します。
	 *
	 * @param parent
	 *            ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// 閉じるボタン
		this.createButton(parent, IDialogConstants.OK_ID, Messages.getString("ok"), false);

		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);

	}
}
