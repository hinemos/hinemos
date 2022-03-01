/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * シナリオ実績作成設定コンポジットクラス<BR>
 */
public class RpaScenarioOperationResultCreateSettingIdListComposite extends Composite {

	// ログ
		private static Log m_log = LogFactory.getLog( RpaScenarioOperationResultCreateSettingIdListComposite.class );

	// ----- instance フィールド ----- //

	/** シナリオ実績作成設定コンボボックス（表示用のIDのみ保持） */
	private Combo comboScenarioResultCreateSetting = null;

	/** シナリオ実績作成設定テキストボックス */
	private Text txtScenarioResultCreateSetting = null;

	/** 変更可能フラグ */
	private boolean enabledFlg = false;

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。<BR>
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param enabledFlg 変更可否フラグ（true:変更可能、false:変更不可）
	 */
	public RpaScenarioOperationResultCreateSettingIdListComposite(Composite parent, int style, String managerName, boolean enabledFlg) {
		super(parent, style);
		this.managerName = managerName;
		this.enabledFlg = enabledFlg;
		this.initialize(parent);
	}


	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。<BR>
	 */
	private void initialize(Composite parent) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		this.setLayout(layout);

		/*
		 * シナリオ実績作成設定
		 */
		if (this.enabledFlg) {
			// 変更可能な場合コンボボックス
			this.comboScenarioResultCreateSetting = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
			WidgetTestUtil.setTestId(this, "comboScenarioResultCreateSetting", comboScenarioResultCreateSetting);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.comboScenarioResultCreateSetting.setLayoutData(gridData);
		} else {
			// 変更不可な場合テキストボックス
			this.txtScenarioResultCreateSetting = new Text(this, SWT.BORDER | SWT.LEFT);
			WidgetTestUtil.setTestId(this, "txtScenarioResultCreateSetting", txtScenarioResultCreateSetting);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.txtScenarioResultCreateSetting.setLayoutData(gridData);
			this.txtScenarioResultCreateSetting.setEnabled(false);
		}

		// 変更可能時はコンボボックスを表示する
		if (this.enabledFlg) {
			this.createScenarioResultCreateSettingList();
			this.update();
		} 
	}

	/**
	 * コンボボックスに値を設定します。<BR>
	 * <p>
	 *
	 */
	public void createScenarioResultCreateSettingList() {
		List<RpaScenarioOperationResultCreateSettingResponse> dtoList = null;
		// データ取得
		dtoList = callSettingList();

		if(dtoList != null){
			String settingOld = this.comboScenarioResultCreateSetting.getText();
			// クリア
			this.comboScenarioResultCreateSetting.removeAll();
			
			// リストのソート
			dtoList = dtoList.stream()
					.sorted(Comparator.comparing(RpaScenarioOperationResultCreateSettingResponse::getScenarioOperationResultCreateSettingId))
					.collect(Collectors.toList());
			
			for(RpaScenarioOperationResultCreateSettingResponse setting : dtoList){
				this.comboScenarioResultCreateSetting.add(setting.getScenarioOperationResultCreateSettingId());
				// オーナーロールIDを格納
				this.comboScenarioResultCreateSetting.setData(setting.getScenarioOperationResultCreateSettingId(), setting.getOwnerRoleId());
			}
			int defaultSelect = this.comboScenarioResultCreateSetting.indexOf(settingOld);
			this.comboScenarioResultCreateSetting.select( (-1 == defaultSelect) ? 0 : defaultSelect );
		}
	}
	
	private List<RpaScenarioOperationResultCreateSettingResponse> callSettingList(){
		List<RpaScenarioOperationResultCreateSettingResponse> dtoList = null;
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.managerName);
			dtoList = wrapper.getRpaScenarioOperationResultCreateSettingList();
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return dtoList;
	}
	
	/**
	 * コンポジットを更新します。<BR>
	 * <p>
	 *
	 */
	@Override
	public void update() {
		this.comboScenarioResultCreateSetting.select(0);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabledFlg) {
			this.comboScenarioResultCreateSetting.setEnabled(enabled);
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public String getText() {
		if (this.enabledFlg) {
			return this.comboScenarioResultCreateSetting.getText();
		} else {
			return this.txtScenarioResultCreateSetting.getText();
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String string) {
		if (this.enabledFlg) {
			this.comboScenarioResultCreateSetting.setText(string);
		} else {
			this.txtScenarioResultCreateSetting.setText(string);
		}
	}

	public void addModifyListener(ModifyListener modifyListener){
		comboScenarioResultCreateSetting.addModifyListener(modifyListener);
	}

	public Combo getComboCreateSetting() {
		return comboScenarioResultCreateSetting;
	}

	public void add(String managerName) {
		this.comboScenarioResultCreateSetting.add(managerName);
		this.update();
	}

	public void delete(String managerName) {
		if (this.comboScenarioResultCreateSetting.indexOf(managerName) > -1) {
			this.comboScenarioResultCreateSetting.remove(managerName);
			this.update();
		}
	}

	public void addComboSelectionListener(SelectionListener listener) {
		if (this.enabledFlg) {
			this.comboScenarioResultCreateSetting.addSelectionListener(listener);
		}
	}

	public String getOwnerRoleId() {
		String ownerRoleId = "";
		if (this.enabledFlg) {
			ownerRoleId = (String)comboScenarioResultCreateSetting.getData(comboScenarioResultCreateSetting.getText());
		}
		if (ownerRoleId == null){
			ownerRoleId = "";
		}
		
		// 変更不可の場合(設定の参照)の場合、オーナーロールIDはシナリオ設定から参照する想定なので、ここでは空文字を返す。
		return ownerRoleId;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
		if (this.enabledFlg) {
			this.createScenarioResultCreateSettingList();
			this.update();
		}
	}
	
	public void addSelectionListenerToComboBox(SelectionListener listener) {
		if (this.enabledFlg && this.comboScenarioResultCreateSetting != null) {
			this.comboScenarioResultCreateSetting.addSelectionListener(listener);
		}
	}
}
