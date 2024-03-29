/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ選択ダイアログクラスです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class JobTreeDialog extends CommonDialog {
	// ログ
	private static Log m_log = LogFactory.getLog(JobTreeDialog.class);

	/** ジョブツリー用のコンポジット */
	private JobTreeComposite treeComposite = null;
	/** ツリーのみフラグ */
	private boolean m_treeOnly = false;

	private JobTreeItemWrapper m_jobTreeItem = null;

	/** オーナーロールID **/
	private String ownerRoleId = null;

	/** マネージャ名 */
	private String managerName = null;

	/**
	 * 表示ツリーの形式
	 * 値として、JobConstantクラスで定義したものが入る
	 * @see com.clustercontrol.jobmanagement.bean.JobConstant
	 *  null : 選択したユニット、ネットの子のみ表示する
	 *  TYPE_REFERJOB,TYPE_REFERJOBNET		: 選択したユニット、ネットの所属するジョブユニット以下すべて表示する
	 */
	private JobInfoWrapper.TypeEnum m_mode = null;

	/**
	 * 表示するジョブ種別のリスト
	 * 値として、JobConstantクラスで定義したものが入る
	 * @see com.clustercontrol.jobmanagement.bean.JobConstant
	 *  null : 全てのユニット、ネット
	 */
	private List<JobInfoWrapper.TypeEnum> m_targetJobTypeList = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親シェル
	 * @param treeOnly
	 *            true：ツリーのみ、false：ジョブ情報を含む
	 */
	public JobTreeDialog(Shell parent, String managerName, String ownerRoleId, boolean treeOnly) {
		super(parent);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
		m_treeOnly = treeOnly;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親シェル
	 * @param jobTreeItem
	 * @param mode
	 *            表示元ジョブ種別
	 * @param targetJobTypeList
	 *            表示対象のジョブ種別
	 */
	public JobTreeDialog(Shell parent, String ownerRoleId, JobTreeItemWrapper jobTreeItem, JobInfoWrapper.TypeEnum mode,
			List<JobInfoWrapper.TypeEnum> targetJobTypeList) {
		super(parent);
		this.ownerRoleId = ownerRoleId;
		m_jobTreeItem = jobTreeItem;
		m_treeOnly = true;
		m_mode = mode;
		m_targetJobTypeList = targetJobTypeList;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 *
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		if (m_log.isTraceEnabled()) {
			m_log.trace("customizeDialog() start");
		}
		// タイトル
		parent.getShell().setText(Messages.getString("select.job"));

		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		if(m_jobTreeItem == null){
			treeComposite = new JobTreeComposite(parent, SWT.NONE, this.managerName, ownerRoleId, m_treeOnly, false);
		}
		else {
			treeComposite = new JobTreeComposite(parent, SWT.NONE, ownerRoleId, m_jobTreeItem, m_mode, m_targetJobTypeList);
		}
		WidgetTestUtil.setTestId(this, null, treeComposite);

		createGridData();
		addDoubleClick();
		if (m_log.isTraceEnabled()) {
			m_log.trace("customizeDialog() end");
		}
	}

	/**
	 * グリッドデータを生成し、コンポジットにセットします
	 */
	private void createGridData() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		treeComposite.setLayoutData(gridData);
	}

	/**
	 * アイテムをダブルクリックした場合、それを選択したこととします
	 */
	private void addDoubleClick() {
		treeComposite.getTreeViewer().addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						okPressed();
					}
				});
	}

	/**
	 * 選択されたジョブツリーアイテムを返します。
	 *
	 * @return ジョブツリーアイテム
	 */
	public List<JobTreeItemWrapper> getSelectItem() {
		return this.treeComposite.getSelectItemList();
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		JobTreeItemWrapper item = this.getSelectItem().isEmpty() ? null : this.getSelectItem().get(0);
		if (item != null) {
			if (item.getData().getType() == JobInfoWrapper.TypeEnum.COMPOSITE) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.1"));
			}
			//参照ジョブ/参照ジョブネットの場合、参照ジョブ/参照ジョブネットは選択不可
			else if (m_mode == JobInfoWrapper.TypeEnum.REFERJOB) {
				if (item.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOB ||
						item.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOBNET) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.127"));
				}
			}
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.1"));
		}

		return result;
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
