/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

/**
 * @appearance tab-view
 */
rwt.qx.Class.define("rwt.widgets.TabFolder",
{
  extend : rwt.widgets.base.BoxLayout,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function() {
    this.base( arguments );
    this.addEventListener( "changeFocused", rwt.widgets.util.TabUtil.onTabFolderChangeFocused );
    this.addEventListener( "keypress", rwt.widgets.util.TabUtil.onTabFolderKeyPress );
    this._bar = new rwt.widgets.base.TabFolderBar();
    this._pane = new rwt.widgets.base.TabFolderPane();
    this.add( this._bar, this._pane );
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    appearance :
    {
      refine : true,
      init : "tab-view"
    },

    orientation :
    {
      refine : true,
      init : "vertical"
    },

    alignTabsToLeft :
    {
      check : "Boolean",
      init : true,
      apply : "_applyAlignTabsToLeft"
    },

    placeBarOnTop :
    {
      check : "Boolean",
      init : true,
      apply : "_applyPlaceBarOnTop"
    }
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {

    /**
     * TODOC
     *
     * @type member
     * @return {AbstractPane} TODOC
     */
    getPane : function() {
      return this._pane;
    },

    /**
     * TODOC
     *
     * @type member
     * @return {AbstractBar} TODOC
     */
    getBar : function() {
      return this._bar;
    },

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyAlignTabsToLeft : function(value)
    {
      var vBar = this._bar;

      vBar.setHorizontalChildrenAlign(value ? "left" : "right");

      // force re-apply of states for all tabs
      vBar._addChildrenToStateQueue();
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyPlaceBarOnTop : function(value)
    {
      // This does not work if we use flexible zones
      // this.setReverseChildrenOrder(!value);
      var vBar = this._bar;

      // move bar around
      if (value) {
        vBar.moveSelfToBegin();
      } else {
        vBar.moveSelfToEnd();
      }

      // force re-apply of states for all tabs
      vBar._addChildrenToStateQueue();
    }
  },

  destruct : function() {
    this._disposeObjects( "_bar", "_pane" );
  }

});
