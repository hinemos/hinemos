/**********************************************************************
 * Copyright (C) 2006 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.jobmanagement.viewer.JobTreeViewer;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブツリーデータ<BR>
 * 
 * @since 5.0.0
 */
public class JobTreeViewerList {
	// Logger
	private static Log m_log = LogFactory.getLog( JobTreeViewerList.class );

	// JobTreeViewerをまとめて更新するためのリスト
	private List<JobTreeViewer> jobTreeViewers = new ArrayList<JobTreeViewer>();

	/** Private constructor */
	private JobTreeViewerList(){}

	/** Get singleton */
	private static JobTreeViewerList getInstance(){
		return SingletonUtil.getSessionInstance( JobTreeViewerList.class );
	}

	/**  Set input object */
	public static void setInput( final JobTreeViewer currentViewer, JobTreeItem jobTree ){
		JobTreeViewerList jobTreeViewerList = getInstance();
		// ビューを更新した場合は、開いているジョブツリーの情報を全て更新する
		for( JobTreeViewer viewer : jobTreeViewerList.jobTreeViewers ){
			if (viewer == currentViewer){
				continue;
			}
			viewer.setInput(jobTree);
		}
	}

	/**
	 * 表示しているすべてのジョブツリーの表示をリフレッシュする
	 * @param element
	 */
	public static void refresh(){
		JobTreeViewerList jobTreeViewerList = getInstance();
		for( JobTreeViewer viewer : jobTreeViewerList.jobTreeViewers){
			m_log.debug("refresh : " + viewer);
			viewer.refresh();
		}
	}

	/**
	 * 表示しているすべてのジョブツリーの表示をリフレッシュする
	 * @param element
	 */
	public static void refresh( Object element ){
		JobTreeViewerList jobTreeViewerList = getInstance();
		for( JobTreeViewer viewer : jobTreeViewerList.jobTreeViewers ){
			m_log.debug("refresh : " + viewer);
			viewer.refresh(element);
		}
	}

	/**
	 * 現在クライアントが表示しているジョブツリーのリストにこのインスタンスのツリーを追加する<BR>
	 * ビューを開く際に呼ぶこと
	 */
	public static void add( final JobTreeViewer viewer ){
		JobTreeViewerList jobTreeViewerList = getInstance();

		m_log.debug( "add treeViewerList: " + viewer );
		m_log.debug( "viewer.size=" + jobTreeViewerList.jobTreeViewers.size()) ;
		jobTreeViewerList.jobTreeViewers.add( viewer );
		m_log.debug( "viewer.size=" + jobTreeViewerList.jobTreeViewers.size()) ;
	}

	/**
	 * 現在クライアントが表示しているジョブツリーのリストからこのインスタンスのツリーを削除する<BR>
	 * ビューを閉じる際に呼ぶこと
	 */
	public static void remove( final JobTreeViewer viewer ){
		JobTreeViewerList jobTreeViewerList = getInstance();

		m_log.debug("remove treeViewerList: " + viewer);
		m_log.debug( "viewer.size=" + jobTreeViewerList.jobTreeViewers.size()) ;
		jobTreeViewerList.jobTreeViewers.remove( viewer );
		m_log.debug( "viewer.size=" + jobTreeViewerList.jobTreeViewers.size()) ;
	}
}
