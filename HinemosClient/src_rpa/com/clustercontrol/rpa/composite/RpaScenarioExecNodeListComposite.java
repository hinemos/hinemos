/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.GetRpaScenarioCorrectExecNodeResponse;
import org.openapitools.client.model.RpaScenarioExecNodeResponse;
import org.openapitools.client.model.RpaScenarioResponseP1;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.rpa.action.GetRpaScenario;
import com.clustercontrol.rpa.dialog.RpaScenarioDialog;
import com.clustercontrol.rpa.util.RpaScenarioCorrectExecNodePropertyUtil;
import com.clustercontrol.rpa.viewer.RpaScenarioExecNodePropertySheet;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * 実行ノード一覧コンポジットクラス<BR>
 */
public class RpaScenarioExecNodeListComposite extends Composite {

	/** 新規シナリオ追加ボタン*/
	private Button buttonRefer = null;

	/** マネージャ名*/
	private String managerName = null;
	
	/** シナリオID*/
	private String scenarioId = null;
	
	/** プロパティ */
	private Property property = null;
	
	/** ノード属性プロパティシート */
	private RpaScenarioExecNodePropertySheet propertySheet = null;
	private Tree tree = null;
	
	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSetting = null;
	
	/** シナリオ識別子 */
	private String scenarioIdentifyString = null;
	
	/** RPAツールID */
	private String rpaToolId = null;
	
	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite, boolean)
	 */
	public RpaScenarioExecNodeListComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize(parent);
	}


	/**
	 * コンポジットを配置します。
	 *
	 * @see #update()
	 */
	private void initialize(Composite parent) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 10;

		this.setLayout(layout);

		// プロパティシート
		tree = new Tree(this, SWT.BORDER | SWT.H_SCROLL | SWT.MULTI
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, tree);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 100;
		tree.setLayoutData(gridData);
		
		//ボタン位置調整用のダミー
		Label dummy = new Label(this, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		dummy.setLayoutData(gridData);
		
		// 新規シナリオ追加ボタン
		this.buttonRefer = new Button(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "refer", buttonRefer);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.buttonRefer.setLayoutData(gridData);
		this.buttonRefer.setText(Messages.getString("dialog.rpa.scenario.create"));
		this.buttonRefer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				// ダイアログ表示及び終了処理
				RpaScenarioDialog dialog = new RpaScenarioDialog(shell, managerName, scenarioOperationResultCreateSetting, scenarioIdentifyString, rpaToolId, PropertyDefineConstant.MODE_ADD);
				if (dialog.open() == IDialogConstants.OK_ID) {
					update();
				}
			}
		});
	}

	/**
	 * コンポジットを更新します。<BR>
	 */
	@Override
	public void update() {
		GetRpaScenarioCorrectExecNodeResponse scenarioInfo = 
				new GetRpaScenario().getRpaScenarioCorrectExecNode(this.managerName, this.scenarioId);
		setExecNodeList(scenarioInfo.getExecNodeList(), scenarioInfo.getScenarioList());
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.buttonRefer.setEnabled(enabled);
		this.tree.setEnabled(enabled);
	}

	public void setButtonEnabled(boolean enabled) {
		this.buttonRefer.setEnabled(enabled);
	}

	/**
	 * 実行ノードを設定します。
	 */
	public void setExecNode(List<RpaScenarioExecNodeResponse> execNodeList, List<RpaScenarioResponseP1> scenarioList) {
		this.propertySheet = new RpaScenarioExecNodePropertySheet(tree);
		this.propertySheet.setSize(230, 300);
		setExecNodeList(execNodeList, scenarioList);
	}
	
	/**
	 * 実行ノード一覧を更新します。
	 */
	public void setExecNodeList(List<RpaScenarioExecNodeResponse> execNodeList, List<RpaScenarioResponseP1> scenarioList) {
		property = RpaScenarioCorrectExecNodePropertyUtil.getProperty(execNodeList, scenarioList);
		// プロパティ設定
		this.propertySheet.setInput(property);
	}

	/**
	 * 入力値を保持したデータモデルを生成します。
	 *
	 * @return データモデル
	 */
	public Property getInputData() {
		return (Property) this.propertySheet.getInput();
	}

	/**
	 * マネージャをセットします。
	 */
	public String getManagerName() {
		return this.managerName;
	}

	/**
	 * マネージャを返します。
	 */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
	
	/**
	 * シナリオIDを返します。
	 */
	public String getScenarioId() {
		return this.scenarioId;
	}

	/**
	 * シナリオIDをセットします。
	 */
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}


	/**
	 * 実績作成設定IDをセットします。
	 */
	public void setScenarioOperationResultCreateSetting(String scenarioOperationResultCreateSetting) {
		this.scenarioOperationResultCreateSetting = scenarioOperationResultCreateSetting;
	}


	/**
	 * シナリオ識別子をセットします。
	 */
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
	}


	/**
	 * RPAツールIDをセットします。
	 */
	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}
}
