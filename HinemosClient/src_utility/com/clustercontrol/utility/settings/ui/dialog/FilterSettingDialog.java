/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.dialog;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ui.util.FilterSettingProcessMode;
import com.clustercontrol.utility.util.AccountUtil;
import com.clustercontrol.utility.util.UtilityManagerUtil;


/**
 * インポート/エクスポート対象のフィルタ設定を選択するダイアログ
 * 
 */
public class FilterSettingDialog extends CommonDialog {
	private static Log logger = LogFactory.getLog(FilterSettingDialog.class);
	
	private static final int SIZE_WIDTH = 400;
	
	/** シェル */
	private Shell shell = null;
	
	/** 共通フィルタボタン */
	private Button btnCommonFilter;
	/** ユーザフィルタボタン */
	private Button btnUserFilter;
	/** 全ユーザボタン */
	private Button btnAllUser;
	/** 操作ユーザボタン */
	private Button btnExecUser;
	/** チェック "同じ選択を次の設定にも適用"  */
	private Button m_chkbxSame = null;
	
	/** フィルタ設定種別情報 */
	private ArrayList<Boolean> m_filtersettingList = new ArrayList<Boolean>();
	/** フィルタ設定のユーザフィルタ設定の範囲情報 */
	private Boolean m_filtersettingUserRange = true;
	/** "同じ選択を次の設定にも適用" の選択結果 */
	private Boolean m_isSame = false;

	
	/** ダイアログの用途*/
	private String command = null;
	/** フィルタ設定の対象名*/
	private String targetName = null;
	
	/** 接続マネージャのうちいずれかで管理者ならtrue */
	private final boolean belongingAdmins;
	
	/**
	 * コンストラクタ
	 * 
	 * @param parent 親シェル
	 */
	public FilterSettingDialog(Shell parent, String command,String targetName) {
		super(parent);
		this.command = command;
		this.targetName = targetName;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		
		boolean belongingAdmins = false;
		//設定エクスポートインポートの処理対象マネージャのADMINISTRATORロールかどうかを確認する。
		String managerName =UtilityManagerUtil.getCurrentManagerName();
		belongingAdmins = AccountUtil.isAdministrator(managerName);
		this.belongingAdmins = belongingAdmins;
	}
	
