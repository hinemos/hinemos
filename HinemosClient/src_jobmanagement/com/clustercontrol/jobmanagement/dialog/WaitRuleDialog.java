/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetWaitRuleTableDefine;
import com.clustercontrol.jobmanagement.action.WaitRuleProperty;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ開始条件ダイアログクラス
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class WaitRuleDialog extends CommonDialog {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( WaitRuleDialog.class );
	/** プロパティシート */
	private PropertySheet m_viewer = null;

	/** ダイアログのサイズの初期値 */
	//    private final int sizeX = 500;
	private final int sizeY = 400;

	/** シェル */
	private Shell m_shell = null;
	/** 判定対象情報 */
	private ArrayList<Object> m_startCondition = null;

	private JobTreeItem m_jobTreeItem = null;


	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 * @param parentJobId 親ジョブID
	 * @param jobId ジョブID
	 */
	public WaitRuleDialog(Shell parent, JobTreeItem jobTreeItem) {
		super(parent);
		m_jobTreeItem = jobTreeItem;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see com.clustercontrol.jobmanagement.action.WaitRuleProperty#getProperty(String, String, int)
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_log.debug("customizeDialog");

		m_shell = this.getShell();

		// ダイアログタイトル
		String displayJobId =  m_jobTreeItem.getData().getId();
		String displayJobName =  m_jobTreeItem.getData().getName();
		if(displayJobId != null && !"".equals(displayJobId)
				&& displayJobName != null && !"".equals(displayJobName)){
			parent.getShell().setText(Messages.getString("wait.rule") + " : " +
					displayJobName + "(" + displayJobId + ")");
		}else{
			// ダイアログを一度も閉じていない状態ではJonInfoは生成されていないため
			parent.getShell().setText(Messages.getString("wait.rule"));
		}

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		Label tableTitle = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "tabletitle", tableTitle);
		tableTitle.setText(Messages.getString("attribute") + " : ");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		tableTitle.setLayoutData(gridData);

		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, tree);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);

		m_viewer = new PropertySheet(tree);

		m_viewer.setInput(new WaitRuleProperty().getProperty(m_jobTreeItem, JudgmentObjectConstant.TYPE_JOB_END_STATUS));
		m_viewer.expandAll();

		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation(
				(display.getBounds().width - m_shell.getSize().x) / 2, (display
						.getBounds().height - m_shell.getSize().y) / 2);

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		m_shell.pack();
		m_shell.setSize(new Point(m_shell.getSize().x, sizeY ));

		//開始条件反映
		reflectStartCondition();

		m_viewer.expandAll();
	}

	/**
	 * 判定対象情報をプロパティシートに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.action.WaitRuleProperty
	 * @see com.clustercontrol.jobmanagement.action.WaitRuleProperty#getProperty(String, String, int)
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 * @see com.clustercontrol.util.PropertyUtil
	 */
	private void reflectStartCondition() {
		Property property = null;
		m_log.debug("refrectStartCondition");
		if (m_startCondition != null) {
			//判定対象を設定
			Integer type = (Integer) m_startCondition
					.get(GetWaitRuleTableDefine.JUDGMENT_OBJECT);

			if (type == JudgmentObjectConstant.TYPE_JOB_END_STATUS) {
				//判定対象がジョブの場合
				property = new WaitRuleProperty().getProperty(m_jobTreeItem, JudgmentObjectConstant.TYPE_JOB_END_STATUS);
				ArrayList<Property> propertyList = PropertyUtil.getProperty(property,
						WaitRuleProperty.ID_JUDGMENT_OBJECT);
				Property judgmentObject = (Property) propertyList.get(0);
				Object values[][] = judgmentObject.getSelectValues();
				judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_END_STATUS);

				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_JOB_END_STATUS];
				@SuppressWarnings("unchecked")
				ArrayList<Property> list = (ArrayList<Property>) map.get("property");

				//ジョブIDを設定
				String jobId = (String) m_startCondition.get(GetWaitRuleTableDefine.JOB_ID);
				list.get(0).setValue(jobId);

				//条件値
				String value = (String) m_startCondition.get(GetWaitRuleTableDefine.START_VALUE);
				list.get(1).setValue(value);

				//説明
				String description = (String) m_startCondition.get(GetWaitRuleTableDefine.DESCRIPTION);
				list.get(2).setValue(description);
			}
			else if (type == JudgmentObjectConstant.TYPE_JOB_END_VALUE) {
				//判定対象がジョブの場合
				property = new WaitRuleProperty().getProperty(m_jobTreeItem, JudgmentObjectConstant.TYPE_JOB_END_VALUE);
				ArrayList<Property> propertyList = PropertyUtil.getProperty(property, WaitRuleProperty.ID_JUDGMENT_OBJECT);
				Property judgmentObject = (Property) propertyList.get(0);
				Object values[][] = judgmentObject.getSelectValues();
				judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_END_VALUE);

				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_JOB_END_VALUE];
				@SuppressWarnings("unchecked")
				ArrayList<Property> list = (ArrayList<Property>) map.get("property");

				//ジョブIDを設定
				String jobId = (String) m_startCondition
						.get(GetWaitRuleTableDefine.JOB_ID);
				((Property) list.get(0)).setValue(jobId);

				//条件値
				Integer value = (Integer) m_startCondition
						.get(GetWaitRuleTableDefine.START_VALUE);
				((Property) list.get(1)).setValue(value);

				//説明
				String description = (String) m_startCondition.get(GetWaitRuleTableDefine.DESCRIPTION);
				list.get(2).setValue(description);
			}
			else if (type == JudgmentObjectConstant.TYPE_TIME) {
				//判定対象が時刻の場合
				property = new WaitRuleProperty().getProperty(m_jobTreeItem, JudgmentObjectConstant.TYPE_TIME);
				ArrayList<Property> propertyList = PropertyUtil.getProperty(property, WaitRuleProperty.ID_JUDGMENT_OBJECT);
				Property judgmentObject = (Property) propertyList.get(0);
				Object values[][] = judgmentObject.getSelectValues();
				judgmentObject.setValue(JudgmentObjectMessage.STRING_TIME);

				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_TIME];
				@SuppressWarnings("unchecked")
				ArrayList<Property> list = (ArrayList<Property>) map.get("property");

				//開始時刻
				Date time = (Date) m_startCondition.get(GetWaitRuleTableDefine.START_VALUE);
				list.get(0).setValue(time);

				//説明
				String description = (String) m_startCondition.get(GetWaitRuleTableDefine.DESCRIPTION);
				list.get(1).setValue(description);
			}
			else if (type == JudgmentObjectConstant.TYPE_START_MINUTE) {
				m_log.debug("refrectStartCondition_TYPE_START_MINUTE");
				//セッション開始後時間指定
				property = new WaitRuleProperty().getProperty(m_jobTreeItem, JudgmentObjectConstant.TYPE_START_MINUTE);
				ArrayList<Property> propertyList = PropertyUtil.getProperty(property, WaitRuleProperty.ID_JUDGMENT_OBJECT);
				Property judgmentObject = (Property) propertyList.get(0);
				Object values[][] = judgmentObject.getSelectValues();
				judgmentObject.setValue(JudgmentObjectMessage.STRING_START_MINUTE);

				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_START_MINUTE];
				@SuppressWarnings("unchecked")
				ArrayList<Property> list = (ArrayList<Property>) map.get("property");

				//セッション開始時の時間（分）
				Integer startMinute = (Integer) m_startCondition
						.get(GetWaitRuleTableDefine.START_VALUE);
				m_log.debug("startMinute=" + startMinute);
				((Property) list.get(0)).setValue(startMinute);

				//説明
				String description = (String) m_startCondition.get(GetWaitRuleTableDefine.DESCRIPTION);
				list.get(1).setValue(description);
			}
			else if (type == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
				m_log.debug("refrectStartCondition_TYPE_JOB_PARAMETER");

				property = new WaitRuleProperty().getProperty(m_jobTreeItem, JudgmentObjectConstant.TYPE_JOB_PARAMETER);
				ArrayList<Property> propertyList = PropertyUtil.getProperty(property, WaitRuleProperty.ID_JUDGMENT_OBJECT);
				Property judgmentObject = (Property) propertyList.get(0);
				Object values[][] = judgmentObject.getSelectValues();
				judgmentObject.setValue(JudgmentObjectMessage.STRING_JOB_PARAMETER);

				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_JOB_PARAMETER];
				@SuppressWarnings("unchecked")
				ArrayList<Property> list = (ArrayList<Property>) map.get("property");

				//判定値1
				String decisionValue1 = (String) m_startCondition.get(GetWaitRuleTableDefine.DECISION_VALUE_1);
				((Property) list.get(0)).setValue(decisionValue1);
				//判定条件
				Integer decisionCondition = (Integer) m_startCondition.get(GetWaitRuleTableDefine.DECISION_CONDITION);
				((Property) list.get(1)).setValue(decisionCondition);
				//判定値2
				String decisionValue2 = (String) m_startCondition.get(GetWaitRuleTableDefine.DECISION_VALUE_2);
				((Property) list.get(2)).setValue(decisionValue2);
				//説明
				String description = (String) m_startCondition.get(GetWaitRuleTableDefine.DESCRIPTION);
				list.get(3).setValue(description);

			}
			
			if (property != null) {
				m_viewer.setInput(property);
	
				//ビュー更新
				m_viewer.refresh();
			} else {
				m_log.warn("property is null");
			}
		}
	}

	/**
	 * ダイアログの情報から判定対象情報を作成します。
	 *
	 * @see com.clustercontrol.jobmanagement.action.WaitRuleProperty
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 * @see com.clustercontrol.util.PropertyUtil
	 */
	private ValidateResult createStartCondition() {
		m_log.debug("createStartCondition");

		ValidateResult result = null;

		m_startCondition = new ArrayList<Object>();

		Property property = (Property) m_viewer.getInput();

		//判定対象を取得
		ArrayList<?> values = PropertyUtil.getPropertyValue(property,
				WaitRuleProperty.ID_JUDGMENT_OBJECT);
		String type = (String) values.get(0);
		m_startCondition.add(Integer.valueOf(JudgmentObjectMessage
				.stringToType(type)));

		if (type.equals(JudgmentObjectMessage.STRING_JOB_END_STATUS)) {
			//ジョブID
			String jobId = "";
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_JOB_ID);
			if (values.get(0) instanceof JobTreeItem) {
				JobTreeItem item = (JobTreeItem) values.get(0);
				jobId = item.getData().getId();
			} else if (values.get(0) instanceof String) {
				jobId = (String) values.get(0);
			}
			if (jobId == null || jobId.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.22"));
				return result;
			}
			m_startCondition.add(jobId);

			//条件値
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_CONDITION_END_STATUS);
			String value = (String) values.get(0);
			if (value == null || value.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.17"));
				return result;
			}
			m_startCondition.add(value);

			//判別値1
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_1, "");
			//判別条件
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_CONDITION, "");
			//判別値2
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_2, "");

			//説明
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_DESCRIPTION);
			String description = (String) values.get(0);
			if (description != null && description.length() != 0) {
				m_startCondition.add(description);
			} else {
				m_startCondition.add("");
			}
		}
		else if (type.equals(JudgmentObjectMessage.STRING_JOB_END_VALUE)) {
			//ジョブID
			String jobId = "";
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_JOB_ID);
			if (values.get(0) instanceof JobTreeItem) {
				JobTreeItem item = (JobTreeItem) values.get(0);
				jobId = item.getData().getId();
			} else if (values.get(0) instanceof String) {
				jobId = (String) values.get(0);
			}
			if (jobId == null || jobId.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.22"));
				return result;
			}
			m_startCondition.add(jobId);

			//条件値
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_CONDITION_END_VALUE);
			Object value = values.get(0);
			if (value == null || !(value instanceof Integer)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.17"));
				return result;
			}
			m_startCondition.add((Integer) value);

			//判別値1
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_1, "");
			//判別条件
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_CONDITION, "");
			//判別値2
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_2, "");

			//説明
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_DESCRIPTION);
			String description = (String) values.get(0);
			if (description != null && description.length() != 0) {
				m_startCondition.add(description);
			} else {
				m_startCondition.add("");
			}
		}
		else if (type.equals(JudgmentObjectMessage.STRING_TIME)) {
			//ジョブID
			m_startCondition.add("");

			//開始時刻取得
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_TIME);
			Date startTime = null;
			if(values.get(0) instanceof Date){
				startTime = (Date) values.get(0);
			}
			if (startTime == null) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.18"));
				return result;
			}
			m_startCondition.add(startTime);

			//判別値1
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_1, "");
			//判別条件
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_CONDITION, "");
			//判別値2
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_2, "");

			//説明
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_DESCRIPTION);
			String description = (String) values.get(0);
			if (description != null && description.length() != 0) {
				m_startCondition.add(description);
			} else {
				m_startCondition.add("");
			}
		}
		else if (type.equals(JudgmentObjectMessage.STRING_START_MINUTE)) {
			//ジョブID
			m_startCondition.add("");

			//セッション開始時の時間（分）取得
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_START_MINUTE);
			m_log.debug("values=" + values);

			Object value = values.get(0);
			if (value == null || !(value instanceof Integer)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.52"));
				return result;
			}
			m_startCondition.add((Integer) value);

			//判別値1
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_1, "");
			//判別条件
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_CONDITION, "");
			//判別値2
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_2, "");

			//説明
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_DESCRIPTION);
			String description = (String) values.get(0);
			if (description != null && description.length() != 0) {
				m_startCondition.add(description);
			} else {
				m_startCondition.add("");
			}

		}
		else if (type.equals(JudgmentObjectMessage.STRING_JOB_PARAMETER)) {
			//ジョブID
			m_startCondition.add(GetWaitRuleTableDefine.JOB_ID, "");
			//値
			m_startCondition.add(GetWaitRuleTableDefine.START_VALUE, "");
			//判定値1
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_DECISION_VALUE_1);
			String decisionValue1 = (String) values.get(0);
			if (decisionValue1 == null || decisionValue1.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.162"));
				return result;
			}
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_1, (String) decisionValue1);

			//判定条件
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_DECISION_CONDITION);
			Object decisionCondition = values.get(0);
			if (decisionCondition == null || !(decisionCondition instanceof Integer)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.163"));
				return result;
			}
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_CONDITION, (Integer) decisionCondition);

			//判定値2
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_DECISION_VALUE_2);
			String decisionValue2 = (String) values.get(0);
			if (decisionValue2 == null || decisionValue2.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.164"));
				return result;
			}
			m_startCondition.add(GetWaitRuleTableDefine.DECISION_VALUE_2, (String) decisionValue2);

			//説明
			values = PropertyUtil.getPropertyValue(property,
					WaitRuleProperty.ID_DESCRIPTION);
			String description = (String) values.get(0);
			if (description != null && description.length() != 0) {
				m_startCondition.add(GetWaitRuleTableDefine.DESCRIPTION, description);
			} else {
				m_startCondition.add(GetWaitRuleTableDefine.DESCRIPTION, "");
			}

		}

		return null;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 *
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(350, 400);
	}

	/**
	 * 判定対象情報を設定する
	 *
	 * @param list 判定対象情報
	 */
	public void setInputData(ArrayList<Object> list) {
		m_startCondition = list;
	}

	/**
	 * 入力値を返します。
	 *
	 * @return 判定対象情報
	 */
	public ArrayList<?> getInputData() {
		return m_startCondition;
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		result = createStartCondition();
		if (result != null) {
			return result;
		}

		return null;
	}
}
