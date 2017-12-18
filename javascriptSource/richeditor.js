'use struct'

const RE = (function(){
	const RE = {
		/**
		 * currentRange  用于保存一个Range对象的四个属性startContainer、startOffset、endContainer、endOffset
		 */


		currentRange: {
			startContainer: null,
			startOffset: 0,
			endContainer: null,
			endOffset: 0
		},

		/**
		 * cache  用于存储一些常用的DOM元素，避免每次查找
		 */


		cache: {
			editor: null,
			title: null,
			currentLink: null,
			line: null
		},

		/**
		 * 设置一些execCommand的原生命令集
		 */

		
		commandSet: ['bold', 'italic', 'strikethrough', 'redo', 'undo'],

		/**
		 * 设置与webWiew交互时，拦截URL的url协议头
		 */

		schemeCache: {
			CHANGE_SCHEME: 'change://',
			STATE_SCHEME: 'state://',
			CALLBACK_SCHEME: 'callback://',
			IMAGE_SCHEME: 'image://'
		},

		/**
		 * 屏幕尺寸的基础设置，需要在每次初始化的时候去获取
		 */

		setting: {
			screenWidth: 0,
			margin: 20
		},

		/**
		 * 给插入图片设置一个map表，内部：
		 * @key：id
		 * @value：block元素
		 */

		imageCache: new Map(),

		/**
		 * init函数
		 * 
		 * @功能：初始化RE对象的内部变量(cahce、 setting、一些元素的事件绑定)
		 */

		init: function(){
			const _self = this;
			_self.initCache();
			_self.initSetting();
			_self.bind();
		},

		/**
		 * initCache缓存
		 * 
		 * @功能：初始化cache数组中的变量，主要时editor元素和title标题元素
		 */

		initCache: function(){
			const _self = this;
			_self.cache.editor = document.getElementById('editor');
			_self.cache.title = document.getElementById('title');
			_self.cache.editor.style.minHeight = window.innerHeight - 69 + 'px';
			_self.cache.line = document.getElementsByClassName('line')[0];
		},

		/**
		 * bind函数
		 * 
		 * @功能：绑定以下几个事件
		 * 
		 * 1. selectchange事件  当editor中的range发生变化时，会记入range的一些属性
		 * 2. 标题栏的focus和blur事件   当标题栏聚焦时，需要通知webView对底栏进行隐藏和显示
		 * 3. editor的blur事件   当editor块失去焦点，需要记住失去焦点前的range部分
		 * 4. editor的click事件   当editor被点击时，需要去通知webView，去转变下底栏的状态
		 * 5. editor的keyup事件   当输入'<'、'>'、回车键、删除键等四个键位，同样需要去通知webView，去转变下底栏的状态
		 * 6. editor的input事件   当输入时，对内容的字数进行监测
		 */
		
		bind: function(){
			const _self = this;

			document.addEventListener('selectionchange', _self.saveRange, false);

			_self.cache.title.addEventListener('focus', function(){
				AndroidInterface.setViewEnabled(true);
			}, false);

			_self.cache.title.addEventListener('blur', () => {
				AndroidInterface.setViewEnabled(false);
			}, false);

			_self.cache.editor.addEventListener('blur', () => {
				_self.saveRange();
			}, false);

			_self.cache.editor.addEventListener('click', (evt) => {
				_self.saveRange();
				_self.getEditItem(evt);
			}, false);

			
			_self.cache.editor.addEventListener('keyup', (evt) => {
				if(evt.which == 37 || evt.which == 39 || evt.which == 13 || evt.which == 8){
					_self.getEditItem(evt);
				}
			}, false);

			_self.cache.editor.addEventListener('input', () => {
				AndroidInterface.staticWords(_self.staticWords());
			}, false);
		},

		/**
		 * initSetting函数
		 * 
		 * @功能：初始化setting变量中的元素，主要是初始化当前屏幕的宽度，方便之后的图片判断
		 */

		initSetting: function(){
			const _self = this;
			_self.setting.screenWidth = window.innerWidth - _self.setting.margin;
		},

		/**
		 * focus函数
		 * 
		 * @功能： 使editor的焦点聚焦到编辑块的末尾
		 */

		focus: function(){   //聚焦
			const _self = this;
			const range = document.createRange();
			range.selectNodeContents(this.cache.editor);
			range.collapse(false);
			const select = window.getSelection();
			select.removeAllRanges();
			select.addRange(range);
			_self.cache.editor.focus();
		},


		setBackgroundColor: function (r ,g, b) {
				document.body.style.backgroundColor = rgb(r, g, b);
		},
		setFontColor: function setFontColor(r, g, b) {
				var _self = this;
				_self.cache.editor.style.color = rgb(r, g, b);
		},

		setLineColor: function setLineColor(r, g, b) {
				var _self = this;
				_self.cache.editor.style.broderColor = rgb(r, g, b);
		},

		/**
		 * getHtml函数
		 * 
		 * @功能：得到editor中的元素内容
		 */

		getHtml: function(){
			const _self = this;
			return _self.cache.editor.innerHTML;
		},

		/**
		 * staticWords函数
		 * 
		 * @功能：获得editor中的字数
		 * 
		 * 使用正则表达式过滤innerHTML中的dom元素以及img块等部分，之后在将制表符空格等不可见字符过滤，然后再将尾部的空行过滤
		 */

		staticWords: function(){
			const _self = this;
			const content = _self.cache.editor.innerHTML.replace(/<div\sclass="tips">.*<\/div>|<\/?[^>]*>/g, '').replace(/\s+/, '').trim();
			return content.length;
		},

		/**
		 * saveRange函数
		 * 
		 * @功能：保存Range属性
		 * 
		 * 通过获取Selection选区中第一个range块，然后去保存range中的startContainer、startOffset、endContainer、endOffset四个属性
		 */

		saveRange: function(){
			const _self = this;
			const selection = window.getSelection();
			if(selection.rangeCount > 0){
				const range = selection.getRangeAt(0);
				const { startContainer, startOffset, endContainer, endOffset} = range;
				_self.currentRange = {
					startContainer: startContainer,
					startOffset: startOffset,
					endContainer: endContainer,
					endOffset: endOffset
				};
			}
		},

		/**
		 * reduceRange函数
		 * 
		 * @功能：还原保存的Range块
		 * 
		 * 通过document去创建一个新的Range对象，然后将保存的Range四个属性设置给创建的Range对象，之后将Selection中已存在的Range块删除，将新的Range添加进去
		 */

		reduceRange: function(){
			const _self = this;
			const { startContainer, startOffset, endContainer, endOffset} = _self.currentRange;
			const range = document.createRange();
			const selection = window.getSelection();
			selection.removeAllRanges();
			range.setStart(startContainer, startOffset);
			range.setEnd(endContainer, endOffset);
			selection.addRange(range);
		},

		/**
		 * exec函数
		 * 
		 * @功能：执行不同的execCommand函数
		 * @param：command指令，例如：'bold','italic','h1','p'
		 * 
		 * 首先去判断command命令是否存在于commandSet中，如果存在，直接执行execCommand；如果不存在，则合成value，然后执行execCommand
		 */

		exec: function(command){
			const _self = this;
			if(_self.commandSet.indexOf(command) !== -1){
				document.execCommand(command, false, null);
			}else{
				let value = '<'+command+'>';
				document.execCommand('formatBlock', false, value);
			}
		},

		/**
		 * getEditItem函数
		 * 
		 * @功能：检测当前的输入点的状态
		 * @param：evt事件对象，可有可无
		 * 
		 * 获得输入点处的标签状态，主要分为两种情况：
		 * 
		 * 1. 如果点击a元素时，返回链接的name和href
		 * 2. 如果不是链接的话，通过queryCommandState()存入标签、或者使用queryCommandValue()，之后将存入状态的数组，拼接返回
		 * 
		 */

		getEditItem: function(evt = {}){
			const _self = this;
			const { STATE_SCHEME, CHANGE_SCHEME } = _self.schemeCache;
			if(evt.target && evt.target.tagName === 'A'){
				_self.cache.currentLink = evt.target;
				const name = evt.target.innerText;
				const href = evt.target.getAttribute('href');
				window.location.href = CHANGE_SCHEME + encodeURI(name + '@_@' + href);
			}else{
				if(e.which == 8){
					AndroidInterface.staticWords(_self.staticWords());
				}
				const items = [];
				_self.commandSet.forEach((item) => {
					if(document.queryCommandState(item)){
						items.push(item);
					}
				});
				if(document.queryCommandValue('formatBlock')){
					items.push(document.queryCommandValue('formatBlock'));
				}
				window.location.href = STATE_SCHEME + encodeURI(items.join(','));
			}
		},

		/**
		 * insertHtml函数
		 * 
		 * @功能：插入html字符串
		 * @param：html字符串
		 * 
		 * 使用execCommand中的insertHtml属性插入html字符串
		 */

		insertHtml: function(html){
			const _self = this;
			document.execCommand('insertHtml', false, html);
		},

		/**
		 * insertLine函数
		 * 
		 * @功能：插入分割行
		 * 
		 * 首先生成html字符串，需要在hr标签后面加入<div><br></div>，这个标签块在编辑块中起到回车的作用
		 */

		insertLine: function(){
			const _self = this;
			const html = '<hr><div><br></div>';
			_self.insertHtml(html);
		},

		/**
		 * insetLink函数
		 * 
		 * @功能：插入链接
		 * @param：name和url，例如：'zimo'和'http://www.lhbzimo.cn'
		 * 
		 * 首先生成html字符串，合成一个a标签的字符串，然后调用insertHtml插入
		 */

		insertLink: function(name, url){
			const _self = this;
			const html = `<a href="${url}" class="editor-link">${name}</a>`;
			_self.insertHtml(html);
		},

		/**
		 * changeLink函数
		 * 
		 * @功能：改变链接信息
		 * @param：name和url
		 * 
		 * 首先通过currentLink来获得当前的a元素，然后在修改a元素中的href和内容信息，之后克隆当前的range，
		 * 将range中的startOffset和endOffset设置成链接长度，保证链接修改完之后，焦点处于链接尾部
		 */

		changeLink: function(name, url){
			const _self = this;
			const current = _self.cache.currentLink;
			const len = name.length;
			current.innerText = name;
			current.setAttribute('href', url);
			const selection = window.getSelection();
			const range = selection.getRangeAt(0).cloneRange();
			const { startContainer, endContainer } = _self.currentRange;
			selection.removeAllRanges();
			range.setStart(startContainer, len);
			range.setEnd(endContainer, len);
			selection.addRange(range);
		},

		/**
		 * insertImage函数
		 * 
		 * @功能：插入图片
		 * @param：url、id、width和height，例如：'../example.png'，不可重复的标识，图片的宽度和图片的高度
		 * 
		 * 首先，通过width和screenWidth比较，如果大于屏幕宽度设置成screenWidth，然后将高度等比例缩小
		 * 然后，生成图片块的html字符串
		 * 之后，给图片块增加点击的监听事件，当点击图片块时，返回标识块id
		 * 最后，将标识和图片块放入imageCache中
		 * 
		 */

		insertImage: function(url, id, width, height){
			const _self = this;
			let newWidth=0, newHeight = 0;
			const { screenWidth } = _self.setting;
			if(width > screenWidth){
				newWidth = screenWidth;
				newHeight = height * newWidth / width;
			}else{
				newWidth = width;
				newHeight = height;
			}
			const image = `<div><br></div><div class="img-block">
					<div style="width: ${newWidth}px" class="process">
						<div class="fill">
						</div>
					</div>
					<img class="images" data-id="${id}" style="width: ${newWidth}px; height: ${newHeight}px;" src="${url}"/>
					<div class="cover" style="width: ${newWidth}px; height: ${newHeight}px"></div>
					<div class="delete">
						<img src="./reload.png">
						<div class="tips">图片上传失败，请点击重试</div>
					</div>
					<input type="text" placeholder="请输入图片名字">
				</div><div><br></div>`;
			_self.insertHtml(image);
			const img = document.querySelector(`img[data-id="${id}"]`);
			const imgBlock = img.parentNode;
			imgBlock.contentEditable = false;
			imgBlock.addEventListener('click', (e) => {
				e.stopPropagation();
				const current = e.currentTarget;
				const img = current.querySelector('.images');
				const id = img.getAttribute('data-id');
				window.location.href = _self.schemeCache.IMAGE_SCHEME + encodeURI(id);
			}, false);
			_self.imageCache.set(id, imgBlock);
		},

		/**
		 * changeProcess函数
		 * 
		 * @功能：改变图片上传的进度
		 * @param：id和process，例如：唯一标识和1-100的进度
		 * 
		 * 首先，通过唯一标识，从图片缓存中得到图片块，然后设置图片块中的fill元素的width，当process等于100的时候，删除上层的遮罩和进度条
		 */

		changeProcess: function(id, process){
			var _self = this;
			var imgBlock = _self.imageCache.get(id);
			var fill = imgBlock.querySelector('.fill');
			fill.style.width = `${process}%`;
			if(process == 100){
				var cover = imgBlock.querySelector('.cover');
				var process = imgBlock.querySelector('.process');
				imgBlock.removeChild(cover);
				imgBlock.removeChild(process);
			}
		},

		/**
		 * 
		 * removeImage函数
		 * 
		 * @功能：删除图片
		 * @param：id
		 * 
		 * 通过id来获得map表中的图片块，然后通过它的父节点删除该图片块
		 */

		removeImage: function(id){
			var _self = this;
			var imgBlock = _self.imageCache.get(id);
			imgBlock.parentNode.removeChild(imgBlock);
			_self.imageCache.delete(id);
		},

		/**
		 * 
		 * uploadFailure函数
		 * 
		 * @功能：上传失败时显示失败的遮罩
		 * @param：id
		 * 
		 * 通过id找出map表中的图片块，然后获得delete的块，将其显示出来
		 */

		uploadFailure: function(id){
			const _self = this;
			const imgBlock = _self.imageCache.get(id);
			const del = imgBlock.querySelector('.delete');
			del.style.display = 'block';
			console.log('uploadFailure');
		},

		/**
		 * uploadReload函数
		 * 
		 * @功能：重传图片
		 * @param：id
		 * 
		 * 通过id找出map表中的图片块，然后将delete的块隐藏掉
		 * 
		 */
		uploadReload: function(id){
			const _self = this;
			const imgBlock = _self.imageCache.get(id);
			const del = imgBlock.querySelector('.delete');
			del.style.display = 'none';
		}
	};
	return RE;
})();

RE.init();