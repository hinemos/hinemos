/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.rpa.dialog.RpaScenarioTagListDialog;
import com.clustercontrol.rpa.util.RpaScenarioTagUtil;
import com.clustercontrol.util.Messages;


/**
 * シナリオタグ名一覧コンポジットクラス<BR>
 */
public class RpaScenarioTagNameListComposite extends Composite {

	/** タグテーブル */
	private Table tagListTable = null;

	/** タグテーブルの高さ */
	private int tableHeight = 100;

	/** 選択ボタン */
	private Button buttonRefer = null;

	/** タグID一覧フィールド*/
	private List<RpaScenarioTagResponse> tagList = null;

	/** オーナーロールID*/
	private String ownerRoleId = null;

	/** マネージャ名*/
	private String managerName = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite, boolean)
	 */
	public RpaScenarioTagNameListComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize(parent);
	}

	/**
	 * コンポジットを配置します。
	 *
	 * @see #update()
	 */
	private void initialize(Composite parent) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 10;
		this.setLayout(layout);

		//タグ一覧
		this.tagListTable = new Table(this, SWT.BORDER
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		this.tagListTable.setHeaderVisible(true);
		this.tagListTable.setLinesVisible(true);
		this.tagListTable.setLayoutData(new RowData(200, 100));


		TableColumn col = new TableColumn(this.tagListTable,SWT.LEFT);
		col.setText(Messages.getString("rpa.tag.name"));
		col.setWidth(200);

		gridData = new GridData();
		gridData.horizontalSpan = 8 ;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = tableHeight;
		this.tagListTable.setLayoutData(gridData);


		// 選択ボタン
		this.buttonRefer = new Button(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.buttonRefer.setLayoutData(gridData);
		this.buttonRefer.setText(Messages.getString("select"));
		this.buttonRefer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				// ダイアログ表示及び終了処理
				RpaScenarioTagListDialog dialog = new RpaScenarioTagListDialog(shell, managerName);
				if (tagList != null) {
					dialog.setSelectTag(tagList);
				}
				dialog.open();

				// ダイアログからデータを取得してタグIDを設定する
				setTagList(dialog.getSelectTag(), dialog.getTagNameMap(), dialog.getTagPathMap());

				// コンポジットを更新する
				update();
			}
		});
	}

	/**
	 * コンポジットを更新します。<BR>
	 */
	@Override
	public void update() {
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.buttonRefer.setEnabled(enabled);
		this.tagListTable.setEnabled(enabled);
	}

	public void setButtonEnabled(boolean enabled) {
		this.buttonRefer.setEnabled(enabled);
	}

	/**
	 * シナリオタグを返します。
	 *
	 * @return シナリオタグ
	 *
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public List<RpaScenarioTagResponse> getTagList() {
		if (tagList == null){
			tagList = new ArrayList<>();
		}
		return tagList;
	}

	/**
	 * シナリオタグを設定します。
	 *
	 * @param tagList 取得したシナリオタグ
	 *
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setTagList(List<RpaScenarioTagResponse> tagList, Map<String,String> tagNameMap, Map<String,String> tagPathMap) {

		//フィールドに追加
		this.tagList = tagList;

		this.tagListTable.removeAll();

		// 表示に追加
		for (RpaScenarioTagResponse tag : tagList) {
			TableItem ti = new TableItem(this.tagListTable, 0);

			String[] repData = { new RpaScenarioTagUtil().getTagLayer(tag, tagNameMap, tagPathMap) };

			ti.setText(repData);
		}
		this.tagListTable.update();
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/**
	 * @return the managerName
	 */
	public String getManagerName() {
		return managerName;
	}

	/**
	 * @param managerName the managerName to set
	 */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
		this.tagListTable.removeAll(); 
		this.tagList = null; 
	}
}
