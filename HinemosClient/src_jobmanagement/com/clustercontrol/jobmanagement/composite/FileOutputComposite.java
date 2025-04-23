/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobOutputInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;

/**
 * ファイル出力タブ用のコンポジットクラスです。
 *
 */
public class FileOutputComposite extends Composite {

	/** 標準出力(チェックボックス) */
	private Button m_valid_Out = null;
	/** 標準出力 出力先 ディレクトリ(テキスト) */
	private Text m_directory_Out = null;
	/** 標準出力 出力先 ファイル名(テキスト) */
	private Text m_fileName_Out = null;
	/** 標準出力 出力先 追記する(チェックボックス) */
	private Button m_append_Out = null;
	/** 標準出力 出力失敗時の操作(チェックボックス) */
	private Button m_operation_Out = null;
	/** 標準出力 出力失敗時 操作(コンボボックス) */
	private Combo m_operationType_Out = null;
	/** 標準出力 出力失敗時 終了状態(コンボボックス) */
	private Combo m_operationEndStatus_Out = null;
	/** 標準出力 出力失敗時 終了値(テキスト) */
	private Text m_operationEndValue_Out = null;
	/** 標準出力 出力失敗時の通知(チェックボックス) */
	private Button m_notify_Out = null;
	/** 標準出力 出力失敗時の通知 重要度(コンボボックス) */
	private Combo m_notifyPriority_Out = null;

	/** 標準エラー出力(チェックボックス) */
	private Button m_valid_Err = null;
	/** 標準エラー出力 出力先 標準出力と同じ出力先を使用する(チェックボックス) */
	private Button m_sameNormal_Err = null;
	/** 標準エラー出力 出力先 ディレクトリ(テキスト) */
	private Text m_directory_Err = null;
	/** 標準エラー出力 出力先 ファイル名(テキスト) */
	private Text m_fileName_Err = null;
	/** 標準エラー出力 出力先 追記する(チェックボックス) */
	private Button m_append_Err = null;
	/** 標準エラー出力 出力失敗時の操作(チェックボックス) */
	private Button m_operation_Err = null;
	/** 標準エラー出力 出力失敗時 操作(コンボボックス) */
	private Combo m_operationType_Err = null;
	/** 標準エラー出力 出力失敗時 終了状態(コンボボックス) */
	private Combo m_operationEndStatus_Err = null;
	/** 標準エラー出力 出力失敗時 終了値(テキスト) */
	private Text m_operationEndValue_Err = null;
	/** 標準エラー出力 出力失敗時の通知(チェックボックス) */
	private Button m_notify_Err = null;
	/** 標準エラー出力 出力失敗時の通知 重要度(コンボボックス) */
	private Combo m_notifyPriority_Err = null;

