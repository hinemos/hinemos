package com.clustercontrol.ws.util;

import java.util.ArrayList;

import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

public class ArrayListInfo {
	private MonitorInfo monitorInfo = null;
	private ArrayList<CollectData> list1 = new ArrayList<>();
	private ArrayList<Integer> list2 = new ArrayList<>();
	private ArrayList<EventDataInfo> list3 = new ArrayList<>();
	
	public ArrayListInfo() {}

	public ArrayList<CollectData> getList() {
		return list1;
	}
	public void setList(ArrayList<CollectData> list1) {
		this.list1 = list1;
	}

	public int size() {
		return list1.size();
	}
	
	public ArrayList<Integer> getList2() {
		return list2;
	}

	public void setList2(ArrayList<Integer> list2) {
		this.list2 = list2;
	}

	public ArrayList<EventDataInfo> getList3() {
		return list3;
	}

	public void setList3(ArrayList<EventDataInfo> list3) {
		this.list3 = list3;
	}

	public MonitorInfo getMonitorInfo() {
		return monitorInfo;
	}
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}
}
