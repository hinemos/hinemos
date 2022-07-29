/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobRpaOptionInfoResponse;
import org.openapitools.client.model.RpaToolRunCommandResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.GetRpaDirectParameterTableDefine;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.dialog.RpaDirectParameterDialog;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * RPAシナリオ 直接実行 シナリオ実行タブ用のコンポジットクラスです
 */
public class RpaDirectScenarioComposite extends Composite {
	/** ロガー */
	private static final Log m_log = LogFactory.getLog(RpaDirectScenarioComposite.class);
	/** スコープ用テキスト */
	private Text m_scopeFixedValueText = null;
	/** スコープ（ジョブ変数）用テキスト */
	private Text m_scopeJobParamText = null;
	/** ジョブ変数用ラジオボタン */
	private Button m_scopeJobParamRadio = null;
	/** 固定値用ラジオボタン */
	private Button m_scopeFixedValueRadio = null;
	/** スコープ参照用ボタン */
	private Button m_scopeFixedValueSelectButton = null;
	/** ファシリティID */
	private String m_facilityId = null;
	/** スコープ */
	private String m_facilityPath = null;
	/** ファシリティID（固定値用） */
	private String m_facilityIdFixed = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;
	/** 全てのノードで実行用ラジオボタン */
	private Button m_allNode = null;
	/** 正常終了するまでノードを順次リトライ用ラジオボタン */
	private Button m_retry = null;
	/** RPAツール種別コンボボックス */
	private ComboViewer m_rpaToolComboViewer = null;
	/** 実行ファイルパステキスト */
	private Text m_exeFilepathText = null;
	/** シナリオファイルパステキスト */
	private Text m_scenarioFilepathText = null;
	/** 実行パラメータテーブルビューア */
	private CommonTableViewer m_scenarioParameterViewer = null;
	/** 選択されている実行パラメータ */
	private List<Object> m_selectScenarioParameter = null;
	/** 実行パラメータ追加ボタン */
	private Button m_scenarioParamAddButton = null;
	/** 実行パラメータ変更ボタン */
	private Button m_scenarioParamModifyButton = null;
	/** 実行パラメータ削除ボタン */
	private Button m_scenarioParamDeleteButton = null;
	/** 実行パラメータ上へボタン */
	private Button m_scenarioParamUpButton = null;
	/** 実行パラメータ下へボタン */
	private Button m_scenarioParamDownButton = null;
	/** シェル */
	private Shell m_shell = null;
	/** RPAシナリオジョブ実行情報 */
	private JobRpaInfoResponse m_rpa = null;
	/** RPAツールIDとRPAツールのMap */
	private Map<String, RpaToolRunCommandResponse> m_rpaToolMap = new ConcurrentHashMap<>();
	/** 読み取り専用モードのフラグ */
	private boolean m_enabled = false;
	/** RPAシナリオジョブ種別 */
	private Integer m_rpaJobType = null;

