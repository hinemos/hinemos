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
import org.openapitools.client.model.EventFilterBaseRequest;
import org.openapitools.client.model.GetEventListRequest;
import org.openapitools.client.model.GetEventListResponse;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.MultiManagerRunUtil;
import com.clustercontrol.util.UIManager;

/**
 * イベント情報取得の実行管理を行う Session Bean クラス<BR>
 *
 */
public class EventSearchRunUtil extends MultiManagerRunUtil{
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog(EventSearchRunUtil.class);
	/** 検索成功可否フラグ */
	private boolean m_searchSuccess = true;

	public Map<String, GetEventListResponse> searchInfo(List<String> managerList, EventFilterBaseRequest filter, int messages) {
		Map<String, GetEventListResponse> dispDataMap = new ConcurrentHashMap<>();
		Map<String, String> errMsgs = new ConcurrentHashMap<>();
		long start = System.currentTimeMillis();
		
		if (managerList == null) {
			return dispDataMap;
		}
		
		try {
			String threadName = Thread.currentThread().getName() + "-EventSearch";
			List<EventSearchTask> searchList = new ArrayList<EventSearchTask>();
			for (String managerName : managerList) {
				EventSearchTask task = null;
				task = new EventSearchTask(threadName, managerName, filter, messages, ContextProvider.getContext());
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
					if (ret.get(POS_INFO) != null && ret.get(POS_INFO) instanceof GetEventListResponse) {
						GetEventListResponse infoList = (GetEventListResponse)ret.get(POS_INFO);
						dispDataMap.put(managerName, infoList);
					}
					if (ret.get(POS_ERROR) != null && ret.get(POS_ERROR) instanceof String) {
						String err = (String)ret.get(POS_ERROR);
						errMsgs.put(managerName, (String)err);
					}
				}
			}
		} catch (InterruptedException e) {
			m_log.error(e.getMessage() + e.getClass().getName());
		} catch (ExecutionException e) {
			m_log.error(e.getMessage() + e.getClass().getName());
		}

		//メッセージ表示
		if( 0 < errMsgs.size() && ClientSession.isDialogFree()){
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

	public class EventSearchTask implements Callable<Map<String, List<?>>>{

		private String threadName = null;
		private String managerName = null;
		private EventFilterBaseRequest filter = null;
		private int messages = 0;
		private ServiceContext context = null;
		private Log m_log2 = LogFactory.getLog(EventSearchTask.class);

		public EventSearchTask(String threadName, String managerName, EventFilterBaseRequest filter, int messages, ServiceContext context) {
			this.threadName = threadName;
			this.managerName = managerName;
			this.filter = filter;
			this.messages = messages;
			this.context = context;
		}

		@Override
		public Map<String, List<?>> call() throws Exception {
			GetEventListResponse records = null;
			Map<String, List<?>> dispDataMap= new ConcurrentHashMap<>();
			String errMsgs = null;

			Thread.currentThread().setName(threadName);
			ContextProvider.releaseContextHolder();
			ContextProvider.setContext(context);

			try {
				MonitorResultRestClientWrapper wrapper = MonitorResultRestClientWrapper.getWrapper(this.managerName);
				GetEventListRequest getEventListRequest = new GetEventListRequest();
				getEventListRequest.setFilter(filter);
				getEventListRequest.setSize(messages);
				GetEventListResponse infoList = wrapper.getEventList(getEventListRequest);
						
				infoList = infoList == null ? setDefaultInfoList(null) : infoList;
				records = infoList;
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				errMsgs = Messages.getString("message.accesscontrol.16");
			} catch (HinemosUnknown e) {
				errMsgs = Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage());
			} catch (Exception e) {
				m_log2.warn("MonitorSearchTask(), " + e.getMessage(), e);
				errMsgs = Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage());
			}
			List<Object> list = new ArrayList<Object>();
			list.add(POS_INFO, records);
			list.add(POS_ERROR, errMsgs);
			dispDataMap.put(this.managerName, list);

			return dispDataMap;
		}

		private GetEventListResponse setDefaultInfoList(GetEventListResponse infoList) {
			infoList = new GetEventListResponse();
			infoList.setTotal(0);
			infoList.setCritical(0);
			infoList.setWarning(0);
			infoList.setInfo(0);
			infoList.setUnKnown(0);
			return infoList;
		}
	}
}
