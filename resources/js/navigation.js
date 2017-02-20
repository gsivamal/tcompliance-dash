
// Updates dynamic content based on the fragment identifier.
  function loadpage(fragmentId, callback){

	// Isolate the fragment identifier using substr.
    // This gets rid of the "#" character.
    //var fragmentId = location.hash.substr(1);
	  
	//  var fragmentId = location.substr(1);  

   // console.log('navigate called..'+fragmentId	);  
    
	$.get(fragmentId, function (content) {
		$("#content").hide().html(content).fadeIn('slow');
	})
	.done(function(){
		
		//callback is optional
		if(callback){
			callback();
		}
		
	});
	
  }  

  
function loadtab(url, callback){ 
	
	$.get(url, function(content) {
		$("#tabcontent").hide().html(content).fadeIn('slow');
	}).done(function(){
		
		//to support optional, just in case..
		if(callback){
			callback();
		}
		
	});	
}
  
function edittab(url, callback){ 

	$.get(url, function (content) {
		$("#tabcontent").hide().html(content).fadeIn('slow');
	})
	.done(function(){
		
		//to support optional, just in case..
		if(callback){
			callback();
		}
		
	});		
		
}


function loadsidetab(url, callback){ 
	
	$.get(url, function(content) {
		$("#sidetabcontent").hide().html(content).fadeIn('slow');
	}).done(function(){
		
		//to support optional, just in case..
		if(callback){
			callback();
		}
		
	});	
}