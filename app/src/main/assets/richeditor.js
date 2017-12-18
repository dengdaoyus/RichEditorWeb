'use strict';
'use struct';

function HashMap(){
     /** Map 大小 **/
     var size = 0;
     /** 对象 **/
     var entry = new Object();

     /** 存 **/
     this.put = function (key , value)
     {
         if(!this.containsKey(key))
         {
             size ++ ;
         }
         entry[key] = value;
     }

     /** 取 **/
     this.get = function (key)
     {
         if( this.containsKey(key) )
         {
             return entry[key];
         }
         else
         {
             return null;
         }
     }

     /** 删除 **/
     this.remove = function ( key )
     {
         if( delete entry[key] )
         {
             size --;
         }
     }

     /** 是否包含 Key **/
     this.containsKey = function ( key )
     {
         return (key in entry);
     }

     /** 是否包含 Value **/
     this.containsValue = function ( value )
     {
         for(var prop in entry)
         {
             if(entry[prop] == value)
             {
                 return true;
             }
         }
         return false;
     }

     /** 所有 Value **/
     this.values = function ()
     {
         var values = new Array(size);
         for(var prop in entry)
         {
             values.push(entry[prop]);
         }
         return values;
     }

     /** 所有 Key **/
     this.keys = function ()
     {
         var keys = new Array(size);
         for(var prop in entry)
         {
             keys.push(prop);
         }
         return keys;
     }

     /** Map Size **/
     this.size = function ()
     {
         return size;
     }
 }

