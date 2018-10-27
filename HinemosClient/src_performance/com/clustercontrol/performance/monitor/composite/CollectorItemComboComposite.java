/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.composite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.performance.util.CollectorItemCodeFactory;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.CollectorItemInfo;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.PerfCheckInfo;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 収集項目コードを選択するコンポジットクラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class CollectorItemComboComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( CollectorItemComboComposite.class );

	public static final int WIDTH_TITLE = 2;
	public static final int WIDTH_TITLE_WIDE = 3;
	public static final int WIDTH_VALUE = 10;

	/** 収集項目 */
	private Combo m_comboCollectorItem = null;

	public CollectorItemComboComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize() {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * 収集項目
		 */
		// ラベル
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitoritem", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_WIDE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.item") + " : ");

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// コンボボックス
		this.m_comboCollectorItem = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_comboCollectorItem);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboCollectorItem.setLayoutData(gridData);
		this.m_comboCollectorItem.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// Default list size
		this.m_comboCollectorItem.setVisibleItemCount(16);

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須入力項目を可視化
		if("".equals(this.m_comboCollectorItem.getText())){
			this.m_comboCollectorItem.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboCollectorItem.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

	}

	/**
	 * 収集項目を選択するコンボボックスを生成します。
	 *
	 * @param facilityId ファシリティID
	 */
	public void setCollectorItemCombo(String managerName, String facilityId){
		// 収集項目の一覧を生成
		List<CollectorItemInfo> itemInfoList = null;

		try{
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			itemInfoList = wrapper.getAvailableCollectorItemList(facilityId);
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e){
			// 上記以外の例外
			m_log.warn("setCollectorItemCombo() getAvailableCollectorItemList, " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		if(itemInfoList == null){
			itemInfoList = new ArrayList<CollectorItemInfo>();
		}

		// 現在設定されている項目を全て消去
		m_comboCollectorItem.removeAll();

		// 空の項目を一つ追加。
		m_comboCollectorItem.add("");
		m_comboCollectorItem.setData("", null);

		Iterator<CollectorItemInfo> itr = itemInfoList.iterator();
		while(itr.hasNext()){
			CollectorItemInfo itemInfo = itr.next();

			String itemName = CollectorItemCodeFactory.getFullItemName(managerName, itemInfo);
			itemName = HinemosMessage.replace(itemName);
			m_comboCollectorItem.add(itemName);
			m_comboCollectorItem.setData(itemName, itemInfo);
		}
	}

	/**
	 * 収集項目情報を取得します。
	 * @return 収集項目情報
	 */
	public CollectorItemInfo getCollectorItem(){
		String itemName = m_comboCollectorItem.getText();
		CollectorItemInfo itemInfo = (CollectorItemInfo)m_comboCollectorItem.getData(itemName);
		return itemInfo;
	}

	/**
	 * 監視設定から選択されている監視項目を設定する。
	 * @param monitor
	 */
	public void select(String managerName, MonitorInfo monitor){
		// 性能監視情報
		PerfCheckInfo perfCheckInfo = monitor.getPerfCheckInfo();

		// 監視対象の情報が設定されていない場合はなにもしない
		if(perfCheckInfo == null){
			return;
		}

		String facilityId = monitor.getFacilityId();

		setCollectorItemCombo(managerName, facilityId);

		int index = 0;

		List<CollectorItemInfo> itemInfoList = null;
		try{
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			itemInfoList = wrapper.getAvailableCollectorItemList(facilityId);
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e){
			// 上記以外の例外
			m_log.warn("select() getAvailableCollectorItemList, " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		if(itemInfoList == null){
			itemInfoList = new ArrayList<CollectorItemInfo>();
		}

		Iterator<CollectorItemInfo> itr = itemInfoList.iterator();
		// getAvailableCollectorItemListはCPU使用率から始まるが、
		// 実際のコンボボックスは空行から始まるので、i=1からスタートさせる。
		int i=1;
		while(itr.hasNext()){
			CollectorItemInfo itemInfo = itr.next();

			if(itemInfo.getItemCode().equals(perfCheckInfo.getItemCode()) &&
					itemInfo.getDisplayName().equals(perfCheckInfo.getDeviceDisplayName())){
				index = i;
				break;
			}
			i++;
		}

		this.m_comboCollectorItem.select(index);
	}

	/**
	 * 入力値を用いて監視情報を生成します。
	 *
	 * @return 検証結果
	 */
	public ValidateResult createInputData(MonitorInfo info) {
		if(info != null){
			// リソース監視情報
			PerfCheckInfo perfCheckInfo = info.getPerfCheckInfo();

			if (this.m_comboCollectorItem.getText() != null
					&& !"".equals((this.m_comboCollectorItem.getText()).trim())) {
				String itemName = this.m_comboCollectorItem.getText();
				CollectorItemInfo itemInfo = (CollectorItemInfo)m_comboCollectorItem.getData(itemName);

				perfCheckInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_PERFORMANCE);
				perfCheckInfo.setMonitorId(info.getMonitorId());

				perfCheckInfo.setItemCode(itemInfo.getItemCode());

				if(itemInfo.getDisplayName() != null){
					perfCheckInfo.setDeviceDisplayName(itemInfo.getDisplayName());
				}
				else {
					perfCheckInfo.setDeviceDisplayName("");
				}

				info.setPerfCheckInfo(perfCheckInfo);
			}
		}
		return null;
	}

	/**
	 * 無効な入力値の情報を設定します
	 *
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	public Combo getCombo(){
		return this.m_comboCollectorItem;
	}
}
