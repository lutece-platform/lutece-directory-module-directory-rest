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
package fr.paris.lutece.plugins.directory.modules.rest.service;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.DirectoryFilter;
import fr.paris.lutece.plugins.directory.business.DirectoryHome;
import fr.paris.lutece.plugins.directory.business.EntryFilter;
import fr.paris.lutece.plugins.directory.business.EntryHome;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.PhysicalFileHome;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.business.RecordFieldFilter;
import fr.paris.lutece.plugins.directory.business.RecordFieldHome;
import fr.paris.lutece.plugins.directory.business.RecordHome;
import fr.paris.lutece.plugins.directory.modules.rest.util.constants.DirectoryRestConstants;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.utils.DirectoryErrorException;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.directory.web.action.DirectoryAdminSearchFields;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.util.http.MultipartUtil;

import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;


/**
 *
 * DirectoryRestService
 *
 */
public class DirectoryRestService
{
    private static final String PARAMETER_DIRECTORY_ID = "directoryId";
    private static final String PARAMETER_RECORD_ID = "recordId";

    /** use this parameter to avoid workflow usage */
    private static final String PARAMETER_NO_WORKFLOW = "noWorkflow";

    /** LAUNCH WORKFLOW ACTION IF THE GIVEN ENTRY IS SET */
    private static final Plugin _pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

    // GET

    /**
     * Gets the record
     * @param nIdRecord resource id
     * @param request the HTTP request
     * @return the record
     * @throws DirectoryRestException if occurs
     * @throws DirectoryErrorException if occurs
     */
    public Record getRecord( int nIdRecord, HttpServletRequest request )
        throws DirectoryRestException, DirectoryErrorException
    {
        Record record = RecordHome.findByPrimaryKey( nIdRecord, _pluginDirectory );

        List<Integer> listIdsEntry = getIdsEntry( record.getDirectory(  ).getIdDirectory(  ), request );

        return getRecord( nIdRecord, listIdsEntry );
    }

    /**
     * Gets the record
     * @param nIdRecord resource id
     * @param listIdsEntry the list of ids entry
     * @return the record
     * @throws DirectoryRestException if occurs
     * @throws DirectoryErrorException if occurs
     */
    public Record getRecord( int nIdRecord, List<Integer> listIdsEntry )
        throws DirectoryRestException, DirectoryErrorException
    {
        Record record = RecordHome.findByPrimaryKey( nIdRecord, _pluginDirectory );

        List<RecordField> listRecordField = RecordFieldHome.getRecordFieldSpecificList( listIdsEntry, nIdRecord,
                _pluginDirectory );
        record.setListRecordField( listRecordField );

        return record;
    }

    /**
     * Finds the list
     * @param nIdDirectory the directory id
     * @param request the HTTP request
     * @return the record list
     * @throws DirectoryRestException if occurs
     * @throws DirectoryErrorException if occurs
     */
    public List<Record> getRecordsList( int nIdDirectory, HttpServletRequest request )
        throws DirectoryRestException, DirectoryErrorException
    {
        Directory directory = getDirectory( nIdDirectory );
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        DirectoryAdminSearchFields searchFields = new DirectoryAdminSearchFields(  );
        Enumeration paramNames = request.getParameterNames(  );
        HashMap<String, List<RecordField>> mapQuery = null;

        if ( paramNames.hasMoreElements(  ) )
        {
            mapQuery = DirectoryUtils.getSearchRecordData( request, nIdDirectory, DirectoryUtils.getPlugin(  ),
                    request.getLocale(  ) );
        }

        searchFields.setMapQuery( mapQuery );
        searchFields.setSortParameters( request, directory, pluginDirectory );

        boolean bWorkflowServiceEnable = WorkflowService.getInstance(  ).isAvailable(  );

        List<Integer> listIdsRecord = null;
        List<Record> listRecords = null;
        List<Integer> listIdsEntry = getIdsEntry( nIdDirectory, request );

        try
        {
            listIdsRecord = DirectoryUtils.getListResults( request, directory, bWorkflowServiceEnable, true,
                    searchFields, null, request.getLocale(  ) );
            listRecords = new ArrayList<Record>(  );

            if ( listIdsRecord != null )
            {
                int nMaxNumber = AppPropertiesService.getPropertyInt( DirectoryRestConstants.PROPERTY_MAX_NUMBER_RECORDS,
                        100 );

                for ( int i = 0; ( i < nMaxNumber ) && ( i < listIdsRecord.size(  ) ); i++ )
                {
                    int nIdRecord = listIdsRecord.get( i );
                    Record record = getRecord( nIdRecord, listIdsEntry );
                    listRecords.add( record );
                }
            }
        }
        catch ( AccessDeniedException e )
        {
            AppLogService.error( e );
        }

        return listRecords;
    }

