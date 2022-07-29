/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.dialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.RpaScenarioOperationResultWithDetailResponse;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.RpaScenarioEndStatusMessage;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.composite.RpaScenarioOperationResultDetailComposite;
import com.clustercontrol.rpa.composite.RpaScenarioOperationResultSearchComposite;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * シナリオ実績[レコードの詳細]ダイアログクラス<BR>
 */
public class RpaScenarioOperationResultDetailDialog extends CommonDialog {

	// ログ.
	private static Log log = LogFactory.getLog(RpaScenarioOperationResultDetailDialog.class);

	/** プロパティシート */
	private PropertySheet propertySheet = null;
	/** プロパティ */
	private Property property = null;
	private Long resultId = null;
	
	/** マネージャー名 */
	private String managerName = null;

	/** シナリオID */
	private Text txtScenarioId = null;

	/** シナリオ名 */
	private Text txtScenarioName = null;

	/** RPAツール */
	private Text txtRpaTool = null;

	/** ファシリティID */
	private Text txtFacilityId = null;
	
	/** ファシリティ名 */
	private Text txtFacilityName = null;

	/** 開始日時 */
	private Text txtStartDate = null;

	/** 終了日時 */
	private Text txtEndDate = null;

	/** ステップ数 */
	private Text txtStepCount = null;

	/** 実行時間 */
	private Text txtRunTime = null;

	/** ステータス */
	private Text txtStatus = null;

	/** 手動操作時間 */
	private Text txtManualTime = null;

	/** 手動操作コスト */
	private Text txtCoefficientCost = null;

	/** 削減時間 */
	private Text txtReductionTime = null;

	/** 削減率 */
	private Text txtReductionRate = null;
	
	/** シナリオ実績詳細コンポジット */
	private RpaScenarioOperationResultDetailComposite resultDetailComposite = null;

