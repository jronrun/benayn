/*
 * Copyright (c) Hangzhou JoJu Banking Co., Ltd All rights reserved.
 */
if (typeof window.jQuery === "undefined") { throw new Error("JoJuBanking lib requires jQuery") }

/**
 * @author paulo.ye
 */
var joju = window.joju || (function($) {

	var thiz = function(obj) {
		if (obj instanceof thiz) {
			return obj;
		}
		
		return new thiz(obj);
	};
	
	//private properties
	var _tmplCache = {};
	var _logger = { log: 1, info: 2, warn: 3, error: 4, none: 5 };

	var _properties = {
		contextPath : '',
		logger		: _logger.log
	};
	
	var _detector = {
		undefined_null: function(obj, type) { return (Object.prototype.toString.call(obj).toLowerCase() == type); },
		types: function(obj, type) {
			if (thiz.isNull(obj)) { return false; }
			return (Object.prototype.toString.call(obj).toLowerCase() == type);
		},
		json: function(obj, type) {
			return typeof(obj) == "object" 
				&& Object.prototype.toString.call(obj).toLowerCase() == type && !obj.length;
		}
	};
	
	var _types = [
	    { name: 'undefined',type: '[object undefined]', cb: _detector.undefined_null},
        { name: 'null', 	type: '[object null]', 		cb: _detector.undefined_null}, 
        { name: 'regexp', 	type: '[object regexp]', 	cb: _detector.types}, 
        { name: 'date', 	type: '[object date]', 		cb: _detector.types},  
        { name: 'number',  	type: '[object number]', 	cb: _detector.types},  
        { name: 'string',  	type: '[object string]', 	cb: _detector.types}, 
        { name: 'boolean', 	type: '[object boolean]', 	cb: _detector.types},  
        { name: 'func', 	type: '[object function]', 	cb: _detector.types},  
        { name: 'object', 	type: '[object object]', 	cb: _detector.types}, 
        { name: 'array', 	type: '[object array]', 	cb: _detector.types}, 
        { name: 'json', 	type: '[object object]', 	cb: _detector.json}
     ];
	
	//public properties
	
	thiz.holder = {};
	thiz.CRLF = '\r\n';
    
    //register dynamic method
    var _registerM = function(methodN, methodBody, prefixN) {
		(function(methodN) {
			thiz[prefixN ? (prefixN + (methodN.charAt(0).toUpperCase() + methodN.slice(1))) : methodN] = methodBody;
		})(methodN);
	};
	    
	var _registerTypes = function() {
		$.each(_types, function(k, v){ _registerM(v.name, function(obj){ return v.cb(obj, v.type); }, 'is'); });
	}
	
	//initialize check type methods
	_registerTypes();
	
	//getter setter methods
    thiz.getset = function(arguments) {
    	var props = {};
    	
    	if (thiz.isString(arguments)) {
    		props[arguments] = null;
    	} else if (thiz.isArray(arguments)) {
    		$.each(arguments, function(i, v){ props[v] = null; });
    	} else if (thiz.isJson(arguments)) {
    		props = arguments;
    	} else {
    		throw new Error(typeof(arguments) + 
    				" unsupport arguments type, supports 'string' or 'string array' or 'json object'.");
    	}
    	
		$.each(props, function(n, v) {
			_registerM(n, function() { return _properties[n]; }, 'get');
			_registerM(n, function(v) { _properties[n] = v; }, 'set');
			_properties[n] = v;	//initialize value
		});
	};
	
	//initialize default properties getter setter
	thiz.getset(_properties);
	
	thiz.settings = function() {
		return _properties;
	}
	
	var _registerLoggers = function() {
		$.each(_logger, function(k, v){
			_registerM(k, function(target, mark, separator){
				_log({name: k, level: v}, target, mark, separator);
			});
		});
	}
	
	//initialize logger method
	_registerLoggers();
	
	//public methods

	thiz.register = function(module, obj, override) {
		if (thiz[module] && !override) {
			throw new Error("the given module is exists. " + module);
		}
		thiz[module] = obj;
	};
	
	thiz.unregister = function(module) {
		delete thiz[module];
	};
	
	thiz.alert = function(target) {
		alert(target);
	};
	
	thiz.confirm = function(target, cb1, cb2) {
		if (!window.confirm(target)) {
			if (thiz.isFunc(cb2)) { cb2(); }
			return;
		}
		if (thiz.isFunc(cb1)) { cb1(); }
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
	
	thiz.timestamp = function() {
		return new Date().getTime();
	};
	
	thiz.funcs = function(obj) {
		var methods = [];
		$.each(obj, function(k, v){ if (thiz.isFunc(v)) { methods.push(k); } });
		return methods.sort();
	};
	
	thiz.isElement = function(obj) {
		return !!(obj && obj.nodeType === 1);
	};
	
	thiz.isBlank = function(obj){
		return ((function(){for(var k in obj)return k})() != null ? false :true);
	};
	
	// Is the given value `NaN`? (NaN is the only number which does not equal
	// itself).
	thiz.isNaN = function(obj) {
		return thiz.isNumber(obj) && obj != +obj;
	};
	
	var _toString = function(target, separator) {
		var details = [];
		var type = typeof (target);
		var recursion = arguments.callee;
		
		switch (type) {
		case 'undefined':
		case 'number':
		case 'string':
		case 'boolean':
		case 'function':
			details.push('[object ' + type + ']: ' + target);
			break;
		case 'object':	//object, array, null
			var ocrlf = thiz.CRLF;
			//null
			if (thiz.isNull(target)) {
				details.push('[object null]: ' + target);
			} 
			//array
			else if (thiz.isArray(target)) {
				var detailA = [];
				$.each(target, function(i, v){ detailA.push(thiz.isJson(v) ? recursion(v) : v); });
				details.push('[object array]: [' + ocrlf + detailA.join(',' + ocrlf) + ocrlf + ']');
			} 
			//JSON http://bestiejs.github.io/json3
			else if (JSON && thiz.isJson(target)) {
				details.push(JSON.stringify(target));
			} 
			//regexp
			else if (thiz.isRegexp(target)) {
				details.push('[object regexp]: ' + String(target));
			}
			//date
			else if (thiz.isDate(target)) {
				details.push('[object date]: ' + String(target));
			}
			//object
			else {
				var detailO = [];
				$.each(target, function(k, v) { 
					if (thiz.isElement(v)) { v = v + ' ' + $(v).html(); } 
					else { v = (thiz.isJson(v) ? recursion(v) : v); }
					detailO.push(k + ': ' + v); 
				});
				details.push('[object object]: {' + ocrlf + detailO.join(',' + ocrlf) + ocrlf +'}');
			}
			break;
		}
	
		return details.join(separator ? separator : thiz.CRLF);
	}
	
	//the checkbox
	thiz.chkbox = {
		
		toggle: function(toggleObj, checkboxName){
			$("input[name='" + checkboxName + "']:enabled").attr("checked", toggleObj.checked);
		},
		
		hasChecked: function(checkboxName, msg){
			if(parseInt(this.checkedSize(checkboxName)) <= 0) {
				if (msg) { thiz.alert(msg); }
				return false;
			}
			return true;
		},
		
		checkedVal: function(checkboxName, separator, msg){
			if(!this.hasChecked(checkboxName, msg)) { return; }
				
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
		if (thiz.isBlank(startTag) || thiz.isBlank(endTag)) {
			return string;
		}
		
		 var result = new Array();
		 var re = new RegExp("(?:\\" + startTag + ")([\\s\\S]*?)(?:" + endTag + ")", 'gim');
         
         if (isContainTag) {
        	 $.each(string.match(re), function(k, v){
        		 result.push(v);
        	 });
             return result;
         }
         
         var tmp = null; var len = string.match(re);
         if (len == null) {
         	return result;
         }

         for(var i = 0; i < len.length; i++){
             tmp = re.exec(string);
             if (tmp != null) { result.push(tmp[1]); }
        }
        
        return result;
	};
	
	thiz.betnMap = function(string, startTag, endTag, mapCB) {
		if (thiz.isBlank(startTag) || thiz.isBlank(endTag)) {
			return string;
		}
		
		if (!thiz.isFunc(mapCB)) { return string; }
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
	
	thiz.before = function(target, method, advice){
        var original  = target[method];
        target[method] = function(){
        	if (advice.apply(this, arguments) === false) { return; }
        	return original.apply(target, arguments); 
        }
        return target
    };
     
    thiz.after = function(target, method, advice){
    	var original  = target[method];
    	target[method] = function(){ 
    		var ret = original.apply(target, arguments); 
    		advice.apply(this, arguments); /*(advice)();*/ 
    		return ret;
    	}
    	return target
    };
     
    thiz.around = function(target, method, advice){
    	var original  = target[method];
    	target[method] = function(){
    		if (advice.apply(this, arguments) === false) { return; }
    		var ret = original.apply(target, arguments);
    		advice.apply(this, arguments);
    		return ret;
    	}
    	return target
    };
    
    //wrapper the setLogger method to support logger name
    thiz.before(thiz, 'setLogger', function(logger){
    	if (thiz.isString(logger) && _logger[logger]) {
    		_properties.logger = _logger[logger]; return false;
    	} else if (!thiz.isNumber(logger) || parseInt(logger) < _logger.log || parseInt(logger) > _logger.none) {
    		throw new Error("Unsupport logger level, supports argument key or value: " + _toString(_logger));
    	}
    });
    
    //micro template, see http://ejohn.org/blog/javascript-micro-templating/
    var _micro = function tmpl(str, data){
        var fn = !/\W/.test(str) ? _tmplCache[str] = _tmplCache[str] || tmpl(document.getElementById(str).innerHTML) :
          new Function("obj", "var p=[],print=function(){p.push.apply(p,arguments);};" +
            "with(obj){p.push('" +
            str.replace(/[\r\t\n]/g, " ")
              .split("<%").join("\t")
              .replace(/((^|%>)[^\t]*)'/g, "$1\r")
              .replace(/\t=(.*?)%>/g, "',$1,'")
              .split("\t").join("');")
              .split("%>").join("p.push('")
              .split("\r").join("\\'")
          + "');}return p.join('');");
        return data ? fn( data ) : fn;
    };
    
    thiz.tmpl = function(target, data) {
    	return _micro(target, data);
    };
	
	var _log = function(logger, target, mark, separator) {
		if (parseInt(logger.level) >= parseInt(thiz.getLogger())) { 
			var tgt4log = (mark ? (mark + thiz.CRLF) : '') + _toString(target, separator);
			switch (logger.name) {
			case 'log': console.log(tgt4log); break;
			case 'info': console.info(tgt4log); break;
			case 'warn': console.warn(tgt4log); break;
			case 'error': console.error(tgt4log); break;
			}
		}
	};
	
	return thiz;
	
})( window.jQuery );

window.joju = $.extend(joju, $.jStorage);
