//<JasobNoObfs>
//---------------------------------------------------------------------------
//jWebSocket JavaScript/Browser Client (Community Edition, CE)
//---------------------------------------------------------------------------
//Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//Alexander Schulze, Germany (NRW)
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//---------------------------------------------------------------------------
//</JasobNoObfs>
 
// ## :#file:*:jWebSocket.js
// ## :#d:en:Implements the jWebSocket Web Client.
 
// Firefox temporarily used MozWebSocket (why??), anyway, consider this here.
// Since the browserSupportNativeWebSocket method evaluates the existance of
// the window.WebSocket class, this abstraction needs to be done on the very top.
//http://www.oschina.net/p/jwebsocket/
//http://jwebsocket.org/

if( window.MozWebSocket ) {
	window.WebSocket = window.MozWebSocket;
}
var ws = {

	//OPEN
	WS_OPEN: 1,
	
	//CLOSING
	WS_CLOSING: 2,
	
	//CLOSED
	WS_CLOSED: 3,
	
	MSG_WS_NOT_SUPPORTED:
		"Unfortunately your browser does neither natively support WebSockets\n" +
		"nor you have the Adobe Flash-PlugIn 10+ installed.\n" +
		"Please download the last recent Adobe Flash Player at http://get.adobe.com/flashplayer.",

	WS_SERVER_SCHEMA: "ws",
	//:const:*:WS_SERVER_HOST:String:[hostname|localhost|IP-Number]
	//:d:en:Default hostname of current website or [tt]localhost|127.0.0.1[/tt] if no hostname can be detected.
	WS_SERVER_HOST: ( self.location.hostname ? self.location.hostname : "127.0.0.1" ),
	//:const:*:WS_SERVER_PORT:Integer:8787
	//:d:en:Default port number, 8787 for stand-alone un-secured servers, _
	//:d:en:80 for Jetty or Glassfish un-secured servers.
	WS_SERVER_PORT: 8787,
	//:const:*:WS_SERVER_CONTEXT:String:webSocket
	//:d:en:Default application context in web application servers or servlet containers like Jetty or GlassFish.
	WS_SERVER_CONTEXT: "/webSocket",
	//:const:*:WS_SERVER_URL:String:ws://[hostname]:8787/websocket
	//:d:en:Current token id, incremented per token exchange to assign results.
	//:@deprecated:en:Use [tt]getDefaultServerURL()[/tt] instead.
	WS_SERVER_URL:
		"ws://" + ( self.location.hostname ? self.location.hostname : "127.0.0.1" ) + ":8787/webSocket",
	
	//:i:en:Browsertype Constants
	//:const:*:BT_UNKNOWN:Integer:0
	//:d:en:Browsertype is unknown.
	BT_UNKNOWN		:  0,
	//:const:*:BT_FIREFOX:Integer::
	//:d:en:Browser is "Firefox".
	BT_FIREFOX		:  1,
	//:const:*:BT_NETSCAPE:Integer:2
	//:d:en:Browser is "Netscape".
	BT_NETSCAPE		:  2,
	//:const:*:BT_OPERA:Integer:3
	//:d:en:Browser is "Opera".
	BT_OPERA		:  3,
	//:const:*:BT_IEXPLORER:Integer:4
	//:d:en:Browser is "Internet Explorer".
	BT_IEXPLORER	:  4,
	//:const:*:BT_SAFARI:Integer:5
	//:d:en:Browser is "Safari".
	BT_SAFARI		:  5,
	//:const:*:BT_CHROME:Integer:6
	//:d:en:Browser is "Chrome".
	BT_CHROME		: 6,
	
	//:const:*:BROWSER_NAMES
	//:d:en:Array of browser names. Each BT_xxx constant can be used as an index to this array.
	BROWSER_NAMES : [
		"Unknown",
		"Firefox",
		"Netscape",
		"Opera",
		"Internet Explorer",
		"Safari",
		"Chrome"
	],
	
	//:m:*:$
	//:d:en:Convenience replacement for [tt]document.getElementById()[/tt]. _
	//:d:en:Returns the first HTML element with the given id or [tt]null[/tt] _
	//:d:en:if the element could not be found.
	//:a:en::aId:String:id of the HTML element to be returned.
	//:r:*:::void:none
	$: function( aId ) {
		return document.getElementById( aId );
	},

	//:m:*:getServerURL
	//:d:en:Returns the URL to the webSocket based on schema, host, port, _
	//:d:en:context and servlet.
	//:a:en::::none
	//:r:*:::String:webSocket server URL consisting of schema://host:port/context
	getServerURL: function( aSchema, aHost, aPort, aContext) {
		var lURL =
			aSchema + "://"
			+ aHost 
			+ ( aPort ? ":" + aPort : "" );
		if( aContext && aContext.length > 0 ) {
			lURL += aContext;
		}
		return lURL;
	},

	//:m:*:browserSupportsWebSockets
	//:d:en:checks if the browser or one of its plug-ins like flash or chrome _
	//:d:en:do support web sockets to be used by an application.
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the browser or one of its plug-ins support websockets, otherwise [tt]false[/tt].
	browserSupportsWebSockets: function() {
		return( 
			window.WebSocket !== null && window.WebSocket !== undefined
		);
	},

	//:m:*:browserSupportsNativeWebSockets
	//:d:en:checks if the browser natively supports web sockets, no plug-ins
	//:d:en:are considered. Caution! This is a public field not a function!
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the browser natively support websockets, otherwise [tt]false[/tt].
	browserSupportsNativeWebSockets: (function() {
		return(
			window.WebSocket !== null && window.WebSocket !== undefined
		);
	})(),

	//:m:*:browserSupportsJSON
	//:d:en:checks if the browser natively or by JSON lib does support JSON.
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the browser or one of its plug-ins support JSON, otherwise [tt]false[/tt].
	browserSupportsJSON: function() {
		return(
			window.JSON !== null && window.JSON !== undefined
		);
	},

	//:m:*:browserSupportsNativeJSON
	//:d:en:checks if the browser natively supports JSON, no plug-ins
	//:d:en:are considered. Caution! This is a public field not a function!
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the browser natively support websockets, otherwise [tt]false[/tt].
	browserSupportsNativeJSON: (function() {
		return(
			window.JSON !== null && window.JSON !== undefined
		);
	})(),

	//:m:*:browserSupportsWebWorkers
	//:d:en:checks if the browser natively supports HTML5 WebWorkers
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the browser natively support WebWorkers, otherwise [tt]false[/tt].
	browserSupportsWebWorkers: (function() {
		return(
			window.Worker !== null && window.Worker !== undefined
		);
	})(),

	//:m:*:isIE
	//:d:en:checks if the browser is Internet Explorer. _
	//:d:en:This is needed to switch to IE specific event model.
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the browser is IE, otherwise [tt]false[/tt].
	isIE: (function() {
		var lUserAgent = navigator.userAgent;
		var lIsIE = lUserAgent.indexOf( "MSIE" );
		return( lIsIE >= 0 );
	})(),

	//:i:de:Bei Erweiterung der Browsertypen auch BROWSER_NAMES entsprechend anpassen!

	//:m:*:getBrowserName
	//:d:en:Returns the name of the browser.
	//:a:en::::none
	//:r:en::browserName:String:Name of the used browser.
	getBrowserName: function() {
		return this.fBrowserName;
	},

	//:m:*:getBrowserVersion
	//:d:en:Returns the browser version als float value.
	//:a:en::::none
	//:r:en::browserVersion:Float:Version number of the browser.
	getBrowserVersion: function() {
		return this.fBrowserVerNo;
	},

	//:m:*:getBrowserVersionString
	//:d:en:Returns the browser version as string value.
	//:a:en::::none
	//:r:en:::String:Version string of the browser.
	getBrowserVersionString: function() {
		return this.fBrowserVerStr;
	},

	//:m:*:isFirefox
	//:d:en:Determines, if the used browser is a "Firefox".
	//:a:en::::none
	//:r:en::isFirefox:Boolean:[tt]true[/tt], if Browser is Firefox, otherwise [tt]false[/tt].
	isFirefox: function() {
		return this.fIsFirefox;
	},

	//:m:*:isOpera
	//:d:en:Determines, if the used browser is a "Opera".
	//:a:en::::none
	//:r:en::isOpera:Boolean:[tt]true[/tt], if Browser is Opera, otherwise [tt]false[/tt].
	isOpera: function() {
		return this.fIsOpera;
	},

	//:m:*:isChrome
	//:d:en:Determines, if the used browser is a "Chrome".
	//:a:en::::none
	//:r:en::isOpera:Boolean:[tt]true[/tt], if Browser is Chrome, otherwise [tt]false[/tt].
	isChrome: function() {
		return this.fIsChrome;
	},

	//:m:*:isIExplorer
	//:d:en:Determines, if the used browser is a "Internet Explorer".
	//:a:en::::none
	//:r:en::isIExplorer:Boolean:[tt]true[/tt], if Browser is Internet Explorer, otherwise [tt]false[/tt].
	isIExplorer: function() {
		return this.fIsIExplorer;
	},

	//:m:*:isIExplorer
	//:d:en:Determines, if the used browser is a "Internet Explorer" and the version number is less than or equal to 6.x.
	//:a:en::::none
	//:r:en::isIExplorer:Boolean:[tt]true[/tt], if Browser is Internet Explorer less then or equal to 6.x, otherwise [tt]false[/tt].
	isIE_LE6: function() {
		return( this.isIExplorer() && this.getBrowserVersion() < 7 );
	},

	//:m:*:isIExplorer
	//:d:en:Determines, if the used browser is a "Internet Explorer" and the version number is less than or equal to 7.x. _
	//:d:en:This is required for cross-browser-abstraction.
	//:a:en::::none
	//:r:en::isIExplorer:Boolean:[tt]true[/tt], if Browser is Internet Explorer less then or equal to 7.x, otherwise [tt]false[/tt].
	isIE_LE7: function() {
		return( this.isIExplorer() && this.getBrowserVersion() < 8 );
	},

	//:m:*:isIExplorer
	//:d:en:Determines, if the used browser is a "Internet Explorer" and the version number is greater than or equal to 8.x. _
	//:d:en:This is required for cross-browser-abstraction.
	//:a:en::::none
	//:r:en::isIExplorer:Boolean:[tt]true[/tt], if Browser is Internet Explorer greater then or equal to 8.x, otherwise [tt]false[/tt].
	isIE_GE8: function() {
		return( this.isIExplorer() && this.getBrowserVersion() >= 8 );
	},

	//:m:*:isSafari
	//:d:en:Determines, if the used browser is a "Safari".
	//:a:en::::none
	//:r:en::isSafari:Boolean:[tt]true[/tt], if Browser is Safari, otherwise [tt]false[/tt].
	isSafari: function() {
		return this.fIsSafari;
	},

	//:m:*:isNetscape
	//:d:en:Determines, if the used browser is a "Netscape".
	//:a:en::::none
	//:r:en:::Boolean:[tt]true[/tt], if Browser is Netscape, otherwise [tt]false[/tt].
	isNetscape: function() {
		return this.fIsNetscape;
	},

	//:m:*:isPocketIE
	//:d:en:Determines, if the used browser is a "Pocket Internet Explorer".
	//:a:en::::none
	//:r:en::isPocketIE:Boolean:[tt]true[/tt], if browser is Pocket Internet Explorer, otherwise [tt]false[/tt].
	isPocketIE: function() {
		return this.fIsPocketIE;
	},	
	
	//:m:*:isConnected
	//:@deprecated:en:Use [tt]isOpened()[/tt] instead.
	//:d:en:Returns [tt]true[/tt] if the WebSocket connection is up otherwise [tt]false[/tt].
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the WebSocket connection is up otherwise [tt]false[/tt].
	isConnected: function() {
		return( this.isOpened() );
	},
	
	//:m:*:isOpened
	//:d:en:Returns [tt]true[/tt] if the WebSocket connection opened up, otherwise [tt]false[/tt].
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the WebSocket connection is up otherwise [tt]false[/tt].
	isOpened: function() {
		return(
			this.fConn != undefined
			&& this.fConn != null
			&& this.fConn.readyState == ws.WS_OPEN
		);
	}

};


