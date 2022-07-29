/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.AgtCalendarInfoResponse;
import org.openapitools.client.model.AgtSdmlControlSettingInfoResponse;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorInfoWrapper;

public class SdmlFileMonitorInfoWrapper extends AbstractFileMonitorInfoWrapper {

	private AgtSdmlControlSettingInfoResponse sdmlControlSettingInfo;

	public SdmlFileMonitorInfoWrapper(AgtSdmlControlSettingInfoResponse sdmlControlSettingInfo) {
		this.sdmlControlSettingInfo = sdmlControlSettingInfo;
	}

	@Override
	public String getId() {
		return sdmlControlSettingInfo.getApplicationId();
	}

	@Override
	public String getDirectory() {
		return sdmlControlSettingInfo.getControlLogDirectory();
	}

	@Override
	public String getFileName() {
		return sdmlControlSettingInfo.getControlLogFilename();
	}

	@Override
	public String getFileEncoding() {
		return SdmlFileMonitorConfig.getInstance().getControlLogFileEncoding();
	}

	@Override
	public String getFileReturnCode() {
		return SdmlFileMonitorConfig.getInstance().getControlLogReturnCode();
	}

	@Override
	public Long getRegDate() {
		return sdmlControlSettingInfo.getRegDate();
	}

	@Override
	public Long getUpdateDate() {
		return sdmlControlSettingInfo.getUpdateDate();
	}

	@Override
	public Integer getMaxStringLength() {
		return SdmlFileMonitorConfig.getInstance().getControlLogMaxReadBytes();
	}

	@Override
	public String getStartRegexString() {
		// SDML制御ログは改行区切り
		return null;
	}

	@Override
	public String getEndRegexString() {
		// SDML制御ログは改行区切り
		return null;
	}

	@Override
	public String getCalendarId() {
		// 制御ログの読み取りはカレンダ無し
		return null;
	}

	@Override
	public AgtCalendarInfoResponse getCalendar() {
		// 制御ログの読み取りはカレンダ無し
		return null;
	}

	@Override
	public Boolean getMonitorFlg() {
		return sdmlControlSettingInfo.getValidFlg();
	}

	@Override
	public Boolean getCollectorFlg() {
		return sdmlControlSettingInfo.getControlLogCollectFlg();
	}

	@Override
	public boolean isMonitorJob() {
		// SDML制御ログ読み取りは監視ジョブでは実行不可能
		return false;
	}

	public String getApplication() {
		return sdmlControlSettingInfo.getApplication();
	}
	
	public static List<SdmlFileMonitorInfoWrapper> createSdmlFileMonitorInfoList(
			List<AgtSdmlControlSettingInfoResponse> list) {
		List<SdmlFileMonitorInfoWrapper> resList = new ArrayList<>();
		for (AgtSdmlControlSettingInfoResponse sdmlControlSettingInfoResponse : list) {
			// SDML制御設定は無効時はエージェントは一切動作しない（再有効化時にファイルの先頭から読むため）
			if(sdmlControlSettingInfoResponse.getValidFlg()){
				resList.add(new SdmlFileMonitorInfoWrapper(sdmlControlSettingInfoResponse));
			}
		}
		return resList;
	}
}
