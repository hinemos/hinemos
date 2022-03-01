/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.ReportingScheduleResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.util.Messages;

/**
 * レポーティング書式設定コンポジットクラス<BR>
 * <p>
 * <dl>
 * <dt>コンポジット</dt>
 * <dd>「レポートタイトル」 テキストボックス</dd>
 * <dd>「ロゴの有無」 チェックボックス</dd>
 * <dd>「ページ数の有無」チェックボックス</dd>
 * </dl>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReportFormatComposite extends Composite {
	

	
	/** ロゴあり */
	public static final int WITHOUT_LOGO = 0;

	/** ロゴなし */
	public static final int WITH_LOGO = 1;

	/** ページ番号あり */
	public static final int WITHOUT_PAGE_NUMBER = 0;

	/** ページ番号なし */
	public static final int WITH_PAGE_NUMBER = 1;

	// レポートタイトル
	private Text m_textTitle = null;
	// ロゴの有無
	private Button m_checkLogo = null;
	// ロゴファイル名
	private Text m_logoFilename = null;
	// ページ数の有無
	private Button m_checkPage = null;
	// 出力タイプ
	private Combo m_outputType = null;
	
	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 * 
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 * 
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public ReportFormatComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridData gridData;

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		Group group = new Group(this, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		group.setLayout(layout);

		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group.setLayoutData(gridData);
		group.setText(Messages.getString("report.format"));

		Label label = new Label(group, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("report.title") + ":");

		m_textTitle = new Text(group, SWT.BORDER | SWT.LEFT | SWT.WRAP
				| SWT.MULTI | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalSpan = 11;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 40;
		m_textTitle.setLayoutData(gridData);
		this.m_textTitle.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * ロゴ設定
		 */
		// 表示有無チェックボックス
		m_checkLogo = new Button(group, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_checkLogo.setLayoutData(gridData);
		m_checkLogo.setText(Messages.getString("report.logo"));
		m_checkLogo.setSelection(true);
		m_checkLogo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_logoFilename.setEnabled(m_checkLogo.getSelection());
				update();
			}
		});

		// ロゴファイル名
		label = new Label(group, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("report.logo.filename") + " : ");
		// テキスト
		m_logoFilename = new Text(group, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 9;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_logoFilename.setLayoutData(gridData);
		m_logoFilename.setText("hinemos_logo.png");
		m_logoFilename.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		m_logoFilename.setEnabled(m_checkLogo.getSelection());
		
		/*
		 * ページ出力設定
		 */
		m_checkPage = new Button(group, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_checkPage.setLayoutData(gridData);
		m_checkPage.setText(Messages.getString("report.page"));
		m_checkPage.setSelection(true);
		
		/*
		 * 出力ファイル形式設定
		 */
		label = new Label(group, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("report.output.type") + ":");
		
		m_outputType = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 11;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_outputType.setLayoutData(gridData);
		
	}

	public void createOutputTypeStrList(String managerName) {
		
		/*
		 *  出力形式コンボボックスの初期化
		 */
		this.m_outputType.removeAll();
		
		this.m_outputType.add(ReportOutputTypeConstant.STRING_PDF);
		this.m_outputType.add(ReportOutputTypeConstant.STRING_XLSX);
		
		this.m_outputType.setText("pdf");
		
	}
	
	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_textTitle.setEnabled(enabled);
		m_checkLogo.setEnabled(enabled);
		m_logoFilename.setEnabled(enabled);
		m_checkPage.setEnabled(enabled);
		m_outputType.setEnabled(enabled);
	}

	/**
	 * 更新処理
	 * 
	 */
	@Override
	public void update() {
		
		/*
		 *  各項目が必須項目であることを明示
		 */
		if (m_textTitle.isEnabled() && m_textTitle.getText().equals("")) {
			this.m_textTitle
				.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
				this.m_textTitle
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	
	/**
	 * マネージャ名を設定します。
	 */
	public void setManagerName (String managerName) {
		createOutputTypeStrList(managerName);
	}

	/**
	 * レポーティング情報を反映させます。
	 * 
	 * @param info
	 *            レポーティング情報
	 */
	public void reflectReportingSchedule(ReportingScheduleResponse info) {
		if (info != null) {
			m_textTitle.setText(info.getReportTitle());
			m_checkLogo.setSelection(info.getLogoValidFlg().booleanValue());
			m_logoFilename.setText(info.getLogoFilename());
			m_checkPage.setSelection(info.getPageValidFlg().booleanValue());
			m_outputType.setText(info.getOutputType().getValue());
		}
	}

	/**
	 * レポートタイトルを返します。
	 * 
	 * @return レポートタイトル
	 */
	public String getReportTitle() {
		return m_textTitle.getText();
	}

	/**
	 * ロゴの有無を返します。
	 * 
	 * @return 有り true, 無し　false
	 */
	public Boolean getLogoValidFlg() {
		return m_checkLogo.getSelection();
	}
	
	/**
	 * ロゴのファイル名を返します。
	 * @return
	 */
	public String getLogoFilename() {
		return m_logoFilename.getText();
	}

	/**
	 * ページ番号の有無を返します。
	 * 
	 * @return 有り true, 無し　false
	 */
	public Boolean getPageValidFlg() {
		return m_checkPage.getSelection();
	}
	
	/**
	 * 出力ファイル形式（文字列）を返します。
	 * @return
	 */
	public String getOutputTypeStr() {
		return m_outputType.getText();
	}
}
