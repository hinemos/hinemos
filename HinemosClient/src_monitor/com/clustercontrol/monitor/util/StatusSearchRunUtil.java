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
import org.openapitools.client.model.GetStatusListRequest;
import org.openapitools.client.model.StatusFilterBaseRequest;
import org.openapitools.client.model.StatusInfoResponse;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.MultiManagerRunUtil;
import com.clustercontrol.util.UIManager;

/**
 * ステータス情報取得の実行管理を行う Session Bean クラス<BR>
 *
 */
public class StatusSearchRunUtil extends MultiManagerRunUtil{
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog(StatusSearchRunUtil.class);
	/** 検索成功可否フラグ */
	private boolean m_searchSuccess = true;

	@SuppressWarnings("unchecked")
	public Map<String, ArrayList<ArrayList<Object>>> searchInfo(List<String> managerList, StatusFilterBaseRequest filter) {

		Map<String, ArrayList<ArrayList<Object>>> dispDataMap= new ConcurrentHashMap<>();
		Map<String, String> errMsgs = new ConcurrentHashMap<>();
		long start = System.currentTimeMillis();

		try {
			String threadName = Thread.currentThread().getName() + "-StatusSearch";
			List<StatusSearchTask> searchList = new ArrayList<StatusSearchTask>();
			for (String managerName : managerList) {
				StatusSearchTask task = null;
				task = new StatusSearchTask(threadName, managerName, filter, ContextProvider.getContext());
				searchList.add(task);
			}

			List<Future<Map<String, List<?>>>> list = getExecutorService().invokeAll(searchList);

			for (Future<Map<String, List<?>>> future : list) {
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
						dispDataMap.put(managerName, infoList);
					}
					if (ret.get(POS_ERROR) != null && ret.get(POS_ERROR) instanceof String) {
						String errList = (String)ret.get(POS_ERROR);
						errMsgs.put(managerName, errList);
					}
				}
			}
		} catch (InterruptedException e) {
			m_log.error(e.getMessage() + e.getClass().getName());
		} catch (ExecutionException e) {
			m_log.error(e.getMessage() + e.getClass().getName());
		}

		//メッセージ表示
		if( 0 < errMsgs.size() && ClientSession.isDialogFree()) {
			ClientSession.occupyDialog();
			m_searchSuccess = false;
			UIManager.showMessageBox(errMsgs, true);
			ClientSession.freeDialog();
		}

		long end = System.currentTimeMillis();
		m_log.debug("time=" + (end - start));
		return dispDataMap;
	}

	/**
	 * 検索成功可否を返します。
	 * @return 更新成功可否
	 */
	public boolean isSearchSuccess() {
		return this.m_searchSuccess;
	}

	public static class StatusSearchTask implements Callable<Map<String, List<?>>>{
		private static final Log m_log2 = LogFactory.getLog(StatusSearchTask.class);

		private String threadName = null;
		private String managerName = null;
		private StatusFilterBaseRequest filter = null;
		private ServiceContext context = null;

		public StatusSearchTask(String threadName, String managerName, StatusFilterBaseRequest filter, ServiceContext context) {
			this.threadName = threadName;
			this.managerName = managerName;
			this.filter = filter;
			this.context = context;
		}

		@Override
		public Map<String, List<?>> call() throws Exception {
			Map<String, List<?>> dispDataMap= new ConcurrentHashMap<>();
			String errMsgs = null;
			ArrayList<ArrayList<Object>> infoList = new ArrayList<ArrayList<Object>>();
			
			Thread.currentThread().setName(threadName);
			ContextProvider.releaseContextHolder();
			ContextProvider.setContext(context);

			try {
				MonitorResultRestClientWrapper wrapper = MonitorResultRestClientWrapper.getWrapper(this.managerName);

				GetStatusListRequest getStatusListRequest = new GetStatusListRequest();
				getStatusListRequest.setFilter(filter);
				List<StatusInfoResponse> records = wrapper.getStatusList(getStatusListRequest);

				infoList = ConvertListUtil.statusInfoDataListToArrayList(this.managerName, records);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				errMsgs = Messages.getString("message.accesscontrol.16");
			} catch (HinemosUnknown e) {
				errMsgs = Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage());
			} catch (Exception e) {
				m_log2.warn("call: " + e.getMessage(), e);
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

