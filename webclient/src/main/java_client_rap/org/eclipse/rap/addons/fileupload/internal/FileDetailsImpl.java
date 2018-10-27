/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload.internal;

import org.eclipse.rap.addons.fileupload.FileDetails;


public final class FileDetailsImpl implements FileDetails {

  private final String fileName;
  private final String contentType;
  private final long contentLength;

  public FileDetailsImpl( String fileName, String contentType, long contentLength ) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.contentLength = contentLength;
  }

  public String getFileName() {
    return fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public long getContentLength() {
    return contentLength;
  }

}
