/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.dialog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.hub.KeyType;
import com.clustercontrol.ws.hub.LogFormatKey;
import com.clustercontrol.ws.hub.ValueType;

public class LogKeyPatternDialog extends CommonDialog {
	private Text txtKey;
	private Text txtDescription;
	private Text txtMsgPattern;
	private Text txtMetaPattern;
	private Text txtValue;
	private Combo cmbValueType;
	private Button btnMsg;
	private Button btnMeta;

	private LogFormatKey m_logFormatKey;
	private int mode;
	private List<LogFormatKey> keys;
	/**
	 * コンストラクタ
	 * @param parent
	 * @wbp.parser.constructor
	 */
	public LogKeyPatternDialog(Shell parent, int mode, List<LogFormatKey> keys) {
		super(parent);
		this.mode = mode;
		this.m_logFormatKey = new LogFormatKey();
		this.keys = keys;
	}
	/**
	 * コンストラクタ
	 * @param parent
	 * @param managerName
	 * @param ownerRoleId
	 * @param logFormatKey
	 */
	public LogKeyPatternDialog(Shell parent, int mode, List<LogFormatKey> keys, LogFormatKey logFormatKey) {
		super(parent);
		this.mode = mode;
		this.m_logFormatKey = logFormatKey;
		this.keys = keys;
	}
	
	public LogFormatKey getLogFormatKey(){
		return this.m_logFormatKey;
	}

