
//function refresh() {

//	console.log('refresh every 6000 ms');    
    
//    window.location.reload(true);
//}

//setTimeout(refresh, 7000);


!function(){
	
	if ('WebSocket' in window){
		// WebSocket is supported. You can proceed with your code
		var connection = new WebSocket('ws://127.0.0.1:4568/livereload')
		
		//alert('connected:'+ connection)
		
		connection.onmessage = function (event) {
			//alert(event.data);
			//window.location.reload(true);
			//location.reload(); //reload from cache
			//document.location.reload();
			//history.go(0);
			//$(window).trigger('hashchange');
			//navigate();
			//refresh()
			location.reload(true)
		}
		
	} else {
		//WebSockets are not supported. Try a fall back method like long-polling etc
		alert('websocket not supported, live reoload feature cannot be used!')
	}

}();