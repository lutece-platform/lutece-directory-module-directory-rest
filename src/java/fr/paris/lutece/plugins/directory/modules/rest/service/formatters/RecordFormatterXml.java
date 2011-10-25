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
package fr.paris.lutece.plugins.directory.modules.rest.service.formatters;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.directory.business.Field;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.modules.rest.service.DirectoryRestService;
import fr.paris.lutece.plugins.directory.modules.rest.service.resourceinfo.IRecordInfoProvider;
import fr.paris.lutece.plugins.directory.modules.rest.util.constants.DirectoryRestConstants;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.rest.service.formatters.IFormatter;
import fr.paris.lutece.plugins.rest.service.resourceinfo.ResourceInfoManager;
import fr.paris.lutece.plugins.rest.util.xml.XMLUtil;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.xml.XmlUtil;
import freemarker.template.utility.StringUtil;


/**
 *
 * Format record output to the XML format
 *
 */
public class RecordFormatterXml implements IFormatter<Record>
{
    private DirectoryRestService _directoryRestService;
    private Locale _locale = I18nService.getDefaultLocale(  );

    /**
     * Set the directory rest service
     * @param directoryRestService the directory rest service
     */
    public void setDirectoryRestService( DirectoryRestService directoryRestService )
    {
        _directoryRestService = directoryRestService;
    }

    /**
     * {@inheritDoc}
     */
    public String formatError( String strCode, String strMessage )
    {
        return XMLUtil.formatError( strMessage, DirectoryUtils.convertStringToInt( strCode ) );
    }

