/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload;

import org.eclipse.rap.addons.fileupload.internal.FileUploadHandlerStore;
import org.eclipse.rap.addons.fileupload.internal.FileUploadListenerList;
import org.eclipse.rap.addons.fileupload.internal.FileUploadServiceHandler;


/**
 * A file upload handler is used to accept file uploads from a client. After creating a file upload
 * handler, the server will accept file uploads to the URL returned by <code>getUploadUrl()</code>.
 * Upload listeners can be attached to react on progress. When the upload has finished, a
 * FileUploadHandler has to be disposed of by calling its <code>dispose()</code> method.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileUploadHandler {

  private final String token;
  private final FileUploadReceiver receiver;
  private final FileUploadListenerList listeners;
  private long maxFileSize = -1;

  /**
   * Constructs a file upload handler that is associated with the given receiver. The receiver is
   * responsible for reading and processing the uploaded data.
   *
   * @param receiver the receiver that should process the uploaded data, must not be
   *          <code>null</code>
   */
  public FileUploadHandler( FileUploadReceiver receiver ) {
    if( receiver == null ) {
      throw new NullPointerException( "receiver is null" );
    }
    this.receiver = receiver;
    token = FileUploadHandlerStore.createToken();
    listeners = new FileUploadListenerList();
    FileUploadHandlerStore.getInstance().registerHandler( token, this );
  }

  /**
   * Returns the upload URL to which a file can be uploaded.
   *
   * @return the encoded upload URL
   */
  public String getUploadUrl() {
    return FileUploadServiceHandler.getUrl( token );
  }

  /**
   * Returns the file upload receiver that is associated with this file upload handler.
   *
   * @return the associated receiver
   */
  public FileUploadReceiver getReceiver() {
    return receiver;
  }

  /**
   * Adds a the given file upload listener to the collection of listeners who will be notified when
   * a file upload proceeds.
   *
   * @param listener the file upload listener to add, must not be <code>null</code>
   * @see #removeUploadListener
   */
  public void addUploadListener( FileUploadListener listener ) {
    if( listener == null ) {
      throw new NullPointerException( "listener is null" );
    }
    listeners.addUploadListener( listener );
  }

  /**
   * Removes the given file upload listener from the collection of listeners who will be notified
   * when a file upload proceeds.
   *
   * @param listener the file upload listener to remove, must not be <code>null</code>
   * @see #addUploadListener
   */
  public void removeUploadListener( FileUploadListener listener ) {
    if( listener == null ) {
      throw new NullPointerException( "listener is null" );
    }
    listeners.removeUploadListener( listener );
  }

  /**
   * Closes and de-registers the upload handler. After calling this method, no subsequent upload
   * requests for this handler will be accepted anymore. Clients <em>must</em> call this method
   * before discarding the instance of the handler to allow it to be garbage collected.
   */
  public void dispose() {
    FileUploadHandlerStore.getInstance().deregisterHandler( token );
  }

  /**
   * Returns the maximum file size in bytes allowed to be uploaded for this handler. The default
   * value of -1, indicates no limit.
   * @see #setMaxFileSize
   */
  public long getMaxFileSize() {
    return maxFileSize;
  }

  /**
   * Sets the maximum file size in bytes allowed to be uploaded for this handler. A value of -1
   * indicates no limit.
   * @see #getMaxFileSize
   */
  public void setMaxFileSize( long maxFileSize ) {
    this.maxFileSize = maxFileSize;
  }

  FileUploadListenerList getListeners() {
    return listeners;
  }

  String getToken() {
    return token;
  }

}
