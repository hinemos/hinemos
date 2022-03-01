/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

public class HinemosToken {
	private String tokenId;
	private String expirationDate;
	private Long validTermMinites;

	public HinemosToken() {
	}

	public HinemosToken(String tokenId, String expirationDate,Long validTermMinites) {
		super();
		this.tokenId = tokenId;
		this.expirationDate = expirationDate;
		this.validTermMinites = validTermMinites;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Long getValidTermMinites() {
		return validTermMinites;
	}

	public void setValidTermMinites(Long validTermMinites) {
		this.validTermMinites = validTermMinites;
	}

	@Override
	public String toString() {
		return "HinemosToken [tokenId=" + tokenId + ", expirationDate=" + expirationDate + ", validTermMinites="
				+ validTermMinites + "]";
	}

}
