/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;


public final class FileUploadServiceHandler implements ServiceHandler {

  private static final String PARAMETER_TOKEN = "token";

  static final String SERVICE_HANDLER_ID = "org.eclipse.rap.fileupload";

  public void service( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException
  {
    // TODO [rst] Revise: does this double security make it any more secure?
    // Ignore requests to this service handler without a valid session for security reasons
    boolean hasSession = request.getSession( false ) != null;
    if( hasSession ) {
      String token = request.getParameter( PARAMETER_TOKEN );
      FileUploadHandler registeredHandler = FileUploadHandlerStore.getInstance().getHandler( token );
      if( registeredHandler == null ) {
        String message = "Invalid or missing token";
        response.sendError( HttpServletResponse.SC_FORBIDDEN, message );
      } else if( !"POST".equals( request.getMethod().toUpperCase() ) ) {
        String message = "Only POST requests allowed";
        response.sendError( HttpServletResponse.SC_METHOD_NOT_ALLOWED, message );
      } else if( !ServletFileUpload.isMultipartContent( request ) ) {
        String message = "Content must be in multipart type";
        response.sendError( HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, message );
      } else {
        FileUploadProcessor processor = new FileUploadProcessor( registeredHandler );
        processor.handleFileUpload( request, response );
      }
    }
  }

  public static String getUrl( String token ) {
    String serviceHandlerUrl = RWT.getServiceManager().getServiceHandlerUrl( SERVICE_HANDLER_ID );
    return new StringBuilder( serviceHandlerUrl )
      .append( '&' )
      .append( PARAMETER_TOKEN )
      .append( '=' )
      .append( token )
      .toString();
  }

}
