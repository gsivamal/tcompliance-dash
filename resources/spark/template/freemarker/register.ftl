<html lang="en-us">

<head>
    <meta content="text/html;charset=utf-8" http-equiv="Content-Type" />
    <meta content="utf-8" http-equiv="encoding" />
    <meta name="description" content="DOT Compliance Application" />
    <meta name="author" content="Com.Pliance.com" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />

    <title>Dashboard</title>

    <link rel="stylesheet" type="text/css" media="screen" href="fonts/font-awesome.css" />

    <link rel="stylesheet" type="text/css" media="screen" href="css/modal.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="css/nustone-admin.css" />

    <link rel="stylesheet" type="text/css" media="screen" href="css/jquery.datepick.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="css/jquery.timepicker.css" />

    <link rel="icon" href="img/favicon.png" type="image/x-icon" />

    <link href='https://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css' />

    <style>
        body {
            font-family: 'Open Sans', serif;
            font-size: 14px;
        }
    </style>

</head>

<body>

    <div id="compreg-form" style="margin:0px 150px 0px 150px;border:2px solid gray; border-radius:8px;font-size:95%;">

        <div>

            <div class="row center">
                <div class="col-9" style="border-top-left-radius:8px;background:#f4f4f4!important;padding-left:20px;"><img style="width:auto;height:50px;" src="loginassets/img/logo.png" alt="Smart Compliance" /></div>
                <div class="col-3" style="border-top-right-radius:8px;background:#f4f4f4!important;height:53px;padding-top:16px;"><i class="fa fa-lock"></i>&nbsp;&nbsp;Secured signup</div>
            </div>

            <div class="hrLine"></div>

            <div class="row">
                <div class="col-12 center">
                    <h2>Customer Registration</h2></div>
            </div>

            <div class="row">
                <div class="col-1"></div>
                <div class="col-2">
                    <label> DOT Number</label>
                </div>
                <div class="col-2">
                    <input data-dbcolname="dot" type="text" data-validation="mandatory" />
                </div>
                <div class="col-3"></div>
            </div>

            <div class="smallspacer"></div>

            <div class="row">
                <div class="col-1"></div>
                <div class="col-2">
                    <label> Company Name</label>
                </div>
                <div class="col-3">
                    <input data-dbcolname="name" type="text" data-validation="mandatory" />
                </div>
                <div class="col-2">
                    <label> DBA(if any)</label>
                </div>
                <div class="col-2">
                    <input data-dbcolname="dba" type="text" />
                </div>
                <div class="col-1"></div>
            </div>

            <div class="smallspacer"></div>

            <div class="row">
                <div class="col-1"></div>
                <div class="col-2">
                    <label> Owner Name</label>
                </div>
                <div class="col-3">
                    <input data-dbcolname="contact1" type="text" data-validation="mandatory" />
                </div>
                <div class="col-2">
                    <label> Primary Contact</label>
                </div>
                <div class="col-2">
                    <input data-dbcolname="contact2" type="text" />
                </div>
                <div class="col-1"></div>
            </div>

            <div class="smallspacer"></div>

            <div class="row">
                <div class="col-1"></div>
                <div class="col-2">
                    <label> Upload logo (150x90)</label>
                </div>
                <div class="col-4">
                    <input id="logo" data-dbcolname="logo" data-validation="mandatory" type="file" onchange="document.getElementById('logopreview').src = window.URL.createObjectURL(this.files[0])" />
                </div>
                <div class="col-4"><img id="logopreview" alt="Company Logo" width="100" height="100" /></div>
                <div class="col-1"></div>
            </div>

        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-5"><b><label>Address</label></b></div>
            <div class="col-3"><b><label>Company</label></b></div>
            <div class="col-1"></div>
        </div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-2">
                <label> Address</label>
            </div>
            <div class="col-3">
                <input data-dbcolname="address" type="text" data-validation="mandatory" />
            </div>
            <div class="col-1">
                <label> Phone</label>
            </div>
            <div class="col-2">
                <input data-dbcolname="phone" type="text" data-validation="mandatory" data-format="usphone" placeholder="   -   -    " />
            </div>
            <div class="col-1"></div>
        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-2">
                <label> Address1</label>
            </div>
            <div class="col-3">
                <input data-dbcolname="address1" type="text" />
            </div>
            <div class="col-1">
                <label> Fax</label>
            </div>
            <div class="col-2">
                <input data-dbcolname="fax" type="text" data-format="usphone" placeholder="   -   -    " />
            </div>
            <div class="col-1"></div>
        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-2">
                <label> City</label>
            </div>
            <div class="col-3">
                <input data-dbcolname="city" type="text" data-validation="mandatory" />
            </div>
            <div class="col-1">
                <label> Email</label>
            </div>
            <div class="col-2">
                <input data-dbcolname="email" type="text" data-format="email" placeholder="id@mail.com" data-validation="mandatory" />
            </div>
            <div class="col-1"></div>
        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-2">
                <label> State</label>
            </div>
            <div class="col-3">
                <select name="state" id="state" data-dbcolname="state" data-validation="mandatory"></select>
            </div>
            <div class="col-1">
                <label> Website</label>
            </div>
            <div class="col-2">
                <input data-dbcolname="website" type="text" />
            </div>
            <div class="col-1"></div>
        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-2">
                <label> Zip</label>
            </div>
            <div class="col-2">
                <input data-dbcolname="zip" type="text" data-format="uszip" placeholder="     -   " data-validation="mandatory" />
            </div>
            <div class="col-3"></div>
        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-2">
                <label> User name (use your email address)</label>
            </div>
            <div class="col-3">
                <input style="border:1px solid green;" data-dbcolname="username" type="text" data-validation="mandatory" placeholder="user@mail.com" />
            </div>
            <div class="col-6"></div>
        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-10">
                <label>For security reasons, upon approval, we will send you a one-time use password to use on your first login. At that time, you will be able to set your own password.</label>
            </div>
            <div class="col-1"></div>
        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-2">
                <label> Security Question </label>
            </div>
            <div class="col-8">
                <select name="securityquestions" id="securityquestions" data-dbcolname="state" data-validation="mandatory" onchange="javascript:document.getElementById('securityanswer').focus();"></select>
            </div>
            <div class="col-1"></div>
        </div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-2">
                <label> Security Answer </label>
            </div>
            <div class="col-2">
                <input id="securityanswer" data-dbcolname="securityanswer" type="text" data-validation="mandatory" />
            </div>
            <div class="col-1"></div>
        </div>

        <div class="smallspacer"></div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-10"><b><label>Choose Your Payment Method</label></b></div>
            <div class="col-1"></div>
        </div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-10">
                <ul id="paymenttabs" class="tabs">
                    <li>
                        <input type="button" id="creditcard_btn" value="Credit Card" class="active" />
                    </li>
                    <li>
                        <input type="button" id="paypal_btn" value="Paypal" />
                    </li>
                </ul>
            </div>
            <div class="col-1"></div>
        </div>

        <div class="row">
            <div class="col-1"></div>
            <div class="col-10">
                <div id="tabcontent"></div>
            </div>
            <div class="col-1"></div>
        </div>
    
    <div class="spacer"></div>
    <div class="spacer"></div>

    <div class="row">
        <div class="col-1"></div>
        <div class="col-9">
            <textarea id="terms_language" style="width:820px;height:130px;" readonly>
            </textarea>
        </div>
        <div class="col-1"></div>
    </div>

    <div class="smallspacer"></div>

    <div class="row">
        <div class="col-1"></div>
        <div class="col-10">
            <span style="font-size:90%;">In agreeing to use this service, I understand that my use is governed by state and federal law and any misuse may subject me to civil and/or criminal penalties.  You should print a copy of this agreement for your records.  I accept this Agreement and will abide by its terms and the law governing my performance. </span>&nbsp;&nbsp;
            <input type="checkbox" id="signed" />
        </div>
        <div class="col-1"></div>
    </div>

    <div class="smallspacer"></div>

    <div class="row" align="center">
        <div class="col-12"><span id="message"></span></div>
    </div>

    <div class="smallspacer"></div>

    <div class="row">
        <div class="col-5"></div>
        <div class="col-2">
            <button type="button" id="comp-signup" style="border:1px solid orange;border-radius:2px;font-size:80%;height:18px">Sign up now >></button>
        </div>
        <div class="col-5"></div>
    </div>

    <div class="smallspacer"></div>

    <div class="hrLine"></div>

    <div class="row addmargin">
        <label>PeopleMatrix Software, &#169; 2016</label>
    </div>
    <div class="row addmargin">
        <a href="help">Help</a> | <a href="contactus">Contact us</a>
    </div>

    </div>
    <!-- flex-container -->

    </div>
    <!-- form -->

    <!-- Include jQuery. -->
    <script src="js/d3.min.3.5.6.js" charset="utf-8"></script>
    <script src="js/jquery.min.2.1.1.js"></script>

    <script src="js/jquery.cookie.js"></script>
    <!-- Jquery Cookie -->

    <!-- MAIN APP JS FILE -->
    <script src="js/navigation.js"></script>
    <script src="js/util.js"></script>
    <script src="js/custom-elements.js"></script>
    <script src="js/workflow.js"></script>

    <script src="js/livereload.js?host=localhost"></script>

    <script src="js/barcode/jquery-barcode.js"></script>

    <script src="js/pdf/jspdf.min.js"></script>
    <script src="js/pdf/html2canvas.js"></script>

    <script type="text/javascript">
        ! function() {

            var paymentmethod = "creditcard";

            //default tab
            loadtab("/pages/company/paymentmethod/creditcard.html");

            var inputs = d3.select("#paymenttabs").selectAll("li").selectAll("input")
                // default credit card
            d3.select("#creditcard_btn")
                .on("click", function() {
                    paymentmethod = "creditcard";
                    inputs.attr("class", null); // remove all previous styles
                    d3.select(this).attr("class", "active");
                    loadtab("/pages/company/paymentmethod/creditcard.html");
                });

            // paypal
            d3.select("#paypal_btn")
                .on("click", function() {
                    paymentmethod = "paypal";
                    inputs.attr("class", null); // remove all previous styles
                    d3.select(this).attr("class", "active");
                    loadtab("/pages/company/paymentmethod/paypal.html");
                });

            //save entry    
            $('#comp-signup').click(function() {

                //1. validation first
                var validation_error = formutil.validateInputFields("#compreg-form");

                var aftersave = workflow.create("#compreg-form").aftersave();

                //check the terms agreed
                if (d3.select("#signed").node().checked == false) {
                    var msg = {
                        "id": 1,
                        "message": "ERROR: Please check the checkbox above to agree!"
                    };
                    aftersave.setStatusMessage(msg);
                    return;
                }

                //2. save maintenance
                if (!validation_error) {

                    var compregdata = formutil.serializeObject("#compreg-form");
                    compregdata.paymentmethod = paymentmethod;

                    var formData = formutil.serializeFileObject("#compreg-form");
                    formData.append("compregdata", JSON.stringify(compregdata));

                    console.log("compregdata:" + JSON.stringify(compregdata));

                    aftersave.disableAll();

                    // send via XHR - look ma, no headers being set!
                    $.ajax({
                        url: '/company/register',
                        type: 'post',
                        cache: false,
                        data: formData,
                        processData: false, //otherwise jQuery tries to transform your FormData object to a string,
                        contentType: false,
                        success: function(resp) {
                                //aftersave.changetext("#cancel","Done"); //changetext enables the el
                                aftersave.setStatusMessage(resp);
                            }
                            //return false; //
                    });
                }
            });

            //1. populate master data - state
            $.getJSON('/data/state.json', function(data) {
                dropdown().create("#state")
                    .populatedata(data);
            });

            //2. security questions			
            $.getJSON('/data/register/security_questions.json', function(data) {
                dropdown().create("#securityquestions")
                    .populatedata(data);
            });

            //2. set today date at Terms Language
            $.get("/data/register/agreement_language.json", function(termlang) {

                //console.log(termlang);

                d3.select("#terms_language").text(termlang.replace("#todaydate#", new Date()));

            });

            //logout time out
            var inactivityTime = function() {
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

            inactivityTime();

        }();
    </script>

</body>

</html>