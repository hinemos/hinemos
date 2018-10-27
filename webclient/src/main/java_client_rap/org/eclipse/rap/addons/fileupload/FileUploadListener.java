/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload;

import org.eclipse.swt.widgets.Display;


/**
 * Listener to react on progress and completion of a file upload.
 * <p>
 * <strong>Note:</strong> This listener will be called from a different thread than the UI thread.
 * Implementations must use {@link Display#asyncExec(Runnable)} to access the UI.
 * </p>
 *
 * @see FileUploadEvent
 */
public interface FileUploadListener {

  /**
   * Called when new information about an in-progress upload is available.
   *
   * @param event event object that contains information about the uploaded file
   * @see FileUploadEvent#getBytesRead()
   */
  void uploadProgress( FileUploadEvent event );

  /**
   * Called when a file upload has finished successfully.
   *
   * @param event event object that contains information about the uploaded file
   * @see FileUploadEvent
   */
  void uploadFinished( FileUploadEvent event );

  /**
   * Called when a file upload failed.
   *
   * @param event event object that contains information about the uploaded file
   * @see FileUploadEvent#getErrorMessage()
   */
  void uploadFailed( FileUploadEvent event );

}
