/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.dialog;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddInfraFileRequest;
import org.openapitools.client.model.InfraFileInfoResponse;
import org.openapitools.client.model.ModifyInfraFileRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InfraManagementDuplicate;
import com.clustercontrol.infra.composite.UploadComponent;
import com.clustercontrol.infra.util.InfraFileUtil;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.rest.JSON;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

public class InfraFileDialog extends CommonDialog {
	// ログ
	private static Log m_log = LogFactory.getLog(InfraFileDialog.class);
	/**
	 * ダイアログの最背面レイヤのカラム数 最背面のレイヤのカラム数のみを変更するとレイアウトがくずれるため、
	 * グループ化されているレイヤは全てこれにあわせる
	 */
	private final int DIALOG_WIDTH = 12;
	/** タイトルラベルのカラム数 */
	private final int TITLE_WIDTH = 4;
	/** テキストフォームのカラム数 */
	private final int FORM_WIDTH = 8;

	/** オーナーロールID用コンポジット（コンポ） */
	private RoleIdListComposite m_ownerRoleId = null;
	/** シェル */
	private Shell m_shell = null;

	/**
	 * アクションの種別 default -1
	 */
	private int mode = PropertyDefineConstant.MODE_ADD;
	/** マネージャ名 */
	private String managerName = null;