    /**
     * Get the list of entries from the record
     * @param record the record
     * @return a list of {@link IEntry}
     */
    public List<IEntry> getEntries( Record record )
    {
        List<IEntry> listEntries = new ArrayList<IEntry>(  );

        if ( record.getListRecordField(  ) != null )
        {
            Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

            for ( RecordField recordField : record.getListRecordField(  ) )
            {
                if ( recordField.getEntry(  ) != null )
                {
                    int nIdEntry = recordField.getEntry(  ).getIdEntry(  );
                    IEntry entry = EntryHome.findByPrimaryKey( nIdEntry, pluginDirectory );

                    if ( ( entry != null ) && !listEntries.contains( entry ) )
                    {
                        listEntries.add( entry );
                    }
                }
            }
        }

        return listEntries;
    }

    /**
     * Get the ids entry from the parameters of the request.
     * <br />
     * If there is no ids entry in the parameter, then get all entries.
     * @param nIdDirectory the id directory
     * @param request the HTTP request
     * @return the list of id entry
     */
    public List<Integer> getIdsEntry( int nIdDirectory, HttpServletRequest request )
    {
        List<Integer> listIdsEntry = new ArrayList<Integer>(  );
        String[] strIdsEntry = request.getParameterValues( DirectoryRestConstants.PARAMETER_ID_ENTRY_FILTER );

        if ( strIdsEntry != null )
        {
            Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

            for ( String strIdEntry : strIdsEntry )
            {
                if ( StringUtils.isNotBlank( strIdEntry ) && StringUtils.isNumeric( strIdEntry ) )
                {
                    // Check if the entry in the parameter is indeed from the directory
                    int nIdEntry = Integer.parseInt( strIdEntry );
                    IEntry entry = EntryHome.findByPrimaryKey( nIdEntry, pluginDirectory );

                    if ( ( entry != null ) && ( entry.getDirectory(  ) != null ) &&
                            ( entry.getDirectory(  ).getIdDirectory(  ) == nIdDirectory ) )
                    {
                        listIdsEntry.add( nIdEntry );
                    }
                }
            }
        }
        else
        {
            listIdsEntry = getIdsEntry( nIdDirectory );
        }

        return listIdsEntry;
    }

    /**
     * Get all entries
     * @param nIdDirectory the id directory
     * @return the list of id entry
     */
    public List<Integer> getIdsEntry( int nIdDirectory )
    {
        List<Integer> listIdsEntry = new ArrayList<Integer>(  );

        EntryFilter filter = new EntryFilter(  );
        filter.setIdDirectory( nIdDirectory );
        filter.setIsComment( EntryFilter.FILTER_FALSE );
        filter.setIsEntryParentNull( EntryFilter.FILTER_TRUE );

        List<IEntry> listEntryFirstLevel = EntryHome.getEntryList( filter, _pluginDirectory );

        for ( IEntry entry : listEntryFirstLevel )
        {
            IEntry entryFistLevel = EntryHome.findByPrimaryKey( entry.getIdEntry(  ), _pluginDirectory );

            if ( ( entryFistLevel != null ) && entryFistLevel.getEntryType(  ).getGroup(  ) )
            {
                filter = new EntryFilter(  );
                filter.setIdEntryParent( entryFistLevel.getIdEntry(  ) );

                for ( IEntry entryChildren : EntryHome.getEntryList( filter, _pluginDirectory ) )
                {
                    IEntry entryTmp = EntryHome.findByPrimaryKey( entryChildren.getIdEntry(  ), _pluginDirectory );

                    if ( entryTmp != null )
                    {
                        listIdsEntry.add( entryTmp.getIdEntry(  ) );
                    }
                }
            }

            listIdsEntry.add( entry.getIdEntry(  ) );
        }

        return listIdsEntry;
    }

