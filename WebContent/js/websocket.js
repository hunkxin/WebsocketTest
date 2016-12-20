/**
 * 
 */
var webSocket = null;
var tryTime = 0;
$(function () {
	initSocket();

	window.onbeforeunload = function () {
	//离开页面时的其他操作
	};
});

/**
 * 初始化websocket，建立连接
*/
function initSocket() {
	if (!window.WebSocket) {
		alert("您的浏览器不支持websocket！");
		return false;
	}

	webSocket = new WebSocket("ws://127.0.0.1:8080/websocket.ws/"+ relationId +"/"+ userCode);

	// 收到服务端消息
	webSocket.onmessage = function (msg) {
		console.log(msg);
	};

	// 异常
	webSocket.onerror = function (event) {
		console.log(event);
	};

	// 建立连接
	webSocket.onopen = function (event) {
		console.log(event);
	};

	// 断线重连
	webSocket.onclose = function () {
		// 重试10次，每次之间间隔10秒
		if (tryTime < 10) {
			setTimeout(function () {
				webSocket = null;
				tryTime++;
				initSocket();
			}, 500);
		} else {
			tryTime = 0;
		}
	};
}