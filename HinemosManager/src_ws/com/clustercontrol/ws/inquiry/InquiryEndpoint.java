/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.ws.inquiry;

import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InquiryTargetCreating;
import com.clustercontrol.fault.InquiryTargetCommandNotFound;
import com.clustercontrol.fault.InquiryTargetNotDownloadable;
import com.clustercontrol.fault.InquiryTargetNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.inquiry.bean.InquiryTarget;
import com.clustercontrol.inquiry.factory.InquiryControllerBean;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * Inquiry用のWebAPIエンドポイント
 */
@MTOM
@WebService(targetNamespace = "http://inquiry.ws.clustercontrol.com")
public class InquiryEndpoint {
	@Resource
	WebServiceContext wsctx;
	
	private static Logger m_log = Logger.getLogger( InquiryEndpoint.class );
	private static Logger m_opelog = Logger.getLogger("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 * 
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 * 
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	
	public List<InquiryTarget> getInquiryTargetList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getInquiryContentList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INQUIRY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_INQUIRY
				+ " Get InquiryContent List, Method=getInquiryContentList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new InquiryControllerBean().getInquiryTargetList();
	}
	
	public InquiryTarget getInquiryTarget(String targetId) throws InvalidUserPass, InvalidRole, HinemosUnknown, InquiryTargetNotFound {
		m_log.debug("getInquiryContentList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INQUIRY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_INQUIRY
				+ " Get InquiryContent List, Method=getInquiryContentList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new InquiryControllerBean().getInquiryTarget(targetId);
	}
	
	public void prepare(String targetName) throws InvalidUserPass, InvalidRole, HinemosUnknown, InquiryTargetNotFound, InquiryTargetCreating, InquiryTargetCommandNotFound {
		m_log.debug("download");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INQUIRY, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_INQUIRY
				+ " Prepare, Method=download, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		new InquiryControllerBean().prepare(targetName);
	}
	
	@XmlMimeType("application/octet-stream")
	public DataHandler download(String targetName) throws InvalidUserPass, InvalidRole, HinemosUnknown, InquiryTargetNotFound, InquiryTargetNotDownloadable {
		m_log.debug("download");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INQUIRY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_INQUIRY
				+ " Download, Method=download, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new InquiryControllerBean().download(targetName);
	}
}