var RE = {
	currentRange: {
		startContainer: null,
		startOffset: 0,
		endContainer: null,
		endOffset: 0
	},
	cache: {
		editor: null,
		title: null,
		currentLink: null,
		line: null
	},
	commandSet: ['bold', 'italic', 'strikethrough', 'redo', 'undo'],
	schemeCache: {
		FOCUS_SCHEME: 'focus://',
		CHANGE_SCHEME: 'change://',
		STATE_SCHEME: 'state://',
		CALLBACK_SCHEME: 'callback://',
		IMAGE_SCHEME: 'image://'
	},
	setting: {
		screenWidth: 0,
		margin: 20
	},
	imageCache: new HashMap(),
	init: function init() {
		//初始化内部变量
		var _self = this;
		_self.initCache();
		_self.initSetting();
		_self.bind();
		_self.focus();
	},
	bind: function bind() {
		var _self = this;

		var _self$schemeCache = _self.schemeCache,
		    FOCUS_SCHEME = _self$schemeCache.FOCUS_SCHEME,
		    STATE_SCHEME = _self$schemeCache.STATE_SCHEME,
		    CALLBACK_SCHEME = _self$schemeCache.CALLBACK_SCHEME;


		document.addEventListener('selectionchange', _self.saveRange, false);

		_self.cache.title.addEventListener('focus', function () {
			AndroidInterface.setViewEnabled(true);
		}, false);

		_self.cache.title.addEventListener('blur', function () {
			AndroidInterface.setViewEnabled(false);
		}, false);

		_self.cache.editor.addEventListener('blur', function () {
			_self.saveRange();
		}, false);

		_self.cache.editor.addEventListener('click', function (evt) {
			_self.saveRange();
			_self.getEditItem(evt);
		}, false);

		_self.cache.editor.addEventListener('keyup', function (evt) {
			if (evt.which == 37 || evt.which == 39 || evt.which == 13 || evt.which == 8) {
				_self.getEditItem(evt);
			}
		}, false);

		_self.cache.editor.addEventListener('input', function () {
			AndroidInterface.staticWords(_self.staticWords());
		}, false);
	},
	initCache: function initCache() {
		var _self = this;
		_self.cache.editor = document.getElementById('editor');
		_self.cache.title = document.getElementById('title');
		_self.cache.line = document.getElementsByClassName('line')[0];
		_self.cache.editor.style.minHeight = window.innerHeight - 69 + 'px';
	},
	initSetting: function initSetting() {
		var _self = this;
		_self.setting.screenWidth = window.innerWidth - 20;
	},
	focus: function focus() {
		//聚焦
		var _self = this;
		var range = document.createRange();
		range.selectNodeContents(this.cache.editor);
		range.collapse(false);
		var select = window.getSelection();
		select.removeAllRanges();
		select.addRange(range);
		_self.cache.editor.focus();
	},
	getHtml: function getHtml() {
		var _self = this;
		return _self.cache.editor.innerHTML;
	},
	staticWords: function staticWords() {
		var _self = this;
		var content = _self.cache.editor.innerHTML.replace(/<div\sclass="tips">.*<\/div>|<\/?[^>]*>/g, '').replace(/\s+/, '').trim();
		return content.length;
	},
	saveRange: function saveRange() {
		//保存节点位置
		var _self = this;
		var selection = window.getSelection();
		if (selection.rangeCount > 0) {
			var range = selection.getRangeAt(0);
			var startContainer = range.startContainer,
			    startOffset = range.startOffset,
			    endContainer = range.endContainer,
			    endOffset = range.endOffset;

			_self.currentRange = {
				startContainer: startContainer,
				startOffset: startOffset,
				endContainer: endContainer,
				endOffset: endOffset
			};
		}
	},
	reduceRange: function reduceRange() {
		//还原节点位置
		var _self = this;
		var _self$currentRange = _self.currentRange,
		    startContainer = _self$currentRange.startContainer,
		    startOffset = _self$currentRange.startOffset,
		    endContainer = _self$currentRange.endContainer,
		    endOffset = _self$currentRange.endOffset;

		var range = document.createRange();
		var selection = window.getSelection();
		selection.removeAllRanges();
		range.setStart(startContainer, startOffset);
		range.setEnd(endContainer, endOffset);
		selection.addRange(range);
	},
	exec: function exec(command) {
		//执行指令
		var _self = this;
		if (_self.commandSet.indexOf(command) !== -1) {
			document.execCommand(command, false, null);
		} else {
			var value = '<' + command + '>';
			document.execCommand('formatBlock', false, value);
			_self.getEditItem({});
		}
	},
	getEditItem: function getEditItem(evt) {
		//通过点击时，去获得一个当前位置的所有状态
		var _self = this;
		var _self$schemeCache2 = _self.schemeCache,
		    STATE_SCHEME = _self$schemeCache2.STATE_SCHEME,
		    CHANGE_SCHEME = _self$schemeCache2.CHANGE_SCHEME,
		    IMAGE_SCHEME = _self$schemeCache2.IMAGE_SCHEME;

		if (evt.target && evt.target.tagName === 'A') {
			_self.cache.currentLink = evt.target;
			var name = evt.target.innerText;
			var href = evt.target.getAttribute('href');
			window.location.href = CHANGE_SCHEME + encodeURI(name + '@_@' + href);
		} else {
			if (evt.which == 8) {
				AndroidInterface.staticWords(_self.staticWords());
			}
			var items = [];
			_self.commandSet.forEach(function (item) {
				if (document.queryCommandState(item)) {
					items.push(item);
				}
			});
			if (document.queryCommandValue('formatBlock')) {
				items.push(document.queryCommandValue('formatBlock'));
			}
			window.location.href = STATE_SCHEME + encodeURI(items.join(','));
		}
	},
	insertHtml: function insertHtml(html) {
		var _self = this;
		document.execCommand('insertHtml', false, html);
	},
	setBackgroundColor: function setBackgroundColor(color) {
	    var _self = this;
	    document.body.style.backgroundColor = color;
	},
	setFontColor: function setFontColor(color) {
        document.body.style.color = color;
	},
	setLineColor: function setLineColor(color) {
	    var _self = this;
	    _self.cache.editor.style.borderColor = color;
	},
	insertLine: function insertLine() {
		var _self = this;
		var html = '<hr><div><br></div>';
		_self.insertHtml(html);
		_self.getEditItem({});
	},
	insertLink: function insertLink(name, url) {
		var _self = this;
		var html = '<a href="' + url + '" class="editor-link">' + name + '</a>';
		_self.insertHtml(html);
	},
	changeLink: function changeLink(name, url) {
		var _self = this;
		var current = _self.cache.currentLink;
		var len = name.length;
		current.innerText = name;
		current.setAttribute('href', url);
		var selection = window.getSelection();
		var range = selection.getRangeAt(0).cloneRange();
		var _self$currentRange2 = _self.currentRange,
		    startContainer = _self$currentRange2.startContainer,
		    endContainer = _self$currentRange2.endContainer;

		selection.removeAllRanges();
		range.setStart(startContainer, len);
		range.setEnd(endContainer, len);
		selection.addRange(range);
	},
	insertImage: function insertImage(url, id, width, height) {
		var _self = this;
		var newWidth = 0,
		    newHeight = 0;
		var screenWidth = _self.setting.screenWidth;

		if (width > screenWidth) {
			newWidth = screenWidth;
			newHeight = height * newWidth / width;
		} else {
			newWidth = width;
			newHeight = height;
		}
		var image = '<div><br></div><div class="block">\n\t\t\t\t<div class="img-block"><div style="width: ' + newWidth + 'px" class="process">\n\t\t\t\t\t<div class="fill">\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t\t<img class="images" data-id="' + id + '" style="width: ' + newWidth + 'px; height: ' + newHeight + 'px;" src="' + url + '"/>\n\t\t\t\t<div class="cover" style="width: ' + newWidth + 'px; height: ' + newHeight + 'px"></div>\n\t\t\t\t<div class="delete">\n\t\t\t\t\t<img src="./reload.png">\n\t\t\t\t\t<div class="tips">\u56FE\u7247\u4E0A\u4F20\u5931\u8D25\uFF0C\u8BF7\u70B9\u51FB\u91CD\u8BD5</div>\n\t\t\t\t</div></div>\n\t\t\t\t<input type="text" placeholder="\u8BF7\u8F93\u5165\u56FE\u7247\u540D\u5B57">\n\t\t\t</div><div><br></div>';
		_self.insertHtml(image);
		var img = document.querySelector('img[data-id="' + id + '"]');
		var imgBlock = img.parentNode;
		imgBlock.parentNode.contentEditable = false;
		imgBlock.addEventListener('click', function (e) {
			e.stopPropagation();
			var current = e.currentTarget;
			var img = current.querySelector('.images');
			var id = img.getAttribute('data-id');
			window.location.href = _self.schemeCache.IMAGE_SCHEME + encodeURI(id);
		}, false);
		_self.imageCache.put(id, imgBlock.parentNode);
	},
	changeProcess: function changeProcess(id, process) {
		var _self = this;
		var block = _self.imageCache.get(id);
		var fill = block.querySelector('.fill');
		fill.style.width = process + '%';
		if (process == 100) {
			var cover = block.querySelector('.cover');
			var process = block.querySelector('.process');
			var imgBlock = block.querySelector('.img-block');
			imgBlock.removeChild(cover);
			imgBlock.removeChild(process);
		}
	},
	removeImage: function removeImage(id) {
		var _self = this;
		var block = _self.imageCache.get(id);
		block.parentNode.removeChild(block);
		_self.imageCache.remove(id);
	},
	uploadFailure: function uploadFailure(id) {
		var _self = this;
		var block = _self.imageCache.get(id);
		var del = block.querySelector('.delete');
		del.style.display = 'block';
	},
	uploadReload: function uploadReload(id) {
		var _self = this;
		var block = _self.imageCache.get(id);
		var del = block.querySelector('.delete');
		del.style.display = 'none';
	}
};

RE.init();