    /**
     * Get the directory given the id directory
     * @param nIdDirectory the id directory
     * @return the directory
     */
    public Directory getDirectory( int nIdDirectory )
    {
        Plugin plugin = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        return DirectoryHome.findByPrimaryKey( nIdDirectory, plugin );
    }

    /**
     * Get the list of directories
     * @return the list of directories
     */
    public List<Directory> getDirectoriesList(  )
    {
        Plugin plugin = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        return DirectoryHome.getDirectoryList( new DirectoryFilter(  ), plugin );
    }

    /**
     * Get the map of id entry - List de Record Field
     * @param record the record
     * @return the map
     */
    public Map<String, List<RecordField>> getMapIdEntryListRecordField( Record record )
    {
        Map<String, List<RecordField>> map = new HashMap<String, List<RecordField>>(  );

        if ( ( record.getListRecordField(  ) != null ) && !record.getListRecordField(  ).isEmpty(  ) )
        {
            for ( RecordField recordField : record.getListRecordField(  ) )
            {
                int nIdEntry = recordField.getEntry(  ).getIdEntry(  );
                List<RecordField> listRecordFields = map.get( Integer.toString( nIdEntry ) );

                if ( listRecordFields == null )
                {
                    listRecordFields = new ArrayList<RecordField>(  );
                }

                listRecordFields.add( recordField );
                map.put( Integer.toString( nIdEntry ), listRecordFields );
            }
        }

        return map;
    }

    // ACTION

    /**
     * Add a record in a directory with the parameters given if they match
     * @param strIdDirectory the directory id
     * @param request the HTTP request
     * @return the record
     * @throws DirectoryErrorException exception if there is an error
     */
    public Record addToDirectory( String strIdDirectory, ServletRequest request )
        throws DirectoryErrorException
    {
        int nDirectoryId = Integer.parseInt( strIdDirectory );
        Directory directory = DirectoryHome.findByPrimaryKey( nDirectoryId, _pluginDirectory );

        Record record = new Record(  );
        record.setDirectory( directory );
        record.setDateCreation( DirectoryUtils.getCurrentTimestamp(  ) );
        record.setEnabled( directory.isRecordActivated(  ) );

        List<RecordField> listRecordFields = getRecordFields( (HttpServletRequest) request, record );

        record.setListRecordField( listRecordFields );

        //save the Record and the RecordFiels
        record.setIdRecord( RecordHome.create( record, _pluginDirectory ) );

        // do not use the workflow if creation is partial
        String strNoWorkflowInit = request.getParameter( PARAMETER_NO_WORKFLOW );

        if ( StringUtils.isBlank( strNoWorkflowInit ) && isEntrySet( listRecordFields, nDirectoryId ) )
        {
            doWorkflowActions( record, directory );
        }

        return record;
    }

    /**
     * Creates or updates the record
     * @param request the request
     * @return the record created or updated
     * @throws DirectoryErrorException if occurs
     * @throws DirectoryRestException if occurs
     */
    public Record insertOrCompleteRecord( HttpServletRequest request )
        throws DirectoryErrorException, DirectoryRestException
    {
        String strIdRecord = request.getParameter( PARAMETER_RECORD_ID );

        if ( StringUtils.isNotBlank( strIdRecord ) && StringUtils.isNumeric( strIdRecord ) )
        {
            // strRecordId ==> update
            if ( AppLogService.isDebugEnabled(  ) )
            {
                AppLogService.debug( "Record id found, updating record " + strIdRecord );
            }

            int nIdRecord = Integer.parseInt( strIdRecord );

            return completeRecord( nIdRecord, request );
        }

        // strRecordId == null ==> create, strDirectoryId should not be null
        String strDirectoryId = request.getParameter( PARAMETER_DIRECTORY_ID );

        if ( AppLogService.isDebugEnabled(  ) )
        {
            AppLogService.debug( "Directory id found, inserting record into directory " + strDirectoryId );
        }

        return addToDirectory( strDirectoryId, request );
    }

