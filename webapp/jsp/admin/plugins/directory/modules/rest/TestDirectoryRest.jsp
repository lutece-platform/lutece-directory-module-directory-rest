<%@page import="fr.paris.lutece.portal.service.util.AppPathService"%>

<html>
    <head>
        <title>Directory - REST webservices test page</title>
        <base href="<%= AppPathService.getBaseUrl( request ) %>" />
        <link rel="stylesheet" type="text/css" href="css/portal_admin.css" title="lutece_admin" />
        <script type="text/javascript">
            function onRecordView(  ) {
                var idDirectory = document.formGetRecord.id_directory.value;
                var idDirectoryRecord = document.formGetRecord.id_directory_record.value;
                var format = document.formGetRecord.format.value;
                if ( typeof( idDirectoryRecord ) == 'undefined' ) {
                	document.location= 'rest/directory/' + idDirectory + '/records' + format;
                } else {
                	document.location= 'rest/directory/' + idDirectory + '/record/' + idDirectoryRecord + format;
                }
            }
            function onDirectoryView(  ) {
                var idDirectory = document.formGetDirectory.id_directory.value;
                var format = document.formGetDirectory.format.value;
                if ( typeof( idDirectory ) == 'undefined' ) {
                	document.location= 'rest/directory' + format;
                } else {
                	document.location= 'rest/directory/' + idDirectory + format;
                }
            }
        </script>
    </head>
    <body>
        <div id="content" >
            <h1>Directory - REST webservices test page </h1>
            <div class="content-box">
	            <div class="highlight-box">
	                <h2>View WADL</h2>
	                <form action="rest/directory/wadl">
	                    <br/>
	                    <input class="button" type="submit" value="View WADL" />
	                </form>
	            </div>
	            
	            <div class="highlight-box">
	                <h2>View directory</h2>
	                <form name="formGetDirectory">
	                    <label for="id_directory">ID directory : </label>
	                    <input type="text" name="id_directory" size="10" maxlength="255" />
	                    <br/>
	                    <label for="format">Format :</label>
	                    <select name="format">
	                        <option value=".xml">XML</option>
	                        <option value=".json">JSON</option>
	                    </select>
	                    <br/>
	                    <input class="button" type="button" value="View" onclick="javascript:onDirectoryView(  )"/>
	                </form>
	            </div>
	            
	            <div class="highlight-box">
	                <h2>View record</h2>
	                <form name="formGetRecord">
	                    <label for="id_directory">ID directory * : </label>
	                    <input type="text" name="id_directory" size="10" maxlength="255" />
	                    <br/>
	                    <label for="id_directory_record">ID record : </label>
	                    <input type="text" name="id_directory_record" size="10" maxlength="255" />
	                    <br/>
	                    <label for="format">Format :</label>
	                    <select name="format">
	                        <option value=".xml">XML</option>
	                        <option value=".json">JSON</option>
	                    </select>
	                    <br/>
	                    <input class="button" type="button" value="View" onclick="javascript:onRecordView(  )"/>
	                </form>
	            </div>
        	</div>
        </div>
    </body>
</html>
