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

import java.util.List;

import javax.servlet.ServletRequest;

import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.modules.rest.DirectoryRestService;
import fr.paris.lutece.plugins.directory.modules.rest.RecordFormater;
import fr.paris.lutece.plugins.directory.modules.rest.RecordFormaterJson;


/**
 * JSON Records List Handler
 */
public class JsonRecordsListHandler extends AbstractUriHandler
{
    private static final String URI_PATH_PATTERN_JSON = "records.json/";
    private static final String URI_PATH_PATTERN = "records/";
    private RecordFormater _formater = new RecordFormaterJson(  );

    /**
     * {@inheritDoc }
     */
    public boolean isHandledUri( String strURI, String strMethod, String strContentType )
    {
        if ( strMethod != METHOD_GET )
        {
            return false;
        }

        // URI using this format /records.json/
        if ( isUriPathMatch( strURI, URI_PATH_PATTERN_JSON ) )
        {
            return true;
        }

        // If URI using this format /records/ + content-type=application/json
        if ( isUriPathMatch( strURI, URI_PATH_PATTERN ) )
        {
            if ( isUriContentTypeMatch( strContentType, CONTENT_TYPE_JSON ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc }
     */
    public String processUri( String strURI, DirectoryRestService service, ServletRequest request )
    {
        String strResponse = "";

        int nDirectoryId = getDirectoryId( strURI );

        try
        {
            List<Record> listRecords = service.getRecordsList( nDirectoryId );
            strResponse = _formater.formatRecordsList( listRecords );
        }
        catch ( Exception ex )
        {
            strResponse = _formater.formatError( "1", ex.getMessage(  ) );
        }

        return strResponse;
    }
}
