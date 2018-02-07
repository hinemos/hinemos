/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.WebServiceException;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.master.xml.JmxMaster;
import com.clustercontrol.utility.settings.master.xml.JmxMasterInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.JmxMasterConv;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.jmxmaster.HinemosUnknown_Exception;
import com.clustercontrol.ws.jmxmaster.InvalidRole_Exception;
import com.clustercontrol.ws.jmxmaster.InvalidSetting_Exception;
import com.clustercontrol.ws.jmxmaster.InvalidUserPass_Exception;
import com.clustercontrol.ws.jmxmaster.JmxMasterEndpoint;
import com.clustercontrol.ws.jmxmaster.JmxMasterEndpointService;

/**
 * JMXマスタ定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 */
public class JmxMasterAction extends BaseAction<com.clustercontrol.ws.monitor.JmxMasterInfo, JmxMasterInfo, JmxMaster> {

	protected JmxMasterConv conv;
	
	public JmxMasterAction() throws ConvertorException {
		super();
		conv = new JmxMasterConv();
	}

	@Override
	protected String getActionName() {return "JmxMaster";}

	@Override
	protected List<com.clustercontrol.ws.monitor.JmxMasterInfo> getList() throws Exception {
		return EndpointManager.get(ClusterControlPlugin.getDefault().getCurrentManagerName()).getEndpoint(JmxMasterEndpointService.class, JmxMasterEndpoint.class).get(0).getEndpoint().getJmxMasterInfoList();
	}

	@Override
	protected void deleteInfo(com.clustercontrol.ws.monitor.JmxMasterInfo info) throws WebServiceException, Exception {
		List<String> args = new ArrayList<>();
		args.add(info.getId());
		EndpointManager.get(ClusterControlPlugin.getDefault().getCurrentManagerName()).getEndpoint(JmxMasterEndpointService.class, JmxMasterEndpoint.class).get(0).getEndpoint().deleteJmxMasterList(args);
	}

	@Override
	protected String getKeyInfoD(com.clustercontrol.ws.monitor.JmxMasterInfo info) {
		return info.getId();
	}

	@Override
	protected JmxMaster newInstance() {
		return new JmxMaster();
	}

	@Override
	protected void addInfo(JmxMaster xmlInfo,com.clustercontrol.ws.monitor.JmxMasterInfo info) throws Exception {
		xmlInfo.addJmxMasterInfo(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(JmxMaster xmlInfo, String xmlFile) throws Exception {
		xmlInfo.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionMasterDto2Xml(Config.getVersion()));
		xmlInfo.setSchemaInfo(new com.clustercontrol.utility.settings.master.xml.SchemaInfo());
		try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
			xmlInfo.marshal(osw);
		}
	}

	@Override
	protected List<JmxMasterInfo> getElements(JmxMaster xmlInfo) {
		return Arrays.asList(xmlInfo.getJmxMasterInfo());
	}

	@Override
	protected int registElement(JmxMasterInfo element) throws Exception {
		JmxMasterEndpoint endpoint = EndpointManager.get(ClusterControlPlugin.getDefault().getCurrentManagerName()).getEndpoint(JmxMasterEndpointService.class, JmxMasterEndpoint.class).get(0).getEndpoint();
		com.clustercontrol.ws.monitor.JmxMasterInfo dto = conv.getDTO(element);
		int ret = 0;
		try {
			List<com.clustercontrol.ws.monitor.JmxMasterInfo> args = new ArrayList<>();
			args.add(dto);
			endpoint.addJmxMasterList(args);
		} catch (InvalidRole_Exception e) {
			log.error(Messages.getString("SettingTools.InvalidRole") + " : " + getKeyInfoE(element), e);
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidUserPass_Exception e) {
			log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + getKeyInfoE(element), e);
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidSetting_Exception e) {
			log.warn(Messages.getString("SettingTools.InvalidSetting") + " : " + getKeyInfoE(element), e);
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (HinemosUnknown_Exception e) {
			// 既に同じ設定が存在する場合は、本Exceptionが発生するため、warningメッセージは出力するが、戻り値は「0」で返す
			log.info(Messages.getString("SettingTools.HinemosUnknown") + " : " + getKeyInfoE(element) + " is exists.");
			log.debug(e);
		}
		return ret;
	}

	@Override
	protected String getKeyInfoE(JmxMasterInfo info) {
		return info.getMasterId();
	}

	@Override
	protected JmxMaster getXmlInfo(String filePath) throws Exception {
		return JmxMaster.unmarshal(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
	}

	@Override
	protected int checkSchemaVersion(JmxMaster xmlInfo) throws Exception {
		int res = conv.checkSchemaVersion(
				xmlInfo.getSchemaInfo().getSchemaType(),
				xmlInfo.getSchemaInfo().getSchemaVersion(),
				xmlInfo.getSchemaInfo().getSchemaRevision());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				conv.getSchemaVersion(com.clustercontrol.utility.settings.monitor.xml.SchemaInfo.class);
		
		if (!BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision())) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
		return 0;
	}

	@Override
	protected JmxMasterInfo[] getArray(JmxMaster info) {
		return info.getJmxMasterInfo();
	}

	@Override
	protected int compare(com.clustercontrol.ws.monitor.JmxMasterInfo info1, com.clustercontrol.ws.monitor.JmxMasterInfo info2) {
		return info1.getId().compareTo(info2.getId());
	}

	@Override
	protected int sortCompare(JmxMasterInfo info1, JmxMasterInfo info2) {
		return info1.getMasterId().compareTo(info2.getMasterId());
	}

	@Override
	protected void setArray(JmxMaster xmlInfo, JmxMasterInfo[] infoList) {
		xmlInfo.setJmxMasterInfo(infoList);
	}

	@Override
	protected void checkDelete(JmxMaster xmlInfo) throws Exception {
		// マスタの差分削除チェックは行わない
	}
	
	@Override
	protected void importObjectPrivilege(List<JmxMasterInfo> objectList){
		// マスタのオブジェクト権限同時インポートは行わない
	}
}
