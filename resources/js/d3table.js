/**
 *  Salai: this is still factory I believe..try
 *  
 *  Usage: table.createtable("#product_table").setcolumnnames(columns).settabledata(data)
 */

// without pager/page sizes
var simpletable = function(){
		
		var 
		_table,
		_tbody,
		_columnnames = [],
		_tabledata,		
		_pagesize = 3,
		_currentpageno;
		
		var method = {};
		
		// 1. create table object
		method.createtable = function (tablesource) {
			_table = d3.select(tablesource);
			_tbody = _table.append("tbody");
			
			// set column names
			_table.select("thead").select("tr").selectAll("th")
						.datum(function() {    											
							_columnnames.push(this.dataset.colname)
							return this.dataset.colname; 
						});
			
			return method;
		};
		
		//3. populate table data
		method.populatedata = function (tabledata) {			
			_tabledata = tabledata;
			// create a row for each object in the data
		    var rows = _tbody.selectAll("tr")
		        .data(_tabledata)
		        .enter()
		        .append("tr");
		    // create a cell in each row for each column
		    var cells = rows.selectAll("td")
		        .data(function(rw) {        
		            return _columnnames.map(function(column) {
		                return {column: column, value: jsonpath(rw, column)};  //rw[column]
		            });
		        })
		        .enter()
		        .append("td")
		            .text(function(dtm) {   
		                return dtm.value; });
		};
		
		//optional - 1. redraw function
		method.redraw = function () {			
			method.settabledata(_tabledata);	
		};		
		
		
		return method;
	}



	var table = function(){
			var
			_table,
			_tbody,
			_tabledata,
			_pagesize = 50;
			
			_columnnames = [];
			
			var _pagerid,
				_inputfilterid;

			var method = {};				
						

			// 1. create table object
			method.createtable = function (tablesource) {
				_table = d3.select(tablesource);
				_tbody = _table.append("tbody");
				
				// set column names
				_columnnames = [];				
				_table.select("thead").select("tr").selectAll("th")
							.datum(function() {    											
								_columnnames.push(this.dataset.colname)
								return this.dataset.colname; 
							});
				
				return method;
			};

			//3. populate table data
			method.populatedata = function (tabledata) {
				_tabledata = tabledata;		

				return method;						
			}
			
			//get selected column ids
			method.getselectedids = function () {
			
				var selectedrows = [];

    			_tbody.selectAll("tr[selected='true']")
        			.each(function(d) { selectedrows.push(d); });					 			
										
				return selectedrows;						
			}
			
			// get selected row
			method.getselectedrow = function () {
				
				var selectedrow = _tbody.selectAll("tr[selected='true']");
										
				return selectedrow;							
			}

			//populate table data (internal)
			method.makerows = function (tabledata) {

				// not sure why selectAll instead select ?
				_table.selectAll("tbody").selectAll("tr")
												.remove();

				// create a row for each object in the data
				var rows = _tbody.selectAll("tr")
					.data(tabledata)
					.enter()
					.append("tr")
						.on("click", function(d,i){

								//table id - you get the last created table id...not sure why :(
								//console.log('_table.id:'+ _table.attr("id"));

								//comment to enable multi row selection 
								_tbody.selectAll("tr")
									.style("background-color", null)
									.attr("selected", false);
									
								var existing = d3.select(this).style("background-color");
								var thisrow = d3.select(this);
								
								//console.log('thisrow:'+ JSON.stringify(thisrow) );
								//console.log("thisrow.background-color:" + existing );
								
								thisrow
									.style("background-color", function(d){
										if(existing !== 'rgb(192, 192, 192)' ){  //rgb for lightgray
											thisrow.attr("selected",true); //salai: make an array push to that...
											return "lightgray";
										}else{
											thisrow.attr("selected", false);
											return null;
										}
																		
								});
						})

				// create a cell in each row for each column
				var cells = rows.selectAll("td")
					.data(function(rw) {
						return _columnnames.map(function(column) {
							return {column: column, value: jsonpath(rw, column)};  //rw[column]
						});
					})
					.enter()
					.append("td")
						.attr("align","center")
						.filter(function(dtm) {
						
							//console.log('driver.column:'+ dtm.column)
							//console.log('dtm.value:'+ dtm.value)

							if(dtm.column.startsWith("sum(")){										
									var trdata = d3.select(this.parentNode).datum()
									// for column sum(alerts), need to get dtm.alerts value from json, not dtm.sum(alerts) :(
									var key = dtm.column.split("(").pop().split(")").shift();
									
									d3.select(this)									    	
										.append("a")
										    .attr("xlink:href","#")
										 	.on("click", function(d, i){
										        alert(replaceAllColonWithNewLine(trdata[key]));
										    })
										    .text(function(){
										    	if(trdata[key]!=undefined)
										    		return "(" + trdata[key].split(':').length + ")";
									    	});

							} else if(dtm.column.startsWith("append(")){										
								var trdata = d3.select(this.parentNode).datum();
								
								// for column append(fn,ln), need to get dtm.fn, dtm.ln value from json, not dtm.append(fn,ln) :(
								var key = dtm.column.split("(").pop().split(")").shift();
								
								var tokens = key.split(",");
									d3.select(this)									    	
										    .text(function(){
										    		var names = [];
										    		for(var i=0; i < tokens.length; i++){
										    			names.push(" " + trdata[tokens[i]] );
										    		}
										    		return names.join(" "); //return array concats the value, join to remove the csv 
									    	});
								    	
							} else if(dtm.column.startsWith("file(")){										
								var trdata = d3.select(this.parentNode).datum();
								// for column sum(alerts), need to get dtm.alerts value from json, not dtm.sum(alerts) :(
								var key = dtm.column.split("(").pop().split(")").shift();
								if(trdata[key]!=undefined){
									//d3.select(this).html('<a href="'+trdata[key]+'" download>&#8659;</a>');
									
									var files = trdata[key].split(",");
									var alinks = [];
										files.forEach(function(f){
											//console.log("file:"+f);
											alinks.push('<a href="'+f+'" download>&#8659;</a>');
										});
									d3.select(this).html(alinks);
									
									//working one, but popup blocking is an issue, so design a wizard
									//d3.select(this)
									//	.append("span")
									//	.attr("class","fa fa-file-text-o")
									//	.on("click", function(){
									//	var files = trdata[key].split(",");
									//	files.forEach(function(f){
									//		console.log("file:"+f);
									//		window.open(f);
									//	});
									//});
								}
								
							} else if(dtm.column.startsWith("alertdate(")){										
								var trdata = d3.select(this.parentNode).datum();
								// for column alertdate(expdate), need to get dtm.expdate value from json
								var key = dtm.column.split("(").pop().split(")").shift();
								if(trdata[key]!=undefined){
									d3.select(this)
									    .text(function(){
											if(trdata[key] != undefined) {
												
												//console.log("typeof dtm.value:"+ (typeof trdata[key]));
												
												//console.log("trdata[key]:"+ trdata[key]);
												
												if(trdata[key].startsWith("***")){
											    	d3.select(this).style("color","red")
											    					.style("font-weight","bold")
											    					.style("font-style","italic");
												}else if(trdata[key].startsWith("**")){
											    	d3.select(this).style("color","red")
											    					.style("font-weight","bold");
												}else if(trdata[key].startsWith("*")){
											 		d3.select(this).style("color","gold")
											    					.style("font-weight","bold");
												}
												return trdata[key].removeAll("*");
											}
									   		return trdata[key];
									    });
							}
									
									
							} else if(dtm.column.startsWith("callback(")){ 									
									var trdata = d3.select(this.parentNode).datum();
									var params = dtm.column.split("(").pop().split(")").shift();
									var keys = params.csvToArray();
									
									//console.log("typeof keys[0]:"+ (typeof keys[0]));
									
									if(keys[0].startsWith("fa-")){ //callback(fa-pencil-square,openchecklist)	
										d3.select(this).html('<a href=javascript:'+keys[1]+'.call()><i class="fa '+keys[0]+'"/></a>');
									} else { //callback(equipid,openequip)	
										d3.select(this).html('<a style="text-decoration:none;" href=javascript:'+keys[1]+'.call()>'+ trdata[keys[0]] +'</a>');
									}

							} else {
								d3.select(this)
									.text(dtm.value); 
							}
						})
						
				return method;
			};
			
			// set page size selector optional(though)
			method.setpagesizeselector = function (pagesizelistid) {
			
				var pagerselector = d3.select(pagesizelistid);
				_pagesize = pagerselector.node().value.replace(/\D/g, ''); //strip not digit chars
				
				//independent code piece - on select
				pagerselector
					.on("change", function(){
						_pagesize = d3.select(this).node().value.replace(/\D/g, ''); //strip not digit chars

						var data1 =	_tabledata.slice(0,_pagesize);
						method.makerows(data1);
						
						//console.log('data1.length:'+_tabledata.length)
						//console.log('selectedtpagesize:'+selectedtpagesize)

						var pagescount = Math.ceil(_tabledata.length / _pagesize);
						
						//console.log('pagescount:'+pagescount)

						return method.populatepager(pagescount);
					});
				
				return method;
			};
			
			// set pager optional(though)
			method.setpager = function (pagerid) {
				_pagerid = pagerid;

				return method;												
			}					
			
			method.setinputfilter = function (typefilterid) {
			
				_inputfilterid = typefilterid;

				return method;												
			}

			// set pager optional(though)
			method.populatepager = function (pagescount) {
			
				//console.log('in setpager:_pagerid:'+ pagescount);
			
				var pager = d3.select(_pagerid);
				
				//clear the existing page nos
				pager.selectAll('li').remove();

				var noofpagearr = [];
				for(var i=1; i<= pagescount; i++){
					noofpagearr.push(i);
				}

				pager.selectAll('li')
					.data(noofpagearr)
					.enter()
					.append('li')
						//.append('a')
						//.text(function(d) {
						//			return d; })
						//.attr("xlink:href", "#")

						.append("input")
						.attr("type", "button")
						.attr("value", function(d) {
						
							//first page active default
							if(d === 1){
								d3.select(this)
										.attr("class", "active");
							}
						
							return d; 
						})						
						.on("click", function(dtm){

								// remove all previous styles
								pager.selectAll('li').selectAll('input')
										.attr("class", null);
										
								//d3.select(this.parentNode)
								//		.attr("class", "active");
								d3.select(this)
										.attr("class", "active");

								return setpagedata(dtm);
							})

				// first page selected default
				//pager.selectAll('li:nth-child(1)')
				//		.attr("class", "active");						
				
				return method;
			};
			
			var setpagedata = function(dtm) {

				var startidx = (dtm-1)*_pagesize;
				var endidx = dtm*_pagesize;

				//console.log('startidx:'+ startidx +', endidx:'+ endidx);

				var data2 =	_tabledata.slice(startidx, endidx);
				method.makerows(data2);

				//console.log('data2:'+data2);
			}
			
			// TODO: sorts only page level, not all pages, kind of ok now :)									
			method.sortable = function(){
				
				//during reload the sort gets appended, so clear it
				_table.select("thead").selectAll("th")
				.selectAll("label")
				.selectAll("i")
				.remove();
				
				//1. table sorting
				_table.select("thead").selectAll("th")
				.append("label").text(" ")
				.append("i")
				.attr("class","fa fa-caret-down")
				.on("click", function(){
					//alert(d3.select(this).datum());	
					
					var thkey = d3.select(this).datum();
					
					_tbody.selectAll("tr")
						.sort(function (a, b) {
								return ascending(jsonpath(a,thkey), jsonpath(b,thkey));										
						});						
						
				});
				
				return method;
			}

			// enter like										
			method.done = function(){
			
				//default
				//var defaultpagesize = _pager.node().value
				var datalength = _tabledata.length;
				
				//console.log('defaultpagesize:'+_pagesize)						
				//console.log('datalength:'+datalength)
				
				if(_pagesize == undefined) //undefined, if the setpagesizeselector() not set
					_pagesize = method._pagesize;
					
				//console.log('defaultpagesize1:'+_pagesize)
				
				var noofpages = Math.ceil(datalength / _pagesize);

				if(_pagerid != undefined) //undefined, if setpager not set 	
					method.populatepager(noofpages);
					
				var defaultsizeddata = _tabledata.slice(0,_pagesize);

				method.makerows(defaultsizeddata);
				
				//console.log('noofpages:'+noofpages)
				
				//** independent feature starts here

				//2. table filter
				d3.select(_inputfilterid).on("keydown", function() {
					
					var searchkeycol = "";
					var searchvalue = this.value;
					
					//TODO: quick fix, think later
					var selectedcolname = d3.select("#filtercolumn").node().value;
						
					_tbody.selectAll("tr")
						.filter(function(d){
							var name = jsonpath(d,selectedcolname);
							if(name === searchvalue){
							   d3.select(this).style("background-color", "lightblue");
							} else if(searchvalue == ""){
								d3.select(this).style("background-color", null);
							}
						})
				});
				

				return method; //to suport get selected rows
			}

			return method;
		}
	