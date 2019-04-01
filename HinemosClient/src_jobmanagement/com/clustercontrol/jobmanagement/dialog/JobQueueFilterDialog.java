/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.jobmanagement.bean.JobQueueConstant;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyBuilder;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.PropertyWrapper;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;
import com.clustercontrol.ws.jobmanagement.JobQueueActivityViewFilter;
import com.clustercontrol.ws.jobmanagement.JobQueueSettingViewFilter;

/**
 * ジョブキュー(同時実行制御キュー)の設定一覧、及び活動状況一覧のフィルタリング情報を入力するダイアログです。
 * <p>
 * いずれの一覧に対応するかは、コンストラクタの引数に{@link JobQueueSettingViewFilter}と
 * {@link JobQueueActivityViewFilter}のどちらのインスタンスが渡されるかで判断します。
 *
 * @version 6.2.0
 */
public class JobQueueFilterDialog extends CommonDialog {

	private static final int WIDTH = 500; // 後でpackするためWIDTHはダミーの値。
	private static final int HEIGHT = 700;

	// PropertyオブジェクトのID
	private static final String PROP_MANAGER = "manager";
	private static final String PROP_QUEUE_ID = "queueId";
	private static final String PROP_QUEUE_NAME = "queueName";
	private static final String PROP_CONCURRENCY_FROM = "concurrencyFrom";
	private static final String PROP_CONCURRENCY_TO = "concurrencyTo";
	private static final String PROP_JOB_COUNT_FROM = "jobCountFrom";
	private static final String PROP_JOB_COUNT_TO = "jobCountTo";
	private static final String PROP_REG_USER = "regUser";
	private static final String PROP_REG_DATE_FROM = "regDateFrom";
	private static final String PROP_REG_DATE_TO = "regDateTo";
	private static final String PROP_UPDATE_USER = "updateUser";
	private static final String PROP_UPDATE_DATE_FROM = "updateDateFrom";
	private static final String PROP_UPDATE_DATE_TO = "updateDateTo";
	private static final String PROP_OWNER_ROLE_ID = "ownerRoleId";

	private Property property;
	private PropertySheet propertySheet;

	private Mode mode;
	
