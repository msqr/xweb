// Copyright (c) 2009 Matt Magoffin (spamsqr@msqr.us)
// 
// ===================================================================
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of
// the License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
// 02111-1307 USA
// ===================================================================
// $Id: matte-locale.js,v 1.8 2007/06/25 10:35:32 matt Exp $
// ===================================================================

/**
 * XwebLocaleClass
 * 
 * This is a very simple class for using a locale-specific message bundle
 * within JavaScript. The general idea is for the application to instantiate
 * one instance of this class as a global variable and then initialize
 * the message bundle by calling either initXmsg() or initJson(). For example,
 * using jQuery, you could do:
 * 
 * var XwebLocale = new XwebLocaleClass();
 * var XwebLocale = new XwebLocaleClass();
 * $(document).ready(function() {
 * 	$.getJSON('messages.json',
 * 		function(data){
 * 			XwebLocale.initJson(data);
 * 		});
 * });
 * 
 * The messages.json URL would generally be mapped to an instance of the
 * magoffin.matt.xweb.util.XwebServiceView view, configured to use the
 * magoffin/matt/xweb/support/locale-messages.xsl XSLT to transform the
 * application's message bundle into an appropriate JSON object, e.g.
 * 
 * {
 *   "my.key.1" : "This is my 1st message.",
 *   "my.key.2" : "This is my 2nd message."
 * }
 */
var XwebLocaleClass = function() {};
XwebLocaleClass.prototype = {
	bundle: {},

	/**
	 * Initialize with initial messages.
	 */
	init: function(messages) {
		if ( messages ) this.bundle = messages;
	},
	
	/**
	 * Initialize from DOM nodes of type <msg key='x'>value</msg>.
	 * This method will append to the existing messages.
	 */
	initXmsg: function(xMsgNodes) {
		if ( !(xMsgNodes && xMsgNodes.length) ) return;
		for ( var i = 0; i < xMsgNodes.length; i++ ) {
			if ( !xMsgNodes[i].hasChildNodes() ) continue;
			var key = xMsgNodes[i].getAttribute('key');
			xMsgNodes[i].normalize();
			var value = xMsgNodes[i].firstChild.nodeValue;
			this.bundle[key] = value;
		}
	},
	
	/**
	 * Initialize from JSON object. The object key/value pairs are turned
	 * into the message bundle data. This method will append to the 
	 * existing messages.
	 */
	initJson: function(msgData) {
		if ( !msgData ) return;
		for ( var key in msgData ) {
			this.bundle[key] = msgData[key];
		}
	},
  
	/**
	 * Get a message based on a key, with optional parameters. Parameters
	 * should be in the form {x} where x is a number starting at 0. Occurrances
	 * of these parameters in the message bundle will be replaced by corresponding
	 * array values from the passed in params array.
	 */
	i18n : function(key, params) {
		var msg = this.bundle[key];
		if ( !msg ) {
			msg = '??'+key+'??';
		} else if ( params ) {
			var i = 0;
			for ( i = 0; i < params.length; i++ ) {
				msg = msg.replace(new RegExp('\\{'+(i)+'\\}','g'),params[i]);
			}
		}
		return msg;
	 }
};
