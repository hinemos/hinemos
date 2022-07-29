/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.DownloadRpaScenarioOperationResultRecordsRequest;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオダウンロードダイアログクラス
 */
public class RpaScenarioOperationResultDownloadDialog extends CommonDialog{
	
	DownloadRpaScenarioOperationResultRecordsRequest downloadRecordsRequest;
	
	private Button btnScenarioOperationResult;
	private Button btnScenario;
	private Button btnScenarioTag;
	private Button btnScope;
	
	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaScenarioOperationResultDownloadDialog(Shell parent) {
		super(parent);
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
		shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.rpa.scenario.operation.result.download"));
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
		
		/*
		 * ダウンロード対象
		 */
		Composite downloadComposite = new Composite(composite, SWT.NONE);
		GridLayout downloadGridLayout = new GridLayout(4, false);
		downloadGridLayout.marginLeft = 3;
		downloadComposite.setLayout(downloadGridLayout);
		//ラベル
		Label labelDonwloadTarget = new Label(downloadComposite, SWT.NONE);
		gridData = new GridData(300, SizeConstant.SIZE_LABEL_HEIGHT);
		labelDonwloadTarget.setLayoutData(gridData);
		labelDonwloadTarget.setText(Messages.getString("dialog.rpa.scenario.operation.result.download.target"));
		
		//シナリオ実績
		btnScenarioOperationResult = new Button(downloadComposite, SWT.CHECK);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
		btnScenarioOperationResult.setLayoutData(gridData);
		btnScenarioOperationResult.setText(Messages.getString("rpa.scenario.operation.result"));
		btnScenarioOperationResult.setSelection(true);
		btnScenarioOperationResult.setEnabled(false);
		
		/*
		 * 分析用情報
		 */
		// グループ
		Group groupAnalysis = new Group(composite, SWT.NONE);
		GridLayout analysisGroupLayout = new GridLayout(1, true);
		analysisGroupLayout.marginWidth = 5;
		analysisGroupLayout.marginHeight = 5;
		analysisGroupLayout.numColumns = 1;
		groupAnalysis.setLayout(analysisGroupLayout);
		groupAnalysis.setText(Messages.getString("dialog.rpa.scenario.operation.result.download.information"));
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupAnalysis.setLayoutData(gridData);
		
		//シナリオ
		btnScenario = new Button(groupAnalysis, SWT.CHECK);
		btnScenario.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnScenario.setText(Messages.getString("scenario"));
		btnScenario.setSelection(true);
		
		//シナリオタグ
		btnScenarioTag = new Button(groupAnalysis, SWT.CHECK);
		btnScenarioTag.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnScenarioTag.setText(Messages.getString("rpa.tag"));
		btnScenarioTag.setSelection(true);
		
		//スコープ
		btnScope = new Button(groupAnalysis, SWT.CHECK);
		btnScope.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnScope.setText(Messages.getString("scope"));
		btnScope.setSelection(true);
		
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
	 * ダイアログの情報からCSVをダウンロードする為の情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		
		downloadRecordsRequest = new DownloadRpaScenarioOperationResultRecordsRequest();
		downloadRecordsRequest.setScenarioOperationResultFlg(false);
		downloadRecordsRequest.setScenarioFlg(false);
		downloadRecordsRequest.setScenarioTagFlg(false);
		downloadRecordsRequest.setScopeFlg(false);
		
		if(btnScenarioOperationResult.getSelection()){
			downloadRecordsRequest.setScenarioOperationResultFlg(true);
		}
		if(btnScenario.getSelection()){
			downloadRecordsRequest.setScenarioFlg(true);
		}
		if(btnScenarioTag.getSelection()){
			downloadRecordsRequest.setScenarioTagFlg(true);
		}
		if(btnScope.getSelection()){
			downloadRecordsRequest.setScopeFlg(true);
		}
		
		if (!downloadRecordsRequest.getScenarioOperationResultFlg() && !downloadRecordsRequest.getScenarioFlg() &&
				!downloadRecordsRequest.getScenarioTagFlg() && !downloadRecordsRequest.getScopeFlg()){
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.rpa.scenario.operation.result.download.nochecked"));
			return result;
		}
		
		return result;
	}
	
	public DownloadRpaScenarioOperationResultRecordsRequest getDownloadRecordsRequest(){
		return downloadRecordsRequest;
	}
}
