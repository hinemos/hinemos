/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;

import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.MultiManagerRunUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.monitor.EventDisplaySettingInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;


/**
 * イベント表示設定情報取得の実行管理を行う Session Bean クラス<BR>
 *
 */
public class EventDisplaySettingGetUtil extends MultiManagerRunUtil{
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog(EventDisplaySettingGetUtil.class);

	public MultiManagerEventDisplaySettingInfo getEventDisplaySettingInfo(List<String> managerList) {
		MultiManagerEventDisplaySettingInfo settingDataMap = new MultiManagerEventDisplaySettingInfo();
		Map<String, String> errMsgs = new ConcurrentHashMap<>();
		long start = System.currentTimeMillis();

		try {
			String threadName = Thread.currentThread().getName() + "-EventDisplaySettingGet";
			List<EventDisplaySettingGetTask> getList = new ArrayList<EventDisplaySettingGetTask>();
			for (String managerName : managerList) {
				EventDisplaySettingGetTask task = null;
				task = new EventDisplaySettingGetTask(threadName, managerName, ContextProvider.getContext());
				getList.add(task);
			}
			
			List<Future<Map<String, List<?>>>> list = getExecutorService().invokeAll(getList);

			for (Future<Map<String, List<?>>> future : list) {
				if (future == null || future.get() == null) {
					continue;
				}
				Map<String, List<?>> map = future.get();
				for(Map.Entry<String, List<?>> entry : map.entrySet()) {
					//必ず1回でループを抜ける
					String managerName = entry.getKey();
					List<?> ret = entry.getValue();
					if (ret.get(POS_INFO) != null && ret.get(POS_INFO) instanceof EventDisplaySettingInfo) {
						EventDisplaySettingInfo displayInfo = (EventDisplaySettingInfo)ret.get(POS_INFO);
						settingDataMap.addDisplayInfo(managerName, displayInfo);
					}
					if (ret.get(POS_ERROR) != null && ret.get(POS_ERROR) instanceof String) {
						String err = (String)ret.get(POS_ERROR);
						errMsgs.put(managerName, (String)err);
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e.getMessage() + e.getClass().getName());
		}

		//メッセージ表示
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}

		long end = System.currentTimeMillis();
		m_log.debug("time=" + (end - start));
		return settingDataMap;
	}
	
	public class EventDisplaySettingGetTask implements Callable<Map<String, List<?>>> {

		private String threadName = null;
		private String managerName = null;
		private ServiceContext context = null;
		private Log m_log = LogFactory.getLog(EventDisplaySettingGetTask.class);

		public EventDisplaySettingGetTask(String threadName, String managerName, ServiceContext context) {
			this.threadName = threadName;
			this.managerName = managerName;
			this.context = context;
		}
		
		@Override
		public Map<String, List<?>> call() throws Exception {
			EventDisplaySettingInfo records = null;
			Map<String, List<?>> settingDataMap= new ConcurrentHashMap<>();
			String errMsgs = null;
			
			Thread.currentThread().setName(threadName);
			ContextProvider.releaseContextHolder();
			ContextProvider.setContext(context);
			
			try {
				MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(this.managerName);
				EventDisplaySettingInfo info = wrapper.getEventDisplaySettingInfo();
				records = info;
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				errMsgs = Messages.getString("message.accesscontrol.16");
			} catch (HinemosUnknown_Exception e) {
				errMsgs = Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage());
			} catch (Exception e) {
				m_log.warn("EventDisplaySettingGetTask(), " + e.getMessage(), e);
				errMsgs = Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage());
			}
			List<Object> list = new ArrayList<Object>();
			list.add(POS_INFO, records);
			list.add(POS_ERROR, errMsgs);
			settingDataMap.put(this.managerName, list);

			return settingDataMap;
		}
	}
}
