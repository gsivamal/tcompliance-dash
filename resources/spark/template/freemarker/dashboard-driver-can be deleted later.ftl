<!DOCTYPE html>
<html lang="en-us">	
	<head>
		<meta content="text/html;charset=utf-8" http-equiv="Content-Type"/>
		<meta content="utf-8" http-equiv="encoding"/>		
		<meta name="description" content="DOT Complaince Application"/>
		<meta name="author" content="Com.Plaince.com"/>
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

		<title>Dashboard</title>

		<link rel="icon" href="img/favicon.png" type="image/x-icon"/>
		<link rel="stylesheet" type="text/css" media="screen" href="fonts/font-awesome.css"/>		
		<link href='https://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'/>

		<link rel="stylesheet" type="text/css" media="screen" href="css/modal.css"/>
		<link rel="stylesheet" type="text/css" media="screen" href="css/nustone-admin.css"/>
		
		<link rel="stylesheet" type="text/css" media="screen" href="css/jquery.datepick.css"/>
		<link rel="stylesheet" type="text/css" media="screen" href="css/jquery.timepicker.css"/>
				
		<style>
			 body {
		        font-family: 'Open Sans', serif;
		        font-size: 14px;
		      }
		</style>
		
  	</head>
  	
<body>		
		
		<div>
			<div class="row center">
				<div class="col-4">
					<img style="width:80px;height:50px;" src="company-logos/${companyid}-logo.jpg" alt="Smart Compliance"/>
					<label style="color:orange;"><b>${companyname}</b></label>
	    		</div>
	    		<div class="col-6"></div>
	    		<div class="col-2 pullright"> 
		    		<!-- img style="padding:1px;border:1px solid gray;width:40px;height:40px;" src="company-logos/avatars/driver.png" alt="NuStone" -->
	    		</div>		
	    		<div class="col-2 pullright">
					<div>
	    				<i style="color:rgb(128,128,0,0.82);" class="fa fa-user">&nbsp;${useremail}</i></a>
	    				<a href="logout"><i class="fa fa-lg fa-sign-out">&nbsp;Logout</i></a>
	    			</div>
	    		</div>
			</div>
		</div>
		
		<div class="hrLine"></div>

		<div class="row">
		
			<#if role == "owner">
			
			
			</#if>
		
			<#if role == "driver">
				<div class="col-1 menuitem orange">
		       		<a href="/pages/trip/timecard/timecard_side_tabs.html"><i class="fa fa-hourglass-half">&nbsp;&nbsp;</i>Timecard</a>
				</div>
				<div class="col-1 menuitem orange">
		       		<a href="/pages/company/inspection/insp_side_tabs.html"><i class="fa fa-lg fa-pencil-square-o">&nbsp;&nbsp;</i>Inspections</a>
				</div>
				<div class="col-1 menuitem orange">
					<a href="/pages/support.html"><i class="fa fa-phone">&nbsp;&nbsp;</i> Support</a>
				</div>					
			</#if>
			
			<#if role == "mechanic">
				<div class="col-1 menuitem black">
					<a href="/pages/mech/mech-queue.html"><i class="fa fa-hourglass-half">&nbsp;&nbsp;</i> My Work Orders</a>
				</div>
				<div class="col-1 menuitem black">
					<a href="/pages/company/inspection/roadsideinsp/all_roadsideinsps.html"><i class="fa fa-lg fa-truck">&nbsp;&nbsp;</i> Road Side Insp</a>
				</div>	
				<div class="col-1 menuitem black">
					<a href="/pages/equip/equip_list.html"><i class="fa fa-lg fa-truck">&nbsp;&nbsp;</i> All Equipments</a>
				</div>	
				<div class="col-1 menuitem black">
					<a href="/pages/support.html"><i class="fa fa-phone">&nbsp;&nbsp;</i> Support</a>
				</div>					
			</#if>
		
		</div>	
		
        <div class="row" id="content"></div>
	    
	    <div class="hrLine"></div>
	    
	    <div class="row addmargin">
	    	<label>PeopleMatrix Software, 2015 &#169; (Demo Version 1.0)</label>
		</div>	
		<div class="row addmargin">		   	
    			<a href="help">Help</a> | <a href="contactus">Contact us</a>
		</div> 
	
	    <!-- Include jQuery. -->
		<script src="js/d3.min.3.5.6.js" charset="utf-8"></script>
	   	<script src="js/jquery.min.2.1.1.js"></script>
	   	
		<script src="js/jquery.cookie.js"></script> <!-- Jquery Cookie -->

		<!-- MAIN APP JS FILE -->
		<script src="js/navigation.js"></script>
		<script src="js/custom-elements.js"></script>
		<script src="js/util.js"></script>
		<script src="js/modal.js"></script>
		<script src="js/workflow.js"></script>
		
		<script src="js/d3table.js"></script>
		
		<!-- DATE/TIME PICKERS -->
		<script src="js/jquery.plugin.js"></script>
		<script src="js/jquery.datepick.js"></script>
		<script src="js/jquery.timepicker.js"></script>
		
		<!-- FOR PDF EXPORT -->
		<script src="js/pdf/jspdf.min.js"></script>
		<script src="js/pdf/html2canvas.js"></script>
		
		<!-- FOR LIVERELOAD -->
		<script src="js/livereload.js?host=localhost"></script>
		
					
		<script>
		
		(function ($) {		

			//landing page
			<#if role == "driver">
				loadpage("/pages/trip/timecard/timecard_side_tabs.html");
			</#if>

			<#if role == "mechanic">
				loadpage("/pages/mech/mech-queue.html");
			</#if>
			
			
			// Navigate whenever the fragment identifier value changes.
  			$( "a" ).click(function( e ) {

				$( "a" ).css('font-weight', 'normal');
 				$(this).css('font-weight', 'bold');
  				
  				var fragmentId = $(this).attr('href')
  				
  				//reset the objects
  				//Current.driver = null; //not the case for driver/mech 
  				Current.equip = null;
  				//Current.company = null; //not the case for driver/mech
  				
  				if(fragmentId != 'logout'){
  					e.preventDefault()
  					loadpage(fragmentId)
  				}
  				
  			});
							
			$.ajaxSetup({
			  'beforeSend': function(xhr) {
			    if ($.cookie("nustone-token")) {
			      xhr.setRequestHeader('Authorization',
			            'Bearer ' + $.cookie("nustone-token"));
			    }
			  }
			});
			
			
			//logout time out
			var inactivityTime = function () {
			    var t;
			    window.onload = resetTimer;
			    document.onmousemove = resetTimer;
			    document.onkeypress = resetTimer;
			
			    function startCountDown() {
			        alert("You are being logged out!")
			        location.href = '/logout';
			    }
			
			    function resetTimer() {
			        clearTimeout(t);
			        t = setTimeout(startCountDown, 1800000)
			        // 1000 milisec = 1 sec
			        // 900000 ms = 15 mins
			        //1800000 = 30 mins
			    }
			};
	
			inactivityTime ();
	
		  	function confirmLogout(){
		      var r = confirm("Are you sure you want to logout ?");
		      if (r != true) {
			      return false;
		      }
		  	}
		  	
		  	
		  	//logout context menu
		  	var toggle = true;
		    d3.select("#loginuser_btn")
		    	.on("click", function(){	    	
						
						d3.select(this.parentNode)
							.select(".multiselect-container")
							.remove();
	
						d3.select(this.parentNode)
							.append("div")
								.attr("class","multiselect-container")			
								.style("color","red")								
								.style("display", function(){
										if(toggle) 
											return "block";
										else 
											return "none";
									})
								//.style("position","relative")	
								.append("i")
								.attr("class","fa fa-lg fa-sign-out")
								.append("label")							
								.text(" Logout")
								//.attr("height","20px")
								.on("click", function(d){
								
									 location.href = '/logout';
									
								})

							
						toggle = !toggle;						
		    	});
			
			
		})(jQuery);
		  	
		</script>
			
    
  </body>
</html>
