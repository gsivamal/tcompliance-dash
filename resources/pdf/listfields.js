function listFormFields()
{
   var allFields = "";

   for (var i = 0; i < this.numFields; i++)
   {
      var newField = '"' + this.getNthFieldName(i) + '" ';
      allFields += newField;
   }
   app.alert(allFields);
}

app.addMenuItem({
   cName: "JSListFormFields",
   cUser: "List all Form Fields",
   cParent: "File",
   cExec: "listFormFields()",
   cEnable: "event.rc = (event.target != null);",
   nPos: 0,
}
,
	{	cName:"Console Window",
		cParent:"View",
		cExec:"console.show()"
	}
);
// end of script