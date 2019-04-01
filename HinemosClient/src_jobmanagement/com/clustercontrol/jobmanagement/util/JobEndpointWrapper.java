/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.jobmanagement.FacilityNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.HinemosUnknown_Exception;
import com.clustercontrol.ws.jobmanagement.IconFileNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidApprovalStatus_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidSetting_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobApprovalFilter;
import com.clustercontrol.ws.jobmanagement.JobApprovalInfo;
import com.clustercontrol.ws.jobmanagement.JobEndpoint;
import com.clustercontrol.ws.jobmanagement.JobEndpointService;
import com.clustercontrol.ws.jobmanagement.JobFileCheck;
import com.clustercontrol.ws.jobmanagement.JobForwardFile;
import com.clustercontrol.ws.jobmanagement.JobHistoryFilter;
import com.clustercontrol.ws.jobmanagement.JobHistoryList;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobInfoNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.JobInvalid_Exception;
import com.clustercontrol.ws.jobmanagement.JobKick;
import com.clustercontrol.ws.jobmanagement.JobKickDuplicate_Exception;
import com.clustercontrol.ws.jobmanagement.JobKickFilterInfo;
import com.clustercontrol.ws.jobmanagement.JobMasterNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.JobNodeDetail;
import com.clustercontrol.ws.jobmanagement.JobOperationInfo;
import com.clustercontrol.ws.jobmanagement.JobPlan;
import com.clustercontrol.ws.jobmanagement.JobPlanFilter;
import com.clustercontrol.ws.jobmanagement.JobQueueActivityViewFilter;
import com.clustercontrol.ws.jobmanagement.JobQueueActivityViewInfo;
import com.clustercontrol.ws.jobmanagement.JobQueueContentsViewInfo;
import com.clustercontrol.ws.jobmanagement.JobQueueNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.JobQueueReferrerViewInfo;
import com.clustercontrol.ws.jobmanagement.JobQueueSetting;
import com.clustercontrol.ws.jobmanagement.JobQueueSettingViewFilter;
import com.clustercontrol.ws.jobmanagement.JobQueueSettingViewInfo;
import com.clustercontrol.ws.jobmanagement.JobSchedule;
import com.clustercontrol.ws.jobmanagement.JobSessionDuplicate_Exception;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobTriggerInfo;
import com.clustercontrol.ws.jobmanagement.JobmapIconImage;
import com.clustercontrol.ws.jobmanagement.NotifyNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.OtherUserGetLock_Exception;
import com.clustercontrol.ws.jobmanagement.OutputBasicInfo;
import com.clustercontrol.ws.jobmanagement.UpdateTimeNotLatest_Exception;
import com.clustercontrol.ws.jobmanagement.UserNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class JobEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( JobEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public JobEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static JobEndpointWrapper getWrapper(String managerName) {
		return new JobEndpointWrapper(EndpointManager.getActive(managerName));
	}

	public EndpointUnit getEndpointUnit() {
		return this.endpointUnit;
	}

	public static List<EndpointSetting<JobEndpoint>> getJobEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(JobEndpointService.class, JobEndpoint.class);
	}

	public void addSchedule(JobSchedule jobSchedule)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, JobKickDuplicate_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addSchedule(jobSchedule);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addSchedule(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addFileCheck(JobFileCheck jobFileCheck)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, JobKickDuplicate_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addFileCheck(jobFileCheck);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addFileCheck(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addJobManual(JobKick jobManual)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, JobKickDuplicate_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addJobManual(jobManual);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addJobManual(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifySchedule(JobSchedule jobSchedule)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifySchedule(jobSchedule);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifySchedule(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyFileCheck(JobFileCheck jobFileCheck)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyFileCheck(jobFileCheck);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyFileCheck(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyJobManual(JobKick jobManual)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyJobManual(jobManual);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyJobManual(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteSchedule(List<String> jobkickIdList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteSchedule(jobkickIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteSchedule(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteFileCheck(List<String> jobkickIdList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteFileCheck(jobkickIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteFileCheck(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteJobManual(List<String> jobkickIdList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteJobManual(jobkickIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteJobManual(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobSchedule getJobSchedule(String jobKickId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobSchedule(jobKickId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobSchedule(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobFileCheck getJobFileCheck(String jobKickId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobFileCheck(jobKickId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobFileCheck(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobKick getJobManual(String jobKickId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobManual(jobKickId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobManual(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobKick getJobKick(String jobKickId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobKick(jobKickId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobKick(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<JobKick> getJobKickList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobKickList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobKickList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<JobKick> getJobKickListByCondition(JobKickFilterInfo condition)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobKickListByCondition(condition);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobKickListByCondition(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void setJobKickStatus(String jobkickId, boolean validFlag)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, JobInfoNotFound_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.setJobKickStatus(jobkickId, validFlag);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("setJobKickStatus(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<JobForwardFile> getForwardFileList(String sessionId, String jobunitId, String jobId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getForwardFileList(sessionId, jobunitId, jobId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getForwardFileList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobTreeItem getJobDetailList(String sessionId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				JobTreeItem item = endpoint.getJobDetailList(sessionId);
				setTreeParent(item);
				return item;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobDetailList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<Integer> getAvailableStartOperation(String sessionId, String jobunitId, String jobId, String facilityId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = (JobEndpoint) endpointSetting.getEndpoint();
				return endpoint.getAvailableStartOperation(sessionId, jobunitId, jobId, facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getAvailableStartOperation(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<Integer> getAvailableStopOperation(String sessionId, String jobunitId, String jobId, String facilityId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = (JobEndpoint) endpointSetting.getEndpoint();
				return endpoint.getAvailableStopOperation(sessionId, jobunitId, jobId, facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getAvailableStopOperation(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobTreeItem getJobTree(String ownerRoleId, boolean treeOnly)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception, NotifyNotFound_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				JobTreeItem item = endpoint.getJobTree(ownerRoleId, treeOnly);
				setTreeParent(item);
				m_log.info("getJobTree role=" + ownerRoleId);
				return item;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobTree(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	// ジョブマップからも利用します。
	public void setTreeParent(JobTreeItem item) {
		List<JobTreeItem> children = item.getChildren();
		for (JobTreeItem child : children) {
			child.setParent(item);
			setTreeParent(child);
		}
	}

	public List<JobNodeDetail> getNodeDetailList(String sessionId, String jobunitId, String jobId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNodeDetailList(sessionId, jobunitId, jobId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodeDetailList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}



	public JobTreeItem getSessionJobInfo(String sessionId, String jobunitId, String jobId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getSessionJobInfo(sessionId, jobunitId, jobId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getSessionJobInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobHistoryList getJobHistoryList(JobHistoryFilter filter, int histories)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobHistoryList(filter, histories);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobHistoryList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<JobPlan> getPlanList(JobPlanFilter filter, int plans)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getPlanList(filter, plans);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getPlanList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void operationJob(JobOperationInfo info)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = (JobEndpoint) endpointSetting.getEndpoint();
				endpoint.operationJob(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("operationJob(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public Long registerJobunit(JobTreeItem jobunit)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInvalid_Exception, JobMasterNotFound_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			JobTreeItem top = null; // TOPを保存しておく
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				removeTreeParent(jobunit);
				top = jobunit.getParent();
				jobunit.setParent(null);
				Long lastUpdateTime = endpoint.registerJobunit(jobunit);
				return lastUpdateTime;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("registerJob(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			} finally {
				jobunit.setParent(top);
				setTreeParent(jobunit);
			}
		}
		throw wse;
	}

	public void deleteJobunit(String jobunitId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInvalid_Exception, JobMasterNotFound_Exception, InvalidSetting_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteJobunit(jobunitId);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("registerJobunit(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	private static void removeTreeParent(JobTreeItem item) {
		List<JobTreeItem> children = item.getChildren();
		for (JobTreeItem child : children) {
			child.setParent(null);
			removeTreeParent(child);
		}
	}

	public void runJob(String jobunitId, String jobId, OutputBasicInfo info, JobTriggerInfo triggerInfo)
			throws FacilityNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception, JobMasterNotFound_Exception, JobSessionDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = (JobEndpoint) endpointSetting.getEndpoint();
				endpoint.runJob(jobunitId, jobId, info, triggerInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("runJob(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobInfo getJobFull(JobInfo jobInfo)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception, NotifyNotFound_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobFull(jobInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobFull(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	
	public List<JobInfo> getJobFullList(List<JobInfo> jobList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobFullList(jobList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobFull(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<Long> getUpdateTimeList(List<String> jobunitIdList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				List<Long> updateTimeList = endpoint.getUpdateTimeList(jobunitIdList);
				return updateTimeList;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobunitUpdateTime(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public Integer getEditLock(String jobunitId, Long updateTime, boolean forceFlag)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, OtherUserGetLock_Exception, UpdateTimeNotLatest_Exception, JobInvalid_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getEditLock(jobunitId, updateTime, forceFlag);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getEditLock(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void checkEditLock(String jobunitId, Integer editSession)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, OtherUserGetLock_Exception, UpdateTimeNotLatest_Exception, JobInvalid_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.checkEditLock(jobunitId, editSession);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("checkEditLock(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void releaseEditLock(Integer editSession)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.releaseEditLock(editSession);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("releaseEditLock(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	
	public List<JobInfo> getRegisteredModule(String jobunitId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getRegisteredModule(jobunitId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getRegisteredModule(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<JobmapIconImage> getJobmapIconImageList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, IconFileNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobmapIconImageList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobmapIconImageList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<String> getJobmapIconImageIdListForSelect(String ownerRoleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, IconFileNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobmapIconImageIdListForSelect(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobmapIconImageIdListForSelect(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public String getApprovalPageLink()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getApprovalPageLink();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getApprovalPageLink(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> getRoleIdListWithReadObjectPrivilege(String objectId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getRoleIdListWithReadObjectPrivilege(objectId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getRoleIdListWithReadObjectPrivilege(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> getUserIdListBelongToRoleId(String roleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getUserIdListBelongToRoleId(roleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getUserIdListBelongToRoleId(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<MonitorInfo> getMonitorListForJobMonitor(String ownerRoleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMonitorListForJobMonitor(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMonitorListForJobMonitor(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<JobApprovalInfo> getApprovalJobList(JobApprovalFilter property, int histories)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getApprovalJobList(property, histories);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getApprovalJobList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void modifyApprovalInfo(JobApprovalInfo info, Boolean isApprove)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobInfoNotFound_Exception, InvalidApprovalStatus_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyApprovalInfo(info, isApprove);
				m_log.info("modifyApprovalJob() succsess");
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyApprovalJob(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public int getScriptContentMaxSize() 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getScriptContentMaxSize();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getScriptContentMaxSize(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobQueueSettingViewInfo getJobQueueSettingViewInfo(JobQueueSettingViewFilter filter)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception,
			InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobQueueSettingViewInfo(filter);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobQueueSettingViewInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobQueueReferrerViewInfo getJobQueueReferrerViewInfo(String queueId) throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception, JobQueueNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobQueueReferrerViewInfo(queueId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobQueueReferrerViewInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobQueueActivityViewInfo getJobQueueActivityViewInfo(JobQueueActivityViewFilter filter)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception,
			InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobQueueActivityViewInfo(filter);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobQueueActivityViewInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobQueueContentsViewInfo getJobQueueContentsViewInfo(String queueId) throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception, JobQueueNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobQueueContentsViewInfo(queueId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobQueueContentsViewInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<JobQueueSetting> getJobQueueList(String roleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobQueueList(roleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobQueueList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public JobQueueSetting getJobQueue(String queueId) throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, JobQueueNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobQueue(queueId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobQueue(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addJobQueue(JobQueueSetting setting) throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addJobQueue(setting);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addJobQueue(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyJobQueue(JobQueueSetting setting) throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidSetting_Exception, InvalidUserPass_Exception, JobQueueNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyJobQueue(setting);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyJobQueue(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteJobQueue(String queueId) throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidSetting_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobEndpoint> endpointSetting : getJobEndpoint(endpointUnit)) {
			try {
				JobEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteJobQueue(queueId);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteJobQueue(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
