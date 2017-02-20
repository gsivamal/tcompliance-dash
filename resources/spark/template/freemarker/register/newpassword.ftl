<!DOCTYPE html>
<html lang="en-us" id="extr-page">
	<head>
		<meta charset="utf-8"/>
		<title>DOT Compliance - Auto piloting the process</title>
		<meta name="description" content=""/>
		<meta name="author" content=""/>
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
		
		<link rel="stylesheet" type="text/css" media="screen" href="../loginassets/css/bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" media="screen" href="../loginassets/css/lockscreen.min.css"/>
		
		<link rel="stylesheet" type="text/css" media="screen" href="../fonts/font-awesome.css"/>	
		
		<link rel="icon" href="img/favicon.png" type="image/x-icon"/>

		<!-- SmartAdmin Styles : Caution! DO NOT change the order -->
		<link rel="stylesheet" type="text/css" media="screen" href="../loginassets/css/smartadmin-production.min.css"/>

		<!-- #GOOGLE FONT -->
		<link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Open+Sans:400italic,700italic,300,400,700"/>

	</head>
	
	<body>

		<div id="main" role="main">

			<form class="lockscreen" action="../../setpassword" method="post">
			
				<div class="logo">
					<h1 class="semi-bold"><img src="../loginassets/img/icon.png" alt="" /> SmartCompliance</h1>
				</div>
				
				<div>
				
					<div>
						<input type="hidden" name="activationcode" value="${activationcode}"/>
						<input type="hidden" name="username" value="${username}"/>
						<h1>
							Set new password &nbsp;<small><i class="fa fa-lock text-muted"></i></small>
						</h1>
						
						<div class="input-group">
							<label>User Name</label><span class="form-control">${username}</span>	
						</div>
						
						<span style="color:red;">Password rules: Minimum 6 Chars, with at least One Special Char(@#$%&) and One Number </span>

						<div class="input-group">
							<label>Password</label>
							<input class="form-control" type="password" id="password" name="password" value="${password}"/>
						</div>

						<div class="input-group">
							<label>Confirm Password</label>
							<input class="form-control" type="password" id="confirmpassword" name="confirmpassword" value="${confirmpassword}"/>
						</div>

						<button class="btn btn-primary" type="submit" style="margin:10px 20px 10px 100px;">Submit</button>
						
						<p class="no-margin margin-top-5">
							<span id="message" style="color:red;">${message}</span>
							<div>Problem signing in?</div>
							<div><a href="support.html"> Contact Support 214-432-6580</a></div>
						</p>
					</div>

				</div>
				<p class="font-xs margin-top-5">
					Smart Compliance &#169; 2015-2016.
				</p>
			</form>

		</div>
		
		<script type="text/javascript">
		
			var checkmatching = function(){
			
				var password = document.getElementById('password').value;
				var confirmpassword = document.getElementById('confirmpassword').value;
				
				console.log(password);
				
				if(password != confirmpassword){
					//document.getElementById('confirmpassword').focus();
					//alert("Password and Confirm Password not matching!");
					document.getElementById('message').innerHTML = "Password and Confirm Password not matching!";
					return false;
				} else {
					return true;
				}
			}
		
		</script>

	</body>
</html>