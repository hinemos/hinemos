package com.clustercontrol.notify.mail.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.mailtemplate.HinemosUnknown_Exception;
import com.clustercontrol.ws.mailtemplate.InvalidRole_Exception;
import com.clustercontrol.ws.mailtemplate.InvalidSetting_Exception;
import com.clustercontrol.ws.mailtemplate.InvalidUserPass_Exception;
import com.clustercontrol.ws.mailtemplate.MailTemplateDuplicate_Exception;
import com.clustercontrol.ws.mailtemplate.MailTemplateEndpoint;
import com.clustercontrol.ws.mailtemplate.MailTemplateEndpointService;
import com.clustercontrol.ws.mailtemplate.MailTemplateInfo;
import com.clustercontrol.ws.mailtemplate.MailTemplateNotFound_Exception;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class MailTemplateEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( MailTemplateEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public MailTemplateEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static MailTemplateEndpointWrapper getWrapper(String managerName) {
		return new MailTemplateEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<MailTemplateEndpoint>> getMailTemplateEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(MailTemplateEndpointService.class, MailTemplateEndpoint.class);
	}

	public boolean addMailTemplate(MailTemplateInfo mailTemplateInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MailTemplateDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MailTemplateEndpoint> endpointSetting : getMailTemplateEndpoint(endpointUnit)) {
			try {
				MailTemplateEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.addMailTemplate(mailTemplateInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addMailTemplate(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean modifyMailTemplate(MailTemplateInfo mailTemplateInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MailTemplateNotFound_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MailTemplateEndpoint> endpointSetting : getMailTemplateEndpoint(endpointUnit)) {
			try {
				MailTemplateEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.modifyMailTemplate(mailTemplateInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyMailTemplate(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean deleteMailTemplate(String mailTemplateId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MailTemplateEndpoint> endpointSetting : getMailTemplateEndpoint(endpointUnit)) {
			try {
				MailTemplateEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.deleteMailTemplate(mailTemplateId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteMailTemplate(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public MailTemplateInfo getMailTemplateInfo(String mailTemplateId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MailTemplateNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MailTemplateEndpoint> endpointSetting : getMailTemplateEndpoint(endpointUnit)) {
			try {
				MailTemplateEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMailTemplateInfo(mailTemplateId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMailTemplateInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MailTemplateInfo> getMailTemplateList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MailTemplateNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MailTemplateEndpoint> endpointSetting : getMailTemplateEndpoint(endpointUnit)) {
			try {
				MailTemplateEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMailTemplateList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMailTemplateList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}


	public List<MailTemplateInfo> getMailTemplateListByOwnerRole(String ownerRoleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MailTemplateNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MailTemplateEndpoint> endpointSetting : getMailTemplateEndpoint(endpointUnit)) {
			try {
				MailTemplateEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMailTemplateListByOwnerRole(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMailTemplateListByOwnerRole(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

}
