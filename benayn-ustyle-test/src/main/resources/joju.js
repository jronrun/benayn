/*
 * Copyright (c) Hangzhou JoJu Banking Co., Ltd All rights reserved.
 */
if (typeof window.jQuery === "undefined") { throw new Error("The JoJuBanking lib requires jQuery"); }

/**
 * @author paulo.ye
 * Call joju.console() first if need to use logger
 */
var joju = window.joju || (function($) {

	var thiz = function() {};
	
	//private properties
	var _tmplCache = {};
	var _undef = 'undefined';
	var _arguments = 'arguments';
	var _reference = thiz;
	var _idCounter = 0;

	var o = $({});
	var _cp = { console: false };
	var _2string = Object.prototype.toString;
	var _logger = { log: 1, info: 2, warn: 3, error: 4, none: 5 };

	var _properties = {
		contextPath : '',			//context path
		regexEnabled: true,			//is weave enabled regex
		logger		: _logger.log	//logger level
	};
	
	var _theTypes = [
	    'Arguments', 'Array', 'Boolean', 'Date', 'Document', 'Element', 'Error', 'Fragment', 
	    'Function', 'NodeList', 'Null', 'Number', 'Object', 'RegExp', 'String', 'TextNode', 'Undefined', 'Window'
	];
	
	var _detector = (function() {
		var _2strings = {}, _nodeTypes = { 1: 'element', 3: 'textnode', 9: 'document', 11: 'fragment' };
 
		for (var _i = _theTypes.length; _i--; ) {
			var _aType = _theTypes[_i], constructor = window[_aType];
			if (constructor) { try { _2strings[_2string.call(new constructor)] = _aType.toLowerCase(); } catch (e) { } }
		}
	 
		return function(item) {
			return item == null && (item === undefined ? _undef : 'null') 
				|| item.nodeType && _nodeTypes[item.nodeType] 
				|| typeof item.length == 'number' && ( item.callee && _arguments 
				|| item.alert && 'window' || item.item && 'nodelist') || _2strings[_2string.call(item)];
		};
	 
	})();
	
	// see http://swip.codylindley.com/popupWindowDemo.html http://www.w3schools.com/jsref/met_win_open.asp
	//1. 常规 -》 选项卡 设置 -》 遇到弹出窗口时 始终在新窗口中打开弹出窗口
	//2. 安全 -》 自定义级别 -》 允许脚本初始化的窗口，不受大小或位置限制
	//3. 安全 -》 自定义级别 -》 允许网站打开没有地址或状态栏的窗口
	var _popupDefaultSettings = {
		channelmode:0,		// whether or not to display the window in theater mode. Default is no. IE only yes|no|1|0
		fullscreen:0,		// whether or not to display the browser in full-screen mode. Default is no.
							// A window in full-screen mode must also be in theater mode. IE only yes|no|1|0
		titlebar:0,			// whether or not to display the title bar. 
							// Ignored unless the calling application is an HTML Application or a trusted dialog box
		centerBrowser:0, 	// center window over browser window? {1 (YES) or 0 (NO)}. overrides top and left
		centerScreen:0, 	// center window over entire screen? {1 (YES) or 0 (NO)}. overrides top and left
		height:500, 		// sets the height in pixels of the window.
		left:0, 			// left position when the window appears.
		location:0, 		// determines whether the address bar is displayed {1 (YES) or 0 (NO)}.
		menubar:0, 			// determines whether the menu bar is displayed {1 (YES) or 0 (NO)}.
		resizable:0, 		// whether the window can be resized {1 (YES) or 0 (NO)}. Can also be overloaded using resizable.
		scrollbars:0, 		// determines whether scrollbars appear on the window {1 (YES) or 0 (NO)}.
		status:0, 			// whether a status line appears at the bottom of the window {1 (YES) or 0 (NO)}.
		width:500, 			// sets the width in pixels of the window.
		windowName:null, 	// name of window set from the name attribute of the element that invokes the click
		windowURL:null, 	// url used for the popup
		top:0, 				// top position when the window appears.
		toolbar:0 			// determines whether a toolbar (includes the forward and back buttons) is displayed {1 (YES) or 0 (NO)}.
	};

	//public properties
	
	thiz.holder = {};
	thiz.CRLF = '\r\n';
    
    //register dynamic method
    var _registerM = function(delegate, methodN, methodBody, prefixN) {
		(function(methodN) {
			delegate[prefixN ? (prefixN + (methodN.charAt(0).toUpperCase() + methodN.slice(1))) : methodN] = methodBody;
		})(methodN);
	};
	
	//initialize detects type methods
	$.each(_theTypes, function(k, v){ 
		_registerM(_reference, v, function(obj){ return _detector(obj) == v.toLowerCase(); }, 'is'); });
	
	thiz.isFunc = thiz.isFunction;
	thiz.isJson = function(obj) {
		return typeof(obj) == "object" && _2string.call(obj).toLowerCase() == '[object object]' && !obj.length;
	};
	
	thiz.isBlank = function(obj){
		return ((function(){for(var k in obj)return k; })() != null ? false :true);
	};
	
	// Is the given value `NaN`? (NaN is the only number which does not equal itself).
	thiz.isNaN = function(obj) {
		return thiz.isNumber(obj) && obj != +obj;
	};
	
	//getter setter methods
    thiz.getset = function(args, delegate) {
    	var props = {};
    	
    	if (thiz.isString(args)) { props[args] = null; }
    	else if (thiz.isArray(args)) { $.each(args, function(i, v){ props[v] = null; }); }
    	else if (thiz.isJson(args)) { props = args; }
    	else { throw new Error(typeof(arguments) +
    		" unsupport arguments type, supports 'string' or 'string array' or 'json object'."); }
    	
		$.each(props, function(n, v) {
			_registerM(delegate ? delegate : _reference, n, function() { return _properties[n]; }, 'get');
			_registerM(delegate ? delegate : _reference, n, function(v) { _properties[n] = v; }, 'set');
			_properties[n] = v;	//initialize value
		});
	};
	
	//initialize default properties getter setter
	thiz.getset(_properties);
	thiz.getset(_cp, _cp);
	thiz.settings = function() { return _properties; };
	
	//initialize logger method
	$.each(_logger, function(k, v){
		if ('none' != k) {
			_registerM(_reference, k, function(target, mark, separator){
				_log({name: k, level: v}, target, mark, separator);
			});
			_registerM(_reference, k + 'Enabled', function(){
				return _cp.getConsole() && (parseInt(v) >= parseInt(thiz.getLogger()));
			}, 'is');
		}
	});
	
	//public methods

	thiz.register = function(module, obj, override) {
		if (_reference[module] && !override) {
			throw new Error("the given module is exists. " + module);
		}
		_reference[module] = obj;
	};
	
	thiz.unregister = function(module) {
		delete _reference[module];
	};
	
	thiz.alert = function(target) {
		alert(target);
	};
	
	thiz.confirm = function(target, cb1, cb2) {
		if (!window.confirm(target)) {
			if (thiz.isFunc(cb2)) { cb2(); } return;
		} if (thiz.isFunc(cb1)) { cb1(); }
	};
	
	thiz.prompt = function(title, defaultInputValue) {
		return window.prompt(title ? title : '', defaultInputValue ? defaultInputValue : '');
	};
	
	thiz.getUrl = function(uri, params){
		return thiz.getContextPath() + uri + (params ? ("?" + ($.isPlainObject(params) ? ($.param(params)) : params)) : "");
	};
	
	thiz.href = function(uri){
		window.location.href = thiz.getUrl(uri);
	};
	
	thiz.hash = function(hash) {
		window.location.hash = hash;
	};
	
	thiz.getUrlVars = function() {
		var vars = {}, hash;
		$.each(window.location.href.slice(window.location.href.indexOf('?') + 1).split('&'), function(i, v){
			hash = v.split('=');
			vars[hash[0]] = hash[1];
		});
		return vars;
	};
	
	thiz.now = thiz.timestamp = function() {
		return +new Date();
	};
	
	thiz.typeDetect = function(obj) {
		return _detector(obj);
	};
	
	thiz.startIf = function(target, start) {
		return new RegExp('^' + start).test(target) ? target : (start + target);
	};
	
	thiz.endIf = function(target, end) {
		return new RegExp(end + '$').test(target) ? target : (target + end);
	};
	
	thiz.prefix = function(target, length, fill) {
		return (Array(length).join(fill ? fill : '0') + target).slice(-length);
	};
	
	thiz.randomStr = function(length) {
	    var str = ''; for ( ; str.length < length; str += Math.random().toString(36).substr(2) );
	    return str.substr(0, length);
	};
	
	//see http://underscorejs.org/
	
	thiz.times = function(n, iterator, context) {
		var accum = Array(Math.max(0, n));
		for (var i = 0; i < n; i++)
			accum[i] = iterator.call(context, i);
		return accum;
	};
	
	thiz.uniqueId = function(prefix) {
		var id = ++_idCounter + '';
		return prefix ? (prefix + id) : id;
	};


	thiz.delay = function(func, wait) {
		var args = Array.prototype.slice.call(arguments, 2);
		return setTimeout(function(){ return func.apply(null, args); }, wait);
	};
	
	thiz.once = function(func) {
		var ran = false, memo = _undef; return function() {
			if (ran) { return memo; }
			ran = true; memo = func.apply(this, arguments); func = null;
			return memo;
		};
	};
	
	thiz.wrap = function(func, wrapper) {
		return function() {
			var args = [ func ]; Array.prototype.push.apply(args, arguments);
			return wrapper.apply(this, args);
		};
	};

	thiz.funcs = function(obj) {
		var methods = []; $.each(obj, function(k, v){ if (thiz.isFunc(v)) { methods.push(k); } });
		return methods.sort();
	};
	
	var _toString = function(target, separator) {
		var details = [], ocrlf = thiz.CRLF, type = _detector(target), recursion = arguments.callee;
		switch (type) {
		case _undef:
		case 'number':
		case 'string':
		case 'boolean':
		case 'function':
		case 'null': details.push('[object ' + type + ']: ' + target); break;
			
		case 'date':
		case 'regexp': details.push('[object ' + type + ']: ' + String(target)); break;
		case 'error': details.push('[object ' + type + ']: ' + (target.stack ? target.stack : target)); break;
		
		case _arguments: target = Array.prototype.slice.call(target);
		case 'array':
			var detailA = [];
			$.each(target, function(i, v){ detailA.push(thiz.isJson(v) ? recursion(v) : v); });
			details.push('[object ' + type + ']: [' + ocrlf + detailA.join(',' + ocrlf) + ocrlf + ']');
			break;
			
		case 'object':
			//JSON http://bestiejs.github.io/json3
			if (JSON && thiz.isJson(target)) {
				details.push(JSON.stringify(target));
				break;
			}
			
		case 'document':
		case 'element':
		case 'fragment':
		case 'nodelist':
		case 'textnode':
		case 'window':
		default:
			var detailO = []; $.each(target, function(k, v) {
				if (thiz.isElement(v)) { v = v + ' ' + $(v).html(); } 
				else { v = (thiz.isJson(v) ? recursion(v) : v); }
				detailO.push(k + ': ' + v); 
			});
			details.push('[object ' + type + ']: {' + ocrlf + detailO.join(',' + ocrlf) + ocrlf +'}');
			break;
		}
	
		return details.join(separator ? separator : thiz.CRLF);
	};
	
	//the checkbox
	thiz.chkbox = {
		
		toggle: function(toggleObj, checkboxName){
			$("input[name='" + checkboxName + "']:enabled").attr("checked", toggleObj.checked);
		},
		
		hasChecked: function(checkboxName, msg){
			if(parseInt(thiz.chkbox.checkedSize(checkboxName)) <= 0) {
				if (msg) { thiz.alert(msg); } return false;
			} return true;
		},
		
		checkedVal: function(checkboxName, separator, msg){
			if(!thiz.chkbox.hasChecked(checkboxName, msg)) { return; }
				
			return $("input:checked[name='" + checkboxName + "']").map(function(){
				return $(this).val();
			}).get().join((separator ? separator : ","));
		},
		
		checkedSize: function(checkboxName){
			return $("input:checked[name='" + checkboxName + "']").size();
		}
	};
	
	thiz.getParamBySelector = function(selector, theObjName, isSerialize){
		var elName = ''; var p = {}; var ron = "", objName = "";
		
		$(selector).each(function(i) {
			elName = $(this).attr("name");
			if(elName.length){
				p[elName] = $.trim($(this).val());
				if(!theObjName){ ron = (ron == "" ? elName : ron); }
			}
		});
		
		if(theObjName && objName == ""){
			var end = elName.indexOf(".");
			objName = ((end == -1) ? "" : (elName.substring(0, end) + "."));
			p['objName'] = objName;
		}
		
		return isSerialize ? $.param(p) : p;
	};
	
	thiz.getParam = function(elementId, extraSelector, theObjName, isSerialize){
		var extra = '';
		elementId = (/^#/.test(elementId)) ? elementId : ('#' + elementId);
		
		$.each(extraSelector || [], function(k, v){
			extra += ', ' + elementId + ' ' + v;		
		});
		
		return thiz.getParamBySelector(
			elementId + " select, " + 
			elementId + " input[type=text], " + 
			elementId + " input[type=checkbox]" + extra, 
			theObjName, isSerialize);
	};
	
	thiz.selectedText = function(selectName){
		return $("select[name='" + selectName + "'] option:selected").text();
	};
	
	thiz.hasEventBind = function(selector, type) {
		return $(selector).data("events") && $(selector).data("events")[type];
	};
	
	thiz.betn = function(string, startTag, endTag, isContainTag) {
		var r = []; thiz.betnMap(string, startTag, endTag, function(vhasT, vnoT){
			r.push(isContainTag ? vhasT : vnoT);
		}); return r;
	};
	
	thiz.betnMap = function(string, startTag, endTag, mapCB) {
		return string.replace(new RegExp("(?:\\" + startTag + ")([\\s\\S]*?)(?:" + endTag + ")", 'gim'), mapCB);
	};
	
	thiz.syncAjax = function(uri, callback, params, ajaxOptions) {
		if (ajaxOptions && ajaxOptions.dataType && ajaxOptions.dataType == 'string') {
			var result = $.ajax($.extend({ url: thiz.getUrl(uri), 
				type: 'post', data: params || {} }, ajaxOptions || {}, { async: false })).responseText;
			if(thiz.isFunc(callback)) { callback(result); } return;
		}
		
		$.ajax($.extend({ url: thiz.getUrl(uri), type: 'post', data: params || {}, 
			dataType: 'json', success: function(result) { if(thiz.isFunc(callback)) { callback(result); } }
        }, ajaxOptions || {}, { async: false }));
	};
	
	thiz.delegate = function(impl) {
		var interf = function(obj) { return obj; };
		
		$.each(impl, function(k, delegate){
			if (thiz.isFunc(delegate)) {
				interf[k] = function() { return delegate.apply(this, arguments); };
			} else { interf[k] = delegate; }
		});
		
		return interf;
	};
	
	thiz.format = function(msg, args){
		if (thiz.isObject(args)) { args = _toString(args); }
		if (!thiz.isArray(args)) { args = [args]; }
		$.each(args, function(i, v) { msg = msg.replace('{' + i + '}', v); });
		return msg;
	};
	
    //micro template, see http://ejohn.org/blog/javascript-micro-templating/
	thiz.tmpl = function tmpl(target, data){
        var fn = !/\W/.test(target) ? _tmplCache[target] = _tmplCache[target] || tmpl(document.getElementById(target).innerHTML) :
          new Function("obj", "var p=[],print=function(){p.push.apply(p,arguments);};" +
            "with(obj){p.push('" +
            target.replace(/[\r\t\n]/g, " ")
              .split("<%").join("\t")
              .replace(/((^|%>)[^\t]*)'/g, "$1\r")
              .replace(/\t=(.*?)%>/g, "',$1,'")
              .split("\t").join("');")
              .split("%>").join("p.push('")
              .split("\r").join("\\'")
          + "');}return p.join('');");
        return data ? fn( data ) : fn;
    };
    
	var _log = function(logger, target, mark, separator) {
		if (_cp.getConsole() && (parseInt(logger.level) >= parseInt(thiz.getLogger()))) { 
			var tgt4log = (mark ? (mark + thiz.CRLF) : '') + _toString(target, separator);
			switch (logger.name) {
			case 'log': if (_cp.getConsole().log) { _cp.getConsole().log(tgt4log); } break;
			case 'info': if (_cp.getConsole().info) { _cp.getConsole().info(tgt4log); } break;
			case 'warn': if (_cp.getConsole().warn) { _cp.getConsole().warn(tgt4log); } break;
			case 'error': if (_cp.getConsole().error) { _cp.getConsole().error(tgt4log); } break;
			}
		}
	};
	
	//AOP see http://jquery-aop.googlecode.com/
	var _after			= 1;
	var _afterThrow		= 2;
	var _afterFinally	= 3;
	var _before			= 4;
	var _around			= 5;
	var _intro			= 6;
	
	var weaveOne = function(source, method, advice) {
		var old = source[method];
		// Work-around IE6/7 behavior on some native method that return object instances
		if (advice.type != _intro && !thiz.isFunc(old)) {
			var oldObject = old;
			old = function() {
				var code = arguments.length > 0 ? _arguments + '[0]' : '';
				for (var i = 1; i < arguments.length; i++) { code += ',' + _arguments + '[' + i + ']'; }
				return eval('oldObject(' + code + ');');
			};
		}

		var aspect;
		if (advice.type == _after || advice.type == _afterThrow || advice.type == _afterFinally) {
			aspect = function() {
				var returnValue = null, exceptionThrown = null;
				try { returnValue = old.apply(this, arguments); } catch (e) { exceptionThrown = e; }
				
				if (advice.type == _after) {
					if (exceptionThrown == null) {
						returnValue = advice.value.apply(this, [returnValue, method]);
					} else { throw exceptionThrown; }
				}
				else if (advice.type == _afterThrow && exceptionThrown != null) {
					returnValue = advice.value.apply(this, [exceptionThrown, method]);
				}
				else if (advice.type == _afterFinally) {
					returnValue = advice.value.apply(this, [returnValue, exceptionThrown, method]);
				}
				return returnValue;
			};
		}
		else if (advice.type == _before) {
			aspect = function() {
				if (advice.value.apply(this, [arguments, method]) == false) { return; }
				return old.apply(this, arguments);
			};
		}
		else if (advice.type == _intro) {
			aspect = function() { return advice.value.apply(this, arguments); };
		}
		else if (advice.type == _around) {
			aspect = function() {
				var invocation = { object: this, args: Array.prototype.slice.call(arguments) };
				return advice.value.apply(invocation.object, [{ arguments: invocation.args, method: method, proceed : 
					function() { return old.apply(invocation.object, invocation.args); }
				}] );
			};
		}

		//fn.unweave()  OR var advices = ... advices[0].unweave();
		aspect.unweave = function() { 
			source[method] = old;
			pointcut = source = aspect = old = null;
		};
		
		source[method] = aspect;
		return aspect;
	};

	var search = function(source, pointcut, advice) {
		var methods = [];
		for (var method in source) {
			var item = null;
			// Ignore exceptions during method retrival
			try { item = source[method]; } catch (e) { }
			if (item != null && method.match(pointcut.method) && thiz.isFunc(item)) {
				methods[methods.length] = { source: source, method: method, advice: advice };
			}
		}
		return methods;
	};

	var weave = function(pointcut, advice) {
		if (thiz.isString(pointcut)) { pointcut = {target: window, method: pointcut}; }
		else if (thiz.isJson(pointcut) && !pointcut.target) { pointcut.target = window; }
		var source = typeof(pointcut.target.prototype) != _undef ? pointcut.target.prototype : pointcut.target;
		var advices = [];
		// If it's not an introduction and no method was found, try with regex...
		if (advice.type != _intro && typeof(source[pointcut.method]) == _undef) {
			// First try directly on target
			var methods = search(pointcut.target, pointcut, advice);
			// No method found, re-try directly on prototype
			if (methods.length == 0) { methods = search(source, pointcut, advice); }
			for (var i in methods) {
				advices[advices.length] = weaveOne(methods[i].source, methods[i].method, methods[i].advice);
			}
		} else { // Return as an array of one element
			advices[0] = weaveOne(source, pointcut.method, advice);
		}
		return thiz.getRegexEnabled() ? advices : advices[0];
	};
	
	$.each({
		before: _before, around: _around, introduction: _intro,
		after: _after, afterThrow: _afterThrow, afterFinally: _afterFinally
	}, function(k, v){ 
		_registerM(_reference, k, function(pointcut, advice){ return weave(pointcut, { type: v, value: advice }); }); });
	
	thiz.weaveLogtime = function(pointcut) {
		var duration = _undef; thiz.before(pointcut, function(){ duration = +new Date(); });
		thiz.after(pointcut, function(result, method){
			thiz.info('>>duration time [' + method + '] ' + (+new Date() - duration) + 'ms');
			return result;
		});
	};
	
	 //wrapper the setLogger method to support logger name
    thiz.before({target: thiz, method: 'setLogger'}, function(args){
    	if (thiz.isString(logger = args[0]) && _logger[logger]) {
    		_properties.logger = _logger[logger]; return false;
    	} else if (!thiz.isNumber(logger) || parseInt(logger) < _logger.log || parseInt(logger) > _logger.none) {
    		throw new Error("Unsupport logger level, supports argument key or value: " + _toString(_logger));
    	}
    });
    
    var _replacer = function(conversionObject) {
        var regexpStr = ''; for ( var k in conversionObject ) { regexpStr += (regexpStr.length ? '|' : '') + k; }
        var regexpr = new RegExp(regexpStr,'ig'); // g: global, m:multi-line, i: ignore case
        return function(s) { return s.replace(regexpr, 
        		function(str, p1, p2, offset, s) { var a = conversionObject[str]; return a == undefined ? str : a; }); };
    };
    
    thiz.replacer = function(configuration) {
    	return new _replacer(configuration);
    };
    
	var _popup = function(instanceSettings, callback) {
		var settings = $.extend({}, _popupDefaultSettings, instanceSettings || {}); var args = [];
		var excludeK = ['left', 'top', 'windowName', 'name', 'windowURL', 'href'];
		$.each(settings, function(k, v){ if (excludeK.indexOf(k) == -1) { args.push(k + '=' + v); } });
		var windowFeatures = args.join(',');

		settings.windowName = settings.name || settings.windowName;
		settings.windowURL = settings.href || settings.windowURL;
		
		var centeredY, centeredX, winObj;
		if (settings.centerBrowser) {
			if ($.browser.msie) {	// hacked together for IE browsers
				centeredY = (window.screenTop - 120) + ((((document.documentElement.clientHeight + 120) / 2) - (settings.height / 2)));
				centeredX = window.screenLeft + ((((document.body.offsetWidth + 20) / 2) - (settings.width / 2)));
			} else {
				centeredY = window.screenY + (((window.outerHeight / 2) - (settings.height / 2)));
				centeredX = window.screenX + (((window.outerWidth / 2) - (settings.width / 2)));
			}
			windowFeatures = windowFeatures + ',left=' + centeredX + ',top=' + centeredY;
			winObj = window.open( settings.windowURL, settings.windowName, windowFeatures);
		} else if (settings.centerScreen) {
			centeredY = (screen.height - settings.height) / 2;
			centeredX = (screen.width - settings.width) / 2;
			windowFeatures = windowFeatures + ',left=' + centeredX + ',top=' + centeredY;
			winObj = window.open( settings.windowURL, settings.windowName, windowFeatures);
		} else {
			windowFeatures = windowFeatures + ',left=' + settings.left + ',top=' + settings.top;
			winObj = window.open( settings.windowURL, settings.windowName, windowFeatures);
		}
		
		if (thiz.isInfoEnabled()) {
			thiz.info('>>window open ' + settings.windowName + '(' + settings.windowURL + ') with arguments: ' + windowFeatures);
		}
		
		thiz.delay(function(winObj){
			if (winObj && thiz.isFunc(callback)) { callback(winObj); }
		}, 500, winObj);
	};
	
	var _popupMs = {
		fullscreen: [{fullscreen : 1}, function(winObj){
			window.opener = null; 
			window.open('', '_self'); 
			window.close(); 
			window.moveTo(0, 0); 
			window.resizeTo(screen.availWidth, screen.availHeight); 
		}],
		win: [],
		centerScreen: [{centerScreen : 1}],
		centerBrowser: [{centerBrowser : 1}]
	};
	
	$.each(_popupMs, function(k, v){
		_registerM(_reference, k, function(href, settings, callback){
			_popup($.extend({href : href}, settings, v[0]), (thiz.isFunc(callback) ? callback : v[1]));
		}, 'popup');
	});
	
	thiz.console = function(logDelegate, reset) {
		if (!_cp.getConsole() || reset) {
			if (thiz.isFunc(logDelegate)) {
				_cp.setConsole({ log: logDelegate, info: logDelegate, warn: logDelegate, error: logDelegate });
			} else if (thiz.isObject(logDelegate)) { _cp.setConsole(logDelegate); } else { eval('_cp.setConsole(console);'); }
		}
		return _cp.getConsole();
	};
	
	//see https://github.com/cowboy/jquery-tiny-pubsub
	var _pubsub_topics = {};
	var _sub = function() { o.on.apply(o, arguments); };
	thiz.publish = function() { o.trigger.apply(o, arguments); };
	thiz.unsubscribe = function() { delete _pubsub_topics[arguments[0]]; o.off.apply(o, arguments); };
	var _pubsub_info = function(topic) { return function(jQueryEvent, data) { thiz.info(data, '>>subscribe topic: ' + topic); }; };
	thiz.subscribe = function() {
		var topic = arguments[0]; 
		if (!_pubsub_topics[topic]) { _pubsub_topics[topic] = arguments[1]; _sub(topic, _pubsub_info(topic)); }
		_sub.apply(this, arguments);
	};
	
	return (function(impl){ return _reference = thiz.delegate(impl); })(thiz);
	
})(window.jQuery);

window.joju.store = $.jStorage;
