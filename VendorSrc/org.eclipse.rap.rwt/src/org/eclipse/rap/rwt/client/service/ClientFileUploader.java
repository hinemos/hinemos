/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client.service;

import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;

/**
 * The ClientFileUploader service allows uploading client-side files to the server.
 *
 * @since 2.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ClientFileUploader extends ClientService {

  /**
   * Starts to upload the provided <code>ClientFile</code> to the given URL using HTTP POST. If no
   * files are provided, nothing happens.
   *
   * @param url the URL to upload to, must not be <code>null</code> or empty
   * @param clientFiles client-side files, must not be <code>null</code>
   * @see ClientFileTransfer
   */
  public void submit( String url, ClientFile[] clientFiles );

}
