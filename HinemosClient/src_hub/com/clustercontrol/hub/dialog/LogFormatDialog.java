/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.dialog;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.hub.action.AddLog;
import com.clustercontrol.hub.action.GetLog;
import com.clustercontrol.hub.action.ModifyLog;
import com.clustercontrol.hub.composite.LogFormatKeyListComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.hub.LogFormat;
import com.clustercontrol.ws.hub.LogFormatKey;

/**
 * 
 *
 */
public class LogFormatDialog extends CommonDialog {

	// ログ
	private static Logger m_log = Logger.getLogger( LogFormatDialog.class );

	//フォーマットID
	private String formatId;

	protected Shell shell;
	private Text txtFormatId;
	private Text txtDescription;

	private Button btnAddKeyPattern;
	private Button btnModifyKeyPattern;
	private Button btnDelKeyPattern;
	private Button btnCopyKeyPattern;

	private Text txtTimestampRegex;
	private Text txtTimestampFormat;
	
	private ManagerListComposite managerListComposite;
	private RoleIdListComposite roleIdListComposite;
	private LogFormatKeyListComposite logFormatKeyListComposite;

	private int mode;
	private String managerName;
	private LogFormat m_LogFormat;
	
	/**
	 * @wbp.parser.constructor
	 */
	public LogFormatDialog(Shell parent, String managerName) {
		super(parent);
		this.managerName = managerName;
	}

