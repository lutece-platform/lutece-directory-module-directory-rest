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
package fr.paris.lutece.plugins.directory.modules.rest.service.http;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import javax.ws.rs.core.MultivaluedMap;


/**
 *
 * DirectoryRestHttpServletRequest
 * This wrapper allows the request to have additional parameters.
 *
 */
public class DirectoryRestHttpServletRequest extends HttpServletRequestWrapper
{
    private MultivaluedMap<String, String> _mapParameters;

    /**
     * Constructor
     * @param request the HTTP request
     * @param mapParameters the map parameters
     */
    public DirectoryRestHttpServletRequest( HttpServletRequest request, MultivaluedMap<String, String> mapParameters )
    {
        super( request );
        _mapParameters = mapParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter( String strName )
    {
        String strParameter = super.getParameter( strName );

        if ( StringUtils.isBlank( strParameter ) )
        {
            strParameter = _mapParameters.getFirst( strName );
        }

        return strParameter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getParameterValues( String strName )
    {
        List<String> listAllParamValues = new ArrayList<String>(  );

        // Get parameter values from HTTP request
        String[] paramValues = super.getParameterValues( strName );

        if ( paramValues != null )
        {
            listAllParamValues.addAll( Arrays.asList( paramValues ) );
        }

        // Get parameter values from additionnal parameters
        List<String> listParamValues = _mapParameters.get( strName );

        if ( ( listParamValues != null ) && !listParamValues.isEmpty(  ) )
        {
            listAllParamValues.addAll( listParamValues );
        }

        // Returns null if the list is empty
        if ( !listAllParamValues.isEmpty(  ) )
        {
            return listAllParamValues.toArray( new String[] {  } );
        }

        return null;
    }
}
