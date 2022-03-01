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
package org.eclipse.rap.rwt.internal.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;


@SuppressWarnings( "deprecation" )
public class ProcessActionRunner {

  private static final String ATTR_RUNNABLE_LIST = ProcessActionRunner.class.getName();

  @SuppressWarnings("unchecked")
  public static void add( Runnable runnable ) {
    PhaseId phaseId = CurrentPhase.get();
    if( PhaseId.PREPARE_UI_ROOT.equals( phaseId ) || PhaseId.PROCESS_ACTION.equals( phaseId ) ) {
      runnable.run();
    } else {
      ServiceStore serviceStore = ContextProvider.getServiceStore();
      List<Runnable> list = ( List<Runnable> )serviceStore.getAttribute( ATTR_RUNNABLE_LIST );
      if( list == null ) {
        list = new ArrayList<Runnable>();
        serviceStore.setAttribute( ATTR_RUNNABLE_LIST, list );
      }
      if( !list.contains( runnable ) ) {
        list.add( runnable );
      }
    }
  }

  public static boolean executeNext() {
    boolean result = false;
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    List list = ( List )serviceStore.getAttribute( ATTR_RUNNABLE_LIST );
    if( list != null && list.size() > 0 ) {
      Runnable runnable = ( Runnable )list.remove( 0 );
      runnable.run();
      result = true;
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static void execute() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    List<Runnable> list = ( List<Runnable> )serviceStore.getAttribute( ATTR_RUNNABLE_LIST );
    if( list != null ) {
      Runnable[] runables = list.toArray( new Runnable[ list.size() ] );
      for( int i = 0; i < runables.length; i++ ) {
        // TODO: [fappel] think about exception handling.
        runables[ i ].run();
      }
    }
  }

}