    /**
     * {@inheritDoc }
     */
    public String format( Record record )
    {
        StringBuffer sbXml = new StringBuffer( AppPropertiesService.getProperty( XmlUtil.PROPERTIES_XML_HEADER ) );
        XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_RESPONSE );
        XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_STATUS, DirectoryRestConstants.STATUS_SUCCESS );

        formatResourceInfo( record );
        formatRecord( sbXml, record );

        XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_RESPONSE );

        return sbXml.toString(  );
    }

    /**
     * {@inheritDoc}
     */
    public String format( List<Record> listRecords )
    {
        StringBuffer sbXml = new StringBuffer( AppPropertiesService.getProperty( XmlUtil.PROPERTIES_XML_HEADER ) );
        XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_RESPONSE );
        XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_STATUS, DirectoryRestConstants.STATUS_SUCCESS );
        XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_RECORDS );

        for ( Record record : listRecords )
        {
            formatRecord( sbXml, record );
        }

        XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_RECORDS );
        XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_RESPONSE );

        return sbXml.toString(  );
    }

    /**
     * Format the record
     * @param sbXml the XML
     * @param record the record to format
     */
    private void formatRecord( StringBuffer sbXml, Record record )
    {
        XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_RECORD );
        // FIXME use a better structure for this XML. A field could be named "Id" and would cause an undeterministic behaviour.
        XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_ID, record.getIdRecord(  ) );

        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<IEntry> listEntries = _directoryRestService.getEntries( record.getDirectory(  ).getIdDirectory(  ) );
        Map<String, List<RecordField>> mapIdsEntryListRecordFields = DirectoryUtils.getMapIdEntryListRecordField( listEntries,
                record.getIdRecord(  ), pluginDirectory );

        XmlUtil.beginElement( sbXml, DirectoryRestConstants.TAG_RECORD_FIELDS );

        for ( IEntry entry : listEntries )
        {
            if ( entry.getEntryType(  ).getGroup(  ) )
            {
                if ( ( entry.getChildren(  ) != null ) && !entry.getChildren(  ).isEmpty(  ) )
                {
                    String strTag = fr.paris.lutece.util.string.StringUtil.replaceAccent( entry.getTitle(  ) );
                    XmlUtil.beginElement( sbXml, strTag );

                    for ( IEntry entryChild : entry.getChildren(  ) )
                    {
                        sbXml.append( getEntryXml( entryChild, mapIdsEntryListRecordFields ) );
                    }

                    XmlUtil.endElement( sbXml, strTag );
                }
            }
            else
            {
                sbXml.append( getEntryXml( entry, mapIdsEntryListRecordFields ) );
            }
        }

        XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_RECORD_FIELDS );
        XmlUtil.endElement( sbXml, DirectoryRestConstants.TAG_RECORD );
    }

    /**
     * Get the entry XML
     * @param entry the entry
     * @param mapIdsEntryListRecordFields the map ids entry - list record fields
     * @return the entry XML
     */
    private String getEntryXml( IEntry entry, Map<String, List<RecordField>> mapIdsEntryListRecordFields )
    {
        StringBuffer sbXml = new StringBuffer(  );

        if ( entry != null )
        {
            List<RecordField> listRecordFields = mapIdsEntryListRecordFields.get( Integer.toString( 
                        entry.getIdEntry(  ) ) );

            if ( ( listRecordFields != null ) && !listRecordFields.isEmpty(  ) )
            {
                String strTag = fr.paris.lutece.util.string.StringUtil.replaceAccent( entry.getTitle(  ) );

                if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeCheckBox ||
                        entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeSelect )
                {
                    // Multi choices => multi tags
                    for ( RecordField recordField : listRecordFields )
                    {
                        XmlUtil.beginElement( sbXml, strTag );
                        sbXml.append( getRecordFieldXml( entry, recordField ) );
                        XmlUtil.endElement( sbXml, strTag );
                    }
                }
                else
                {
                    XmlUtil.beginElement( sbXml, strTag );

                    for ( RecordField recordField : listRecordFields )
                    {
                        sbXml.append( getRecordFieldXml( entry, recordField ) );
                    }

                    XmlUtil.endElement( sbXml, strTag );
                }
            }
        }

        return sbXml.toString(  );
    }

    /**
     * Get the record field XML
     * @param entry the entry
     * @param recordField the record field
     * @return the XML of the record field
     */
    private String getRecordFieldXml( IEntry entry, RecordField recordField )
    {
        StringBuffer sbXml = new StringBuffer(  );

        if ( recordField != null )
        {
            if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg )
            {
                Field field = recordField.getField(  );

                if ( field != null )
                {
                    XmlUtil.beginElement( sbXml, field.getValue(  ) );

                    // For Entry type Image, we put the ID file instead of the ID of the record field
                    if ( recordField.getFile(  ) != null )
                    {
                        XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_ID, recordField.getFile(  ).getIdFile(  ) );
                    }

                    if ( ( field.getWidth(  ) != 0 ) && ( field.getHeight(  ) != 0 ) )
                    {
                        XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_WIDTH, field.getWidth(  ) );
                        XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_HEIGHT, field.getHeight(  ) );
                    }

                    XmlUtil.endElement( sbXml, field.getValue(  ) );
                }
            }
            else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeFile )
            {
                // For Entry type File, we put the ID file
                if ( recordField.getFile(  ) != null )
                {
                    XmlUtil.addElement( sbXml, DirectoryRestConstants.TAG_ID, recordField.getFile(  ).getIdFile(  ) );
                }
            }
            else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeCheckBox ||
                    entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeSelect ||
                    entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeRadioButton )
            {
                sbXml.append( StringUtil.XMLEnc( entry.convertRecordFieldTitleToString( recordField, _locale, false ) ) );
            }
            else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeGeolocation )
            {
                Field field = recordField.getField(  );

                if ( ( field != null ) && StringUtils.isNotBlank( field.getTitle(  ) ) )
                {
                    XmlUtil.addElement( sbXml, field.getTitle(  ),
                        entry.convertRecordFieldValueToString( recordField, _locale, false, false ) );
                }
            }
            else
            {
                sbXml.append( StringUtil.XMLEnc( entry.convertRecordFieldValueToString( recordField, _locale, false,
                            false ) ) );
            }
        }
        else
        {
            if ( AppLogService.isDebugEnabled(  ) )
            {
                AppLogService.debug( "Record field is null for entry " + entry.getTitle(  ) + " (" +
                    entry.getIdEntry(  ) + ")" );
            }
        }

        return sbXml.toString(  );
    }

    /**
     * Format the resource info
     * @param record the record
     * @return the XML
     */
    private String formatResourceInfo( Record record )
    {
        StringBuffer sbXml = new StringBuffer(  );
        Map<String, String> listResourceInfos = ResourceInfoManager.getResourceInfo( IRecordInfoProvider.class,
                Integer.toString( record.getIdRecord(  ) ) );

        if ( ( listResourceInfos != null ) && !listResourceInfos.isEmpty(  ) )
        {
            for ( Entry<String, String> resourceInfo : listResourceInfos.entrySet(  ) )
            {
                XmlUtil.addElement( sbXml, resourceInfo.getKey(  ), resourceInfo.getValue(  ) );
            }
        }

        return sbXml.toString(  );
    }
}
