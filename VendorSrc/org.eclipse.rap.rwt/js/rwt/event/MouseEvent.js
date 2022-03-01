/*******************************************************************************
 *  Copyright: 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                        and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/


/** A mouse event instance contains all data for each occured mouse event */
rwt.qx.Class.define("rwt.event.MouseEvent",
{
  extend : rwt.event.DomEvent,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function(vType, vDomEvent, vDomTarget, vTarget, vOriginalTarget, vRelatedTarget)
  {
    this.base(arguments, vType, vDomEvent, vDomTarget, vTarget, vOriginalTarget);

    if (vRelatedTarget) {
      this.setRelatedTarget(vRelatedTarget);
    }
    this._storeButton( vType );
  },




  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics :
  {
    C_BUTTON_LEFT : "left",
    C_BUTTON_MIDDLE : "middle",
    C_BUTTON_RIGHT : "right",
    C_BUTTON_NONE : "none",

    _screenX : 0,
    _screenY : 0,
    _clientX : 0,
    _clientY : 0,
    _pageX : 0,
    _pageY : 0,
    _button : null,

    buttons : {
      left   : 0,
      right  : 2,
      middle : 1
    },

    /**
     * TODOC
     *
     * @type static
     * @param e {Event} TODOC
     * @return {void}
     */
    storeEventState : function(e)
    {
      this._screenX = e.getScreenX();
      this._screenY = e.getScreenY();
      this._clientX = e.getClientX();
      this._clientY = e.getClientY();
      this._pageX = e.getPageX();
      this._pageY = e.getPageY();
      this._button = e.getButton();
    },


    /**
     * TODOC
     *
     * @type static
     * @return {var} TODOC
     */
    getScreenX : function() {
      return this._screenX;
    },


    /**
     * TODOC
     *
     * @type static
     * @return {var} TODOC
     */
    getScreenY : function() {
      return this._screenY;
    },


    /**
     * TODOC
     *
     * @type static
     * @return {var} TODOC
     */
    getClientX : function() {
      return this._clientX;
    },


    /**
     * TODOC
     *
     * @type static
     * @return {var} TODOC
     */
    getClientY : function() {
      return this._clientY;
    },


    /**
     * TODOC
     *
     * @type static
     * @return {var} TODOC
     */
    getPageX : function() {
      return this._pageX;
    },


    /**
     * TODOC
     *
     * @type static
     * @return {var} TODOC
     */
    getPageY : function() {
      return this._pageY;
    },


    /**
     * TODOC
     *
     * @type static
     * @return {var} TODOC
     */
    getButton : function() {
      return this._button;
    }
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    button :
    {
      _fast    : true,
      readOnly : true
    },

    wheelDelta :
    {
      _fast    : true,
      readOnly : true
    }
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /*
    ---------------------------------------------------------------------------
      PAGE COORDINATES SUPPORT
    ---------------------------------------------------------------------------
    */

    getPageX : function() {
      return Math.round( this.getDomEvent().pageX );
    },

    getPageY : function() {
      return Math.round( this.getDomEvent().pageY );
    },

    /*
    ---------------------------------------------------------------------------
      CLIENT COORDINATES SUPPORT
    ---------------------------------------------------------------------------
    */

    getClientX : function() {
      return this.getDomEvent().clientX;
    },

    getClientY : function() {
      return this.getDomEvent().clientY;
    },



    /*
    ---------------------------------------------------------------------------
      SCREEN COORDINATES SUPPORT
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getScreenX : function() {
      return this.getDomEvent().screenX;
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getScreenY : function() {
      return this.getDomEvent().screenY;
    },




    /*
    ---------------------------------------------------------------------------
      BUTTON SUPPORT
    ---------------------------------------------------------------------------
    */

    isLeftButtonPressed : function() {
      return this.getButton() === rwt.event.MouseEvent.C_BUTTON_LEFT;
    },

    isMiddleButtonPressed : function() {
      return this.getButton() === rwt.event.MouseEvent.C_BUTTON_MIDDLE;
    },

    isRightButtonPressed : function() {
      return this.getButton() === rwt.event.MouseEvent.C_BUTTON_RIGHT;
    },

    __buttons : {
      0 : "left",
      2 : "right",
      1 : "middle"
    },

    /**
     * During mouse events caused by the depression or release of a mouse button,
     * this method can be used to check which mouse button changed state.
     *
     * @type member
     * @return {String} One of "left", "right", "middle" or "none"
     */
    _computeButton : function()
    {
      switch(this.getDomEvent().type)
      {
        case "click":
        case "dblclick":
          return "left";

        case "contextmenu":
          return "right";

        default:
          return this.__buttons[this.getDomEvent().button] || "none";
      }
    },

    _storeButton : function( eventType ) {
      switch( eventType ) {
        case "mousedown":
        case "mouseup":
        case "click":
        case "dblclick":
        case "contextmenu":
          rwt.remote.EventUtil._button = this.getButton();
          break;
        default:
          rwt.remote.EventUtil._button = rwt.event.MouseEvent.C_BUTTON_NONE;
      }
    },

    /*
    ---------------------------------------------------------------------------
      WHEEL SUPPORT
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     * @signature function()
     */
    _computeWheelDelta : rwt.util.Variant.select("qx.client",
    {
      "default" : function() {
        return this.getDomEvent().wheelDelta / 120;
      },

      "gecko" : function() {
        return -(this.getDomEvent().detail / 3);
      }
    })
  }
});
