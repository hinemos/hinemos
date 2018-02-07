/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload;

import java.io.IOException;
import java.io.InputStream;


/**
 * Instances of this interface are responsible for reading and processing the data from a file
 * upload.
 */
public abstract class FileUploadReceiver {

  /**
   * Reads and processes all data from the provided input stream.
   *
   * @param stream the stream to read from
   * @param details the details of the uploaded file like file name, content-type and size
   * @throws IOException if an input / output error occurs
   */
  public abstract void receive( InputStream stream, FileDetails details ) throws IOException;

}
