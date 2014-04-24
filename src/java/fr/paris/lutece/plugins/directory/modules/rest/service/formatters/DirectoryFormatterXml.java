/*
 * Copyright (c) 2002-2014, Mairie de Paris
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
package fr.paris.lutece.plugins.directory.modules.rest.service.formatters;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.modules.rest.util.constants.DirectoryRestConstants;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.rest.service.formatters.IFormatter;
import fr.paris.lutece.plugins.rest.util.xml.XMLUtil;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.xml.XmlUtil;

import org.apache.commons.lang.StringUtils;

import java.util.List;


/**
 *
 * Format directory output to the XML format
 * Example of the formatted directories list XML :
 * <code>
 * <Response>
 *                 <Status>SUCCESS</Status>
 *                 <Directories>
 *                         <Directory>
 *                                 <Id>1</Id>
 *                                 <Title>directoryTitle</Title>
 *                                 <Description>directoryDescription</Description>
 *                                 <IsEnable>true</IsEnable>
 *                                 <Role>none</Role>
 *                                 <Workgroup>all</Workgroup>
 *                                 <IdWorkflow>1</IdWorkflow>
 *                         </Directory>
 *                         <Directory>
 *                                 ...
 *                         </Directory>
 *                         ...
 *                 </Directories>
 * </Response>
 * </code>
 */
public class DirectoryFormatterXml implements IFormatter<Directory>
{
    /**
    * {@inheritDoc}
    */
    @Override
    public String formatError( String strCode, String strMessage )
    {
        return XMLUtil.formatError( strMessage, DirectoryUtils.convertStringToInt( strCode ) );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String format( Directory directory )
    {
        StringBuffer sbXml = new StringBuffer( AppPropertiesService.getProperty( XmlUtil.PROPERTIES_XML_HEADER ) );
        XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_RESPONSE );
        XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_STATUS, DirectoryRestConstants.STATUS_SUCCESS );

        formatDirectory( sbXml, directory );

        XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_RESPONSE );

        return sbXml.toString(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format( List<Directory> listDirectories )
    {
        StringBuffer sbXml = new StringBuffer( AppPropertiesService.getProperty( XmlUtil.PROPERTIES_XML_HEADER ) );
        XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_RESPONSE );
        XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_STATUS, DirectoryRestConstants.STATUS_SUCCESS );
        XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_DIRECTORIES );

        for ( Directory directory : listDirectories )
        {
            formatDirectory( sbXml, directory );
        }

        XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_DIRECTORIES );
        XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_RESPONSE );

        return sbXml.toString(  );
    }

    /**
     * Format the record
     * @param sbXml the XML
     * @param directory the directory to format
     */
    private void formatDirectory( StringBuffer sbXml, Directory directory )
    {
        if ( directory != null )
        {
            XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_DIRECTORY );
            XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_ID, directory.getIdDirectory(  ) );
            XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_TITLE, directory.getTitle(  ) );
            XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_DESCRIPTION, directory.getDescription(  ) );
            XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_IS_ENABLE, Boolean.toString( directory.isEnabled(  ) ) );

            if ( StringUtils.isNotBlank( directory.getRoleKey(  ) ) )
            {
                XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_ROLE, directory.getRoleKey(  ) );
            }

            if ( StringUtils.isNotBlank( directory.getWorkgroup(  ) ) )
            {
                XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_WORKGROUP, directory.getWorkgroup(  ) );
            }

            if ( directory.getIdWorkflow(  ) != DirectoryUtils.CONSTANT_ID_NULL )
            {
                XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_ID_WORKFLOW, directory.getIdWorkflow(  ) );
            }

            XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_DIRECTORY );
        }
        else
        {
            if ( AppLogService.isDebugEnabled(  ) )
            {
                AppLogService.debug( "Directory is null" );
            }
        }
    }
}
