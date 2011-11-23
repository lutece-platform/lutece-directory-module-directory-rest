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

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.Field;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.modules.rest.service.DirectoryRestService;
import fr.paris.lutece.plugins.directory.modules.rest.util.constants.DirectoryRestConstants;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.rest.business.resourceinfo.IResourceInfo;
import fr.paris.lutece.plugins.rest.service.formatters.IFormatter;
import fr.paris.lutece.plugins.rest.service.resourceinfo.ResourceInfoManager;
import fr.paris.lutece.plugins.rest.util.json.JSONUtil;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 *
 * Format record output to the JSON format
 * Example of a formatted record JSON :
 * <br />
 * <code>
 * [
 *         {
 *                 "Id":1,
 *                 "state":
 *                 {
 *                         "id-state":1,
 *                         "name":"state1",
 *                         "description":"State 1",
 *                         "id-workflow":"1",
 *                         "is-initial-state":"true",
 *                         "is-required-workgroup-assigned":"false"
 *                 },
 *                 "RecordFields":
 *                 [
 *                         {
 *                                 "group1":
 *                                 {
 *                                         "recordFieldTitle1":"recordFieldValue1"
 *                                 },
 *                                 {
 *                                         "recordFieldTitle2":
 *                                         {
 *                                                                                                "choiceTitle2":"choiceValue2",
 *                                               "choiceTitle3":"choiceValue3"
 *                                        }
 *                                 }
 *                         },
 *                         {
 *                                 "recordFieldTitle3":"recordFieldValue3"
 *                         },
 *                         {
 *                                 "recordFieldTitle4":
 *                                 {
 *                                                                                "choiceTitle4":"choiceValue4",
 *                                                                                "choiceTitle5":"choiceValue5"
 *                                }
 *                         }
 *
 *                 ]
 *         }
 *         {
 *                 "Id:2,
 *                 ...
 *  }
 * ]
 * </code>
 */
