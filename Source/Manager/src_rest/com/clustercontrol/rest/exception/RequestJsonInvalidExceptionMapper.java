/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.clustercontrol.fault.RequestJsonInvalidException;

@Provider
public class RequestJsonInvalidExceptionMapper implements ExceptionMapper<RequestJsonInvalidException> {

	@Override
	public Response toResponse(RequestJsonInvalidException exception) {
		return Response.status(Status.BAD_REQUEST)
				.entity(new ExceptionBody(Status.BAD_REQUEST.getStatusCode(), exception)).build();
	}
}