//i:en:Browser detection (embedded into a function to not polute global namespace...
(function() {

	ws.fBrowserName	= "unknown";
	ws.fBrowserType	= ws.BT_UNKNOWN;
	ws.fBrowserVerNo	= undefined;

	ws.fIsIExplorer	= false;
	ws.fIsFirefox		= false;
	ws.fIsChrome		= false;
	
	var lUA = navigator.userAgent;

	//:i:en:First evaluate name of the browser
	ws.fIsChrome = lUA.indexOf( "Chrome" ) >= 0;
	if(ws.fIsChrome ) {
		ws.fBrowserType = ws.BT_CHROME;
	} else {
		ws.fIsFirefox = navigator.appName == "Netscape";
		if( ws.fIsFirefox ) {
			ws.fBrowserType = ws.BT_FIREFOX;
		} else {
			ws.fIsIExplorer = navigator.appName == "Microsoft Internet Explorer";
			if( ws.fIsIExplorer ) {
				ws.fBrowserType = ws.BT_IEXPLORER;
			}
		}
	}

	var p, i;
	var lStr;
	var lFound;
	var lVersion;

	if( ws.fIsIExplorer ) {
		//:i:de:Beispiel f&uuml;r userAgent bei IE6:
		//:i:de:"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"
		ws.fBrowserName = ws.BROWSER_NAMES[ ws.BT_IEXPLORER ];
		lVersion = lUA.match( /MSIE.*/i );
		if ( lVersion ) {
			lStr = lVersion[ 0 ].substr( 5 );
			p = lStr.indexOf( ";" );
			ws.fBrowserVerStr = p > 0 ? lStr.substr( 0, p ) : lStr;
			ws.fBrowserVerNo = parseFloat( ws.fBrowserVerStr );
		}
	} else if( ws.fIsFirefox ) {
		ws.fBrowserName = ws.BROWSER_NAMES[ ws.BT_FIREFOX ];
		//:i:de:Beispiel f&uuml;r userAgent bei FF 2.0.0.11:
		//:i:de:"Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11"
		lVersion = lUA.match( /Firefox\/.*/i );
		if ( lVersion ) {
			lStr = lVersion[ 0 ].substr( 8 );
			p = lStr.indexOf( " " );
			if( p > 0 ) {
				ws.fBrowserVerStr = lStr.substring( 0, p );
			} else	{
				ws.fBrowserVerStr = lStr;
			}	
			lFound = 0;
			i = 0;
			while( i < lStr.length ) {
				if( lStr.charAt( i ) == '.' ) {
					lFound++;
				}	
				if( lFound >= 2 ) {
					break;
				}	
				i++;
			}
			lStr = lStr.substring( 0, i );
			ws.fBrowserVerNo = parseFloat( lStr );
		}
	} else if( ws.fIsChrome ) {
		//:i:de:Beispiel f&uuml;r userAgent bei Chrome 4.0.211.7
		//:i:de:Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/4.0.211.7 Safari/532.0
		ws.fBrowserName = ws.BROWSER_NAMES[ ws.BT_CHROME ];
		lVersion = lUA.match( /Chrome\/.*/i );
		if ( lVersion ) {
			lStr = lVersion[ 0 ].substr( 7 );
			p = lStr.indexOf( " " );
			ws.fBrowserVerStr = p > 0 ? lStr.substr( 0, p ) : lStr;
			ws.fBrowserVerNo = parseFloat( lStr );
		}
	}
}());