	/**
	 * 
	 * @param parent
	 * @param managerName
	 * @param id
	 * @param mode
	 * mode = 0 ; new create, mode = 1 ; modify
	 */
	public LogFormatDialog(Shell parent, String managerName, String id, int mode) {
		super(parent);
		this.managerName = managerName;
		this.formatId = id;
		this.mode = mode;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(final Composite parent) {
		shell = this.getShell();
		parent.getShell().setText(Messages.getString("dialog.hub.log.format"));

		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		GridLayout baseLayout = new GridLayout(1, true);
		baseLayout.marginWidth = 10;
		baseLayout.marginHeight = 10;
		//一番下のレイヤー
		parent.setLayout(baseLayout);

		Composite logFormatComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		logFormatComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
//		gridData.heightHint = 664;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		logFormatComposite.setLayoutData(gridData);

		/** TOP Composite */
		Composite topComposite = new Composite(logFormatComposite, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite.heightHint = 121;
		topComposite.setLayoutData(gd_composite);
		topComposite.setLayout(new GridLayout(2, false));

		//マネージャ
		Label label = new Label(topComposite, SWT.LEFT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		label.setText(Messages.getString("facility.manager"));
		if( mode == PropertyDefineConstant.MODE_MODIFY ){
			managerListComposite = new ManagerListComposite(topComposite, SWT.NONE, false);
		} else {
			managerListComposite = new ManagerListComposite(topComposite, SWT.NONE, true);
			managerListComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Update 
					String managerName = managerListComposite.getText();
					roleIdListComposite.createRoleIdList(managerName);
				}
			});
		}
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		managerListComposite.setLayoutData(gridData);
		if( null != managerName ){
			managerListComposite.setText(managerName);
		}

		//フォーマットID
		Label labelFormatId = new Label(topComposite, SWT.LEFT);
		GridData gd_labelFormatId = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_labelFormatId.widthHint = 136;
		labelFormatId.setLayoutData(gd_labelFormatId);
		labelFormatId.setText(Messages.getString("hub.log.format.id"));
		txtFormatId = new Text(topComposite, SWT.BORDER);
		txtFormatId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtFormatId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//説明
		Label lblDescription = new Label(topComposite, SWT.LEFT);
		lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDescription.setText(Messages.getString("description"));
		txtDescription = new Text(topComposite, SWT.BORDER);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		// オーナーロールID
		Label labelRoleId = new Label(topComposite, SWT.LEFT);
		labelRoleId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		labelRoleId.setText(Messages.getString("owner.role.id"));
		if( this.mode == PropertyDefineConstant.MODE_MODIFY ){
			roleIdListComposite = 
					new RoleIdListComposite(topComposite, SWT.NONE, this.managerListComposite.getText(), false, Mode.OWNER_ROLE);
		}else{
			roleIdListComposite = 
					new RoleIdListComposite(topComposite, SWT.NONE, this.managerListComposite.getText(), true, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		roleIdListComposite.setLayoutData(gridData);

		//日時抽出
		Group timestampLayout = new Group(logFormatComposite, SWT.NONE);
		GridData gd_timestampLayout = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_timestampLayout.heightHint = 58;
		timestampLayout.setLayoutData(gd_timestampLayout);
		timestampLayout.setText(Messages.getString("dialog.hub.log.format.date.extraction"));
		timestampLayout.setLayout(new GridLayout(2, false));

		Label lblTimestampRegex = new Label(timestampLayout, SWT.NONE);
		GridData gd_lblTimestampRegex = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblTimestampRegex.widthHint = 144;
		lblTimestampRegex.setLayoutData(gd_lblTimestampRegex);
		lblTimestampRegex.setText(Messages.getString("dialog.hub.log.format.date.extraction.pattern"));

		txtTimestampRegex = new Text(timestampLayout, SWT.BORDER);
		GridData gd_txtTimestampRegex = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtTimestampRegex.widthHint = 237;
		txtTimestampRegex.setLayoutData(gd_txtTimestampRegex);

		 Label lblTimestampFormat = new Label(timestampLayout, SWT.NONE);
		GridData gd_lblTimestampFormat = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblTimestampFormat.widthHint = 113;
		lblTimestampFormat.setLayoutData(gd_lblTimestampFormat);
		lblTimestampFormat.setText(Messages.getString("dialog.hub.log.format.date.extraction.format"));

		txtTimestampFormat = new Text(timestampLayout, SWT.BORDER);
		GridData gd_txtTimestampFormat = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtTimestampFormat.widthHint = 296;
		txtTimestampFormat.setLayoutData(gd_txtTimestampFormat);
		txtTimestampFormat.setMessage("ex. yyyy-MM-dd HH:mm:ss");

		//メッセージパターン
		Group keypatLayout = new Group(logFormatComposite, SWT.NONE);
		keypatLayout.setLayout(new GridLayout(2, false));
		GridData gridData_1 = new GridData();
		gridData_1.grabExcessHorizontalSpace = true;
		gridData_1.horizontalAlignment = GridData.FILL;
		keypatLayout.setLayoutData(gridData_1);
		keypatLayout.setText(Messages.getString("hub.log.format.key.pattern"));

		logFormatKeyListComposite = new LogFormatKeyListComposite(keypatLayout, SWT.BORDER, managerName);
		GridData gd_logFormatKeyListComposite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_logFormatKeyListComposite.heightHint = 217;
		gd_logFormatKeyListComposite.widthHint = 393;
		logFormatKeyListComposite.setLayoutData(gd_logFormatKeyListComposite);

		Composite composite = new Composite(keypatLayout, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite.setLayout(new GridLayout(1, false));

		btnAddKeyPattern = new Button(composite, SWT.NONE);
		GridData gd_btnAddKeyPattern = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_btnAddKeyPattern.widthHint = 75;
		btnAddKeyPattern.setLayoutData(gd_btnAddKeyPattern);
		btnAddKeyPattern.setText(Messages.getString("add"));
		btnAddKeyPattern.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				
				LogKeyPatternDialog dialog = new LogKeyPatternDialog(
						shell, PropertyDefineConstant.MODE_ADD, logFormatKeyListComposite.getLogFormatKeyList());
				if (dialog.open() == IDialogConstants.OK_ID) {
					logFormatKeyListComposite.addLogFormatKeyList(dialog.getLogFormatKey());
					logFormatKeyListComposite.update();
				}
			}
		});

		btnModifyKeyPattern = new Button(composite, SWT.NONE);
		btnModifyKeyPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnModifyKeyPattern.setText(Messages.getString("modify"));
		btnModifyKeyPattern.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//選択したテーブルのキーを取得
				String order = logFormatKeyListComposite.getSelectionLogFormatKey();

				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				//キーをもとに、LogFormatKey情報をコンポジットのリストから検索する
				LogFormatKey logFormatKey = logFormatKeyListComposite.getLogFormatKeyListByKey(order);

				if (logFormatKey != null) {
					LogKeyPatternDialog dialog = new LogKeyPatternDialog(shell, 
							PropertyDefineConstant.MODE_MODIFY, 
							logFormatKeyListComposite.getLogFormatKeyList(),
							logFormatKey);
					if (dialog.open() == IDialogConstants.OK_ID) {
						logFormatKeyListComposite.getLogFormatKeyList().remove(logFormatKey);
						logFormatKeyListComposite.addLogFormatKeyList(dialog.getLogFormatKey());
						logFormatKeyListComposite.update();
					}
				}else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		btnDelKeyPattern = new Button(composite, SWT.NONE);
		btnDelKeyPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnDelKeyPattern.setText(Messages.getString("delete"));
		btnDelKeyPattern.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				//選択したテーブルのキーを取得
				String order = logFormatKeyListComposite.getSelectionLogFormatKey();
				//キーをもとに、LogFormatKey情報をコンポジットのリストから検索する
				LogFormatKey logFormatKey = logFormatKeyListComposite.getLogFormatKeyListByKey(order);
				String[] args = new String[1];
				args[0] = order;
				if (logFormatKey != null) {
					if (MessageDialog.openConfirm(
							null,
							Messages.getString("confirmed"),
							Messages.getString("message.hub.log.format.pattern.delete", args))) {
						logFormatKeyListComposite.getLogFormatKeyList().remove(logFormatKey);
						logFormatKeyListComposite.update();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});
		
		btnCopyKeyPattern = new Button(composite, SWT.NONE);
		btnCopyKeyPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnCopyKeyPattern.setText(Messages.getString("copy"));
		btnCopyKeyPattern.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//選択したテーブルのキーを取得
				String order = logFormatKeyListComposite.getSelectionLogFormatKey();

				if (order != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					//キーをもとに、LogFormatKey情報をコンポジットのリストから検索する
					LogFormatKey logFormatKey = logFormatKeyListComposite.getLogFormatKeyListByKey(order);

					if (logFormatKey != null) {
						LogKeyPatternDialog dialog = new LogKeyPatternDialog(shell, 
								PropertyDefineConstant.MODE_COPY, 
								logFormatKeyListComposite.getLogFormatKeyList(),
								logFormatKey);
						if (dialog.open() == IDialogConstants.OK_ID) {
							logFormatKeyListComposite.addLogFormatKeyList(dialog.getLogFormatKey());
							logFormatKeyListComposite.update();
							//logFormatKeyListComposite.setSelection();
						}
					}
				}else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});
		this.reflectLogFormat();
		update();
	}

	/**
	 * 
	 */
	public void update() {
		//フォーマットID
		if("".equals(this.txtFormatId.getText())){
			this.txtFormatId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.txtFormatId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
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
		//フォーマットID
		if ("".equals(txtFormatId.getText())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.hub.log.format.required.logformatid",
								new Object[]{Messages.getString("hub.log.format.id")}));
		}
		//オーナロールID
		if ("".equals(roleIdListComposite.getText())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.hub.log.format.required.owner.role.id",
							new Object[]{Messages.getString("owner.role.id")}));
		}
		return super.validate();
	}

	/**
	 * 無効な入力値の情報を設定します
	 *
	 */
	private ValidateResult createValidateResult(String id, String message) {
		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	/**
	 * ダイアログにログ[フォーマット]情報を反映します。
	 */
	private void reflectLogFormat() {
		// 初期表示
		
		LogFormat format = null;
		
		if (mode == PropertyDefineConstant.MODE_MODIFY
				|| mode == PropertyDefineConstant.MODE_COPY) {
			// 変更、コピーの場合、情報取得
			format = new GetLog().getLogFormat(this.managerName, this.formatId);
		} else {
			// 作成の場合
			format = new LogFormat();
		}
		
		this.m_LogFormat = format;
		//フォーマットID
		if (format.getLogFormatId() != null) {
			this.formatId = format.getLogFormatId();
			this.txtFormatId.setText(this.formatId);
			//ログ[フォーマット]定義変更の際には収集IDは変更不可
			if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				this.txtFormatId.setEnabled(false);
			}
		}
		//説明
		if (format.getDescription() != null){
			this.txtDescription.setText(format.getDescription());
		}

		if (format.getTimestampRegex() != null) {
			this.txtTimestampRegex.setText(format.getTimestampRegex());
		}
		
		if (format.getTimestampFormat() != null) {
			this.txtTimestampFormat.setText(format.getTimestampFormat());
		}
		
		this.logFormatKeyListComposite.setLogFormatKeyList(format.getKeyPatternList());
		
		// オーナーロールID取得
		if (format.getOwnerRoleId() != null) {
			this.roleIdListComposite.setText(format.getOwnerRoleId());
			//this.ownerRoleId = format.getOwnerRoleId();
		}
		this.update();
	}

	/**
	 * ダイアログの情報からログフォーマット設定情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see
	 */
	private void createLogFormat() {
		this.m_LogFormat = new LogFormat();
		this.m_LogFormat.setLogFormatId(this.txtFormatId.getText());
		this.m_LogFormat.setDescription(this.txtDescription.getText());
		this.m_LogFormat.setOwnerRoleId(this.roleIdListComposite.getText());
		
		this.m_LogFormat.setTimestampRegex(this.txtTimestampRegex.getText());
		this.m_LogFormat.setTimestampFormat(this.txtTimestampFormat.getText());
		
		if (this.logFormatKeyListComposite.getLogFormatKeyList() != null) {
			Logger.getLogger(this.getClass()).debug("Add LogCollectTarget : " +
					this.logFormatKeyListComposite.getLogFormatKeyList().size());
			for (LogFormatKey key : this.logFormatKeyListComposite.getLogFormatKeyList()) {
				this.m_LogFormat.getKeyPatternList().add(key);
			}
		}
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
		createLogFormat();
		LogFormat format = this.m_LogFormat;
		String managerName = this.managerListComposite.getText();
		
		if(format != null){
			if(mode == PropertyDefineConstant.MODE_ADD){
				// 作成の場合+
				result = new AddLog().addLogFormat(managerName, format);
			} 
			else if (mode == PropertyDefineConstant.MODE_MODIFY){
				// 変更の場合
				format.setLogFormatId(txtFormatId.getText());
				result = new ModifyLog().modifyLogFormat(managerName, format);
			} 
			else if (mode == PropertyDefineConstant.MODE_COPY){
				// コピーの場合
				format.setLogFormatId(txtFormatId.getText());
				result = new AddLog().addLogFormat(managerName, format);
			}
		} else {
			m_log.error("action() LogFormat is null");
		}
		return result;
	}
}