	public RpaDirectScenarioComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		this.m_shell = this.getShell();
		initialize();
	}

	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());

		// スコープ（グループ）
		Group cmdScopeGroup = new Group(this, SWT.NONE);
		cmdScopeGroup.setText(Messages.getString("scope"));
		cmdScopeGroup.setLayout(new GridLayout(3, false));

		// スコープ：ジョブ変数（ラジオ）
		this.m_scopeJobParamRadio = new Button(cmdScopeGroup, SWT.RADIO);
		this.m_scopeJobParamRadio.setText(Messages.getString("job.parameter") + " : ");
		this.m_scopeJobParamRadio.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeJobParamRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_scopeJobParamText.setEditable(true);
					m_scopeFixedValueRadio.setSelection(false);
					m_scopeFixedValueSelectButton.setEnabled(false);
					m_facilityId = m_scopeJobParamText.getText();
				}
				update();
			}
		});

		// スコープ：ジョブ変数（テキスト）
		this.m_scopeJobParamText = new Text(cmdScopeGroup, SWT.BORDER);
		this.m_scopeJobParamText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scopeJobParamText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
				if (m_scopeJobParamRadio.getSelection()) {
					m_facilityId = m_scopeJobParamText.getText();
				}
			}
		});

		// dummy
		new Label(cmdScopeGroup, SWT.LEFT);

		// スコープ：固定値（ラジオ）
		this.m_scopeFixedValueRadio = new Button(cmdScopeGroup, SWT.RADIO);
		this.m_scopeFixedValueRadio.setText(Messages.getString("fixed.value") + " : ");
		this.m_scopeFixedValueRadio.setLayoutData(new GridData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValueRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_scopeFixedValueSelectButton.setEnabled(true);
					m_scopeJobParamRadio.setSelection(false);
					m_scopeJobParamText.setEditable(false);
				}
				update();
			}
		});

		// スコープ：固定値（テキスト）
		this.m_scopeFixedValueText = new Text(cmdScopeGroup, SWT.BORDER | SWT.READ_ONLY);
		this.m_scopeFixedValueText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scopeFixedValueText.addModifyListener(e -> update());

		// スコープ：参照（ボタン）
		this.m_scopeFixedValueSelectButton = new Button(cmdScopeGroup, SWT.NONE);
		this.m_scopeFixedValueSelectButton.setText(Messages.getString("refer"));
		this.m_scopeFixedValueSelectButton.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValueSelectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityInfoResponse info = selectItem.getData();
					FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityIdFixed = info.getFacilityId();
					m_scopeFixedValueText.setText(m_facilityPath);
					update();
				}
			}
		});

		// スコープ処理（グループ）
		Group cmdScopeProcGroup = new Group(this, SWT.NONE);
		cmdScopeProcGroup.setText(Messages.getString("scope.process"));
		cmdScopeProcGroup.setLayout(new RowLayout());

		// スコープ処理：全てのノード（ラジオ）
		this.m_allNode = new Button(cmdScopeProcGroup, SWT.RADIO);
		this.m_allNode.setText(Messages.getString("scope.process.all.nodes"));
		this.m_allNode.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));

		// スコープ処理：正常終了するまでリトライ（ラジオ）
		this.m_retry = new Button(cmdScopeProcGroup, SWT.RADIO);
		this.m_retry.setText(Messages.getString("scope.process.retry.nodes"));
		this.m_retry.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));

		// RPAツール種別（Composite）
		Composite rpaToolComposite = new Composite(this, SWT.NONE);
		rpaToolComposite.setLayout(new RowLayout());

		// RPAツール種別（ラベル）
		Label rpaToolLabel = new Label(rpaToolComposite, SWT.NONE);
		rpaToolLabel.setText(Messages.getString("rpa.kind") + " : ");
		rpaToolLabel.setLayoutData(new RowData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// RPAツール種別（コンボボックス）
		this.m_rpaToolComboViewer = new ComboViewer(rpaToolComposite, SWT.CENTER | SWT.READ_ONLY);
		this.m_rpaToolComboViewer.getCombo().setLayoutData(new RowData(120, SizeConstant.SIZE_COMBO_HEIGHT));
		// プルダウン項目を設定
		m_rpaToolComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		m_rpaToolComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RpaToolRunCommandResponse) {
					RpaToolRunCommandResponse rpaTool = (RpaToolRunCommandResponse) element;
					return rpaTool.getRpaToolName();
				}
				return super.getText(element);
			}
		});
		m_rpaToolComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				// 実行ファイルパスのデフォルト値を入力
				m_exeFilepathText.setText(((RpaToolRunCommandResponse) selection.getFirstElement()).getExeFilepath());
			}
		});

		// 実行ファイルパス（Composite）
		Composite exeFilepathComposite = new Composite(this, SWT.NONE);
		exeFilepathComposite.setLayout(new RowLayout());

		// 実行ファイルパス（ラベル）
		Label exeFilepathLabel = new Label(exeFilepathComposite, SWT.NONE);
		exeFilepathLabel.setText(Messages.getString("rpa.exe.path") + " : ");
		exeFilepathLabel.setLayoutData(new RowData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// 実行ファイルパス（テキスト）
		this.m_exeFilepathText = new Text(exeFilepathComposite, SWT.BORDER);
		this.m_exeFilepathText.setLayoutData(new RowData(300, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_exeFilepathText.addModifyListener(e -> update());

		// シナリオ（グループ）
		Group scenarioGroup = new Group(this, SWT.NONE);
		scenarioGroup.setText(Messages.getString("scenario"));
		scenarioGroup.setLayout(new GridLayout(1, false));

		// シナリオファイルパス（Composite）
		Composite scenarioFilepathComposite = new Composite(scenarioGroup, SWT.NONE);
		scenarioFilepathComposite.setLayout(new RowLayout());

		// シナリオファイルパス（ラベル）
		Label scenarioFilepathLabel = new Label(scenarioFilepathComposite, SWT.NONE);
		scenarioFilepathLabel.setText(Messages.getString("rpa.scenario.filepath") + " : ");
		scenarioFilepathLabel.setLayoutData(new RowData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// シナリオファイルパス（テキスト）
		this.m_scenarioFilepathText = new Text(scenarioFilepathComposite, SWT.BORDER);
		this.m_scenarioFilepathText.setLayoutData(new RowData(300, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scenarioFilepathText.addModifyListener(e -> update());

		// 実行パラメータ（Composite）
		Composite scenarioParamComposite = new Composite(scenarioGroup, SWT.NONE);
		scenarioParamComposite.setLayout(new RowLayout());

		// 実行パラメータ（ラベル）
		Label scenarioParamLabel = new Label(scenarioParamComposite, SWT.NONE);
		scenarioParamLabel.setText(Messages.getString("run.parameter") + " : ");
		scenarioParamLabel.setLayoutData(new RowData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// 実行パラメータ（テーブル）
		Table table = new Table(scenarioParamComposite,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new RowData(250, 100));
		this.m_scenarioParameterViewer = new CommonTableViewer(table);
		this.m_scenarioParameterViewer.createTableColumn(GetRpaDirectParameterTableDefine.get(),
				GetRpaDirectParameterTableDefine.SORT_COLUMN_INDEX, GetRpaDirectParameterTableDefine.SORT_ORDER);
		this.m_scenarioParameterViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					// 選択行を取得
					m_selectScenarioParameter = (List<Object>) ((StructuredSelection) event.getSelection())
							.getFirstElement();
				} else {
					m_selectScenarioParameter = null;
				}

			}
		});

		// 実行パラメータボタン(Composite)
		Composite scenarioParamButtonComposite = new Composite(scenarioParamComposite, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(1, false);
		// テーブルとボタンの上部を揃える
		buttonLayout.marginHeight = 0;
		scenarioParamButtonComposite.setLayout(buttonLayout);

		// 実行パラメータボタン(追加)
		m_scenarioParamAddButton = new Button(scenarioParamButtonComposite, SWT.NONE);
		m_scenarioParamAddButton.setText(Messages.getString("add"));
		m_scenarioParamAddButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_scenarioParamAddButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				RpaDirectParameterDialog dialog = new RpaDirectParameterDialog(m_shell);
				if (dialog.open() == IDialogConstants.OK_ID) {
					List<Object> newRow = dialog.getInputData();
					@SuppressWarnings("unchecked")
					List<List<Object>> parameterRows = (List<List<Object>>) m_scenarioParameterViewer.getInput();
					if (parameterRows == null) {
						parameterRows = new ArrayList<>();
					}
					// 新たなパラメータの行を末尾に追加
					newRow.add(GetRpaDirectParameterTableDefine.ORDER_NO, parameterRows.size() + 1);
					parameterRows.add(newRow);
					m_scenarioParameterViewer.setInput(parameterRows);
				}
			}
		});

		// 実行パラメータボタン(変更)
		m_scenarioParamModifyButton = new Button(scenarioParamButtonComposite, SWT.NONE);
		m_scenarioParamModifyButton.setText(Messages.getString("modify"));
		m_scenarioParamModifyButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_scenarioParamModifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RpaDirectParameterDialog dialog = new RpaDirectParameterDialog(m_shell);
				if (m_selectScenarioParameter != null) {
					dialog.setInputData(m_selectScenarioParameter);
					if (dialog.open() == IDialogConstants.OK_ID) {
						List<Object> modifiedRow = dialog.getInputData();
						// 順序を設定
						modifiedRow.add(GetRpaDirectParameterTableDefine.ORDER_NO,
								m_selectScenarioParameter.get(GetRpaDirectParameterTableDefine.ORDER_NO));
						@SuppressWarnings("unchecked")
						List<List<Object>> parameterRows = (List<List<Object>>) m_scenarioParameterViewer.getInput();
						int index = parameterRows.indexOf(m_selectScenarioParameter);
						parameterRows.set(index, modifiedRow);
						m_scenarioParameterViewer.setInput(parameterRows);
						m_selectScenarioParameter = modifiedRow;
						m_scenarioParameterViewer.refresh();
						selectItem(index);
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.rpa.39"));
				}
			}
		});

		// 実行パラメータボタン(削除)
		m_scenarioParamDeleteButton = new Button(scenarioParamButtonComposite, SWT.NONE);
		m_scenarioParamDeleteButton.setText(Messages.getString("delete"));
		m_scenarioParamDeleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_scenarioParamDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				@SuppressWarnings("unchecked")
				List<List<Object>> parameterRows = (List<List<Object>>) m_scenarioParameterViewer.getInput();
				if (m_selectScenarioParameter != null) {
					parameterRows.remove(m_selectScenarioParameter);
					refreshParameterOrder(parameterRows);
					m_scenarioParameterViewer.setInput(parameterRows);
					m_scenarioParameterViewer.refresh();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.rpa.39"));
				}
			}
		});

		// 実行パラメータボタン(上へ)
		m_scenarioParamUpButton = new Button(scenarioParamButtonComposite, SWT.NONE);
		m_scenarioParamUpButton.setText(Messages.getString("up"));
		m_scenarioParamUpButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_scenarioParamUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				@SuppressWarnings("unchecked")
				List<List<Object>> parameterRows = (List<List<Object>>) m_scenarioParameterViewer.getInput();
				if (m_selectScenarioParameter != null) {
					int index = parameterRows.indexOf(m_selectScenarioParameter);
					if (index > 0) {
						Collections.swap(parameterRows, index, index - 1);
						refreshParameterOrder(parameterRows);
						m_scenarioParameterViewer.setInput(parameterRows);
						m_scenarioParameterViewer.refresh();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.rpa.39"));
				}
			}
		});

		// 実行パラメータボタン(下へ)
		m_scenarioParamDownButton = new Button(scenarioParamButtonComposite, SWT.NONE);
		m_scenarioParamDownButton.setText(Messages.getString("down"));
		m_scenarioParamDownButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		m_scenarioParamDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				@SuppressWarnings("unchecked")
				List<List<Object>> parameterRows = (List<List<Object>>) m_scenarioParameterViewer.getInput();
				if (m_selectScenarioParameter != null) {
					int index = parameterRows.indexOf(m_selectScenarioParameter);
					if (index < parameterRows.size() - 1) {
						Collections.swap(parameterRows, index, index + 1);
						refreshParameterOrder(parameterRows);
						m_scenarioParameterViewer.setInput(parameterRows);
						m_scenarioParameterViewer.refresh();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.rpa.39"));
				}
			}
		});

		// RPA種別のプルダウンを生成
		List<RpaToolRunCommandResponse> rpaToolList;
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.m_managerName);
			rpaToolList = wrapper.getRpaToolRunCommand();
			// プルダウン項目を設定
			m_rpaToolComboViewer.setInput(rpaToolList);
			// 先頭の要素を選択しておく
			m_rpaToolComboViewer.setSelection(new StructuredSelection(rpaToolList.get(0)));
			// RPAツール名と実行ファイルパスのデフォルト値の表示のためのMapを作成
			for (RpaToolRunCommandResponse rpaTool : rpaToolList) {
				m_rpaToolMap.put(rpaTool.getRpaToolId(), rpaTool);
			}
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("initialize() : " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		}
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update() {
		// 必須項目を明示
		if (m_enabled && m_scopeFixedValueRadio.getSelection() && "".equals(this.m_scopeFixedValueText.getText())) {
			this.m_scopeFixedValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_scopeFixedValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_enabled && m_scopeJobParamRadio.getSelection() && "".equals(this.m_scopeJobParamText.getText())) {
			this.m_scopeJobParamText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_scopeJobParamText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_enabled && "".equals(this.m_exeFilepathText.getText())) {
			this.m_exeFilepathText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_exeFilepathText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_enabled && "".equals(this.m_scenarioFilepathText.getText())) {
			this.m_scenarioFilepathText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_scenarioFilepathText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	private void refreshParameterOrder(List<List<Object>> parameterRows) {
		// 順序を更新
		int order = 1;
		for (List<Object> parameterRow : parameterRows) {
			parameterRow.set(GetRpaDirectParameterTableDefine.ORDER_NO, order++);
		}
	}

	public void reflectRpaJobInfo() {
		// スコープ（ジョブ変数）の初期値は"#[FACILITY_ID]"とする
		m_scopeJobParamText.setText(SystemParameterConstant.getParamText(SystemParameterConstant.FACILITY_ID));
		if (this.m_rpa != null) {
			// スコープ設定
			if (m_rpa.getScope() != null) {
				this.m_facilityPath = HinemosMessage.replace(m_rpa.getScope());
			}
			this.m_facilityId = m_rpa.getFacilityID();
			if (this.m_facilityId != null) {
				if (isParamFormat(this.m_facilityId)) {
					// ファシリティIDがジョブ変数の場合
					this.m_facilityPath = "";
					this.m_scopeFixedValueText.setText(this.m_facilityPath);
					this.m_scopeJobParamRadio.setSelection(true);
					this.m_scopeJobParamText.setText(this.m_facilityId);
					this.m_scopeFixedValueRadio.setSelection(false);
				} else {
					if (this.m_facilityPath != null && this.m_facilityPath.length() > 0) {
						this.m_scopeFixedValueText.setText(this.m_facilityPath);
						this.m_facilityIdFixed = this.m_facilityId;
					}
					this.m_scopeJobParamRadio.setSelection(false);
					this.m_scopeFixedValueRadio.setSelection(true);
				}
			}
			// 処理方法設定
			if (m_rpa.getProcessingMethod() != null) {
				if (m_rpa.getProcessingMethod() == JobRpaInfoResponse.ProcessingMethodEnum.ALL_NODE) {
					m_allNode.setSelection(true);
					m_retry.setSelection(false);
				} else {
					m_allNode.setSelection(false);
					m_retry.setSelection(true);
				}
			}
			// RPAツール種別
			if (m_rpa.getRpaToolId() != null) {
				m_rpaToolComboViewer.setSelection(new StructuredSelection(m_rpaToolMap.get(m_rpa.getRpaToolId())));
			}
			// 実行ファイルパス
			if (m_rpa.getRpaExeFilepath() != null) {
				m_exeFilepathText.setText(m_rpa.getRpaExeFilepath());
			}
			if (m_rpa.getRpaScenarioFilepath() != null) {
				m_scenarioFilepathText.setText(m_rpa.getRpaScenarioFilepath());
			}
			// 実行パラメータ
			if (m_rpa.getRpaJobOptionInfos() != null) {
				List<Object> tableData = new ArrayList<Object>();
				for (JobRpaOptionInfoResponse param : m_rpa.getRpaJobOptionInfos()) {
					ArrayList<Object> tableLineData = new ArrayList<Object>();
					tableLineData.add(param.getOrderNo());
					tableLineData.add(param.getOption());
					tableLineData.add(param.getDescription());
					tableData.add(tableLineData);
				}
				m_scenarioParameterViewer.setInput(tableData);
			}
		} else {
			// 新規作成の場合はデフォルト値を表示
			m_scopeFixedValueRadio.setSelection(true);
			m_scopeFixedValueText.setText("");
			m_scopeJobParamRadio.setSelection(false);
			m_allNode.setSelection(true);
			m_retry.setSelection(false);
			m_scenarioFilepathText.setText("");
		}
	}

	public ValidateResult validateRpaJobInfo() {
		ValidateResult result = null;
		// スコープ
		if (m_scopeJobParamRadio.getSelection()) {
			// ジョブ変数の場合
			if (!isParamFormat(m_scopeJobParamText.getText())) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.4"));
				return result;
			}
		} else {
			// 固定値の場合
			if (m_rpaJobType == RpaJobTypeConstant.DIRECT
					&& (m_facilityIdFixed == null || m_facilityIdFixed.length() == 0)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.3"));
				return result;
			}
		}

		if (m_rpaJobType == RpaJobTypeConstant.DIRECT) {
			// 実行ファイルパス
			if (!JobDialogUtil.validateText(m_exeFilepathText)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.rpa.3"));
				return result;
			}

			// シナリオファイルパス
			if (!JobDialogUtil.validateText(m_scenarioFilepathText)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.rpa.4"));
				return result;
			}
		}

		return result;
	}

	public void createRpaJobInfo() {
		if (m_scopeJobParamRadio.getSelection()) {
			// ジョブ変数の場合
			m_rpa.setFacilityID(m_scopeJobParamText.getText());
			m_rpa.setScope("");
		} else {
			m_rpa.setFacilityID(m_facilityIdFixed);
			m_rpa.setScope(m_facilityPath);
		}

		// 処理方法取得
		if (m_allNode.getSelection()) {
			m_rpa.setProcessingMethod(JobRpaInfoResponse.ProcessingMethodEnum.ALL_NODE);
		} else {
			m_rpa.setProcessingMethod(JobRpaInfoResponse.ProcessingMethodEnum.RETRY);
		}

		// RPAツール種別取得
		IStructuredSelection rpaToolSelection = (StructuredSelection) m_rpaToolComboViewer.getSelection();
		m_rpa.setRpaToolId(((RpaToolRunCommandResponse) rpaToolSelection.getFirstElement()).getRpaToolId());

		// 実行ファイルパス
		m_rpa.setRpaExeFilepath(m_exeFilepathText.getText());

		// シナリオファイルパス
		m_rpa.setRpaScenarioFilepath(m_scenarioFilepathText.getText());

		// 実行パラメータ
		@SuppressWarnings("unchecked")
		List<List<Object>> parameterRows = (List<List<Object>>) m_scenarioParameterViewer.getInput();
		if (parameterRows != null) {
			m_rpa.setRpaJobOptionInfos(new ArrayList<>());
			parameterRows.forEach(r -> {
				JobRpaOptionInfoResponse option = new JobRpaOptionInfoResponse();
				option.setOrderNo((Integer) r.get(GetRpaDirectParameterTableDefine.ORDER_NO));
				option.setOption((String) r.get(GetRpaDirectParameterTableDefine.PARAMETER));
				option.setDescription((String) r.get(GetRpaDirectParameterTableDefine.DESCRIPTION));
				m_rpa.getRpaJobOptionInfos().add(option);
			});
		} else {
			m_rpa.setRpaJobOptionInfos(Collections.emptyList());
		}
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_scopeFixedValueText.setEditable(false);
		this.m_scopeJobParamRadio.setEnabled(enabled);
		this.m_scopeJobParamText.setEditable(m_scopeJobParamRadio.getSelection() && enabled);
		this.m_scopeFixedValueRadio.setEnabled(enabled);
		this.m_scopeFixedValueSelectButton.setEnabled(enabled);
		this.m_allNode.setEnabled(enabled);
		this.m_retry.setEnabled(enabled);
		this.m_rpaToolComboViewer.getCombo().setEnabled(enabled);
		this.m_exeFilepathText.setEditable(enabled);
		this.m_scenarioFilepathText.setEditable(enabled);
		this.m_scenarioParamAddButton.setEnabled(enabled);
		this.m_scenarioParamModifyButton.setEnabled(enabled);
		this.m_scenarioParamDeleteButton.setEnabled(enabled);
		this.m_scenarioParamUpButton.setEnabled(enabled);
		this.m_scenarioParamDownButton.setEnabled(enabled);
		this.m_enabled = enabled;
		update(); // 読み込み専用時は必須項目を明示しない
	}

	/**
	 * strがジョブ変数の書式(#[xxx])かどうかを判定する
	 * 
	 * @param str
	 * @return
	 */
	private boolean isParamFormat(String str) {
		if (str == null) {
			return false;
		}
		return str.startsWith(SystemParameterConstant.PREFIX) && str.endsWith(SystemParameterConstant.SUFFIX);
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_scopeFixedValueText.setText("");
		this.m_facilityId = null;
	}

	/**
	 * @return the m_rpa
	 */
	public JobRpaInfoResponse getRpaJobInfo() {
		return m_rpa;
	}

	/**
	 * @param m_rpa
	 *            the m_rpa to set
	 */
	public void setRpaJobInfo(JobRpaInfoResponse rpa) {
		this.m_rpa = rpa;
	}

	/**
	 * RPAシナリオジョブ種別を設定します。<br>
	 * 必須項目のチェック有無を判断するために使用します。
	 * 
	 * @param rpaJobType
	 */
	public void setRpaJobType(Integer rpaJobType) {
		this.m_rpaJobType = rpaJobType;
	}
	
	/**
	 * 引数で指定された実行パラメータの行を選択状態にします。
	 *
	 * @param order 実行パラメータの順序
	 */
	private void selectItem(Integer order) {
		Table scenarioParameterItemTable = m_scenarioParameterViewer.getTable();
		TableItem[] items = scenarioParameterItemTable.getItems();

		if (items == null || order == null) {
			return;
		}
		scenarioParameterItemTable.select(order);
		return;
	}
}
