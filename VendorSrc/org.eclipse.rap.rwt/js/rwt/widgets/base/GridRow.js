/*******************************************************************************
 * Copyright (c) 2010, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

/**
 * Represents a visible TreeItem.
 */

(function( $ ) {

var cellRenderer = rwt.widgets.util.CellRendererRegistry.getInstance().getAll();

rwt.qx.Class.define( "rwt.widgets.base.GridRow", {

  extend : rwt.qx.Target,

  construct : function() {
    this.base( arguments );
    this.$el = $( "<div>" ).css( {
      "overflow" : "hidden",
      "userSelect" : "none",
      "height" : 16,
      "position" : "relative"
    });
    this.$el.prop( "row", this );
    this._styleMap = {};
    this._appearance = null;
    this._overlayStyleMap = {};
    this._elementStyleCache = {};
    this._variant = null;
    this.$expandIcon = null;
    this.$checkBox = null;
    this.$overlay = null;
    this.$treeColumnParts = [];
    this._lastAttributes = {};
    this.$cellLabels = [];
    this.$cellImages = [];
    this.$cellCheckBoxes = [];
    this.$cellBackgrounds = [];
    this.$indentIcons = [];
    this._usedIdentIcons = 0;
    this._cellsRendered = 0;
    this._templateRenderer = null;
  },

  destruct : function() {
    this.$el.removeProp( "row" ).detach();
    this.$el = null;
    this.$expandIcon = null;
    this.$checkBox = null;
    this.$treeColumnParts = null;
    this.$cellLabels = null;
    this.$cellImages = null;
    this.$cellCheckBoxes = null;
    this.$cellBackgrounds = null;
    this.$indentIcons = null;
  },

  members : {

    renderItem : function( item, config, selected, hoverTarget, scrolling ) {
      this._usedIdentIcons = 0;
      if( item !== null ) {
        var renderSelected = this._renderAsSelected( config, selected );
        this._renderStates( item, config, renderSelected, hoverTarget );
        this._renderBackground( item, config, renderSelected );
        // TODO [tb] : item foreground and font could be inherited
        this._renderItemForeground( item, config );
        this._renderItemFont( item, config );
        if( config.treeColumn !== -1 ) {
          this._renderIndention( item, config, hoverTarget );
        }
        if( config.rowTemplate ) {
          this.renderHeight( item, config );
          this._renderTemplate( item, config );
        } else {
          this._renderColumnModel( item, config, hoverTarget, renderSelected, scrolling );
          this.renderHeight( item, config );
        }
        this._renderOverlay( item, config );
        this._hideRemainingElements();
      } else {
        var contentOnly = scrolling || !config;
        this.$el.css( {
          "backgroundColor" : "",
          "backgroundImage" : "",
          "backgroundGradient" : ""
       } );
        if( config.rowTemplate ) {
          this._renderTemplate( null, config );
          this._hideRemainingElements();
        } else {
          this._clearContent( config );
          if( !contentOnly ) {
            this._renderAllBounds( config );
          }
        }
      }
      this._renderHtmlAttributes( item, config );
      this.dispatchSimpleEvent( "itemRendered", item );
    },

    identify : function( node ) {
      var result = [ "other" ];
      var match = function( candidate ) {
        return candidate != null && candidate.is( node );
      };
      if( match( this.$expandIcon ) ) {
        result = [ "expandIcon" ];
      } else if( match( this.$checkBox ) ) {
        result = [ "checkBox" ];
      } else if( this.$cellCheckBoxes.some( match ) ) {
        var cell = this.$cellCheckBoxes.filter( match )[ 0 ];
        result = [ "cellCheckBox", this.$cellCheckBoxes.indexOf( cell ) ];
      } else {
        while( !this.$el.is( node ) && result[ 0 ] === "other" ) { // Can be removed?
          if( this.$treeColumnParts.some( match ) ) {
            result = [ "treeColumn" ]; // TODO [tb] : now should be [ "label", 0 ] / [ "image", 0 ]
          } else if( this._templateRenderer ) {
            if( this._templateRenderer.isCellSelectable( node ) ) {
              result = [ "selectableCell", this._templateRenderer.getCellName( node ) ];
            }
          }
          node = node.parentNode;
        }
      }
      return result;
    },

    updateEvenState : function( index ) {
      this.setState( "even", index % 2 === 0 );
    },

    setAppearance : function( appearance ) {
      this._appearance = appearance;
    },

    getAppearance : function() {
      return this._appearance;
    },

    setWidth : function( width ) {
      this.$el.css( "width", width );
    },

    setHeight : function( height ) {
      this.$el.css( "height", height );
    },

    getTop : function() {
      return this.$el.get( 0 ).offsetTop;
    },

    getWidth : function() {
      // Do NOT use anything like offsetWidth/outerWidth/clientRectBounds for this, it would
      // force rendering and potentially impact performance!
      return parseInt( this.$el.css( "width" ) || "0" );
    },

    getHeight : function() {
      return parseInt( this.$el.css( "height" ) || "0" );
    },

    ////////////
    // internals

    _renderColumnModel : function( item, config, hoverTarget, renderSelected, contentOnly ) {
      this._renderCheckBox( item, config, hoverTarget, contentOnly );
      this._renderCells( item, config, renderSelected, hoverTarget, contentOnly );
    },

    _renderTemplate : function( item, config ) {
      var hasIndention =    item
                         && typeof config.treeColumn === "number"
                         && config.treeColumn > -1;
      var xOffset = hasIndention ? this._correctOffset( 0, item, config ) : 0;
      var renderer = this._getTemplateRenderer( config );
      renderer.targetBounds = [ xOffset, 0, this.getWidth() - xOffset, this.getHeight() ];
      renderer.markupEnabled = config.markupEnabled;
      renderer.targetIsEnabled = config.enabled;
      renderer.targetIsSeeable = config.seeable;
      renderer.renderItem( item );
    },

    _getTemplateRenderer : function( config ) {
      if( this._templateRenderer == null ) {
        this._templateRenderer = new rwt.widgets.util.TemplateRenderer(
          config.rowTemplate,
          this.$el.get( 0 ),
          100
        );
      }
      return this._templateRenderer;
    },

    renderHeight : function( item, config ) {
      if( config.autoHeight ) {
        var computedHeight = this._computeAutoHeight( item, config );
        if( item.getDefaultHeight() >= computedHeight - 1 ) {
          // forgive rounding error for network optimization
          computedHeight = null;
        }
        item.setHeight( computedHeight, true );
      }
      var itemHeight = item.getOwnHeight();
      if( itemHeight !== this.getHeight() ) {
        this.$el.css( "height", item.getOwnHeight() );
      }
    },

    _computeAutoHeight : function( item, config ) {
      var maxHeight = 0;
      for( var i = 0; i < this.$cellLabels.length; i++ ) {
        if( this.$cellLabels[ i ] ) {
          maxHeight = Math.max( maxHeight, Math.ceil( this.$cellLabels[ i ].outerHeight() ) );
        }
      }
      var padding = this._getCellPadding( config );
      return maxHeight + padding[ 0 ] + padding[ 2 ];
    },

    _renderStates : function( item, config, selected, hoverTarget ) {
      this.setState( "rowtemplate", config.rowTemplate != null );
      this.setState( "checked", item.isChecked() );
      this.setState( "grayed", item.isGrayed() );
      this.setState( "parent_unfocused", this._renderAsUnfocused( config ) );
      this.setState( "selected", config.fullSelection ? selected : false );
      this._renderVariant( item.getVariant() );
      this._renderOverState( hoverTarget, config );
      this._styleMap = this._getStyleMap();
      this.setState( "selected", selected );
      if( config.fullSelection ) {
        this._overlayStyleMap = this._getOverlayStyleMap( selected );
      } else {
        this._overlayStyleMap = this._getTreeColumnStyleMap( selected );
      }
    },

    _renderVariant : function( variant ) {
      if( this._variant != variant ) {
        if( this._variant != null ) {
          this.setState( this._variant, false );
        }
        this._variant = variant;
        if( this._variant != null ) {
          this.setState( this._variant, true );
        }
      }
    },

    _renderOverState : function( hoverTarget, config ) {
      var fullOverState = hoverTarget !== null && config.fullSelection;
      var singleOverState = hoverTarget != null && hoverTarget[ 0 ] === "treeColumn";
      this.setState( "over", fullOverState || singleOverState );
    },

    setState : function( state, value ) {
      if( !this.__states ) {
        this.__states = {};
      }
      if( value ) {
        this.__states[ state ] = true;
      } else {
        delete this.__states[ state ];
      }
    },

    hasState : function( state ) {
      return this.__states && this.__states[ state ] ? true : false;
    },

    _getStyleMap : function() {
      var manager = rwt.theme.AppearanceManager.getInstance();
      return manager.styleFrom( this._appearance, this.__states );
    },

    _getOverlayStyleMap : function() {
      var manager = rwt.theme.AppearanceManager.getInstance();
      return manager.styleFrom( this._appearance + "-overlay", this.__states );
    },

    _getTreeColumnStyleMap : function( selected ) {
      var manager = rwt.theme.AppearanceManager.getInstance();
      var overlayMap = manager.styleFrom( this._appearance + "-overlay", this.__states );
      if( selected ) {
        var rowMap = manager.styleFrom( this._appearance, this.__states );
        overlayMap.rowForeground = rowMap.foreground;
      } else {
        overlayMap.rowForeground = "undefined";
      }
      return overlayMap;
    },

    _renderBackground : function( item, config ) {
      var color = this._styleMap.background;
      var image = this._styleMap.backgroundImage;
      var gradient = this._styleMap.backgroundGradient;
      if( config.enabled !== false && item !== null && item.getBackground() !== null ) {
        color = item.getBackground();
        image = null;
        gradient = null;
      }
      // Note: "undefined" is a string stored in the themestore
      this.$el.css( {
        "backgroundColor" :  color !== "undefined" ? color || "" : "",
        "backgroundImage" : image !== "undefined" ? image || "" : "",
        "backgroundGradient" : gradient !== "undefined" || "" ? gradient : ""
      } );
    },

    _hasOverlayBackground : function( config ) {
      if( !config.fullSelection && config.rowTemplate ) {
        return false;
      }
      return    this._overlayStyleMap.background !== "undefined"
             || this._overlayStyleMap.backgroundImage !== null
             || this._overlayStyleMap.backgroundGradient !== null;
    },

    // TODO: broken on first render
    _renderIndention : function( item, config, hoverTarget ) {
      var expandSymbol = this._getExpandSymbol( item, config, hoverTarget );
      if( expandSymbol != null ) {
        this.$expandIcon = this._addIndentSymbol( item.getLevel(), config, expandSymbol );
      } else {
        this.$expandIcon = null;
      }
      var lineSymbol = this._getLineSymbol( item, config );
      if( lineSymbol != null ) {
        var parent = item.getParent();
        while( !parent.isRootItem() ) {
          if( parent.hasNextSibling() ) {
            this._addIndentSymbol( parent.getLevel(), config, lineSymbol );
          }
          parent = parent.getParent();
        }
      }
    },

    _getExpandSymbol : function( item, config, hoverTarget ) {
      var states = this._getParentStates( config );
      if( item.getLevel() === 0 && !item.hasPreviousSibling() ) {
        states.first = true;
      }
      if( !item.hasNextSibling() ) {
        states.last = true;
      }
      if( item.hasChildren() ) {
        if( item.isExpanded() ) {
          states.expanded = true;
        } else {
          states.collapsed = true;
        }
      }
      if( hoverTarget && hoverTarget[ 0 ] === "expandIcon" ) {
        states.over = true;
      }
      return this._getImageFromAppearance( "indent", states );
    },

    _getLineSymbol : function( item, config ) {
      var states = this._getParentStates( config );
      states.line = true;
      return this._getImageFromAppearance( "indent", states );
    },

    _getParentStates : function( config ) {
      var result = {};
      if( config.variant ) {
        result[ config.variant ] = true;
      }
      return result;
    },

    _getImageFromAppearance : function( image, states ) {
      var appearance = this._appearance + "-" + image;
      var manager = rwt.theme.AppearanceManager.getInstance();
      var styleMap = manager.styleFrom( appearance, states );
      var valid = styleMap && styleMap.backgroundImage;
      return valid ? styleMap.backgroundImage : null;
    },

    _addIndentSymbol : function( level, config, source ) {
      var result = null;
      var nextLevelOffset = ( level + 1 ) * config.indentionWidth;
      var cellWidth = config.itemWidth[ config.treeColumn ];
      if( nextLevelOffset <= cellWidth || config.rowTemplate ) {
        var offset = level * config.indentionWidth;
        var width = nextLevelOffset - offset;
        var element = this._getIndentImage();
        this._setImage( element, source, config.enabled );
        element.css( { "left" : offset, "top" : 0, "width" : width, "height" : "100%" } );
        result = element;
      }
      return result;
    },

    _renderCheckBox : function( item, config, hoverTarget, contentOnly ) {
      if( config.hasCheckBoxes ) {
        this.setState( "over", hoverTarget && hoverTarget[ 0 ] === "checkBox" );
        this.setState( "disabled", !item.isCellCheckable( 0 ) );
        var image = this._getImageFromAppearance( "check-box", this.__states );
        this.setState( "over", hoverTarget !== null );
        this.setState( "disabled", false );
        if( this.$checkBox === null ) {
          this.$checkBox = this._createElement( 3 ).css( {
            "backgroundRepeat" : "no-repeat",
            "backgroundPosition" : "center"
          } );
        }
        this.$checkBox.css( "display", image === null ? "none" : "" );
        this._setImage( this.$checkBox, image, config.enabled );
        if( config.treeColumn !== -1 || !contentOnly ) {
          var left = this._getCheckBoxLeft( item, config );
          var width = this._getCheckBoxWidth( item, config );
          this.$checkBox.css( { "left" : left, "top" : 0, "width" : width, "height" : "100%" } );
        }
      }
    },

    _renderCells : function( item, config, selected, hoverTarget, contentOnly ) {
      var columns = this._getColumnCount( config );
      if( this._cellsRendered > columns ) {
        this._removeCells( columns, this._cellsRendered );
      }
      for( var i = 0; i < columns; i++ ) {
        var isTreeColumn = this._isTreeColumn( i, config );
        if( this._getItemWidth( item, i, config ) > 0 ) {
          this._renderCellBackground( item, i, config, contentOnly );
          this._renderCellCheckBox( item, i, config, isTreeColumn, contentOnly, hoverTarget );
          var imageElement = this._renderCellImage( item, i, config, isTreeColumn, contentOnly );
          var labelElement = this._renderCellLabel( item, i, config, isTreeColumn, contentOnly );
          if( !config.fullSelection && isTreeColumn ) {
            this.$treeColumnParts = [ imageElement, labelElement ];
          }
        } else {
          this._removeCell( i );
        }
      }
      this._cellsRendered = columns;
    },

    _renderOverlay : function( item, config ) {
      if( item && this._hasOverlayBackground( config ) ) {
        this._styleOverlay( item, config );
        this._layoutOverlay( item, config );
      } else if( this.$overlay ){
        this.$overlay.css( "display", "none" );
      }
    },

    _styleOverlay : function() {
      var element = this._getOverlayElement();
      var styleMap = this._overlayStyleMap;
      var gradient = styleMap.backgroundGradient;
      if( gradient ) {
        this._renderOverlayGradient( element, gradient );
      } else {
        element.css( {
          "backgroundColor" : styleMap.background,
          "opacity" : styleMap.backgroundAlpha
      } );
      }
    },

    _renderOverlayGradient : function( element, gradient ) {
      element.css( "backgroundGradient", gradient || "" );
    },

    _layoutOverlay : function( item, config ) { // TODO: broken on first render
      if( !config.fullSelection ) {
        var element = this._getOverlayElement();
        var cell = config.treeColumn;
        var padding = config.selectionPadding;
        var left = this._getItemTextLeft( item, cell, config );
        left -= padding[ 0 ];
        var width = this._getItemTextWidth( item, cell, config );
        width += width > 0 ? padding[ 0 ] : 0;
        var visualWidth  = this._getVisualTextWidth( item, cell, config );
        visualWidth  += padding[ 0 ] + padding[ 1 ];
        width = Math.min( width, visualWidth );
        element.css( { "left" : left, "width" : width } );
      }
    },

    _renderCellBackground : function( item, cell, config, contentOnly ) {
      var background = this._getCellBackgroundColor( item, cell, config );
      var renderBounds = false;
      if( background !== "undefined" && background != this._styleMap.backgroundColor ) {
        renderBounds = !contentOnly || !this.$cellBackgrounds[ cell ];
        var element = this._getBackgroundElement( cell );
        element.css( "backgroundColor", background );
      } else if( this.$cellBackgrounds[ cell ] ) {
        this.$cellBackgrounds[ cell ].css( "backgroundColor", "" );
        renderBounds = !contentOnly;
      }
      if( renderBounds ) {
        this._renderCellBackgroundBounds( item, cell, config );
      }
    },

    _renderCellBackgroundBounds : function( item, cell, config ) {
      var element = this.$cellBackgrounds[ cell ];
      if( element ) {
        var left = this._getItemLeft( item, cell, config );
        var width = this._getItemWidth( item, cell, config );
        element.css( { "left" : left, "top" : 0, "width" : width, "height" : "100%" } );
      }
    },

    _renderCellCheckBox : function( item, cell, config, isTreeColumn, contentOnly, hoverTarget ) {
      var element = null;
      var renderBounds = false;
      if( config.itemCellCheck[ cell ] ) {
        this.setState( "checked", item.isCellChecked( cell ) );
        this.setState( "disabled", !item.isCellCheckable( cell ) );
        this.setState( "grayed", item.isCellGrayed( cell ) );
        this.setState( "over",    hoverTarget
                               && hoverTarget[ 0 ] === "cellCheckBox"
                               && hoverTarget[ 1 ] === cell );
        var image = this._getImageFromAppearance( "check-box", this.__states );
        this.setState( "disabled", false );
        renderBounds = isTreeColumn || !contentOnly || !this.$cellCheckBoxes[ cell ];
        element = this._getCellCheckImage( cell );
        element.css( "display", image === null ? "none" : "" );
        this._setImage( element, image, config.enabled );
      }
      if( renderBounds ) {
        this._renderCellCheckBounds( item, cell, config );
      }
      return element;
    },

    _renderCellCheckBounds : function( item, cell, config ) {
      var element = this.$cellCheckBoxes[ cell ];
      if( element ) {
        var left = this._getCellCheckLeft( item, cell, config );
        var width = this._getCellCheckWidth( item, cell, config );
        element.css( { "left" : left, "top" : 0, "width" : width, "height" : "100%" } );
      }
    },

    _renderCellImage : function( item, cell, config, isTreeColumn, contentOnly ) {
      var source = item.getImage( cell );
      var element = null;
      var renderBounds = false;
      if( source !== null ) {
        renderBounds = isTreeColumn || !contentOnly || !this.$cellImages[ cell ];
        element = this._getCellImage( cell );
        this._setImage( element, source[ 0 ], renderBounds ? config.enabled : null );
      } else if( this.$cellImages[ cell ] ) {
        renderBounds = isTreeColumn || !contentOnly;
        element = this._getCellImage( cell );
        this._setImage( element, null, null );
      }
      if( renderBounds ) {
        this._renderCellImageBounds( item, cell, config );
      }
      return element;
    },

    _renderCellImageBounds : function( item, cell, config ) {
      var element = this.$cellImages[ cell ];
      if( element ) {
        var left = this._getItemImageLeft( item, cell, config );
        var width = this._getItemImageWidth( item, cell, config );
        element.css( { "left" : left, "top" : 0, "width" : width, "height" : "100%" } );
      }
    },

    _renderCellLabel : function( item, cell, config, isTreeColumn, contentOnly ) {
      // NOTE [tb] : When scrolling in Firefox, it may happen that the text
      //             becomes temorarily invisible. This is a browser-bug
      //             that ONLY occurs when Firebug is installed.
      var element = null;
      var renderBounds = false;
      if( item.hasText( cell ) ) {
        renderBounds = isTreeColumn || !contentOnly || !this.$cellLabels[ cell ];
        element = this._getTextElement( cell );
        this._renderElementContent( element, item, cell, config );
        if( renderBounds ) {
          element.css( "textAlign", isTreeColumn ? "left" : this._getAlignment( cell, config ) );
        }
        this._styleLabel( element, item, cell, config );
      } else if( this.$cellLabels[ cell ] ) {
        renderBounds = isTreeColumn || !contentOnly;
        element = this._getTextElement( cell );
        this._renderElementContent( element, null, -1, config );
      }
      if( renderBounds ) {
        this._renderCellLabelBounds( item, cell, config );
      }
      return element;
    },

    _renderCellLabelBounds : function( item, cell, config ) {
      var element = this.$cellLabels[ cell ];
      if( element ) {
        var left = this._getItemTextLeft( item, cell, config );
        var width = this._getItemTextWidth( item, cell, config );
        var top = this._getCellPadding( config )[ 0 ];
        // TODO : for vertical center rendering line-height should also be set,
        //        but not otherwise. Also not sure about bottom alignment.
        element.css( { "left" : left, "top" : top, "width" : width, "height" : "auto" } );
      }
    },

    _renderElementContent : function( element, item, cell, config ) {
      var options = {
        "markupEnabled" : config.markupEnabled,
        "seeable" : config.seeable,
        "removeNewLines" : true
      };
      cellRenderer.text.renderContent( element.get( 0 ),
                                       item ? item.getText( cell ) : null,
                                       null,
                                       options );
    },

    _styleLabel : function( element, item, cell, config ) {
      this._setForeground( element, this._getCellColor( item, cell, config ) );
      this._setFont( element, item.getCellFont( cell ) );
      this._setWordWrap( element, config.wordWrap[ cell ] );
    },

    _renderItemForeground : function( item, config ) {
      this._setForeground( this.$el, this._getItemColor( item, config ) );
    },

    _renderItemFont : function( item, config ) {
      if( this._elementStyleCache.font !== config.font ) {
        this._elementStyleCache.font = config.font;
        this._setFont( this.$el, config.font );
      }
      if( this._elementStyleCache.textDecoration !== this._styleMap.textDecoration ) {
        this._elementStyleCache.textDecoration = this._styleMap.textDecoration;
        this._setTextDecoration( this.$el, this._styleMap.textDecoration );
      }
      if( this._elementStyleCache.textOverflow !== this._styleMap.textOverflow ) {
        this._elementStyleCache.textOverflow = this._styleMap.textOverflow;
        this._setTextOverflow( this.$el, this._styleMap.textOverflow );
      }
      if( this._elementStyleCache.textShadow !== this._styleMap.textShadow ) {
        this._elementStyleCache.textShadow = this._styleMap.textShadow;
        this.$el.css( "textShadow", this._styleMap.textShadow || "" );
      }
    },

    _renderHtmlAttributes : function( item, config ) {
      this.$el.removeAttr( Object.keys( this._lastAttributes ).join( " " ) );
      var attributes = item ? item.getHtmlAttributes() : {};
      if( attributes[ "id" ] && config.containerNumber === 1 ) {
        attributes = rwt.util.Objects.copy( attributes );
        attributes[ "id" ] += "-1";
      }
      this.$el.attr( attributes );
      this._lastAttributes = attributes;
    },

    _getCellBackgroundColor : function( item, cell, config ) {
      var result;
      if( config.enabled === false ) {
        result = "undefined";
      } else {
        result = item.getCellBackground( cell );
      }
      return result;
    },

    _getCellColor : function( item, cell, config ) {
      var treeColumn = this._isTreeColumn( cell, config );
      var allowOverlay = config.fullSelection || treeColumn;
      var result = allowOverlay ? this._overlayStyleMap.foreground : "undefined";
      if(    result === "undefined"
          && config.enabled !== false
          && item.getCellForeground( cell )
      ) {
        result = item.getCellForeground( cell );
      }
      if( result === "undefined" && treeColumn && !config.fullSelection ) {
        // If there is no overlay the tree column foreground may still have a different color
        // due to selection. In this case _overlayStyleMap has the tree column foreground color.
        result = this._overlayStyleMap.rowForeground;
      }
       if( result === "undefined" ) {
         result = "inherit";
      }
      return result;
    },

    _getItemColor : function( item, config ) {
      var result = "undefined";
      if( config.fullSelection ) {
        result = this._overlayStyleMap.foreground;
      }
      if( result === "undefined" ) {
        result = this._styleMap.foreground;
      }
      if( result === "undefined" ) {
        result = config.textColor;
      }
      if( result === "undefined" ) {
        result = "inherit";
      }
      return result;
    },

    _getCellFont : function( item, cell, config ) {
      var result = item.getCellFont( cell );
      if( result === null || result === "" ) {
        result = config.font;
      }
      return result;
    },

    _getVisualTextWidth : function( item, cell, config ) {
      var calc = rwt.widgets.util.FontSizeCalculation;
      var result = 0;
      if( this.$cellLabels[ cell ] ) {
        var font = this._getCellFont( item, cell, config );
        var fontProps = this._getFontProps( font );
        var text = this.$cellLabels[ cell ].html();
        var dimensions = calc.computeTextDimensions( text, fontProps );
        result = dimensions[ 0 ];
      }
      return result;
    },

    _renderAsUnfocused : function( config ) {
      return !config.focused && !this.hasState( "dnd_selected" );
    },

    _renderAsSelected : function( config, selected ) {
      return    ( selected || this.hasState( "dnd_selected" ) )
             && ( !config.hideSelection || config.focused )
             && !config.alwaysHideSelection;
    },

    _getFontProps : function( font ) {
      var result = {};
      if( font instanceof rwt.html.Font ) {
        font.renderStyle( result );
      } else {
        var fontObject = rwt.html.Font.fromString( font );
        fontObject.renderStyle( result );
        fontObject.dispose();
      }
      return result;
    },

    /////////////
    // DOM-Helper

    _setFont : function( element, font ) {
      if( font === "" || font === null ) {
        this._resetFont( element );
      } else {
        if( font instanceof rwt.html.Font ) {
          font.renderStyle( element.get( 0 ).style );
        } else {
          element.css( "font", font );
        }
      }
    },

    _resetFont : function( element ) {
      element.css( {
        "font" : "",
        "fontFamily" : "",
        "fontSize" : "",
        "fontVariant" : "",
        "fontStyle" : "",
        "fontWeight" : ""
      } );
    },

    _setTextDecoration : function( element, decoration ) {
      element.css( {
        "textDecoration" : ( decoration == null || decoration === "none" ) ? "" : decoration
      } );
    },

    _setTextOverflow : function( element, overflow ) {
      element.css( "textOverflow", ( overflow == null || overflow === "clip" ) ? "" : overflow );
    },

    _setWordWrap : function( element, wordWrap ) {
      element.css( "whiteSpace", wordWrap ? "" : "nowrap" );
    },

    _setForeground : function( element, color ) {
      element.css( "color", color || "" );
    },

    _setImage : function( element, src, enabled ) {
      var opacity = enabled ? 1 : 0.3;
      element.css( { "opacity" : enabled != null ? opacity : "", "backgroundImage" : src || "" } );
    },

    _getTextElement : function( cell ) {
      var result = this.$cellLabels[ cell ];
      if( !result ) {
        result = this._createElement( 3 );
        result.css( {
          "textDecoration" : "inherit",
          "textOverflow": "inherit",
          "backgroundColor" : ""
        } );
        this.$cellLabels[ cell ] = result;
      }
      return result;
    },

    _getCellImage : function( cell ) {
      var result = this.$cellImages[ cell ];
      if( !result ) {
        result = this._createElement( 3 );
        result.css( { "backgroundRepeat" : "no-repeat", "backgroundPosition" : "center" } );
        this.$cellImages[ cell ] = result;
      }
      return result;
    },

    _getCellCheckImage : function( cell ) {
      var result = this.$cellCheckBoxes[ cell ];
      if( !result ) {
        result = this._createElement( 3 );
        result.css( { "backgroundRepeat" : "no-repeat", "backgroundPosition" : "center" } );
        this.$cellCheckBoxes[ cell ] = result;
      }
      return result;
    },

    _getOverlayElement : function() {
      if( this.$overlay === null ) {
        this.$overlay = this._createElement( 2 );
        this.$overlay.css( { "width" : "100%", "height" : "100%" } );
      }
      return this.$overlay.css( { "display" : "" } );
    },

    _getBackgroundElement : function( cell ) {
      var result = this.$cellBackgrounds[ cell ];
      if( !result ) {
        result = this._createElement( 1 );
        this.$cellBackgrounds[ cell ] = result;
      }
      return result;
    },

    _getIndentImage : function() {
      var result;
      if( this._usedIdentIcons < this.$indentIcons.length ) {
        result = this.$indentIcons[ this._usedIdentIcons ];
      } else {
        result = this._createElement( 3 ).css( {
          "backgroundRepeat" : "no-repeat",
          "backgroundPosition" : "center",
          "zIndex" : 3
        } );
        this.$indentIcons.push( result );
      }
      this._usedIdentIcons++;
      return result.html( "" ).css( { "backgroundColor" : "", "display" : "" } );
    },

    _createElement : function( zIndex ) {
      return $( "<div>" ).css( {
        "position" : "absolute",
        "overflow" : "hidden",
        "zIndex" : zIndex
      } ).appendTo( this.$el );
    },

    _clearContent : function( config ) {
      for( var i = 0; i < this.$cellBackgrounds.length; i++ ) {
        if( this.$cellBackgrounds[ i ] ) {
          this.$cellBackgrounds[ i ].css( "backgroundColor", "" );
        }
      }
      for( var i = 0; i < this.$cellCheckBoxes.length; i++ ) {
        if( this.$cellCheckBoxes[ i ] ) {
          this.$cellCheckBoxes[ i ].css( "backgroundImage", "" );
        }
      }
      for( var i = 0; i < this.$cellImages.length; i++ ) {
        if( this.$cellImages[ i ] ) {
          this.$cellImages[ i ].css( "backgroundImage", "" );
        }
      }
      for( var i = 0; i < this.$cellLabels.length; i++ ) {
        if( this.$cellLabels[ i ] ) {
          this._renderElementContent( this.$cellLabels[ i ], null, -1, config );
        }
      }
      if( this.$checkBox ) {
        this.$checkBox.css( "backgroundImage", "" );
      }
      this._renderOverlay( null );
      this._hideRemainingElements();
    },

    _renderAllBounds : function( config ) {
      var columns = this._getColumnCount( config );
      for( var i = 0; i < columns; i++ ) {
        // tree column bounds can not be rendered without item, is rendered always anyway
        if( !this._isTreeColumn( i, config ) ) {
          this._renderCellLabelBounds( null, i, config );
          this._renderCellImageBounds( null, i, config );
        }
        this._renderCellBackgroundBounds( null, i, config );
      }
    },

    _hideRemainingElements : function() {
      for( var i = this._usedIdentIcons; i < this.$indentIcons.length; i++ ) {
        this.$indentIcons[ i ].css( "display", "none" );
      }
    },

    _removeCells : function( from, to ) {
      for( var i = from; i < to; i++ ) {
        this._removeCell( i );
      }
    },

    _removeCell : function( cell ) {
      this._removeNode( this.$cellBackgrounds, cell );
      this._removeNode( this.$cellImages, cell );
      this._removeNode( this.$cellCheckBoxes, cell );
      this._removeNode( this.$cellLabels, cell );
    },

    _removeNode : function( arr, pos ) {
      var node = arr[ pos ];
      if( node ) {
        node.detach();
        arr[ pos ] = null;
      }
    },

    ////////////////
    // layout-helper

    _getCheckBoxLeft : function( item, config ) {
      return this._correctOffset( config.checkBoxLeft, item, config );
    },

    _getCheckBoxWidth : function( item, config ) {
      var result = config.checkBoxWidth;
      var offset = this._getCheckBoxLeft( item, config );
      return this._correctWidth( result, offset, 0, config );
    },

    _getItemLeft : function( item, columnIndex, config ) {
      return config.itemLeft[ columnIndex ];
    },

    _getItemWidth : function( item, columnIndex, config ) {
      return config.itemWidth[ columnIndex ];
    },

    _getItemImageLeft : function( item, columnIndex, config ) {
      var result = config.itemImageLeft[ columnIndex ];
      if( this._isTreeColumn( columnIndex, config ) ) {
        result = this._correctOffset( result, item, config );
      }
      return result;
    },

    _getItemImageWidth : function( item, columnIndex, config ) {
      var result = config.itemImageWidth[ columnIndex ];
      if( this._isTreeColumn( columnIndex, config ) ) {
        var offset = this._getItemImageLeft( item, columnIndex, config );
        result = this._correctWidth( result, offset, columnIndex, config );
      }
      return result;
    },

    _getCellCheckLeft : function( item, columnIndex, config ) {
      var result = config.itemCellCheckLeft[ columnIndex ];
      if( this._isTreeColumn( columnIndex, config ) ) {
        result = this._correctOffset( result, item, config );
      }
      return result;
    },

    _getCellCheckWidth : function( item, columnIndex, config ) {
      var result = config.itemCellCheckWidth[ columnIndex ];
      if( this._isTreeColumn( columnIndex, config ) ) {
        var offset = this._getCellCheckLeft( item, columnIndex, config );
        result = this._correctWidth( result, offset, columnIndex, config );
      }
      return result;
    },

    _getItemTextLeft : function( item, columnIndex, config ) {
      var result = config.itemTextLeft[ columnIndex ];
      if( this._isTreeColumn( columnIndex, config ) ) {
        result = this._correctOffset( result, item, config );
      }
      return result;
    },

    _getItemTextWidth : function( item, columnIndex, config ) {
      var result = config.itemTextWidth[ columnIndex ];
      if( this._isTreeColumn( columnIndex, config ) ) {
        var offset = this._getItemTextLeft( item, columnIndex, config );
        result = this._correctWidth( result, offset, columnIndex, config );
      }
      return result;
    },

    _getCellPadding : function( config ) {
      var manager = rwt.theme.AppearanceManager.getInstance();
      return manager.styleFrom( config.baseAppearance + "-cell", {} ).padding;
    },

    _correctOffset : function( offset, item, config ) {
      return offset + this._getIndentionOffset( item.getLevel() + 1, config );
    },

    _correctWidth : function( width, offset, column, config ) {
      var result = width;
      var columnEnd = config.itemLeft[ column ] + config.itemWidth[ column ];
      var elementEnd = offset + result;
      if( elementEnd > columnEnd ) {
        result = Math.max( 0, columnEnd - offset );
      }
      return result;
    },

    _getAlignment : function( column, config ) {
      return config.alignment[ column ] ? config.alignment[ column ] : "left";
    },

    _getIndentionOffset : function( level, config ) {
      // NOTE [tb] : Shoud actually add the isTreeColumns own offset, assumes 0 now.
      return config.indentionWidth * level;
    },

    _getColumnCount : function( config ) {
      return Math.max( 1, config.columnCount );
    },

    _isTreeColumn : function( columnIndex, config ) {
      return columnIndex === config.treeColumn;
    }

  }

} );

}( rwt.util._RWTQuery ));
