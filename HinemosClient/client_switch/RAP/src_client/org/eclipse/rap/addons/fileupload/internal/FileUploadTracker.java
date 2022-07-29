/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;


final class FileUploadTracker {

  private final FileUploadHandler handler;
  private final List<FileDetails> files;
  private long contentLength;
  private long bytesRead;
  private Exception exception;

  FileUploadTracker( FileUploadHandler handler ) {
    this.handler = handler;
    files = new ArrayList<FileDetails>();
  }

  void addFile( FileDetails details ) {
    files.add( details );
  }

  boolean isEmpty() {
    return files.isEmpty();
  }

  void setContentLength( long contentLength ) {
    this.contentLength = contentLength;
  }

  void setBytesRead( long bytesRead ) {
    this.bytesRead = bytesRead;
  }

  void setException( Exception exception ) {
    this.exception = exception;
  }

  void handleProgress() {
    new InternalFileUploadEvent( handler ).dispatchAsProgress();
  }

  void handleFinished() {
    new InternalFileUploadEvent( handler ).dispatchAsFinished();
  }

  void handleFailed() {
    new InternalFileUploadEvent( handler ).dispatchAsFailed();
  }

  private final class InternalFileUploadEvent extends FileUploadEvent {

    private static final long serialVersionUID = 1L;

    private InternalFileUploadEvent( FileUploadHandler source ) {
      super( source );
    }

    @Override
    public FileDetails[] getFileDetails() {
      return files.toArray( new FileDetails[ 0 ] );
    }

    @Override
    public long getContentLength() {
      return contentLength;
    }

    @Override
    public long getBytesRead() {
      return bytesRead;
    }

    @Override
    public Exception getException() {
      return exception;
    }

    void dispatchAsProgress() {
      super.dispatchProgress();
    }

    void dispatchAsFinished() {
      super.dispatchFinished();
    }

    void dispatchAsFailed() {
      super.dispatchFailed();
    }
  }
}
