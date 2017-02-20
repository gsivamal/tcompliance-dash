<html lang="en-us">	
	<head>
		<meta content="text/html;charset=utf-8" http-equiv="Content-Type"/>
		<meta content="utf-8" http-equiv="encoding"/>		
		<meta name="description" content="DOT Compliance Application"/>
		<meta name="author" content="Com.Pliance.com"/>
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

		<title>Dashboard</title>

		<link rel="stylesheet" type="text/css" media="screen" href="fonts/font-awesome.css"/>		

		<link rel="stylesheet" type="text/css" media="screen" href="css/modal.css"/>
		<link rel="stylesheet" type="text/css" media="screen" href="css/nustone-admin.css"/>
		
		<link rel="stylesheet" type="text/css" media="screen" href="css/jquery.datepick.css"/>
		<link rel="stylesheet" type="text/css" media="screen" href="css/jquery.timepicker.css"/>
		
		<link rel="icon" href="img/favicon.png" type="image/x-icon"/>
		
		<link href='https://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'/>
				
		<style>
			 body {
		        font-family: 'Open Sans', serif;
		        font-size: 14px;
		      }
		</style>
		
  	</head>
  	
<body>	
		<#if role == "driver_disabled_timebeing">
			<div id="google_translate_element"></div>
		</#if>
			
		<div>
			<div class="row center">
				<div class="col-5">
					<img style="width:80px;height:50px;" src="company-logos/${companyid}-logo.jpg" alt="Smart Compliance"/>
					<label style="color:orange;"><b>${companyname}</b></label>
	    		</div>
	    		<div class="col-4"></div>
	    		
	    		<#if role == "owner">
		    		<div class="col-1 center">
		    			<div id="notifications-badge" class="badge1" data-badge="0" onclick="toggle('notifications-list', loadtasks)">	    				
		    				<i style="color:teal;" class="fa fa-2x fa-comments"></i>
		    			</div>
		    			
		    			<ul id="notifications-list"/>
		    		</div>		
				</#if>
				
	    		<div class="col-2 pullright"> 
		    		<!-- img style="padding:1px;border:1px solid gray;width:40px;height:40px;" src="company-logos/avatars/driver.png" alt="NuStone" -->
	    		</div>		
	    		<div class="col-2 pullright">
					<div>
	    				<i style="color:darkblue;padding-bottom:10px;" class="fa fa-user">&nbsp;${useremail}</i></a>
	    				&nbsp;<button id="support" style="color:teal;border:none;background:white;cursor:pointer;"><i class="fa fa-lg fa-phone"></i></button>
	    				<button id="logout" style="color:teal;border:none;background:white;cursor:pointer;"><i class="fa fa-lg fa-sign-out">&nbsp;Logout</i></button>
	    			</div>
	    		</div>
			</div>
		</div>
		
		<div class="hrLine"></div>

		<div class="row">	
		
		<#if role == "owner">
			<div class="col-1 menuitem icon">
				<a href="/pages/dashboard.html"><i class="fa fa-lg fa-home"></i></a>
			</div>
			<div class="col-1 menuitem">
	       		<a href="/pages/company/timecard/timecard_tabs.html"><i class="fa fa-hourglass-half">&nbsp;&nbsp;</i>Timecard</a>
			</div>
			<div class="col-1 menuitem">
	       		<a href="/pages/company/inspection/insp_side_tabs.html"><i class="fa fa-lg fa-pencil-square-o">&nbsp;&nbsp;</i>Inspections</a>
			</div>
			<div class="col-1 menuitem">
				<a href="/pages/driver/driver_list.html" id="drivermenu" class="badge1" data-badge="0"><i class="fa fa-lg fa-user">&nbsp;&nbsp;</i>Driver</a>
			</div>
			<div class="col-1 menuitem">
	           	<a href="/pages/equip/equip_list.html" id="equipmenu" class="badge1" data-badge="0"><i class="fa fa-lg fa-truck">&nbsp;&nbsp;</i>Equipment</a>
			</div>
			<div class="col-1 menuitem">
		       	<a href="/pages/company/company_tabs.html" id="compmenu" class="badge1" data-badge="0"><i class="fa fa-lg fa-building">&nbsp;&nbsp;</i>myCompany</a>
			</div>
		</#if>
	
		<#if role == "driver">
			<div class="col-1 menuitem steelblue">
	       		<a href="/pages/trip/timecard/timecard_side_tabs.html"><i class="fa fa-hourglass-half">&nbsp;&nbsp;</i><span class="trn">Timecard</span></a>
			</div>
			<div class="col-1 menuitem steelblue">
	       		<a href="/pages/company/inspection/insp_side_tabs.html"><i class="fa fa-lg fa-pencil-square-o">&nbsp;&nbsp;</i><span class="trn">Inspections</span></a>
			</div>
			
			<!-- div class="col-1 menuitem black">
				<a href="/pages/driver/geolocation/map.html"><i class="fa fa-truck">&nbsp;&nbsp;</i> Locate Me</a>
			</div>
			
			<div class="col-1 menuitem black">
				<a href="/pages/driver/geolocation/marker_demo.html"><i class="fa fa-search">&nbsp;&nbsp;</i> Truck Track</a>
			</div>	
			 -->		

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
		</#if>
		</div>	
		
        <div class="row" id="content"></div>
	    
	    <div class="hrLine"></div>
	    
	    <div class="row addmargin">
		    	<label>PeopleMatrix Software, 2016 &#169;</label>
		</div>	
	
	    <!-- Include jQuery. -->
		<script src="js/d3.min.3.5.6.js" charset="utf-8"></script>
	   	<script src="js/jquery.min.2.1.1.js"></script>
	   	
		<script src="js/jquery.cookie.js"></script> <!-- Jquery Cookie -->

		<!-- MAIN APP JS FILE -->
		<script src="js/navigation.js"></script>
		<script src="js/util.js"></script>
		<script src="js/custom-elements.js"></script>
		<script src="js/modal.js"></script>
		<script src="js/workflow.js"></script>
		
		<script src="js/d3table.js"></script>
		
		<script src="js/jquery.plugin.js"></script>
		<script src="js/jquery.datepick.js"></script>
		<script src="js/jquery.timepicker.js"></script>
		
		<script src="js/jquery.connections.js"></script>
		
		<script src="js/livereload.js?host=localhost"></script>
		
		<script src="js/barcode/jquery-barcode.js"></script>		

		<!-- pdf (these 2 below working one)
		mrrio.github.io/jsPDF/dist/jspdf.debug.js
		html2canvas.hertzen.com/build/html2canvas.js
		-->
		<script src="js/pdf/jspdf.min.js"></script>
		<script src="js/pdf/html2canvas.js"></script>

		<!--google charts api-->
		<script src="gstatic/loader.js"></script>
		
		<!--google maps api-->
		<script src="//maps.googleapis.com/maps/api/js?key=AIzaSyCylumkIswvSZ2dL3g1MQdosHKuDuzxAQ0" async defer></script>	
		
		<!-- Open Layer -->
		<link rel="stylesheet" href="http://openlayers.org/en/v3.18.2/css/ol.css" type="text/css">
	    <script src="http://cdn.polyfill.io/v2/polyfill.min.js?features=requestAnimationFrame,Element.prototype.classList,URL"></script>
	    <script src="http://openlayers.org/en/v3.18.2/build/ol.js"></script>
		<script src="https://api.mapbox.com/mapbox.js/plugins/arc.js/v0.1.0/arc.js"></script>
		
		<!-- -->
		<script src="js/logbook.js"></script>
		
		<#if role == "driver">
	
				<script type="text/javascript">
					function googleTranslateElementInit() {
					  new google.translate.TranslateElement({pageLanguage: 'en', includedLanguages: 'es,fr', layout: google.translate.TranslateElement.InlineLayout.SIMPLE, gaTrack: true, gaId: 'UA-85420217-1'}, 'google_translate_element');
					}
				</script>
				
				<script type="text/javascript" src="//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit">
				</script>
									
		</#if>
		
				
		<script type="text/javascript">
		
			// Load the Visualization API and the corechart package.
	      	google.charts.load('current', {'packages':['corechart']});
	      	
			//landing page
			<#if role == "owner">
				loadpage("/pages/dashboard.html");
			</#if>
				
			<#if role == "driver">
	       		loadpage("/pages/trip/logbook/new_logbook.html");
			</#if>
			
			<#if role == "mechanic">
				loadpage("/pages/mech/mech-queue.html");
			</#if>
				
				
			// Navigate whenever the fragment identifier value changes.
  			$( "a" ).click(function( e ) {
  			
  				$("a").css('color', 'white');

				$( "a" ).css('font-weight', 'normal');
 				$(this).css('font-weight', 'bold');
 				$(this).css('color', 'gold');
  				
  				var fragmentId = $(this).attr('href')
  				
  				//reset the objects
  				Current.driver = null;
  				Current.equip = null;
  				Current.company = null;
  				
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
	
			$( "#logout" ).click(function( e ) {		  	
		      if(!confirm("Are you sure you want to logout ?")){
    				return;
	   			}
		      window.location = "index.html";
		  	});
		  	
		  	$( "#support" ).click(function( e ) {		  	
				loadpage("/pages/support.html");
		  	});
		  		    				
		  	//$(document).click(function(e){			
		  		//d3.selectAll(".multiselect-container").style("display","none");
		  	//	closeContainer('multiselect-container');
			//});

		</script>
    
  </body>
</html>