    /**
     * delete the record
     * @param nIdRecord the id record
     * @param request the HTTP request
     * @return the record deleted
     * @throws DirectoryErrorException if occurs
     * @throws DirectoryRestException if occurs
     */
    public String deleteRecord( int nIdRecord, HttpServletRequest request )
        throws DirectoryErrorException, DirectoryRestException
    {
        if ( AppLogService.isDebugEnabled(  ) )
        {
            AppLogService.debug( "Record id found, deleting record " + nIdRecord );
        }

        Record record = getRecord( nIdRecord, request );
        RecordHome.remove( record.getIdRecord(  ), _pluginDirectory );
        WorkflowService.getInstance(  ).doRemoveWorkFlowResource( record.getIdRecord(  ), Record.WORKFLOW_RESOURCE_TYPE );

        return "record deleted";
    }

    /**
     * Gets record fields values from the request and complete the record for asynchronous record creation.
     * This method is <b>NOT</b> a modification of the record.
     * @param nIdRecord the id of the record to complete
     * @param request the request
     * @return the stored record
     * @throws DirectoryErrorException if a directory exception occurs
     * @throws DirectoryRestException if a rest exception occurs
     */
    public Record completeRecord( int nIdRecord, ServletRequest request )
        throws DirectoryErrorException, DirectoryRestException
    {
        Record record = getRecord( nIdRecord, (HttpServletRequest) request );

        List<RecordField> listRecordFields = getRecordFields( (HttpServletRequest) request, record );

        List<RecordField> listOldRecordField = record.getListRecordField(  );
        // remove all non-relevant old record fields (null record fields)
        removeNullRecordFields( listOldRecordField );

        for ( RecordField oldRecordField : listOldRecordField )
        {
            int nIdEntry = oldRecordField.getEntry(  ).getIdEntry(  );

            // idField should be 0 IF AND ONLY IF the entry is a "file". 
            int nIdField = ( oldRecordField.getField(  ) == null ) ? 0 : oldRecordField.getField(  ).getIdField(  );

            RecordField submitRecordField = findRecordField( nIdEntry, nIdField, listRecordFields );

            if ( submitRecordField == null )
            {
                listRecordFields.add( oldRecordField );
            }
            else
            {
                // old value is kept and NOT modified, the new value is dropped.
                if ( oldRecordField.getFile(  ) != null )
                {
                    // entry type file
                    removeRecordField( nIdEntry, nIdField, listRecordFields );
                    // get old field data
                    oldRecordField.getFile(  )
                                  .setPhysicalFile( PhysicalFileHome.findByPrimaryKey( 
                            oldRecordField.getFile(  ).getPhysicalFile(  ).getIdPhysicalFile(  ), _pluginDirectory ) );
                    listRecordFields.add( oldRecordField );
                }

                if ( oldRecordField.getValue(  ) != null )
                {
                    // other entries
                    removeRecordField( nIdEntry, nIdField, listRecordFields );
                    listRecordFields.add( oldRecordField );
                }
            }
        }

        record.setListRecordField( listRecordFields );

        RecordHome.updateWidthRecordField( record, _pluginDirectory );

        // do not use the workflow if update is partial
        String strNoWorkflowInit = request.getParameter( PARAMETER_NO_WORKFLOW );

        if ( StringUtils.isBlank( strNoWorkflowInit ) &&
                isEntrySet( listRecordFields, record.getDirectory(  ).getIdDirectory(  ) ) )
        {
            doWorkflowActions( record,
                DirectoryHome.findByPrimaryKey( record.getDirectory(  ).getIdDirectory(  ), _pluginDirectory ) );
        }

        return record;
    }

