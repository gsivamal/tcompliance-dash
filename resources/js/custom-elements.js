
// for dropdown factory
var dropdown = function(){
		var 
		_select,
		_data;

		var methods = {
			// default page size
			//_pagesize : 3
		};
		
		// 1. create select object
		methods.create = function (elementid) {
			_select = d3.select(elementid);
			return methods;
		};
    
    	// 2. populate table data
		methods.populatedata = function (data) {		
			
			//need eval if you are using $.getJSON, d3.json not required
			_data = eval(data);
			
			_select.selectAll("option")
        		.remove();
			
			_select.selectAll("option")
	        	.data(_data)
	        	.enter()
	        	.append("option")
	        	 .text(function(dtm) {
					return dtm.name; 
		         })
		         .attr("value", function(dtm) {     		 	
					return dtm.id; 
		         }); 	
			
			return methods;
		};
		
		//select bind
		methods.setselected = function (val) {
			_select.each(function() {
				//console.log("In setselected:"+val);
				this.value = val; 
			});
		};
		
		//used, for dropdown callback
		methods.getselected = function () {
			var selecteddata;
				_select.selectAll("option")
    				.each(function(d) { 
    				if(this.selected === true){
    					selecteddata = d;
    					//return d;
    				}    				
    			});					 	
			return selecteddata;
			//return _select.node(); //will return selected 'value'
		};
		
		//on change callback
		methods.onchange = function (callback) {
			_select.on("change", function(){
				if(callback)
					callback();
			});
		};
		
		return methods;
};




var signature = (function(){
	
	var _canvas;

	var thiselmt = {
		// default ink black
		_fillcolor : '#0000ff',		
		penradius : 2		
	};
	
	// 1. create select object
	thiselmt.create = function (elementid) {
		
		_canvas = d3.select(elementid)
				.append("canvas")
					.attr("width","300px")
					.attr("height","100px")
					.style({"background": "#FFFFCC"});
		
		var ctx = _canvas.node().getContext("2d");
		
		var isDrawing = false;	
		
		var mouse = {x: 0, y: 0};
		var last_mouse = {x: 0, y: 0};

		//to draw dotted line		
		ctx.setLineDash([10, 5]); //wide, space between
		ctx.beginPath();
		ctx.moveTo(0,85);
		ctx.lineTo(300, 85);
	   	ctx.stroke();
	    
	   	//start draw
	   	_canvas.node().addEventListener('mousedown', function(e) {
	   		_canvas.node().addEventListener('mousemove', onPaint, false);
		}, false);
	   	
	   	//end draw
	   	_canvas.node().addEventListener('mouseup', function() {
	    	_canvas.node().removeEventListener('mousemove', onPaint, false);
		}, false);

	    window.addEventListener('mousemove', (function(e) {	    	
	            
	    	var rect = _canvas.node().getBoundingClientRect();	
		    
	    	//alert("window.mousemove:x"+ mouse.x + ",y:"+ mouse.y);

	    	last_mouse.x = mouse.x;
			last_mouse.y = mouse.y;
			
			mouse.x = e.clientX - rect.left;
			mouse.y = e.clientY - rect.top;			
		     
		}), false);
	    
	    var onPaint = function() {
	    	ctx.strokeStyle = thiselmt._fillcolor;
			ctx.beginPath();
			ctx.moveTo(last_mouse.x, last_mouse.y);
			ctx.lineTo(mouse.x, mouse.y);
			ctx.closePath();
			ctx.stroke();
		};
	   	
	};
	
	return thiselmt;
	
}());



