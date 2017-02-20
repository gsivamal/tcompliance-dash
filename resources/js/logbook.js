
	var setPageDisplayValues = function(){

		//TODO: Based on final logbook design, this might change..
		if(Current.driver == null){
			$.getJSON("api/user/get",function(data){
				$.getJSON('/api/driver/get', { email : data.email }, function(driver) {
					// get the logged in Driver
					Current.driver = driver;
					d3.select("#drivername").text(Current.driver.firstname  + " " + Current.driver.lastname);
					var driversign = signature1().create("driversign", Current.driver.firstname  + " " + Current.driver.lastname);	
				});
			});
		}
		
		//set the Current company, used in the trip screens
		$.getJSON('api/company/get', function(data) {
			//set the company json
			Current.company = data;
			d3.select("#companyname").text(Current.company.name);
			d3.select("#companyaddress").text(Current.company.address + " " + Current.company.city + " " + Current.company.state + " " + Current.company.zip);
		});
	
		//date today
		
		d3.select("#todaydate").text(new Date().displayDate() );

	}