    /**
     * Update record
     * @param request the HTTP request
     * @return the record
     * @throws DirectoryErrorException exception if there is an error
     * @throws DirectoryRestException exception if there is an error
     */
    public Record updateRecord( HttpServletRequest request )
        throws DirectoryRestException, DirectoryErrorException
    {
        String strIdRecord = request.getParameter( PARAMETER_RECORD_ID );

        if ( StringUtils.isNotBlank( strIdRecord ) && StringUtils.isNumeric( strIdRecord ) )
        {
            int nIdRecord = Integer.parseInt( strIdRecord );
            Record record = getRecord( nIdRecord, request );

            if ( record != null )
            {
                List<RecordField> listRecordFields = getRecordFields( request, record );

                for ( RecordField recordField : listRecordFields )
                {
                    int idEntry = recordField.getEntry(  ).getIdEntry(  );
                    String strEntryId = request.getParameter( Integer.toString( idEntry ) );

                    if ( strEntryId != null )
                    {
                        RecordFieldFilter filter = new RecordFieldFilter(  );
                        filter.setIdEntry( idEntry );
                        filter.setIdRecord( record.getIdRecord(  ) );
                        RecordFieldHome.removeByFilter( filter, _pluginDirectory );
                        recordField.setRecord( record );
                        RecordFieldHome.create( recordField, _pluginDirectory );
                    }
                }

                return record;
            }
        }

        return null;
    }

    /**
     * Convert the HTTP request to a {@link MultipartHttpServletRequest}
     * @param request the HTTP request
     * @return a {@link MultipartHttpServletRequest}, null if the content is not multipart
     * @throws SizeLimitExceededException exception if the file size is too big
     * @throws FileUploadException exception if an unknown error has occurred
     */
    public MultipartHttpServletRequest convertRequest( HttpServletRequest request )
        throws SizeLimitExceededException, FileUploadException
    {
        int nSizeThreshold = AppPropertiesService.getPropertyInt( DirectoryRestConstants.PROPERTY_MULTIPART_SIZE_THRESHOLD,
                -1 );
        boolean bActivateNormalizeFileName = Boolean.getBoolean( AppPropertiesService.getProperty( 
                    DirectoryRestConstants.PROPERTY_MULTIPART_NORMALIZE_FILE_NAME ) );
        String strRequestSizeMax = AppPropertiesService.getProperty( DirectoryRestConstants.PROPERTY_MULTIPART_REQUEST_SIZE_MAX );
        long nRequestSizeMax = 0;

        if ( StringUtils.isNotBlank( strRequestSizeMax ) && StringUtils.isNumeric( strRequestSizeMax ) )
        {
            nRequestSizeMax = Long.parseLong( strRequestSizeMax );
        }

        return MultipartUtil.convert( nSizeThreshold, nRequestSizeMax, bActivateNormalizeFileName, request );
    }

    // PRIVATE METHODS

