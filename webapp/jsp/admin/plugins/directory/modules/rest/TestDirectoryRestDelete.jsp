<%@page import="fr.paris.lutece.portal.service.util.AppPathService"%>
<%@page import="org.apache.commons.httpclient.methods.DeleteMethod"%>
<%@page import="org.apache.commons.httpclient.HttpClient"%>
<%@page import="java.net.HttpURLConnection"%>

<html>
    <head>
        <title>CRM - REST webservices test delete page</title>
        <base href="<%= AppPathService.getBaseUrl( request ) %>" />
	</head>
	<body>
<%
	String strIdRecord = request.getParameter( "id_directory_record" );
	String strUrl = AppPathService.getBaseUrl( request ) + "rest/directory/record/" + strIdRecord;
	
	DeleteMethod method = new DeleteMethod( strUrl );
	HttpClient client = new HttpClient(  );
	String strResponse = "";
	try
	{
		int nResponseCode = client.executeMethod( method );
		if ( nResponseCode == HttpURLConnection.HTTP_OK )
		{
			strResponse = method.getResponseBodyAsString(  );
		}
		else
		{
			strResponse = "Error getting URL : " + strUrl + " - return code : " + nResponseCode;
		}
	}
	finally
	{
		method.releaseConnection(  );
	}
	
	out.print( strResponse );
	out.flush();
%>
	
	</body>
</html>