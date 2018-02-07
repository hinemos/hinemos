/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetControlNextJobOrderTableDefine;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobNextJobOrderInfo;
import com.clustercontrol.ws.jobmanagement.JobObjectInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;

public class ExclusiveBranchComposite extends Composite{

	/** 排他分岐用チェックボタン */
	private Button m_exclusiveBranchCondition;

	/** 排他分岐用終了状態用テキスト  */
	private Combo m_exclusiveBranchEndStatus;

	/** 排他分岐用終了値用テキスト  */
	private Text m_exclusiveBranchEndValue;

	/** 排他分岐用後続ジョブ優先度テーブル*/
	private Table m_exclusiveBranchNextJobOrderTable = null;
	private Button m_exclusiveBranchNextJobOrderTableButtonUp = null;
	private Button m_exclusiveBranchNextJobOrderTableButtonDown = null;

	/** 排他分岐用後続ジョブ優先度*/
	private List<JobNextJobOrderInfo> m_exclusiveBranchNextJobOrderList = null;
	private CommonTableViewer m_viewer = null;
	private JobWaitRuleInfo m_waitRule;

	private JobTreeItem m_jobTreeItem;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public ExclusiveBranchComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// 排他分岐（ラジオ）
		this.m_exclusiveBranchCondition = new Button(this, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_exclusiveBranchCondition", this.m_exclusiveBranchCondition);
		this.m_exclusiveBranchCondition.setText(Messages.getString("job.exclusive.branch.flg"));
		this.m_exclusiveBranchCondition.setLayoutData(new RowData(200,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_exclusiveBranchCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_exclusiveBranchEndStatus.setEnabled(true);
					m_exclusiveBranchEndValue.setEditable(true);
					m_exclusiveBranchNextJobOrderTableButtonUp.setEnabled(true);
					m_exclusiveBranchNextJobOrderTableButtonDown.setEnabled(true);
					reflectNextJobOrderTable();
				} else {
					m_exclusiveBranchEndStatus.setEnabled(false);
					m_exclusiveBranchEndValue.setEditable(false);
					m_exclusiveBranchNextJobOrderTableButtonUp.setEnabled(false);
					m_exclusiveBranchNextJobOrderTableButtonDown.setEnabled(false);
					reflectNextJobOrderTable();
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 排他分岐（Composite）
		Composite exclusiveBranchConditionGroup = new Composite(this, SWT.BORDER);
		exclusiveBranchConditionGroup.setLayout(new GridLayout(1, false));

		// 排他分岐：終了状態（ラベル）
		Composite exclusiveBranchComposite = new Composite(exclusiveBranchConditionGroup, SWT.NONE);
		exclusiveBranchComposite.setLayout(new GridLayout(2, false));

		Label exclusiveBranchStatusTitle = new Label(exclusiveBranchComposite, SWT.LEFT);
		exclusiveBranchStatusTitle.setText(Messages.getString("job.exclusive.branch.end.status") + " : ");
		exclusiveBranchStatusTitle.setLayoutData(new GridData(240,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 排他分岐：終了状態（コンボ）
		this.m_exclusiveBranchEndStatus = new Combo(exclusiveBranchComposite, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_exclusiveBranchEndStatus", this.m_exclusiveBranchEndStatus);
		this.m_exclusiveBranchEndStatus.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_exclusiveBranchEndStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_exclusiveBranchEndStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_exclusiveBranchEndStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// 排他分岐：終了値（ラベル）
		Label exclusiveBranchValueTitle = new Label(exclusiveBranchComposite, SWT.LEFT);
		exclusiveBranchValueTitle.setText(Messages.getString("job.exclusive.branch.end.value") + " : ");
		exclusiveBranchValueTitle.setLayoutData(new GridData(240,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 排他分岐：終了値（テキスト）
		this.m_exclusiveBranchEndValue = new Text(exclusiveBranchComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_exclusiveBranchEndValue", this.m_exclusiveBranchEndValue);
		this.m_exclusiveBranchEndValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_exclusiveBranchEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_exclusiveBranchEndValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
			);
		
		// 排他分岐：後続ジョブ優先順（ラベル）
		Composite exclusiveBranchOrderComposite = new Composite(exclusiveBranchConditionGroup, SWT.NONE);
		exclusiveBranchOrderComposite.setLayout(new GridLayout(1, false));

		Label exclusiveBranchNextJobOrderTitle = new Label(exclusiveBranchOrderComposite, SWT.LEFT);
		exclusiveBranchNextJobOrderTitle.setText(Messages.getString("job.exclusive.branch.nextjob.order") + " : ");
		exclusiveBranchNextJobOrderTitle.setLayoutData(new GridData(240,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 排他分岐：後続ジョブ優先順（テーブル）
		Composite exclusiveBranchTableComposite = new Composite(exclusiveBranchOrderComposite, SWT.NONE);
		exclusiveBranchTableComposite.setLayout(new GridLayout(2, false));

		this.m_exclusiveBranchNextJobOrderTable = new Table(exclusiveBranchTableComposite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "table", this.m_exclusiveBranchNextJobOrderTable);
		this.m_exclusiveBranchNextJobOrderTable.setHeaderVisible(true);
		this.m_exclusiveBranchNextJobOrderTable.setLinesVisible(true);
		this.m_exclusiveBranchNextJobOrderTable.setLayoutData(new GridData(200, 100));
		
		this.m_viewer = new CommonTableViewer(this.m_exclusiveBranchNextJobOrderTable);
		this.m_viewer.createTableColumn(GetControlNextJobOrderTableDefine.get(),
			GetControlNextJobOrderTableDefine.SORT_COLUMN_INDEX,
			GetControlNextJobOrderTableDefine.SORT_ORDER
		);
		
		// 上へボタン
		Composite exclusiveBranchButtonComposite = new Composite(exclusiveBranchTableComposite, SWT.NONE);
		exclusiveBranchButtonComposite.setLayout(new GridLayout(1, false));

		this.m_exclusiveBranchNextJobOrderTableButtonUp = this.createButton(exclusiveBranchButtonComposite, Messages.getString("up"));
		WidgetTestUtil.setTestId(this, "up", m_exclusiveBranchNextJobOrderTableButtonUp);
		this.m_exclusiveBranchNextJobOrderTableButtonUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = m_viewer.getTable().getSelectionIndex();
				if (index >= 0) {
					upOrder(index);
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 下へボタン
		this.m_exclusiveBranchNextJobOrderTableButtonDown = this.createButton(exclusiveBranchButtonComposite, Messages.getString("down"));
		WidgetTestUtil.setTestId(this, "down", m_exclusiveBranchNextJobOrderTableButtonDown);
		this.m_exclusiveBranchNextJobOrderTableButtonDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = m_viewer.getTable().getSelectionIndex();
				if (index >= 0) {
					downOrder(index);
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});
	}

	/**
	 * 後続ジョブ優先度をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public void reflectExclusiveBranchInfo() {
		if (m_waitRule == null) {
			throw new InternalError("JobWaitRuleInfo is null");
		}
		//排他分岐
		m_exclusiveBranchCondition.setSelection(m_waitRule.isExclusiveBranch() && !m_waitRule.getExclusiveBranchNextJobOrderList().isEmpty());
		//排他分岐終了値
		m_exclusiveBranchEndValue.setText(String.valueOf(m_waitRule.getExclusiveBranchEndValue()));

		//排他分岐
		if (m_exclusiveBranchCondition.getSelection()) {
			m_exclusiveBranchEndStatus.setEnabled(true);
			m_exclusiveBranchEndValue.setEditable(true);
		} else {
			m_exclusiveBranchEndStatus.setEnabled(false);
			m_exclusiveBranchEndValue.setEditable(false);
		}

		//排他分岐の終了状態
		setSelectEndStatus(m_exclusiveBranchEndStatus, m_waitRule.getExclusiveBranchEndStatus());
		
		//後続ジョブ優先度を設定
		//優先度順でリストに格納されている
		m_exclusiveBranchNextJobOrderList = m_waitRule.getExclusiveBranchNextJobOrderList();
		//後続ジョブ優先度をテーブルに反映
		reflectNextJobOrderTable();
	}
	
	/**
	 * 後続ジョブ優先度をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobNextJobOrderInfo
	 */
	public void reflectNextJobOrderTable() {
		List<Object> tableData = new ArrayList<Object>();
		if (!m_exclusiveBranchCondition.getSelection()) {
			//チェックがついていない場合は優先度テーブルは空にする
			m_viewer.setInput(tableData);
			return;
		}

		//このジョブを待ち条件としているジョブIDリスト
		List<String> nextJobIdList = new ArrayList<>();
		//待ち条件が削除されたジョブを除外するために使用
		List<String> notNextJobIdList = new ArrayList<>();
		JobTreeItem parent= m_jobTreeItem.getParent();
		if (parent != null) {
			//同一階層のジョブリスト
			List<JobTreeItem> siblingJobList = parent.getChildren();
			List<String> siblingJobIdList = siblingJobList.stream().map(treeItem -> treeItem.getData().getId()).collect(Collectors.toList());
			//新規に作成された後続ジョブを表示するために使用する
			for (JobTreeItem sibling : siblingJobList) {
				if (sibling == m_jobTreeItem) {
					continue;
				}
				//Full Jobで情報が取得されていないジョブはスキップ
				JobInfo siblingJobInfo = sibling.getData();
				if (siblingJobInfo.getWaitRule() == null) {
					continue;
				}
				List<JobObjectInfo> siblingWaitJobObjectInfoList = siblingJobInfo.getWaitRule().getObject();
				if (siblingWaitJobObjectInfoList != null) {
					for (JobObjectInfo siblingWaitJobObjectInfo : siblingWaitJobObjectInfoList) {
						//同じ階層のジョブの中でこのジョブを待ち条件としているもの
						if ((siblingWaitJobObjectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS ||
							siblingWaitJobObjectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE) &&
							siblingWaitJobObjectInfo.getJobId().equals(m_jobTreeItem.getData().getId())) {
							nextJobIdList.add(sibling.getData().getId());
							break;
						} 
					}
				}
				//このジョブを待ち条件としていないジョブ
				if (!nextJobIdList.contains(sibling.getData().getId())) {
					notNextJobIdList.add(sibling.getData().getId());
				}
			}
			//同一階層ジョブにないものは優先度設定から除く(削除されたジョブ)
			m_exclusiveBranchNextJobOrderList.removeIf(nextJobOrder -> !siblingJobIdList.contains(nextJobOrder.getNextJobId()));
			//後続ジョブでないジョブは優先度設定から除く（待ち条件が削除された場合など）
			m_exclusiveBranchNextJobOrderList.removeIf(nextJobOrder -> notNextJobIdList.contains(nextJobOrder.getNextJobId()));
		}
		Integer order = 1;
		for(JobNextJobOrderInfo nextJobOrder: m_exclusiveBranchNextJobOrderList) {
			ArrayList<Object> tableLineData = new ArrayList<Object>();
			tableLineData.add(order++);  
			tableLineData.add(nextJobOrder.getNextJobId());
			tableData.add(tableLineData);
		}

		//新規に待ち条件に追加されたジョブも反映する
		//既に優先度設定がある後続ジョブIDリスト
		List<String> nextJobOrderJobIdList = m_exclusiveBranchNextJobOrderList.stream()
											.map(nextJobOrder -> nextJobOrder.getNextJobId()).collect(Collectors.toList());
		for (String nextJobId: nextJobIdList) {
			//後続ジョブに優先度設定がなければ下位の優先度として追加する
			if (!nextJobOrderJobIdList.contains(nextJobId)) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(order);  
				tableLineData.add(nextJobId);
				tableData.add(tableLineData);
				
				JobNextJobOrderInfo nextJobOrder = new JobNextJobOrderInfo();
				nextJobOrder.setJobunitId(m_jobTreeItem.getData().getJobunitId());
				nextJobOrder.setJobId(m_jobTreeItem.getData().getId());
				nextJobOrder.setNextJobId(nextJobId);
				m_exclusiveBranchNextJobOrderList.add(nextJobOrder);

				order += 1;
			}
		}
		m_viewer.setInput(tableData);
	}
	
	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if (m_exclusiveBranchCondition.getSelection() && "".equals(this.m_exclusiveBranchEndValue.getText())){
			this.m_exclusiveBranchEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_exclusiveBranchEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブ待ち条件情報を設定します。
	 *
	 * @param start ジョブ待ち条件情報
	 */
	public void setWaitRuleInfo(JobWaitRuleInfo start) {
		m_waitRule = start;
	}

	/**
	 * ジョブ待ち条件情報を返します。
	 *
	 * @return ジョブ待ち条件情報
	 */
	public JobWaitRuleInfo getWaitRuleInfo() {
		return m_waitRule;
	}

	/**
	 * 後続ジョブ表示用JobTreeItemを設定します。
	 *
	 * @return ジョブ待ち条件情報
	 */
	public void setJobTreeItem(JobTreeItem jobTreeItem) {
		m_jobTreeItem = jobTreeItem;
	}

	public ValidateResult createExclusiveBranchInfo() {
		ValidateResult result = null;

		//排他分岐フラグ
		m_waitRule.setExclusiveBranch(m_exclusiveBranchCondition.getSelection() && !m_waitRule.getExclusiveBranchNextJobOrderList().isEmpty());

		//排他分岐終了値、終了状態
		try {
			m_waitRule.setExclusiveBranchEndStatus(getSelectEndStatus(m_exclusiveBranchEndStatus));
			m_waitRule.setExclusiveBranchEndValue(Integer.parseInt(m_exclusiveBranchEndValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.isExclusiveBranch().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}
		
		return null;
	}

	/**
	 *終了状態用コンボボックスにて選択している項目を取得します。
	 *
	 */
	private int getSelectEndStatus(Combo combo) {
		String select = combo.getText();
		return EndStatusMessage.stringToType(select);
	}

	/**
	 * 指定した重要度に該当する終了状態用コンボボックスの項目を選択します。
	 *
	 */
	private void setSelectEndStatus(Combo combo, int status) {
		String select = "";

		select = EndStatusMessage.typeToString(status);

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}
	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_exclusiveBranchCondition.setEnabled(enabled);
		m_exclusiveBranchEndStatus.setEnabled(m_exclusiveBranchCondition.getSelection() && enabled);
		m_exclusiveBranchEndValue.setEditable(m_exclusiveBranchCondition.getSelection() && enabled);
		m_exclusiveBranchNextJobOrderTableButtonUp.setEnabled(enabled);
		m_exclusiveBranchNextJobOrderTableButtonDown.setEnabled(enabled);
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param label ボタンに表示するテキスト
	 * @return ボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);

		GridData gridData = new GridData();
		gridData.minimumWidth = 80;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
	}

	/**
	 * 引数で指定された判定情報の行を選択状態にします。
	 *
	 * @param identifier 識別キー
	 */
	private void selectItem(Integer order) {
		Table stringValueListSelectItemTable = m_viewer.getTable();
		WidgetTestUtil.setTestId(this, null, stringValueListSelectItemTable);
		TableItem[] items = stringValueListSelectItemTable.getItems();

		if (items == null || order == null) {
			return;
		}
		stringValueListSelectItemTable.select(order);
		return;
	}
	
	/**
	 * テーブル選択項目の優先度を上げる
	 */
	private void upOrder(final Integer currentIndex) {
		swapOrder(currentIndex, currentIndex - 1);
		//更新後に再度選択項目にフォーカスをあてる
		selectItem(currentIndex - 1);
	}

	/**
	 * テーブル選択項目の優先度を下げる
	 */
	private void downOrder(final Integer currentIndex) {
		swapOrder(currentIndex, currentIndex + 1);
		//更新後に再度選択項目にフォーカスをあてる
		selectItem(currentIndex + 1);
	}
	
	private void swapOrder(final Integer currentIndex, final Integer targetIndex) {
		JobNextJobOrderInfo current = m_exclusiveBranchNextJobOrderList.get(currentIndex);
		JobNextJobOrderInfo target;
		try {
			target = m_exclusiveBranchNextJobOrderList.get(targetIndex);
		} catch (IndexOutOfBoundsException e) {
			return;
		}
		//リストの順序を入れ替える
		m_exclusiveBranchNextJobOrderList.set(currentIndex, target);
		m_exclusiveBranchNextJobOrderList.set(targetIndex, current);
		//テーブルを更新
		reflectNextJobOrderTable();
	}
}

