/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class AddApprovalJobRequest extends AbstractAddJobRequest implements RequestDto {

	/** 承認依頼先ロールID */
	private String approvalReqRoleId;

	/** 承認依頼先ユーザID */
	private String approvalReqUserId;

	/** 承認依頼文 */
	private String approvalReqSentence;

	/** 承認依頼メール件名 */
	private String approvalReqMailTitle;

	/** 承認依頼メール本文 */
	private String approvalReqMailBody;

	/** 承認依頼文の利用有無フラグ */
	private Boolean isUseApprovalReqSentence;

	public AddApprovalJobRequest() {
	}

	public String getApprovalReqRoleId() {
		return approvalReqRoleId;
	}

	public void setApprovalReqRoleId(String approvalReqRoleId) {
		this.approvalReqRoleId = approvalReqRoleId;
	}

	public String getApprovalReqUserId() {
		return approvalReqUserId;
	}

	public void setApprovalReqUserId(String approvalReqUserId) {
		this.approvalReqUserId = approvalReqUserId;
	}

	public String getApprovalReqSentence() {
		return approvalReqSentence;
	}

	public void setApprovalReqSentence(String approvalReqSentence) {
		this.approvalReqSentence = approvalReqSentence;
	}

	public String getApprovalReqMailTitle() {
		return approvalReqMailTitle;
	}

	public void setApprovalReqMailTitle(String approvalReqMailTitle) {
		this.approvalReqMailTitle = approvalReqMailTitle;
	}

	public String getApprovalReqMailBody() {
		return approvalReqMailBody;
	}

	public void setApprovalReqMailBody(String approvalReqMailBody) {
		this.approvalReqMailBody = approvalReqMailBody;
	}

	public Boolean getIsUseApprovalReqSentence() {
		return isUseApprovalReqSentence;
	}

	public void setIsUseApprovalReqSentence(Boolean isUseApprovalReqSentence) {
		this.isUseApprovalReqSentence = isUseApprovalReqSentence;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
	}

}
