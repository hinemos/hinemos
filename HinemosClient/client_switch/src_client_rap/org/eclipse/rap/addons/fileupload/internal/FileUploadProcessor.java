/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.fileupload.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadReceiver;


final class FileUploadProcessor {

  private final FileUploadHandler handler;
  private final FileUploadTracker tracker;

  FileUploadProcessor( FileUploadHandler handler ) {
    this.handler = handler;
    tracker = new FileUploadTracker( handler );
  }

  void handleFileUpload( HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    try {
      ServletFileUpload upload = createUpload();
      FileItemIterator iter = upload.getItemIterator( request );
      while( iter.hasNext() ) {
        FileItemStream item = iter.next();
        if( !item.isFormField() ) {
          receive( item );
        }
      }
      if( tracker.isEmpty() ) {
        String errorMessage = "No file upload data found in request";
        tracker.setException( new Exception( errorMessage ) );
        tracker.handleFailed();
        response.sendError( HttpServletResponse.SC_BAD_REQUEST, errorMessage );
      } else {
        tracker.handleFinished();
      }
    } catch( Exception exception ) {
      Throwable cause = exception.getCause();
      if( cause instanceof FileSizeLimitExceededException ) {
        exception = ( Exception )cause;
      }
      tracker.setException( exception );
      tracker.handleFailed();
      int errorCode = exception instanceof FileSizeLimitExceededException
                    ? HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE
                    : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      response.sendError( errorCode, exception.getMessage() );
    }
  }

  private ServletFileUpload createUpload() {
    ServletFileUpload upload = new ServletFileUpload();
    upload.setFileSizeMax( handler.getMaxFileSize() );
    upload.setProgressListener( createProgressListener() );
    return upload;
  }

  private ProgressListener createProgressListener() {
    ProgressListener result = new ProgressListener() {
      long prevTotalBytesRead = -1;
      public void update( long totalBytesRead, long contentLength, int item ) {
        // Depending on the servlet engine and other environmental factors,
        // this listener may be notified for every network packet, so don't notify unless there
        // is an actual increase.
        if ( totalBytesRead > prevTotalBytesRead ) {
          prevTotalBytesRead = totalBytesRead;
          tracker.setContentLength( contentLength );
          tracker.setBytesRead( totalBytesRead );
          tracker.handleProgress();
        }
      }
    };
    return result;
  }

  private void receive( FileItemStream item ) throws IOException {
    InputStream stream = item.openStream();
    try {
      String fileName = stripFileName( item.getName() );
      String contentType = item.getContentType();
      FileDetails details = new FileDetailsImpl( fileName, contentType, -1 );
      FileUploadReceiver receiver = handler.getReceiver();
      receiver.receive( stream, details );
      tracker.addFile( details );
    } finally {
      stream.close();
    }
  }

  private static String stripFileName( String name ) {
    String result = name;
    int lastSlash = result.lastIndexOf( '/' );
    if( lastSlash != -1 ) {
      result = result.substring( lastSlash + 1 );
    } else {
      int lastBackslash = result.lastIndexOf( '\\' );
      if( lastBackslash != -1 ) {
        result = result.substring( lastBackslash + 1 );
      }
    }
    return result;
  }

}