	public void setLogFormatKey(LogFormatKey inputData) {
		this.m_logFormatKey = inputData;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		parent.getShell().setText(Messages.getString("dialog.hub.log.format.key.pattern"));
		parent.setLayout(new GridLayout(2, false));

		// 共通用
		GridData griddata;

		//キー
		Label lblKey = new Label(parent, SWT.LEFT);
		griddata = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		griddata.widthHint = 106;
		lblKey.setLayoutData(griddata);
		lblKey.setText(Messages.getString("hub.log.format.key"));
		this.txtKey = new Text(parent, SWT.BORDER);
		griddata = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		griddata.widthHint = 480;
		this.txtKey.setLayoutData(griddata);
		this.txtKey.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.txtKey.setTextLimit(64);
		this.txtKey.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				String text = e.text;
				if (e.character == SWT.BS || e.character == SWT.DEL){
					return;
				}
				if (e.text.equals("")) {
					return;
				}
				if (!text.matches("^[A-Za-z0-9-_.@]+$")){
					e.doit = false;
				}
			}
		});

		//ラベル
		Label lblDescription = new Label(parent, SWT.LEFT);
		lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDescription.setText(Messages.getString("description"));

		//テキスト
		this.txtDescription = new Text(parent, SWT.BORDER);
		this.txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblValueType = new Label(parent, SWT.NONE);
		lblValueType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValueType.setText(Messages.getString("dialog.hub.log.format.key.pattern.value.type"));

		this.cmbValueType = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.cmbValueType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		for (ValueType valueType : ValueType.values()) {
			String disp = getDispValue(valueType);
			this.cmbValueType.add(disp);
			this.cmbValueType.setData(disp, valueType);
		}
		cmbValueType.setText(getDispValue(ValueType.STRING));

		// ラジオグループ
		Group compositeMsgOrMeta = new Group(parent, SWT.NONE);
		GridData gd_compositeMsgOrMeta = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_compositeMsgOrMeta.horizontalSpan = 2;
		compositeMsgOrMeta.setLayoutData(gd_compositeMsgOrMeta);
		compositeMsgOrMeta.setLayout(new GridLayout(2, false));

		this.btnMsg = new Button(compositeMsgOrMeta, SWT.RADIO);
		this.btnMsg.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		this.btnMsg.setText(Messages.getString("dialog.hub.log.format.key.pattern.message"));
		this.btnMsg.setSelection(true);
		this.btnMsg.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		Label lblMsgPattern = new Label(compositeMsgOrMeta, SWT.LEFT);
		griddata = new GridData();
		griddata.horizontalIndent = 20;
		griddata.widthHint = 145;
		lblMsgPattern.setLayoutData(griddata);
		lblMsgPattern.setText(Messages.getString("hub.log.format.key.pattern.regex"));

		this.txtMsgPattern = new Text(compositeMsgOrMeta, SWT.BORDER);
		this.txtMsgPattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.txtMsgPattern.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		this.btnMeta = new Button(compositeMsgOrMeta, SWT.RADIO);
		this.btnMeta.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		this.btnMeta.setText(Messages.getString("dialog.hub.log.format.key.pattern.meta"));
		this.btnMeta.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		//パターン
		Label lblMetaPattern = new Label(compositeMsgOrMeta, SWT.LEFT);
		griddata = new GridData();
		griddata.horizontalIndent = 20;
		lblMetaPattern.setLayoutData(griddata);
		lblMetaPattern.setText(Messages.getString("hub.log.format.key.pattern.regex"));
		this.txtMetaPattern = new Text(compositeMsgOrMeta, SWT.BORDER);
		this.txtMetaPattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.txtMetaPattern.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//バリュー
		Label lblValue = new Label(compositeMsgOrMeta, SWT.LEFT);
		griddata.horizontalIndent = 20;
		lblValue.setLayoutData(griddata);
		lblValue.setText(Messages.getString("hub.log.format.key.value"));
		this.txtValue = new Text(compositeMsgOrMeta, SWT.BORDER);
		this.txtValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.reflectLogFormatKey();
		this.update();
	}
	
	/**
	 * 
	 */
	public void update() {
		if("".equals(this.txtKey.getText())){
			this.txtKey.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.txtKey.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.cmbValueType.getText())){
			this.cmbValueType.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.cmbValueType.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.btnMsg.getSelection()) {
			this.txtMsgPattern.setEnabled(true);
			this.txtMetaPattern.setEnabled(false);
			this.txtValue.setEnabled(false);
//			this.txtMetaPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//			if("".equals(this.txtMsgPattern.getText())){
//				this.txtMsgPattern.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
//			}else{
//				this.txtMsgPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//			}
		} else {
			this.txtMsgPattern.setEnabled(false);
			this.txtMetaPattern.setEnabled(true);
			this.txtValue.setEnabled(true);
//			this.txtMsgPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//			if("".equals(this.txtMetaPattern.getText())){
//				this.txtMetaPattern.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
//			}else{
//				this.txtMetaPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//			}
		}
	}

	/**
	 * 
	 */
	private void createLogFormatKey(){
		this.m_logFormatKey = new LogFormatKey();
		this.m_logFormatKey.setKey(this.txtKey.getText());
		this.m_logFormatKey.setDescription(this.txtDescription.getText());
		if (this.cmbValueType.getText() != null) {
			this.m_logFormatKey.setValueType((ValueType)this.cmbValueType.getData(this.cmbValueType.getText()));
		}
		if (btnMsg.getSelection()) {
			this.m_logFormatKey.setKeyType(KeyType.PARSING);
			this.m_logFormatKey.setPattern(this.txtMsgPattern.getText());
			this.m_logFormatKey.setValue(null);
		} else {
			this.m_logFormatKey.setKeyType(KeyType.FIXED);
			this.m_logFormatKey.setPattern(this.txtMetaPattern.getText());
			this.m_logFormatKey.setValue(this.txtValue.getText());
		}
	}

	/**
	 * 
	 */
	private void reflectLogFormatKey() {
		if (this.m_logFormatKey.getKey() != null) {
			this.txtKey.setText(this.m_logFormatKey.getKey());
			if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				this.txtKey.setEnabled(false);
			}
		}
		if (this.m_logFormatKey.getDescription() != null) {
			this.txtDescription.setText(this.m_logFormatKey.getDescription());
		}
		if (this.m_logFormatKey.getValueType() == null) {
			this.cmbValueType.select(0);
		} else {
			this.cmbValueType.setText(getDispValue(this.m_logFormatKey.getValueType()));
		}
		if (this.m_logFormatKey.getKeyType() == null || this.m_logFormatKey.getKeyType() == KeyType.PARSING) {
			this.btnMsg.setSelection(true);
			this.btnMeta.setSelection(false);
		} else {
			this.btnMsg.setSelection(false);
			this.btnMeta.setSelection(true);
		}
		if (this.m_logFormatKey.getPattern() != null) {
			if (this.m_logFormatKey.getKeyType() == KeyType.PARSING) {
				this.txtMsgPattern.setText(this.m_logFormatKey.getPattern());
			} else {
				this.txtMetaPattern.setText(this.m_logFormatKey.getPattern());
			}
		}
		if (this.m_logFormatKey.getValue() != null) {
			this.txtValue.setText(this.m_logFormatKey.getValue());
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
		//キー
		if ("".equals(txtKey.getText())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.hub.log.format.required.key",
								new Object[]{Messages.getString("hub.log.format.key")}));
		}
		//キー重複チェック 変更時はチェックしない
		if (mode != PropertyDefineConstant.MODE_MODIFY) {
			Set<String> set = new HashSet<String>();
			for (LogFormatKey key : keys) {
				set.add(key.getKey());
			}
			if (set.contains(txtKey.getText())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.hub.log.format.duplication.key",
								new Object[]{Messages.getString("hub.log.format.key")}));
			} else {
				set.add(txtKey.getText());
			}
		}
		//バリュータイプ
		if ("".equals(cmbValueType.getText())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.hub.log.format.required.value.type",
							new Object[]{Messages.getString("dialog.hub.log.format.key.pattern.value.type")}));
		}

//		if (this.btnMsg.getSelection()) {
//			this.txtMetaPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//			if("".equals(this.txtMsgPattern.getText())){
//				this.txtMsgPattern.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
//			}else{
//				this.txtMsgPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//			}
//		} else {
//			this.txtMsgPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//			if("".equals(this.txtMetaPattern.getText())){
//				this.txtMetaPattern.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
//			}else{
//				this.txtMetaPattern.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//			}
//		}
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
	 * OKボタン押下時のアクション
	 * @return
	 */
	public boolean action() {
		createLogFormatKey();
		return true;
	}

	private String getDispValue(ValueType type){
		String dispValue=null;
		switch(type){
			case NUMBER:
				dispValue=Messages.getString("dialog.hub.log.format.key.type.number");
				break;
			case STRING:
				dispValue=Messages.getString("dialog.hub.log.format.key.type.string");
				break;
			case BOOL:
				dispValue=Messages.getString("dialog.hub.log.format.key.type.bool");
				break;
		}
		return dispValue;
	}
}
