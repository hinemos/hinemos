/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.dialog;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.infra.dialog.ChangeBackgroundModifyListener;
import com.clustercontrol.utility.traputil.action.MibManager;
import com.clustercontrol.utility.traputil.action.ParseMib;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMasterInfo;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMibMasterData;
import com.clustercontrol.utility.traputil.composite.MibListComposite;
import com.clustercontrol.util.Messages;

import com.clustercontrol.utility.mib.MibLoaderException;
import com.clustercontrol.utility.mib.MibLoaderLog.LogEntry;

/**
 * SNMPTRAP[インポート] MIBファイル指定ダイアログ<BR>
 * 
 * @version 2.4.0
 * @since 2.4.0
 */
public class MibFileInputDialog extends CommonDialog {
	
	/** ログ出力用 */
	private static Log log = LogFactory.getLog(MibFileInputDialog.class);
	
	/** このダイアログインスタンス */
	private MibFileInputDialog m_dialog;
	
	/** 読み込んだファイル名の履歴とパースステータス(boolean) */
	private Properties history = null;
	
	/** インポートビューのMIB表示コンポジット */
	private MibListComposite mibListComposite = null;
	
	/** 読込み時のデフォルトの重要度 */
	private int defaultPriority = PriorityConstant.TYPE_CRITICAL;

	/** 読込み時の重要度 */
	private int priority;
	
	/** ファイル名Text */
	private Text m_textInputMIB = null;
	
	/** ディレクトリ名Text */
	private Text m_textSearchDir = null;
	
	/** 入力MIBファイル指定ボタン */
	private Button m_buttonInputFile = null;
	
	/** 入力MIBディレクトリ指定ボタン */
	private Button m_buttonInputDir = null;
	
	/** 重要度用ボタン */
	private Button m_buttonInfo = null;
	private Button m_buttonWarn = null;
	private Button m_buttonCrit = null;
	private Button m_buttonUnknown = null;
	

	/** 対象MIBファイル */
	private String mibFile = null;

	/** MIBファイル解析時の検索パス */
	private String searchPath = null;
	
	/** 重要度選択ボタングループ */
	private Group priorityGroup = null;
	
	/** MIBパーサ */
	ParseMib parser;

	
	/** MIB検索用チェックボックス */
	private Button m_currentInput = null;
	
	/** 厳密にチェックするチェックボックス */
	private Button m_strictlyAnalyze = null;
	
	/**
	 * インスタンスを返します。
	 * 
	 * @param parent 親のシェルオブジェクト
	 */
	public MibFileInputDialog(Shell parent) {
		super(parent);
		this.history = new Properties();
		this.priority = this.defaultPriority;
		parser = new ParseMib();
		m_dialog = this;
	}
	
