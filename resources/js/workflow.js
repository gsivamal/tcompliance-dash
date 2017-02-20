

//TODO: multiple instances of workflow in same screen not quite good...be cautious

var workflow = (function(){

	var _contextel;
	this.create = function(elid) {
  	_contextel = d3.select(elid);
    return this;
  }
  
  var beforesaveflow = {
  	enableAll : function(){
  		alert("enableAll");
  	}
  }
  
  var aftersaveflow = {
  	disableAll : function(){
  		_contextel.selectAll("input, select, textarea, button, label").attr("disabled",true);
  		//previous error message always in the dom, so clear it, otherwise captured in the snapshot
  		_contextel.select("#message").text("");
  	},
  	
	enable : function(el){
  		_contextel.select(el).attr("disabled",null);
  	},
  	
  	disable : function(el){
  		_contextel.select(el).attr("disabled",true);
  	},
  	
  	changetext : function(el,txt){
  			_contextel.select(el)  			
  						.text(function(){  							
  							d3.select(this).attr("disabled",null); //enable it first
  							return txt;
  						});
  	},
  	
	setStatusMessage : function(resp){
		var msg = "";
		//it is json, so stringify
		if({}.constructor === resp.constructor)
			msg = JSON.stringify(resp);
		
		if(resp.message.startsWith("ERROR"))		
			_contextel.select("#message")
						.text(resp.message)
						.style("color","red");
		else
			_contextel.select("#message")
						.text(resp.message)
						.style("color","green");	
			
	}
  }

  // 1. flow1
  this.beforesave = function () {
    return beforesaveflow;
  };

  // 2. flow2
  this.aftersave = function () {
    return aftersaveflow;
  };

  //so that workflow.method() would work
  return this;
  
}());


