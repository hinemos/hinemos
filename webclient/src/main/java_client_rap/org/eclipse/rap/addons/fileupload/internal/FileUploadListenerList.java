/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadListener;


public final class FileUploadListenerList {

  private final Set<FileUploadListener> listeners;

  public FileUploadListenerList() {
    listeners = new HashSet<FileUploadListener>();
  }

  public void addUploadListener( FileUploadListener listener ) {
    listeners.add( listener );
  }

  public void removeUploadListener( FileUploadListener listener ) {
    listeners.remove( listener );
  }

  public void notifyUploadProgress( FileUploadEvent event ) {
    Iterator<FileUploadListener> iterator = listeners.iterator();
    while( iterator.hasNext() ) {
      FileUploadListener listener = iterator.next();
      listener.uploadProgress( event );
    }
  }

  public void notifyUploadFinished( FileUploadEvent event ) {
    Iterator<FileUploadListener> iterator = listeners.iterator();
    while( iterator.hasNext() ) {
      FileUploadListener listener = iterator.next();
      listener.uploadFinished( event );
    }
  }

  public void notifyUploadFailed( FileUploadEvent event ) {
    Iterator<FileUploadListener> iterator = listeners.iterator();
    while( iterator.hasNext() ) {
      FileUploadListener listener = iterator.next();
      listener.uploadFailed( event );
    }
  }

}
