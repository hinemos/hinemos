/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.viewer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.bean.JobImageConstant;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブツリー用コンポジットのツリービューア用のLabelProviderクラスです。
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class JobTreeLabelProvider extends LabelProvider {
	
	private static Log m_log = LogFactory.getLog( JobTreeLabelProvider.class );
	
	private boolean printEditable = false; // [編集モード]と表示する場合はtrue

	public JobTreeLabelProvider() {
		super();
		this.printEditable = false;
	}

	public JobTreeLabelProvider(boolean printEditable) {
		super();
		this.printEditable = printEditable;
	}
	/**
	 * ジョブツリーアイテムから表示名を作成し返します。
	 *
	 * @param ジョブツリーアイテム
	 * @return 表示名
	 *
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		JobTreeItemWrapper item = (JobTreeItemWrapper) element;
		JobInfoWrapper info = item.getData();
		JobInfoWrapper.TypeEnum type = info.getType();

		String editable = "";
		if (printEditable && type == JobInfoWrapper.TypeEnum.JOBUNIT ){
			String managerName = JobTreeItemUtil.getManagerName(item);
			if( JobEditStateUtil.getJobEditState(managerName).isLockedJobunitId(info.getJobunitId())) {
				editable = " ["+Messages.getString("edit.mode") + "]";
			}
		}

		if (type == JobInfoWrapper.TypeEnum.COMPOSITE) {
			m_log.debug("TYPE_COMPOSITE name=" + info.getName());
			info.setName(HinemosMessage.replace(info.getName()));
			return info.getName(); 
		} else if (type == JobInfoWrapper.TypeEnum.MANAGER) {
			return Messages.getString("facility.manager") + " (" + info.getName() + ")";
		} else {
			return info.getName() + " (" + info.getId() + ")" + editable;
		}
	}

	/**
	 * ジョブツリーアイテムのジョブ種別に該当するイメージを返します。
	 *
	 * @param ジョブツリーアイテム
	 * @return イメージ
	 *
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		JobTreeItemWrapper item = (JobTreeItemWrapper) element;
		JobInfoWrapper.TypeEnum type = item.getData().getType();
		return JobImageConstant.typeEnumValueToImage(type.getValue());
	}
}