	/** 入力値を保持するオブジェクト */
	private RpaScenarioOperationResultWithDetailResponse resultData = null;
		
	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaScenarioOperationResultDetailDialog(Shell parentShell, String managerName, Long resultId) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		
		this.managerName = managerName;
		this.resultId = resultId;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		shell.setText(Messages.getString("dialog.rpa.scenario.operation.result.detail.info.records"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// 表示UI
		Composite resultComposite = new Composite(parent, SWT.NONE);
		resultComposite.setLayout(new GridLayout(1, false));

		// シナリオID
		Composite compositeScenarioId = new Composite(resultComposite, SWT.NONE);
		GridLayout gl_compositeScenarioId = new GridLayout(3, false);
		gl_compositeScenarioId.marginHeight = 0;
		compositeScenarioId.setLayout(gl_compositeScenarioId);
		GridData gd_compositeScenarioId = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeScenarioId.setLayoutData(gd_compositeScenarioId);
		
		Label lblScenarioId = new Label(compositeScenarioId, SWT.NONE);
		GridData gd_lblScenarioId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblScenarioId.widthHint = 100;
		lblScenarioId.setLayoutData(gd_lblScenarioId);
		lblScenarioId.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.scenario.id"));

		txtScenarioId = new Text(compositeScenarioId, SWT.BORDER);
		GridData gd_txtScenarioId = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtScenarioId.widthHint = 200;
		txtScenarioId.setLayoutData(gd_txtScenarioId);
		txtScenarioId.setEnabled(false);
		
		// シナリオ名
		Composite compositeScenarioName = new Composite(compositeScenarioId, SWT.NONE);
		GridLayout gl_compositeScenarioName = new GridLayout(3, false);
		gl_compositeScenarioName.marginHeight = 0;
		compositeScenarioName.setLayout(gl_compositeScenarioName);
		GridData gd_compositeScenarioName = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeScenarioName.setLayoutData(gd_compositeScenarioName);
		
		Label lblScenarioName = new Label(compositeScenarioName, SWT.NONE);
		GridData gd_lblScenarioName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblScenarioName.widthHint = 150;
		lblScenarioName.setLayoutData(gd_lblScenarioName);
		lblScenarioName.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.scenario.name"));

		txtScenarioName = new Text(compositeScenarioName, SWT.BORDER);
		GridData gd_txtScenarioName = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtScenarioName.widthHint = 200;
		txtScenarioName.setLayoutData(gd_txtScenarioName);
		txtScenarioName.setEnabled(false);
		
		// RPAツール
		Composite compositeRpaTool = new Composite(compositeScenarioName, SWT.NONE);
		GridLayout gl_compositeRpaTool = new GridLayout(3, false);
		gl_compositeRpaTool.marginHeight = 0;
		compositeRpaTool.setLayout(gl_compositeRpaTool);
		GridData gd_compositeRpaTool = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeRpaTool.setLayoutData(gd_compositeRpaTool);
		
		Label lblRpaTool = new Label(compositeRpaTool, SWT.NONE);
		GridData gd_lblRpaTool = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblRpaTool.widthHint = 80;
		lblRpaTool.setLayoutData(gd_lblRpaTool);
		lblRpaTool.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.rpatool"));

		txtRpaTool = new Text(compositeRpaTool, SWT.BORDER);
		GridData gd_txtRpaTool = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtRpaTool.widthHint = 200;
		txtRpaTool.setLayoutData(gd_txtRpaTool);
		txtRpaTool.setEnabled(false);
		
		// ファシリティID
		Composite compositeFacilityId = new Composite(resultComposite, SWT.NONE);
		GridLayout gl_compositeFacilityId = new GridLayout(4, false);
		gl_compositeFacilityId.marginHeight = 0;
		compositeFacilityId.setLayout(gl_compositeFacilityId);
		GridData gd_compositeFacilityId = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeFacilityId.setLayoutData(gd_compositeFacilityId);
		
		Label lblFacilityId = new Label(compositeFacilityId, SWT.NONE);
		GridData gd_lblFacilityId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblFacilityId.widthHint = 100;
		lblFacilityId.setLayoutData(gd_lblFacilityId);
		lblFacilityId.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.facility.id"));

		txtFacilityId = new Text(compositeFacilityId, SWT.BORDER);
		GridData gd_txtFacilityId = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtFacilityId.widthHint = 200;
		txtFacilityId.setLayoutData(gd_txtFacilityId);
		txtFacilityId.setEnabled(false);
		
		// ファシリティ名
		Composite compositeFacilityName = new Composite(compositeFacilityId, SWT.NONE);
		GridLayout gl_compositeFacilityName = new GridLayout(3, false);
		gl_compositeFacilityName.marginHeight = 0;
		compositeFacilityName.setLayout(gl_compositeFacilityName);
		GridData gd_compositeFacilityName = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeFacilityName.setLayoutData(gd_compositeFacilityName);
		
		Label lblFacilityName = new Label(compositeFacilityName, SWT.NONE);
		GridData gd_lblFacilityName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblFacilityName.widthHint = 150;
		lblFacilityName.setLayoutData(gd_lblFacilityName);
		lblFacilityName.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.facility.name"));

		txtFacilityName = new Text(compositeFacilityName, SWT.BORDER);
		GridData gd_txtFacilityName = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtFacilityName.widthHint = 200;
		txtFacilityName.setLayoutData(gd_txtFacilityName);
		txtFacilityName.setEnabled(false);
		
		// 実行日時
		Composite compositeStartDate = new Composite(compositeFacilityId, SWT.NONE);
		GridLayout gl_compositeStartDate = new GridLayout(4, false);
		gl_compositeStartDate.marginHeight = 0;
		compositeStartDate.setLayout(gl_compositeStartDate);
		GridData gd_compositeStartDate = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_compositeStartDate.horizontalIndent = -10;
		compositeStartDate.setLayoutData(gd_compositeStartDate);
		
		Label lblStartDateFrom = new Label(compositeStartDate, SWT.NONE);
		GridData gd_lblStartDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblStartDate.widthHint = 80;
		lblStartDateFrom.setLayoutData(gd_lblStartDate);
		lblStartDateFrom.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.date.run"));

		txtStartDate = new Text(compositeStartDate, SWT.BORDER);
		GridData gd_txtStartDate = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtStartDate.widthHint = 150;
		txtStartDate.setLayoutData(gd_txtStartDate);
		txtStartDate.setEnabled(false);
		
		Label lblStartEndDate = new Label(compositeStartDate, SWT.NONE);
		lblStartEndDate.setAlignment(SWT.CENTER);
		GridData gd_lblStartEndDate = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblStartEndDate.widthHint = 39;
		lblStartEndDate.setLayoutData(gd_lblStartEndDate);
		lblStartEndDate.setText(Messages.getString("wave"));
		
		txtEndDate = new Text(compositeStartDate, SWT.BORDER);
		GridData gd_txtEndDate = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtEndDate.widthHint = 150;
		txtEndDate.setLayoutData(gd_txtEndDate);
		txtEndDate.setEnabled(false);
		
		// 実績ラベル
		Composite compositeResultLabel = new Composite(resultComposite, SWT.NONE);
		GridLayout gl_compositeResultLabel = new GridLayout(2, false);
		gl_compositeResultLabel.marginHeight = 0;
		compositeResultLabel.setLayout(gl_compositeResultLabel);
		GridData gd_compositeResultLabel = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeResultLabel.setLayoutData(gd_compositeResultLabel);
		
		Label lblResultLabel = new Label(compositeResultLabel, SWT.NONE);
		GridData gd_lblResultLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblResultLabel.widthHint = 100;
		lblResultLabel.setLayoutData(gd_lblResultLabel);
		lblResultLabel.setText(Messages.getString("view.rpa.scenario.operation.result.search.label.result"));
		
		// ステップ数
		Composite compositeStepCount = new Composite(compositeResultLabel, SWT.NONE);
		GridLayout gl_compositeStepCount = new GridLayout(3, false);
		gl_compositeStepCount.marginHeight = 0;
		compositeStepCount.setLayout(gl_compositeStepCount);
		GridData gd_compositeStepCount = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeStepCount.setLayoutData(gd_compositeStepCount);
		
		Label lblStepCount = new Label(compositeStepCount, SWT.NONE);
		GridData gd_lblStepCount = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblStepCount.widthHint = 140;
		lblStepCount.setLayoutData(gd_lblStepCount);
		lblStepCount.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.step"));

		txtStepCount = new Text(compositeStepCount, SWT.BORDER);
		GridData gd_txtStepCount = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtStepCount.widthHint = 50;
		txtStepCount.setLayoutData(gd_txtStepCount);
		txtStepCount.setEnabled(false);
		
		// 実行時間
		Composite compositeRunTime = new Composite(compositeStepCount, SWT.NONE);
		GridLayout gl_compositeRunTime = new GridLayout(3, false);
		gl_compositeRunTime.marginHeight = 0;
		compositeRunTime.setLayout(gl_compositeRunTime);
		GridData gd_compositeRunTime = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
		compositeRunTime.setLayoutData(gd_compositeRunTime);
		
		Label lblRunTime = new Label(compositeRunTime, SWT.NONE);
		GridData gd_lblRunTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblRunTime.widthHint = 150;
		lblRunTime.setLayoutData(gd_lblRunTime);
		lblRunTime.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.runtime"));

		txtRunTime = new Text(compositeRunTime, SWT.BORDER);
		GridData gd_txtRunTime = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtRunTime.widthHint = 100;
		txtRunTime.setLayoutData(gd_txtRunTime);
		txtRunTime.setEnabled(false);
		
		// ステータス
		Composite compositeStatus = new Composite(compositeRunTime, SWT.NONE);
		GridLayout gl_compositeStatus = new GridLayout(3, false);
		gl_compositeStatus.marginHeight = 0;
		compositeStatus.setLayout(gl_compositeStatus);
		GridData gd_compositeStatus = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_compositeStatus.horizontalIndent = 10;
		compositeStatus.setLayoutData(gd_compositeStatus);
		
		Label lblStatus = new Label(compositeStatus, SWT.NONE);
		GridData gd_lblStatus = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblStatus.widthHint = 100;
		lblStatus.setLayoutData(gd_lblStatus);
		lblStatus.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.status"));

		txtStatus = new Text(compositeStatus, SWT.BORDER);
		GridData gd_txtStatus = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtStatus.widthHint = 100;
		txtStatus.setLayoutData(gd_txtStatus);
		txtStatus.setEnabled(false);
		
		// 削減工数ラベル
		Composite compositeReductionLabel = new Composite(resultComposite, SWT.NONE);
		GridLayout gl_compositeReductionLabel = new GridLayout(2, false);
		gl_compositeReductionLabel.marginHeight = 0;
		compositeReductionLabel.setLayout(gl_compositeReductionLabel);
		GridData gd_compositeReductionLabel = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeReductionLabel.setLayoutData(gd_compositeReductionLabel);
		
		Label lblReductionLabel = new Label(compositeReductionLabel, SWT.NONE);
		GridData gd_lblReductionLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblReductionLabel.widthHint = 100;
		lblReductionLabel.setLayoutData(gd_lblReductionLabel);
		lblReductionLabel.setText(Messages.getString("view.rpa.scenario.operation.result.search.label.reduction"));
		
		// 手動操作時間
		Composite compositeManualTime = new Composite(compositeReductionLabel, SWT.NONE);
		GridLayout gl_compositeManualTime = new GridLayout(3, false);
		gl_compositeManualTime.marginHeight = 0;
		compositeManualTime.setLayout(gl_compositeManualTime);
		GridData gd_compositeManualTime = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeManualTime.setLayoutData(gd_compositeManualTime);
		
		Label lblManualTime = new Label(compositeManualTime, SWT.NONE);
		GridData gd_lblManualTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblManualTime.widthHint = 140;
		lblManualTime.setLayoutData(gd_lblManualTime);
		lblManualTime.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.manualtime"));

		txtManualTime = new Text(compositeManualTime, SWT.BORDER);
		GridData gd_txtManualTime = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtManualTime.widthHint = 50;
		txtManualTime.setLayoutData(gd_txtManualTime);
		txtManualTime.setEnabled(false);
		
		// 手動操作コスト
		Composite compositeCoefficientCost = new Composite(compositeManualTime, SWT.NONE);
		GridLayout gl_compositeCoefficientCost = new GridLayout(3, false);
		gl_compositeCoefficientCost.marginHeight = 0;
		compositeCoefficientCost.setLayout(gl_compositeCoefficientCost);
		GridData gd_compositeCoefficientCost = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		compositeCoefficientCost.setLayoutData(gd_compositeCoefficientCost);
		
		Label lblCoefficientCost = new Label(compositeCoefficientCost, SWT.NONE);
		GridData gd_lblCoefficientCost = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblCoefficientCost.widthHint = 150;
		lblCoefficientCost.setLayoutData(gd_lblCoefficientCost);
		lblCoefficientCost.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.coefficient.cost"));

		txtCoefficientCost = new Text(compositeCoefficientCost, SWT.BORDER);
		GridData gd_txtCoefficientCost = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtCoefficientCost.widthHint = 50;
		txtCoefficientCost.setLayoutData(gd_txtCoefficientCost);
		txtCoefficientCost.setEnabled(false);
		
		// 削減時間
		Composite compositeReductionTime = new Composite(compositeCoefficientCost, SWT.NONE);
		GridLayout gl_compositeReductionTime = new GridLayout(3, false);
		gl_compositeReductionTime.marginHeight = 0;
		compositeReductionTime.setLayout(gl_compositeReductionTime);
		GridData gd_compositeReductionTime = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_compositeReductionTime.horizontalIndent = 60;
		compositeReductionTime.setLayoutData(gd_compositeReductionTime);
		
		Label lblReductionTime = new Label(compositeReductionTime, SWT.NONE);
		GridData gd_lblReductionTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblReductionTime.widthHint = 100;
		lblReductionTime.setLayoutData(gd_lblReductionTime);
		lblReductionTime.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.reduction.time"));

		txtReductionTime = new Text(compositeReductionTime, SWT.BORDER);
		GridData gd_txtReductionTime = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtReductionTime.widthHint = 100;
		txtReductionTime.setLayoutData(gd_txtReductionTime);
		txtReductionTime.setEnabled(false);
		
		// 削減率
		Composite compositeReductionRate = new Composite(compositeReductionTime, SWT.NONE);
		GridLayout gl_compositeReductionRate = new GridLayout(3, false);
		gl_compositeReductionRate.marginHeight = 0;
		compositeReductionRate.setLayout(gl_compositeReductionRate);
		GridData gd_compositeReductionRate = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_compositeReductionRate.horizontalIndent = 10;
		compositeReductionRate.setLayoutData(gd_compositeReductionRate);
		
		Label lblReductionRate = new Label(compositeReductionRate, SWT.NONE);
		GridData gd_lblReductionRate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblReductionRate.widthHint = 100;
		lblReductionRate.setLayoutData(gd_lblReductionRate);
		lblReductionRate.setText(Messages.getString("view.rpa.scenario.operation.result.search.column.reduction.rate"));

		txtReductionRate = new Text(compositeReductionRate, SWT.BORDER);
		GridData gd_txtReductionRate = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtReductionRate.widthHint = 50;
		txtReductionRate.setLayoutData(gd_txtReductionRate);
		txtReductionRate.setEnabled(false);
		
		setRpaScenarioOperationResult();
		
		// 詳細一覧
		this.resultDetailComposite = new RpaScenarioOperationResultDetailComposite(parent, SWT.BORDER, this.resultData);
		this.resultDetailComposite.setManagerName(this.managerName);
		WidgetTestUtil.setTestId(this, "taglist", resultDetailComposite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		gridData.heightHint = SWT.MIN;
		this.resultDetailComposite.setLayoutData(gridData);
		this.resultDetailComposite.update();

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
		
		// サイズを最適化
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, 500));
	}

