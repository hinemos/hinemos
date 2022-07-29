/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload;


/**
 * Provides details of the uploaded file like file name, content-type and size
 */
public interface FileDetails {

  /**
   * The content type as transmitted by the uploading client.
   *
   * @return the content type or <code>null</code> if unknown
   */
  String getContentType();

  /**
   * The total number of bytes which are expected in total, as transmitted by the uploading client.
   * May be unknown.
   *
   * @return the content length in bytes or -1 if unknown
   */
  long getContentLength();

  /**
   * The original file name of the uploaded file, as transmitted by the client. If a path was
   * included in the request, it is stripped off.
   *
   * @return the plain file name without any path segments
   */
  String getFileName();

}
