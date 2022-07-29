/* Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.job;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.xcloud.model.cloud.IStorage;

/**
 * クラウドストレージ選択用のツリーダイアログクラス
 */
public class StorageTreeDialog extends CommonDialog {

	/** マネージャ名 */
	private String managerName = null;

	/** 選択されたアイテム */
	private StorageTreeComposite treeComposite = null;

	/** コンストラクタ */
	public StorageTreeDialog(Shell parent, String managerName) {
		super(parent);
		this.managerName = managerName;
	}

	/** 選択されたストレージを取得 */
	public IStorage getSelectItem() {
		return this.treeComposite.getSelectItem();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	/**
	 * ダイアログエリアの生成
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		// タイトル
		parent.getShell().setText(Messages.getString("select.storage"));

		// レイアウト設定
		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// ストレージツリー生成
		treeComposite = new StorageTreeComposite(parent, SWT.NONE, this.managerName);
		WidgetTestUtil.setTestId(this, null, treeComposite);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		treeComposite.setLayoutData(gridData);

		// アイテムをダブルクリックした場合、それを選択したこととする。
		treeComposite.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
	}

	/**
	 * 選択値の検証
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		IStorage item = this.getSelectItem();
		if (item == null) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.select_subject"
					, new Object[]{Messages.getString("word.storage")}));
		}

		return result;
	}

	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
}
