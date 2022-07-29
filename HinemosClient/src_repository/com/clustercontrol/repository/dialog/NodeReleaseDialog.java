/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.ReleaseNodeScopeRequest;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.composite.NodeFilterComposite;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * スコープへのノード割り当て解除ダイアログクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeReleaseDialog extends CommonDialog {

	private static final int sizeX = 800;
	private static final int sizeY = 500;

	// ----- instance フィールド ----- //

	/** 割り当て対象スコープのファシリティID */
	private String facilityId = "";

	/** ノード一覧テーブル */
	private NodeFilterComposite nodeList = null;

	/** 選択されたアイテム */
	private List<String> filterItems = null;

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * 指定した形式のダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param facilityId
	 *            初期表示するスコープのファシリティID
	 */
	public NodeReleaseDialog(Shell parent, String managerName, String facilityId) {
		super(parent);

		this.managerName = managerName;
		this.facilityId = facilityId;
	}

	// ----- instance メソッド ----- //

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
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.repository.select.nodes"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		/*
		 * ノード一覧
		 */

		// テーブル
		this.nodeList = new NodeFilterComposite(parent, SWT.NONE, this.managerName, this.facilityId, false);
		WidgetTestUtil.setTestId(this, null, nodeList);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = SWT.MIN;
		this.nodeList.setLayoutData(gridData);

		this.nodeList.update();

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(sizeX, sizeY ));

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
		return super.validate();
	}

	/**
	 * 割り当て対象スコープのファシリティIDを返します。
	 *
	 * @return 割り当て対象スコープのファシリティID
	 */
	public String getScopeId() {
		return this.facilityId;
	}

	/**
	 * 選択されたノードを返します。
	 *
	 * @return ノードのファシリティID
	 */
	public List<String> getFilterItems() {
		return this.filterItems;
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

	/**
	 * OKボタンが押下された場合の動作。
	 */
	@Override
	protected void okPressed() {

		// 画面情報を取得
		StructuredSelection selection = (StructuredSelection) this.nodeList
				.getTableViewer().getSelection();

		Object[] items = selection.toArray();
		if (items != null) {
			int size = items.length;
			this.filterItems = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				this.filterItems.add((String) ((ArrayList<?>) items[i]).get(1));
			}
		}

		// 登録
		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(this.managerName);
			ReleaseNodeScopeRequest requestDto = new ReleaseNodeScopeRequest();
			requestDto.setFacilityIdList(this.filterItems);
			wrapper.releaseNodeScope(facilityId, requestDto);

			// リポジトリキャッシュの更新
			ClientSession.doCheck();

			// 成功報告ダイアログを生成
			Object[] arg = {this.managerName};
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.repository.12", arg));

			super.okPressed();

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}
			// 失敗報告ダイアログを生成
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.repository.13") + errMessage);
		}
	}
}
