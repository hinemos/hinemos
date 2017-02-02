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

package com.clustercontrol.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.presentation.PresentationReconciler;

import com.clustercontrol.util.Messages;


/**
 * テキストエリアにテキストを表示するためのダイアログ<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class TextAreaDialog extends CommonDialog{

	// ----- instance フィールド ----- //

	/** 値変更の可/不可 */
	private boolean m_modify = false;
	private boolean editable = true;

	/** テキストエリア */
	private TextViewer m_text = null;

	/** ダイアログのタイトル */
	private String m_title = Messages.getString("text");

	/** テキストエリアに表示する文字列 */
	private String m_displayString = null;
	
	private String okButtonText = Messages.getString("ok");
	private String cancelButtonText = Messages.getString("cancel");
	

	// ----- コンストラクタ ----- //

	/**
	 * ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 *            親とするシェル
	 * @param title
	 *            ダイアログのタイトル
	 * @param modify
	 *            true: OK/CANCEL, false: CANCEL
	 * @param editable
	 *            テキストエリアの変更有無
	 */
	public TextAreaDialog(Shell parent, String title, boolean modify) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		if (title != null) {
			this.m_title = title;
		}
		this.m_modify = modify;
		this.editable = true;
	}
	
	public TextAreaDialog(Shell parent, String title, boolean modify, boolean editable) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		if (title != null) {
			this.m_title = title;
		}
		this.m_modify = modify;
		this.editable = editable;
	}

	// ----- instance メソッド ----- //

	@Override
	protected Point getInitialSize() {
		return new Point(600, 600);
	}

	@Override
	protected void customizeDialog(Composite parent) {
		// タイトル
		parent.getShell().setText(m_title);

		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 3;
		layout.marginWidth = 3;

		// テキストエリア
		this.m_text = new TextViewer(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP
				| SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.m_text.getTextWidget().setLayoutData(gridData);
		this.m_text.setDocument(new Document(m_displayString), 0, m_displayString.length());
		this.m_text.getTextWidget().setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.m_text.setEditable(m_modify && editable);
		
		// ハイパーリンク
		PresentationReconciler reconciler= new PresentationReconciler();
		reconciler.setDocumentPartitioning(IDocumentExtension3.DEFAULT_PARTITIONING);
		reconciler.install((ITextViewer) this.m_text);
		this.m_text.setHyperlinkPresenter(new DefaultHyperlinkPresenter(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE)));
		this.m_text.setHyperlinkDetectors(new IHyperlinkDetector[] { new URLHyperlinkDetector() }, SWT.NONE);
	}

	/**
	 * テキスト設定
	 * 
	 * @param displayString
	 */
	public void setText(String displayString) {
		this.m_displayString = displayString;
	}

	/**
	 * テキスト取得
	 * 
	 * @return
	 */
	public String getText() {
		return this.m_displayString;
	}

	public void setOkButtonText(String str) {
		okButtonText = str;
	}
	
	public void setCancelButtonText(String str) {
		cancelButtonText = str;
	}
	
	@Override
	protected String getOkButtonText() {
		return okButtonText;
	}

	@Override
	protected String getCancelButtonText() {
		return cancelButtonText;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		this.m_displayString = this.m_text.getDocument().get();
		return null;
	}

	/**
	 * ボタンを作成します。
	 * 
	 * @param parent
	 *            ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if (this.m_modify) {
			super.createButtonsForButtonBar(parent);
		} else {
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID,
					Messages.getString("ok"), false);
		}
	}
}