	/** マネージャリスト用コンポジット */
	private ManagerListComposite m_managerComposite = null;
	private Text m_fileId;
	private UploadComponent uploadComponent;
	private InfraFileInfoResponse m_infraFileInfo;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親シェル
	 */
	public InfraFileDialog(Shell parent) {
		super(parent);
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親シェル
	 */
	public InfraFileDialog(Shell parent, String managerName, int mode, InfraFileInfoResponse infraFileInfo) {
		super(parent);
		this.managerName = managerName;
		this.mode = mode;
		this.m_infraFileInfo = infraFileInfo;
	}

	/**
	 * ダイアログエリアを生成します。
	 * <P>
	 *
	 *
	 * @param parent
	 *            親コンポジット
	 *
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();
		parent.getShell().setText(Messages.getString("dialog.infra.file.create.modify"));
		/**
		 * レイアウト設定 ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		GridLayout baseLayout = new GridLayout(1, true);
		baseLayout.marginWidth = 10;
		baseLayout.marginHeight = 10;
		baseLayout.numColumns = DIALOG_WIDTH;
		// 一番下のレイヤー
		parent.setLayout(baseLayout);

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = DIALOG_WIDTH;

		GridData gridData = null;

		Composite infraInfoComposite = new Composite(parent, SWT.NONE);
		infraInfoComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		infraInfoComposite.setLayoutData(gridData);

		// マネージャ
		Label label = new Label(infraInfoComposite, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", label);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.manager") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
			this.m_managerComposite = new ManagerListComposite(infraInfoComposite, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(infraInfoComposite, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					m_ownerRoleId.createRoleIdList(managerName);
				}
			});
		}
		WidgetTestUtil.setTestId(this, "managerComposite", m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);
		if (null != this.managerName) {
			this.m_managerComposite.setText(this.managerName);
		}

		// ファイルID
		Label fileIdTitle = new Label(infraInfoComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileIdTitle.setLayoutData(gridData);
		fileIdTitle.setText(Messages.getString("infra.filemanager.file.id") + " : ");

		m_fileId = new Text(infraInfoComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_fileId.setLayoutData(gridData);

		m_fileId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		m_fileId.addModifyListener(new ChangeBackgroundModifyListener());

		if (mode == PropertyDefineConstant.MODE_MODIFY) {
			m_fileId.setEnabled(false);
			m_fileId.setText(m_infraFileInfo.getFileId());
		}

		uploadComponent = new UploadComponent(infraInfoComposite,
				Messages.getString("infra.filemanager.file.name") + " : ", TITLE_WIDTH, FORM_WIDTH);
		if (mode == PropertyDefineConstant.MODE_MODIFY) {
			uploadComponent.setFileName(m_infraFileInfo.getFileName());
		}

		// オーナーロールID
		Label labelRoleId = new Label(infraInfoComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if (mode == PropertyDefineConstant.MODE_MODIFY) {
			m_ownerRoleId = new RoleIdListComposite(infraInfoComposite, SWT.NONE, this.m_managerComposite.getText(),
					false, Mode.OWNER_ROLE);
		} else {
			m_ownerRoleId = new RoleIdListComposite(infraInfoComposite, SWT.NONE, this.m_managerComposite.getText(),
					true, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_ownerRoleId.setLayoutData(gridData);

		if (mode == PropertyDefineConstant.MODE_MODIFY) {
			m_ownerRoleId.setEnabled(false);
			m_ownerRoleId.setText(m_infraFileInfo.getOwnerRoleId());
		}

		m_shell.pack();
		m_shell.setSize(new Point(540, m_shell.getSize().y));

		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 * @since 5.0.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 * @since 5.0.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
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
		if ("".equals(m_fileId.getText())) {
			return createValidateResult(Messages.getString("message.hinemos.1"), Messages.getString(
					"message.infra.specify.item", new Object[] { Messages.getString("infra.filemanager.file.id") }));
		}
		if ("".equals(uploadComponent.getFileName())) {
			return createValidateResult(Messages.getString("message.hinemos.1"), Messages.getString(
					"message.infra.specify.item", new Object[] { Messages.getString("infra.filemanager.file.name") }));
		}
		if (null == uploadComponent.getFilePath()) {
			return createValidateResult(Messages.getString("message.hinemos.1"), Messages.getString(
					"message.infra.specify.item", new Object[] { Messages.getString("infra.filemanager.file.new") }));
		}

		if (ClusterControlPlugin.isRAP()) {
			if (!uploadComponent.isReady()) {
				return createValidateResult(Messages.getString("upload"), Messages.getString("upload.busy.message"));
			}
		}
		return super.validate();
	}

	/**
	 * 無効な入力値の情報を設定します
	 *
	 */
	protected ValidateResult createValidateResult(String id, String message) {
		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	/**
	 * ダイアログの情報から、ファイル情報を作成します。
	 *
	 */
	protected InfraFileInfoResponse createInputData() {
		InfraFileInfoResponse info = new InfraFileInfoResponse();

		info.setFileId(m_fileId.getText());
		info.setFileName(uploadComponent.getFileName());
		info.setOwnerRoleId(m_ownerRoleId.getText());

		return info;
	}

	@Override
	protected boolean action() {
		boolean result = false;
		InfraFileInfoResponse info = createInputData();
		String action = Messages.getString("create");
		if (mode == PropertyDefineConstant.MODE_MODIFY) {
			action = Messages.getString("modify");
		}
		String errMsg = null;
		String managerName = this.m_managerComposite.getText();
		InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
		try {
			long fileSize = new File(uploadComponent.getFilePath()).length();
			int infraMaxFileSize = getInfraMaxFileSize();
			if (fileSize > infraMaxFileSize) {
				InfraFileUtil.showFailureDialog(action, Messages.getString("message.infra.too.large.file",
						new Object[] { fileSize, infraMaxFileSize }));
				return result;
			}

			if (mode == PropertyDefineConstant.MODE_ADD) {
				AddInfraFileRequestEx dtoReqEx = new AddInfraFileRequestEx();
				RestClientBeanUtil.convertBean(info, dtoReqEx);
				wrapper.addInfraFile(new File(uploadComponent.getFilePath()), dtoReqEx);
			} else {
				String fileId = info.getFileId();
				ModifyInfraFileRequestEx dtoReqEx = new ModifyInfraFileRequestEx();
				RestClientBeanUtil.convertBean(info, dtoReqEx);
				wrapper.modifyInfraFile(fileId, new File(uploadComponent.getFilePath()), dtoReqEx);
			}
			result = true;
		} catch (InfraManagementDuplicate e) {
			m_log.warn("action() modifyInfraFile, " + e.getMessage());
			errMsg = Messages.getString("message.infra.file.duplicate", new String[] { m_fileId.getText() });
		} catch (Exception e) {
			m_log.error("action() modifyInfraFile, " + e.getMessage());
			errMsg = HinemosMessage.replace(e.getMessage());
		} finally {
			uploadComponent.cleanup();
		}

		if (result) {
			InfraFileUtil.showSuccessDialog(action, m_fileId.getText() + "(" + managerName + ")");
		} else {
			String extraArg = m_fileId.getText() + "(" + managerName + ")";
			if (errMsg != null) {
				extraArg += "\n" + errMsg;
			}
			InfraFileUtil.showFailureDialog(action, extraArg);
		}
		return result;
	}

	private int getInfraMaxFileSize() {
		int infraMaxFileSize = 1024 * 1024 * 64; //64MB
		
		CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(this.m_managerComposite.getText());
		try {
			String value = wrapper.getInfraMaxFileSize().getValue();
			infraMaxFileSize = Integer.parseInt(value);
		} catch (Exception e) {
			m_log.warn("getInfraMaxFileSize() getHinemosProperty, " + e.getClass().getSimpleName() + ", "
					+ e.getMessage());
		}
		return infraMaxFileSize;
	}
	
	// ファイルアップロード用のDTO
	private static class AddInfraFileRequestEx extends AddInfraFileRequest {
		@Override
		public String toString() {
			return new JSON().serialize(this);
		}
	}
	
	private static class ModifyInfraFileRequestEx extends ModifyInfraFileRequest {
		@Override
		public String toString() {
			return new JSON().serialize(this);
		}
	}
}