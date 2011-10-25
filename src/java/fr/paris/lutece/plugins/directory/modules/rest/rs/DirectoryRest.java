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
package fr.paris.lutece.plugins.directory.modules.rest.rs;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.modules.rest.service.DirectoryRestService;
import fr.paris.lutece.plugins.directory.modules.rest.util.constants.DirectoryRestConstants;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;


/**
 *
 * DirectoryRest
 *
 */
@Path( RestConstants.BASE_PATH + DirectoryPlugin.PLUGIN_NAME )
public class DirectoryRest
{
    private DirectoryRestService _directoryRestService;

    // SET

    /**
     * Set the directory rest service
     * @param directoryRestService the directory rest service
     */
    public void setDirectoryRestService( DirectoryRestService directoryRestService )
    {
        _directoryRestService = directoryRestService;
    }

    /**
     * Get the wadl.xml content
     * @param request {@link HttpServletRequest}
     * @return the content of wadl.xml
     */
    @GET
    @Path( DirectoryRestConstants.PATH_WADL )
    @Produces( MediaType.APPLICATION_XML )
    public String getWADL( @Context
    HttpServletRequest request )
    {
        StringBuilder sbBase = new StringBuilder( AppPathService.getBaseUrl( request ) );

        if ( sbBase.toString(  ).endsWith( DirectoryRestConstants.SLASH ) )
        {
            sbBase.deleteCharAt( sbBase.length(  ) - 1 );
        }

        sbBase.append( RestConstants.BASE_PATH + DirectoryPlugin.PLUGIN_NAME );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( DirectoryRestConstants.MARK_BASE_URL, sbBase.toString(  ) );

        HtmlTemplate t = AppTemplateService.getTemplate( DirectoryRestConstants.TEMPLATE_WADL, request.getLocale(  ),
                model );

        return t.getHtml(  );
    }

    // GET

    /**
     * Get the record
     * @param nIdDirectory the id directory
     * @param nIdRecord the id record
     * @return the XML/JSON
     */
    @GET
    @Path( DirectoryRestConstants.PATH_ID_DIRECTORY + DirectoryRestConstants.PATH_RECORD +
    DirectoryRestConstants.PATH_ID_DIRECTORY_RECORD )
    @Produces( {MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    } )
    public List<Record> getRecord( @PathParam( DirectoryRestConstants.PARAMETER_ID_DIRECTORY )
    int nIdDirectory, @PathParam( DirectoryRestConstants.PARAMETER_ID_DIRECTORY_RECORD )
    int nIdRecord )
    {
        try
        {
            List<Record> listRecord = new ArrayList<Record>(  );
            Record record = _directoryRestService.getRecord( Integer.toString( nIdRecord ) );

            if ( ( record != null ) && ( record.getDirectory(  ) != null ) &&
                    ( record.getDirectory(  ).getIdDirectory(  ) == nIdDirectory ) )
            {
                listRecord.add( record );

                return listRecord;
            }
        }
        catch ( Exception e )
        {
            AppLogService.error( e );
        }

        return null;
    }

    /**
     * Get the records list
     * @param nIdDirectory the id directory
     * @return the XML/JSON
     */
    @GET
    @Path( DirectoryRestConstants.PATH_ID_DIRECTORY + DirectoryRestConstants.PATH_RECORDS )
    @Produces( {MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    } )
    public List<Record> getRecordsList( @PathParam( DirectoryRestConstants.PARAMETER_ID_DIRECTORY )
    int nIdDirectory )
    {
        try
        {
            return _directoryRestService.getRecordsList( nIdDirectory );
        }
        catch ( Exception e )
        {
            AppLogService.error( e );
        }

        return null;
    }

    /**
     * Get the directory
     * @param nIdDirectory the id directory
     * @return the XML/JSON
     */
    @GET
    @Path( DirectoryRestConstants.PATH_ID_DIRECTORY )
    @Produces( {MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    } )
    public List<Directory> getDirectory( @PathParam( DirectoryRestConstants.PARAMETER_ID_DIRECTORY )
    int nIdDirectory )
    {
        List<Directory> listDirectories = new ArrayList<Directory>(  );
        listDirectories.add( _directoryRestService.getDirectory( nIdDirectory ) );

        return listDirectories;
    }

    /**
     * Get the directories list
     * @param nIdDirectory the id directory
     * @return the XML/JSON
     */
    @GET
    @Path( StringUtils.EMPTY )
    @Produces( {MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    } )
    public List<Directory> getDirectoriesList( 
        @PathParam( DirectoryRestConstants.PARAMETER_ID_DIRECTORY )
    int nIdDirectory )
    {
        return _directoryRestService.getDirectoriesList(  );
    }

    // ACTIONS
}
