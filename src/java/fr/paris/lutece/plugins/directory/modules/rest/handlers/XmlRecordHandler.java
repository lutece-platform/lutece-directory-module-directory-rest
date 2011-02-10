/*
 * Copyright (c) 2002-2009, Mairie de Paris
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

import javax.servlet.ServletRequest;

import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.modules.rest.DirectoryRestService;
import fr.paris.lutece.plugins.directory.modules.rest.RecordFormater;
import fr.paris.lutece.plugins.directory.modules.rest.RecordFormaterXml;


/**
 * XML Record URI Handler
 */
public class XmlRecordHandler extends AbstractUriHandler
{
    private static final String URI_PATH_PATTERN = "record/";
    private RecordFormater _formater = new RecordFormaterXml(  );

    /**
     * {@inheritDoc }
     */
    public boolean isHandledUri( String strURI, String strMethod, String strContentType )
    {
        if ( ( !isUriPathMatch( strURI, URI_PATH_PATTERN ) ) || ( !isUriExtensionMatch( strURI, EXTENSION_XML ) ) ||
                ( strMethod != METHOD_GET ) || ( isUriContentTypeMatch( strContentType, CONTENT_TYPE_JSON ) ) )
        {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc }
     */
    public String processUri( String strURI, DirectoryRestService service, ServletRequest request )
    {
        String strResponse = "";
        String strResourceId = getResourceId( strURI, URI_PATH_PATTERN );

        try
        {
            Record record = service.getRecord( strResourceId );
            strResponse = _formater.formatRecord( record );
        }
        catch ( Exception ex )
        {
            strResponse = _formater.formatError( "1", ex.getMessage(  ) );
        }

        return strResponse;
    }
}
