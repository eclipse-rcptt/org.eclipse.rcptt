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
rcptt = {
  define: function( name, object ) {
    var splits = name.split( "." );
    var parent = window;
    var part = splits[ 0 ];
    for( var i = 0, len = splits.length - 1; i < len; i++, part = splits[ i ] ) {
      if( !parent[ part ] ) {
        parent = parent[ part ] = {};
      } else {
        parent = parent[ part ];
      }
    }
    if( !( part in parent ) ) {
      parent[ part ] = object || {};
    }
    return part;
  }

};

var namespace = rcptt.define;