//	---------------------------------------------------------------------------
//  WebSocket - some convenience JavaScript OOP tools
//	---------------------------------------------------------------------------
//	
//:package:*:ws
//:class:*:ws.oop
//:ancestor:*:-
//:d:en:Implements simple class declaration to support multi-level inheritance _
//:d:en:and easy 'inherited' calls (super-calls) in JavaScript
ws.oop = {};

//:m:*:declareClass
//:d:en:Declares a new JavaScript class which supports easy inheritance and _
//:d:en:super calls. This is required in the webSocket framework to e.g. _
//:d:en:extend the basic communication classes to the token based communication.
//:a:en::aNamespace:String:Namespace (package) of the class as a string.
//:a:en::aClassname:String:Name of the class as a string.
//:a:en::aAncestor:Class:Ancestor class (class-variables and methods are inherited)
//:a:en::aFields:Array:Array of class fields (class-variables and public methods)
//:r:*:::void:none
ws.oop.declareClass = function( aNamespace, aClassname, aAncestor, aFields ) {
	
	var lNS = self[ aNamespace ];
	if( !lNS ) { 
		self[ aNamespace ] = {};
	}
	
	var lConstructor = function() {
		if( this.create ) {
			this.create.apply( this, arguments );
		}
	};
	
	// publish the new class in the given name space
	lNS[ aClassname ] = lConstructor;

	// move all fields from spec to new class' prototype
	var lField;
	for( lField in aFields ) {
		lConstructor.prototype[ lField ] = aFields[ lField ];
	}
	if( aAncestor != null ) {
		// every class maintains an array of its direct descendants
		if( !aAncestor.descendants ) {
			aAncestor.descendants = [];
		}
		aAncestor.descendants.push( lConstructor );
		for( lField in aAncestor.prototype ) {
			var lAncMthd = aAncestor.prototype[ lField ];
			if( typeof lAncMthd == "function" ) {
				if( lConstructor.prototype[ lField ] ) {
					lConstructor.prototype[ lField ].inherited = lAncMthd;
				} else {
					lConstructor.prototype[ lField ] = lAncMthd;
				}
				// every method gets a reference to its super class
				// to allow class to inherited method from such
				lConstructor.prototype[ lField ].superClass = aAncestor;
			}
		}
	}
};


// plug-in functionality to allow to add plug-ins into existing classes
ws.oop.addPlugIn = function( aClass, aPlugIn ) {

	// if the class has no plug-ins yet initialize array
	if( !aClass.fPlugIns ) {
		aClass.fPlugIns = [];
	}
	// add the plug-in to the class
	aClass.fPlugIns.push( aPlugIn );
	// clone all methods of the plug-in to the class
	for( var lField in aPlugIn ) {
		if( !aClass.prototype[ lField ] ) {
			aClass.prototype[ lField ] = aPlugIn[ lField ];
		}
	}
	// if the class already has descendants recursively
	// clone the plug-in methods to these as well.
	if( aClass.descendants ) {
		for( var lIdx = 0, lCnt = aClass.descendants.length; lIdx < lCnt; lIdx ++ ) {
			ws.oop.addPlugIn( aClass.descendants[ lIdx ], aPlugIn );
		}
	}
};


//	---------------------------------------------------------------------------
//  WebSocket - Base Client
//  This class does not handle exceptions or error, it throws exceptions,
//  which are handled by the descendant classes.
//	---------------------------------------------------------------------------

//:package:*:ws
//:class:*:ws.webSocketBaseClient
//:ancestor:*:-
//:d:en:Implementation of the [tt]ws.webSocketBaseClient[/tt] class. _
//:d:en:This class does not handle exceptions or error, it throws exceptions, _
//:d:en:which are (have to be) handled by the descendant classes.

