/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.WaitRuleProperty;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectMessage;
import com.clustercontrol.jobmanagement.bean.ValueSeparatorConstant;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobWaitRuleUtil;
import com.clustercontrol.jobmap.composite.JobMapComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * ジョブ開始条件ダイアログクラス
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class WaitRuleDialog extends CommonDialog {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(WaitRuleDialog.class);
	/** プロパティシート */
	private PropertySheet m_viewer = null;

	/** ダイアログのサイズの初期値 */
	// private final int sizeX = 500;
	private final int sizeY = 400;

	/** シェル */
	private Shell m_shell = null;
	/** 判定対象情報 */
	private JobObjectGroupInfoResponse m_condition = null;
	/** ジョブツリー */
	private JobTreeItemWrapper m_jobTreeItem = null;
	/** 呼び出し元コンポジット */
	private Composite m_parentComposite;
	/** 判定対象の条件関係 AND用ラジオボタン */
	private Button m_andCondition = null;
	/** 判定対象の条件関係 OR用ラジオボタン */
	private Button m_orCondition = null;

	/** 追加用ボタン */
	private Button m_createCondition = null;
	/** 削除用ボタン */
	private Button m_deleteCondition = null;

	/** 選択されているプロパティ */
	private Property m_selectProperty = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親シェル
	 * @param jobTreeItem
	 *            ジョブツリー
	 */
	public WaitRuleDialog(Shell parent, JobTreeItemWrapper jobTreeItem) {
		super(parent);
		m_jobTreeItem = jobTreeItem;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親シェル
	 * @param jobTreeItem
	 *            ジョブツリー
	 * @param composite
	 *            呼び出し元コンポジット
	 */
	public WaitRuleDialog(Shell parent, JobTreeItemWrapper jobTreeItem, Composite composite) {
		super(parent);
		m_jobTreeItem = jobTreeItem;
		m_parentComposite = composite;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親コンポジット
	 *
	 * @see com.clustercontrol.jobmanagement.action.WaitRuleProperty#getProperty(String,
	 *      String, int)
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_log.debug("customizeDialog");

		m_shell = this.getShell();

		// ダイアログタイトル
		String displayJobId = m_jobTreeItem.getData().getId();
		String displayJobName = m_jobTreeItem.getData().getName();
		if (displayJobId != null && !"".equals(displayJobId) && displayJobName != null && !"".equals(displayJobName)) {
			parent.getShell()
					.setText(Messages.getString("wait.rule") + " : " + displayJobName + "(" + displayJobId + ")");
		} else {
			// ダイアログを一度も閉じていない状態ではJonInfoは生成されていないため
			parent.getShell().setText(Messages.getString("wait.rule"));
		}

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		Label tableTitle = new Label(parent, SWT.NONE);
		tableTitle.setText(Messages.getString("attribute") + " : ");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		tableTitle.setLayoutData(gridData);

		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = SWT.MIN;
		gridData.verticalSpan = 2;
		tree.setLayoutData(gridData);

		m_viewer = new PropertySheet(tree);
		this.m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				m_selectProperty = (Property) selection.getFirstElement();
				m_deleteCondition.setEnabled(true);
			}
		});

		// ボタン（グループ）
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, true));

		// ボタン：追加（ボタン）
		this.m_createCondition = new Button(buttonComposite, SWT.NONE);
		this.m_createCondition.setText(Messages.getString("add"));
		this.m_createCondition.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_createCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_log.debug("widgetSelected");
				Property property = (Property) m_viewer.getInput();
				property.addChildren(new WaitRuleProperty().getProperty(m_jobTreeItem,
						JobObjectInfoResponse.TypeEnum.JOB_END_STATUS));
				m_viewer.setInput(property);
			}
		});

		// ボタン：削除（ボタン）
		this.m_deleteCondition = new Button(buttonComposite, SWT.NONE);
		this.m_deleteCondition.setText(Messages.getString("delete"));
		this.m_deleteCondition.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_deleteCondition.setEnabled(false);
		this.m_deleteCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_selectProperty.getStringHighlight() != null && m_selectProperty.getStringHighlight()) {
					Property property = (Property) m_viewer.getInput();
					property.removeChildren(m_selectProperty);
					m_selectProperty = null;
					m_viewer.setInput(property);
					m_deleteCondition.setEnabled(false);
				}
			}
		});

		// 判定対象の条件関係（グループ）
		Group conditionGroup = new Group(parent, SWT.NONE);
		conditionGroup.setText(Messages.getString("job.wait.group.condition"));
		conditionGroup.setLayout(new GridLayout(1, true));

		// 判定対象の条件関係：AND（ラジオ）
		this.m_andCondition = new Button(conditionGroup, SWT.RADIO);
		this.m_andCondition.setText(Messages.getString("and"));
		this.m_andCondition.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 判定対象の条件関係：OR（ラジオ）
		this.m_orCondition = new Button(conditionGroup, SWT.RADIO);
		this.m_orCondition.setText(Messages.getString("or"));
		this.m_orCondition.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);

		// ダイアログのサイズ調整（pack:resize to be its preferred size）
		m_shell.pack();
		m_shell.setSize(new Point(m_shell.getSize().x, sizeY));

		// 開始条件反映
		reflectCondition();

		m_viewer.expandAll();
	}

	/**
	 * 判定対象情報をプロパティシートに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.action.WaitRuleProperty
	 * @see com.clustercontrol.jobmanagement.action.WaitRuleProperty#getProperty(String,
	 *      String, int)
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 * @see com.clustercontrol.util.PropertyUtil
	 */
	private void reflectCondition() {
		// 初期値設定
		m_andCondition.setSelection(true);
		m_orCondition.setSelection(false);

		Property property = new Property(null, null, null);
		property.removeChildren();

		if (m_condition != null) {
			if (m_condition.getConditionType() == JobObjectGroupInfoResponse.ConditionTypeEnum.AND) {
				m_andCondition.setSelection(true);
				m_orCondition.setSelection(false);
			} else {
				m_andCondition.setSelection(false);
				m_orCondition.setSelection(true);
			}
			if (m_condition.getJobObjectList() != null) {
				Property typeProperty = null;
				for (JobObjectInfoResponse objectInfo : m_condition.getJobObjectList()) {
					if (objectInfo == null || objectInfo.getType() == null) {
						continue;
					}
					// 判定対象を設定
					JobObjectInfoResponse.TypeEnum typeEnum = (JobObjectInfoResponse.TypeEnum) objectInfo.getType();
					// プロパティ取得
					typeProperty = new WaitRuleProperty().getProperty(m_jobTreeItem, typeEnum);
					ArrayList<Property> propertyList = PropertyUtil.getProperty(typeProperty,
							WaitRuleProperty.ID_JUDGMENT_OBJECT);
					Property judgmentObject = (Property) propertyList.get(0);
					Object values[][] = judgmentObject.getSelectValues();
					judgmentObject.setValue(JudgmentObjectMessage.enumToString(typeEnum));

					if (typeEnum == JobObjectInfoResponse.TypeEnum.JOB_END_STATUS) {
						// 判定対象：ジョブ(終了状態)
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_JOB_END_STATUS];
						@SuppressWarnings("unchecked")
						ArrayList<Property> list = (ArrayList<Property>) map.get("property");

						// ジョブID
						list.get(0).setValue(objectInfo.getJobId());
						// 条件値
						list.get(1).setValue(objectInfo.getStatus());
						// 説明
						list.get(2).setValue(objectInfo.getDescription());

					} else if (typeEnum == JobObjectInfoResponse.TypeEnum.JOB_END_VALUE) {
						// 判定対象：ジョブ(終了値)
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_JOB_END_VALUE];
						@SuppressWarnings("unchecked")
						ArrayList<Property> list = (ArrayList<Property>) map.get("property");

						// ジョブID
						list.get(0).setValue(objectInfo.getJobId());
						// 判定条件
						list.get(1).setValue(objectInfo.getDecisionCondition());
						// 条件値
						list.get(2).setValue(objectInfo.getValue());
						// 説明
						list.get(3).setValue(objectInfo.getDescription());

					} else if (typeEnum == JobObjectInfoResponse.TypeEnum.TIME) {
						// 判定対象：時刻
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_TIME];
						@SuppressWarnings("unchecked")
						ArrayList<Property> list = (ArrayList<Property>) map.get("property");

						// 開始時刻
						list.get(0).setValue(new Date(JobTreeItemUtil.convertTimeStringtoLong(objectInfo.getTime())));
						// 説明
						list.get(1).setValue(objectInfo.getDescription());

					} else if (typeEnum == JobObjectInfoResponse.TypeEnum.START_MINUTE) {
						// 判定対象：セッション開始後の時間(分)
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_START_MINUTE];
						@SuppressWarnings("unchecked")
						ArrayList<Property> list = (ArrayList<Property>) map.get("property");

						// セッション開始時の時間（分）
						((Property) list.get(0)).setValue(objectInfo.getStartMinute());
						// 説明
						list.get(1).setValue(objectInfo.getDescription());

					} else if (typeEnum == JobObjectInfoResponse.TypeEnum.JOB_PARAMETER) {
						// 判定対象：ジョブ変数
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_JOB_PARAMETER];
						@SuppressWarnings("unchecked")
						ArrayList<Property> list = (ArrayList<Property>) map.get("property");

						// 判定値1
						list.get(0).setValue(objectInfo.getDecisionValue());
						// 判定条件
						list.get(1).setValue(objectInfo.getDecisionCondition());
						// 判定値2
						list.get(2).setValue(objectInfo.getValue());
						// 説明
						list.get(3).setValue(objectInfo.getDescription());

					} else if (typeEnum == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_STATUS) {
						// 判定対象：セッション横断ジョブ(終了状態)
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS];
						@SuppressWarnings("unchecked")
						ArrayList<Property> list = (ArrayList<Property>) map.get("property");

						// ジョブID
						list.get(0).setValue(objectInfo.getJobId());
						// 条件値
						list.get(1).setValue(objectInfo.getStatus());
						// セッション横断ジョブ履歴対象範囲
						list.get(2).setValue(objectInfo.getCrossSessionRange());
						// 説明
						list.get(3).setValue(objectInfo.getDescription());

					} else if (typeEnum == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_VALUE) {
						// 判定対象：セッション横断ジョブ(終了値)
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE];
						@SuppressWarnings("unchecked")
						ArrayList<Property> list = (ArrayList<Property>) map.get("property");

						// ジョブID
						list.get(0).setValue(objectInfo.getJobId());
						// 判定条件
						list.get(1).setValue(objectInfo.getDecisionCondition());
						// 条件値
						list.get(2).setValue(objectInfo.getValue());
						// セッション横断ジョブ履歴対象範囲
						list.get(3).setValue(objectInfo.getCrossSessionRange());
						// 説明
						list.get(4).setValue(objectInfo.getDescription());

					} else if (typeEnum == JobObjectInfoResponse.TypeEnum.JOB_RETURN_VALUE) {
						// 判定対象：ジョブ(戻り値)
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE];
						@SuppressWarnings("unchecked")
						ArrayList<Property> list = (ArrayList<Property>) map.get("property");

						// ジョブID
						list.get(0).setValue(objectInfo.getJobId());
						// 判定条件
						list.get(1).setValue(objectInfo.getDecisionCondition());
						// 条件値
						list.get(2).setValue(objectInfo.getValue());
						// 説明
						list.get(3).setValue(objectInfo.getDescription());
					}
					if (typeProperty != null) {
						property.addChildren(typeProperty);
					}
				}
			}
		}
		m_viewer.setInput(property);
		m_viewer.expandAll();
		m_viewer.refresh();
	}

	/**
	 * ダイアログの情報から判定対象情報を作成します。
	 *
	 * @see com.clustercontrol.jobmanagement.action.WaitRuleProperty
	 * @see com.clustercontrol.bean.JudgmentObjectConstant
	 * @see com.clustercontrol.util.PropertyUtil
	 */
	private ValidateResult createCondition() {
		m_log.debug("createStartCondition");

		ValidateResult result = null;
		Property property = (Property) m_viewer.getInput();
		if (property == null || property.getChildren() == null || property.getChildren().length == 0) {
			m_condition = null;
		} else {
			// 待ち条件群
			m_condition = new JobObjectGroupInfoResponse();
			if (m_andCondition.getSelection()) {
				m_condition.setConditionType(JobObjectGroupInfoResponse.ConditionTypeEnum.AND);
			} else {
				m_condition.setConditionType(JobObjectGroupInfoResponse.ConditionTypeEnum.OR);
			}
			m_condition.setJobObjectList(new ArrayList<>());

			for (Object child : property.getChildren()) {
				Property typeProperty = (Property) child;

				String propJudgementObject = (String) PropertyUtil
						.getPropertyValue(typeProperty, WaitRuleProperty.ID_JUDGMENT_OBJECT).get(0);
				JobObjectInfoResponse.TypeEnum type = JudgmentObjectMessage.stringToEnum(propJudgementObject);

				WaitTypeStrategy strategy = createStrategy(type);
				if (strategy == null) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.187"));
					return result;
				}

				result = strategy.validateProperty(typeProperty);
				if (result != null) {
					return result;
				}
				JobObjectInfoResponse info = strategy.convertToResponse(typeProperty);

				// 種別
				info.setType(JudgmentObjectMessage.stringToEnum(propJudgementObject));

				// 説明
				String description = (String) PropertyUtil
						.getPropertyValue(typeProperty, WaitRuleProperty.ID_DESCRIPTION).get(0);
				if (description != null && description.length() != 0) {
					info.setDescription(description);
				} else {
					info.setDescription("");
				}
				m_condition.getJobObjectList().add(info);
			}

			// 時刻は待ち条件として複数設定できない
			if (!JobWaitRuleUtil.typeUniqueCheck(m_condition.getJobObjectList(), JobObjectInfoResponse.TypeEnum.TIME)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.61"));
				return result;
			}

			// セッション開始後の時間は待ち条件として複数設定できない
			if (!JobWaitRuleUtil.typeUniqueCheck(m_condition.getJobObjectList(),
					JobObjectInfoResponse.TypeEnum.START_MINUTE)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.62"));
				return result;
			}

			// 待ち条件群内の待ち条件重複チェック
			result = JobWaitRuleUtil.validateWait(m_condition.getJobObjectList());
			if (result != null) {
				return result;
			}

			// 基本的に待ち条件群の重複チェックは[ジョブ/ジョブネットの作成変更]のOKクリック時に行う
			// ジョブマップからの設定時には経由しないので、その場合はここで行う
			if (m_parentComposite != null && m_parentComposite instanceof JobMapComposite) {
				List<JobObjectGroupInfoResponse> copyList = new ArrayList<JobObjectGroupInfoResponse>();
				for (JobObjectGroupInfoResponse info : m_jobTreeItem.getData().getWaitRule().getObjectGroup()) {
					copyList.add(info);
				}
				copyList.add(m_condition);
				// 待ち条件群の重複チェック
				result = JobWaitRuleUtil.validateWaitGroup(copyList);
				if (result != null) {
					return result;
				}
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
	 * @param list
	 *            判定対象情報
	 */
	public void setInputData(JobObjectGroupInfoResponse condition) {
		m_condition = condition;
	}

	/**
	 * 入力値を返します。
	 *
	 * @return 判定対象情報
	 */
	public JobObjectGroupInfoResponse getInputData() {
		return m_condition;
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

		result = createCondition();
		if (result != null) {
			return result;
		}

		return null;
	}

	/**
	 * 待ち条件項目ごとにストラテジを生成する
	 *
	 */
	private static WaitTypeStrategy createStrategy(JobObjectInfoResponse.TypeEnum type) {
		WaitTypeStrategy strategy = null;

		switch (type) {
		case JOB_END_STATUS:
			strategy = new WaitJobEndStatusStrategy();
			break;
		case JOB_END_VALUE:
			strategy = new WaitJobEndValueStrategy();
			break;
		case TIME:
			strategy = new WaitTimeStrategy();
			break;
		case START_MINUTE:
			strategy = new WaitStartMinuteStrategy();
			break;
		case JOB_PARAMETER:
			strategy = new WaitJobParamStrategy();
			break;
		case CROSS_SESSION_JOB_END_STATUS:
			strategy = new WaitCrossEndStatusStrategy();
			break;
		case CROSS_SESSION_JOB_END_VALUE:
			strategy = new WaitCrossEndValueStrategy();
			break;
		case JOB_RETURN_VALUE:
			strategy = new WaitReturnValueStrategy();
			break;
		default:
			// ここに到達することは実装漏れ以外想定してない
			m_log.debug("createStrategy() Error : unknown JudgmentObject.");
		}
		return strategy;

	}

	/**
	 * 待ち条件項目ごとのストラテジ定義クラス
	 *
	 */
	private interface WaitTypeStrategy {
		/**
		 * ダイアログの情報(Property)のバリデーションを行う
		 * 
		 * @param prop
		 * @return
		 */
		public abstract ValidateResult validateProperty(Property prop);

		/**
		 * ダイアログの情報(Property)をレスポンスに変換する
		 * 
		 * @param prop
		 * @return
		 */
		public abstract JobObjectInfoResponse convertToResponse(Property prop);
	}

	/**
	 * ジョブ(終了状態)のストラテジクラス
	 *
	 */
	private static class WaitJobEndStatusStrategy implements WaitTypeStrategy {

		public ValidateResult validateProperty(Property prop) {
			ValidateResult result = null;

			// ジョブID
			String jobId = findJobId(prop, WaitRuleProperty.ID_JOB_ID);
			result = validateJobId(jobId);
			if (result != null) {
				return result;
			}

			
			// 条件値
			JobObjectInfoResponse.StatusEnum status = null;
			ArrayList<?> statusList = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_CONDITION_END_STATUS);
			if (statusList.size() > 0 && !(statusList.get(0) instanceof String) && !"".equals(statusList.get(0))) {
				status = (JobObjectInfoResponse.StatusEnum) statusList.get(0);
			}
			result = validateStatus(status);
			if (result != null) {
				return result;
			}

			return result;
		}

		public JobObjectInfoResponse convertToResponse(Property prop) {
			JobObjectInfoResponse info = new JobObjectInfoResponse();
			String jobId = findJobId(prop, WaitRuleProperty.ID_JOB_ID);
			JobObjectInfoResponse.StatusEnum status = (JobObjectInfoResponse.StatusEnum) PropertyUtil
					.getPropertyValue(prop, WaitRuleProperty.ID_CONDITION_END_STATUS).get(0);
			info.setJobId(jobId);
			info.setStatus(status);
			return info;
		}
	}

	/**
	 * ジョブ(終了値)のストラテジクラス
	 *
	 */
	private static class WaitJobEndValueStrategy implements WaitTypeStrategy {

		public ValidateResult validateProperty(Property prop) {
			ValidateResult result = null;

			// ジョブID
			String jobId = findJobId(prop, WaitRuleProperty.ID_JOB_ID);
			result = validateJobId(jobId);
			if (result != null) {
				return result;
			}

			// 判定条件
			JobObjectInfoResponse.DecisionConditionEnum condition = null;
			ArrayList<?> conditionList = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_END_VALUE_CONDITION);
			if (conditionList.size() > 0 && !(conditionList.get(0) instanceof String)
					&& !"".equals(conditionList.get(0))) {
				condition = (JobObjectInfoResponse.DecisionConditionEnum) conditionList.get(0);
			}
			if (condition == null) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.163"));
				return result;
			}

			// 判定値
			String value = PropertyUtil.findStringValue(prop, WaitRuleProperty.ID_CONDITION_END_VALUE);
			result = validateValue(value, condition);
			if (result != null) {
				return result;
			}

			return result;
		}

		public JobObjectInfoResponse convertToResponse(Property prop) {
			JobObjectInfoResponse info = new JobObjectInfoResponse();
			String jobId = findJobId(prop, WaitRuleProperty.ID_JOB_ID);
			JobObjectInfoResponse.DecisionConditionEnum condition = (JobObjectInfoResponse.DecisionConditionEnum) PropertyUtil
					.getPropertyValue(prop, WaitRuleProperty.ID_END_VALUE_CONDITION).get(0);
			String value = PropertyUtil.findStringValue(prop, WaitRuleProperty.ID_CONDITION_END_VALUE);
			info.setJobId(jobId);
			info.setDecisionCondition(condition);
			info.setValue(value);
			return info;
		}
	}

	/**
	 * 時刻のストラテジクラス
	 *
	 */
	private static class WaitTimeStrategy implements WaitTypeStrategy {

		public ValidateResult validateProperty(Property prop) {
			ValidateResult result = null;

			// 時刻
			Object time = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_TIME).get(0);
			if (!(time instanceof Date)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.18"));
				return result;
			}
			return result;
		}

		public JobObjectInfoResponse convertToResponse(Property prop) {
			JobObjectInfoResponse info = new JobObjectInfoResponse();
			Object time = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_TIME).get(0);
			info.setTime(JobTreeItemUtil.convertTimeLongtoString(((Date) time).getTime()));
			return info;
		}
	}

	/**
	 * セッション開始後の時間(分)のストラテジクラス
	 *
	 */
	private static class WaitStartMinuteStrategy implements WaitTypeStrategy {

		public ValidateResult validateProperty(Property prop) {
			ValidateResult result = null;

			// セッション開始時の時間（分）
			Object minute = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_START_MINUTE).get(0);
			if (minute == null || !(minute instanceof Integer)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.52"));
				return result;
			}
			return result;
		}

		public JobObjectInfoResponse convertToResponse(Property prop) {
			JobObjectInfoResponse info = new JobObjectInfoResponse();
			Object minute = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_START_MINUTE).get(0);
			info.setStartMinute((Integer) minute);
			return info;
		}
	}

	/**
	 * ジョブ変数のストラテジクラス
	 *
	 */
	private static class WaitJobParamStrategy implements WaitTypeStrategy {
		public ValidateResult validateProperty(Property prop) {
			ValidateResult result = null;


			// 判定条件
			JobObjectInfoResponse.DecisionConditionEnum condition = null;
			ArrayList<?> conditionList = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_DECISION_CONDITION);

			if (conditionList.size() > 0 && !(conditionList.get(0) instanceof String)
					&& !"".equals(conditionList.get(0))) {
				condition = (JobObjectInfoResponse.DecisionConditionEnum) conditionList.get(0);
			}
			if (condition == null) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.163"));
				return result;
			}

			// 判定値1
			String value1 = PropertyUtil.findStringValue(prop, WaitRuleProperty.ID_DECISION_VALUE_1);
			if (value1 == null || value1.isEmpty()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.162"));
				return result;
			}
			result = validateParamValue(value1, condition, false);
			if (result != null) {
				return result;
			}

			// 判定値2
			String value2 = PropertyUtil.findStringValue(prop, WaitRuleProperty.ID_DECISION_VALUE_2);
			if (value2 == null || value2.isEmpty()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.164"));
				return result;
			}
			result = validateParamValue(value2, condition, true);
			if (result != null) {
				return result;
			}

			return result;
		}

		public JobObjectInfoResponse convertToResponse(Property prop) {
			JobObjectInfoResponse info = new JobObjectInfoResponse();
			String value1 = PropertyUtil.findStringValue(prop, WaitRuleProperty.ID_DECISION_VALUE_1);
			JobObjectInfoResponse.DecisionConditionEnum condition = (JobObjectInfoResponse.DecisionConditionEnum) PropertyUtil
					.getPropertyValue(prop, WaitRuleProperty.ID_DECISION_CONDITION).get(0);
			String value2 = PropertyUtil.findStringValue(prop, WaitRuleProperty.ID_DECISION_VALUE_2);
			info.setDecisionValue(value1);
			info.setDecisionCondition(condition);
			info.setValue(value2);
			return info;
		}
	}

	/**
	 * セッション横断ジョブ(終了状態)のストラテジクラス
	 *
	 */
	private static class WaitCrossEndStatusStrategy implements WaitTypeStrategy {

		public ValidateResult validateProperty(Property prop) {
			ValidateResult result = null;

			// ジョブID
			String jobId = findJobId(prop, WaitRuleProperty.ID_CROSS_SESSION_JOB_ID);
			result = validateJobId(jobId);
			if (result != null) {
				return result;
			}

			// 条件値
			JobObjectInfoResponse.StatusEnum status = null;
			ArrayList<?> statusList = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_CONDITION_END_STATUS);
			if (statusList.size() > 0 && !(statusList.get(0) instanceof String) && !"".equals(statusList.get(0))) {
				status = (JobObjectInfoResponse.StatusEnum) statusList.get(0);
			}

			result = validateStatus(status);
			if (result != null) {
				return result;
			}

			// セッション横断待ち条件ジョブ履歴範囲
			Object range = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_CROSS_SESSION_RANGE).get(0);
			result = validateSessionRange(range);
			if (result != null) {
				return result;
			}

			return result;
		}

		public JobObjectInfoResponse convertToResponse(Property prop) {
			JobObjectInfoResponse info = new JobObjectInfoResponse();
			String jobId = findJobId(prop, WaitRuleProperty.ID_CROSS_SESSION_JOB_ID);
			JobObjectInfoResponse.StatusEnum status = (JobObjectInfoResponse.StatusEnum) PropertyUtil
					.getPropertyValue(prop, WaitRuleProperty.ID_CONDITION_END_STATUS).get(0);
			Object range = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_CROSS_SESSION_RANGE).get(0);
			info.setJobId(jobId);
			info.setStatus(status);
			info.setCrossSessionRange((Integer) range);
			return info;
		}
	}

	/**
	 * セッション横断ジョブ(終了値)のストラテジクラス
	 *
	 */
	private static class WaitCrossEndValueStrategy implements WaitTypeStrategy {

		public ValidateResult validateProperty(Property prop) {
			ValidateResult result = null;

			// ジョブID
			String jobId = findJobId(prop, WaitRuleProperty.ID_CROSS_SESSION_JOB_ID);
			result = validateJobId(jobId);
			if (result != null) {
				return result;
			}
			
			// 判定条件
			JobObjectInfoResponse.DecisionConditionEnum condition = null;
			ArrayList<?> conditionList = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_END_VALUE_CONDITION);
			if(conditionList.size()>0 && !(conditionList.get(0) instanceof String) && !"".equals(conditionList.get(0))){
				condition = (JobObjectInfoResponse.DecisionConditionEnum)conditionList.get(0);
			}

			if (condition == null) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.163"));
				return result;
			}

			// 判定値
			String value = PropertyUtil.findStringValue(prop, WaitRuleProperty.ID_CONDITION_END_VALUE);
			result = validateValue(value, condition);
			if (result != null) {
				return result;
			}

			// セッション横断待ち条件ジョブ履歴範囲
			Object range = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_CROSS_SESSION_RANGE).get(0);
			result = validateSessionRange(range);
			if (result != null) {
				return result;
			}

			return result;
		}

		public JobObjectInfoResponse convertToResponse(Property prop) {
			JobObjectInfoResponse info = new JobObjectInfoResponse();
			String jobId = findJobId(prop, WaitRuleProperty.ID_CROSS_SESSION_JOB_ID);
			JobObjectInfoResponse.DecisionConditionEnum condition = (JobObjectInfoResponse.DecisionConditionEnum) PropertyUtil
					.getPropertyValue(prop, WaitRuleProperty.ID_END_VALUE_CONDITION).get(0);
			String value = PropertyUtil.findStringValue(prop, WaitRuleProperty.ID_CONDITION_END_VALUE);
			Object range = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_CROSS_SESSION_RANGE).get(0);
			info.setJobId(jobId);
			info.setDecisionCondition(condition);
			info.setValue(value);
			info.setCrossSessionRange((Integer) range);
			return info;
		}
	}

	/**
	 * ジョブ(戻り値)のストラテジクラス
	 *
	 */
	private static class WaitReturnValueStrategy implements WaitTypeStrategy {

		public ValidateResult validateProperty(Property prop) {
			ValidateResult result = null;

			// ジョブID
			String jobId = findJobId(prop, WaitRuleProperty.ID_RETURN_VALUE_JOB_ID);
			result = validateJobId(jobId);
			if (result != null) {
				return result;
			}
			
			// 判定条件
			JobObjectInfoResponse.DecisionConditionEnum condition = null;
			ArrayList<?> conditionList = PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_RETURN_VALUE_CONDITION);
			if(conditionList.size()>0 && !(conditionList.get(0) instanceof String) && !"".equals(conditionList.get(0))){
				condition = (JobObjectInfoResponse.DecisionConditionEnum)conditionList.get(0);
			}

			if (condition == null) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.163"));
				return result;
			}

			// 判定値
			String value = (String) PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_CONDITION_RETURN_VALUE)
					.get(0);
			result = validateValue(value, condition);
			if (result != null) {
				return result;
			}

			return result;
		}

		public JobObjectInfoResponse convertToResponse(Property prop) {
			JobObjectInfoResponse info = new JobObjectInfoResponse();
			String jobId = findJobId(prop, WaitRuleProperty.ID_RETURN_VALUE_JOB_ID);
			JobObjectInfoResponse.DecisionConditionEnum condition = (JobObjectInfoResponse.DecisionConditionEnum) PropertyUtil
					.getPropertyValue(prop, WaitRuleProperty.ID_RETURN_VALUE_CONDITION).get(0);
			String value = (String) PropertyUtil.getPropertyValue(prop, WaitRuleProperty.ID_CONDITION_RETURN_VALUE)
					.get(0);
			info.setJobId(jobId);
			info.setDecisionCondition(condition);
			info.setValue(value);
			return info;
		}
	}

	private static String findJobId(Property prop, String Id) {
		Object propJobId = PropertyUtil.getPropertyValue(prop, Id).get(0);
		String jobId = "";
		if (propJobId instanceof JobTreeItemWrapper) {
			JobTreeItemWrapper item = (JobTreeItemWrapper) propJobId;
			jobId = item.getData().getId();
		} else if (propJobId instanceof String) {
			jobId = (String) propJobId;
		}
		return jobId;
	}

	/**
	 * @param jobId
	 * @return
	 */
	private static ValidateResult validateJobId(String jobId) {
		ValidateResult result = null;
		if (jobId == null || jobId.isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.22"));
			return result;
		}
		return result;
	}

	/**
	 * @param status
	 * @return
	 */
	private static ValidateResult validateStatus(JobObjectInfoResponse.StatusEnum status) {
		ValidateResult result = null;
		if (status == null) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.17"));
			return result;
		}
		return result;
	}

	/**
	 * 値についてバリデーションを行う 判定条件がIN(数値)またはNOT IN(数値)の場合のみカンマ、コロンで指定可能
	 * それ以外については数値以外は許容しない
	 * 
	 * @param value
	 * @return
	 */
	private static ValidateResult validateValue(String value, JobObjectInfoResponse.DecisionConditionEnum condition) {
		ValidateResult result = null;
		if (value == null || value.isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.17"));
			return result;
		}

		// 判定条件がIN(数値)またはNOT IN(数値)の場合のみカンマ、コロンで指定可能
		if (condition == JobObjectInfoResponse.DecisionConditionEnum.IN_NUMERIC
				|| condition == JobObjectInfoResponse.DecisionConditionEnum.NOT_IN_NUMERIC) {
			String[] valueArray = value.split(ValueSeparatorConstant.MULTIPLE, -1);
			try {
				for (String v : valueArray) {
					if (v.contains(ValueSeparatorConstant.RANGE)) {
						// 範囲指定の場合
						String[] valueRange = v.split(ValueSeparatorConstant.RANGE);
						// 最小、最大の2つの値の表現であるかチェック
						if (valueRange.length != 2) {
							throw new NumberFormatException("Only two numbers can be specified");
						}
						// 最小:最大のように表現されているかチェック
						int min = Integer.parseInt(valueRange[0]);
						int max = Integer.parseInt(valueRange[1]);
						if (min >= max) {
							throw new NumberFormatException("Set the minimum value to exceed the maximum value");
						}
						// 最小値の範囲チェック
						result = validateValueRange(min);
						if (result != null) {
							return result;
						}
						// 最大値の範囲チェック
						result = validateValueRange(max);
						if (result != null) {
							return result;
						}
					} else {
						result = validateValueRange(Integer.parseInt(v));
						if (result != null) {
							return result;
						}
					}
				}
			} catch (NumberFormatException e) {
				m_log.info("validateValue() : " + e.getMessage());
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.191"));
				return result;
			}
		} else {
			// IN, NOT IN 条件以外はカンマ、コロンを許容しないためそのままパースする
			try {
				result = validateValueRange(Integer.parseInt(value));
			} catch (NumberFormatException e) {
				m_log.info("validateValue() : " + e.getMessage());
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.190"));
				return result;
			}
		}
		return result;
	}

	/**
	 * 数値の-32768～32767の範囲チェックを行う
	 * 
	 * @param value
	 * @return
	 */
	private static ValidateResult validateValueRange(Integer i) {
		ValidateResult result = null;
		Integer minSize = DataRangeConstant.SMALLINT_LOW;
		Integer maxSize = DataRangeConstant.SMALLINT_HIGH;
		String[] args = { Integer.toString(minSize), Integer.toString(maxSize) };

		if (i == null || i < minSize || maxSize < i) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.hinemos.8", args));
			return result;
		}
		return result;
	}

	/**
	 * 判定条件が数値でジョブ変数でないときは実数値チェックを行う ジョブ変数では数値範囲はDoubleを許容しているが実数値以外は許容しない
	 * 複数の数値のチェックがfalseの場合は数値変換チェックのみ
	 * 
	 * @param condition
	 * @param value1
	 * @return
	 */
	static final String regex = "^#\\[[a-zA-Z0-9-_:]+\\]$";
	static final Pattern pattern = Pattern.compile(regex);

	private static ValidateResult validateParamValue(String value,
			JobObjectInfoResponse.DecisionConditionEnum condition, boolean isMulti) {
		ValidateResult result = null;
		boolean conditionIsString = condition == JobObjectInfoResponse.DecisionConditionEnum.EQUAL_STRING
				|| condition == JobObjectInfoResponse.DecisionConditionEnum.NOT_EQUAL_STRING;
		// 判定条件が文字列のときはチェックを行わない
		if (conditionIsString) {
			return null;
		}
		// 判定条件がIN(数値)またはNOT IN(数値)の場合のみカンマ、コロンで指定可能
		if ((condition == JobObjectInfoResponse.DecisionConditionEnum.IN_NUMERIC
				|| condition == JobObjectInfoResponse.DecisionConditionEnum.NOT_IN_NUMERIC) && isMulti) {
			// カンマ区切りで値を分割する
			String[] separatedValueArray = value.split(ValueSeparatorConstant.MULTIPLE, -1);
			try {
				for (String sepVal : separatedValueArray) {
					// Double.parseDoubleだとスペースを含んでいてもパースしてしまうため
					// ※ジョブ変数は仕様からスペースを含むことはない NG→#[TEST A]
					if (sepVal.contains(" ")) {
						throw new NumberFormatException("Cannot contain spaces");
					}
					if (sepVal.contains(ValueSeparatorConstant.RANGE)) {
						// コロン区切りの範囲指定の場合、さらにコロンで分割する
						String[] valueRange = sepVal.split(ValueSeparatorConstant.RANGE);
						// (最小:最大)の2つの値の表現であるかチェック
						if (valueRange.length != 2) {
							throw new NumberFormatException("Only two numbers can be specified");
						}
						Double min = null;
						Double max = null;
						// 最小値がジョブ変数ではない時、数値チェックを行う
						if (!pattern.matcher(valueRange[0]).find()) {
							min = Double.parseDouble(valueRange[0]);
						}
						// 最大値がジョブ変数ではない時、数値チェックを行う
						if (!pattern.matcher(valueRange[1]).find()) {
							max = Double.parseDouble(valueRange[1]);
						}
						// 範囲指定が両方ともジョブ変数ではなかった場合、最小:最大のように表現されているかチェックを行う
						if (min != null && max != null && min >= max) {
							throw new NumberFormatException("Set the minimum value to exceed the maximum value");
						}
					} else {
						// 判定値がジョブ変数ではない時、数値チェックを行う
						if (!pattern.matcher(sepVal).find()) {
							Double.parseDouble(sepVal);
						}
					}
				}
			} catch (NumberFormatException e) {
				m_log.info("validateParamValue() : " + e.getMessage());
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.193"));
				return result;
			}
		} else {
			// 判定値がジョブ変数ではない時、数値チェックを行う
			try {
				// Double.parseDoubleだとスペースを含んでいてもパースしてしまうため
				// ※ジョブ変数は仕様からスペースを含むことはない NG→#[TEST A]
				if (value.contains(" ")) {
					throw new NumberFormatException("Cannot contain spaces");
				}
				if (!pattern.matcher(value).find()) {
					Double.parseDouble(value);
				}
			} catch (NumberFormatException e) {
				m_log.info("validateParamValue() : " + e.getMessage());
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.192"));
				return result;
			}
		}
		return result;
	}

	/**
	 * セッション横断待ち条件ジョブ履歴範囲のチェックを行う
	 * 
	 * @param range
	 * @return
	 */
	private static ValidateResult validateSessionRange(Object range) {
		ValidateResult result = null;
		if (range == null || !(range instanceof Integer)) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.168"));
			return result;
		}
		return result;
	}
}