	/**
	 * ダイアログエリアを生成します。
	 * 
	 * @param parent 親コンポジット
	 * 
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		logger.debug("customizeDialog () :  start");
		shell = this.getShell();

		// タイトル
		if(command.equals("import")){
			shell.setText(Messages.getString("dialog.utility.filtersetting.import.title") +" " +targetName);
		} else if(command.equals("export")){
			shell.setText(Messages.getString("dialog.utility.filtersetting.export.title") +" " +targetName);
		} else if(command.equals("clear")){
			shell.setText(Messages.getString("dialog.utility.filtersetting.clear.title") +" " +targetName);
		}
		GridData gridData = new GridData();
		
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, false);
		composite.setLayout(gridLayout);
		
		// 全体
		Group group = new Group(composite, SWT.NONE);
		GridLayout groupLayout = new GridLayout(1, true);
		groupLayout.marginWidth = 5;
		groupLayout.marginHeight = 5;
		groupLayout.numColumns = 1;
		group.setLayout(groupLayout);
		if(command.equals("import")){
			group.setText(Messages.getString("dialog.utility.filtersetting.import.type"));
		} else if(command.equals("export")){
			group.setText(Messages.getString("dialog.utility.filtersetting.export.type"));
		} else if(command.equals("clear")){
			group.setText(Messages.getString("dialog.utility.filtersetting.clear.type"));
		}
		gridData = new GridData();
		gridData.widthHint = SIZE_WIDTH;
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group.setLayoutData(gridData);
		
		// フィルタ種別
		Group typeGroup = new Group(group, SWT.NONE);
		GridLayout typeGroupLayout = new GridLayout(1, true);
		typeGroupLayout.marginWidth = 5;
		typeGroupLayout.marginHeight = 5;
		typeGroupLayout.numColumns = 1;
		typeGroup.setLayout(typeGroupLayout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		typeGroup.setLayoutData(gridData);
		
		// 共通フィルタ設定
		btnCommonFilter = new Button(typeGroup, SWT.CHECK);
		btnCommonFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnCommonFilter.setText(Messages.getString("dialog.utility.filtersetting.common"));
		btnCommonFilter.setSelection(true);
		
		// ユーザフィルタ設定
		btnUserFilter = new Button(typeGroup, SWT.CHECK);
		btnUserFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnUserFilter.setText(Messages.getString("dialog.utility.filtersetting.user"));
		
		// ユーザフィルタ設定グループ
		Group userFilterGroup = new Group(group, SWT.NONE);
		GridLayout userFilterGroupLayout = new GridLayout(1, true);
		userFilterGroupLayout.marginWidth = 5;
		userFilterGroupLayout.marginHeight = 5;
		userFilterGroupLayout.numColumns = 1;
		userFilterGroup.setLayout(userFilterGroupLayout);
		userFilterGroup.setText(Messages.getString("dialog.utility.filtersetting.user"));
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.exclude = !belongingAdmins;
		userFilterGroup.setLayoutData(gridData);
		userFilterGroup.setEnabled(false);
		userFilterGroup.setVisible(belongingAdmins);
		
		btnUserFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					userFilterGroup.setEnabled(true);
				} else {
					userFilterGroup.setEnabled(false);
				}
			}
		});
		
		// 全ユーザ関連に対するアクション
		btnAllUser = new Button(userFilterGroup, SWT.RADIO);
		btnAllUser.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		if(command.equals("import")){
			btnAllUser.setText(Messages.getString("dialog.utility.filtersetting.import.all"));
		} else if(command.equals("export")){
			btnAllUser.setText(Messages.getString("dialog.utility.filtersetting.export.all"));
		} else if(command.equals("clear")){
			btnAllUser.setText(Messages.getString("dialog.utility.filtersetting.clear.all"));
		}
		if(belongingAdmins){
			btnAllUser.setSelection(true);
		}
		
		// 実行ユーザ関連に対するアクション
		btnExecUser = new Button(userFilterGroup, SWT.RADIO);
		btnExecUser.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		if(command.equals("import")){
			btnExecUser.setText(Messages.getString("dialog.utility.filtersetting.import.exec"));
		} else if(command.equals("export")){
			btnExecUser.setText(Messages.getString("dialog.utility.filtersetting.export.exec"));
		} else if(command.equals("clear")){
			btnExecUser.setText(Messages.getString("dialog.utility.filtersetting.clear.exec"));
		}
		
		//チェック 同じ処理を次の設定情報にも適用
		m_chkbxSame = new Button(composite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_chkbxSame.setLayoutData(gridData);
		m_chkbxSame.setText(Messages.getString("message.import.confirm11"));
		
		// ダイアログを調整
		this.adjustDialog();
	}
	
	/**
	 * ダイアログエリアを調整します。
	 */
	private void adjustDialog(){
		// 画面中央に配置
		Display calDisplay = shell.getDisplay();
		shell.setLocation((calDisplay.getBounds().width - shell.getSize().x) / 2,
				(calDisplay.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * チェックがONになっているフィルタ設定一覧を返却
	 * 
	 */
	public ArrayList<Boolean> getSelectionData() {
		return m_filtersettingList;
	}
	
	/**
	 * フィルタ設定のユーザフィルタの範囲を返却
	 * 
	 */
	public Boolean getSelectionUserRange() {
		return m_filtersettingUserRange;
	}
	
	/**
	 * ＯＫボタンテキスト取得
	 * 
	 * @return ＯＫボタンのテキスト
	 * 
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}
	
	/**
	 * キャンセルボタンテキスト取得
	 * 
	 * @return キャンセルボタンのテキスト
	 * 
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
		ValidateResult result = null;
		
		ArrayList<Boolean> data = new ArrayList<Boolean>();
		if(btnCommonFilter.getSelection()){
			data.add(true);
		}
		if(btnUserFilter.getSelection()){
			data.add(false);
			
			m_filtersettingUserRange = false;
			if(btnAllUser.getSelection()){
				m_filtersettingUserRange = true;
			} else if(btnExecUser.getSelection()){
				m_filtersettingUserRange = false;
			}
		}
		
		//選択していない場合、入力を促す
		if(data.size() == 0){
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.select.filtersetting"));
			return result;
		} else {
			m_filtersettingList = data;
		}
		
		//"同じ処理を次の設定情報にも適用" の選択結果反映
		m_isSame = m_chkbxSame.getSelection();
		
		return result;
	}

	/*
	 * ダイアログの選択結果に基づいて FilterSettingProcessMode を設定します
	 * 
	 * @see com.clustercontrol.utility.settings.ui.util.FilterSettingProcessMode.FilterSettingProcessMode()
	 */

	public void setFilterSettingProcessMode(String className){
		FilterSettingProcessMode.setFilterTypeList(className,this.getSelectionData());
		FilterSettingProcessMode.setUserFilterRange(className,this.getSelectionUserRange());
		FilterSettingProcessMode.setSameNextChoice(m_isSame);
		FilterSettingProcessMode.setLastFilterTypeList(this.getSelectionData());
		FilterSettingProcessMode.setLastUserFilterRange(this.getSelectionUserRange());
	}
}