	private void setRpaScenarioOperationResult(){
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
		try {
			this.resultData = wrapper.getRpaScenarioOperationResultWithDetail(resultId);
		} catch (InvalidRole e) {
			// 権限なし
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			// 上記以外の例外
			String errMessage = HinemosMessage.replace(e.getMessage());
			log.warn("update(), " + errMessage, e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		
		SimpleDateFormat df = TimezoneUtil.getSimpleDateFormat();
		
		this.txtScenarioId.setText(this.resultData.getScenarioId());
		this.txtScenarioName.setText(this.resultData.getScenarioName());
		this.txtRpaTool.setText(this.resultData.getRpaToolId());
		this.txtFacilityId.setText(this.resultData.getFacilityId());
		this.txtFacilityName.setText(this.resultData.getFacilityName());
		this.txtStartDate.setText(df.format(new Date(this.resultData.getStartDate())));
		this.txtStepCount.setText(this.resultData.getStep().toString());
		this.txtRunTime.setText(RpaScenarioOperationResultSearchComposite.convertTimeToHMS(this.resultData.getRunTime()));
		this.txtStatus.setText(RpaScenarioEndStatusMessage.typeEnumValueToString(this.resultData.getStatus().getValue()));

		// 以下パラメータは、nullの場合がある。
		if (this.resultData.getEndDate() != null) {
			// シナリオ実績が実行中の場合、終了時刻はnull;
			this.txtEndDate.setText(df.format(new Date(this.resultData.getEndDate())));
		}
		
		if (this.resultData.getManualTime() != null) {
			this.txtManualTime.setText(RpaScenarioOperationResultSearchComposite.convertTimeToHMS(this.resultData.getManualTime()));
		}
		
		if (this.resultData.getCoefficientCost() != null) {
			this.txtCoefficientCost.setText(
					this.resultData.getCoefficientCost() != null ? this.resultData.getCoefficientCost().toString() + "%":"");
		}
		
		if (this.resultData.getReductionTime() != null) {
			this.txtReductionTime.setText(RpaScenarioOperationResultSearchComposite.convertTimeToHMS(this.resultData.getReductionTime()));
		}
		
		if (this.resultData.getReductionRate() != null) {
			this.txtReductionRate.setText(
					this.resultData.getReductionRate() != null ? this.resultData.getReductionRate() + "%":"");
		}
	}

	/**
	 * 入力値を保持したプロパティを返します。<BR>
	 * プロパティシートよりプロパティを取得します。
	 *
	 * @return プロパティ
	 *
	 * @see com.clustercontrol.viewer.PropertySheet#getInput()
	 */
	public Property getInputData() {
		if (property != null) {
			Property copy = PropertyUtil.copy(property);
			return copy;
		} else {
			return null;
		}
	}

	/**
	 * 入力値を保持したプロパティを設定します。
	 *
	 * @param property
	 *            プロパティ
	 */
	public void setInputData(Property property) {
		propertySheet.setInput(property);
	}

	/**
	 * ボタンを作成.
	 *
	 * @param parent
	 *            親のコンポジット（ボタンバー）
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// 閉じる(OK)ボタン
		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("message.ok"), false);
	}
}
