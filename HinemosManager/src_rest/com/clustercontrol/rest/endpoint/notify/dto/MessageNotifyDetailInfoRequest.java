/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class MessageNotifyDetailInfoRequest implements RequestDto {

	private static final Pattern RULE_BASE_PATTERN = Pattern.compile("^[!-~]+$");

	@RestValidateObject(notNull = true)
	private Boolean infoValidFlg;

	@RestValidateObject(notNull = true)
	private Boolean warnValidFlg;

	@RestValidateObject(notNull = true)
	private Boolean criticalValidFlg;

	@RestValidateObject(notNull = true)
	private Boolean unknownValidFlg;

	@RestItemName(MessageConstant.MESSAGE_NOTIFY_RULE_BASE)
	@RestValidateString(maxLen = 200)
	private String infoRulebaseId;

	@RestItemName(MessageConstant.MESSAGE_NOTIFY_RULE_BASE)
	@RestValidateString(maxLen = 200)
	private String warnRulebaseId;

	@RestItemName(MessageConstant.MESSAGE_NOTIFY_RULE_BASE)
	@RestValidateString(maxLen = 200)
	private String criticalRulebaseId;

	@RestItemName(MessageConstant.MESSAGE_NOTIFY_RULE_BASE)
	@RestValidateString(maxLen = 200)
	private String unknownRulebaseId;

	public MessageNotifyDetailInfoRequest() {
	}

	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	public String getInfoRulebaseId() {
		return infoRulebaseId;
	}

	public void setInfoRulebaseId(String infoRulebaseId) {
		this.infoRulebaseId = infoRulebaseId;
	}

	public String getWarnRulebaseId() {
		return warnRulebaseId;
	}

	public void setWarnRulebaseId(String warnRulebaseId) {
		this.warnRulebaseId = warnRulebaseId;
	}

	public String getCriticalRulebaseId() {
		return criticalRulebaseId;
	}

	public void setCriticalRulebaseId(String criticalRulebaseId) {
		this.criticalRulebaseId = criticalRulebaseId;
	}

	public String getUnknownRulebaseId() {
		return unknownRulebaseId;
	}

	public void setUnknownRulebaseId(String unknownRulebaseId) {
		this.unknownRulebaseId = unknownRulebaseId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (infoValidFlg != null && infoValidFlg && (infoRulebaseId == null || infoRulebaseId.isEmpty())) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT
					.getMessage(MessageConstant.MESSAGE_NOTIFY_RULE_BASE.getMessage()));
		}

		if (warnValidFlg != null && warnValidFlg && (warnRulebaseId == null || warnRulebaseId.isEmpty())) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT
					.getMessage(MessageConstant.MESSAGE_NOTIFY_RULE_BASE.getMessage()));
		}

		if (criticalValidFlg != null && criticalValidFlg
				&& (criticalRulebaseId == null || criticalRulebaseId.isEmpty())) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT
					.getMessage(MessageConstant.MESSAGE_NOTIFY_RULE_BASE.getMessage()));
		}

		if (unknownValidFlg != null && unknownValidFlg && (unknownRulebaseId == null || unknownRulebaseId.isEmpty())) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT
					.getMessage(MessageConstant.MESSAGE_NOTIFY_RULE_BASE.getMessage()));
		}

		if (infoRulebaseId != null && !infoRulebaseId.isEmpty()) {
			Matcher infoMatcher = RULE_BASE_PATTERN.matcher(infoRulebaseId);
			if (!infoMatcher.matches()) {
				throw new InvalidSetting(MessageConstant.MESSAGE_NOTIFY_RULE_BASE_INVALID_CHARACTER.getMessage());
			}
		}

		if (warnRulebaseId != null && !warnRulebaseId.isEmpty()) {
			Matcher infoMatcher = RULE_BASE_PATTERN.matcher(warnRulebaseId);
			if (!infoMatcher.matches()) {
				throw new InvalidSetting(MessageConstant.MESSAGE_NOTIFY_RULE_BASE_INVALID_CHARACTER.getMessage());
			}
		}

		if (criticalRulebaseId != null && !criticalRulebaseId.isEmpty()) {
			Matcher infoMatcher = RULE_BASE_PATTERN.matcher(criticalRulebaseId);
			if (!infoMatcher.matches()) {
				throw new InvalidSetting(MessageConstant.MESSAGE_NOTIFY_RULE_BASE_INVALID_CHARACTER.getMessage());
			}
		}

		if (unknownRulebaseId != null && !unknownRulebaseId.isEmpty()) {
			Matcher infoMatcher = RULE_BASE_PATTERN.matcher(unknownRulebaseId);
			if (!infoMatcher.matches()) {
				throw new InvalidSetting(MessageConstant.MESSAGE_NOTIFY_RULE_BASE_INVALID_CHARACTER.getMessage());
			}
		}
	}
}