ws.oop.declareClass( "ws", "webSocketBaseClient", null, {

	//:m:*:open
	//:d:en:Tries to establish a connection the webSocket server.
	//:a:en::aURL:String:URL to the webSocket Server
	//:a:en::aOptions:Object:Optional arguments, see below...
	//:a:en:aOptions:OnOpen:function:Callback when connection was successfully established.
	//:r:*:::void:none
	open: function( aURL, aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		
		
		
		// if browser natively supports WebSockets...
		// otherwise flash bridge may have embedded WebSocket class
		if( self.WebSocket ) {
			
			// TODO: !this.fConn is not enough here! Check for readystate!
			// if connection not already established...
			if( !this.fConn ) {
				var lThis = this;
				var lValue = null;
				
				this.fConn = new ReconnectingWebSocket( aURL);
				// save URL and sub prot for optional re-connect
				this.fURL = aURL; 
				
				// assign the listeners to local functions (closure) to allow
				// to handle event before and after the application
				this.fConn.onopen = function( aEvent ) {
					lThis.fStatus = ws.WS_OPEN;
					// give application change to handle event
					if( aOptions.OnOpen ) {
						aOptions.OnOpen( aEvent);
					}
					// process outgoing queue
					//lThis.processQueue();
					lThis.processToken(aEvent);
				};

				this.fConn.onmessage = function( aEvent ) {

                    if( aOptions.OnMessage ) {
						aOptions.OnMessage( aEvent, lValue);
					}
					//lThis.processToken(aEvent);
					// give application change to handle event first

					
				};

				this.fConn.onclose = function( aEvent ) {
					lThis.fStatus = ws.WS_CLOSED;
					// give application chance to handle event
					if( aOptions.OnClose ) {
						aOptions.OnClose( aEvent);
					}
				};
			} else {
				throw new Error( "Already connected" );
			}
		} else {
			throw new Error( "WebSockets not supported by browser" );
		}
		
	},
	
	//:m:*:processQueue
	//:d:en:Processes the token queue. _
	//:d:en:Tries to send out all tokens stored in the quere
	//:a:en::::-
	//:r:*:::void:none
	processQueue: function() {
		this.sendToken({
			type:"welcome",
			thisDN:"",
			message:""
		});
	},
	
	processToken: function(aToken) {
		 
			if( this.fOnWelcome ) {
				//this.fOnWelcome(token);
				this.fOnWelcome(aToken);
			}
		 
	},
	
	sendToken: function(aToken) {
		this.fConn.send($.toJSON(aToken));
	},

	//:m:*:connect
	//:a:en::aURL:String:Please refer to [tt]open[/tt] method.
	//:a:en::aOptions:Object:Please refer to [tt]open[/tt] method.
	//:r:*:::void:none
	connect: function( aURL, aOptions ) {
		return this.open(aURL, aOptions );
	},

	//:m:*:isOpened
	//:d:en:Returns [tt]true[/tt] if the WebSocket connection opened up, otherwise [tt]false[/tt].
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the WebSocket connection is up otherwise [tt]false[/tt].
	isOpened: function() {
		return(
			this.fConn != undefined
			&& this.fConn != null
			&& this.fConn.readyState == ws.WS_OPEN
		);
	},

	//:m:*:getURL
	//:d:en:Returns the URL if the WebSocket connection opened up, otherwise [tt]null[/tt].
	//:a:en::::none
	//:r:*:::String:the URL if the WebSocket connection opened up, otherwise [tt]null[/tt].
	getURL: function() {
		return this.fURL;
	},
	
	//:m:*:isConnected
	//:@deprecated:en:Use [tt]isOpened()[/tt] instead.
	//:d:en:Returns [tt]true[/tt] if the WebSocket connection is up otherwise [tt]false[/tt].
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the WebSocket connection is up otherwise [tt]false[/tt].
	isConnected: function() {
		return( this.isOpened() );
	},

	//:m:*:forceClose
	//:d:en:Forces an immediate client side disconnect. The processClosed
	//:d:en:method is called if the connection was up otherwise no operation is
	//:d:en:performed.
	//:a:en::::none
	//:r:*:::void:none
	forceClose: function( aOptions ) {
		
	},

	//:m:*:close
	//:d:en:Closes the connection either immediately or with an optional _
	//:d:en:timeout. _
	//:d:en:If the connection is established up an exception s fired.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:timeout:Number:The close timeout in milliseconds, default [tt]0[/tt].
	//:r:*:::void:none
	close: function( aOptions ) {
		
	},

	//:m:*:disconnect
	//:d:en:Deprecated, kept for upward compatibility only. Do not use anymore! _
	//:d:en:Please refer to the [tt]close[/tt] method.
	//:a:en::aOptions:Object:Please refer to the [tt]close[/tt] method.
	//:r:*::::Please refer to the [tt]close[/tt] method.
	disconnect: function( aOptions ) {
		return this.close( aOptions );
	},
	
	//:m:*:createDefaultResult
	//:d:en:Creates a response token with [tt]code = 0[/tt] and _
	//:d:en:[tt]msg = "Ok"[/tt]. It automatically increases the TOKEN_ID _
	//:d:en:to obtain a unique serial id for the next request.
	//:a:en::::none
	//:r:*:::void:none
	createDefaultResult: function() {
		return{
			code: 0,
			msg: "ok"
		};
	},

	//:m:*:addPlugIn
	//:d:en:Adds a client side plug-in to the instance - not to the class!
	//:a:en::aPlugIn:Object:Plug-in to be appended to the client side plug-in chain.
	//:r:*:::void:none
	addPlugIn: function( aPlugIn, aId ) {
		// if the class has no plug-ins yet initialize array
		if( !this.fPlugIns ) {
			this.fPlugIns = [];
		}
		// add the plug-in to the class
		this.fPlugIns.push( aPlugIn );

		if( !aId ) {
			aId = aPlugIn.ID;
		}
		//:todo:en:check if plug-in with given id already exists!
		if( aId ) {
			aPlugIn.conn = this;
		}
	}

});


//	---------------------------------------------------------------------------
//  WebSocket token client (this is an abstract class)
//  don't create direct instances of WebSocketTokenClient
//	---------------------------------------------------------------------------

//:package:*:ws
//:class:*:ws.webSocketClient
//:ancestor:*:ws.webSocketBaseClient
//:d:en:Implementation of the [tt]webSocketTokenClient[/tt] class. This is _
//:d:en:an abstract class as an ancestor for the JSON-, CSV- and XML client. _
//:d:en:Do not create direct instances of webSocketTokenClient.
ws.oop.declareClass( "ws", "webSocketClient", ws.webSocketBaseClient, {

	//:m:*:open
	//:d:en:Tries to establish a connection to the webSocket server. Unlike _
	//:d:en:the inherited [tt]open[/tt] method no exceptions is fired in case _
	//:d:en:of an error but a response token is returned.
	//:a:en::aURL:String:URL to the webSocket server.
	//:a:en::aOptions:Object:Optional arguments, for details please refer to the open method of the [tt]webSocketBaseClient[/tt] class.
	//:r:*:::Object:The response token.
	//:r:*:Object:code:Number:Response code (0 = ok, otherwise error).
	//:r:*:Object:msg:String:"Ok" or error message.
	open: function( aURL, aOptions ) {
		var lRes = this.createDefaultResult();
		try {
			if( aOptions && aOptions.OnWelcome && typeof aOptions.OnWelcome == "function" ) {
				this.fOnWelcome = aOptions.OnWelcome;
			}
		    //调用父类的open方法
			arguments.callee.inherited.call( this, aURL, aOptions );
		} catch( ex ) {
			lRes.code = -1;
			lRes.msg = "Exception on open: " + ex.message;
		}
		return lRes;
	},

	//:m:*:connect
	//:d:en:Deprecated, kept for upward compatibility only. Do not use anymore!
	//:d:en:Please refer to the [tt]open[/tt] method.
	//:a:en:::Deprecated:Please refer to the [tt]open[/tt] method.
	//:r:*:::Deprecated:Please refer to the [tt]open[/tt] method.
	connect: function( aURL, aOptions ) {
		return this.open( aURL, aOptions );
	},

	//:m:*:close
	//:d:en:Closes an established WebSocket connection.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:timeout:Number:Timeout in milliseconds.
	//:r:*:::void:none
	close: function( aOptions ) {
		//return lRes;
	},

	//:m:*:disconnect
	//:d:en:Deprecated, kept for upward compatibility only. Do not use anymore!
	//:d:en:Please refer to the [tt]close[/tt] method.
	//:a:en:::Deprecated:Please refer to the [tt]close[/tt] method.
	//:r:*:::Deprecated:Please refer to the [tt]close[/tt] method.
	disconnect: function( aOptions ) {
		return this.close( aOptions );
	}

});


//	---------------------------------------------------------------------------
//  WebSocket Client System Plug-In
//	---------------------------------------------------------------------------

