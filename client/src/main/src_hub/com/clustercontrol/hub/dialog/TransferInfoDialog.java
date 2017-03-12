/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.calendar.util.CalendarIdListCombo;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.hub.action.AddLog;
import com.clustercontrol.hub.action.GetLog;
import com.clustercontrol.hub.action.GetTransferInfoDestPropDefine;
import com.clustercontrol.hub.action.ModifyLog;
import com.clustercontrol.hub.composite.TransferDataTypeConstant;
import com.clustercontrol.hub.composite.TransferTransIntervalConstant;
import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.hub.DataType;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.TransferDestProp;
import com.clustercontrol.ws.hub.TransferDestTypePropMst;
import com.clustercontrol.ws.hub.TransferInfo;
import com.clustercontrol.ws.hub.TransferInfoDestTypeMst;
import com.clustercontrol.ws.hub.TransferType;

/**
 * 
 *
 */
public class TransferInfoDialog extends CommonDialog {

	// ログ
	private static Logger m_log = Logger.getLogger( TransferInfoDialog.class );
	
	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = CommonMonitorDialog.WIDTH_TITLE;

	/** カラム数（テキスト）。*/
	public static final int WIDTH_TEXT = CommonMonitorDialog.WIDTH_TEXT;
	
	private static final int WIDTH_LABEL = 100;

	/** 作成・変更フラグ*/
	private int mode;
	private String managerName;
	//受け渡し設定ID
	private String exportId;
	private TransferInfo transferInfo;
	private GridData gd_TransferInfoComposite;
	
	protected Shell shell;
	
	private ManagerListComposite managerListComposite;
	private RoleIdListComposite roleIdListComposite;
	
	private Text txtTransferId;
	private Text txtDescription;
	private Combo cmbDestTypeIdList;
	private Combo cmbDataType;
	private Combo cmbRegular;
	private Combo cmbLag;
	private Button btnRealTime;
	private Button btnRegular;
	private Button btnLag;
	private Button chkValid;
	
	/** カレンダID コンポジット。 */
	//private CalendarIdListComposite m_calendarId = null;
	private CalendarIdListCombo m_calendarId = null;
	
	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;
	
