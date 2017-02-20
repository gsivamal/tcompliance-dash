<!DOCTYPE html>
<html lang="en-us" id="extr-page">
	<head>
		<meta charset="utf-8"/>
		<title>DOT Compliance - Auto piloting the process</title>
		<meta name="description" content=""/>
		<meta name="author" content=""/>
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
		
		<link rel="stylesheet" type="text/css" media="screen" href="loginassets/css/bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" media="screen" href="loginassets/css/lockscreen.min.css"/>
		
		<link rel="stylesheet" type="text/css" media="screen" href="fonts/font-awesome.css"/>	
		
		<link rel="icon" href="img/favicon.png" type="image/x-icon"/>

		<!-- SmartAdmin Styles : Caution! DO NOT change the order -->
		<link rel="stylesheet" type="text/css" media="screen" href="loginassets/css/smartadmin-production.min.css"/>

		<!-- #GOOGLE FONT -->
		<link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Open+Sans:400italic,700italic,300,400,700"/>

	</head>
	
	<body>

		<div id="main" role="main">

			<form class="lockscreen animated flipInY" action="app" method="post">
				<div class="logo">
					<h1 class="semi-bold"><img src="loginassets/img/icon.png" alt="" /> SmartCompliance</h1>
				</div>
				<div>
					<input type="hidden" name="entity" value="${entity}"/>
					<img src="${logoname}" alt="" width="120" height="120" />
					<div>
						<h1><i class="fa fa-user fa-2x text-muted air air-top-right hidden-mobile"></i>${entity} Demo Login<small><i class="fa fa-lock text-muted"></i> &nbsp; Protected</small></h1>
						<p class="text-muted">
							<input class="form-control" type="text" name="name" placeholder="Your/Company Name" value="demo-user"/>	
						</p>

						<input class="form-control" type="text" name="phone" placeholder="Phone" value="123-456-7890"/>

						<button class="btn btn-primary" type="submit" style="margin:10px 20px 10px 100px;">Goto Demo</button>
						
						<p class="no-margin margin-top-5">
							<label style="color:red;">${message}</label>
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

	</body>
</html>