var signature1 = function(){
	
	var canvas, ctx, blank;
	
	var methods = {
		// default ink black
		_fillcolor : '#0000ff',		
		_penradius : 2,
		_width : 300,
		_height : 100
	};	
	
	methods.getImageData = function (){		
		  var data = ctx.getImageData(0, 0, canvas.width, canvas.height);
		  return data;
	}
	
	methods.createImageData = function (data){
		 return ctx.createImageData(canvas.width, canvas.height);
	}
	
	methods.putImageData = function (data){
		  ctx.putImageData(data, 0, 0);
	}
	
	methods.isBlank = function () {
	    return canvas.toDataURL() == blank.toDataURL();
	}
	
	methods.erase = function () {	   
		var blankImageData = blank.getContext('2d').getImageData(0, 0, blank.width, blank.height);		
		ctx.putImageData(blankImageData, 0, 0);		
	}
	
	//ex: jsondata -> { "data" : { "0":100, "0":200, .... }}
	methods.putJsonImageData = function(jsondata){
		
		var arr = []; 
		for(var p in Object.getOwnPropertyNames(jsondata.data)) {
		    arr[p] = jsondata.data[p];
		}
		
		var unit8arr = new Uint8ClampedArray(arr);
		var imgdata = new ImageData(unit8arr, canvas.width, canvas.height);
		
		ctx.putImageData(imgdata, 0, 0);
	}

	methods.create = function (canvasdivid, text){
		
			function cloneCanvas(oldCanvas) {
	
			    //create a new canvas
			    var newCanvas = document.createElement('canvas');
			    var newContext = newCanvas.getContext('2d');
	
			    //set dimensions
			    newCanvas.width = oldCanvas.width;
			    newCanvas.height = oldCanvas.height;
	
			    //apply the old canvas to the new one
			    newContext.drawImage(oldCanvas, 0, 0);
	
			    //return the new canvas
			    return newCanvas;
			}
		
			var isDrawing = false;	
			
			var mouse = {x: 0, y: 0};
			var last_mouse = {x: 0, y: 0};
	
			// Create the canvas (Neccessary for IE because it doesn't know what a canvas element is)
			var canvasDiv = document.getElementById(canvasdivid);
			canvas = document.createElement('canvas');
			canvas.setAttribute('width', methods._width);
			canvas.setAttribute('height', methods._height);
			canvas.style.border = "1px solid white";
			canvasDiv.appendChild(canvas);
			
			var clearbtn = document.createElement('button');
			clearbtn.appendChild(document.createTextNode("Clear")); 
			clearbtn.style.display = "flex";
			clearbtn.style.height = "18px";
			clearbtn.style.fontSize = "x-small";
			clearbtn.style.padding = "0px";
			clearbtn.style.margin = "1px";
			clearbtn.onclick = (function(){
				methods.erase();
			});
			//style="display:flex;height:18px;font-size:75%;padding:0px;"
			canvasDiv.appendChild(clearbtn);
			
			var rect = canvas.getBoundingClientRect();
			
			if(typeof G_vmlCanvasManager != 'undefined') {
				canvas = G_vmlCanvasManager.initElement(canvas);
			}
			
			ctx = canvas.getContext("2d");

			//to draw dotted line		
			ctx.setLineDash([10, 5]); //wide, space between
			ctx.beginPath();
			ctx.moveTo(0,85);
			ctx.lineTo(300, 85);
		   	ctx.stroke();
		   	
		   	var textwidth = ctx.measureText(text).width;
		   	
		   	//ctx.textAlign = "right";
		   	//ctx.textBaseline = "bottom";
		   	ctx.fillText(text,(290-textwidth),96);
		   	
			//snapshot the empty canvas
			blank = cloneCanvas(canvas);		   	
			
			function drawstart(e) {	
				
				var rect = canvas.getBoundingClientRect();	

				mouse.x = e.clientX - rect.left;
				mouse.y = e.clientY - rect.top;	
			    
		    	last_mouse.x = mouse.x;
				last_mouse.y = mouse.y;
			
				isDrawing = true;
				
			}
			
			function movestart(e) {	
			
			    var rect = canvas.getBoundingClientRect();	

				mouse.x = e.changedTouches[0].pageX - rect.left;
				mouse.y = e.changedTouches[0].pageY - rect.top;	
			    
		    	last_mouse.x = mouse.x;
				last_mouse.y = mouse.y;
			
				isDrawing = true;
				
			}
			
			function enddraw(e) {		    
			
				isDrawing = false;
				
			}
			
			function mouse_draw(e) {	
			
				if(isDrawing){	    
				    var rect = canvas.getBoundingClientRect();	
				    
			    	last_mouse.x = mouse.x;
					last_mouse.y = mouse.y;
					
					mouse.x = e.clientX - rect.left;
					mouse.y = e.clientY - rect.top;		
				    
				    ctx.strokeStyle = '#0000ff';
					ctx.beginPath();
					ctx.moveTo(last_mouse.x, last_mouse.y);
					ctx.lineTo(mouse.x, mouse.y);
					ctx.closePath();
					ctx.stroke();
				}		
			}
			
			function touch_move(e) {	 
			
				if(isDrawing){	     
				    var rect = canvas.getBoundingClientRect();	
				    
			    	last_mouse.x = mouse.x;
					last_mouse.y = mouse.y;
					
					mouse.x = e.changedTouches[0].pageX - rect.left;
					mouse.y = e.changedTouches[0].pageY - rect.top;		
				    
				    ctx.strokeStyle = '#0000ff';
					ctx.beginPath();
					ctx.moveTo(last_mouse.x, last_mouse.y);
					ctx.lineTo(mouse.x, mouse.y);
					ctx.closePath();
					ctx.stroke();	
				}		    
			}
			
			window.addEventListener('mousedown', drawstart, false);	
			window.addEventListener('touchstart', movestart, false);
				
			window.addEventListener('mousemove', mouse_draw, false);		
			window.addEventListener('touchmove', touch_move, false);
			
			window.addEventListener('mouseup', enddraw, false);
	        window.addEventListener('mouseout', enddraw, false);
	        window.addEventListener('mouseleave', enddraw, false);
	        
	        window.addEventListener('touchend', enddraw, false);
	        window.addEventListener('touchcancel', enddraw, false);
			
		
		return methods;
    
	} //create 
  
  
	return methods;
};