public class RecordFormatterJson implements IFormatter<Record>
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
     * {@inheritDoc }
     */
    public String formatError( String strCode, String strMessage )
    {
        return JSONUtil.formatError( strMessage, DirectoryUtils.convertStringToInt( strCode ) );
    }

    /**
     * {@inheritDoc }
     */
    public String format( Record record )
    {
        JSONObject jsonObject = new JSONObject(  );

        // Put the ID record
        jsonObject.element( DirectoryRestConstants.TAG_ID, record.getIdRecord(  ) );

        // Put the resource info
        formatResourceInfo( jsonObject, record );

        // Put the entry info
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<IEntry> listEntries = _directoryRestService.getEntries( record.getDirectory(  ).getIdDirectory(  ) );
        Map<String, List<RecordField>> mapIdsEntryListRecordFields = DirectoryUtils.getMapIdEntryListRecordField( listEntries,
                record.getIdRecord(  ), pluginDirectory );

        JSONArray jsonRecordField = new JSONArray(  );

        for ( IEntry entry : listEntries )
        {
            JSONObject jsonEntry = new JSONObject(  );

            if ( entry.getEntryType(  ).getGroup(  ) )
            {
                if ( ( entry.getChildren(  ) != null ) && !entry.getChildren(  ).isEmpty(  ) )
                {
                    String strTag = fr.paris.lutece.util.string.StringUtil.replaceAccent( entry.getTitle(  ) );
                    JSONObject json = new JSONObject(  );

                    for ( IEntry entryChild : entry.getChildren(  ) )
                    {
                        formatEntry( json, entryChild, mapIdsEntryListRecordFields );
                    }

                    jsonEntry.accumulate( strTag, json );
                }
            }
            else
            {
                formatEntry( jsonEntry, entry, mapIdsEntryListRecordFields );
            }

            if ( !jsonEntry.isEmpty(  ) )
            {
                jsonRecordField.element( jsonEntry );
            }
        }

        jsonObject.element( DirectoryRestConstants.TAG_RECORD_FIELDS, jsonRecordField );

        return jsonObject.toString(  );
    }

    /**
     * {@inheritDoc}
     */
    public String format( List<Record> listRecords )
    {
        JSONArray jsonArray = new JSONArray(  );

        for ( Record record : listRecords )
        {
            jsonArray.element( format( record ) );
        }

        return jsonArray.toString(  );
    }

    /**
     * Format the entry
     * Example of a formatted entry type Image/File/CheckBox/Select/RadioButton/Geolocation JSON :
     * <br />
     * <code>
     * {
     *                 "recordFieldTitle":
     *                 {
     *                                                 "choiceTitle1":"choiceValue1",
     *                      "choiceTitle2":"choiceValue2"
     *                }
     * }
     * </code>
     * <br />
     * Example of a formatted JSON for other entry types :
     * <br />
     * <code>
     * {
     *                 "recordFieldTitle":"recordFieldValue"
     * }
     * </code>
     * @param jsonObject the json object
     * @param entry the entry
     * @param mapIdsEntryListRecordFields the map ids entry - list record fields
     */
    private void formatEntry( JSONObject jsonObject, IEntry entry,
        Map<String, List<RecordField>> mapIdsEntryListRecordFields )
    {
        if ( entry != null )
        {
            List<RecordField> listRecordFields = mapIdsEntryListRecordFields.get( Integer.toString( 
                        entry.getIdEntry(  ) ) );

            if ( ( listRecordFields != null ) && !listRecordFields.isEmpty(  ) )
            {
                String strTag = fr.paris.lutece.util.string.StringUtil.replaceAccent( entry.getTitle(  ) );

                if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg ||
                        entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeFile ||
                        entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeCheckBox ||
                        entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeSelect ||
                        entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeRadioButton ||
                        entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeGeolocation )
                {
                    JSONObject json = new JSONObject(  );

                    for ( RecordField recordField : listRecordFields )
                    {
                        formatRecordField( json, entry, recordField );
                    }

                    jsonObject.accumulate( strTag, json );
                }
                else
                {
                    for ( RecordField recordField : listRecordFields )
                    {
                        jsonObject.accumulate( strTag,
                            entry.convertRecordFieldValueToString( recordField, _locale, false, false ) );
                    }
                }
            }
        }
    }

    /**
     * Format the record field
     * Example of the formatted record field JSON for entry type Image
     * (display the ID of the file) :
     * <br />
     * <code>
     * {
     *                 "Id":1,
     *                 "Width":100,
     *                 "Height":100
     * }
     * </code>
     * <br />
     * Example of the formatted record field JSON for entry type File
     * (display the ID of the file) :
     * <br />
     * <code>
     * {
     *                 "Id":2
     * }
     * </code>
     * <br />
     * Example of the formatted record field JSON for other entry types :
     * <br />
     * <code>
     * {
     *                 "choiceTitle1":"choiceValue1"
     * }
     * </code>
     * @param jsonObject the json object
     * @param entry the entry
     * @param recordField the record field
     */
    private void formatRecordField( JSONObject jsonObject, IEntry entry, RecordField recordField )
    {
        if ( ( recordField != null ) && ( recordField.getField(  ) != null ) )
        {
            Field field = recordField.getField(  );

            if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg &&
                    StringUtils.isNotBlank( field.getValue(  ) ) )
            {
                JSONObject jsonField = new JSONObject(  );

                if ( recordField.getFile(  ) != null )
                {
                    jsonField.element( DirectoryRestConstants.TAG_ID, recordField.getFile(  ).getIdFile(  ) );
                }

                if ( ( field.getWidth(  ) != 0 ) && ( field.getHeight(  ) != 0 ) )
                {
                    jsonField.element( DirectoryRestConstants.TAG_WIDTH, field.getWidth(  ) );
                    jsonField.element( DirectoryRestConstants.TAG_HEIGHT, field.getHeight(  ) );
                }

                jsonObject.accumulate( field.getValue(  ), jsonField );
            }
            else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeFile &&
                    StringUtils.isNotBlank( field.getValue(  ) ) && ( recordField.getFile(  ) != null ) )
            {
                JSONObject jsonField = new JSONObject(  );
                jsonField.element( DirectoryRestConstants.TAG_ID, recordField.getFile(  ).getIdFile(  ) );
                jsonObject.accumulate( field.getValue(  ), jsonField );
            }
            else if ( ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeCheckBox ||
                    entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeSelect ||
                    entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeRadioButton ) &&
                    StringUtils.isNotBlank( field.getValue(  ) ) )
            {
                jsonObject.element( field.getValue(  ),
                    entry.convertRecordFieldTitleToString( recordField, _locale, false ) );
            }
            else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeGeolocation &&
                    StringUtils.isNotBlank( field.getTitle(  ) ) )
            {
                jsonObject.element( field.getTitle(  ),
                    entry.convertRecordFieldTitleToString( recordField, _locale, false ) );
            }
            else
            {
                if ( StringUtils.isNotBlank( field.getValue(  ) ) )
                {
                    jsonObject.element( field.getValue(  ),
                        entry.convertRecordFieldValueToString( recordField, _locale, false, false ) );
                }
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
    }

    /**
     * Format the resource info
     * Example of the JSON :
     * <br />
     * <code>
     * [
     *         {
     *                 "state":
     *                 {
     *                         "id-state":1,
     *                         "name":"state1",
     *                         "description":"State 1",
     *                         "id-workflow":"1",
     *                         "is-initial-state":"true",
     *                         "is-required-workgroup-assigned":"false"
     *                 }
     *         },
     *         {
     *                 "resourceInfo2":
     *                 {
     *                         "id-resource-info-2":2,
     *                         "data":"somedata"
     *                 }
     *         }
     * ]
     * </code>
     * @param jsonObject the json
     * @param record the record
     */
    private void formatResourceInfo( JSONObject jsonObject, Record record )
    {
        Directory directory = _directoryRestService.getDirectory( record.getDirectory(  ).getIdDirectory(  ) );
        Map<String, String> mapParams = new HashMap<String, String>(  );
        mapParams.put( DirectoryRestConstants.PARAMETER_ID_RESOURCE, Integer.toString( record.getIdRecord(  ) ) );
        mapParams.put( DirectoryRestConstants.PARAMETER_RESOURCE_TYPE, Record.WORKFLOW_RESOURCE_TYPE );
        mapParams.put( DirectoryRestConstants.PARAMETER_ID_WORKFLOW, Integer.toString( directory.getIdWorkflow(  ) ) );

        List<IResourceInfo> listResourceInfos = ResourceInfoManager.getResourceInfo( mapParams );

        if ( ( listResourceInfos != null ) && !listResourceInfos.isEmpty(  ) )
        {
            for ( IResourceInfo resourceInfo : listResourceInfos )
            {
                formatResourceInfo( jsonObject, resourceInfo );
            }
        }
    }

    /**
     * Format the resource info
     * Example of the JSON :
     * <br />
     * <code>
     * {
     *         "state":
     *         {
     *                 "id-state":1,
     *                 "name":"state1",
     *                 "description":"State 1",
     *                 "id-workflow":"1",
     *                 "is-initial-state":"true",
     *                 "is-required-workgroup-assigned":"false"
     *         }
     * }
     * </code>
     * @param jsonObject the json
     * @param resourceInfo the resource info
     */
    private void formatResourceInfo( JSONObject jsonObject, IResourceInfo resourceInfo )
    {
        if ( resourceInfo != null )
        {
            if ( resourceInfo.hasChildren(  ) )
            {
                JSONObject jsonResourceInfo = new JSONObject(  );

                for ( IResourceInfo child : resourceInfo.getListChildren(  ) )
                {
                    formatResourceInfo( jsonResourceInfo, child );
                }

                jsonObject.element( resourceInfo.getKey(  ), jsonResourceInfo );
            }
            else
            {
                jsonObject.element( resourceInfo.getKey(  ), resourceInfo.getValue(  ) );
            }
        }
    }
}