	/**
	 * ダイアログの初期サイズを返します。
	 * 
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}
	
	/**
	 * ダイアログエリアを生成します。
	 * 
	 * @param parent 親のコンポジット
	 * 
	 * @see com.clustercontrol.monitor.action.GetEventReportProperty#getProperty()
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		
		Shell shell = this.getShell();
		
		
		// 変数として利用されるラベル
		Label label = null;
		
		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		
		// タイトル
		shell.setText(Messages.getString("dialog.traputil.fileinput.title"));
		
		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);
		
		/*
		 * 入力MIBファイル
		 */
		// ラベル
		label = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("dialog.traputil.fileinput.label.1"));

		// テキスト
		this.m_textInputMIB = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textInputMIB.setLayoutData(gridData);
		this.m_textInputMIB.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		this.m_textInputMIB.addModifyListener(new ChangeBackgroundModifyListener());

		// 入力MIBファイル指定ボタン
		this.m_buttonInputFile = new Button(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonInputFile.setLayoutData(gridData);
		this.m_buttonInputFile.setText(Messages.getString("button.traputil.file"));
		this.m_buttonInputFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				
				// ファイルダイアログを開く
				FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.APPLICATION_MODAL );

				String filePath = dialog.open();
				
				if(filePath != null && !"".equals(filePath)){
				
					String fileName =dialog.getFileName();
				
					if(fileName != null && !"".equals(fileName)){
						m_textInputMIB.setText(filePath);
					}
				}
			}
		});
		
		// 空白行
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		
		
		// 入力MIBディレクトリ指定ボタン
		this.m_buttonInputDir = new Button(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonInputDir.setLayoutData(gridData);
		this.m_buttonInputDir.setText(Messages.getString("button.traputil.directory"));
		this.m_buttonInputDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				
				// ディレクトリダイアログを開く
				DirectoryDialog dialog = new DirectoryDialog(shell, SWT.APPLICATION_MODAL);
				
				String dirPath = dialog.open();
				
				if(dirPath != null && !"".equals(dirPath)){
					m_textInputMIB.setText(dirPath);
				}
			}
		});

		// 空白行
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		
		/*
		 * MIB検索ディレクトリパス
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("dialog.traputil.fileinput.label.2"));

		// テキスト
		this.m_textSearchDir = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textSearchDir.setLayoutData(gridData);
		
		// MIB検索ディレクトリ指定ボタン
		this.m_buttonInputFile = new Button(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonInputFile.setLayoutData(gridData);
		this.m_buttonInputFile.setText(Messages.getString("button.traputil.directory"));
		this.m_buttonInputFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				
				// ディレクトリダイアログを開く
				DirectoryDialog dialog = new DirectoryDialog(shell, SWT.APPLICATION_MODAL);
				dialog.setMessage(Messages.getString("dialog.traputil.fileinput.searchpath"));

				String dirPath = dialog.open();
				if(dirPath != null && !"".equals(dirPath)){
					m_textSearchDir.setText(dirPath);
				}
			}
		});
		
		// 入力MIBファイルのディレクトリをMIB検索ディレクトリに含めるチェックボックスを用意（デフォルトtrue）
		this.m_currentInput = new Button(parent, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_currentInput.setLayoutData(gridData);
		this.m_currentInput.setText(Messages.getString("message.traputil.20"));
		this.m_currentInput.setSelection(true);
		
		
		// 空白行
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * デフォルトのイベント重要度選択ラジオボタン
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("dialog.traputil.fileinput.label.3"));

		// ラジオボタンを格納するグループ
		priorityGroup = new Group(parent, SWT.NONE);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		priorityGroup.setLayout(fillLayout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		gridData.verticalSpan = 4;
		priorityGroup.setLayoutData(gridData);
		
		// ラジオボタン
		m_buttonInfo = new Button(priorityGroup, SWT.RADIO);
		m_buttonInfo.setText(Messages.getString(PriorityMessage.typeToString(PriorityConstant.TYPE_INFO)));
		if(defaultPriority == PriorityConstant.TYPE_INFO) {
			m_buttonInfo.setSelection(true);
		}
		m_buttonInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(m_buttonInfo.getSelection()) {
					setPriority(PriorityConstant.TYPE_INFO);
				}
			}
		});
		
		m_buttonWarn = new Button(priorityGroup, SWT.RADIO);
		m_buttonWarn.setText(Messages.getString(PriorityMessage.typeToString(PriorityConstant.TYPE_WARNING)));
		if(defaultPriority == PriorityConstant.TYPE_WARNING) {
			m_buttonWarn.setSelection(true);
		}
		m_buttonWarn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(m_buttonWarn.getSelection()) {
					setPriority(PriorityConstant.TYPE_WARNING);
				}
			}
		});
		
		m_buttonCrit = new Button(priorityGroup, SWT.RADIO);
		m_buttonCrit.setText(Messages.getString(PriorityMessage.typeToString(PriorityConstant.TYPE_CRITICAL)));
		if(defaultPriority == PriorityConstant.TYPE_CRITICAL) {
			m_buttonCrit.setSelection(true);
		}
		
		m_buttonCrit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(m_buttonCrit.getSelection()) {
					setPriority(PriorityConstant.TYPE_CRITICAL);
				}
			}
		});
		
		m_buttonUnknown = new Button(priorityGroup, SWT.RADIO);
		m_buttonUnknown.setText(Messages.getString(PriorityMessage.typeToString(PriorityConstant.TYPE_UNKNOWN)));
		if(defaultPriority == PriorityConstant.TYPE_UNKNOWN) {
			m_buttonUnknown.setSelection(true);
		}
		m_buttonUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(m_buttonUnknown.getSelection()) {
					setPriority(PriorityConstant.TYPE_UNKNOWN);
				}
			}
		});
		
		// 空白行
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		
		// 厳密な解析を行うかのチェック（デフォルトtrue）
		this.m_strictlyAnalyze = new Button(parent, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_strictlyAnalyze.setLayoutData(gridData);
		this.m_strictlyAnalyze.setText(Messages.getString("message.traputil.21"));
		this.m_strictlyAnalyze.setSelection(true);
		
		
		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);
		
		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, shell.getSize().y));
		
		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}
	
	/**
	 * 入力値チェックをします。
	 * 
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		
		ValidateResult validateResult = null;

		
		if (this.m_textInputMIB.getText() != null
                && !"".equals((this.m_textInputMIB.getText()).trim())) {
			
    		this.mibFile = this.m_textInputMIB.getText();
    		
        }
		else {
			validateResult = new ValidateResult();
            validateResult.setValid(false);
            validateResult.setID(Messages.getString("message.hinemos.1"));
            validateResult.setMessage(Messages.getString("message.traputil.1"));
    		return validateResult;
		}
		
		// 重複チェック
		// 重複が無ければ履歴に追加、重複の場合はエラー
		// このインスタンスが閉じられるまでに読み込んだファイル履歴との比較
		// 異なるインスタンスからの入力にはファイル解析後にMibManagerにて行う
		
		if(history.containsKey(this.m_textInputMIB.getText())) {
			if((Boolean) history.get(this.m_textInputMIB.getText()) == true) {
				validateResult = new ValidateResult();
				validateResult.setValid(false);
	            validateResult.setID(Messages.getString("message.hinemos.1"));
	            validateResult.setMessage(Messages.getString("message.traputil.9"));
	    		return validateResult;
			}
		}

		// MIB検索パスのチェックは不要(入力非必須)
		if(this.m_textSearchDir != null && !"".equals(this.m_textSearchDir.getText().trim())) {
			this.searchPath = this.m_textSearchDir.getText();
		}
		else {
			this.searchPath = null;
		}
		
		// ファイル・ディレクトリの実在判定はParseMibクラスの
		// Fileオブジェクト作成時に行う
		
		// デフォルト重要度指定のチェックは不要(デフォルトが利用される)
		
		
		return null;

	}
	
	@Override
	protected boolean action() {
		
		SnmpTrapMibMasterData master = null;
		ArrayList<SnmpTrapMasterInfo> details = null;
		
			//ファイルのロード
			if(this.searchPath != null) {
				parser.addSearchPath(searchPath);
			}
			
			// ファイル・ディレクトリのいずれかチェック
			File mibfile = new File(this.mibFile);
			boolean isFile = mibfile.isFile();
			boolean isDirectory = mibfile.isDirectory();
			boolean trapExist = false;
			ArrayList<String> filePathList = new ArrayList<String>();
			
			//読み込み成功したものをダイアログ表示するために使用
			MultiStatus mStatusInfo = new MultiStatus(this.toString(), IStatus.OK, null, null);
			//読み込み失敗したものをダイアログ表示するために使用
			MultiStatus mStatusError = new MultiStatus(this.toString(), IStatus.OK, null, null);
			
			String errorMessage = Messages.getString("message.traputil.10");

			// 入力としてディレクトリが指定された場合
			if (isDirectory) {
				String[] files = mibfile.list();
				if (null != files){
					for (String file : files) {
						filePathList.add(mibfile.getPath() + "\\" + file);
					}
				}
			}
			// 入力としてファイルが指定された場合
			else if (isFile){
				filePathList.add(mibfile.getPath());
			}
			// 入力がファイルでもディレクトリでも場合
			else{
				errorMessage = Messages.getString("message.traputil.1");
			}
			
			for (String filepath : filePathList) {
				try {
					if(parser.parseTrapMib(filepath, this.m_currentInput.getSelection())){
						history.put(filepath, Boolean.TRUE);
						//MIB情報とMIB詳細情報の抽出
						details = parser.getDetails(this.priority, this.m_strictlyAnalyze.getSelection());
						
						if(details != null && details.size() != 0) {
							trapExist = true;
							master = parser.getMibMaster();
							mStatusInfo.add(new Status(IStatus.INFO, this.toString(), IStatus.OK, Messages.getString("message.traputil.23")
								+ " : " + master.getMib(), null));
							log.info(Messages.getString("message.traputil.23") + " : " + filepath);
							
							//クライアント内のデータ更新
							try {
								if(MibManager.getInstance(false).addMibMaster(master)){
									//MIB情報側にて重複チェックが行われる
									MibManager.getInstance(false).addMibDetails(details);
									mibListComposite.update();
								} else {
									continue;
								}
							} catch (InvocationTargetException e) {
								MessageDialog.openError(
										null,
										com.clustercontrol.util.Messages.getString("failed"),
										com.clustercontrol.util.Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getCause().getMessage());
							}
						}
					} else {
						// トラップが存在しない場合
						mStatusInfo.add(new Status(IStatus.INFO, this.toString(), IStatus.OK, Messages.getString("message.traputil.10")
								+ " : " + filepath, null));
						errorMessage = Messages.getString("message.traputil.10");
						log.info(errorMessage + " : " + filepath);
					}
				} catch (IOException e) {
					mStatusError.add(new Status(IStatus.WARNING, this.toString(), IStatus.OK, Messages.getString("message.traputil.18") + " : " + filepath, null));
					errorMessage = Messages.getString("message.traputil.18");
					log.error(errorMessage + " : " + filepath, e);
					
				} catch (MibLoaderException e) {
					errorMessage = Messages.getString("message.traputil.12");
					log.error(errorMessage + " : " + filepath, e);
					
					String message = "";
					Iterator<?> it = e.getLog().entries();
					while (it.hasNext()) {
						LogEntry entry = (LogEntry) it.next();
						message = entry.getFile().getName() +" at "+ entry.getLineNumber() + " : " + entry.getMessage();
						mStatusError.add(new Status(IStatus.WARNING, this.toString(),
								IStatus.OK, Messages.getString("message.traputil.12") + " : " + message, null));
						log.error(Messages.getString("message.traputil.12") + " : " + message);
					}
				}
			}
			
			// 読み込みエラーがあった場合はダイアログ出力
			if (!mStatusError.isOK()) {
				ErrorDialog.openError(null, Messages.getString("message.confirm"),
					Messages.getString("message.traputil.22") + "\n" + " - "
							+ Messages.getString("message.traputil.check.1")
							+ "\n" + " - "
							+ Messages.getString("message.traputil.check.2")
							+ "\n" + " - "
							+ Messages.getString("message.traputil.check.3")
							+ "\n", mStatusError);
				return false;
			}
			
			//トラップが無い場合、警告ダイアログを出して終了
			if(!trapExist) {
				
				MessageDialog.openWarning(
						null,
						Messages.getString("message"),
						errorMessage);
				
				MibManager.getInstance(false).clearMibData();
				parser.reset();
				return false;
			}
			
			//最新のMIB情報でSNMPTRAP[インポート]ビューを更新
			mibListComposite.update();
			
			//終了ダイアログ
			ErrorDialog.openError(null,
					Messages.getString("message"),
					Messages.getString("message.traputil.11"), mStatusInfo);

			return true;
			
			
	}

	/**
	 * OK(実行)ボタン押下時の動作
	 * 引き続き処理を継続するため閉じない
	 */
	@Override
	protected void okPressed() {
    	ValidateResult result = this.validate();
        
        if (result == null || result.isValid()) {
        	
        	this.getButton(IDialogConstants.OK_ID).setEnabled(false);
        	this.getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
        	
        	this.action();
        	
        	this.getButton(IDialogConstants.OK_ID).setEnabled(true);
        	this.getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
        	
        	
        } else {
            this.displayError(result);
        }
	}

	/**
	 * 既存のボタンに加え、クリアボタンを追加します。<BR>
	 * クリアボタンがクリックされた場合、MIBファイル入力欄、
	 * 検索パスをクリアし、重要度をデフォルトに戻します。
	 * 
	 * @param parent 親のコンポジット（ボタンバー）
	 * 
	 * @see org.eclipse.swt.widgets.Button#addSelectionListener(SelectionListener)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		
		// クリアボタン
		this.createButton(parent, IDialogConstants.OPEN_ID, Messages
				.getString("clear"), false);
		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						m_dialog.clear();
					}
				});
		
		super.createButtonsForButtonBar(parent);
	}
	
	protected void clear() {
		//このオブジェクトの保持情報のクリア
		this.m_textInputMIB.setText("");
		this.m_textSearchDir.setText("");
		this.mibFile = null;
		this.searchPath = null;
		
		//MibLoaderのリセット
		parser.reset();
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 * 
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("run");
	}
	
	/**
	 * キャンセルボタンのテキストを返します。
	 * 
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("close");
	}

	public void setPriority(int input) {
		this.priority = input;
	}

	public void setMibListComposite(MibListComposite mibListComposite) {
		this.mibListComposite = mibListComposite;
	}
	
}