var multiselectd3 = function(){
	var 
	_containerdiv,
	_itmdiv,
    _toggle;       

	var methods = function() {
		// default values  
		_toggle:false
	};
	
	// 1. create select object
	methods.create = function (elementid, containerid) {
		
		_multiinput = d3.select(elementid);		
	
	    _multiinput.on("click", function(){        
             if(_toggle){
            	 _multiinput.text(_multiinput.text().replace("-","+"));
            	 _containerdiv.style("display","none")
             }else{
            	 _multiinput.text(_multiinput.text().replace("+","-"));
            	 _containerdiv.style("display","block");
             }
             _toggle = !_toggle;
	    })
				        		
		_containerdiv = d3.select(containerid);
        
		return methods;
	};     

	// 2. populate data
	methods.populatedata = function (itemdata) {
		
		  _itmdiv = _containerdiv.selectAll("div")
	   		.data(itemdata)
	   		.enter()
	  			.append("div");
	   
			_itmdiv.append("input")
		       	.attr("type", "checkbox")
		       	.attr("id", function(d,i) { 
		       			return 'id'+i; 
		           })
		  		.on("click", function(e){
		               
		  		var selectedrows = [];
	               _containerdiv.selectAll("div")
					 .selectAll("input")
					 .each(function(d) { 
						if(d3.select(this).node().checked){
							//console.log("data:"+JSON.stringify(d))
							//console.log("data.id:"+d.id)
							selectedrows.push(d.id); 
						}	
	               	});	
	                
					//_alinkselection.text("("+selectedrows.length+") selected");
					_multiinput.text("(-)  "+selectedrows.length+" selected");
					
					//console.log("selectedrows:"+selectedrows)
					
					_multiinput.attr("value",selectedrows);
		  	})
		   
		    _itmdiv.append("label")
		   		.attr("for", function(d,i) { 
		       			return 'id'+i;  //it was 'a' before
		        })
		  		.text(function(d){
		       		return d.name;
		  	})
		  	
		  	return methods;
	  
    };
    
    // 3. rebind data
    methods.setselected = function (ids) {
		
		  _containerdiv.selectAll("div")
		  			.selectAll("input")
					 .each(function(dtm) {
						 var inputel = d3.select(this).node();	
						 ids.csvToArray().forEach(function(currid, index){
							 if(dtm.id === currid){
								 inputel.checked = true;
							 }
						 });
					 });			  
		  			_multiinput.text("(-)  "+ids.csvToArray().length+" selected");
		 return methods;			
    };
    
    // 4. rebind data
    methods.disable = function (ids) {
		
		  _containerdiv.selectAll("div")
		  			.selectAll("input")
					 .each(function(dtm) {
						 d3.select(this).attr("disabled",true);
					 });			  
    };
	
	return methods;
};


var combobox3 = function(){

    var _labellist,
        _itmlabel,
        _toggle;       

    var methods = function() {
      // default values  
      _toggle:false
    };
	
    // 1. create select object
    methods.createcombo = function (inputid, listid) {

          _inputel = d3.select(inputid);
       		_labellist = d3.select(listid);

          _inputel.on("click", function(){        
               if(_toggle){
                 _labellist.style("display","none");
              }else{
                 _labellist.style("display","block");
               }
               _toggle = !_toggle;
        	}).on("keyup", function(){        
        		var enteredtext = this.value;
   					 		_labellist.selectAll("label")
          					.filter(function(d) {
					    				var loc = new String(d.name);
					                    if(loc.toUpperCase().startsWith(enteredtext.toUpperCase())){                      	
					                     		d3.select(this)
					                        		.style("color","red");
					                    }
   										return true; //???
					   			});
         
       					if(enteredtext.length == 0){
              		 			_labellist.selectAll("label")                                          									
          									.style("color","black");
              		 			_labellist.style("display","none");
       					} else if(enteredtext.length == 1){
       							_labellist.style("display","block");
       					}
       						
          			if(d3.event.keyCode == 13){
   	        			console.log("entered...");
           			}
        	});
          
        	return methods;
    }

    // 2. populate data
    methods.populatecombodata = function (itemdata) {

        _itmlabel = _labellist.selectAll("label")
          .data(itemdata)
          .enter()
          	.append("label") 
          	.attr("id", function(d){
          		return d.id;
          	})
            .text(function(d){
                return d.name;
            });
        
        _itmlabel.on("click", function(){
            		//console.log(_inputel.node());
                
                d3.select(this.parentNode).selectAll("label").style("color","black");
                d3.select(this).style("color","red");

                _inputel
                  	.attr("value", d3.select(this).text());
                 // the above line, not updating the dom, though the debug messages the value bring set, so 
                 // do with raw plain javascript :0(, do not know where the problem is still, but works :o)
                document.getElementById("headquarters").value = d3.select(this).text();
                
                //minimize after selection
                _labellist.style("display","none");
            });
    }
    
    return methods;
    
  } //combobox3

//clicking outside multi-select close the element, need to work this later..
//$(document).bind('click', function (e) {
//    var $clicked = $(e.target);
//    if (!$clicked.parents().hasClass("dropdown")) $(".dropdown dd ul").hide();
//});



