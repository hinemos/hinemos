/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.composite;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.MaintenanceTypeInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.util.MaintenanceRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * メンテナンス種別コンポジットクラスです。
 *
 * @version 2.2.0
 * @since 2.2.0
 */
public class MaintenanceTypeListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( MaintenanceTypeListComposite.class );

	public static final int WIDTH_TITLE = 5;
	public static final int WIDTH_TEXT = 9;

	// ----- instance フィールド ----- //

	/** メンテナンス種別ラベル */
	private Label labelMaintenanceType = null;

	/** メンテナンス種別コンボボックス */
	private Combo comboMaintenanceType = null;

	/** メンテナンス種別一覧リスト */
	ConcurrentHashMap<String, List<MaintenanceTypeInfoResponse>> dispDataMap= new ConcurrentHashMap<String, List<MaintenanceTypeInfoResponse>>();

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 * @param labelFlg メンテナンス種別ラベル表示フラグ
	 */
	public MaintenanceTypeListComposite(Composite parent, int style, String managerName, boolean labelFlg) {
		super(parent, style);

		this.managerName = managerName;
		this.initialize(parent, labelFlg);
	}


	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize(Composite parent, boolean labelFlg) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		if(labelFlg){
			layout.numColumns = 15;
		}
		else{
			layout.numColumns = 10;
		}
		this.setLayout(layout);

		/*
		 * メンテナンス種別
		 */
		if(labelFlg){
			// ラベル
			this.labelMaintenanceType = new Label(this, SWT.NONE);
			WidgetTestUtil.setTestId(this, "maintenancetype", labelMaintenanceType);
			gridData = new GridData();
			gridData.horizontalSpan = WIDTH_TITLE;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.labelMaintenanceType.setLayoutData(gridData);
			this.labelMaintenanceType.setText(Messages.getString("maintenance.type") + " : ");
		}

		// コンボボックス
		this.comboMaintenanceType = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, comboMaintenanceType);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.comboMaintenanceType.setLayoutData(gridData);

		this.update();
	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 */
	@Override
	public void update() {

		// データ取得
		if(dispDataMap.get(managerName) == null || dispDataMap.get(managerName).isEmpty()){
			getMaintenanceTypeList(getManagerName());
		}

		this.comboMaintenanceType.removeAll();
		// メンテナンス種別プルダウンメニューの作成
		for(Map.Entry<String, List<MaintenanceTypeInfoResponse>> map : dispDataMap.entrySet()) {
			for(MaintenanceTypeInfoResponse type : map.getValue()){
				this.comboMaintenanceType.add(Messages.getString(type.getNameId()));
				this.comboMaintenanceType.setData(Messages.getString(type.getNameId()), type);
			}
		}
	}

	/**
	 * メンテナンス種別のプルダウンメニューより選択された種別の選択番号を取得
	 * @return
	 */
	public int getSelectionIndex(){
		return comboMaintenanceType.getSelectionIndex();
	}

	/**
	 * メンテナンス種別のプルダウンメニューより選択された種別の取得
	 * @param managerName
	 * @return
	 */
	public String getSelectionTypeId() {
		MaintenanceTypeInfoResponse mst = (MaintenanceTypeInfoResponse)this.comboMaintenanceType.getData(this.comboMaintenanceType.getText());
		String typeId = mst.getTypeId().getValue();
		return typeId;
	}

	/**
	 * メンテナンス種別IDよりメンテナンス種別名の取得
	 * @param managerName
	 * @param type_id
	 * @return
	 */
	public String getMaintenanceTypeName(String managerName, String type_id){
		String name = null;
		// データ取得
		if(dispDataMap.isEmpty()){
			getMaintenanceTypeList(managerName);
		}

		// メンテナンス種別IDよりメンテナンス種別名を取得
		List<MaintenanceTypeInfoResponse> list = dispDataMap.get(managerName);
		for(MaintenanceTypeInfoResponse type : list){
			if((type.getTypeId().getValue()).equals(type_id)){
				name = Messages.getString(type.getNameId());
				break;
			}
		}
		return name;
	}

	/**
	 * メンテナンス種別一覧の取得
	 * @param managerName マネージャ名
	 */
	private void getMaintenanceTypeList(String managerName){
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		List<MaintenanceTypeInfoResponse> mst = null;
		dispDataMap.clear();
		try {
			MaintenanceRestClientWrapper wrapper = MaintenanceRestClientWrapper.getWrapper(managerName);
			mst = wrapper.getMaintenanceTypeList();
			dispDataMap.put(managerName, mst);
		} catch (InvalidRole e) {
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			m_log.warn("getMaintenanceTypeList(), " + e.getMessage(), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.comboMaintenanceType.setEnabled(enabled);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public String getText() {
		return this.comboMaintenanceType.getText();
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String string) {
		this.comboMaintenanceType.setText(string);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#setBackground(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setBackground(Color color) {
		this.comboMaintenanceType.setBackground(color);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#addModifyListener(org.eclipse.swt.events.ModifyListener)
	 */
	public void addModifyListener(ModifyListener listener) {
		this.comboMaintenanceType.addModifyListener(listener);
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

}
