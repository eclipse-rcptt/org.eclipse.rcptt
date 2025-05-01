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
function installDetailsWorkaround() {
	var nodes = document.getElementsByTagName("details")
  	for (var i=0; i<nodes.length; i++) {
    //console.log(i, nodes[i]);
		installCollapse(nodes[i]);
	}
}

function getContent(detailsNode) {
	var children = detailsNode.children;
	for (var i=0; i<children.length; i++) {
		if (children[i].tagName == 'SUMMARY')
			continue;
		return children[i];		
	}     
	return null;
}

function hasClass( elem, name ) {
	return ( " " + elem.className + " " ).indexOf( " " + name + " " ) >= 0;
}

function addClass( elem, name ) {
	if ( !hasClass( elem, name ) ) {
		elem.className += ( elem.className ? " " : "" ) + name;
	}
}

function toggleClass( elem, name ) {
	if ( hasClass( elem, name ) ) {
		removeClass( elem, name );
	} else {
		addClass( elem, name );
	}
}

function removeClass( elem, name ) {
	var set = " " + elem.className + " ";

	// Class name may appear multiple times
	while ( set.indexOf( " " + name + " " ) >= 0 ) {
		set = set.replace( " " + name + " ", " " );
	}

	// trim for prettiness
	elem.className = typeof set.trim === "function" ? set.trim() : set.replace( /^\s+|\s+$/g, "" );
}

function toggleDetails(detailsNode) {
	toggleClass(detailsNode, "closed");
}

function installCollapse(detailsNode) {
	var children = detailsNode.children;
	for (var i=0; i<children.length; i++) {     
		var child = children[i];
		if (child.tagName != 'SUMMARY')
			continue;
		child.addEventListener('click', function() {
			toggleDetails(detailsNode);
		});
		break;
	}
}

if(navigator.userAgent.indexOf('AppleWebKit') == -1){
    addClass(document.getElementsByTagName("html")[0], "notwebkit");
}
