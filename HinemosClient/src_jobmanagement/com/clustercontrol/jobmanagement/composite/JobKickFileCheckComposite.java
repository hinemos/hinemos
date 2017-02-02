/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * ジョブ実行契機の作成・変更ダイアログのスケジュールタブ用の
 * コンポジットクラスです。
 *
 * @version 5.1.0
 */
public class JobKickFileCheckComposite extends Composite {

	/** シェル */
	private Shell m_shell = null;

	/** マネージャ名 */
	private String m_managerName = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** 選択されたスコープのファシリティID。 */
	private String m_facilityId = null;

	/** スコープ用テキスト */
	private Text m_txtScope = null;
	/** スコープ参照用ボタン */
	private Button m_btnScopeSelect = null;
	/** ファイルパス */
	private Text m_txtDirectory = null;
	/** ファイル名 */
	private Text m_txtFileName = null;
	/** ファイルチェック種別 - 作成 */
	private Button m_btnTypeCreate = null;
	/** ファイルチェック種別 - 削除 */
	private Button m_btnTypeDelete = null;
	/** ファイルチェック種別 - 変更 */
	private Button m_btnTypeModify = null;
	/** 変更種別 - タイムスタンプ*/
	private Button m_btnTypeTimeStamp = null;
	/** 変更種別 - ファイルサイズ*/
	private Button m_btnTypeFileSize = null;


	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public JobKickFileCheckComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.m_shell = this.getShell();
		Label label = null;

		this.setLayout(JobDialogUtil.getParentLayout());

