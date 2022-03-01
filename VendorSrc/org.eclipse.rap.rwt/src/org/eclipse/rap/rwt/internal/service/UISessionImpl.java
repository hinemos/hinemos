/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.service;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.client.service.ClientInfo;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.client.ClientMessages;
import org.eclipse.rap.rwt.internal.client.ClientSelector;
import org.eclipse.rap.rwt.internal.lifecycle.ContextUtil;
import org.eclipse.rap.rwt.internal.lifecycle.ISessionShutdownAdapter;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.internal.util.SerializableLock;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.service.ApplicationContextEvent;
import org.eclipse.rap.rwt.service.ApplicationContextListener;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;
import org.eclipse.swt.internal.SerializableCompatibility;


public class UISessionImpl
  implements UISession, ApplicationContextListener, HttpSessionBindingListener,
             SerializableCompatibility
{

  private static final String ATTR_UI_SESSION = UISessionImpl.class.getName() + "#uisession:";
  private static final String ATTR_LOCALE = UISessionImpl.class.getName() + "#locale";

  private final SerializableLock requestLock;
  private final SerializableLock lock;
  private final Map<String, Object> attributes;
  private final Set<UISessionListener> listeners;
  private final String id;
  private final String connectionId;
  private Connection connection;
  private boolean bound;
  private boolean inDestroy;
  private transient HttpSession httpSession;
  private transient ISessionShutdownAdapter shutdownAdapter;
  private transient ApplicationContextImpl applicationContext;

  public UISessionImpl( ApplicationContextImpl applicationContext, HttpSession httpSession ) {
    this( applicationContext, httpSession, null );
  }

  public UISessionImpl( ApplicationContextImpl applicationContext,
                        HttpSession httpSession,
                        String connectionId )
  {
    setApplicationContext( applicationContext );
    this.httpSession = httpSession;
    this.connectionId = connectionId;
    requestLock = new SerializableLock();
    lock = new SerializableLock();
    attributes = new HashMap<String, Object>();
    listeners = new HashSet<UISessionListener>();
    id = Integer.toHexString( hashCode() );
    bound = true;
    connection = new ConnectionImpl( this );
  }

  public static UISessionImpl getInstanceFromSession( HttpSession httpSession, String connectionId )
  {
    return ( UISessionImpl )httpSession.getAttribute( getUISessionAttributeName( connectionId ) );
  }

  public void attachToHttpSession() {
    httpSession.setAttribute( getUISessionAttributeName( connectionId ), this );
  }

  public void setApplicationContext( ApplicationContextImpl applicationContext ) {
    if( this.applicationContext != null ) {
      this.applicationContext.removeApplicationContextListener( this );
    }
    this.applicationContext = applicationContext;
    if( this.applicationContext != null ) {
      this.applicationContext.addApplicationContextListener( this );
    }
  }

  public ApplicationContextImpl getApplicationContext() {
    return applicationContext;
  }

  public void setShutdownAdapter( ISessionShutdownAdapter adapter ) {
    shutdownAdapter = adapter;
    if( shutdownAdapter != null ) {
      shutdownAdapter.setUISession( this );
      shutdownAdapter.setShutdownCallback( new Runnable() {
        public void run() {
          destroy();
        }
      } );
    }
  }

  public ISessionShutdownAdapter getShutdownAdapter() {
    return shutdownAdapter;
  }

  public void beforeDestroy( ApplicationContextEvent event ) {
    shutdown();
  }

  public void shutdown() {
    // Removing the object from the httpSession will trigger the valueUnbound method,
    // which actually kills the session
    getHttpSession().removeAttribute( getUISessionAttributeName( connectionId ) );
  }

  public Object getAttribute( String name ) {
    ParamCheck.notNull( name, "name" );
    Object result = null;
    synchronized( lock ) {
      result = attributes.get( name );
    }
    return result;
  }

  public boolean setAttribute( String name, Object value ) {
    ParamCheck.notNull( name, "name" );
    boolean result = false;
    synchronized( lock ) {
      if( bound ) {
        result = true;
        attributes.put( name, value );
      }
    }
    return result;
  }

  public boolean removeAttribute( String name ) {
    ParamCheck.notNull( name, "name" );
    boolean result = false;
    synchronized( lock ) {
      if( bound ) {
        result = true;
        attributes.remove( name );
      }
    }
    return result;
  }

  public Enumeration<String> getAttributeNames() {
    return createAttributeNameEnumeration();
  }

  public String getId() {
    return id;
  }

  public HttpSession getHttpSession() {
    synchronized( lock ) {
      return httpSession;
    }
  }

  public void setHttpSession( HttpSession httpSession ) {
    ParamCheck.notNull( httpSession, "httpSession" );
    synchronized( lock ) {
      this.httpSession = httpSession;
    }
  }

  public boolean isBound() {
    synchronized( lock ) {
      return bound;
    }
  }

  public Client getClient() {
    ClientSelector clientSelector = applicationContext.getClientSelector();
    return clientSelector.getSelectedClient( this );
  }

  public Connection getConnection() {
    return connection;
  }

  public Locale getLocale() {
    Locale locale = ( Locale )getAttribute( ATTR_LOCALE );
    if( locale == null ) {
      ClientInfo clientInfo = getClient().getService( ClientInfo.class );
      if( clientInfo != null ) {
        locale = clientInfo.getLocale();
      }
      if( locale == null ) {
        locale = Locale.getDefault();
      }
    }
    return locale;
  }

  public void setLocale( Locale locale ) {
    Locale oldLocale = getLocale();
    setAttribute( ATTR_LOCALE, locale );
    Locale newLocale = getLocale();
    if( !newLocale.equals( oldLocale ) ) {
      ClientMessages messages = getClient().getService( ClientMessages.class );
      if( messages != null ) {
        messages.update( newLocale );
      }
    }
  }

  public void exec( Runnable runnable ) {
    ParamCheck.notNull( runnable, "runnable" );
    ContextUtil.runNonUIThreadWithFakeContext( this, runnable );
  }

  public boolean addUISessionListener( UISessionListener listener ) {
    ParamCheck.notNull( listener, "listener" );
    boolean result = false;
    synchronized( lock ) {
      if( bound && !inDestroy ) {
        result = true;
        listeners.add( listener );
      }
    }
    return result;
  }

  public boolean removeUISessionListener( UISessionListener listener ) {
    ParamCheck.notNull( listener, "listener" );
    boolean result = false;
    synchronized( lock ) {
      if( bound && !inDestroy ) {
        result = true;
        listeners.remove( listener );
      }
    }
    return result;
  }

  public void valueBound( HttpSessionBindingEvent event ) {
    synchronized( lock ) {
      bound = true;
      inDestroy = false;
    }
  }

  public void valueUnbound( HttpSessionBindingEvent event ) {
    if( shutdownAdapter != null ) {
      shutdownAdapter.interceptShutdown();
    } else {
      boolean fakeContext = false;
      if( !ContextProvider.hasContext() ) {
        fakeContext = true;
        ServiceContext context = ContextUtil.createFakeContext( this );
        ContextProvider.setContext( context );
      }
      try {
        destroy();
      } finally {
        if( fakeContext ) {
          ContextProvider.releaseContextHolder();
        }
      }
    }
  }

  public String getConnectionId() {
    return connectionId;
  }

  /*
   * test hook
   */
  public void setConnection( Connection connection ) {
    this.connection = connection;
  }

  Object getRequestLock() {
    return requestLock;
  }

  private static String getUISessionAttributeName( String connectionId ) {
    return ATTR_UI_SESSION + ( connectionId == null ? "" : connectionId );
  }

  private void destroy() {
    synchronized( lock ) {
      inDestroy = true;
    }
    fireBeforeDestroy();
    synchronized( lock ) {
      setApplicationContext( null );
      attributes.clear();
      listeners.clear();
      bound = false;
      inDestroy = false;
    }
  }

  private void fireBeforeDestroy() {
    UISessionListener[] listenersCopy;
    synchronized( lock ) {
      int size = listeners.size();
      listenersCopy = listeners.toArray( new UISessionListener[ size ] );
    }
    UISessionEvent event = new UISessionEvent( this );
    for( UISessionListener listener : listenersCopy ) {
      try {
        listener.beforeDestroy( event );
      } catch( RuntimeException exception ) {
        handleBeforeDestroyException( listener, exception );
      }
    }
  }

  private void handleBeforeDestroyException( UISessionListener listener,
                                             RuntimeException exception )
  {
    String txt = "Could not execute {0}.beforeDestroy(UISessionEvent).";
    Object[] param = new Object[] { listener.getClass().getName() };
    String msg = MessageFormat.format( txt, param );
    httpSession.getServletContext().log( msg, exception );
  }

  private Enumeration<String> createAttributeNameEnumeration() {
    Set<String> names;
    synchronized( lock ) {
      names = new HashSet<String>( attributes.keySet() );
    }
    final Iterator<String> iterator = names.iterator();
    return new Enumeration<String>() {
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }
      public String nextElement() {
        return iterator.next();
      }
    };
  }

}
