/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

import java.io.File;
import java.util.List;

import org.openapitools.client.model.AddJobmapIconImageRequest;
import org.openapitools.client.model.CheckPublishResponse;
import org.openapitools.client.model.JobmapIconIdDefaultListResponse;
import org.openapitools.client.model.JobmapIconImageInfoResponse;
import org.openapitools.client.model.ModifyJobmapIconImageRequest;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileDuplicate;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class JobMapRestClientWrapper implements ICheckPublishRestClientWrapper {
	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.JobMapRestEndpoints;

	public static JobMapRestClientWrapper getWrapper(String managerName) {
		return new JobMapRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public JobMapRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	@Override
	public CheckPublishResponse checkPublish()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<CheckPublishResponse> proxy = new RestUrlSequentialExecuter<CheckPublishResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public CheckPublishResponse executeMethod(DefaultApi apiClient) throws Exception {
				CheckPublishResponse result = apiClient.jobmapCheckPublish();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed def) {//通信異常
			throw new RestConnectFailed(Messages.getString("message.hinemos.failure.transfer") + ", " + HinemosMessage.replace(def.getMessage()), def);
		} catch (InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (UrlNotFound e) {
			// UrlNotFoundが返された場合エンドポイントがPublishされていないためメッセージを設定する
			throw new HinemosUnknown(Messages.getString("message.expiration.term"), e);
		} catch (Exception unknown) {
			throw new HinemosUnknown(Messages.getString("message.hinemos.failure.unexpected") + HinemosMessage.replace(unknown.getMessage()), unknown);
		}
	}
	public JobmapIconImageInfoResponse addJobmapIconImage(File file,
			AddJobmapIconImageRequest addJobmapIconImageRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileDuplicate {
		RestUrlSequentialExecuter<JobmapIconImageInfoResponse> proxy = new RestUrlSequentialExecuter<JobmapIconImageInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public JobmapIconImageInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				AddJobmapIconImageRequest req = new com.clustercontrol.jobmap.util.UtilAddJobmapIconImageRequest();
				req.setIconId(addJobmapIconImageRequest.getIconId());
				req.setDescription(addJobmapIconImageRequest.getDescription());
				req.setOwnerRoleId(addJobmapIconImageRequest.getOwnerRoleId());
				JobmapIconImageInfoResponse result = apiClient.jobmapAddJobmapIconImage(file, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting
				| IconFileDuplicate def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public JobmapIconImageInfoResponse modifyJobmapIconImage(String iconId, File file,
			ModifyJobmapIconImageRequest modifyJobmapIconImageRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileNotFound {
		RestUrlSequentialExecuter<JobmapIconImageInfoResponse> proxy = new RestUrlSequentialExecuter<JobmapIconImageInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public JobmapIconImageInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ModifyJobmapIconImageRequest req = new com.clustercontrol.jobmap.util.UtilModifyJobmapIconImageRequest();
				req.setDescription(modifyJobmapIconImageRequest.getDescription());
				JobmapIconImageInfoResponse result = apiClient.jobmapModifyJobmapIconImage(iconId, file, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting
				| IconFileNotFound def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobmapIconImageInfoResponse> deleteJobmapIconImage(String iconIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileNotFound {
		RestUrlSequentialExecuter<List<JobmapIconImageInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JobmapIconImageInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<JobmapIconImageInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JobmapIconImageInfoResponse> result = apiClient.jobmapDeleteJobmapIconImage(iconIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting
				| IconFileNotFound def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public JobmapIconImageInfoResponse getJobmapIconImage(String iconId)
			throws RestConnectFailed, IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<JobmapIconImageInfoResponse> proxy = new RestUrlSequentialExecuter<JobmapIconImageInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public JobmapIconImageInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				JobmapIconImageInfoResponse result = apiClient.jobmapGetJobmapIconImage(iconId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | IconFileNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadJobmapIconImageFile(String iconId)
			throws RestConnectFailed, IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit, this.restKind) {
			@Override
			public File executeMethod(DefaultApi apiClient) throws Exception {
				File result = apiClient.jobmapDownloadJobmapIconImageFile(iconId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | IconFileNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<JobmapIconImageInfoResponse> getJobmapIconImageList() throws RestConnectFailed, IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<JobmapIconImageInfoResponse>> proxy = 
				new RestUrlSequentialExecuter<List<JobmapIconImageInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<JobmapIconImageInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JobmapIconImageInfoResponse> result = apiClient.jobmapGetJobmapIconImageList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | IconFileNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobmapIconIdDefaultListResponse> getJobmapIconIdDefaultList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<JobmapIconIdDefaultListResponse>> proxy = new RestUrlSequentialExecuter<List<JobmapIconIdDefaultListResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<JobmapIconIdDefaultListResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JobmapIconIdDefaultListResponse> result = apiClient.jobmapGetJobmapIconIdDefaultList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}
}
