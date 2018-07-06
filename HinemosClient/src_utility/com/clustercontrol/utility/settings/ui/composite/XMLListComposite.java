/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ui.action.ReadXMLAction;
import com.clustercontrol.utility.settings.ui.bean.FuncTreeItem;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ[一覧]ビュー用のコンポジットクラスです。
 * 
 * @version 6.1.0
 * @since 1.2.0
 */
public class XMLListComposite extends Composite {
	/** テーブルビューア */
	protected CommonTableViewer m_viewer = null;
	/** xmlディレクトリ表示用ラベル */
	protected Label m_xmlDirLabel = null;

	/**
	* コンストラクタ
	* 
	* @param parent 親のコンポジット
	* @param style スタイル
	* 
	* @see org.eclipse.swt.SWT
	* @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	* @see #initialize()
	*/
	public XMLListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	* コンポジットを配置します。
	*/
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		m_xmlDirLabel = new Label(this, SWT.NONE);
		String dir = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		m_xmlDirLabel.setText(Messages.getString("perference.xml.directory") + " : " + dir);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		m_xmlDirLabel.setLayoutData(gridData);

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		ArrayList<TableColumnInfo> column_defs = new ArrayList<TableColumnInfo>();
		
		TableColumnInfo column_def;
		
		//カラムの表示名を定義
		column_def = new TableColumnInfo(Messages.getString("string.setting.name"),TableColumnInfo.NONE,120,SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("string.file.name"),TableColumnInfo.NONE,120,SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("string.schema.type"),TableColumnInfo.NONE,80,SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("string.generate.date"),TableColumnInfo.NONE,120,SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("string.generate.tool"),TableColumnInfo.NONE,90,SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("string.generate.user"),TableColumnInfo.NONE,70,SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("string.generate.client"),TableColumnInfo.NONE,90,SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("string.generate.manager"),TableColumnInfo.NONE,130,SWT.LEFT);
		column_defs.add(column_def);
		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(column_defs,
				1, 1);
		

		UtilityManagerUtil.addManagerChangeListener(new UtilityManagerUtil.ManagerChangeListener() {
			@Override
			public void notifyManagerChanged() {
				String dir = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
				m_xmlDirLabel.setText(Messages.getString("perference.xml.directory") + " : " + dir);
				update();
			}
		});
	}

	/**
	* このコンポジットが利用するテーブルビューアを返します。
	* 
	* @return テーブルビューア
	*/
	public TableViewer getTableViewer() {
		return m_viewer;
	}

	/**
	* このコンポジットが利用するテーブルを返します。
	* 
	* @return テーブル
	*/
	public Table getTable() {
		return m_viewer.getTable();
	}

	/**
	* テーブルビューアーを更新します。<BR>
	* 引数で指定されたジョブツリー情報からジョブ一覧情報を取得し、
	* 共通テーブルビューアーにセットします。
	* <p>
	 * <ol>
	 * <li>引数で指定されたジョブツリー情報からジョブ一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにジョブ一覧情報をセットします。</li>
	 * </ol>
	* 
	* @param item ジョブツリー情報
	* 
	* @see com.clustercontrol.jobmanagement.action.GetJobList#getJobList(FuncTreeItem)
	*/
	@Override
	public void update() {

		List<List<String>> table_value = ReadXMLAction.readHeader();
		m_viewer.setInput(table_value);
		
		String dir = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		m_xmlDirLabel.setText(Messages.getString("perference.xml.directory") + " : " + dir);
		
	}
}