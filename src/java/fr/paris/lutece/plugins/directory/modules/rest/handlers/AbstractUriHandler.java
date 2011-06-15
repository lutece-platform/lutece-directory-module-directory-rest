/*
 * Copyright (c) 2002-2011, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.directory.modules.rest.handlers;


/**
 * Abstract Uri Handler
 */
public abstract class AbstractUriHandler implements UriHandler
{
    protected static final String EXTENSION_XML = "xml";
    protected static final String EXTENSION_JSON = "json";
    protected static final String CONTENT_TYPE_JSON = "application/json";
    protected static final String METHOD_GET = "GET";
    protected static final String METHOD_POST = "POST";
    public static final String URI_BASE_PATH_PATTERN = "/jsp/site/rest/directory/";

    protected boolean isUriExtensionMatch( String strUri, String strValidExtension )
    {
        String strExtension = EXTENSION_XML; // Default extension
        int nPos = strUri.lastIndexOf( "." );

        if ( nPos > 0 )
        {
            strExtension = strUri.substring( nPos + 1 );
        }

        return strExtension.equalsIgnoreCase( strValidExtension );
    }

    public static boolean isUriPathGloballyMatch( String strUri )
    {
        int nPos = strUri.indexOf( URI_BASE_PATH_PATTERN );

        return nPos > 0;
    }

    protected boolean isUriPathMatch( String strUri, String strPath )
    {
        int nPos = strUri.indexOf( strPath );

        return nPos > 0;
    }

    protected boolean isUriContentTypeMatch( String strValidContentType, String strContentType )
    {
        return strContentType.equalsIgnoreCase( strValidContentType );
    }

    protected String getResourceId( String strURI, String strPattern )
    {
        int nPos = strURI.indexOf( strPattern );
        String strResourceId = strURI.substring( nPos + strPattern.length(  ) );

        // Remove eventual extension
        nPos = strResourceId.lastIndexOf( "." );

        if ( nPos > 0 )
        {
            strResourceId = strResourceId.substring( 0, nPos );
        }

        return strResourceId;
    }

    protected int getDirectoryId( String strURI )
    {
        int nPos = strURI.indexOf( URI_BASE_PATH_PATTERN );
        String strURI2 = strURI.substring( nPos + URI_BASE_PATH_PATTERN.length(  ) );
        int nPos2 = strURI2.indexOf( "/" );
        String strResourceId = strURI2.substring( 0, nPos2 );
        int nResourceId = Integer.parseInt( strResourceId );

        return nResourceId;
    }
}
