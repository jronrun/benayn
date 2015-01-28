/**
 * @author paulo.ye
 * Call ccq.console() first if need to use logger
 */
(function (root, factory, instance) {

  "use strict";
  if (typeof define === "function" && define.amd) {
    // AMD. Register as an anonymous module.
    define(["jquery"], factory);
  } else if (typeof exports === "object") {
    // Node. Does not work with strict CommonJS, but
    // only CommonJS-like environments that support module.exports, like Node.
    module.exports = factory(require("jquery"));
  } else {
	if (typeof root.jQuery === "undefined") { 
		throw new Error('The window.' + instance + ' lib requires jQuery'); 
	}
	
    root[instance] = root[instance] || factory(root, root.jQuery);
  }

}(this, function initialize(root, $, undefined) {

	var thiz = {};
	
	thiz = definition = function(obj) {
		if (obj instanceof thiz) { return obj; }
		if (!(this instanceof thiz)) { return new thiz(obj); }
		this._wrapped = obj;
	};
	
	//private properties
	var _undef = 'undefined';
	var _arguments = 'arguments';
	var _reference = thiz;
	var _idCounter = 0;

	var o = $({});
	var _cp = { console: false };
	var _2string = Object.prototype.toString;
	var _logger = { log: 1, info: 2, warn: 3, error: 4, none: 5 };

	var _properties = {
		contextPath : '',				//context path
		regexEnabled: true,				//is weave enabled regex
		logger		: _logger.log,		//logger level
		logPrefix: function(levelN){	//the log prefix
			return '>> ' + levelN + ' [' + thiz.when.log() + '] ';
		},
		toStringEvent: false,			//convert event to string may cause Maximum call stack size exceeded exception
		toStringShowType: true,			//if show object's type when convert object to string
		dateMask: {						//date time formats
			'default':      "yyyy-mm-dd HH:MM:ss", 			log:			"yyyy-mm-dd HH:MM:ss l",
			day:			"yyyy-mm-dd", 					fullDateTime:   "ddd mmm dd yyyy HH:MM:ss",
			shortDate:      "m/d/yy", 						mediumDate:     "mmm d, yyyy",
			longDate:       "mmmm d, yyyy", 				fullDate:       "dddd, mmmm d, yyyy",
			shortTime:      "h:MM TT", 						mediumTime:     "h:MM:ss TT",
			longTime:       "h:MM:ss TT Z", 				isoDate:        "yyyy-mm-dd",
			isoTime:        "HH:MM:ss", 					isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
			isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'", week:			"dddd"
		},								//week month i18n
		dateI18n: {
			dayNames: [
				"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
			],
			monthNames: [
				"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
				"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
			]
		}
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
	
	thiz.isEvent = function(obj) {
		return thiz.isObject(obj) && !(typeof obj.altKey == "undefined");
	};
	
	thiz.isBlank = function(obj){
		return ((function(){for(var k in obj)return k; })() != null ? false :true);
	};
	
	// Is the given value `NaN`? (NaN is the only number which does not equal itself).
	thiz.isNaN = function(obj) {
		return thiz.isNumber(obj) && obj != +obj;
	};
	
	thiz.removeEventProperty = function(obj, keepTheEventKey) {
		var noEventObj = {};
		if (thiz.isObject(obj) && thiz.isJson(obj)) {
			$.each(obj, function(k, v){ 
				if (thiz.isEvent(v)) { 
					var _evtSimpleStr = _showType('[object event]: ') + v.type;
					if (keepTheEventKey) {
						noEventObj[k] = _evtSimpleStr;
					}
					if (thiz.isInfoEnabled()) {
						_cp.getConsole().info('The ' + _evtSimpleStr + ' from property \'' + k + '\' below is event detail:');
						_cp.getConsole().info(v);
					}
				} else { noEventObj[k] = v; }
			});
		}
		
		return noEventObj;
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
			_registerM(delegate ? delegate : _reference, n, function() { 
				var _theProperty = _properties[n];
				if (!thiz.isFunc(_theProperty)) {
					return _theProperty; 
				}
				return _theProperty.apply(this, arguments);
			}, 'get');
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
			_registerM(_reference, k, function(target, mark, separator, showType){
				_log({name: k, level: v}, target, mark, separator, showType);
			});
			_registerM(_reference, k + 'Enabled', function(){
				return _cp.getConsole() && (parseInt(v) >= parseInt(thiz.getLogger()));
			}, 'is');
		}
	});
	
	//public methods
	
	thiz.alert = function(target) {
		alert(target);
	};
	
	thiz.confirm = function(target, cb1, cb2) {
		if (!root.confirm(target)) {
			if (thiz.isFunc(cb2)) { cb2(); } return;
		} if (thiz.isFunc(cb1)) { cb1(); }
	};
	
	thiz.prompt = function(title, defaultInputValue) {
		return root.prompt(title ? title : '', defaultInputValue ? defaultInputValue : '');
	};
	
	thiz.getUrl = function(uri, params){
		return thiz.endIf(thiz.getContextPath(), '/') + uri + (params ? ('?' + ($.isPlainObject(params) ? ($.param(params)) : params)) : '');
	};
	
	thiz.href = function(uri){
		root.location.href = /^http/.test(uri) ? uri : thiz.getUrl(uri);
	};
	
	thiz.hash = function(hash) {
		root.location.hash = hash;
	};
	
	thiz.getUrlVars = function() {
		var vars = {}, hash;
		$.each(root.location.href.slice(root.location.href.indexOf('?') + 1).split('&'), function(i, v){
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
	
	thiz.startWith = function(target, start) {
		return new RegExp('^' + start).test(target);
	};
	
	thiz.endWith = function(target, end) {
		return new RegExp(end + '$').test(target);
	};
	
	thiz.startIf = function(target, start) {
		return thiz.startWith(target, start) ? target : (start + target);
	};
	
	thiz.endIf = function(target, end) {
		return thiz.endWith(target, end) ? target : (target + end);
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
	
	thiz.clone = function(target) {
		return ((!thiz.isObject(target)) ? target : (thiz.isArray(target) ? target.slice() : $.extend({}, target)));
	};
	
	thiz.has = function(target, key) {
		return Object.prototype.hasOwnProperty.call(target, key);
	};

	thiz.funcs = function(obj) {
		var methods = []; $.each(obj, function(k, v){ if (thiz.isFunc(v)) { methods.push(k); } });
		return methods.sort();
	};
	
	thiz.mixin = function(obj) {
		$.each(thiz.funcs(obj), function(i, name) {
			var func = thiz[name] = obj[name];
			thiz.prototype[name] = function() {
				var args = [ this._wrapped ];
				Array.prototype.push.apply(args, arguments);
				return result.call(this, func.apply(thiz, args));
			};
		});
	};

	thiz.register = function(module, obj, override) {
		if (_reference[module] && !override) {
			throw new Error("the given module is exists. " + module);
		}
		_reference[module] = obj;
		
		if (thiz.isFunc(obj)) {
			var target = {};
			target[module] = obj;
			thiz.mixin(target);
		}
	};
	
	thiz.unregister = function(module) {
		delete _reference[module];
	};
	
	thiz.methodRegister = function(delegate, methodN, methodBody, prefixN) {
		 _registerM(delegate, methodN, methodBody, prefixN);
	};
	
	// see http://blog.stevenlevithan.com/archives/date-time-format
	var _datefmt = function () {
		var	token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,
			timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
			timezoneClip = /[^-+\dA-Z]/g,
			pad = function (val, len) {
				val = String(val);
				len = len || 2;
				while (val.length < len) val = "0" + val;
				return val;
			};

		// Regexes and supporting functions are cached through closure
		return function (date, mask, utc) {
			// You can't provide utc if you skip other args (use the "UTC:" mask prefix)
			if (arguments.length == 1 && thiz.isString(date) && !/\d/.test(date)) {
				mask = date; date = undefined;
			}

			// Passing date through Date applies Date.parse, if necessary
			date = date ? new Date(date) : new Date; if (isNaN(date)) { throw SyntaxError('invalid date: ' + date); }
			mask = String(thiz.getDateMask()[mask] || mask || thiz.getDateMask()["default"]);
			// Allow setting the utc argument via the mask
			if (mask.slice(0, 4) == "UTC:") { mask = mask.slice(4); utc = true; }

			var	_ = utc ? "getUTC" : "get",
				d = date[_ + "Date"](),
				D = date[_ + "Day"](),
				m = date[_ + "Month"](),
				y = date[_ + "FullYear"](),
				H = date[_ + "Hours"](),
				M = date[_ + "Minutes"](),
				s = date[_ + "Seconds"](),
				L = date[_ + "Milliseconds"](),
				o = utc ? 0 : date.getTimezoneOffset(),
				flags = {
					d:    d,
					dd:   pad(d),
					ddd:  thiz.getDateI18n().dayNames[D],
					dddd: thiz.getDateI18n().dayNames[D + 7],
					m:    m + 1,
					mm:   pad(m + 1),
					mmm:  thiz.getDateI18n().monthNames[m],
					mmmm: thiz.getDateI18n().monthNames[m + 12],
					yy:   String(y).slice(2),
					yyyy: y,
					h:    H % 12 || 12,
					hh:   pad(H % 12 || 12),
					H:    H,
					HH:   pad(H),
					M:    M,
					MM:   pad(M),
					s:    s,
					ss:   pad(s),
					l:    pad(L, 3),
					L:    pad(L > 99 ? Math.round(L / 10) : L),
					t:    H < 12 ? "a"  : "p",
					tt:   H < 12 ? "am" : "pm",
					T:    H < 12 ? "A"  : "P",
					TT:   H < 12 ? "AM" : "PM",
					Z:    utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
					o:    (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
					S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
				};

			return mask.replace(token, function ($0) { return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1); });
		};
	}();
	
	thiz.addDateMask = function(mask) {
		$.extend(_properties.dateMask, mask || {});
	};
	
	thiz.dateFmt = function (date, mask, utc) {
		return _datefmt(date, mask, utc);
	};
	
	var eq = function(a, b, aStack, bStack) {
		if (a === b) { return a !== 0 || 1 / a == 1 / b; } if (a == null || b == null) { return a === b; }
		// Compare `[[Class]]` names.
		var className = _2string.call(a); if (className != _2string.call(b)) { return false; }
		switch (className) {
		// Primitives and their corresponding object wrappers are equivalent; thus, `"5"` is equivalent to `new String("5")`.
		case '[object String]': return a == String(b);
		// `NaN`s are equivalent, but non-reflexive. An `egal` comparison is performed for other numeric values.
		case '[object Number]': return a != +a ? b != +b : (a == 0 ? 1 / a == 1 / b : a == +b);
		// Coerce dates and booleans to numeric primitive values. Dates are compared by their millisecond representations. 
		//Note that invalid dates with millisecond representations of `NaN` are not equivalent.
		case '[object Date]':
		case '[object Boolean]': return +a == +b;
		// RegExps are compared by their source patterns and flags.
		case '[object RegExp]':
			return a.source == b.source && a.global == b.global && a.multiline == b.multiline && a.ignoreCase == b.ignoreCase;
		}
		if (typeof a != 'object' || typeof b != 'object') { return false; }
		var length = aStack.length;
		while (length--) { if (aStack[length] == a) { return bStack[length] == b; } }
		var aCtor = a.constructor, bCtor = b.constructor;
		if (aCtor !== bCtor && !(thiz.isFunction(aCtor) && (aCtor instanceof aCtor)
						&& thiz.isFunction(bCtor) && (bCtor instanceof bCtor))) {
			return false;
		} aStack.push(a); bStack.push(b); var size = 0, result = true;
		// Recursively compare objects and arrays.
		if (className == '[object Array]') {
			size = a.length; result = size == b.length;
			if (result) { while (size--) { if (!(result = eq(a[size], b[size], aStack, bStack))) { break; } } }
		} else { // Deep compare objects.
			for ( var key in a) {
				if (thiz.has(a, key)) { size++; if (!(result = thiz.has(b, key) && eq(a[key], b[key], aStack, bStack))) { break; } }
			}
			if (result) { for (key in b) { if (thiz.has(b, key) && !(size--)) break; } result = !size; }
		} aStack.pop(); bStack.pop(); 
		return result;
	};

	thiz.isEqual = function(targetA, targetB, excludesKeys) {
		if (thiz.isInfoEnabled()) {
			thiz.info(thiz.format('Deep comparison to check if two objects are equal '
					+ '\n targetA: \n{0}\n targetB:\n{1}\n excludesKeys:\n{2}', [targetA, targetB, excludesKeys]));
		}
		if (thiz.isObject(targetA) && thiz.isObject(targetB) && thiz.isArray(excludesKeys)) {
			var a = thiz.clone(targetA), b = thiz.clone(targetB);
			$.each(excludesKeys, function(i, v){ delete a[v]; delete b[v]; });
			return eq(a, b, [], []);
		}
		
		return eq(targetA, targetB, [], []);
	};
	
	var _toString = function(target, separator, showType) {
		var details = [], ocrlf = thiz.CRLF, type = _detector(target), recursion = arguments.callee;
		switch (type) {
		case _undef:
		case 'number':
		case 'string':
		case 'boolean':
		case 'function':
		case 'null': details.push(_showType('[object ' + type + ']: ', showType) + target); break;
			
		case 'date':
		case 'regexp': details.push(_showType('[object ' + type + ']: ', showType) + String(target)); break;
		case 'error': details.push(_showType('[object ' + type + ']: ', showType) + (target.stack ? target.stack : target)); break;
		
		case _arguments: target = Array.prototype.slice.call(target);
		case 'array':
			var detailA = [];
			$.each(target, function(i, v){ detailA.push(thiz.isJson(v) ? recursion(v) : v); });
			details.push(_showType('[object ' + type + ']: ', showType) + '[' + ocrlf + detailA.join(',' + ocrlf) + ocrlf + ']');
			break;
			
		case 'object':
			
			if (!thiz.getToStringEvent()) {
				target = thiz.removeEventProperty(target, true);
			}
			
			//JSON http://bestiejs.github.io/json3
			if (root.JSON && thiz.isJson(target)) {
				try {
					// see http://stackoverflow.com/questions/11616630/json-stringify-avoid-typeerror-converting-circular-structure-to-json
					var _stringifyCache = [];
					details.push(JSON.stringify(target, function(key, value) {
					    if (typeof value === 'object' && value !== null) {
					    	// Circular reference found, discard key
					        if (_stringifyCache.indexOf(value) !== -1) { return; } _stringifyCache.push(value);
					    } return value;
					}));
					_stringifyCache = null;
					break;
				} catch (e) { thiz.warn(e.message, 'JSON.stringify ERROR (ignorable)'); }
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
			details.push(_showType('[object ' + type + ']: ', showType) + '{' + ocrlf + detailO.join(',' + ocrlf) + ocrlf +'}');
			break;
		}
	
		return details.join(separator ? separator : thiz.CRLF);
	};
	
	var _showType = function(type, showType) { 
		return ((_detector(showType) === _undef) ? thiz.getToStringShowType() : showType) ? type : ''; 
	};
	
	thiz.asStr = function(target, separator, showType) {
		return _toString(target, separator ? separator : '', showType ? showType : false);
	};
	
	thiz.unhighlight = function(text, tag) {
		tag = tag || 'span';
		var re = new RegExp('(<'+ tag +'.+?>|<\/'+ tag +'>)', 'g');
		return text.replace(re, '');
	};
	
	//see http://jsbin.com/iledos/6/edit
	thiz.highlight = function(text, words, tag) {
		tag = tag || 'span';
		var i, len = words.length, re;
		for (i = 0; i < len; i++) {
			// Global regex to highlight all matches
			re = new RegExp(words[i], 'g');
			if (re.test(text)) {
				text = text.replace(re, '<' + tag + ' class="highlight">$&</' + tag + '>');
			}
		}
		return text;
	};
	
	thiz.getParamBySelector = function(selector, theObjName, isSerialize){
		var elName = ''; var p = {}; var ron = "", objName = "";
		
		$(selector).each(function(i) {
			elName = $(this).attr("name");
			if(elName && elName.length){
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
			elementId + " input[type=hidden], " + 
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
	
	thiz.delegateFn = function(fn) {
		return thiz.isFunc(fn)? function() { return fn.apply(this, arguments); } : fn;
	};
	
	thiz.delegate = function(impl, definition) {
		if (impl.__delegated__) {
			return impl;
		}
		
		var interf = definition ? ((typeof definition === 'function') 
				? function() { return definition.apply(this, arguments); } : definition) : (function(obj) { return obj; });
		interf.__delegated__ = true;
		
		$.each(impl, function(k, fn){
			if (!fn.__delegated__ && thiz.isFunc(fn)) { interf[k] = thiz.delegateFn(fn); } else { interf[k] = fn; }
		});
		
		return interf;
	};
	
	thiz.format = function(msg, args){
		if (thiz.isObject(args)) { args = _toString(args); }
		if (!thiz.isArray(args)) { args = [args]; }
		$.each(args, function(i, v) { msg = msg.replace('{' + i + '}', (thiz.isObject(v) ? _toString(v) : v)); });
		return msg;
	};
	
    //micro template, see http://ejohn.org/blog/javascript-micro-templating/
	var _tmplCache = {};
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
    
	var _log = function(logger, target, mark, separator, showType) {
		if (_cp.getConsole() && (parseInt(logger.level) >= parseInt(thiz.getLogger()))) { 
			var tgt4log = thiz.getLogPrefix(logger.name) + (mark ? (mark + thiz.CRLF) : '') + _toString(target, separator, showType);
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
		_registerM(_reference, k, function(pointcut, advice){ return weave(pointcut, { type: v, value: advice }); }); 
	});
	
	thiz.weaveLogtime = function(pointcut) {
		var duration = _undef; thiz.before(pointcut, function(){ duration = +new Date(); });
		thiz.after(pointcut, function(result, method){
			thiz.info('duration time [' + method + '] ' + (+new Date() - duration) + 'ms');
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
    
    thiz.before({target: thiz, method: 'addDateMask'}, function(args){
    	var mask = _undef; if (args && (mask = args[0]) && thiz.isJson(mask)) {
    		$.each(mask, function(k, v){
    			_registerM(thiz.when, k, thiz.delegateFn(function(date, utc){ return _datefmt(date, v, utc); }));
    		});
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
	var _pubsub_info = function(topic) { return function(jQueryEvent, data) { thiz.info(data, 'subscribe topic: ' + topic); }; };
	thiz.subscribe = function() {
		var topic = arguments[0]; 
		if (!_pubsub_topics[topic]) { _pubsub_topics[topic] = arguments[1]; _sub(topic, _pubsub_info(topic)); }
		_sub.apply(this, arguments);
	};
	
	//popup plugin
	(function(core, module){
		
		var proto = {};
		
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
		
		var _popup = function(instanceSettings, callback) {
			var settings = $.extend({}, _popupDefaultSettings, instanceSettings || {}); var args = [];
			var excludeK = ['left', 'top', 'windowName', 'name', 'windowURL', 'href'];
			$.each(settings, function(k, v){ if (excludeK.indexOf(k) == -1) { args.push(k + '=' + v); } });
			var windowFeatures = args.join(',');

			settings.windowName = settings.name || settings.windowName;
			settings.windowURL = settings.href || settings.windowURL;
			
			var centeredY, centeredX, winObj;
			if (settings.centerBrowser) {
				if (($.browser && $.browser.msie) || (/msie/.test(navigator.userAgent.toLowerCase()))) {	// hacked together for IE browsers
					centeredY = (root.screenTop - 120) + ((((document.documentElement.clientHeight + 120) / 2) - (settings.height / 2)));
					centeredX = root.screenLeft + ((((document.body.offsetWidth + 20) / 2) - (settings.width / 2)));
				} else {
					centeredY = root.screenY + (((root.outerHeight / 2) - (settings.height / 2)));
					centeredX = root.screenX + (((root.outerWidth / 2) - (settings.width / 2)));
				}
				windowFeatures = windowFeatures + ',left=' + centeredX + ',top=' + centeredY;
				winObj = root.open( settings.windowURL, settings.windowName, windowFeatures);
			} else if (settings.centerScreen) {
				centeredY = (screen.height - settings.height) / 2;
				centeredX = (screen.width - settings.width) / 2;
				windowFeatures = windowFeatures + ',left=' + centeredX + ',top=' + centeredY;
				winObj = root.open( settings.windowURL, settings.windowName, windowFeatures);
			} else {
				windowFeatures = windowFeatures + ',left=' + settings.left + ',top=' + settings.top;
				winObj = root.open( settings.windowURL, settings.windowName, windowFeatures);
			}
			
			if (core.isInfoEnabled()) {
				core.info('window open ' + settings.windowName + '(' + settings.windowURL + ') with arguments: ' + windowFeatures);
			}
			
			core.delay(function(winObj){
				if (winObj && core.isFunc(callback)) { callback(winObj); }
			}, 500, winObj);
		};
		
		$.each({
			fullscreen: [{fullscreen : 1}, function(winObj){
				root.opener = null; 
				root.open('', '_self'); 
				root.close(); 
				root.moveTo(0, 0); 
				root.resizeTo(screen.availWidth, screen.availHeight); 
			}],
			win: [],
			centerScreen: [{centerScreen : 1}],
			centerBrowser: [{centerBrowser : 1}]
		}, function(k, v){
			core.methodRegister(proto, k, function(href, settings, callback){
				_popup($.extend({href : href}, settings, v[0]), (core.isFunc(callback) ? callback : v[1]));
			});
		});
		
		core.register(module, core.delegate(proto));
		
	})(thiz, 'popup');
	
	//chkbox plugin
	(function(core, module) {
		
		var proto = {};
		
		proto.toggle = function(toggleObj, checkboxName){
			$("input[name='" + checkboxName + "']:enabled").attr("checked", toggleObj.checked);
		};
		
		proto.hasChecked = function(checkboxName, msg){
			if(parseInt(proto.checkedSize(checkboxName)) <= 0) {
				if (msg) { core.alert(msg); } return false;
			} return true;
		};
		
		proto.checkedVal = function(checkboxName, separator, msg){
			if(!proto.hasChecked(checkboxName, msg)) { return; }
				
			return $("input:checked[name='" + checkboxName + "']").map(function(){
				return $(this).val();
			}).get().join((separator ? separator : ","));
		};
		
		proto.checkedSize = function(checkboxName){
			return $("input:checked[name='" + checkboxName + "']").size();
		};
		
		core.register(module, core.delegate(proto));
		
	})(thiz, 'chkbox');
	
	//when plugin
	(function(core, module) {
		
		var proto = {};
		
		$.each(core.getDateMask(), function(k, v){
			core.methodRegister(proto, k, function(date, utc){
				return core.dateFmt(date, v, utc);
			});
		});
		
		core.register(module, core.delegate(proto, function(mask, date, utc) {
			return core.dateFmt(date, mask, utc);
		}));
		
	})(thiz, 'when');
	
	var result = function(obj) {
		return this._chain ? thiz(obj).chain() : obj;
	};

	thiz.mixin(thiz);

	thiz.chain = function(obj) {
		return thiz(obj).chain();
	};
	
	$.extend(thiz.prototype, {
		chain : function() {
			this._chain = true;
			return this;
		},
		value : function() {
			return this._wrapped;
		}
	});

	/* add module usage
	(function($, thiz, module) {
		
		var proto = function() {};
		thiz.register(module, thiz.delegate(proto)); 
		
	})(window.jQuery, window.ccq, 'paging');
	*/
	
	return (function(impl){ return _reference = thiz.delegate(impl, definition); })(thiz);
	
}, 'ccq'));

/*
 * Paging
 * query box format: 
 * <? id="query-box" action=""><?>
 * </?>
 * <? id="query-box-detail">
 * </?>
 * <? id="query-box-page">
 * 	<? page="prev">上一页</?>
 * 	<? page="3">3</?>
 * 	<? page="next">下一页</?>
 * </?>
 */
(function($, thiz, module) {
	
	var proto = function() {};
	
	var _currentBoxId = null;
	var _pageSize = 10;
	var _opts = {};
	_opts.actions = {};
	
	var _defaultOptions = {
		boxId: 'query-box',
		detailId: 'query-box-detail',
		pageId: 'query-box-page',
		before: null,
		after: null,
		objName: null,
		afterPaging: null
	};
	
	var asId = function(elId) { return thiz.startIf(elId, '#'); };
	
	proto.query = function(qryBoxId, options) {
		if (ccq.isJson(qryBoxId)) {
			options = qryBoxId;
			qryBoxId = options.boxId ? options.boxId : _defaultOptions.boxId;
		}
		
		var qbId = qryBoxId ? qryBoxId : _defaultOptions.boxId;
		qbId = _currentBoxId = asId(qbId);
		
		//前一次查询条件
		var prevCond = thiz.clone(_opts[qbId] || {});
		
		//
		var opts = options = $.extend({}, _defaultOptions, _opts[qbId] || {}, options || {}, {boxId: qbId});
		_opts[qbId] = options;
		
		opts.params = $.extend({
			pageNo: 1,
			pageSize: _pageSize
		}, opts.params || {}, thiz.getParam(opts.boxId, opts.objName));
		
		//去除空条件
		$.each(opts.params, function(k, v){
			if (!v || '' == v) { delete opts.params[k]; }
		});
		
		if (thiz.isFunc(opts.before) && !opts.before(opts.params)) {
			return;
		}
		
		//两次条件不一样重置页码为一
		if (!thiz.isBlank(prevCond)) {
			if (!thiz.isEqual(prevCond.params, opts.params)) {
				_opts[qbId].params.pageNo = opts.params.pageNo = 1;
			}
		}
		
		
		var act = $(opts.boxId).attr('action');
		
		//前后action不一致重置页码为一
		if (_opts.actions[qbId] !== act) {
			_opts.actions[qbId] = act;
			_opts[qbId].params.pageNo = opts.params.pageNo = 1;
		}
		
		if (!act) {
			thiz.error(thiz.format('The query box({0}) cannot find action property! '
					+ 'Query box fomat like: <? id="query-box" action=""><?>', opts.boxId));
			return;
		}
		
		$.post((opts.url = thiz.getUrl(act)), opts.params || {}, function(data, textStatus, jqXHR) {
			if (thiz.isFunc(opts.after)) {
				opts.after(data, textStatus, jqXHR, opts);
			} else {
				$(asId(opts.detailId, '#')).html(data);
				
				var pgId = asId(opts.pageId);
				if ($(pgId).length) {
					$(pgId + ' a').click(function(){
						var pn, page = $.trim($(this).attr('page'));
						switch (page) {
						case 'prev': if ((pn = (opts.params.pageNo - 1)) <= 0) { return; } break;
						case 'next': pn = opts.params.pageNo + 1; break;
						default: if (String(page).length <= 0) { return; } pn = page; break;
						}
						
						if (opts.params.pageNo == pn) { return; }
						proto.topage(parseInt(pn), options);
					});
				} else {
					thiz.error('The page bar cannot find! like: '
							+ '<? id="query-box-page"><? page="prev">上一页</?><? page="3">3</?><? page="next">下一页</?></?>');
				}
			}
			
			if (thiz.isFunc(opts.afterPaging)) { opts.afterPaging(); }
		});
		
		if (thiz.isInfoEnabled()) {
			thiz.info(opts, 'Load page with options: ');
		}
	};
	
	proto.options = function(qryBoxId) {
		return _opts[asId(qryBoxId)];
	};
	
	proto.topage = function(pageNo, options){
		var qbId = (options && options.boxId) ? options.boxId : _currentBoxId;
		$.extend(options.params, { pageNo: pageNo });
		proto.query(qbId, options);
	};
	
	thiz.register(module, thiz.delegate(proto)); 
	
})(window.jQuery, window.ccq, 'paging');

/*
 * UI
 * depend bootboxjs.com
 */
var _ui = $.extend({
	
	ajax: function(url, options) {
		var defaultContainer = 'ajax-modal';
		var o = $.extend({
			//common option
			data: {},
			title: '',
			footer: '',
			containerId: defaultContainer,
			
			buttons: {
				close: {
					label: "关  闭",
					className: "btn btn-primary",
					callback: function() {
						$modal.modal('hide');
					}
				}
			},
			
			//modal option
			keyboard: false,
			backdrop: 'static',
			
			//modal css
			width: 'auto',
			height: 'auto',
			'margin-left': function() {
				return -($(this).width() / 2);
			}
		}, options || {});
		
		var modalId = ccq.startIf(o.containerId, '#');
		var $modal = $(modalId);
		
		var modalFunc = function(responseText, textStatus, XMLHttpRequest){
			$modal.addClass('modal hide fade').modal(o).css(o);
			$modal.css({
				width: function() {
					return $(this).width() + 25;
				}
			});
		};
		
		if (defaultContainer == o.containerId) {
			$(modalId + ' .modal-header').html(o.title);
			
			var btnHolder = {};
			var footers = [o.footer];
			var now = ccq.now();
			
			if (o.buttons) {
				$.each(o.buttons, function(k, v){
					var btnTmpl = '<a href="javascript:void(0);" class="{0}" id="btns{1}-{2}">{3}</a>';
					footers.push(ccq.format(btnTmpl, [v.className, now, k, v.label]));
					btnHolder[k] = v.callback;
				});
			}
			
			$(modalId + ' .modal-footer').html(footers.join(''));
			$.each(btnHolder, function(k, v){ $('#btns' + now + '-' + k).click(v); });
			
			modalId = modalId + ' .modal-body';
		} 
		
		$(modalId).load(ccq.getUrl(url), o.data, modalFunc);
		return $modal;
	},
	
	close: function($modal, closeCallback) {
		if (ccq.isFunc(closeCallback)) { $modal.on('hide', closeCallback); }
		$modal.modal('hide').off('hide');
		
	}

}, bootbox);
ccq.register('ui', _ui);

ccq.register('toui', function(target, duration, settings){
	$.scrollTo(target, duration ? duration : 200, settings);
});

/*
 * Notify
 * depend http://ned.im/noty/
 */
ccq.register('noty', {
	alert : function(text, options) {
		return this._notify(text, 'alert', options);
	},
	
	warn : function(text, options) {
		return this._notify(text, 'warning', options);
	},
	
	succ : function(text, options) {
		return this._notify(text, 'success', options);
	}, 
	
	info : function(text, options) {
		return this._notify(text, 'information', options);
	},
	
	confirm : function(text, cb1, cb2, options) {
		var confirmOptions = {
			timeout : false,
			modal : true,
			layout : 'center',
			buttons : [ {
				addClass : 'btn btn-primary',
				text : '确定',
				onClick : function($noty) {
					$noty.close();
					if (ccq.isFunc(cb1)) { cb1(); }
				}
			}, {
				addClass : 'btn btn-danger',
				text : '取消',
				onClick : function($noty) {
					$noty.close();
					if (ccq.isFunc(cb2)) { cb2(); }
				}
			} ]
		};
		return this._notify(text, 'confirmation', $.extend(confirmOptions, options));
	}, 
	
	error : function(text, options) {
		return this._notify('<h3>出错啦..</h3>' + text, 'error', $.extend({
			layout: 'top',
			timeout: false,
			buttons: [
		          {addClass: 'btn btn-danger', text: '关闭', onClick: function($noty) {
		              $noty.close();
		          }
			 } ]
		}, options));
	},
	
	_notify : function(text, type, options) {
		var defaultOptions = {
			timeout : 5000,
			layout : 'topCenter'	
		};
		return noty($.extend(defaultOptions, ($.extend({text : text}, options || {}, {type : type}))));
	}
});


/*
 * global
 * depend http://fgnass.github.io/spin.js/
 * usage html: <div id="ajax-loading" style="position:absolute;left:2%;top:33px;padding:3px;"></div>
 */
(function($, thiz, module) {
	
	var proto = function() {};
	
	//请求超时时间, 单位毫秒(ms)
	var REQUEST_TIMEOUT = 20000;

	var _spinner = false;
	var _loading = 'ajax-loading';
	var opts = {
		  lines: 7, 			// The number of lines to draw
		  length: 5, 			// The length of each line
		  width: 11, 			// The line thickness
		  radius: 4, 			// The radius of the inner circle
		  corners: 0.7, 		// Corner roundness (0..1)
		  rotate: 0, 			// The rotation offset
		  direction: 1, 		// 1: clockwise, -1: counterclockwise
		  color: '#FF0000', 	// #rgb or #rrggbb or array of colors
		  speed: 0.8, 			// Rounds per second
		  trail: 80, 			// Afterglow percentage
		  shadow: true, 		// Whether to render a shadow
		  hwaccel: true, 		// Whether to use hardware acceleration
		  className: 'spinner', // The CSS class to assign to the spinner
		  zIndex: 2e9, 			// The z-index (defaults to 2000000000)
		  top: 'auto', 			// Top position relative to parent in px
		  left: 'auto' 			// Left position relative to parent in px
	};
	
	proto.spinStart = function() {
		if (!_spinner) {
			var target = document.getElementById(_loading);
			_spinner = new Spinner(opts).spin(target);
			
			$(window).scroll(function(){
			   var scrollTop = $(this).scrollTop();
			   var top = ($(this).scrollTop() >= 30) ? (scrollTop + 33 + 'px') : '33px';
			   $('#' + _loading).css({ "position":"absolute", left: '2%', top: top });
			});
		}
	};
	
	proto.spinStop = function() {
		if (_spinner) {
			_spinner.stop();
			_spinner = false;
		}
	};
	
	$(function(){
		$(document)
			.ajaxStart(function(event, request, settings){ proto.spinStart(); })
			.ajaxStop(function(event, request, settings){ proto.spinStop(); })
			.ajaxSuccess(function(event, request, settings){ proto.spinStop(); })
			.ajaxSend(function(event, request, settings){ })
			.ajaxComplete(function(event, request, settings){ })
			.ajaxError(function(event, request, settings){
				if (thiz.isWarnEnabled()) {
					var logS = {
						type: settings.type, timeout: settings.timeout, url: settings.url, jsonp: settings.jsonp,
						dataType: settings.dataType, data: settings.data, crossDomain: settings.crossDomain, 
						contentType: settings.contentType, cache: settings.cache, async: settings.async, global: settings.global, 
						hasContent: settings.hasContent, isLocal: settings.isLocal
					};
					var logR = { readyState: request.readyState, status: request.status, statusText: request.statusText };
					var logE = { type: event.type, timeStamp: event.timeStamp };
					
					thiz.error(thiz.format('ERROR EVENT: {0}\n request: \n{1}\n settings: \n{2}', [logE, logR, logS]));
				}
				
				if (request.getResponseHeader("X-cas") && request.status == 401){
					ccq.ui.confirm('会话已过期，请重新登录！', function(result){
						if (result) { ccq.href('signin.html'); }
					}); 
					return;
				}
				
				if (request.responseText && request.responseText.length > 0) {
					var browserH = $(window).height() - 60;
					var options = {
							closeWith: 'button',
							modal: true,
							template: '<div class="noty_message" style="overflow-y:auto;height:' + browserH 
							+ 'px;"><div class="noty_close"></div><span class="noty_text"></span></div>',
					};
					thiz.error(request.responseText, "AJAX ERROR request responseText");
					ccq.noty.error(request.responseText, options);
				}
			});
		
	    $.ajaxSetup({ 
	    	cache: false, 
	    	beforeSend: function(request) {
	            //request.setRequestHeader('Accept', 
	            //	'application/json,text/javascript,text/html;charset=UTF-8,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8,*/*');
	        },
	        timeout: REQUEST_TIMEOUT,
	        error: function(jqXHR, textStatus, errorThrown) {
	        	if (textStatus == 'timeout') {
	        		ccq.noty.warn('请求超时!');
	      		}
	        }
	    });
	});
	
	thiz.register(module, thiz.delegate(proto));
	
})(window.jQuery, window.ccq, 'global');



/*
 * Translated default messages for the jQuery validation plugin.
 * Locale: ZH (Chinese, 中文 (Zhōngwén), 汉语, 漢語)
 */
(function ($) {
	$.extend($.validator.messages, {
		required: "必选字段",
		remote: "请修正该字段",
		email: "请输入正确格式的电子邮件",
		url: "请输入合法的网址",
		date: "请输入合法的日期",
		dateISO: "请输入合法的日期 (ISO).",
		number: "请输入合法的数字",
		digits: "只能输入整数",
		creditcard: "请输入合法的信用卡号",
		equalTo: "请再次输入相同的值",
		accept: "请输入拥有合法后缀名的字符串",
		maxlength: $.validator.format("长度太长({0})"),
		minlength: $.validator.format("长度太短({0})"),
		rangelength: $.validator.format("长度太短或太长({0}和 {1}之间)"),
		range: $.validator.format("请输入一个介于 {0} 和 {1} 之间的值"),
		max: $.validator.format("请输入一个最大为 {0} 的值"),
		min: $.validator.format("请输入一个最小为 {0} 的值")
	});
}(jQuery));

jQuery.validator.addMethod("requiredSelect", function(value, element, params) {
	var defaultSelectValue = ((typeof(params) != 'boolean') ? params : "");
	return value != defaultSelectValue;
}, "请选择");

jQuery.validator.addMethod("requiredCity", function(value, element, params) {
	var defaultSelectValue = ((typeof(params) != 'boolean') ? params : "");
	if (value == defaultSelectValue) {
		return false;
	}
	
	var $city = $(element).parent().find('.city:visible');
	if ($city.length == 0) {
		return true;
	}
	
	return $city.val() != defaultSelectValue;
}, "请选择城市");

jQuery.validator.addMethod("mobile", function(value, element) {
	return this.optional(element) || /^1[3|4|5|8][0-9]\d{4,8}$/.test(value);
}, "请输入正确的手机号码");


