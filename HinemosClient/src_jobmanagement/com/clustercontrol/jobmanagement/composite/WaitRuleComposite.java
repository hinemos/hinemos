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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetWaitRuleTableDefine;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobWaitRuleUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 待ち条件タブ用のコンポジットクラスです。
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class WaitRuleComposite extends Composite {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(WaitRuleComposite.class);
	/** 対象条件一覧テーブルコンポジット */
	private JobWaitTableComposite m_jobWaitTable = null;
	/** 判定対象の条件関係 AND用ラジオボタン */
	private Button m_andCondition = null;
	/** 判定対象の条件関係 OR用ラジオボタン */
	private Button m_orCondition = null;
	/** 条件を満たさなければ終了する用チェックボタン */
	private Button m_endCondition = null;
	/** 条件を満たさない時の終了状態用テキスト */
	private Combo m_endStatus = null;
	/** 条件を満たさない時の終了値用テキスト */
	private Text m_endValue = null;
	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfoResponse m_waitRule = null;
	/** 読み取り専用フラグ */
	private boolean m_readOnly = false;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 * @param jobType
	 *            ジョブタイプ
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public WaitRuleComposite(Composite parent, int style, JobInfoWrapper.TypeEnum jobType) {
		super(parent, style);
		initialize(jobType);
	}

	/**
	 * コンポジットを構築します。
	 * 
	 * @param jobType
	 *            ジョブタイプ
	 */
	private void initialize(JobInfoWrapper.TypeEnum jobType) {

		this.setLayout(JobDialogUtil.getParentLayout());

		// 判定対象一覧（テーブル）
		this.m_jobWaitTable = new JobWaitTableComposite(this, SWT.NONE, jobType);
		m_jobWaitTable.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					// 選択行を取得
					@SuppressWarnings("unchecked")
					ArrayList<Object> info = (ArrayList<Object>) ((StructuredSelection) event.getSelection())
							.getFirstElement();
					m_jobWaitTable.setSelectItem(info);
					if (!m_readOnly && info != null && (Integer) info.get(GetWaitRuleTableDefine.ORDER_NO_SUB) == 0) {
						m_jobWaitTable.setModifyButtonEnabled(true);
						m_jobWaitTable.setCopyButtonEnabled(true);
						m_jobWaitTable.setDeleteButtonEnabled(true);
						return;
					}
				}
				m_jobWaitTable.setModifyButtonEnabled(false);
				m_jobWaitTable.setCopyButtonEnabled(false);
				m_jobWaitTable.setDeleteButtonEnabled(false);
			}
		});

		// separator
		JobDialogUtil.getSeparator(this);

		// 判定対象の条件関係（グループ）
		Group group = new Group(this, SWT.NONE);
		group.setText(Messages.getString("condition.between.objects"));
		group.setLayout(new RowLayout());

		// 判定対象の条件関係：AND（ラジオ）
		this.m_andCondition = new Button(group, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_andCondition", this.m_andCondition);
		this.m_andCondition.setText(Messages.getString("and"));
		this.m_andCondition.setLayoutData(new RowData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 判定対象の条件関係：OR（ラジオ）
		this.m_orCondition = new Button(group, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_orCondition", this.m_orCondition);
		this.m_orCondition.setText(Messages.getString("or"));
		this.m_orCondition.setLayoutData(new RowData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		// separator
		JobDialogUtil.getSeparator(this);

		// 条件を満たさなければ終了（チェック）
		this.m_endCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_endCondition", this.m_endCondition);
		this.m_endCondition.setText(Messages.getString("end.if.condition.unmatched"));
		this.m_endCondition.setLayoutData(new RowData(220, SizeConstant.SIZE_BUTTON_HEIGHT + 5));
		this.m_endCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_endStatus.setEnabled(true);
					m_endValue.setEditable(true);
				} else {
					m_endStatus.setEnabled(false);
					m_endValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 条件を満たさなければ終了（Composite）
		Composite endConditionGroup = new Composite(this, SWT.BORDER);
		endConditionGroup.setLayout(new GridLayout(2, false));

		// 条件を満たさなければ終了：終了状態（ラベル）
		Label endStatusTitle = new Label(endConditionGroup, SWT.LEFT);
		endStatusTitle.setText(Messages.getString("end.status") + " : ");
		endStatusTitle.setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// 条件を満たさなければ終了：終了状態（コンボ）
		this.m_endStatus = new Combo(endConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_endStatus", this.m_endStatus);
		this.m_endStatus.setLayoutData(new GridData(100, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_endStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_endStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_endStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// 条件を満たさなければ終了：終了値（ラベル）
		Label endValueTitle = new Label(endConditionGroup, SWT.LEFT);
		endValueTitle.setText(Messages.getString("end.value") + " : ");
		endValueTitle.setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// 条件を満たさなければ終了：終了値（テキスト）
		this.m_endValue = new Text(endConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_endValue", this.m_endValue);
		this.m_endValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_endValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_endValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	@Override
	public void update() {
		if (m_endCondition.getSelection() && "".equals(this.m_endValue.getText())) {
			this.m_endValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_endValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブ待ち条件情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public void reflectWaitRuleInfo() {
		// 初期値設定
		m_andCondition.setSelection(true);
		m_orCondition.setSelection(false);

		if (m_waitRule != null) {
			// 判定対象と開始条件値設定
			List<JobObjectGroupInfoResponse> objectGroupList = new ArrayList<JobObjectGroupInfoResponse>();
			if (m_waitRule.getObjectGroup() != null) {
				objectGroupList.clear();
				objectGroupList.addAll(m_waitRule.getObjectGroup());
			}
			m_jobWaitTable.setObjectGroupList(objectGroupList);
			m_jobWaitTable.reflectObjectGroup();

			// 条件関係設定
			if (m_waitRule.getCondition() == JobWaitRuleInfoResponse.ConditionEnum.AND) {
				m_andCondition.setSelection(true);
				m_orCondition.setSelection(false);
			} else {
				m_andCondition.setSelection(false);
				m_orCondition.setSelection(true);
			}

			// 開始条件を満たさないとき終了 設定
			m_endCondition.setSelection(m_waitRule.getEndCondition());

			// 終了状態
			setSelectEndStatus(m_endStatus, m_waitRule.getEndStatus());

			// 終了値
			m_endValue.setText(String.valueOf(m_waitRule.getEndValue()));
		}

		// 開始条件を満たさないとき終了
		if (m_endCondition.getSelection()) {
			m_endStatus.setEnabled(true);
			m_endValue.setEditable(true);
		} else {
			m_endStatus.setEnabled(false);
			m_endValue.setEditable(false);
		}
	}

	/**
	 * ジョブ待ち条件情報を設定します。
	 *
	 * @param waitRule
	 *            ジョブ待ち条件情報
	 */
	public void setWaitRuleInfo(JobWaitRuleInfoResponse waitRule) {
		m_waitRule = waitRule;
	}

	/**
	 * ジョブ待ち条件情報を返します。
	 *
	 * @return ジョブ待ち条件情報
	 */
	public JobWaitRuleInfoResponse getWaitRuleInfo() {
		return m_waitRule;
	}

	/**
	 * コンポジットの情報から、ジョブ待ち条件情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public ValidateResult createWaitRuleInfo() {
		m_log.debug("createWaitRuleInfo");
		ValidateResult result = null;

		// 判定対象と開始条件
		List<JobObjectGroupInfoResponse> objectGroupList = m_jobWaitTable.getObjectGroupList();
		if (objectGroupList != null) {
			if (objectGroupList.size() > 1) {
				result = JobWaitRuleUtil.validateWaitGroup(objectGroupList);
				if (result != null) {
					return result;
				}
			}
			m_waitRule.getObjectGroup().clear();
			m_waitRule.getObjectGroup().addAll(objectGroupList);
		}

		// 条件関係取得
		if (m_andCondition.getSelection()) {
			m_waitRule.setCondition(JobWaitRuleInfoResponse.ConditionEnum.AND);
		} else {
			m_waitRule.setCondition(JobWaitRuleInfoResponse.ConditionEnum.OR);
		}

		// 開始条件を満たさないとき終了 設定
		m_waitRule.setEndCondition(m_endCondition.getSelection());

		// 終了状態、終了値
		try {
			m_waitRule.setEndStatus(getSelectEndStatus(m_endStatus));
			m_waitRule.setEndValue(Integer.parseInt(m_endValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.getEndCondition().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		return null;
	}

	public void setJobTreeItem(JobTreeItemWrapper jobTreeItem) {
		m_jobWaitTable.setJobTreeItem(jobTreeItem);
	}

	/**
	 * 指定した重要度に該当するカレンダ終了状態用コンボボックスの項目を選択します。
	 *
	 */
	private void setSelectEndStatus(Combo combo, JobWaitRuleInfoResponse.EndStatusEnum status) {
		String select = "";
		int statusInt = 0;
		if (status == JobWaitRuleInfoResponse.EndStatusEnum.NORMAL) {
			statusInt = EndStatusConstant.TYPE_NORMAL;
		} else if (status == JobWaitRuleInfoResponse.EndStatusEnum.WARNING) {
			statusInt = EndStatusConstant.TYPE_WARNING;
		} else if (status == JobWaitRuleInfoResponse.EndStatusEnum.ABNORMAL) {
			statusInt = EndStatusConstant.TYPE_ABNORMAL;
		} else {
			m_log.error("setSelectEndStatus()  combo.getText() value is unknown type[" + select + "]");
		}

		select = EndStatusMessage.typeToString(statusInt);

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * 終了状態用コンボボックスにて選択している項目を取得します。
	 *
	 */
	private JobWaitRuleInfoResponse.EndStatusEnum getSelectEndStatus(Combo combo) {
		String select = combo.getText();
		int type = EndStatusMessage.stringToType(select);
		if (type == EndStatusConstant.TYPE_NORMAL) {
			return JobWaitRuleInfoResponse.EndStatusEnum.NORMAL;
		} else if (type == EndStatusConstant.TYPE_WARNING) {
			return JobWaitRuleInfoResponse.EndStatusEnum.WARNING;
		} else if (type == EndStatusConstant.TYPE_ABNORMAL) {
			return JobWaitRuleInfoResponse.EndStatusEnum.ABNORMAL;
		}
		// ここに来た場合、IF上存在しない選択枝が選ばれている
		m_log.error("getSelectEndStatus()  combo.getText() value is unknown type[" + select + "]");
		return null;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_andCondition.setEnabled(enabled);
		m_orCondition.setEnabled(enabled);
		m_endCondition.setEnabled(enabled);
		m_endStatus.setEnabled(m_endCondition.getSelection() && enabled);
		m_endValue.setEditable(m_endCondition.getSelection() && enabled);
		// this.m_jobWaitTable.setButtonEnabled(enabled);
		m_jobWaitTable.setCreateButtonEnabled(enabled);
		if (enabled && m_jobWaitTable.getSelectItem() != null
				&& (Integer) m_jobWaitTable.getSelectItem().get(GetWaitRuleTableDefine.ORDER_NO_SUB) == 0) {
			m_jobWaitTable.setModifyButtonEnabled(true);
			m_jobWaitTable.setCopyButtonEnabled(true);
			m_jobWaitTable.setDeleteButtonEnabled(true);
		} else {
			m_jobWaitTable.setModifyButtonEnabled(false);
			m_jobWaitTable.setCopyButtonEnabled(false);
			m_jobWaitTable.setDeleteButtonEnabled(false);
		}
		this.m_readOnly = !enabled;
	}

}
