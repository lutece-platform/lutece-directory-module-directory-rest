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
package fr.paris.lutece.plugins.directory.modules.rest;

import fr.paris.lutece.plugins.directory.modules.rest.handlers.AbstractUriHandler;
import fr.paris.lutece.plugins.directory.modules.rest.handlers.UriHandler;
import fr.paris.lutece.plugins.directory.modules.rest.handlers.UriHandlersRegistry;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * Servlet filter handling REST request
 */
public class DirectoryRestFilter implements Filter
{
    private static final String BEAN_HANDLERS_REGISTRY = "listUriHandlers";
    private static final String MODULE_NAME = "directory-rest";
    private static final String PROPERTY_RESPONSE_ENCODING = "directory-rest.responseEncoding";
    private DirectoryRestService _service;
    private List<UriHandler> _listHandlers;

    /**
     * {@inheritDoc }
     */
    public void init( FilterConfig config ) throws ServletException
    {
        _service = new DirectoryRestService(  );

        UriHandlersRegistry registry = (UriHandlersRegistry) SpringContextService.getPluginBean( MODULE_NAME,
                BEAN_HANDLERS_REGISTRY );
        _listHandlers = registry.getHandlersList(  );
    }

    /**
     * {@inheritDoc }
     */
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain filterChain )
        throws IOException, ServletException
    {
        String strResponse = "URI not handled!";
        String strURI = ( (HttpServletRequest) request ).getRequestURI(  );
        String strMethod = ( (HttpServletRequest) request ).getMethod(  );

        if ( AbstractUriHandler.isUriPathGloballyMatch( strURI ) )
        {
            for ( UriHandler handler : _listHandlers )
            {
                if ( handler.isHandledUri( strURI, strMethod, request.getContentType(  ) ) )
                {
                    strResponse = handler.processUri( strURI, _service, request );
                }
            }
        }

        String strCharacterEncoding = AppPropertiesService.getProperty( PROPERTY_RESPONSE_ENCODING );

        if ( StringUtils.isNotBlank( strCharacterEncoding ) )
        {
            response.setCharacterEncoding( strCharacterEncoding );
        }

        PrintWriter out = response.getWriter(  );
        out.println( strResponse );
        out.flush(  );
        out.close(  );
    }

    /**
     * {@inheritDoc }
     */
    public void destroy(  )
    {
    }
}