	private enum Mode {
		SETTING, ACTIVITY
	}
	
	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public JobQueueFilterDialog(Shell parent, String managerFilter, JobQueueSettingViewFilter queueFilter) {
		super(parent);
		this.mode = (queueFilter instanceof JobQueueActivityViewFilter) ? Mode.ACTIVITY : Mode.SETTING;
		convertFilterToProperty(managerFilter, queueFilter);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(WIDTH, HEIGHT);
	}

	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.get("dialog.jobqueue.filter"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// 「属性」ラベル
		Label label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "attribute", label);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("attribute") + " : ");

		// プロパティシート
		Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, table);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		propertySheet = new PropertySheet(table);
		propertySheet.setInput(property);
		propertySheet.expandAll();

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, HEIGHT));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// クリアボタンを追加する。
		this.createButton(parent, IDialogConstants.OPEN_ID, Messages.getString("clear"), false);
		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				convertFilterToProperty("", newFilterObject());
				propertySheet.setInput(property);
				propertySheet.expandAll();
			}
		});

		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
	
	// フィルタ設定をPropertyオブジェクトへ反映させる
	private void convertFilterToProperty(String managerFilter, JobQueueSettingViewFilter queueFilter) {
		property = new Property(null, null, "");
		property.removeChildren();

		// マネージャ
		List<Object> managerList = new ArrayList<>(Arrays.asList(""));
		managerList.addAll(EndpointManager.getActiveManagerSet());
		property.addChildren(new PropertyBuilder(PROP_MANAGER, "facility.manager", PropertyDefineConstant.EDITOR_SELECT)
				.setOptions(managerList.toArray()).setModifiable(true).setValue(managerFilter).build());
		
		// キューID
		property.addChildren(new PropertyBuilder(PROP_QUEUE_ID, "jobqueue.id", PropertyDefineConstant.EDITOR_TEXT)
				.setModifiable(true).setUpperBound(JobQueueConstant.ID_MAXLEN).setValue(queueFilter.getQueueId())
				.build());

		// キュー名
		property.addChildren(new PropertyBuilder(PROP_QUEUE_NAME, "jobqueue.name", PropertyDefineConstant.EDITOR_TEXT)
				.setModifiable(true).setUpperBound(JobQueueConstant.NAME_MAXLEN).setValue(queueFilter.getQueueName())
				.build());

		// 同時実行可能数
		if (mode == Mode.SETTING) {
			Property concurrency = new PropertyBuilder(null, "jobqueue.concurrency", PropertyDefineConstant.EDITOR_TEXT)
					.build();
			concurrency.addChildren(new PropertyBuilder(PROP_CONCURRENCY_FROM, "start", PropertyDefineConstant.EDITOR_NUM)
					.setModifiable(true).setLowerBound(JobQueueConstant.CONCURRENCY_MIN)
					.setUpperBound(JobQueueConstant.CONCURRENCY_MAX).setValue(queueFilter.getConcurrencyFrom()).build());
			concurrency.addChildren(new PropertyBuilder(PROP_CONCURRENCY_TO, "end", PropertyDefineConstant.EDITOR_NUM)
					.setModifiable(true).setLowerBound(JobQueueConstant.CONCURRENCY_MIN)
					.setUpperBound(JobQueueConstant.CONCURRENCY_MAX).setValue(queueFilter.getConcurrencyTo()).build());
			property.addChildren(concurrency);
		}

		// ジョブ同時実行数
		if (mode == Mode.ACTIVITY) {
			JobQueueActivityViewFilter f = (JobQueueActivityViewFilter) queueFilter;
			Property jobCount = new PropertyBuilder(null, "jobqueue.job_count", PropertyDefineConstant.EDITOR_TEXT)
					.build();
			jobCount.addChildren(new PropertyBuilder(PROP_JOB_COUNT_FROM, "start", PropertyDefineConstant.EDITOR_NUM)
					.setModifiable(true).setLowerBound(0)
					.setUpperBound(JobQueueConstant.CONCURRENCY_MAX).setValue(f.getJobCountFrom()).build());
			jobCount.addChildren(new PropertyBuilder(PROP_JOB_COUNT_TO, "end", PropertyDefineConstant.EDITOR_NUM)
					.setModifiable(true).setLowerBound(0)
					.setUpperBound(JobQueueConstant.CONCURRENCY_MAX).setValue(f.getJobCountTo()).build());
			property.addChildren(jobCount);
		}

		// 新規作成者
		property.addChildren(new PropertyBuilder(PROP_REG_USER, "creator.name", PropertyDefineConstant.EDITOR_TEXT)
				.setModifiable(true).setUpperBound(DataRangeConstant.USER_ID_MAXLEN)
				.setValue(queueFilter.getRegUser()).build());

		// 作成日時
		Property regDate = new PropertyBuilder(null, "create.time", PropertyDefineConstant.EDITOR_TEXT).build();
		regDate.addChildren(new PropertyBuilder(PROP_REG_DATE_FROM, "start", PropertyDefineConstant.EDITOR_DATETIME)
				.setModifiable(true).setValue(queueFilter.getRegDateFrom()).build());
		regDate.addChildren(new PropertyBuilder(PROP_REG_DATE_TO, "end", PropertyDefineConstant.EDITOR_DATETIME)
				.setModifiable(true).setValue(queueFilter.getRegDateTo()).build());
		property.addChildren(regDate);

		// 最終変更者
		property.addChildren(new PropertyBuilder(PROP_UPDATE_USER, "modifier.name", PropertyDefineConstant.EDITOR_TEXT)
				.setModifiable(true).setUpperBound(DataRangeConstant.USER_ID_MAXLEN)
				.setValue(queueFilter.getUpdateUser()).build());

		// 最終変更日時
		Property updateDate = new PropertyBuilder(null, "update.time", PropertyDefineConstant.EDITOR_TEXT).build();
		updateDate.addChildren(new PropertyBuilder(PROP_UPDATE_DATE_FROM, "start", PropertyDefineConstant.EDITOR_DATETIME)
				.setModifiable(true).setValue(queueFilter.getUpdateDateFrom()).build());
		updateDate.addChildren(new PropertyBuilder(PROP_UPDATE_DATE_TO, "end", PropertyDefineConstant.EDITOR_DATETIME)
				.setModifiable(true).setValue(queueFilter.getUpdateDateTo()).build());
		property.addChildren(updateDate);

		// オーナーロールID
		property.addChildren(new PropertyBuilder(PROP_OWNER_ROLE_ID, "owner.role.id", PropertyDefineConstant.EDITOR_TEXT)
				.setModifiable(true).setUpperBound(DataRangeConstant.OWNER_ROLE_ID_MAXLEN)
				.setValue(queueFilter.getOwnerRoleId()).build());
	}

	/**
	 * 入力されたフィルタリング設定のうち、マネージャのフィルタリング設定を返します。
	 */
	public String getManagerFilter() {
		return PropertyUtil.findStringValue(property, PROP_MANAGER);
	}

	/**
	 * 入力されたフィルタリング設定のうち、マネージャのフィルタリング設定以外(ジョブキューに関するフィルタリング設定)を返します。
	 */
	public JobQueueSettingViewFilter getQueueFilter() {
		PropertyWrapper ps = new PropertyWrapper(property);
		JobQueueSettingViewFilter filter = newFilterObject();
		filter.setQueueId(ps.findString(PROP_QUEUE_ID));
		filter.setQueueName(ps.findString(PROP_QUEUE_NAME));
		if (mode == Mode.SETTING) {
			filter.setConcurrencyFrom(ps.findInteger(PROP_CONCURRENCY_FROM));
			filter.setConcurrencyTo(ps.findInteger(PROP_CONCURRENCY_TO));
		}
		if (mode == Mode.ACTIVITY) {
			JobQueueActivityViewFilter f = (JobQueueActivityViewFilter) filter;
			f.setJobCountFrom(ps.findInteger(PROP_JOB_COUNT_FROM));
			f.setJobCountTo(ps.findInteger(PROP_JOB_COUNT_TO));
		}
		filter.setOwnerRoleId(ps.findString(PROP_OWNER_ROLE_ID));
		filter.setRegUser(ps.findString(PROP_REG_USER));
		filter.setRegDateFrom(ps.findTime(PROP_REG_DATE_FROM));
		filter.setRegDateTo(ps.findEndTime(PROP_REG_DATE_TO));
		filter.setUpdateUser(ps.findString(PROP_UPDATE_USER));
		filter.setUpdateDateFrom(ps.findTime(PROP_UPDATE_DATE_FROM));
		filter.setUpdateDateTo(ps.findEndTime(PROP_UPDATE_DATE_TO));
		return filter;
	}

	/**
	 * 入力されたフィルタリング設定のうち、マネージャのフィルタリング設定以外(ジョブキューに関するフィルタリング設定)を返します。
	 * コンストラクタに{@link JobQueueActivityViewFilter}を渡した場合のみ有効です。
	 * 
	 * @throws IllegalStateException コンストラクタで{@link JobQueueActivityViewFilter}を渡していない。
	 */
	// getQueueFilter() の戻り値を呼び出し元でキャストしても同じことではあるが、
	// それをやると、「このダイアログが JobQueueActivityViewFilter を返しうる」という
	// コントラクトが暗黙のものになってしまうので、敢えてこのメソッドを設けた。
	public JobQueueActivityViewFilter getQueueFilterForActivity() {
		if (mode == Mode.ACTIVITY) {
			return (JobQueueActivityViewFilter) getQueueFilter();
		}
		throw new IllegalStateException("Mode is not ACTIVITY");
	}

	/** モードにあったフィルタオブジェクトを生成する。 */
	private JobQueueSettingViewFilter newFilterObject() {
		if (mode == Mode.ACTIVITY) {
			return new JobQueueActivityViewFilter();
		} else {
			return new JobQueueSettingViewFilter();
		}
	}
}