//:package:*:ws
//:class:*:ws.SystemClientPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]ws.SystemClientPlugIn[/tt] class.
ws.SystemClientPlugIn = {

	//:m:*:login
	//:d:en:Tries to authenticate the client against the webSocket Server by _
	//:d:en:sending a [tt]login[/tt] token.
	//:a:en::thisDN:String:The login name of the user.
	//:a:en::message:String:The message of the user.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:pool:String:Default pool the user want to register at (default [tt]null[/tt], no pool).
	//:a:en:aOptions:autoConnect:Boolean:not yet supported (defautl [tt]true[/tt]).
	//:r:*:::void:none
	login: function( thisDN, message, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isOpened() ) {
			this.sendToken({
				type: "login",
				thisDN: thisDN,
				message: message
			});
		} else {
			lRes.code = -1;
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	logon: function( url, thisDN, message, options ) {
		var lRes = this.createDefaultResult();
		if( !options ) {
			options = {};
		}
		// if already connected, just send the login token 
		if( this.isOpened() ) {
			this.login( thisDN, message, options );
		} else {
			var lAppOnWelcomeClBk = options.OnWelcome;
			var lThis = this;
			options.OnWelcome = function( event ) {
				if( lAppOnWelcomeClBk ) {
				    
					lAppOnWelcomeClBk.call( lThis, event );
				}
				lThis.login(thisDN,message,options);
			};
			this.open(
				url,
				options
			);
		}
		return lRes;
	},
	
	//:m:*:ping
	//:d:en:Sends a simple [tt]ping[/tt] token to the webSocket Server as a _
	//:d:en:notification that the client is still alive. The client optionally _
	//:d:en:can request an echo so that the client also get a notification _
	//:d:en:that the server still is alive. The [tt]ping[/tt] thus is an _
	//:d:en:important part of the webSocket connection management.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:echo:Boolean:Specifies whether the client expects a response from the server (default=[tt]true[/tt]).
	//:r:*:::void:none
	ping: function() {
		var lRes = this.createDefaultResult();
		if( this.isOpened() ) {
			this.sendToken({
				type: "ping",
				thisDN: "",
				message: ""
				});
		} else {
			lRes.code = -1;
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	//:m:*:startKeepAlive
	//:d:en:Starts the keep-alive timer in background. keep-alive sends _
	//:d:en:periodic pings to the server with an configurable interval.
	//:d:en:If the keep-alive timer has already has been started, the previous _
	//:d:en:one will be stopped automatically and a new one with new options _
	//:d:en:will be initiated.
	//:a:en::aOptions:Objects:Optional arguments as listed below...
	//:a:en:aOptions:interval:Number:Number of milliseconds for the interval.
	//:a:en:aOptions:echo:Boolean:Specifies wether the server is supposed to send an answer to the client.
	//:a:en:aOptions:immediate:Boolean:Specifies wether to send the first ping immediately or after the first interval.
	//:r:*:::void:none
	startKeepAlive: function( aOptions ) {
		// if we have a keep alive running already stop it
		if( this.hKeepAlive ) {
			stopKeepAlive();
		}
		// return if not (yet) connected
		if( !this.isOpened() ) {
			// TODO: provide reasonable result here!
			return;
		}
		var lInterval = 10000;
		var lImmediate = true;
		if( aOptions ) {
			if( aOptions.interval != undefined ) {
				lInterval = aOptions.interval;
			}
			if( aOptions.immediate != undefined ) {
				lImmediate = aOptions.immediate;
			}
		}
		if( lImmediate ) {
			// send first ping immediately, if requested
			this.ping();
		}
		// and then initiate interval...
		var lThis = this;
		this.hKeepAlive = setInterval(
			function() {
				if( lThis.isOpened() ) {
					lThis.ping();
				} else {
					lThis.stopKeepAlive();
				}
			},
			lInterval
		);
	},

	//:m:*:stopKeepAlive
	//:d:en:Stops the keep-alive timer in background. If no keep-alive is _
	//:d:en:running no operation is performed.
	//:a:en::::none
	//:r:*:::void:none
	stopKeepAlive: function() {
		// TODO: return reasonable results here
		if( this.hKeepAlive ) {
			clearInterval( this.hKeepAlive );
			this.hKeepAlive = null;
		}
	}
};

// add the WebSocket SystemClient PlugIn into the BaseClient class
ws.oop.addPlugIn( ws.webSocketClient, ws.SystemClientPlugIn );

if( !ws.browserSupportsNativeWebSockets ) {
	//	<JasobNoObfs>
	// --- swfobject.js ---
	// SWFObject v2.2 <http://code.google.com/p/swfobject/> 
	// released under the MIT License <http://www.opensource.org/licenses/mit-license.php> 
	var swfobject=function(){var D="undefined",r="object",S="Shockwave Flash",W="ShockwaveFlash.ShockwaveFlash",q="application/x-shockwave-flash",R="SWFObjectExprInst",x="onreadystatechange",O=window,j=document,t=navigator,T=false,U=[h],o=[],N=[],I=[],l,Q,E,B,J=false,a=false,n,G,m=true,M=function(){var aa=typeof j.getElementById!=D&&typeof j.getElementsByTagName!=D&&typeof j.createElement!=D,ah=t.userAgent.toLowerCase(),Y=t.platform.toLowerCase(),ae=Y?/win/.test(Y):/win/.test(ah),ac=Y?/mac/.test(Y):/mac/.test(ah),af=/webkit/.test(ah)?parseFloat(ah.replace(/^.*webkit\/(\d+(\.\d+)?).*$/,"$1")):false,X=!+"\v1",ag=[0,0,0],ab=null;if(typeof t.plugins!=D&&typeof t.plugins[S]==r){ab=t.plugins[S].description;if(ab&&!(typeof t.mimeTypes!=D&&t.mimeTypes[q]&&!t.mimeTypes[q].enabledPlugin)){T=true;X=false;ab=ab.replace(/^.*\s+(\S+\s+\S+$)/,"$1");ag[0]=parseInt(ab.replace(/^(.*)\..*$/,"$1"),10);ag[1]=parseInt(ab.replace(/^.*\.(.*)\s.*$/,"$1"),10);ag[2]=/[a-zA-Z]/.test(ab)?parseInt(ab.replace(/^.*[a-zA-Z]+(.*)$/,"$1"),10):0}}else{if(typeof O.ActiveXObject!=D){try{var ad=new ActiveXObject(W);if(ad){ab=ad.GetVariable("$version");if(ab){X=true;ab=ab.split(" ")[1].split(",");ag=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}}catch(Z){}}}return{w3:aa,pv:ag,wk:af,ie:X,win:ae,mac:ac}}(),k=function(){if(!M.w3){return}if((typeof j.readyState!=D&&j.readyState=="complete")||(typeof j.readyState==D&&(j.getElementsByTagName("body")[0]||j.body))){f()}if(!J){if(typeof j.addEventListener!=D){j.addEventListener("DOMContentLoaded",f,false)}if(M.ie&&M.win){j.attachEvent(x,function(){if(j.readyState=="complete"){j.detachEvent(x,arguments.callee);f()}});if(O==top){(function(){if(J){return}try{j.documentElement.doScroll("left")}catch(X){setTimeout(arguments.callee,0);return}f()})()}}if(M.wk){(function(){if(J){return}if(!/loaded|complete/.test(j.readyState)){setTimeout(arguments.callee,0);return}f()})()}s(f)}}();function f(){if(J){return}try{var Z=j.getElementsByTagName("body")[0].appendChild(C("span"));Z.parentNode.removeChild(Z)}catch(aa){return}J=true;var X=U.length;for(var Y=0;Y<X;Y++){U[Y]()}}function K(X){if(J){X()}else{U[U.length]=X}}function s(Y){if(typeof O.addEventListener!=D){O.addEventListener("load",Y,false)}else{if(typeof j.addEventListener!=D){j.addEventListener("load",Y,false)}else{if(typeof O.attachEvent!=D){i(O,"onload",Y)}else{if(typeof O.onload=="function"){var X=O.onload;O.onload=function(){X();Y()}}else{O.onload=Y}}}}}function h(){if(T){V()}else{H()}}function V(){var X=j.getElementsByTagName("body")[0];var aa=C(r);aa.setAttribute("type",q);var Z=X.appendChild(aa);if(Z){var Y=0;(function(){if(typeof Z.GetVariable!=D){var ab=Z.GetVariable("$version");if(ab){ab=ab.split(" ")[1].split(",");M.pv=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}else{if(Y<10){Y++;setTimeout(arguments.callee,10);return}}X.removeChild(aa);Z=null;H()})()}else{H()}}function H(){var ag=o.length;if(ag>0){for(var af=0;af<ag;af++){var Y=o[af].id;var ab=o[af].callbackFn;var aa={success:false,id:Y};if(M.pv[0]>0){var ae=c(Y);if(ae){if(F(o[af].swfVersion)&&!(M.wk&&M.wk<312)){w(Y,true);if(ab){aa.success=true;aa.ref=z(Y);ab(aa)}}else{if(o[af].expressInstall&&A()){var ai={};ai.data=o[af].expressInstall;ai.width=ae.getAttribute("width")||"0";ai.height=ae.getAttribute("height")||"0";if(ae.getAttribute("class")){ai.styleclass=ae.getAttribute("class")}if(ae.getAttribute("align")){ai.align=ae.getAttribute("align")}var ah={};var X=ae.getElementsByTagName("param");var ac=X.length;for(var ad=0;ad<ac;ad++){if(X[ad].getAttribute("name").toLowerCase()!="movie"){ah[X[ad].getAttribute("name")]=X[ad].getAttribute("value")}}P(ai,ah,Y,ab)}else{p(ae);if(ab){ab(aa)}}}}}else{w(Y,true);if(ab){var Z=z(Y);if(Z&&typeof Z.SetVariable!=D){aa.success=true;aa.ref=Z}ab(aa)}}}}}function z(aa){var X=null;var Y=c(aa);if(Y&&Y.nodeName=="OBJECT"){if(typeof Y.SetVariable!=D){X=Y}else{var Z=Y.getElementsByTagName(r)[0];if(Z){X=Z}}}return X}function A(){return !a&&F("6.0.65")&&(M.win||M.mac)&&!(M.wk&&M.wk<312)}function P(aa,ab,X,Z){a=true;E=Z||null;B={success:false,id:X};var ae=c(X);if(ae){if(ae.nodeName=="OBJECT"){l=g(ae);Q=null}else{l=ae;Q=X}aa.id=R;if(typeof aa.width==D||(!/%$/.test(aa.width)&&parseInt(aa.width,10)<310)){aa.width="310"}if(typeof aa.height==D||(!/%$/.test(aa.height)&&parseInt(aa.height,10)<137)){aa.height="137"}j.title=j.title.slice(0,47)+" - Flash Player Installation";var ad=M.ie&&M.win?"ActiveX":"PlugIn",ac="MMredirectURL="+O.location.toString().replace(/&/g,"%26")+"&MMplayerType="+ad+"&MMdoctitle="+j.title;if(typeof ab.flashvars!=D){ab.flashvars+="&"+ac}else{ab.flashvars=ac}if(M.ie&&M.win&&ae.readyState!=4){var Y=C("div");X+="SWFObjectNew";Y.setAttribute("id",X);ae.parentNode.insertBefore(Y,ae);ae.style.display="none";(function(){if(ae.readyState==4){ae.parentNode.removeChild(ae)}else{setTimeout(arguments.callee,10)}})()}u(aa,ab,X)}}function p(Y){if(M.ie&&M.win&&Y.readyState!=4){var X=C("div");Y.parentNode.insertBefore(X,Y);X.parentNode.replaceChild(g(Y),X);Y.style.display="none";(function(){if(Y.readyState==4){Y.parentNode.removeChild(Y)}else{setTimeout(arguments.callee,10)}})()}else{Y.parentNode.replaceChild(g(Y),Y)}}function g(ab){var aa=C("div");if(M.win&&M.ie){aa.innerHTML=ab.innerHTML}else{var Y=ab.getElementsByTagName(r)[0];if(Y){var ad=Y.childNodes;if(ad){var X=ad.length;for(var Z=0;Z<X;Z++){if(!(ad[Z].nodeType==1&&ad[Z].nodeName=="PARAM")&&!(ad[Z].nodeType==8)){aa.appendChild(ad[Z].cloneNode(true))}}}}}return aa}function u(ai,ag,Y){var X,aa=c(Y);if(M.wk&&M.wk<312){return X}if(aa){if(typeof ai.id==D){ai.id=Y}if(M.ie&&M.win){var ah="";for(var ae in ai){if(ai[ae]!=Object.prototype[ae]){if(ae.toLowerCase()=="data"){ag.movie=ai[ae]}else{if(ae.toLowerCase()=="styleclass"){ah+=' class="'+ai[ae]+'"'}else{if(ae.toLowerCase()!="classid"){ah+=" "+ae+'="'+ai[ae]+'"'}}}}}var af="";for(var ad in ag){if(ag[ad]!=Object.prototype[ad]){af+='<param name="'+ad+'" value="'+ag[ad]+'" />'}}aa.outerHTML='<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"'+ah+">"+af+"</object>";N[N.length]=ai.id;X=c(ai.id)}else{var Z=C(r);Z.setAttribute("type",q);for(var ac in ai){if(ai[ac]!=Object.prototype[ac]){if(ac.toLowerCase()=="styleclass"){Z.setAttribute("class",ai[ac])}else{if(ac.toLowerCase()!="classid"){Z.setAttribute(ac,ai[ac])}}}}for(var ab in ag){if(ag[ab]!=Object.prototype[ab]&&ab.toLowerCase()!="movie"){e(Z,ab,ag[ab])}}aa.parentNode.replaceChild(Z,aa);X=Z}}return X}function e(Z,X,Y){var aa=C("param");aa.setAttribute("name",X);aa.setAttribute("value",Y);Z.appendChild(aa)}function y(Y){var X=c(Y);if(X&&X.nodeName=="OBJECT"){if(M.ie&&M.win){X.style.display="none";(function(){if(X.readyState==4){b(Y)}else{setTimeout(arguments.callee,10)}})()}else{X.parentNode.removeChild(X)}}}function b(Z){var Y=c(Z);if(Y){for(var X in Y){if(typeof Y[X]=="function"){Y[X]=null}}Y.parentNode.removeChild(Y)}}function c(Z){var X=null;try{X=j.getElementById(Z)}catch(Y){}return X}function C(X){return j.createElement(X)}function i(Z,X,Y){Z.attachEvent(X,Y);I[I.length]=[Z,X,Y]}function F(Z){var Y=M.pv,X=Z.split(".");X[0]=parseInt(X[0],10);X[1]=parseInt(X[1],10)||0;X[2]=parseInt(X[2],10)||0;return(Y[0]>X[0]||(Y[0]==X[0]&&Y[1]>X[1])||(Y[0]==X[0]&&Y[1]==X[1]&&Y[2]>=X[2]))?true:false}function v(ac,Y,ad,ab){if(M.ie&&M.mac){return}var aa=j.getElementsByTagName("head")[0];if(!aa){return}var X=(ad&&typeof ad=="string")?ad:"screen";if(ab){n=null;G=null}if(!n||G!=X){var Z=C("style");Z.setAttribute("type","text/css");Z.setAttribute("media",X);n=aa.appendChild(Z);if(M.ie&&M.win&&typeof j.styleSheets!=D&&j.styleSheets.length>0){n=j.styleSheets[j.styleSheets.length-1]}G=X}if(M.ie&&M.win){if(n&&typeof n.addRule==r){n.addRule(ac,Y)}}else{if(n&&typeof j.createTextNode!=D){n.appendChild(j.createTextNode(ac+" {"+Y+"}"))}}}function w(Z,X){if(!m){return}var Y=X?"visible":"hidden";if(J&&c(Z)){c(Z).style.visibility=Y}else{v("#"+Z,"visibility:"+Y)}}function L(Y){var Z=/[\\\"<>\.;]/;var X=Z.exec(Y)!=null;return X&&typeof encodeURIComponent!=D?encodeURIComponent(Y):Y}var d=function(){if(M.ie&&M.win){window.attachEvent("onunload",function(){var ac=I.length;for(var ab=0;ab<ac;ab++){I[ab][0].detachEvent(I[ab][1],I[ab][2])}var Z=N.length;for(var aa=0;aa<Z;aa++){y(N[aa])}for(var Y in M){M[Y]=null}M=null;for(var X in swfobject){swfobject[X]=null}swfobject=null})}}();return{registerObject:function(ab,X,aa,Z){if(M.w3&&ab&&X){var Y={};Y.id=ab;Y.swfVersion=X;Y.expressInstall=aa;Y.callbackFn=Z;o[o.length]=Y;w(ab,false)}else{if(Z){Z({success:false,id:ab})}}},getObjectById:function(X){if(M.w3){return z(X)}},embedSWF:function(ab,ah,ae,ag,Y,aa,Z,ad,af,ac){var X={success:false,id:ah};if(M.w3&&!(M.wk&&M.wk<312)&&ab&&ah&&ae&&ag&&Y){w(ah,false);K(function(){ae+="";ag+="";var aj={};if(af&&typeof af===r){for(var al in af){aj[al]=af[al]}}aj.data=ab;aj.width=ae;aj.height=ag;var am={};if(ad&&typeof ad===r){for(var ak in ad){am[ak]=ad[ak]}}if(Z&&typeof Z===r){for(var ai in Z){if(typeof am.flashvars!=D){am.flashvars+="&"+ai+"="+Z[ai]}else{am.flashvars=ai+"="+Z[ai]}}}if(F(Y)){var an=u(aj,am,ah);if(aj.id==ah){w(ah,true)}X.success=true;X.ref=an}else{if(aa&&A()){aj.data=aa;P(aj,am,ah,ac);return}else{w(ah,true)}}if(ac){ac(X)}})}else{if(ac){ac(X)}}},switchOffAutoHideShow:function(){m=false},ua:M,getFlashPlayerVersion:function(){return{major:M.pv[0],minor:M.pv[1],release:M.pv[2]}},hasFlashPlayerVersion:F,createSWF:function(Z,Y,X){if(M.w3){return u(Z,Y,X)}else{return undefined}},showExpressInstall:function(Z,aa,X,Y){if(M.w3&&A()){P(Z,aa,X,Y)}},removeSWF:function(X){if(M.w3){y(X)}},createCSS:function(aa,Z,Y,X){if(M.w3){v(aa,Z,Y,X)}},addDomLoadEvent:K,addLoadEvent:s,getQueryParamValue:function(aa){var Z=j.location.search||j.location.hash;if(Z){if(/\?/.test(Z)){Z=Z.split("?")[1]}if(aa==null){return L(Z)}var Y=Z.split("&");for(var X=0;X<Y.length;X++){if(Y[X].substring(0,Y[X].indexOf("="))==aa){return L(Y[X].substring((Y[X].indexOf("=")+1)))}}}return""},expressInstallCallback:function(){if(a){var X=c(R);if(X&&l){X.parentNode.replaceChild(l,X);if(Q){w(Q,true);if(M.ie&&M.win){l.style.display="block"}}if(E){E(B)}}a=false}}}}();
	//	</JasobNoObfs>
	
	// check if appropriate flash player version is installed
	if( swfobject.hasFlashPlayerVersion( "10.0.0" ) ) {
	
	    WEB_SOCKET_DEBUG = true;
		
		// init flash bridge
		// use function to not polute the namespace with identifiers
		// get all scripts on the page to find webSocket.js path
		(function() {
			var lScripts = document.getElementsByTagName( "script" );
			for( var lIdx = 0, lCnt = lScripts.length; lIdx < lCnt; lIdx++ ) {
				var lScript = lScripts[ lIdx ];
				var lPath = lScript.src;
				if( !lPath ) {
					lPath = lScript.getAttribute( "src" );
				}
				if( lPath ) { 
					var lPos = lPath.lastIndexOf( "websocket.js" );
					if( lPos > 0 ) {
						
						window.WEB_SOCKET_SWF_LOCATION = 
							lPath.substr( 0, lPos ) + "flash-bridge/WebSocketMain.swf";
						ws.WS_FLASHBRIDGE = window.WEB_SOCKET_SWF_LOCATION;
						break;
					}
				}
			}
		})();
		
		if( window.WEB_SOCKET_SWF_LOCATION ) {
			//	<JasobNoObfs>
			// --- web_socket.js (minified) ---
			// Copyright: Hiroshi Ichikawa, http://gimite.net/en/, https://github.com/gimite/web-socket-js
			// License: New BSD License
			// Reference: http://dev.w3.org/html5/websockets/
			// Reference: http://tools.ietf.org/html/rfc6455
			// Full sources codes provided in web_socket.js
			(function(){if(window.WEB_SOCKET_FORCE_FLASH){}else if(window.WebSocket){return;}else if(window.MozWebSocket){window.WebSocket=MozWebSocket;return;}var logger;if(window.WEB_SOCKET_LOGGER){logger=WEB_SOCKET_LOGGER;}else if(window.console&&window.console.log&&window.console.error){logger=window.console;}else{logger={log:function(){},error:function(){}};}if(swfobject.getFlashPlayerVersion().major<10){logger.error("Flash Player >= 10.0.0 is required.");return;}if(location.protocol=="file:"){logger.error("WARNING: web-socket-js doesn't work in file:///... URL "+"unless you set Flash Security Settings properly. "+"Open the page via Web server i.e. http://...");}window.WebSocket=function(url,protocols,proxyHost,proxyPort,headers){var self=this;self.__id=WebSocket.__nextId++;WebSocket.__instances[self.__id]=self;self.readyState=WebSocket.CONNECTING;self.bufferedAmount=0;self.__events={};if(!protocols){protocols=[];}else if(typeof protocols=="string"){protocols=[protocols];}self.__createTask=setTimeout(function(){WebSocket.__addTask(function(){self.__createTask=null;WebSocket.__flash.create(self.__id,url,protocols,proxyHost||null,proxyPort||0,headers||null);});},0);};WebSocket.prototype.send=function(data){if(this.readyState==WebSocket.CONNECTING){throw "INVALID_STATE_ERR: Web Socket connection has not been established";}var result=WebSocket.__flash.send(this.__id,encodeURIComponent(data));if(result<0){return true;}else{this.bufferedAmount+=result;return false;}};WebSocket.prototype.close=function(){if(this.__createTask){clearTimeout(this.__createTask);this.__createTask=null;this.readyState=WebSocket.CLOSED;return;}if(this.readyState==WebSocket.CLOSED||this.readyState==WebSocket.CLOSING){return;}this.readyState=WebSocket.CLOSING;WebSocket.__flash.close(this.__id);};WebSocket.prototype.addEventListener=function(type,listener,useCapture){if(!(type in this.__events)){this.__events[type]=[];}this.__events[type].push(listener);};WebSocket.prototype.removeEventListener=function(type,listener,useCapture){if(!(type in this.__events))return;var events=this.__events[type];for(var i=events.length-1;i>=0;--i){if(events[i]===listener){events.splice(i,1);break;}}};WebSocket.prototype.dispatchEvent=function(event){var events=this.__events[event.type]||[];for(var i=0;i<events.length;++i){events[i](event);}var handler=this["on"+event.type];if(handler)handler.apply(this,[event]);};WebSocket.prototype.__handleEvent=function(flashEvent){if("readyState"in flashEvent){this.readyState=flashEvent.readyState;}if("protocol"in flashEvent){this.protocol=flashEvent.protocol;}var jsEvent;if(flashEvent.type=="open"||flashEvent.type=="error"){jsEvent=this.__createSimpleEvent(flashEvent.type);}else if(flashEvent.type=="close"){jsEvent=this.__createSimpleEvent("close");jsEvent.wasClean=flashEvent.wasClean?true:false;jsEvent.code=flashEvent.code;jsEvent.reason=flashEvent.reason;}else if(flashEvent.type=="message"){var data=decodeURIComponent(flashEvent.message);jsEvent=this.__createMessageEvent("message",data);}else{throw "unknown event type: "+flashEvent.type;}this.dispatchEvent(jsEvent);};WebSocket.prototype.__createSimpleEvent=function(type){if(document.createEvent&&window.Event){var event=document.createEvent("Event");event.initEvent(type,false,false);return event;}else{return{type:type,bubbles:false,cancelable:false};}};WebSocket.prototype.__createMessageEvent=function(type,data){if(document.createEvent&&window.MessageEvent&& !window.opera){var event=document.createEvent("MessageEvent");event.initMessageEvent("message",false,false,data,null,null,window,null);return event;}else{return{type:type,data:data,bubbles:false,cancelable:false};}};WebSocket.CONNECTING=0;WebSocket.OPEN=1;WebSocket.CLOSING=2;WebSocket.CLOSED=3;WebSocket.__initialized=false;WebSocket.__flash=null;WebSocket.__instances={};WebSocket.__tasks=[];WebSocket.__nextId=0;WebSocket.loadFlashPolicyFile=function(url){WebSocket.__addTask(function(){WebSocket.__flash.loadManualPolicyFile(url);});};WebSocket.__initialize=function(){if(WebSocket.__initialized)return;WebSocket.__initialized=true;if(WebSocket.__swfLocation){window.WEB_SOCKET_SWF_LOCATION=WebSocket.__swfLocation;}if(!window.WEB_SOCKET_SWF_LOCATION){logger.error("[WebSocket] set WEB_SOCKET_SWF_LOCATION to location of WebSocketMain.swf");return;}if(!window.WEB_SOCKET_SUPPRESS_CROSS_DOMAIN_SWF_ERROR&& !WEB_SOCKET_SWF_LOCATION.match(/(^|\/)WebSocketMainInsecure\.swf(\?.*)?$/)&&WEB_SOCKET_SWF_LOCATION.match(/^\w+:\/\/([^\/]+)/)){var swfHost=RegExp.$1;if(location.host!=swfHost){logger.error("[WebSocket] You must host HTML and WebSocketMain.swf in the same host "+"('"+location.host+"' != '"+swfHost+"'). "+"See also 'How to host HTML file and SWF file in different domains' section "+"in README.md. If you use WebSocketMainInsecure.swf, you can suppress this message "+"by WEB_SOCKET_SUPPRESS_CROSS_DOMAIN_SWF_ERROR = true;");}}var container=document.createElement("div");container.id="webSocketContainer";container.style.position="absolute";if(WebSocket.__isFlashLite()){container.style.left="0px";container.style.top="0px";}else{container.style.left="-100px";container.style.top="-100px";}var holder=document.createElement("div");holder.id="webSocketFlash";container.appendChild(holder);document.body.appendChild(container);swfobject.embedSWF(WEB_SOCKET_SWF_LOCATION,"webSocketFlash","1","1","10.0.0",null,null,{hasPriority:true,swliveconnect:true,allowScriptAccess:"always"},null,function(e){if(!e.success){logger.error("[WebSocket] swfobject.embedSWF failed");}});};WebSocket.__onFlashInitialized=function(){setTimeout(function(){WebSocket.__flash=document.getElementById("webSocketFlash");WebSocket.__flash.setCallerUrl(location.href);WebSocket.__flash.setDebug(! !window.WEB_SOCKET_DEBUG);for(var i=0;i<WebSocket.__tasks.length;++i){WebSocket.__tasks[i]();}WebSocket.__tasks=[];},0);};WebSocket.__onFlashEvent=function(){setTimeout(function(){try{var events=WebSocket.__flash.receiveEvents();for(var i=0;i<events.length;++i){WebSocket.__instances[events[i].webSocketId].__handleEvent(events[i]);}}catch(e){logger.error(e);}},0);return true;};WebSocket.__log=function(message){logger.log(decodeURIComponent(message));};WebSocket.__error=function(message){logger.error(decodeURIComponent(message));};WebSocket.__addTask=function(task){if(WebSocket.__flash){task();}else{WebSocket.__tasks.push(task);}};WebSocket.__isFlashLite=function(){if(!window.navigator|| !window.navigator.mimeTypes){return false;}var mimeType=window.navigator.mimeTypes["application/x-shockwave-flash"];if(!mimeType|| !mimeType.enabledPlugin|| !mimeType.enabledPlugin.filename){return false;}return mimeType.enabledPlugin.filename.match(/flashlite/i)?true:false;};if(!window.WEB_SOCKET_DISABLE_AUTO_INITIALIZATION){swfobject.addDomLoadEvent(function(){WebSocket.__initialize();});}})(); 
			//	</JasobNoObfs>
		}
		
	} else {
		// no Flash Player installed
		WebSocket = null;
	}
}
