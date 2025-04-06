/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
namespace( "rwt.client.rcptt" );

rwt.client.rcptt.Assertion = function() {
  this._intervalID = null;
  this._active = false;
  this._running = false;
  this._widgetId = -100;
  this._requestTimer = new rwt.client.Timer( 0 );
  this._mousePosition = {
    x: 0,
    y: 0
  };
  this._requestTimer.addEventListener( "interval", this._doSendServerRequest, this );
};

rwt.client.rcptt.Assertion.getInstance = function() {
  return rwt.runtime.Singletons.get( rwt.client.rcptt.Assertion );
};

rwt.client.rcptt.Assertion.prototype = {

  setActive : function(active) {
    var old = this._active;
    this._active = active;
    if(active) {
      this._start();
    }
    else  if(old) {
      this._stop();
    }
  },

  _start : function() {
    document.onmousemove = this._handleMouseMove;
    this._updateSelection.par = this;
    this._intervalID = setInterval(this._updateSelection, 1000);
  },

  _stop: function() {
    this._widgetId = -100;
    clearInterval(this._intervalID)
    document.onmousemove = function(event){};
  },

  _updateSelection : function() {
    var _this = rwt.client.rcptt.Assertion.getInstance();
    if(!_this._active) {
      return;
    }
    var dom = document.elementFromPoint(_this._mousePosition.x, _this._mousePosition.y);
    var widget = rwt.event.EventHandlerUtil.getOriginalTargetObject(dom);

    if(_this._isAssertCanvas(widget)) {
      var shell = rwt.widgets.util.WidgetUtil.getShell(widget);
      var domShell = shell.getElement();
      var cacheZIndex = domShell.style.zIndex;
      var cacheTabIndex = domShell.tabIndex;
      domShell.tabIndex = -100000;
      domShell.style.zIndex = -10000;

      //try again search element
      dom = document.elementFromPoint(_this._mousePosition.x, _this._mousePosition.y);
      widget = rwt.event.EventHandlerUtil.getOriginalTargetObject(dom);

      domShell.tabIndex = cacheTabIndex;
      domShell.style.zIndex = cacheZIndex;
    }

    var id = rwt.remote.ObjectRegistry.getId(widget);

    if(id && id != _this._widgetId) {
      _this._widgetId = id;
      _this._sendServerRequest();
    }
  },

  _sendServerRequest : function() {
    if( this._active && !this._running ) {
      this._running = true;
      this._requestTimer.start();
    }
  },

   _doSendServerRequest : function() {
    this._requestTimer.stop();
    var request = this._createRequest();
    request.send();
  },

  _createRequest : function() {
    var connection = rwt.remote.Connection.getInstance();
    var request = new rwt.remote.Request( connection.getUrl(), "GET", "application/javascript" );
    request.setSuccessHandler( this._handleSuccess, this );
    request.setErrorHandler( this._handleError, this );
    request.setData( "servicehandler=org.eclipse.rcptt.rap.assertion&cid=" + connection.getConnectionId() +"&widgetId="+this._widgetId );
    return request;
  },
  _handleSuccess : function() {
      this._running = false;
  },


  _handleError : function( event ) {
    this._running = false;
    if( rwt.remote.Connection.getInstance()._isConnectionError( event.status ) ) {
      if( this._retryCount < 3 ) {
        var delay = 1000 * this._retryCount++;
        rwt.client.Timer.once( this._sendServerRequest, this, delay );
      } else {
        this._handleConnectionError();
      }
    } else {
      this._handleServerError( event );
    }
  },

  _handleConnectionError : function() {
    rwt.remote.Connection.getInstance().sendImmediate( true );
  },

  _handleServerError : function( event ) {
    var text = event.responseText;
    if( text && text.length > 0 ) {
      rwt.runtime.ErrorHandler.showErrorBox( "server error", true, text );
    } else {
      rwt.runtime.ErrorHandler.showErrorBox( "request failed" );
    }
  },


  _handleMouseMove :function(event) {
    var dot, eventDoc, doc, body, pageX, pageY;

    event = event || window.event; // IE-ism
    // If pageX/Y aren't available and clientX/Y are,
    // calculate pageX/Y - logic taken from jQuery.
    // (This is to support old IE)
    if (event.pageX == null && event.clientX != null) {
      eventDoc = (event.target && event.target.ownerDocument) || document;
      doc = eventDoc.documentElement;
      body = eventDoc.body;

      event.pageX = event.clientX +
        (doc && doc.scrollLeft || body && body.scrollLeft || 0) -
        (doc && doc.clientLeft || body && body.clientLeft || 0);
      event.pageY = event.clientY +
        (doc && doc.scrollTop  || body && body.scrollTop  || 0) -
        (doc && doc.clientTop  || body && body.clientTop  || 0 );
    }
    var _this = rwt.client.rcptt.Assertion.getInstance();
    _this._mousePosition = {
      x: event.pageX,
      y: event.pageY
    };
  },
  _isAssertCanvas : function(widget)
  {
    var data = rwt.remote.HandlerUtil.getServerData(widget);
    try {
      return data != null && Boolean(data["ASSERT_CANVAS"]) === true;
    } catch (e) {
      return false;
    }
  }

};