    /**
     * <code>true</code> if the entry is set, or if {@link #PROPERTY_FIELD_WORKFLOW_PREFIX} is empty, <code>false</code> otherwise.
     * This is use to bypass workflow initialization if the field is not set.
     * @param listRecordFields record field list
     * @param nIdDirectory the id directory
     * @return <code>true</code> if the entry is set, or if {@link #PROPERTY_FIELD_WORKFLOW_PREFIX} is empty, <code>false</code> otherwise.
     * @see #PROPERTY_FIELD_WORKFLOW_PREFIX
     */
    private boolean isEntrySet( List<RecordField> listRecordFields, int nIdDirectory )
    {
        int nIdEntry = AppPropertiesService.getPropertyInt( DirectoryRestConstants.PROPERTY_FIELD_WORKFLOW_PREFIX +
                nIdDirectory, DirectoryUtils.CONSTANT_ID_NULL );

        if ( nIdEntry == DirectoryUtils.CONSTANT_ID_NULL )
        {
            return true;
        }

        for ( RecordField recordField : listRecordFields )
        {
            if ( recordField.getEntry(  ).getIdEntry(  ) == nIdEntry )
            {
                if ( StringUtils.isNotBlank( recordField.getValue(  ) ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Inits workflow actions (if available).
     * @param record the record
     * @param directory the directory
     */
    private void doWorkflowActions( Record record, Directory directory )
    {
        if ( WorkflowService.getInstance(  ).isAvailable(  ) &&
                ( directory.getIdWorkflow(  ) != DirectoryUtils.CONSTANT_ID_NULL ) )
        {
            WorkflowService.getInstance(  )
                           .getState( record.getIdRecord(  ), Record.WORKFLOW_RESOURCE_TYPE,
                directory.getIdWorkflow(  ), Integer.valueOf( directory.getIdDirectory(  ) ), null );
            WorkflowService.getInstance(  )
                           .executeActionAutomatic( record.getIdRecord(  ), Record.WORKFLOW_RESOURCE_TYPE,
                directory.getIdWorkflow(  ), Integer.valueOf( directory.getIdDirectory(  ) ) );
        }
    }

    /**
     * Gets the record fields list for the record.
     * @param request the request
     * @param record the record
     * @return the record fields
     * @throws DirectoryErrorException if occurs
     */
    private List<RecordField> getRecordFields( HttpServletRequest request, Record record )
        throws DirectoryErrorException
    {
        List<RecordField> listRecordFields = new ArrayList<RecordField>(  );
        EntryFilter filter = new EntryFilter(  );
        filter.setIdDirectory( record.getDirectory(  ).getIdDirectory(  ) );
        filter.setIsComment( EntryFilter.FILTER_FALSE );
        filter.setIsEntryParentNull( EntryFilter.FILTER_TRUE );

        List<IEntry> listEntryFirstLevel = EntryHome.getEntryList( filter, _pluginDirectory );

        for ( IEntry entry : listEntryFirstLevel )
        {
            // no directory error testing (i.e. mandatory fields can be blanks)
            DirectoryUtils.getDirectoryRecordFieldData( record, request, entry.getIdEntry(  ), false, listRecordFields,
                _pluginDirectory, request.getLocale(  ) );
        }

        return listRecordFields;
    }

    /**
     * Finds the record field matching the entry id and field id
     * @param nIdEntry the entry id
     * @param nIdField the field Id
     * @param listRecordFields the list
     * @return the record field found, <code>null</code> otherwise.
     */
    private RecordField findRecordField( int nIdEntry, int nIdField, List<RecordField> listRecordFields )
    {
        for ( RecordField recordField : listRecordFields )
        {
            if ( isSameRecordField( nIdEntry, nIdField, recordField ) )
            {
                return recordField;
            }
        }

        return null;
    }

    /**
     * Removes the record field matching the entry id and field id from the list
     * @param nIdEntry entry id
     * @param nIdField field id
     * @param listRecordFields record fields to check
     */
    private void removeRecordField( int nIdEntry, int nIdField, List<RecordField> listRecordFields )
    {
        Iterator<RecordField> itRecordFields = listRecordFields.iterator(  );

        while ( itRecordFields.hasNext(  ) )
        {
            RecordField recordField = itRecordFields.next(  );

            if ( isSameRecordField( nIdEntry, nIdField, recordField ) )
            {
                itRecordFields.remove(  );

                break;
            }
        }
    }

    /**
     * Removes <code>null</code> record fields (i.e. with no file nor field nor value associated)
     * @param listRecordFields recordfield list.
     */
    private void removeNullRecordFields( List<RecordField> listRecordFields )
    {
        Iterator<RecordField> itRecordFields = listRecordFields.iterator(  );

        while ( itRecordFields.hasNext(  ) )
        {
            RecordField recordField = itRecordFields.next(  );

            if ( ( recordField.getField(  ) == null ) && ( recordField.getFile(  ) == null ) &&
                    ( recordField.getValue(  ) == null ) )
            {
                itRecordFields.remove(  );
            }
        }
    }

    /**
     * Checks if the record field has the same entry id and field id
     * @param nIdEntry the entry id
     * @param nIdField the field id
     * @param recordField the record field
     * @return boolean.
     */
    private boolean isSameRecordField( int nIdEntry, int nIdField, RecordField recordField )
    {
        if ( recordField.getEntry(  ).getIdEntry(  ) == nIdEntry )
        {
            int nIdFieldFound = ( recordField.getField(  ) == null ) ? 0 : recordField.getField(  ).getIdField(  );

            if ( nIdFieldFound == nIdField )
            {
                return true;
            }
        }

        return false;
    }
}
