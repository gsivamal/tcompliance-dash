var VERSION = "1.0"

var colorarr = [ "peru", "steelblue", "yellowgreen", "#b87333", "teal", "blueviolet", "cornflowerblue", "silver", "darkgoldenrod", "darkgoldenrod", "violet", "coral", "palevioletred" ];

//	["Joe", 40, "#b87333"],
//	["Darryl", 55, "silver"],
//	["Mike", 90, "gold"],
//	["John", 25, "color: steelblue"]


var daysarr = ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];

var monthsarr = ["","Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ]; 
var monthsfullarr = ["", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" ];

var utildate = new Date();

var utiltodaystr = (utildate.getMonth()+1) + "/" + utildate.getDate() + "/" + utildate.getFullYear();
var utiltodaydate = Date.parse(utiltodaystr);

var Current = {
		driver : null,
		equip : null,
		company : null,
	    mvrid : 0,
	    drugtestid : 0,
	    
	    date : utildate.getDate(),
	    year : utildate.getFullYear(),
	    month : monthsarr[utildate.getMonth()],
		monthidx : utildate.getMonth()
	};


var getTimeStep15 = function(){	
	var today = new Date();
	var timeToReturn = new Date();

    timeToReturn.setMilliseconds(Math.round(today.getMilliseconds() / 1000) * 1000);
    timeToReturn.setSeconds(Math.round(timeToReturn.getSeconds() / 60) * 60);
	timeToReturn.setMinutes(Math.round(timeToReturn.getMinutes() / 15) * 15);

	return timeToReturn;
}

var format24Hour = function(currtime){	
	var caloptions = { hour12: false, hour: "numeric", minute : "numeric" };
	return (currtime.toLocaleString('en-US', caloptions));
}


//-> move this to logbook impl later..

	var cellwidth = 36; //const
	var cellblockwidth = 9; //cons
	
	var calculateWidth = function(){	
	
		var currtime = getTimeStep15();
		
		console.log("curr time:"+ currtime);	
		
		var mins = currtime.getMinutes();	
		var cellwidth = currtime.getHours() * 36;
		if(mins == 15){	cellwidth = cellwidth + 9;
		} else if(mins == 30){ cellwidth = cellwidth + 18;
		} else if(mins == 45){ cellwidth = cellwidth + 27; //todo: later do multuplication..
		}
		
		console.log("Cell width:"+ cellwidth);
		
		var options = { hour12: false, hour: "numeric", minute : "numeric" };
		console.log(currtime.toLocaleString('en-US', options));
		
		return { "time" : currtime.toLocaleString('en-US', options), "width" : cellwidth };
	}

//<-

var daysarr = ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
var monthsarr = ["-","Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ]; 

//** DATE PROTOTYPES ** //
(function () {

	if (typeof Date.prototype.getMonthIdx != 'function') {
  		Date.prototype.getMonthIdx = function (){
			return (this.getMonth()+1);
		};
	}
	
	if (typeof Date.prototype.getMonthText != 'function') {
		Date.prototype.getMonthText = function (){
			return monthsarr[this.getMonthIdx()];
		};
	}
	  
	if (typeof Date.prototype.formatDate != 'function') {
		Date.prototype.formatDate = function (){
	      	//var datestr = ("0"+this.getMonthIdx()).slice(-2) + "/" + ("0"+this.getDate()).slice(-2) +"/" + this.getFullYear();
			var datestr = this.getMonthIdx() + "/" + this.getDate() +"/" + this.getFullYear();
			return datestr;
		};
	}
	 
	if (typeof Date.prototype.getYearMonth != 'function') {
		Date.prototype.getYearMonth = function (){
			return this.getFullYear() + "-" +this.getMonthIdx();
		};
	}
	 
	if (typeof Date.prototype.getDayOfWeek != 'function') {
		Date.prototype.getDayOfWeek = function (){
			return daysarr[this.getDay()];
		};
	}
	
	//var date_subtract = new Date().minusDate(-10),
	if(!Date.prototype.minusDate){
	    Date.prototype.minusDate = function(days){
	        var today = new Date();
	        this.setTime(today.getTime()-(days*24*3600000));
	        return this;
	    };
	}

	//not used, delete later
	if (typeof Date.prototype.displayDate != 'function') { //note this work only within months
		Date.prototype.displayDate = function (){
			var datestr = this.getMonthText() + " " + this.getDate() +", " + this.getFullYear();
			return datestr;
		};
	} 
 

}());

	

//** ARRAY PROTOTYPES ** //
(function () {
	
	//to get unique values from Array, works for int and string values
	if (typeof Array.prototype.unique != 'function') {
		Array.prototype.unique = function() {
			var uq = this.filter(function(elem, index, self) {
		    return index == self.indexOf(elem);
			});
		  return uq;
		};
	}
	
}());
	
// ** STRING PROTOTYPES ** //
(function () {
	if (typeof String.prototype.startsWith != 'function') {
		String.prototype.startsWith = function (str){
			return this.indexOf(str) === 0;
		};
	}
	if (typeof String.prototype.insert != 'function') {
		String.prototype.insert = function (index, string) {
		  if (index > 0)
		    return this.substring(0, index) + string + this.substring(index, this.length);
		  else
		    return string + this;
		};
	}
	if (typeof String.prototype.paddingLeft != 'function') { //char filler,  ex: hour.paddingLeft("0000")
		String.prototype.paddingLeft = function (paddingValue) {
			return String(paddingValue + this).slice(-paddingValue.length);
		};
	}
	if (typeof String.prototype.removeAll != 'function') {
		String.prototype.removeAll = function(search) {
		    var target = this;
		    return target.split(search).join("");
		};
	}
	if (typeof String.prototype.replaceAll != 'function') {
		String.prototype.replaceAll = function(oldchar, newchar) {
		    var target = this;
		    return target.split(oldchar).join(newchar);
		};
	}	
	
	if (typeof String.prototype.csvToArray != 'function') {
		String.prototype.csvToArray = function() { //to convert "1,2,3" to ["1","2","3"]
			var result = [];
			var tokens = this.split(",");
			for(var i=0; i<tokens.length; i++){
			  result.push(tokens[i]);
			}	  
			return result; //JavaScript object
		}
	}
	
}());


function toggle(x, callback){
    var panel = document.getElementById(x);
    if(panel.style.display === "block"){
        panel.style.display = "none";
    } else {
        panel.style.display = "block";
        panel.style.marginTop = "25px";
        //loadtasks();
        if (callback) {
        	callback();
        }
    }
}

//not in use now, will be used in future..
//loadScript("js/javascript.js");
//loadScript("http://sweet.watermelonduck.com/js/main.js");

var jsArray = {};
var loadScript = function (src, callback) {
	
	var element = document.createElement("script");
		element.src = src;
		element.async = true;
		document.body.appendChild(element);
		
		if (callback) {
		
			element.onreadystatechange = function () {
				if (this.readyState === "loaded" || this.readyState === "complete") { 
					callback();
				}
			};
			element.onload = callback;
		}
};



//to blink a label
(function(){
	  var show = 'visible'; // state var toggled by interval
	  var time = 1000; // milliseconds between each interval

	  setInterval(function() {
	    // Toggle our visible state on each interval
	    show = (show === 'hidden') ? 'visible' : 'hidden';

	    // Get the cursor elements
	    var cursors = document.getElementsByClassName('blink');
	    // We could do this outside the interval callback,
	    // but then it wouldn't be kept in sync with the DOM
	    
	    //console.log(cursors);

	    // Loop through the cursor elements and update them to the current state
	    for (var i = 0; i < cursors.length; i++) {
	      cursors[i].style.visibility = show;
	    }
	  }, time);
	})()

//TODO: the replaceAll method can be used instead this one..
var replaceAllColonWithNewLine = function(value) {
	return value.replace(/\:/g,"\n");
}

// used in d3 table
// example: jsondoc: { "product:{id:1, name:text} "}
// example: path : "product.id"
var jsonpath = function(jsondoc, path) {
	if (path!= null){
	    var parts = path.split('.');
	    var partslen = parts.length;
	    for(var i = 1; i <= partslen; i++) {
	          jsondoc = jsondoc[parts.shift()];
	    }
	    return jsondoc;
	} else {   
    	return;
   	}
}

//example:jsonarray: [{id:1,name:bob},{id:2,name:Joe}...]
//example:key:id
//example:value:2
var jsonquery = function(jsonarr, key, value) {
	var jdoc = {};
	for(var i = 0; i < jsonarr.length; i++) {
		jdoc = jsonarr[i];
		if(jdoc[key] === value){
			break;
		}
	}
	return jdoc;
}




var dataURLtoBlob = function(dataURI) {
    // convert base64/URLEncoded data component to raw binary data held in a string
    var byteString;
    if (dataURI.split(',')[0].indexOf('base64') >= 0)
        byteString = atob(dataURI.split(',')[1]);
    else
        byteString = unescape(dataURI.split(',')[1]);

    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

    // write the bytes of the string to a typed array
    var ia = new Uint8Array(byteString.length);
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }

    return new Blob([ia], {type:mimeString});
}



//to convert "1,2,3" to ["1","2","3"]
//var csvJSON = function (csv){
//	  var result = [];
//	  var tokens = csv.split(",");
//	  for(var i=0; i<tokens.length; i++){
//		  result.push(tokens[i]);
//	  }	  
//	  return result; //JavaScript object
	  /////return JSON.stringify(result); //JSON
//	}

// for table sorting, similar to d3.ascending(later use d3.ascending, but use this time being)
var ascending = function(a, b) {
	if(typeof a === 'string'){
		a = a.toLowerCase();
		b = b.toLowerCase();
	}
	return a > b ? 1 : a == b ? 0 : -1;
}

//replace NaN
var replaceNaN = function(curText){
	
	  var nanChars = [];					  
	  
	  // make NaN chars list
	  for (var i = 0; i < curText.length; i++) {
	    var curChar = curText.charAt(i)
	    if( isNaN(curChar) === true ){
	      nanChars.push(curChar);
	    }
	  }						
	  
	  // replace them from entered
	  for (var i = 0; i < nanChars.length; i++) {
		var nanChar = nanChars[i]
	    curText = curText.replace(nanChar,'');
	  }
	  
	return curText;
}


//for formatter factory
var formutil = (function(){	

		var thisformutil = {			
			_phoneformat : /(\d{3})(\d{3})(\d{4})/,
			_emailformat : /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/,
			_ssnformat : /(\d{3})(\d{2})(\d{4})/,
			_dateformat: /(\d{2})(\d{2})(\d{4})/,
			_hhmmformat: /(\d{2})(\d{2})/
		}
		
		// 1. validate form 
		thisformutil.validateInputFields = function(dialogid) {
			var is_error = false
			var dialog = d3.select(dialogid)

			//salai: not so efficient, loops all the elements, need to check later 
			dialog
				.selectAll("input, textarea")
				.each(function(dtm) { 
					if(this.dataset.validation === 'mandatory'){
						if(this.value.trim() === ''){
							d3.select(this).style("border-right", "3px solid red");
							is_error = true;
						}else{
							d3.select(this).style("border-right", "3px solid green");
						}
					}
					
					//->for date validations
					if(this.value.trim().startsWith("*")){ 
						d3.select(this).style("border-right", "3px solid red");
						is_error = true;
					}
					//<-
				})
				
			dialog
				.selectAll("label")
				.each(function(dtm) { 
					if(this.dataset.validation === 'mandatory'){
						if(d3.select(this).text().trim() === '(+)  0 selected'){					
							d3.select(this).style("border-right", "3px solid red")
							is_error = true
						}else{
							d3.select(this).style("border-right", "3px solid green")
						}
					}						
				})
				
			return is_error
		}
		
		//2. serialize Object
		thisformutil.serializeObject = function(dialogid) {			
			var o = {}
			var dialog = d3.select(dialogid)
			//type='file' serialization is separate call
			dialog
				.selectAll("input[type='text'], select, textarea, input[type='password']")
					.each(function(dtm) { 
						if(this.dataset.dbcolname != undefined) {
							//console.log(this.dataset.dbcolname +":"+ this.value)
							o[this.dataset.dbcolname] = this.value; //name not supported for dl elements, so using id		        
						}
					});
			
			dialog
				.selectAll(".multiselect").select("label") // check box multi-select only, refer listbox binding in new_training.html
					.each(function(dtm) { 
						if(this.dataset.dbcolname != undefined) {
							o[this.dataset.dbcolname] = d3.select(this).attr("value")+"".csvToArray(); //name not supported for dl elements, so using id		        
						}
					});
			
			dialog
				.selectAll("input[type='radio']:checked")
					.each(function(dtm) { 
						if(this.dataset.dbcolname != undefined) {
							var valueselected = d3.select(this).node().value;
							o[this.dataset.dbcolname] = valueselected+"";	        
						}
					});		
			
			dialog
				.selectAll("input[type='checkbox']")
					.each(function(dtm) { 
						if(this.dataset.dbcolname != undefined) {
							var ischecked = d3.select(this).node().checked;
							o[this.dataset.dbcolname] = ischecked+"";	        
						}
					});	
			
			dialog
				.selectAll(".dblabelfield")
					.each(function(dtm) { 
						if(this.dataset.dbcolname != undefined) {
							o[this.dataset.dbcolname] = d3.select(this).text(); //calculated fileds are label, those also need to be saved, ex, in timecard
						}
					});				
					
			return o
		}
		
		//3. to serialize file upload fields
		//salai: works for only single file upload
		thisformutil.serializeFileObject= function(dialogid) {
			var imgData = new FormData(); // FormData doesn't work on IE before IE10
			var dialog = d3.select(dialogid)				
			dialog
				.selectAll("input[type='file']")
					.each(function(dtm) {
						if(this.dataset.dbcolname != undefined) {
							var files = d3.select(this).node().files;
							if(files.length > 0){
								imgData.append(this.dataset.dbcolname, files[0], files[0].name);	
							}												        
						}
					});
					
			return imgData
		}
		
		//rebind data for edit 
		thisformutil.rebind = function(formid, data) {

			var pageform = d3.select(formid);
			
			pageform.selectAll("input[type='text']")			
				.attr("value",function() { 
					return data[this.dataset.dbcolname]
				});
			
			pageform.selectAll("textarea")
				.text(function() { 
					return data[this.dataset.dbcolname]
				});
			
			pageform.selectAll("span") // TODO: need more filter ?
				.text(function() { 
					return data[this.dataset.dbcolname]
				});
			
			pageform.selectAll("input[type='checkbox']")
				.attr("checked", function() { 
					if(data[this.dataset.dbcolname] === "true"){
						return true;
					}else{
						return null; //null to make unchecked
					}
				});
			
			pageform.selectAll("input[type='radio']")
				.each(function(dtm){
					
					var thisredio = d3.select(this).property("value");
					//radio selection
					if(thisredio === data[this.dataset.dbcolname]){
						d3.select(this).property("checked",true);
					}
				});			
					
			
			//select bind
			//pageform.select("#state")
			//	.each(function() {
			//		var selectdbcolname = this.dataset.dbcolname;  
			//		this.value = data[selectdbcolname]; 
			//	});		
			
			//for files- remove the mandatory check during update(salai:quick fix)
			pageform.selectAll("input[type='file']")
				.each(function(dtm) {
					this.dataset.validation = null
				});
			
			pageform.selectAll(".dbdatefield")
						.attr("value",function() { 
							return data[this.dataset.dbcolname]
						});
			
		}
		
		
		thisformutil.checkboxcount = function(formid){
			var chkcount = 0;
			d3.select(formid)
				.selectAll("input[type='checkbox']")
					.each(function(){
						if(this.dataset.dbcolname != undefined) { //count only db enabled checkboxes
							if(d3.select(this).node().checked == true){
								chkcount = chkcount + 1;	
							}
						}	
				});
			return chkcount;
		}
		
		// 1. phone format
		thisformutil.formatfields = function (dialogid) {
			
				d3.select(dialogid)
					// salai: working code, to avoid non numerical char typing
					//.on("keyup", function() {
					//	var curChar = String.fromCharCode(d3.event.keyCode)
					//	if( isNaN( curChar ) === true ){
					//    var temp = d3.select(this).property("value").toUpperCase()
					//		this.value = temp.replace(curChar,'')
					//    d3.event.stopPropagation()
					//  }
					//})
				
				d3.select(dialogid)
					.selectAll("input[type='text']")
						.datum(function() {
							
							//alert("this.dataset.format:"+this.dataset.format)
							
							//us phone format							
							if(this.dataset.format === 'usphone'){
								d3.select(this)
									.on("blur", function() {
										var curText = replaceNaN(this.value);						
										if(curText > 0){
										 this.value = curText.replace(thisformutil._phoneformat, "$1-$2-$3");
										 if(this.value.length != 12){
											 alert("Invalid phone format, please verify !");
											 this.focus();
											 return false;
										 }
									  }
									  return true;
								});
							} else if(this.dataset.format === 'email'){  //email								
								d3.select(this)
									.on("blur", function() {
									  var curText = this.value;	
									  if(curText.length > 0){
										  if (thisformutil._emailformat.test(curText) === false){ 
											  alert('Invalid Email Address');
											  this.focus();
											  return false;
										  }
									  }
									  return true;									  
								});								
							} else if(this.dataset.format === 'usssn'){  //usssn	
								d3.select(this)
								.on("blur", function() {
									var curText = replaceNaN(this.value);
									if(curText > 0){
										this.value = curText.replace(thisformutil._ssnformat, "$1-$2-$3");
										if(this.value.length != 11){
											alert("Invalid ssn format, please verify !");
											this.focus();
											return false;
										 }
									  }
									  return true;
								});
							} else if(this.dataset.format === 'hhmm'){  //hhmm, need to test this, not used, thought use in timecard, but that is table, not input field	
								d3.select(this)
									.on("blur", function() {
										var curText = replaceNaN(this.value);				
										if(curText > 0){
											
												curText = curText.paddingLeft("0000");
												
											  var hour = parseInt(curText.substr(0,2));
											  var minute = parseInt(curText.substr(2));
											  
											  //alert("hour:"+ hour + ", min: "+minute);
											  
											  var amPM = (hour > 11) ? "PM" : "AM";
											  if(hour > 12) {
											    hour -= 12;
											  } else if(hour == 0) {
											    hour = "12";
											  }
											  if(minute < 10) {
											    minute = "0" + minute;
											  }
											  
											  this.value = hour + ":" + minute + " " + amPM;
											
										}
										return true;
									});
							} else if(this.dataset.format === 'floatfield'){  //hhmm, need to test this, not used, thought use in timecard, but that is table, not input field	
								d3.select(this)
									.on('input', function() {
									  		this.value = this.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');
									});
							}
					});			
		};
		
		return 	thisformutil	
}());	



var exportpdf = function(formid, filename){
	  
		//save as pdf
		html2canvas($(formid), {
					useCORS:true,
			        onrendered: function(canvas) {				            
			            var imgData = canvas.toDataURL("image/jpg");
			            
						/* Here are the numbers (paper width and height) that I found to work. 
							It still creates a little overlap part between the pages, but good enough for me.
							if you can find an official number from jsPDF, use them. */
							
						var imgWidth = 200; 
						var pageHeight = 250;  
						var imgHeight = canvas.height * imgWidth / canvas.width;
						var heightLeft = imgHeight;
						
						var doc = new jsPDF('p', 'mm', 'a4');
						var position = 0;
						
						doc.addImage(imgData, 'PNG', 2, position, imgWidth, imgHeight);
						heightLeft -= pageHeight;
						
						while (heightLeft >= 0) {
							position = heightLeft - imgHeight;
							doc.addPage();
							doc.addImage(imgData, 'PNG', 2, position, imgWidth, imgHeight);
							heightLeft -= pageHeight;
						}
						doc.save(filename); 				
					}//onrendered
			});	
  
}

function deleteImagePreview(previewpanel){
	var imgs = document.getElementById(previewpanel)
			.getElementsByTagName("img");
	
	if(imgs.length > 0){		
		if(confirm("Are you sure you want to remove the photos?")){
			$('#'+previewpanel).empty();
		}
	}
}

function handleImageFiles(files, previewpanel) {
  
	for (var i = 0; i < files.length; i++) {
		var file = files[i];
		var imageType = /^image\//;
    
		if (!imageType.test(file.type)) {
			continue;
		}
    
	    var img = document.createElement("img");
	    	img.height = 100;
	    	img.width = 200;
	    	img.classList.add("obj");
	    	img.file = file;
	    
	    var preview = document.getElementById(previewpanel);
	    	preview.appendChild(img); // Assuming that "preview" is the div output where the content will be displayed.
	    
	    var reader = new FileReader();
	    	reader.onload = (function(aImg) { return function(e) { aImg.src = e.target.result; }; })(img);
	    	reader.readAsDataURL(file);
	}
	
}	