		// ファイルチェック設定（Composite）
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new RowData());
		((RowData)composite.getLayoutData()).width = 525;

		// スコープ（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("scope") + " : ");

		// スコープ（テキスト）
		this.m_txtScope =  new Text(composite, SWT.READ_ONLY | SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtScope", this.m_txtScope);
		this.m_txtScope.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// スコープ参照（ボタン）
		this.m_btnScopeSelect = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnScopeSelect", this.m_btnScopeSelect);
		this.m_btnScopeSelect.setLayoutData(new GridData(40, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnScopeSelect.setText(Messages.getString("refer"));
		this.m_btnScopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem selectItem = dialog.getSelectItem();
					FacilityInfo info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_facilityId = info.getFacilityId();
					m_txtScope.setText(path.getPath(selectItem));
					update();
				}
			}
		});

		// ディレクトリ（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("file.check.directory") + " : ");
		
		// ディレクトリ（テキスト）
		this.m_txtDirectory = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtDirectory", this.m_txtDirectory);
		this.m_txtDirectory.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		this.m_txtDirectory.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtDirectory.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// ファイル名（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("file.name") + "(" + Messages.getString("regex") + ") : ");

		// ファイル名（テキスト）
		this.m_txtFileName = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtFileName", this.m_txtFileName);
		this.m_txtFileName.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_txtFileName.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtFileName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// separator
		JobDialogUtil.getSeparator(this);

		// ファイルチェック種別（グループ）
		Group checkGroup = new Group(composite, SWT.NONE);
		checkGroup.setLayoutData(new GridData());
		((GridData)checkGroup.getLayoutData()).horizontalSpan = 3;
		checkGroup.setLayout(new GridLayout(2, false));
		checkGroup.setText(Messages.getString("file.check.type")+ " : ");

		// ファイルチェック種別：作成（ラジオボタン）
		this.m_btnTypeCreate = new Button(checkGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_btnTypeCreate", this.m_btnTypeCreate);
		this.m_btnTypeCreate.setText(Messages.getString("create"));
		this.m_btnTypeCreate.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_btnTypeCreate.getLayoutData()).verticalAlignment = SWT.BEGINNING; 
		this.m_btnTypeCreate.setSelection(true);
		this.m_btnTypeCreate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// dummy
		new Label(checkGroup, SWT.NONE);

		// ファイルチェック種別：削除（ラジオボタン）
		this.m_btnTypeDelete = new Button(checkGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_btnTypeDelete", this.m_btnTypeDelete);
		this.m_btnTypeDelete.setText(Messages.getString("delete"));
		this.m_btnTypeDelete.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_btnTypeDelete.getLayoutData()).verticalAlignment = SWT.BEGINNING; 
		this.m_btnTypeDelete.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// dummy
		new Label(checkGroup, SWT.NONE);

		// ファイルチェック種別：変更（ラジオボタン）
		this.m_btnTypeModify = new Button(checkGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_btnTypeModify", this.m_btnTypeModify);
		this.m_btnTypeModify.setText(Messages.getString("modify"));
		this.m_btnTypeModify.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_btnTypeModify.getLayoutData()).verticalAlignment = SWT.BEGINNING; 
		this.m_btnTypeModify.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 変更：種別（Composite）
		Composite modifyTypeComposite = new Composite(checkGroup, SWT.BORDER);
		modifyTypeComposite.setLayout(new GridLayout(2, false));

		// 変更：タイムスタンプ変更（ラジオボタン）
		this.m_btnTypeTimeStamp = new Button(modifyTypeComposite, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_btnTypeTimeStamp", this.m_btnTypeTimeStamp);
		this.m_btnTypeTimeStamp.setText(Messages.getString("file.check.type.modify.timestamp"));
		this.m_btnTypeTimeStamp.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_btnTypeTimeStamp.getLayoutData()).horizontalAlignment = GridData.CENTER;
		this.m_btnTypeTimeStamp.setSelection(true);
		this.m_btnTypeTimeStamp.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 変更：ファイルサイズ変更（ラジオボタン）
		this.m_btnTypeFileSize = new Button(modifyTypeComposite, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_btnTypeFileSize", this.m_btnTypeFileSize);
		this.m_btnTypeFileSize.setText(Messages.getString("file.check.type.modify.file.size"));
		this.m_btnTypeFileSize.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_btnTypeFileSize.getLayoutData()).horizontalAlignment = GridData.CENTER;
		this.m_btnTypeFileSize.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 初期設定
		this.m_btnTypeCreate.setSelection(true);
		this.m_btnTypeTimeStamp.setSelection(true);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		//スコープ
		if("".equals(this.m_txtScope.getText())){
			this.m_txtScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//ファイルパス
		if("".equals(this.m_txtDirectory.getText())){
			this.m_txtDirectory.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtDirectory.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//ファイル名
		if("".equals(this.m_txtFileName.getText())){
			this.m_txtFileName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtFileName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// ファイルチェック種別
		this.m_btnTypeTimeStamp.setEnabled(this.m_btnTypeModify.getSelection());
		this.m_btnTypeFileSize.setEnabled(this.m_btnTypeModify.getSelection());
	}

	/**
	 * ジョブファイルチェック情報をコンポジットに反映します。
	 * 
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 * @param facilityId ファシリティID
	 * @param scope スコープ
	 * @param directory ディレクトリ
	 * @param fileName ファイル名
	 * @param eventType ファイルチェック種別
	 * @param modifyType 変更種別
	 */
	public void setJobFileCheck(
			String managerName,
			String ownerRoleId,
			String facilityId,
			String scope,
			String directory,
			String fileName,
			Integer eventType,
			Integer modifyType
			) {

		// マネージャ名
		this.m_managerName = managerName;
		// オーナーロールID
		this.m_ownerRoleId = ownerRoleId;

		//スコープ
		if(facilityId != null){
			this.m_facilityId = facilityId;
			this.m_txtScope.setText(HinemosMessage.replace(scope));
		}
		//ファイルパス
		if(directory != null){
			this.m_txtDirectory.setText(directory);
		}
		//ファイル名
		if(fileName != null){
			this.m_txtFileName.setText(fileName);
		}

		//ファイルチェック種別
		if(eventType != null){
			switch(eventType){
			case FileCheckConstant.TYPE_CREATE :
				//作成の場合
				this.m_btnTypeCreate.setSelection(true);
				this.m_btnTypeDelete.setSelection(false);
				this.m_btnTypeModify.setSelection(false);
				break;
			case FileCheckConstant.TYPE_DELETE :
				//削除の場合
				this.m_btnTypeCreate.setSelection(false);
				this.m_btnTypeDelete.setSelection(true);
				this.m_btnTypeModify.setSelection(false);
				break;
			case FileCheckConstant.TYPE_MODIFY :
				//変更の場合
				this.m_btnTypeCreate.setSelection(false);
				this.m_btnTypeDelete.setSelection(false);
				this.m_btnTypeModify.setSelection(true);
				if(modifyType == null){
					break;
				}
				if(modifyType
						== FileCheckConstant.TYPE_MODIFY_TIMESTAMP){
					//変更 - タイムスタンプの場合
					this.m_btnTypeTimeStamp.setSelection(true);
					this.m_btnTypeFileSize.setSelection(false);
				} else {
					//変更 - ファイルサイズの場合
					this.m_btnTypeTimeStamp.setSelection(false);
					this.m_btnTypeFileSize.setSelection(true);
				}
				break;
			default: // 既定の対処はスルー。
				break;
			}
		}
	}

	/**
	 * オーナーロールIDを設定します。
	 * @param ownerRoleId オーナーロールID
	 * @param managerName マネージャ名
	 */
	public void setOwnerRoleId(String managerName, String ownerRoleId) {
		this.m_managerName = managerName;
		this.m_ownerRoleId = ownerRoleId;
		this.m_facilityId = "";
		m_txtScope.setText("");
	}

	/**
	 * ファシリティIDを戻します。
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		String result = null;
		if(this.m_facilityId != null && this.m_facilityId.length() > 0) {
			result = this.m_facilityId;
		}
		return result;
	}

	/**
	 * スコープを戻します。
	 * @return スコープ
	 */
	public String getScope() {
		String result = null;
		if(this.m_facilityId != null && this.m_facilityId.length() > 0) {
			result = this.m_txtScope.getText();
		}
		return result;
	}

	/**
	 * ディレクトリを戻します。
	 * @return ディレクトリ
	 */
	public String getDirectory() {
		String result = null;
		if(this.m_txtDirectory.getText().length() > 0){
			result = this.m_txtDirectory.getText();
		}
		return result;
	}

	/**
	 * ファイル名を戻します。
	 * @return ファイル名
	 */
	public String getFileName() {
		String result = null;
		if(this.m_txtFileName.getText().length() > 0){
			result = this.m_txtFileName.getText();
		}
		return result;
	}

	/**
	 * ファイルチェック種別を戻します。
	 * @return チェック種別
	 */
	public Integer getEventType() {
		Integer result = null;
		if(this.m_btnTypeCreate.getSelection()){
			result = FileCheckConstant.TYPE_CREATE;
		} else if (this.m_btnTypeDelete.getSelection()){
			result = FileCheckConstant.TYPE_DELETE;
		} else if (this.m_btnTypeModify.getSelection()) {
			result = FileCheckConstant.TYPE_MODIFY;
		}
		return result;
	}

	/**
	 * 変更種別を戻します。
	 * @return 変更種別
	 */
	public Integer getModifyType() {
		Integer result = null;
		if (this.m_btnTypeModify.getSelection()) {
			if (this.m_btnTypeTimeStamp.getSelection()) {
				result = FileCheckConstant.TYPE_MODIFY_TIMESTAMP;
			} else if (this.m_btnTypeFileSize.getSelection()) {
				result = FileCheckConstant.TYPE_MODIFY_FILESIZE;
			}
		}
		return result;
	}
}
