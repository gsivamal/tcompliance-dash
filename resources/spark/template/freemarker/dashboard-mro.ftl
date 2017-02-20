<!DOCTYPE html>
<html lang="en-us">	
	<head>
		<meta content="text/html;charset=utf-8" http-equiv="Content-Type"/>
		<meta content="utf-8" http-equiv="encoding"/>		
		<meta name="description" content="DOT Complaince Application"/>
		<meta name="author" content="Com.Plaince.com"/>
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

		<title>MRO Dashboard</title>


		<link rel="stylesheet" type="text/css" media="screen" href="fonts/font-awesome.css"/>		

		<link rel="stylesheet" type="text/css" media="screen" href="css/modal.css"/>
		<link rel="stylesheet" type="text/css" media="screen" href="css/nustone-admin.css"/>
		
		<link rel="stylesheet" type="text/css" media="screen" href="css/jquery.datepick.css"/>
		
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

		<div>
			<div class="row center">
				<div class="col-4">
					<img style="width:80px;height:50px;" src="company-logos/${companyid}-logo.jpg" alt="MRO"/>
					<label style="color:orange;"><b>${companyname}</b></label>
	    		</div>
	    		<div class="col-7"></div>		
	    		<div class="col-2 pullright">
					<div>
	    				<i style="color:rgb(128,128,0,0.82);" class="fa fa-user">&nbsp;${useremail}</i></a>
	    				<a href="logout" style="color:teal;"><i class="fa fa-lg fa-sign-out">&nbsp;Logout</i></a>
	    			</div>	    			
	    		</div>
			</div>
		</div>
		
		<div class="hrLine"></div>

		<div class="row">
		
			<div class="col-2 menuitem">
				<a href="/pages/mro/mro_queue.html"><i class="fa fa-lg fa-home"></i>&nbsp;&nbsp;MRO Queue</a>
			</div>
			<div class="col-2 menuitem">
		       	<a href="/pages/mro/mro_company-info.html"><i class="fa fa-lg fa-user-md"></i>&nbsp;&nbsp;myProfile</a>
			</div>
			<div class="col-2 menuitem">
		       	<a href="/pages/support.html"><i class="fa fa-lg fa-phone"></i>&nbsp;&nbsp;Support</a>
			</div>
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
		<script src="//cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js" charset="utf-8"></script>
	   	<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
	   	
		<script src="js/jquery.cookie.js"></script> <!-- Jquery Cookie -->

		<!-- MAIN APP JS FILE -->
		<script src="js/navigation.js"></script>
		<script src="js/custom-elements.js"></script>
		<script src="js/util.js"></script>
		<script src="js/modal.js"></script>
		<script src="js/workflow.js"></script>
		
		<script src="js/d3table.js"></script>
		
		<script src="js/jquery.plugin.js"></script>
		<script src="js/jquery.datepick.js"></script>
		
		<script src="js/livereload.js?host=localhost"></script>
		
		<script src="js/barcode/jquery-barcode.js"></script>		

		<!-- pdf (these 2 below working one)
		mrrio.github.io/jsPDF/dist/jspdf.debug.js
		html2canvas.hertzen.com/build/html2canvas.js
		-->
		<script src="js/pdf/jspdf.min.js"></script>
		<script src="js/pdf/html2canvas.js"></script>
		
		<!--google maps api-->
		<script src="//maps.googleapis.com/maps/api/js?key=AIzaSyCylumkIswvSZ2dL3g1MQdosHKuDuzxAQ0" async defer></script>	
				
		<script>
		
			//landing page
			loadpage("/pages/mro/mro_queue.html");
				
			// Navigate whenever the fragment identifier value changes.
  			$( "a" ).click(function( e ) {

				$( "a" ).css('font-weight', 'normal');
 				$(this).css('font-weight', 'bold');
  				
  				var fragmentId = $(this).attr('href')
  				
  				//reset the objects
  				Current.driver = {};
  				Current.equip = {};
  				Current.company = {};
  				
  				if(fragmentId != 'logout'){
  					e.preventDefault()
  					loadpage(fragmentId)
  				}
  			})
  				
  			
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
			        t = setTimeout(startCountDown, 900000)
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

	  
</script>
			
			
		</script>
    
  </body>
</html>