	/** 標準出力情報 */
	private JobOutputInfoResponse m_output;
	/** 標準エラー出力情報 */
	private JobOutputInfoResponse m_errOutput;

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
	public FileOutputComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// 標準出力
		this.m_valid_Out = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		this.m_valid_Out.setText(Messages.getString("job.output.stdout"));
		this.m_valid_Out.setLayoutData(new RowData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_valid_Out.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				// 標準出力のオブジェクトの使用可否を設定
				setOutputEnabled(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 標準出力（Composite）
		Composite composite_Out = new Composite(this, SWT.BORDER);
		composite_Out.setLayout(new GridLayout(2, false));

		// 出力先（Group）
		Group outDestinationGroup_Out = new Group(composite_Out, SWT.NONE);
		outDestinationGroup_Out.setText(Messages.getString("output.destination"));
		outDestinationGroup_Out.setLayout(new GridLayout(3, false));
		outDestinationGroup_Out.setLayoutData(new GridData());
		((GridData)outDestinationGroup_Out.getLayoutData()).horizontalSpan = 3;

		// 標準出力 出力先 ディレクトリ(ラベル)
		Label directoryLabel_Out = new Label(outDestinationGroup_Out, SWT.NONE);
		directoryLabel_Out.setText(Messages.getString("directory") + " : ");
		directoryLabel_Out.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 標準出力 出力先 ディレクトリ
		this.m_directory_Out = new Text(outDestinationGroup_Out, SWT.BORDER);
		this.m_directory_Out.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_directory_Out.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//dummy
		new Label(outDestinationGroup_Out, SWT.LEFT);

		// 標準出力 出力先 ファイル名(ラベル)
		Label fileNameLabel_Out = new Label(outDestinationGroup_Out, SWT.NONE);
		fileNameLabel_Out.setText(Messages.getString("file.name") + " : ");
		fileNameLabel_Out.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 標準出力 出力先 ファイル名
		this.m_fileName_Out = new Text(outDestinationGroup_Out, SWT.BORDER);
		this.m_fileName_Out.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_fileName_Out.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 標準出力 出力先 追記する
		this.m_append_Out = new Button(outDestinationGroup_Out, SWT.CHECK);
		this.m_append_Out.setText(Messages.getString("append"));
		this.m_append_Out.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 標準出力 出力失敗時の操作（Composite）
		Composite compositeOperation_Out = new Composite(composite_Out, SWT.BORDER);
		compositeOperation_Out.setLayout(new GridLayout(4, false));
		compositeOperation_Out.setLayoutData(new GridData());
		((GridData)compositeOperation_Out.getLayoutData()).horizontalSpan = 2;

		// 標準出力 出力失敗時の操作
		this.m_operation_Out = new Button(compositeOperation_Out, SWT.CHECK);
		this.m_operation_Out.setText(Messages.getString("job.output.failure.operation"));
		this.m_operation_Out.setLayoutData(new GridData(250, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_operation_Out.getLayoutData()).verticalAlignment = SWT.TOP;
		((GridData)this.m_operation_Out.getLayoutData()).horizontalSpan = 2;
		this.m_operation_Out.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_operationType_Out.setEnabled(true);
					JobOutputInfoResponse.FailureOperationTypeEnum type = getSelectOperation(m_operationType_Out);
					if (type == JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND) {
						m_operationEndStatus_Out.setEnabled(false);
						m_operationEndValue_Out.setEditable(false);
					} else {
						m_operationEndStatus_Out.setEnabled(true);
						m_operationEndValue_Out.setEditable(true);
					}
				} else {
					m_operationType_Out.setEnabled(false);
					m_operationEndStatus_Out.setEnabled(false);
					m_operationEndValue_Out.setEditable(false);
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 標準出力 出力失敗時 操作
		this.m_operationType_Out = new Combo(compositeOperation_Out, SWT.CENTER | SWT.READ_ONLY);
		this.m_operationType_Out.setLayoutData(new GridData(120, SizeConstant.SIZE_COMBO_HEIGHT));
		((GridData)this.m_operationType_Out.getLayoutData()).horizontalSpan = 2;
		this.m_operationType_Out.add(OperationMessage.STRING_STOP_SUSPEND);
		this.m_operationType_Out.add(OperationMessage.STRING_STOP_SET_END_VALUE);
		this.m_operationType_Out.add(OperationMessage.STRING_STOP_SET_END_VALUE_FORCE);
		this.m_operationType_Out.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo check = (Combo) e.getSource();
				JobOutputInfoResponse.FailureOperationTypeEnum type = getSelectOperation(check);
				if (type == JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND) {
					m_operationEndStatus_Out.setEnabled(false);
					m_operationEndValue_Out.setEditable(false);
				} else {
					m_operationEndStatus_Out.setEnabled(true);
					m_operationEndValue_Out.setEditable(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 標準出力 出力失敗時 終了状態（ラベル）
		Label operationStatusLabel_Out = new Label(compositeOperation_Out, SWT.NONE);
		operationStatusLabel_Out.setText(Messages.getString("end.status") + " : ");
		operationStatusLabel_Out.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 標準出力 出力失敗時 終了状態
		this.m_operationEndStatus_Out = new Combo(compositeOperation_Out, SWT.CENTER | SWT.READ_ONLY);
		this.m_operationEndStatus_Out.setLayoutData(new GridData(80, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_operationEndStatus_Out.add(EndStatusMessage.STRING_NORMAL);
		this.m_operationEndStatus_Out.add(EndStatusMessage.STRING_WARNING);
		this.m_operationEndStatus_Out.add(EndStatusMessage.STRING_ABNORMAL);

		// 標準出力 出力失敗時 終了値（ラベル）
		Label operationValueLabel_Out = new Label(compositeOperation_Out, SWT.RIGHT);
		operationValueLabel_Out.setText(Messages.getString("end.value") + " : ");
		operationValueLabel_Out.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 標準出力 出力失敗時 終了値
		this.m_operationEndValue_Out = new Text(compositeOperation_Out, SWT.BORDER);
		this.m_operationEndValue_Out.setLayoutData(new GridData(80, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_operationEndValue_Out.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_operationEndValue_Out.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
			);

		// 標準出力 出力失敗時の通知
		this.m_notify_Out = new Button(composite_Out, SWT.CHECK);
		this.m_notify_Out.setText(Messages.getString("job.output.failure.notify") + " : ");
		this.m_notify_Out.setLayoutData(new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_notify_Out.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_notifyPriority_Out.setEnabled(m_notify_Out.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 標準出力 出力失敗時の通知 重要度
		this.m_notifyPriority_Out = new Combo(composite_Out, SWT.CENTER | SWT.READ_ONLY);
		this.m_notifyPriority_Out.setLayoutData(new GridData(120, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_notifyPriority_Out.add(PriorityMessage.STRING_INFO);
		this.m_notifyPriority_Out.add(PriorityMessage.STRING_WARNING);
		this.m_notifyPriority_Out.add(PriorityMessage.STRING_CRITICAL);
		this.m_notifyPriority_Out.add(PriorityMessage.STRING_UNKNOWN);

		// separator
		JobDialogUtil.getSeparator(this);

		// 標準エラー出力
		this.m_valid_Err = new Button(this, SWT.CHECK);
		this.m_valid_Err.setText(Messages.getString("job.output.stderr"));
		this.m_valid_Err.setLayoutData(new RowData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_valid_Err.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				// 標準エラー出力のオブジェクトの使用可否を設定
				setErrOutputEnabled(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 標準エラー出力（Composite）
		Composite composite_Err = new Composite(this, SWT.BORDER);
		composite_Err.setLayout(new GridLayout(2, false));

		// 出力先（Group）
		Group outDestinationGroup_Err = new Group(composite_Err, SWT.NONE);
		outDestinationGroup_Err.setText(Messages.getString("output.destination"));
		outDestinationGroup_Err.setLayout(new GridLayout(3, false));
		outDestinationGroup_Err.setLayoutData(new GridData());
		((GridData)outDestinationGroup_Err.getLayoutData()).horizontalSpan = 3;

		// 標準エラー出力 出力先 標準出力と同じ出力先を使用する
		this.m_sameNormal_Err = new Button(outDestinationGroup_Err, SWT.CHECK);
		this.m_sameNormal_Err.setText(Messages.getString("job.output.failure.same.normal"));
		this.m_sameNormal_Err.setLayoutData(new GridData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_sameNormal_Err.getLayoutData()).horizontalSpan = 3;
		this.m_sameNormal_Err.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_directory_Err.setEditable(!check.getSelection());
				m_fileName_Err.setEditable(!check.getSelection());
				m_append_Err.setEnabled(!check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 標準エラー出力 出力先 ディレクトリ(ラベル)
		Label directoryLabel_Err = new Label(outDestinationGroup_Err, SWT.NONE);
		directoryLabel_Err.setText(Messages.getString("directory") + " : ");
		directoryLabel_Err.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 標準エラー出力 出力先 ディレクトリ
		this.m_directory_Err = new Text(outDestinationGroup_Err, SWT.BORDER);
		this.m_directory_Err.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_directory_Err.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//dummy
		new Label(outDestinationGroup_Err, SWT.LEFT);

		// 標準エラー出力 出力先 ファイル名(ラベル)
		Label fileNameLabel_Err = new Label(outDestinationGroup_Err, SWT.NONE);
		fileNameLabel_Err.setText(Messages.getString("file.name") + " : ");
		fileNameLabel_Err.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 標準エラー出力 出力先 ファイル名
		this.m_fileName_Err = new Text(outDestinationGroup_Err, SWT.BORDER);
		this.m_fileName_Err.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_fileName_Err.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 標準エラー出力 出力先 追記する
		this.m_append_Err = new Button(outDestinationGroup_Err, SWT.CHECK);
		this.m_append_Err.setText(Messages.getString("append"));
		this.m_append_Err.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 標準エラー出力 出力失敗時の操作（Composite）
		Composite compositeOperation_Err = new Composite(composite_Err, SWT.BORDER);
		compositeOperation_Err.setLayout(new GridLayout(4, false));
		compositeOperation_Err.setLayoutData(new GridData());
		((GridData)compositeOperation_Err.getLayoutData()).horizontalSpan = 2;

		// 標準エラー出力 出力失敗時の操作
		this.m_operation_Err = new Button(compositeOperation_Err, SWT.CHECK);
		this.m_operation_Err.setText(Messages.getString("job.output.failure.operation"));
		this.m_operation_Err.setLayoutData(new GridData(250, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_operation_Err.getLayoutData()).verticalAlignment = SWT.TOP;
		((GridData)this.m_operation_Err.getLayoutData()).horizontalSpan = 2;
		this.m_operation_Err.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_operationType_Err.setEnabled(true);
					JobOutputInfoResponse.FailureOperationTypeEnum type = getSelectOperation(m_operationType_Err);
					if (type == JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND) {
						m_operationEndStatus_Err.setEnabled(false);
						m_operationEndValue_Err.setEditable(false);
					} else {
						m_operationEndStatus_Err.setEnabled(true);
						m_operationEndValue_Err.setEditable(true);
					}
				} else {
					m_operationType_Err.setEnabled(false);
					m_operationEndStatus_Err.setEnabled(false);
					m_operationEndValue_Err.setEditable(false);
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 標準エラー出力 出力失敗時 操作
		this.m_operationType_Err = new Combo(compositeOperation_Err, SWT.CENTER | SWT.READ_ONLY);
		this.m_operationType_Err.setLayoutData(new GridData(120, SizeConstant.SIZE_COMBO_HEIGHT));
		((GridData)this.m_operationType_Err.getLayoutData()).horizontalSpan = 2;
		this.m_operationType_Err.add(OperationMessage.STRING_STOP_SUSPEND);
		this.m_operationType_Err.add(OperationMessage.STRING_STOP_SET_END_VALUE);
		this.m_operationType_Err.add(OperationMessage.STRING_STOP_SET_END_VALUE_FORCE);
		this.m_operationType_Err.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo check = (Combo) e.getSource();
				JobOutputInfoResponse.FailureOperationTypeEnum type = getSelectOperation(check);
				if (type == JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND) {
					m_operationEndStatus_Err.setEnabled(false);
					m_operationEndValue_Err.setEditable(false);
				} else {
					m_operationEndStatus_Err.setEnabled(true);
					m_operationEndValue_Err.setEditable(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 標準エラー出力 出力失敗時 終了状態（ラベル）
		Label operationStatusLabel_Err = new Label(compositeOperation_Err, SWT.NONE);
		operationStatusLabel_Err.setText(Messages.getString("end.status") + " : ");
		operationStatusLabel_Err.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 標準エラー出力 出力失敗時 終了状態
		this.m_operationEndStatus_Err = new Combo(compositeOperation_Err, SWT.CENTER | SWT.READ_ONLY);
		this.m_operationEndStatus_Err.setLayoutData(new GridData(80, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_operationEndStatus_Err.add(EndStatusMessage.STRING_NORMAL);
		this.m_operationEndStatus_Err.add(EndStatusMessage.STRING_WARNING);
		this.m_operationEndStatus_Err.add(EndStatusMessage.STRING_ABNORMAL);

		// 標準エラー出力 出力失敗時 終了値（ラベル）
		Label operationValueLabel_Err = new Label(compositeOperation_Err, SWT.RIGHT);
		operationValueLabel_Err.setText(Messages.getString("end.value") + " : ");
		operationValueLabel_Err.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 標準エラー出力 出力失敗時 終了値
		this.m_operationEndValue_Err = new Text(compositeOperation_Err, SWT.BORDER);
		this.m_operationEndValue_Err.setLayoutData(new GridData(80, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_operationEndValue_Err.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_operationEndValue_Err.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
			);

		// 標準エラー出力 出力失敗時の通知
		this.m_notify_Err = new Button(composite_Err, SWT.CHECK);
		this.m_notify_Err.setText(Messages.getString("job.output.failure.notify") + " : ");
		this.m_notify_Err.setLayoutData(new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_notify_Err.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 標準エラー出力のオブジェクトの使用可否を設定
				m_notifyPriority_Err.setEnabled(m_notify_Err.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		// 標準エラー出力 出力失敗時の通知 重要度
		this.m_notifyPriority_Err = new Combo(composite_Err, SWT.CENTER | SWT.READ_ONLY);
		this.m_notifyPriority_Err.setLayoutData(new GridData(120, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_notifyPriority_Err.add(PriorityMessage.STRING_INFO);
		this.m_notifyPriority_Err.add(PriorityMessage.STRING_WARNING);
		this.m_notifyPriority_Err.add(PriorityMessage.STRING_CRITICAL);
		this.m_notifyPriority_Err.add(PriorityMessage.STRING_UNKNOWN);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if (this.m_directory_Out.getEditable() && "".equals(this.m_directory_Out.getText())) {
			this.m_directory_Out.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_directory_Out.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_fileName_Out.getEditable() && "".equals(this.m_fileName_Out.getText())) {
			this.m_fileName_Out.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_fileName_Out.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_operationEndValue_Out.getEditable() && "".equals(this.m_operationEndValue_Out.getText())) {
			this.m_operationEndValue_Out.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_operationEndValue_Out.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_directory_Err.getEditable() && "".equals(this.m_directory_Err.getText())) {
			this.m_directory_Err.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_directory_Err.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_fileName_Err.getEditable() && "".equals(this.m_fileName_Err.getText())) {
			this.m_fileName_Err.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_fileName_Err.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_operationEndValue_Err.getEditable() && "".equals(this.m_operationEndValue_Err.getText())) {
			this.m_operationEndValue_Err.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_operationEndValue_Err.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * ファイル出力情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobOutputInfo
	 */
	public void reflectOutputInfo() {

		m_valid_Out.setSelection(false);
		m_directory_Out.setText("");
		m_fileName_Out.setText("");
		m_append_Out.setSelection(false);
		m_operation_Out.setSelection(false);
		setSelectOperation(m_operationType_Out, JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND);
		setSelectStatus(m_operationEndStatus_Out, JobOutputInfoResponse.FailureOperationEndStatusEnum.ABNORMAL);
		m_operationEndValue_Out.setText(String.valueOf(EndStatusConstant.INITIAL_VALUE_ABNORMAL));
		m_notify_Out.setSelection(false);
		setSelectPriority(m_notifyPriority_Out, JobOutputInfoResponse.FailureNotifyPriorityEnum.CRITICAL);

		m_valid_Err.setSelection(false);
		m_sameNormal_Err.setSelection(false);
		m_directory_Err.setText("");
		m_fileName_Err.setText("");
		m_append_Err.setSelection(false);
		m_operation_Err.setSelection(false);
		setSelectOperation(m_operationType_Err, JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND);
		setSelectStatus(m_operationEndStatus_Err, JobOutputInfoResponse.FailureOperationEndStatusEnum.ABNORMAL);
		m_operationEndValue_Err.setText(String.valueOf(EndStatusConstant.INITIAL_VALUE_ABNORMAL));
		m_notify_Err.setSelection(false);
		setSelectPriority(m_notifyPriority_Err, JobOutputInfoResponse.FailureNotifyPriorityEnum.CRITICAL);

		if (m_output != null) {
			// 標準出力
			m_valid_Out.setSelection(m_output.getValid());

			// 出力先 ディレクトリ
			if (m_output.getDirectory() != null) {
				m_directory_Out.setText(m_output.getDirectory());
			}

			// 出力先 ファイル名
			if (m_output.getFileName() != null) {
				m_fileName_Out.setText(m_output.getFileName());
			}

			// 出力先 追記する
			m_append_Out.setSelection(m_output.getAppendFlg());

			// ファイル出力失敗時の操作を指定
			m_operation_Out.setSelection(m_output.getFailureOperationFlg());

			// ファイル出力失敗時 操作
			setSelectOperation(m_operationType_Out, m_output.getFailureOperationType());

			// ファイル出力失敗時 終了状態
			setSelectStatus(m_operationEndStatus_Out, m_output.getFailureOperationEndStatus());

			// ファイル出力失敗時 終了値
			if (m_output.getFailureOperationEndValue() != null) {
				m_operationEndValue_Out.setText(
						String.valueOf(m_output.getFailureOperationEndValue()));
			}

			// ファイル出力失敗時に通知する
			m_notify_Out.setSelection(m_output.getFailureNotifyFlg());

			// ファイル出力失敗時に通知 重要度
			setSelectPriority(m_notifyPriority_Out, m_output.getFailureNotifyPriority());
		}

		if (m_errOutput != null) {
			// 標準エラー出力
			m_valid_Err.setSelection(m_errOutput.getValid());

			// 標準エラー出力 出力先 標準出力と同じ出力先を使用する
			m_sameNormal_Err.setSelection(m_errOutput.getSameNormalFlg());

			// 出力先 ディレクトリ
			if (m_errOutput.getDirectory() != null) {
				m_directory_Err.setText(m_errOutput.getDirectory());
			}

			// 出力先 ファイル名
			if (m_errOutput.getFileName() != null) {
				m_fileName_Err.setText(m_errOutput.getFileName());
			}

			// 出力先 追記する
			m_append_Err.setSelection(m_errOutput.getAppendFlg());

			// ファイル出力失敗時の操作を指定
			m_operation_Err.setSelection(m_errOutput.getFailureOperationFlg());

			// ファイル出力失敗時 操作
			setSelectOperation(m_operationType_Err, m_errOutput.getFailureOperationType());

			// ファイル出力失敗時 終了状態
			setSelectStatus(m_operationEndStatus_Err, m_errOutput.getFailureOperationEndStatus());

			// ファイル出力失敗時 終了値
			if (m_errOutput.getFailureOperationEndValue() != null) {
				m_operationEndValue_Err.setText(
						String.valueOf(m_errOutput.getFailureOperationEndValue()));
			}

			// ファイル出力失敗時に通知する
			m_notify_Err.setSelection(m_errOutput.getFailureNotifyFlg());

			// ファイル出力失敗時に通知 重要度
			setSelectPriority(m_notifyPriority_Err, m_errOutput.getFailureNotifyPriority());
		}

		// オブジェクトの使用不可を設定
		setOutputEnabled(m_valid_Out.getSelection());
		setErrOutputEnabled(m_valid_Err.getSelection());
		update();
	}

	/**
	 * ファイル出力情報(標準出力)を設定します。
	 *
	 * @param normalOutput ファイル出力情報(標準出力)
	 */
	public void setOutputInfo(JobOutputInfoResponse output) {
		m_output = output;
	}

	/**
	 * ファイル出力情報(標準出力)を返します。
	 *
	 * @return ファイル出力情報(標準出力)
	 */
	public JobOutputInfoResponse getOutputInfo() {
		return m_output;
	}

	/**
	 * ファイル出力情報(標準エラー出力)を設定します。
	 *
	 * @param errOutput ファイル出力情報(標準エラー出力)
	 */
	public void setErrOutputInfo(JobOutputInfoResponse errOutput) {
		m_errOutput = errOutput;
	}

	/**
	 * ファイル出力情報(標準エラー出力)を返します。
	 *
	 * @return ファイル出力情報(標準エラー出力)
	 */
	public JobOutputInfoResponse getErrOutputInfo() {
		return m_errOutput;
	}

	/**
	 * コンポジットの情報から、ファイル出力情報を作成する。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult createOutputInfo() {
		ValidateResult result = null;

		// 標準出力情報のインスタンスを作成・取得
		m_output = new JobOutputInfoResponse();

		// 標準出力
		m_output.setValid(m_valid_Out.getSelection());

		if (m_valid_Out.getSelection()) {
			// 出力先 ディレクトリ
			if (m_directory_Out.getText() == null || m_directory_Out.getText().isEmpty()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("directory")
						+ "(" + Messages.getString("stdout") + ")"}));
				return result;
			}

			// 出力先 ファイル名
			if (m_fileName_Out.getText() == null || m_fileName_Out.getText().isEmpty()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("file.name")
						+ "(" + Messages.getString("stdout") + ")"}));
				return result;
			}
		}

		if (!m_directory_Out.getText().isEmpty()) {
			m_output.setDirectory(m_directory_Out.getText());
		}
		if (!m_fileName_Out.getText().isEmpty()) {
			m_output.setFileName(m_fileName_Out.getText());
		}

		// 出力先 追記する
		m_output.setAppendFlg(m_append_Out.getSelection());

		// ファイル出力失敗時の操作を指定
		m_output.setFailureOperationFlg(m_operation_Out.getSelection());

		// ファイル出力失敗時 操作
		m_output.setFailureOperationType(getSelectOperation(m_operationType_Out));

		// ファイル出力失敗時 終了状態
		m_output.setFailureOperationEndStatus(getSelectStatus(m_operationEndStatus_Out));

		// ファイル出力失敗時 終了値
		try {
			m_output.setFailureOperationEndValue(
					Integer.parseInt(m_operationEndValue_Out.getText()));
		} catch (NumberFormatException e) {
			if (m_output.getValid() && m_output.getFailureOperationFlg()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("end.value")
						+ "(" + Messages.getString("stdout") + ")"}));
				return result;
			}
		}

		// ファイル出力失敗時に通知する
		m_output.setFailureNotifyFlg(m_notify_Out.getSelection());

		// ファイル出力失敗時に通知 重要度(コンボボックス) */
		m_output.setFailureNotifyPriority(getSelectPriority(m_notifyPriority_Out));

		// 標準エラー出力情報のインスタンスを作成・取得
		m_errOutput = new JobOutputInfoResponse();

		// 標準エラー出力
		m_errOutput.setValid(m_valid_Err.getSelection());

		// 出力先 標準出力と同じ出力先を使用する
		if (m_valid_Err.getSelection() && m_sameNormal_Err.getSelection()) {
			if (m_output.getDirectory() == null || m_output.getDirectory().isEmpty()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("directory")
						+ "(" + Messages.getString("stdout") + ")"}));
				return result;
			}
			if (m_output.getFileName() == null || m_output.getFileName().isEmpty()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("file.name")
						+ "(" + Messages.getString("stdout") + ")"}));
				return result;
			}

		}
		m_errOutput.setSameNormalFlg(m_sameNormal_Err.getSelection());

		if (m_valid_Err.getSelection() && !m_sameNormal_Err.getSelection()) {
			// 出力先 ディレクトリ
			if (m_directory_Err.getText() == null || m_directory_Err.getText().isEmpty()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("directory")
						+ "(" + Messages.getString("stderr") + ")"}));
				return result;
			}

			// 出力先 ファイル名
			if (m_fileName_Err.getText() == null || m_fileName_Err.getText().isEmpty()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("file.name")
						+ "(" + Messages.getString("stderr") + ")"}));
				return result;
			}
		}
		if (!m_directory_Err.getText().isEmpty()) {
			m_errOutput.setDirectory(m_directory_Err.getText());
		}
		if (!m_fileName_Err.getText().isEmpty()) {
			m_errOutput.setFileName(m_fileName_Err.getText());
		}

		// 出力先 追記する
		m_errOutput.setAppendFlg(m_append_Err.getSelection());

		// ファイル出力失敗時の操作を指定
		m_errOutput.setFailureOperationFlg(m_operation_Err.getSelection());

		// ファイル出力失敗時 操作
		m_errOutput.setFailureOperationType(getSelectOperation(m_operationType_Err));

		// ファイル出力失敗時 終了状態
		m_errOutput.setFailureOperationEndStatus(getSelectStatus(m_operationEndStatus_Err));

		// ファイル出力失敗時 終了値
		try {
			m_errOutput.setFailureOperationEndValue(
					Integer.parseInt(m_operationEndValue_Err.getText()));
		} catch (NumberFormatException e) {
			if (m_errOutput.getValid() && m_errOutput.getFailureOperationFlg()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("end.value")
						+ "(" + Messages.getString("stderr") + ")"}));
				return result;
			}
		}

		// ファイル出力失敗時に通知する
		m_errOutput.setFailureNotifyFlg(m_notify_Err.getSelection());

		// ファイル出力失敗時に通知 重要度(コンボボックス) */
		m_errOutput.setFailureNotifyPriority(getSelectPriority(m_notifyPriority_Err));

		return null;
	}


	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_valid_Out.setEnabled(enabled);
		setOutputEnabled(enabled && m_valid_Out.getSelection());
		m_valid_Err.setEnabled(enabled);
		setErrOutputEnabled(enabled && m_valid_Err.getSelection());
	}

	/**
	 * 標準出力のオブジェクトの使用可・使用不可を設定します。
	 *
	 * @param enabled true：標準出力をファイルに出力する、false：出力しない
	 */
	private void setOutputEnabled(boolean enabled) {
		if (enabled) {
			m_directory_Out.setEditable(true);
			m_fileName_Out.setEditable(true);
			m_append_Out.setEnabled(true);
			m_operation_Out.setEnabled(true);
			m_operationType_Out.setEnabled(m_operation_Out.getSelection());
			JobOutputInfoResponse.FailureOperationTypeEnum type = getSelectOperation(m_operationType_Out);
			if (!m_operation_Out.getSelection() || (m_operation_Out.getSelection()
					&& type == JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND)) {
				m_operationEndStatus_Out.setEnabled(false);
				m_operationEndValue_Out.setEditable(false);
			} else {
				m_operationEndStatus_Out.setEnabled(true);
				m_operationEndValue_Out.setEditable(true);
			}
			m_notify_Out.setEnabled(true);
			m_notifyPriority_Out.setEnabled(m_notify_Out.getSelection());
		} else {
			m_directory_Out.setEditable(false);
			m_fileName_Out.setEditable(false);
			m_append_Out.setEnabled(false);
			m_operation_Out.setEnabled(false);
			m_operationType_Out.setEnabled(false);
			m_operationEndStatus_Out.setEnabled(false);
			m_operationEndValue_Out.setEditable(false);
			m_notify_Out.setEnabled(false);
			m_notifyPriority_Out.setEnabled(false);
		}
	}

	/**
	 * 標準エラー出力のオブジェクトの使用可・使用不可を設定します。
	 *
	 * @param enabled true：標準出力をファイルに出力する、false：出力しない
	 */
	private void setErrOutputEnabled(boolean enabled) {
		if (enabled) {
			m_sameNormal_Err.setEnabled(m_valid_Err.getSelection());
			m_directory_Err.setEditable(!m_sameNormal_Err.getSelection());
			m_fileName_Err.setEditable(!m_sameNormal_Err.getSelection());
			m_append_Err.setEnabled(!m_sameNormal_Err.getSelection());
			m_operation_Err.setEnabled(true);
			m_operationType_Err.setEnabled(m_operation_Err.getSelection());
			JobOutputInfoResponse.FailureOperationTypeEnum type = getSelectOperation(m_operationType_Err);
			if (!m_operation_Err.getSelection() || (m_operation_Err.getSelection()
					&& type == JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND)) {
				m_operationEndStatus_Err.setEnabled(false);
				m_operationEndValue_Err.setEditable(false);
			} else {
				m_operationEndStatus_Err.setEnabled(true);
				m_operationEndValue_Err.setEditable(true);
			}
			m_notify_Err.setEnabled(true);
			m_notifyPriority_Err.setEnabled(m_notify_Err.getSelection());
		} else {
			m_sameNormal_Err.setEnabled(false);
			m_directory_Err.setEditable(false);
			m_fileName_Err.setEditable(false);
			m_append_Err.setEnabled(false);
			m_operation_Err.setEnabled(false);
			m_operationType_Err.setEnabled(false);
			m_operationEndStatus_Err.setEnabled(false);
			m_operationEndValue_Err.setEditable(false);
			m_notify_Err.setEnabled(false);
			m_notifyPriority_Err.setEnabled(false);
		}
	}

	/**
	 * 指定した重要度に該当するコンボボックスの項目を選択します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @param enumValue 重要度
	 */
	private void setSelectPriority(Combo combo, JobOutputInfoResponse.FailureNotifyPriorityEnum enumValue) {
		String select = "";
		if (enumValue == JobOutputInfoResponse.FailureNotifyPriorityEnum.CRITICAL) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (enumValue == JobOutputInfoResponse.FailureNotifyPriorityEnum.WARNING) {
			select = PriorityMessage.STRING_WARNING;
		} else if (enumValue == JobOutputInfoResponse.FailureNotifyPriorityEnum.INFO) {
			select = PriorityMessage.STRING_INFO;
		} else if (enumValue == JobOutputInfoResponse.FailureNotifyPriorityEnum.UNKNOWN) {
			select = PriorityMessage.STRING_UNKNOWN;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * コンボボックスにて選択している重要度を取得します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @return 重要度
	 */
	private JobOutputInfoResponse.FailureNotifyPriorityEnum getSelectPriority(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return JobOutputInfoResponse.FailureNotifyPriorityEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return JobOutputInfoResponse.FailureNotifyPriorityEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return JobOutputInfoResponse.FailureNotifyPriorityEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return JobOutputInfoResponse.FailureNotifyPriorityEnum.UNKNOWN;
		}

		return null;
	}

	/**
	 * 指定した操作に該当するコンボボックスの項目を選択します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @param enumValue 操作
	 */
	private void setSelectOperation(Combo combo, JobOutputInfoResponse.FailureOperationTypeEnum enumValue) {
		String select = "";
		if (enumValue == JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND) {
			select = OperationMessage.STRING_STOP_SUSPEND;
		} else if (enumValue == JobOutputInfoResponse.FailureOperationTypeEnum.SET_END_VALUE) {
			select = OperationMessage.STRING_STOP_SET_END_VALUE;
		} else if (enumValue == JobOutputInfoResponse.FailureOperationTypeEnum.SET_END_VALUE_FORCE) {
			select = OperationMessage.STRING_STOP_SET_END_VALUE_FORCE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * コンボボックスにて選択している操作を取得します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @return 操作
	 */
	private JobOutputInfoResponse.FailureOperationTypeEnum getSelectOperation(Combo combo) {
		String select = combo.getText();
		if (select.equals(OperationMessage.STRING_STOP_SUSPEND)) {
			return JobOutputInfoResponse.FailureOperationTypeEnum.SUSPEND;
		} else if (select.equals(OperationMessage.STRING_STOP_SET_END_VALUE)) {
			return JobOutputInfoResponse.FailureOperationTypeEnum.SET_END_VALUE;
		} else if (select.equals(OperationMessage.STRING_STOP_SET_END_VALUE_FORCE)) {
			return JobOutputInfoResponse.FailureOperationTypeEnum.SET_END_VALUE_FORCE;
		}

		return null;
	}

	/**
	 * 指定した終了状態に該当するコンボボックスの項目を選択します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @param enumValue 終了状態
	 */
	private void setSelectStatus(Combo combo, JobOutputInfoResponse.FailureOperationEndStatusEnum enumValue) {
		String select = "";
		select = EndStatusMessage.typeEnumValueToString(enumValue.getValue());

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * コンボボックスにて選択している終了状態を取得します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @return 終了状態
	 */
	private JobOutputInfoResponse.FailureOperationEndStatusEnum getSelectStatus(Combo combo) {
		String select = combo.getText();
		String enmuValue = EndStatusMessage.stringTotypeEnumValue(select);
		return JobOutputInfoResponse.FailureOperationEndStatusEnum.fromValue(enmuValue) ;
	}
}