	/**
	 * @wbp.parser.constructor
	 */
	public TransferInfoDialog(Shell parent, String managerName) {
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
	public TransferInfoDialog(Shell parent, String managerName, String id, int mode) {
		super(parent);
		this.managerName = managerName;
		this.exportId = id;
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
		parent.getShell().setText(Messages.getString("dialog.hub.log.transfer"));

		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		GridLayout baseLayout = new GridLayout(1, true);
		baseLayout.marginWidth = 10;
		baseLayout.marginHeight = 10;
		//一番下のレイヤー
		parent.setLayout(baseLayout);

		Composite transferInfoComposite = new Composite(parent, SWT.NONE);
		GridLayout gl_TransferInfoComposite = new GridLayout(1, true);
		gl_TransferInfoComposite.marginWidth = 5;
		gl_TransferInfoComposite.marginHeight = 5;
		transferInfoComposite.setLayout(gl_TransferInfoComposite);
		GridData gridData;
		gd_TransferInfoComposite = new GridData();
		gd_TransferInfoComposite.heightHint = 550;
		gd_TransferInfoComposite.verticalAlignment = SWT.FILL;
		gd_TransferInfoComposite.horizontalAlignment = GridData.FILL;
		gd_TransferInfoComposite.grabExcessHorizontalSpace = true;
		transferInfoComposite.setLayoutData(gd_TransferInfoComposite);

		/** TOP Composite */
		Composite topComposite = new Composite(transferInfoComposite, SWT.NONE);
		GridData gd_validComposite = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		topComposite.setLayoutData(gd_validComposite);
		topComposite.setLayout(new GridLayout(2, false));

		//マネージャ
		Label label = new Label(topComposite, SWT.LEFT);
		GridData gd_label = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_label.widthHint = WIDTH_LABEL;
		label.setLayoutData(gd_label);
		label.setText(Messages.getString("facility.manager") + " : ");
		managerListComposite = new ManagerListComposite(topComposite, SWT.NONE, true);
		if( mode == PropertyDefineConstant.MODE_MODIFY ){
			managerListComposite.setEnabled(false);
		} else {
			managerListComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Update 
					managerName = managerListComposite.getText();
					roleIdListComposite.createRoleIdList(managerName);
					m_calendarId.createCalIdCombo(managerName, roleIdListComposite.getText());
					
					// 転送先種別IDのクリア
					updateTransferInfoDestTypeMstList(managerName);
					cmbDestTypeIdList.setText("");
					setDestTypeIdList();
					setTableParam();
					update();
				}
			});
		}
		managerListComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		if (managerName != null) {
			managerListComposite.setText(managerName);
		}
		
		//受け渡し設定ID
		Label labelTransferId = new Label(topComposite, SWT.LEFT);
		GridData gd_labelTransferId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_labelTransferId.widthHint = WIDTH_LABEL;
		labelTransferId.setLayoutData(gd_labelTransferId);
		labelTransferId.setText(Messages.getString("hub.log.transfer.id") + " : ");
		txtTransferId = new Text(topComposite, SWT.BORDER);
		GridData gd_textTransferId = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		txtTransferId.setLayoutData(gd_textTransferId);
		txtTransferId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		//説明
		Label lblDescription = new Label(topComposite, SWT.LEFT);
		GridData gd_lblDescription = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblDescription.widthHint = WIDTH_LABEL;
		lblDescription.setLayoutData(gd_lblDescription);
		lblDescription.setText(Messages.getString("description") + " : ");
		txtDescription = new Text(topComposite, SWT.BORDER);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// オーナーロールID
		Label labelRoleId = new Label(topComposite, SWT.LEFT);
		GridData gd_labelRoleId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_labelRoleId.widthHint = WIDTH_LABEL;
		labelRoleId.setLayoutData(gd_labelRoleId);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		roleIdListComposite = 
				new RoleIdListComposite(topComposite, SWT.NONE, this.managerListComposite.getText(), true, Mode.OWNER_ROLE);
		if( this.mode == PropertyDefineConstant.MODE_MODIFY ){
			roleIdListComposite.setEnabled(false);
		}
		this.roleIdListComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.roleIdListComposite.getComboRoleId().addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				m_calendarId.setText("");
				m_calendarId.createCalIdCombo(managerName, roleIdListComposite.getText());
				update();
			}
		});
		
		Label labelCalIdList = new Label(topComposite, SWT.NONE);
		GridData gd_labelCalIdList = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_labelCalIdList.widthHint = WIDTH_LABEL;
		labelCalIdList.setLayoutData(gd_labelCalIdList);
		labelCalIdList.setText(Messages.getString("calendar.id") + " : ");
		
		this.m_calendarId = new CalendarIdListCombo(topComposite, SWT.READ_ONLY);
		this.m_calendarId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.m_calendarId.createCalIdCombo(managerName, roleIdListComposite.getText());
		
		Label labelDataType = new Label(topComposite, SWT.NONE);
		GridData gd_labelDataType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		labelDataType.setLayoutData(gd_labelDataType);
		labelDataType.setText(Messages.getString("dialog.hub.log.transfer.data.type") + " : ");
		
		this.cmbDataType = new Combo(topComposite, SWT.READ_ONLY);
		this.cmbDataType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.cmbDataType.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		for (DataType type : DataType.values()) {
			cmbDataType.add(Messages.getString(TransferDataTypeConstant.typeToString(type)));
			cmbDataType.setData(Messages.getString(TransferDataTypeConstant.typeToString(type)), type);
		}
		this.cmbDataType.setEnabled(mode != PropertyDefineConstant.MODE_MODIFY);
		
		
		//転送先グループ
		Group transferDestGroup = new Group(transferInfoComposite, SWT.NONE);
		transferDestGroup.setLayout(new GridLayout(2, false));
		GridData gd_transferDestGroup = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_transferDestGroup.heightHint = 176;
		gd_transferDestGroup.widthHint = 477;
		transferDestGroup.setLayoutData(gd_transferDestGroup);
		transferDestGroup.setText(Messages.getString("dialog.hub.log.transfer.dest.info"));
		
		Label labelTransType = new Label(transferDestGroup, SWT.NONE);
		GridData gd_labelTransType = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_labelTransType.widthHint = 100;
		labelTransType.setLayoutData(gd_labelTransType);
		labelTransType.setText(Messages.getString("hub.log.transfer.dest.type.id") + " : ");
		
		this.cmbDestTypeIdList = new Combo(transferDestGroup, SWT.READ_ONLY);
		this.cmbDestTypeIdList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// データ取得
		if (managerName != null) {
			List<TransferInfoDestTypeMst> list = transDestTypeMstMap.get(managerName);
			if (list == null || list.isEmpty()){
				updateTransferInfoDestTypeMstList(managerName);
			}
		}
		
		// 転送データ種別プルダウンメニューの作成
		setDestTypeIdList();
		
		this.cmbDestTypeIdList.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				setTableParam();
				update();
			}
		});
		
		new Label(transferDestGroup, SWT.NONE);//空白ラベル
		
		Table tblDestProps = new Table(transferDestGroup, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tblDestProps.setHeaderVisible(true);
		tblDestProps.setLinesVisible(true);
		
		gridData = new GridData();
		gridData.widthHint = 228;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tblDestProps.setLayoutData(gridData);
				
		// テーブルビューアの作成
		m_tableViewer = new CommonTableViewer(tblDestProps);
		m_tableViewer.createTableColumn(GetTransferInfoDestPropDefine.get(),
				GetTransferInfoDestPropDefine.SORT_COLUMN_INDEX,
				GetTransferInfoDestPropDefine.SORT_ORDER);
		
		Table table = m_tableViewer.getTable();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Point point = new Point(e.x, e.y);

				Table table = (Table) e.getSource();
				TableItem item = table.getItem(point);
				if (item == null) {
					return;
				}
				createEditor(table, item, GetTransferInfoDestPropDefine.VALUE);
			}
		});
		
		Group transferIntervalGroup = new Group(transferInfoComposite, SWT.NONE);
		transferIntervalGroup.setLayout(new GridLayout(2, false));
		GridData gd_transferIntervalGroup = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_transferIntervalGroup.widthHint = 480;
		transferIntervalGroup.setLayoutData(gd_transferIntervalGroup);
		transferIntervalGroup.setText(Messages.getString("dialog.hub.log.transfer.trans.interval"));
		
		btnRealTime = new Button(transferIntervalGroup, SWT.RADIO);
		btnRealTime.setText(Messages.getString("hub.log.transfer.trans.type.realtime"));
		btnRealTime.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		new Label(transferIntervalGroup, SWT.NONE);
		
		btnRegular = new Button(transferIntervalGroup, SWT.RADIO);
		btnRegular.setText(Messages.getString("hub.log.transfer.trans.type.regular"));
		btnRegular.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		cmbRegular = new Combo(transferIntervalGroup, SWT.READ_ONLY);
		cmbRegular.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for (int type : TransferTransIntervalConstant.getRegularList()) {
			cmbRegular.add(Messages.getString(TransferTransIntervalConstant.typeToString(type)));
			cmbRegular.setData(Messages.getString(TransferTransIntervalConstant.typeToString(type)), type);
		}
		cmbRegular.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		
		btnLag = new Button(transferIntervalGroup, SWT.RADIO);
		btnLag.setText(Messages.getString("hub.log.transfer.trans.type.lag"));
		btnLag.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		cmbLag = new Combo(transferIntervalGroup, SWT.READ_ONLY);
		cmbLag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for (int type : TransferTransIntervalConstant.getLagList()) {
			cmbLag.add(Messages.getString(TransferTransIntervalConstant.typeToString(type)));
			cmbLag.setData(Messages.getString(TransferTransIntervalConstant.typeToString(type)), type);
		}
		cmbLag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		
		Composite validComposite = new Composite(transferInfoComposite, SWT.NONE);
		validComposite.setLayout(new GridLayout(2, false));
		gd_validComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_validComposite.widthHint = 486;
		validComposite.setLayoutData(gd_validComposite);
		
		Label labelValid = new Label(validComposite, SWT.NONE);
		labelValid.setText(Messages.getString("setting.valid.confirmed"));
		
		chkValid = new Button(validComposite, SWT.CHECK);
		chkValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		
		// サイズ調整
		adjustDialog();

		//デフォルト値設定
		btnRealTime.setSelection(true);
		
		this.reflectLogTransfer();
		update();
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
		Display tarnsferAdjustDisplay = shell.getDisplay();
		shell.setLocation((tarnsferAdjustDisplay.getBounds().width - shell.getSize().x) / 2,
				(tarnsferAdjustDisplay.getBounds().height - shell.getSize().y) / 2);
	}
	
	/**
	 * 
	 */
	public void update() {
		//受け渡し設定IDが必須項目であることを明示
		if("".equals(this.txtTransferId.getText())){
			this.txtTransferId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.txtTransferId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		
		//転送データ種別が必須項目であることを明示
		if("".equals(this.cmbDataType.getText())){
			this.cmbDataType.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.cmbDataType.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		
		//受け渡し先IDが必須項目であることを明示
		if("".equals(this.cmbDestTypeIdList.getText())){
			this.cmbDestTypeIdList.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.cmbDestTypeIdList.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}		
		if (btnRealTime.getSelection()) {
			this.cmbRegular.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.cmbRegular.setEnabled(false);
			this.cmbLag.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.cmbLag.setEnabled(false);
		}
		// 一定間隔
		if (btnRegular.getSelection()) {
			if("".equals(this.cmbRegular.getText())){
				this.cmbRegular.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				this.cmbRegular.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			this.cmbRegular.setEnabled(true);
			this.cmbLag.setEnabled(false);
			this.cmbLag.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 保存期間を経て
		if (btnLag.getSelection()) {
			if("".equals(this.cmbLag.getText())){
				this.cmbLag.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				this.cmbLag.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			this.cmbRegular.setEnabled(false);
			this.cmbRegular.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.cmbLag.setEnabled(true);
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
		//受け渡し設定ID
		if ("".equals(txtTransferId.getText())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.hub.log.transfer.required.transfer.id"));
		}
		if ("".equals(cmbDataType.getText())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.hub.log.transfer.required.data.id"));
		}
		if ("".equals(cmbDestTypeIdList.getText())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.hub.log.transfer.required.dest.id"));
		}
		//オーナロールID
		if ("".equals(roleIdListComposite.getText())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.hub.log.transfer.required.owner.role.id"));
		}
		if (btnRegular.getSelection()) {
			if ("".equals(cmbRegular.getText())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.hub.log.transfer.required.interval.regular"));
			}
		}
		if (btnLag.getSelection()) {
			if ("".equals(cmbLag.getText())) {
				return createValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.hub.log.transfer.required.interval.lag"));
			}
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
	 * ダイアログに収集蓄積[転送]情報を反映します。
	 */
	private void reflectLogTransfer() {
		// 初期表示
		
		TransferInfo export = null;
		
		if (mode == PropertyDefineConstant.MODE_MODIFY
				|| mode == PropertyDefineConstant.MODE_COPY) {
			// 変更、コピーの場合、情報取得
			export = new GetLog().getLogTransfer(this.managerName, this.exportId);
		} else {
			// 作成の場合
			export = new TransferInfo();
		}
		
		this.transferInfo = export;
		//受け渡し設定ID
		if (export.getTransferId() != null) {
			this.exportId = export.getTransferId();
			this.txtTransferId.setText(this.exportId);
			//収集蓄積[エクスポート]定義変更の際には収集IDは変更不可
			if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				this.txtTransferId.setEnabled(false);
			}
		}
		//説明
		if (export.getDescription() != null){
			this.txtDescription.setText(export.getDescription());
		}
		// オーナーロールID取得
		if (export.getOwnerRoleId() != null) {
			this.roleIdListComposite.setText(export.getOwnerRoleId());
		}
		
		//カレンダID
		if (export.getCalendarId() != null) {
			this.m_calendarId.setText(export.getCalendarId());
		}
		
		//転送データ種別
		if (export.getDataType() != null) {
			this.cmbDataType.setText(Messages.getString(TransferDataTypeConstant.typeToString(export.getDataType())));
			this.cmbDataType.setData(Messages.getString(TransferDataTypeConstant.typeToString(export.getDataType())),export.getDataType());
		}
		//転送先種別
		if (export.getDestTypeId() != null) {
			this.cmbDestTypeIdList.setText(Messages.getString(export.getDestTypeId()));
			if (export.getDestProps() != null && export.getDestProps().isEmpty()) {
				setTableParam();
			}	
		}
		//転送間隔
		if (export.getTransType() != null) {
			switch(export.getTransType()){
			case REALTIME:
				btnRealTime.setSelection(true);
				btnRegular.setSelection(false);
				btnLag.setSelection(false);
				break;
			case BATCH:
				btnRealTime.setSelection(false);
				btnRegular.setSelection(true);
				btnLag.setSelection(false);
				if (export.getInterval() != null) {
					cmbRegular.setText(Messages.getString(TransferTransIntervalConstant.typeToString(export.getInterval())));
					cmbRegular.setData(Messages.getString(TransferTransIntervalConstant.typeToString(export.getInterval())),export.getInterval());
				}
				break;
			case DELAY:
				btnRealTime.setSelection(false);
				btnRegular.setSelection(false);
				btnLag.setSelection(true);
				if (export.getInterval() != null) {
					cmbLag.setText(Messages.getString(TransferTransIntervalConstant.typeToString(export.getInterval())));
					cmbLag.setData(Messages.getString(TransferTransIntervalConstant.typeToString(export.getInterval())),export.getInterval());
				}
				break;
			default:
				m_log.warn("LogTransfer.getTransType is illegal value.");
				break;
			}
		}
		//有効・無効
		if (export.isValidFlg() != null) {
			chkValid.setSelection(export.isValidFlg());
		}
		this.update();
	}

	/**
	 * ダイアログの情報から収集蓄積[エクスポート]設定情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see
	 */
	private void createTransfer() {
		this.transferInfo = new TransferInfo();
		this.transferInfo.setTransferId(this.txtTransferId.getText());
		this.transferInfo.setDescription(this.txtDescription.getText());
		
		this.transferInfo.setOwnerRoleId(this.roleIdListComposite.getText());
		
		this.transferInfo.setCalendarId(this.m_calendarId.getText());
		
		this.transferInfo.setDataType((DataType) this.cmbDataType.getData(this.cmbDataType.getText()));
		
		String destTypeId = this.cmbDestTypeIdList.getText();
		this.transferInfo.setDestTypeId(destTypeId);
		//転送先情報
		Table table = this.m_tableViewer.getTable();
		TableItem[] items = table.getItems();
		transferInfo.getDestProps().clear();
		for (int i = 0; i < m_tableViewer.getTable().getItemCount(); i++) {
			TransferDestProp prop = new TransferDestProp();
			//名前
			prop.setName(replaceDestPropName.get(items[i].getText(0)));
			//値
			prop.setValue(items[i].getText(1));
			transferInfo.getDestProps().add(prop);
		}
		
		if (this.btnRealTime.getSelection()) {
			this.transferInfo.setTransType(TransferType.REALTIME);
			this.transferInfo.setInterval(null);
		}else if (this.btnRegular.getSelection()) {
			this.transferInfo.setTransType(TransferType.BATCH);
			this.transferInfo.setInterval(
					(Integer)cmbRegular.getData(cmbRegular.getText()));
		}else if (this.btnLag.getSelection()) {
			this.transferInfo.setTransType(TransferType.DELAY);
			this.transferInfo.setInterval(
					(Integer)cmbLag.getData(cmbLag.getText()));
		}else {
			m_log.warn("");
		}

		this.transferInfo.setValidFlg(this.chkValid.getSelection());
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
		createTransfer();
		TransferInfo export = this.transferInfo;
		String managerName = this.managerListComposite.getText();
		
		if(export != null){
			if(mode == PropertyDefineConstant.MODE_ADD){
				// 作成の場合+
				result = new AddLog().addLogTransfer(managerName, export);
			} 
			else if (mode == PropertyDefineConstant.MODE_MODIFY){
				// 変更の場合
				export.setTransferId(txtTransferId.getText());
				result = new ModifyLog().modifyLogTransfer(managerName, export);
			} 
			else if (mode == PropertyDefineConstant.MODE_COPY){
				// コピーの場合
				export.setTransferId(txtTransferId.getText());
				result = new AddLog().addLogTransfer(managerName, export);
			}
		} else {
			m_log.error("action() LogTransfer is null");
		}
		return result;
	}
	/**
	 * セルエディタ
	 * @param table
	 * @param item
	 * @param column
	 */
	private void createEditor(Table table, final TableItem item, final int column) {
		final Text text = new Text(table, SWT.NONE);
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				item.setText(column, text.getText());
				text.dispose(); 
			}
		});
		text.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
				case SWT.TRAVERSE_RETURN:
					item.setText(column, text.getText()); 
					text.dispose(); 
					break;
				case SWT.TRAVERSE_ESCAPE:
					text.dispose();
					e.doit = false;
					break;
				default:
					break;
				}
			}
		});
		

		TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 64;
		editor.setEditor(text, item, column);

		text.setText(item.getText(column));
		text.selectAll();
		text.setFocus();
	}
	
	/** 転送先の一覧 */
	private Map<String, List<TransferInfoDestTypeMst>> transDestTypeMstMap= new HashMap<>();
	
	/**
	 * 転送データ種別一覧の取得
	 * @param managerName マネージャ名
	 */
	private void updateTransferInfoDestTypeMstList(String managerName){
		Map<String, String> errorMsgs = new HashMap<>();

		List<TransferInfoDestTypeMst> mst = null;
		try {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			mst = wrapper.getTransferInfoDestTypeMstList();
			transDestTypeMstMap.put(managerName, mst);
		} catch (InvalidRole_Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			m_log.warn("getLogTransferDestTypeMstList(), " + e.getMessage(), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
	}
	
	private Map<String, String> replaceDestPropName = new HashMap<>();
	
	/**
	 * 
	 */
	private void setTableParam(){
		// テーブル更新用リスト
		List<List<Object>> listAll = new ArrayList<>();
		// DBから取得したマスタデータをテーブルに反映
		List<TransferInfoDestTypeMst> msts = transDestTypeMstMap.get(managerName);
		for (TransferInfoDestTypeMst mst : msts) {
			if (mst.getDestTypeId().equals(cmbDestTypeIdList.getText())) {
				for (TransferDestTypePropMst prop : mst.getDestTypePropMsts()) {
					List<Object> list = new ArrayList<>();
					replaceDestPropName.put(Messages.getString(prop.getName()), prop.getName());
					list.add(Messages.getString(prop.getName()));
					list.add(prop.getValue() != null? prop.getValue():"");
					list.add(Messages.getString(prop.getDescription()));
					list.add(null);
					listAll.add(list);
				}
			}
		}
		if (transferInfo != null && transferInfo.getDestTypeId() != null) {
			//変更操作の場合、テーブルの値をマージする。
			if (transferInfo.getDestTypeId().equals(cmbDestTypeIdList.getText())) {
				for (List<Object> list: listAll) {
					for (TransferDestProp prop : transferInfo.getDestProps()) {
						if (prop.getName().equals(replaceDestPropName.get(list.get(0)))) {
							if (prop.getValue() != null && !prop.getValue().isEmpty()) {
								list.set(1, prop.getValue());
							}
						}
					}
				}
			}
		}
		m_tableViewer.setInput(listAll);
	}
	
	/**
	 * 転送データ種別プルダウンメニューの作成
	 * 
	 */
	private void setDestTypeIdList() {
		if (this.cmbDestTypeIdList.getItemCount() != 0) {
			this.cmbDestTypeIdList.removeAll();
		}
		// 転送データ種別プルダウンメニューの作成
		for(Map.Entry<String, List<TransferInfoDestTypeMst>> map : transDestTypeMstMap.entrySet()) {
			if (map.getKey().equals(this.managerName)) {
				for(TransferInfoDestTypeMst type : map.getValue()){
					this.cmbDestTypeIdList.add(Messages.getString(type.getDestTypeId()));
				}
			}
		}
	}
}
