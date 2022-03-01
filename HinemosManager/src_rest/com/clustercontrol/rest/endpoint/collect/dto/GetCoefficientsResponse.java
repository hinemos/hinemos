/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

public class GetCoefficientsResponse {

	public GetCoefficientsResponse(){
	}

	private Double[] coefficients = null;

	public Double[] getCoefficients(){
		return this.coefficients;
	}

	public void setCoefficients(Double[] coefficients){
		this.coefficients = coefficients;
	}

}
