/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.AddPerformanceMonitorRequest;
import org.openapitools.client.model.CollectorItemInfoResponse;
import org.openapitools.client.model.ModifyPerformanceMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoRequest;
import org.openapitools.client.model.PerfCheckInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.performance.monitor.composite.CollectorItemComboComposite;
import com.clustercontrol.performance.util.CollectorItemCodeFactory;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;
/**
 * リソース監視作成・変更ダイアログクラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class PerformanceCreateDialog extends CommonMonitorNumericDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( PerformanceCreateDialog.class );

	// ----- instance フィールド ----- //

	/** 収集項目 */
	private CollectorItemComboComposite m_comboCollectorItem = null;

	/** 内訳も合わせて収集するかチェックボックス */
	private Button m_breakdownFlg = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public PerformanceCreateDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param notifyId
	 *            変更する通知ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 */
	public PerformanceCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// 項目名に「取得値」を設定
		item1 = Messages.getString("select.value");
		item2 = Messages.getString("select.value");

		super.customizeDialog(parent);
		itemName.setEditable(false);
		measure.setEditable(false);

		// タイトル
		shell.setText(Messages.getString("dialog.performance.monitor.create.modify"));

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));

		// コンボボックス
		this.m_comboCollectorItem =
				new CollectorItemComboComposite(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "collectoritem", m_comboCollectorItem);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboCollectorItem.setLayoutData(gridData);

		// デバイス別の項目を含まない収集項目のリストを設定する
		m_comboCollectorItem.setCollectorItemCombo(this.m_monitorBasic.getManagerListComposite().getText(), null);

		m_monitorBasic.getButtonScope().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_comboCollectorItem.setCollectorItemCombo(PerformanceCreateDialog.this.getManagerName(),m_monitorBasic.getFacilityId());
			}
		});

		//マネージャを変更した場合
		if(!updateFlg) {
			this.getMonitorBasicScope().getManagerListComposite()
				.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_comboCollectorItem.setCollectorItemCombo(PerformanceCreateDialog.this.getManagerName(),m_monitorBasic.getFacilityId());
				}
			});
		}

		// 内訳フラグ
		this.m_breakdownFlg = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "breakdownflg", m_breakdownFlg);
		this.m_breakdownFlg.setText(Messages.getString("dialog.performance.monitor.collect.detail"));
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_breakdownFlg.setLayoutData(gridData);

		// 収集値表示名と収集値単位を設定
		m_comboCollectorItem.getCombo().addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0){
				itemName.setText(m_comboCollectorItem.getCombo().getText());
				if(m_comboCollectorItem.getCollectorItem() != null){
					measure.setText(HinemosMessage.replace(CollectorItemCodeFactory.getMeasure(
							PerformanceCreateDialog.this.getManagerName(), m_comboCollectorItem.getCollectorItem().getItemCode())));
					update();
				}
			}
		});

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfoResponse info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfoResponse();
			this.setInfoInitialValue(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(this.getManagerName());
				info = wrapper.getMonitor(this.monitorId);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				throw new InternalError(e.getMessage());
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				throw new InternalError(e.getMessage());
			}
		}
		this.setInputData(info);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	protected void update() {
		super.update();

		try{
			// 項目名の「取得値（単位）」のうち、単位部分を更新
			item1 = Messages.getString("select.value")
					+ "("
					+ HinemosMessage.replace(CollectorItemCodeFactory.getMeasure(this.getManagerName(),
							m_comboCollectorItem.getCollectorItem()
									.getItemCode())) + ")";
			item2 = item1;

			this.m_numericValueInfo.setTextItem1(item1);
			this.m_numericValueInfo.setTextItem2(item2);

		}catch(NullPointerException e){
			// スコープが選択されていない場合
		}

	}
	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		// 監視条件リソース監視情報
		PerfCheckInfoResponse perfInfo = monitor.getPerfCheckInfo();
		if(perfInfo == null){
			perfInfo = new PerfCheckInfoResponse();
		}
		// 内訳を収集するかのフラグ
		if (perfInfo.getBreakdownFlg() == null) {
			this.m_breakdownFlg.setSelection(false);
		} else {
			this.m_breakdownFlg.setSelection(perfInfo.getBreakdownFlg());
		}

		// 収集項目
		m_comboCollectorItem.select(this.getManagerName(), monitor);

		m_numericValueInfo.setInputData(monitor);

		this.update();
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// リソース監視情報を生成
		PerfCheckInfoResponse perfInfo = new PerfCheckInfoResponse();

		// 内訳を収集するかのフラグ
		perfInfo.setBreakdownFlg(m_breakdownFlg.getSelection());

		monitorInfo.setPerfCheckInfo(perfInfo);

		// 結果判定の定義
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 監視対象の収集項目を設定
		validateResult = m_comboCollectorItem.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 通知関連情報とアプリケーションの設定
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if(validateResult.getID() == null){	// 通知ID警告用出力
				if(!displayQuestion(validateResult)){
					validateResult = null;
					return null;
				}
			}
			else{	// アプリケーション未入力チェック
				return null;
			}
		}
		return monitorInfo;
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

		if (this.inputData != null) {
			String[] args = { this.inputData.getMonitorId(), getManagerName() };

			CollectorItemInfoResponse itemInfo = (CollectorItemInfoResponse)this.m_comboCollectorItem.getCombo().getData(this.itemName.getText());
			if(itemInfo == null || itemInfo.getItemCode().length() == 0) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.monitor.34", args));
				return false;
			}
			String itemName = CollectorItemCodeFactory.getItemName(managerName, itemInfo.getItemCode());
			String itemMeasure = CollectorItemCodeFactory.getMeasure(itemName, itemInfo.getItemCode());

			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
			if(!this.updateFlg){
				// 作成の場合
				try {
					AddPerformanceMonitorRequest info = new AddPerformanceMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setItemName(itemName);
					info.setMeasure(itemMeasure);
					info.setRunInterval(AddPerformanceMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(AddPerformanceMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.addPerformanceMonitor(info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;

				} catch (MonitorIdInvalid e) {
					// 監視項目IDが不適切な場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.97", args));

				} catch (MonitorDuplicate e) {
					// 監視項目IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
	
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else {
				// 変更の場合
				try {
					ModifyPerformanceMonitorRequest info = new ModifyPerformanceMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setItemName(itemName);
					info.setMeasure(itemMeasure);
					info.setRunInterval(ModifyPerformanceMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(ModifyPerformanceMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.modifyPerformanceMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}
		return result;
	}
}
