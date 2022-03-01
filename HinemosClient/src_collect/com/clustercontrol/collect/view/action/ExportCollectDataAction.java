/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.CollectKeyInfoResponseP1;

import com.clustercontrol.collect.dialog.ExportDialog;
import com.clustercontrol.collect.util.CollectGraphUtil;
import com.clustercontrol.collect.util.CollectGraphUtil.CollectFacilityDataInfo;
import com.clustercontrol.util.Messages;


/**
 * 収集済みデータのエクスポートを行うアクションクラス
 *
 * @version 5.0.0
 * @since 1.0.0
 */

public class ExportCollectDataAction extends AbstractHandler{
	public static final String ID = ExportCollectDataAction.class.getName();
	private static Log m_log = LogFactory.getLog( ExportCollectDataAction.class );

	/** ビュー */
	private IWorkbenchPart viewPart;
	private IWorkbenchWindow window;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.window = null;
		this.viewPart = null;
	}

	/**
	 * 選択されている性能値をエクスポートします。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( window == null || !isEnabled() ){
			return null;
		}
		
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		
		String id = this.getClass().getName();
		m_log.debug("execute id=" + id);
		
		//FIXME CollectGraphViewで選ばれている情報をとってくる
		//暫定的に最後に「適用」ボタンを押した際の情報を持ってくるようになっている
		//(つまりグラフを一度表示させないとCSVがDLできない状態)
		
		//(マネージャ名＋#＋ファシリティID)、ファシリティ名のマップ
		TreeMap<String, CollectFacilityDataInfo> m_managerFacilityDataInfoMap = CollectGraphUtil.getManagerFacilityDataInfoMap();
		
		//サマリタイプ
		Integer m_summaryType = CollectGraphUtil.getSummaryType();
		
		// itemanmeとmonitorid
		List<CollectKeyInfoResponseP1> collectKeyInfoList = CollectGraphUtil.getCollectKeyInfoList();

		// マネージャ名とmonitorIdとcollectIdのリスト
		TreeMap<String, Map<String, List<Integer>>> m_targetItemCodeCollectMap = CollectGraphUtil.getManagerMonitorCollectIdMap();
		if (m_targetItemCodeCollectMap != null && m_targetItemCodeCollectMap.size() > 1) {
			m_log.debug("Download CSV multi manager not supported. size = " + m_targetItemCodeCollectMap.size());
			MessageDialog.openError(
					null,
					Messages.getString("message"), // 複数マネージャでダウンロード不可
					Messages.getString("message.collection.graph.do.not.download.csvfile.in.multiplemanager"));
			return null;
		}
		
		//マネージャ名、(ファシリティID、collectIDのリスト)のマップ(ItemCode混合)
		TreeMap<String, TreeMap<String, List<Integer>>> m_targetManagerFacilityCollectMap = CollectGraphUtil.getTargetManagerFacilityCollectMap();
		
		// マネージャ名とファシリティIdのMapを作成
		TreeMap<String, List<String>> managerFacilityIdMap = new TreeMap<String, List<String>>();
		for(Map.Entry<String, TreeMap<String, List<Integer>>> managerEntry : m_targetManagerFacilityCollectMap.entrySet()){
			List<String> facilityIdList = new ArrayList<String>(managerEntry.getValue().keySet());
			managerFacilityIdMap.put(managerEntry.getKey() , facilityIdList); // key=managername
		}
		
		// エクスポート時はマネージャ情報とファシリティ情報と収集地項目情報のみ渡す
		// それ以外(collectIDなど)は、マネージャ側で取り直す
		if (!m_managerFacilityDataInfoMap.isEmpty() || !m_targetItemCodeCollectMap.isEmpty() 
				|| !m_targetManagerFacilityCollectMap.isEmpty() || m_summaryType == null || !collectKeyInfoList.isEmpty()) {
			ExportDialog exportDialog = new ExportDialog(
					this.viewPart.getSite().getShell(), m_managerFacilityDataInfoMap, m_summaryType, 
					collectKeyInfoList, managerFacilityIdMap);
			exportDialog.open();
		} else {
			String msg = Messages.getString("performance.not.selected");
			MessageDialog.openError(null, Messages.getString("error"), msg);
		}
		return null;
	}
}