/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.common.util;

import java.util.List;

import org.openapitools.client.model.AddHinemosPropertyRequest;
import org.openapitools.client.model.AddMailTemplateRequest;
import org.openapitools.client.model.AddRestAccessInfoRequest;
import org.openapitools.client.model.CommandTemplateResponse;
import org.openapitools.client.model.EventCustomCommandInfoDataResponse;
import org.openapitools.client.model.EventDisplaySettingInfoResponse;
import org.openapitools.client.model.HinemosPropertyResponse;
import org.openapitools.client.model.HinemosPropertyResponseP1;
import org.openapitools.client.model.HinemosTimeResponse;
import org.openapitools.client.model.MailTemplateInfoResponse;
import org.openapitools.client.model.ModifyHinemosPropertyRequest;
import org.openapitools.client.model.ModifyMailTemplateRequest;
import org.openapitools.client.model.ModifyRestAccessInfoRequest;
import org.openapitools.client.model.RestAccessInfoResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosPropertyDuplicate;
import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MailTemplateDuplicate;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.RestAccessDuplicate;
import com.clustercontrol.fault.RestAccessNotFound;
import com.clustercontrol.fault.RestAccessUsed;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class CommonRestClientWrapper {

	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.CommonRestEndpoints;

	public static CommonRestClientWrapper getWrapper(String managerName) {
		return new CommonRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public CommonRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public MailTemplateInfoResponse addMailTemplate(AddMailTemplateRequest addMailTemplateRequest)
			throws RestConnectFailed, HinemosUnknown, MailTemplateDuplicate, InvalidUserPass, InvalidRole,
			InvalidSetting {
		RestUrlSequentialExecuter<MailTemplateInfoResponse> proxy = new RestUrlSequentialExecuter<MailTemplateInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public MailTemplateInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				MailTemplateInfoResponse result = apiClient.commonAddMailTemplate(addMailTemplateRequest);
				return result;
			}
		};
		try {
			return (MailTemplateInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | MailTemplateDuplicate | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public MailTemplateInfoResponse modifyMailTemplate(String mailTemplateId,
			ModifyMailTemplateRequest modifyMailTemplateRequest) throws RestConnectFailed, HinemosUnknown,
			MailTemplateNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<MailTemplateInfoResponse> proxy = new RestUrlSequentialExecuter<MailTemplateInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public MailTemplateInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				MailTemplateInfoResponse result = apiClient.commonModifyMailTemplate(mailTemplateId,
						modifyMailTemplateRequest);
				return result;
			}
		};
		try {
			return (MailTemplateInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | MailTemplateNotFound | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MailTemplateInfoResponse> deleteMailTemplate(String mailTemplateIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<MailTemplateInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MailTemplateInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<MailTemplateInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MailTemplateInfoResponse> result = apiClient.commonDeleteMailTemplate(mailTemplateIds);
				return result;
			}
		};
		try {
			return (List<MailTemplateInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public MailTemplateInfoResponse getMailTemplateInfo(String mailTemplateId)
			throws RestConnectFailed, HinemosUnknown, MailTemplateNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<MailTemplateInfoResponse> proxy = new RestUrlSequentialExecuter<MailTemplateInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public MailTemplateInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				MailTemplateInfoResponse result = apiClient.commonGetMailTemplateInfo(mailTemplateId);
				return result;
			}
		};
		try {
			return (MailTemplateInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | MailTemplateNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MailTemplateInfoResponse> getMailTemplateList(String ownerRoleId)
			throws RestConnectFailed, HinemosUnknown, MailTemplateNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<MailTemplateInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MailTemplateInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<MailTemplateInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MailTemplateInfoResponse> result = apiClient.commonGetMailTemplateList(ownerRoleId);
				return result;
			}
		};
		try {
			return (List<MailTemplateInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | MailTemplateNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	// HinemosPropertyEndpoint
	public HinemosPropertyResponse addHinemosProperty(AddHinemosPropertyRequest addHinemosPropertyRequest)
			throws RestConnectFailed, HinemosUnknown, HinemosPropertyDuplicate, InvalidUserPass, InvalidRole,
			InvalidSetting {
		RestUrlSequentialExecuter<HinemosPropertyResponse> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponse executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponse result = apiClient.commonAddHinemosProperty(addHinemosPropertyRequest);
				return result;
			}
		};
		try {
			return (HinemosPropertyResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | HinemosPropertyDuplicate | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponse modifyHinemosProperty(String key,
			ModifyHinemosPropertyRequest modifyHinemosPropertyRequest) throws RestConnectFailed, HinemosUnknown,
			HinemosPropertyNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<HinemosPropertyResponse> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponse executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponse result = apiClient.commonModifyHinemosProperty(key,
						modifyHinemosPropertyRequest);
				return result;
			}
		};
		try {
			return (HinemosPropertyResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | HinemosPropertyNotFound | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<HinemosPropertyResponse> deleteHinemosProperty(String keys)
			throws RestConnectFailed, HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<HinemosPropertyResponse>> proxy = new RestUrlSequentialExecuter<List<HinemosPropertyResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<HinemosPropertyResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<HinemosPropertyResponse> result = apiClient.commonDeleteHinemosProperty(keys);
				return result;
			}
		};
		try {
			return (List<HinemosPropertyResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | HinemosPropertyNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponse getHinemosProperty(String key)
			throws RestConnectFailed, HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponse> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponse executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponse result = apiClient.commonGetHinemosProperty(key);
				return result;
			}
		};
		try {
			return (HinemosPropertyResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | HinemosPropertyNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<HinemosPropertyResponse> getHinemosPropertyList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<HinemosPropertyResponse>> proxy = new RestUrlSequentialExecuter<List<HinemosPropertyResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<HinemosPropertyResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<HinemosPropertyResponse> result = apiClient.commonGetHinemosPropertyList();
				return result;
			}
		};
		try {
			return (List<HinemosPropertyResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosTimeResponse getHinemosTime() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosTimeResponse> proxy = new RestUrlSequentialExecuter<HinemosTimeResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosTimeResponse executeMethod(DefaultApi apiClient) throws Exception {
				HinemosTimeResponse result = apiClient.commonGetHinemosTime();
				return result;
			}
		};
		try {
			return (HinemosTimeResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	// JobEndpoint
	public HinemosPropertyResponseP1 getApprovalPageLink()
			throws RestConnectFailed, HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetApprovalPageLink();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | HinemosPropertyNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponseP1 getScriptContentMaxSize()
			throws RestConnectFailed, HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetScriptContentMaxSize();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | HinemosPropertyNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	// InfraEndpoint
	public HinemosPropertyResponseP1 getInfraMaxFileSize()
			throws RestConnectFailed, HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetInfraMaxFileSize();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | HinemosPropertyNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	// JobMapEndpoint
	public HinemosPropertyResponseP1 getJobmapIconIdJobDefault()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetJobmapIconIdJobDefault();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponseP1 getJobmapIconIdJobnetDefault()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetJobmapIconIdJobnetDefault();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponseP1 getJobmapIconIdApprovalDefault()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetJobmapIconIdApprovalDefault();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponseP1 getJobmapIconIdMonitorDefault()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetJobmapIconIdMonitorDefault();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponseP1 getJobmapIconIdFileDefault()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetJobmapIconIdFileDefault();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponseP1 getJobmapIconIdFileCheckDefault()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetJobmapIconIdFileCheckDefault();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	// NodeMapEndpoint
	public HinemosPropertyResponseP1 getDownloadNodeConfigCount()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetDownloadNodeConfigCount();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	// MonitorEndpoint
	public EventDisplaySettingInfoResponse getEventDisplaySettingInfo()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<EventDisplaySettingInfoResponse> proxy = new RestUrlSequentialExecuter<EventDisplaySettingInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public EventDisplaySettingInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				EventDisplaySettingInfoResponse result = apiClient.commonGetEventDisplaySettingInfo();
				return result;
			}
		};
		try {
			return (EventDisplaySettingInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public EventCustomCommandInfoDataResponse getEventCustomCommandSettingInfo()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<EventCustomCommandInfoDataResponse> proxy = new RestUrlSequentialExecuter<EventCustomCommandInfoDataResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public EventCustomCommandInfoDataResponse executeMethod(DefaultApi apiClient) throws Exception {
				EventCustomCommandInfoDataResponse result = apiClient.commonGetEventCustomCommandSettingInfo();
				return result;
			}
		};
		try {
			return (EventCustomCommandInfoDataResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public HinemosPropertyResponseP1 getExpimpXcloudKeyprotectEnable()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HinemosPropertyResponseP1> proxy = new RestUrlSequentialExecuter<HinemosPropertyResponseP1>(
				this.connectUnit, this.restKind) {
			@Override
			public HinemosPropertyResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				HinemosPropertyResponseP1 result = apiClient.commonGetExpimpXcloudKeyprotectEnable();
				return result;
			}
		};
		try {
			return (HinemosPropertyResponseP1) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public CommandTemplateResponse getCommandTemplate(String templateId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<CommandTemplateResponse> proxy = new RestUrlSequentialExecuter<CommandTemplateResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public CommandTemplateResponse executeMethod(DefaultApi apiClient) throws Exception {
				CommandTemplateResponse result = apiClient.commonGetCommandTemplate(templateId);
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

	public List<CommandTemplateResponse> getCommandTemplateList() throws InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown {
		return getCommandTemplateList(null);
	}

	public List<CommandTemplateResponse> getCommandTemplateList(String ownerRoleId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CommandTemplateResponse>> proxy = new RestUrlSequentialExecuter<List<CommandTemplateResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<CommandTemplateResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CommandTemplateResponse> result = apiClient.commonGetCommandTemplateList(ownerRoleId);
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

	public RestAccessInfoResponse addRestAccessInfo(AddRestAccessInfoRequest addRestAccessInfoRequest)
			throws RestConnectFailed, HinemosUnknown, RestAccessDuplicate, InvalidUserPass, InvalidRole,
			InvalidSetting {
		RestUrlSequentialExecuter<RestAccessInfoResponse> proxy = new RestUrlSequentialExecuter<RestAccessInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public RestAccessInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				RestAccessInfoResponse result = apiClient.commonAddRestAccessInfo(addRestAccessInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RestAccessDuplicate | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public RestAccessInfoResponse modifyRestAccessInfo(String RestAccessInfoId,
			ModifyRestAccessInfoRequest modifyRestAccessInfoRequest) throws RestConnectFailed, HinemosUnknown,
			RestAccessNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RestAccessInfoResponse> proxy = new RestUrlSequentialExecuter<RestAccessInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public RestAccessInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				RestAccessInfoResponse result = apiClient.commonModifyRestAccessInfo(RestAccessInfoId,
						modifyRestAccessInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RestAccessNotFound | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RestAccessInfoResponse> deleteRestAccessInfo(String RestAccessInfoIds)
			throws HinemosException {
		RestUrlSequentialExecuter<List<RestAccessInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RestAccessInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<RestAccessInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<RestAccessInfoResponse> result = apiClient.commonDeleteRestAccessInfo(RestAccessInfoIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | RestAccessUsed | RestAccessNotFound def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public RestAccessInfoResponse getRestAccessInfo(String RestAccessInfoId)
			throws RestConnectFailed, HinemosUnknown, RestAccessNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RestAccessInfoResponse> proxy = new RestUrlSequentialExecuter<RestAccessInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public RestAccessInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				RestAccessInfoResponse result = apiClient.commonGetRestAccessInfo(RestAccessInfoId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RestAccessNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RestAccessInfoResponse> getRestAccessInfoList(String ownerRoleId)
			throws RestConnectFailed, HinemosUnknown, RestAccessNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RestAccessInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RestAccessInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<RestAccessInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<RestAccessInfoResponse> result = apiClient.commonGetRestAccessInfoList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RestAccessNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}
}
