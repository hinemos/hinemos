/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.MultiManagerRunUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.ScopeDataInfo;


/**
 * イベント情報取得の実行管理を行う Session Bean クラス<BR>
 *
 */
public class ScopeSearchRunUtil extends MultiManagerRunUtil{
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog(ScopeSearchRunUtil.class);

	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<Object>> searchInfo(List<String> managerList) {

		ArrayList<ArrayList<Object>> dispList = new ArrayList<ArrayList<Object>>();

		Map<String, String> errMsgs = new ConcurrentHashMap<>();
		long start = System.currentTimeMillis();

		try {
			String threadName = Thread.currentThread().getName() + "-ScopeSearch";
			List<RepositorySearchTask> searchList = new ArrayList<RepositorySearchTask>();
			for (String managerName : managerList) {
				RepositorySearchTask task = null;
				task = new RepositorySearchTask(threadName, managerName, ContextProvider.getContext());
				searchList.add(task);
			}
			
			List<Future<Map<String, List<?>>>> retList = getExecutorService().invokeAll(searchList);

			for (Future<Map<String, List<?>>> future : retList) {
				if (future == null || future.get() == null) {
					continue;
				}
				Map<String, List<?>> map = future.get();
				for(Map.Entry<String, List<?>> entry : map.entrySet()) {
					//必ず1回でループを抜ける
					String managerName = entry.getKey();
					List<?> ret = entry.getValue();
					if (ret.get(POS_INFO) != null && ret.get(POS_INFO) instanceof ArrayList) {
						ArrayList<ArrayList<Object>> infoList = (ArrayList<ArrayList<Object>>)ret.get(POS_INFO);
						for(ArrayList<Object> l : infoList) {
							dispList.add(l);
						}
					}

					if (ret.get(POS_ERROR) != null && ret.get(POS_ERROR) instanceof String) {
						errMsgs.put(managerName, (String)ret.get(POS_ERROR));
					}
				}
			}
		} catch (InterruptedException e) {
			m_log.error(e.getMessage());
		} catch (ExecutionException e) {
			m_log.error(e.getMessage());
		}

		//メッセージ表示
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}

		long end = System.currentTimeMillis();
		m_log.debug("time=" + (end - start));
		return dispList;
	}

	public class RepositorySearchTask implements Callable<Map<String, List<?>>>{

		private String threadName = null;
		private String managerName = null;
		private ServiceContext context = null;
		private Log m_log = LogFactory.getLog(RepositorySearchTask.class);

		public RepositorySearchTask(String threadName, String managerName, ServiceContext context) {
			this.threadName = threadName;
			this.managerName = managerName;
			this.context = context;
		}

		@Override
		public Map<String,List<?>> call() throws Exception {
			Map<String, List<?>> dispDataMap= new ConcurrentHashMap<>();
			String errMsgs = null;
			List<ScopeDataInfo> records = null;
			ArrayList<ArrayList<Object>> infoList = null;

			Thread.currentThread().setName(threadName);
			ContextProvider.releaseContextHolder();
			ContextProvider.setContext(context);

			try {
				MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(managerName);
				records = wrapper.getScopeList("", true, true, false);
				infoList = ConvertListUtil.scopeInfoDataListToArrayList(managerName, records);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				errMsgs = Messages.getString("message.accesscontrol.16");
			} catch (MonitorNotFound_Exception | HinemosUnknown_Exception e) {
				errMsgs = Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage());
			} catch (Exception e) {
				m_log.warn("MonitorSearchTask(), " + e.getMessage(), e);
				errMsgs = Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage());
			}
			List<Object> list = new ArrayList<Object>();
			list.add(POS_INFO, infoList);
			list.add(POS_ERROR, errMsgs);
			dispDataMap.put(this.managerName, list);

			return dispDataMap;
		}
	}
}

