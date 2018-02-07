/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.dialog;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.infra.composite.FileDiffComposite;
import com.clustercontrol.infra.util.CharsetSetting;


/**
 * 環境構築[ログイン情報入力]ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class FileDiffDialog extends CommonDialog {

	private static Logger m_log = Logger.getLogger(FileDiffDialog.class);
	
	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 4;

	private String m_title = null;
	
	private byte[] m_oldFile;
	private byte[] m_newFile;
	private String m_oldFilename = "";
	private String m_newFilename = "";
	
	/** 文字コード */
	private Combo m_comboCharset = null;
	private FileDiffComposite m_compositeDiff = null;
	private CharsetSetting m_charsetSetting = null;


	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 */
	public FileDiffDialog(Shell parent) {
		super(parent);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(800, 700);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 * @throws UnsupportedEncodingException 
	 * @see #setInputData(Pattern)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// タイトル
		parent.getShell().setText(m_title);

		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		layout.marginHeight = 3;
		layout.marginWidth = 3;
		
		Composite compositeCharset = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "charset", compositeCharset);
		layout = new GridLayout(2, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 4;
		compositeCharset.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		compositeCharset.setLayoutData(gridData);
		
		Label label = new Label(compositeCharset, SWT.NONE);
		WidgetTestUtil.setTestId(this, "charset", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("character.set") + " : ");

		// テキスト
		this.m_comboCharset= new Combo(compositeCharset, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "charset", m_comboCharset);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboCharset.setLayoutData(gridData);
		this.m_comboCharset.add("UTF-8");
		this.m_comboCharset.add("EUC-JP");
		this.m_comboCharset.add("MS932");
		
		m_charsetSetting = SingletonUtil.getSessionInstance(CharsetSetting.class);
		if (m_charsetSetting.getCharset() == null) {
			m_charsetSetting.setCharset("UTF-8");
		}
		this.m_comboCharset.setText(m_charsetSetting.getCharset());

		this.m_comboCharset.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateFile();
			}
		});
		
		m_compositeDiff = new FileDiffComposite(parent, SWT.BACKGROUND);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_compositeDiff.setLayoutData(gridData);
		
		updateFile();
		
		String oldMd5 = "";
		String newMd5 = "";
		MessageDigest md;
		
		try {
			md = MessageDigest.getInstance("MD5");
			if (m_oldFile != null) {
				oldMd5 = hashByte2MD5(md.digest(m_oldFile));
			}
			if (m_newFile != null) {
				newMd5 = hashByte2MD5(md.digest(m_newFile));
			}
		} catch (NoSuchAlgorithmException e) {
			m_log.warn(e.getMessage());
		}

		m_compositeDiff.setHeader(Messages.getString("infra.module.file.old") + "(" + m_oldFilename + ")\n" + oldMd5,
			Messages.getString("infra.module.file.new") +"(" + m_newFilename + ")\n" + newMd5);
	}
	
	public void setTitle(String title){
		m_title = title;
	}
	
	private static String hashByte2MD5(byte []input) {
		StringBuilder ret = new StringBuilder();
		for (byte b : input) {
			if ((0xff & b) < 0x10) {
				ret.append("0" + Integer.toHexString((0xFF & b)));
			} else {
				ret.append(Integer.toHexString(0xFF & b));
			}
		}
		return ret.toString();
	}

	public void setOldFile(byte[] oldFile) {
		m_oldFile = oldFile;
	}
	
	public void setNewFile(byte[] newFile) {
		m_newFile = newFile;
	}

	public void setOldFilename(String oldFilename) {
		m_oldFilename = oldFilename;
	}
	
	public void setNewFilename(String newFilename) {
		m_newFilename = newFilename;
	}
	
	private void updateFile() {
		Charset charset = Charset.forName(m_comboCharset.getText());
		
		String oldFileStr = null;
		if (m_oldFile == null) {
			oldFileStr = "";
		} else {
			oldFileStr = new String(m_oldFile, charset);
		}
		String newFileStr = null;
		if (m_newFile == null) {
			newFileStr = "";
		} else {
			newFileStr = new String(m_newFile, charset);
		}
		m_compositeDiff.setInput(oldFileStr, newFileStr);
		m_charsetSetting.setCharset(m_comboCharset.getText());
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
}
