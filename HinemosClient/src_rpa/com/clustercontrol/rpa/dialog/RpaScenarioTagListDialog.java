/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.dialog;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.rpa.composite.RpaScenarioTagListToDialogComposite;
import com.clustercontrol.util.Messages;

/**
 * シナリオタグ選択ダイアログクラス<BR>
 */
public class RpaScenarioTagListDialog extends CommonDialog {

	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 800;
	private static final int sizeY = 450;

	/** タグ一覧 コンポジット */
	private RpaScenarioTagListToDialogComposite tagListComposite = null;

	private String managerName = null;

	/** 選択されているタグ */
	private List<RpaScenarioTagResponse> tagList = null;

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public RpaScenarioTagListDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	public RpaScenarioTagListDialog(Shell parent, String managerName) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);

		this.managerName = managerName;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	/**
	 * ダイアログエリアを生成します。
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		shell.setText(Messages.getString("dialog.rpa.tag.list"));

		// レイアウト
		GridLayout layout = new GridLayout(8, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 8;
		parent.setLayout(layout);

		// タグ一覧
		this.tagListComposite = new RpaScenarioTagListToDialogComposite(parent, SWT.BORDER);
		this.tagListComposite.setManagerName(this.managerName);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 7;
		gridData.heightHint = SWT.MIN;
		this.tagListComposite.setLayoutData(gridData);
		this.tagListComposite.setSelectTag(this.tagList);
		this.tagListComposite.update();

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 8;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(sizeX, sizeY ));
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		return super.validate();
	}

	/**
	 * ボタンを生成します。<BR>
	 * 閉じるボタンを生成します。
	 *
	 * @param parent ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("ok"), false);
	}

	/**
	 * ダイアログにもともと選択されていたタグを反映します。
	 */
	public void setSelectTag(List<RpaScenarioTagResponse> tagList){
		this.tagList = tagList;
	}

	/**
	 * 閉じる、キャンセルボタンが押された場合に呼ばれるメソッド
	 * エラーの場合、ダイアログを閉じずにエラー内容を通知します。
	 */
	@Override
	protected void cancelPressed() {

			if(!tagListComposite.makeTagData()){
				MessageDialog.openWarning(
						null,
						Messages.getString("warning"),
						Messages.getString("message.hinemos.failure.unexpected"));
				return;
			}

		//上位のcancelPressで
		super.cancelPressed();
	}

	public List<RpaScenarioTagResponse> getSelectTag(){
		return this.tagListComposite.getSelectTag();
	}
	
	public Map<String,String> getTagNameMap(){
		return this.tagListComposite.getTagNameMap();
	}
	
	public Map<String,String> getTagPathMap(){
		return this.tagListComposite.getTagPathMap();
	}
}
