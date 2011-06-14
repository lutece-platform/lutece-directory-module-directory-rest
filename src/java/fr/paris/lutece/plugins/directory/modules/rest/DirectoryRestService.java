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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.directory.business.Directory;
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
import fr.paris.lutece.plugins.directory.utils.DirectoryErrorException;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;


/**
 * DirectoryRestService
 */
public class DirectoryRestService
{
	private static final String PARAMETER_DIRECTORY_ID = "directoryId";
	private static final String PARAMETER_RECORD_ID = "recordId";
	/** use this parameter to avoid workflow usage */
	private static final String PARAMETER_NO_WORKFLOW = "noWorkflow";
	/** LAUNCH WORKFLOW ACTION IF THE GIVEN ENTRY IS SET */
	private static final String PROPERTY_FIELD_WORKFLOW_PREFIX = "directory-rest.entry.workflow.";
    private static final String PLUGIN_DIRECTORY = "directory";
    private static final int ENTRY_NOT_SET = -1;
    private static final Plugin _pluginDirectory = PluginService.getPlugin( PLUGIN_DIRECTORY );

    /**
     * Gets the record
     * @param strRessourceId resource id
     * @return the record
     * @throws DirectoryRestException if occurs
     * @throws DirectoryErrorException if occurs
     */
    public Record getRecord( String strRessourceId ) throws DirectoryRestException, DirectoryErrorException
    {
        int nRecordId = Integer.parseInt( strRessourceId );
        Record record = RecordHome.findByPrimaryKey( nRecordId, _pluginDirectory );

        EntryFilter filter = new EntryFilter(  );
        filter.setIdDirectory( record.getDirectory(  ).getIdDirectory(  ) );
        filter.setIsComment( EntryFilter.FILTER_FALSE );
        filter.setIsEntryParentNull( EntryFilter.FILTER_TRUE );

        List<IEntry> listEntryFirstLevel = EntryHome.getEntryList( filter, _pluginDirectory );
        List<Integer> listId = new ArrayList<Integer>(  );

        for ( IEntry entry : listEntryFirstLevel )
        {
            listId.add( entry.getIdEntry(  ) );
        }

        List<RecordField> listRecordField = RecordFieldHome.getRecordFieldSpecificList( listId, nRecordId,
                _pluginDirectory );
        record.setListRecordField( listRecordField );

        return record;
    }

    /**
     * Finds the list
     * @param nDirectoryId the directory id
     * @return the record list
     * @throws DirectoryRestException if occurs
     * @throws DirectoryErrorException if occurs
     */
    public List<Record> getRecordsList( int nDirectoryId )
        throws DirectoryRestException, DirectoryErrorException
    {
        List<Record> listRecords = new ArrayList<Record>(  );
        RecordFieldFilter filter = new RecordFieldFilter(  );
        filter.setIdDirectory( nDirectoryId );

        for ( Integer i : RecordHome.getListRecordId( filter, _pluginDirectory ) )
        {
            Record record = getRecord( i.toString(  ) );
            listRecords.add( record );
        }

        return listRecords;
    }

    /**
     * Add a record in a directory with the parameters given if they match
     * @param strDirectoryId
     * @param mapParameters
     * @throws DirectoryErrorException
     */
    public Record addToDirectory( String strDirectoryId, ServletRequest request )
        throws DirectoryErrorException
    {
        int nDirectoryId = Integer.parseInt( strDirectoryId );
        Directory directory = DirectoryHome.findByPrimaryKey( nDirectoryId, _pluginDirectory );

        Record record = new Record(  );
        record.setDirectory( directory );
        record.setDateCreation( DirectoryUtils.getCurrentTimestamp(  ) );
        record.setEnabled( directory.isRecordActivated(  ) );
        
        List<RecordField> listRecordFields = getRecordFields( ( HttpServletRequest ) request, record );

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
     * <code>true</code> if the entry is set, or if {@link #PROPERTY_FIELD_WORKFLOW_PREFIX} is empty, <code>false</code> otherwise.
     * This is use to bypass workflow initialization if the field is not set.
     * @param listRecordFields record field list
     * @return <code>true</code> if the entry is set, or if {@link #PROPERTY_FIELD_WORKFLOW_PREFIX} is empty, <code>false</code> otherwise.
     * @see #PROPERTY_FIELD_WORKFLOW_PREFIX
     */
    private boolean isEntrySet( List<RecordField> listRecordFields, int nIdDirectory )
    {
    	int nIdEntry = AppPropertiesService.getPropertyInt( PROPERTY_FIELD_WORKFLOW_PREFIX + nIdDirectory, ENTRY_NOT_SET );
    	if ( nIdEntry == ENTRY_NOT_SET )
    	{
    		return true;
    	}
    	
    	
    	for ( RecordField recordField : listRecordFields )
    	{
    		if ( recordField.getEntry().getIdEntry() == nIdEntry )
    		{
    			if ( StringUtils.isNotBlank( recordField.getValue() ) )
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
    private List<RecordField> getRecordFields( HttpServletRequest request, Record record ) throws DirectoryErrorException
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
            DirectoryUtils.getDirectoryRecordFieldData( record, request, entry.getIdEntry(  ), false,
            		listRecordFields, _pluginDirectory, request.getLocale(  ) );
        }
        
        return listRecordFields;
    }
    
    /**
     * Creates or updates the record
     * @param request the request
     * @return the record created or updated
     * @throws DirectoryErrorException if occurs
     * @throws DirectoryRestException if occurs
     */
    public Record insertOrCompleteRecord( HttpServletRequest request ) throws DirectoryErrorException, DirectoryRestException
    {
    	String strRecordId = request.getParameter( PARAMETER_RECORD_ID );
    	if ( StringUtils.isNotBlank( strRecordId ) )
    	{
    		// strRecordId ==> update
	    	if ( AppLogService.isDebugEnabled(  ) )
			{
				AppLogService.debug( "Record id found, updating record " + strRecordId );
			}
	    	
	    	return completeRecord( strRecordId, request );
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
     * Gets record fields values from the request and complete the record for asynchronous record creation.
     * This method is <b>NOT</b> a modification of the record.
     * @param strRecordId the id of the record to complete
     * @param request the request
     * @return the stored record
     * @throws DirectoryErrorException if a directory exception occurs
     * @throws DirectoryRestException if a rest exception occurs
     */
    public Record completeRecord( String strRecordId, ServletRequest request )
    	throws DirectoryErrorException, DirectoryRestException
    {
    	Record record = getRecord( strRecordId );
    	
    	List<RecordField> listRecordFields = getRecordFields( ( HttpServletRequest ) request, record );
    	
    	List<RecordField> listOldRecordField = record.getListRecordField(  );
    	// remove all non-relevant old record fields (null record fields)
    	removeNullRecordFields( listOldRecordField );
    	
    	for ( RecordField oldRecordField : listOldRecordField )
    	{
    		int nIdEntry = oldRecordField.getEntry(  ).getIdEntry(  );
    		// idField should be 0 IF AND ONLY IF the entry is a "file". 
    		int nIdField = oldRecordField.getField(  ) == null ? 0 : oldRecordField.getField(  ).getIdField(  );
    		
    		RecordField submitRecordField = findRecordField( nIdEntry, nIdField, listRecordFields );
    		
    		if ( submitRecordField == null )
    		{
    			listRecordFields.add( oldRecordField );
    		}
    		else
    		{
    			// old value is kept and NOT modified, the new value is dropped.
    			if (  oldRecordField.getFile(  ) != null )
    			{
    				// entry type file
    				removeRecordField( nIdEntry, nIdField, listRecordFields );
    				// get old field data
    				oldRecordField.getFile(  ).setPhysicalFile( PhysicalFileHome.findByPrimaryKey( 
    						oldRecordField.getFile(  ).getPhysicalFile(  ).getIdPhysicalFile(  ),
		                 _pluginDirectory ) );
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
        if ( StringUtils.isBlank( strNoWorkflowInit ) && isEntrySet( listRecordFields, record.getDirectory().getIdDirectory() ) )
        {
        	doWorkflowActions( record, DirectoryHome.findByPrimaryKey( record.getDirectory().getIdDirectory(), _pluginDirectory ) );
        }
    	
    	return record;
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
     * Checks if the record field has the same entry id and field id
     * @param nIdEntry the entry id
     * @param nIdField the field id
     * @param recordField the record field
     * @return boolean.
     */
    private boolean isSameRecordField( int nIdEntry, int nIdField, RecordField recordField )
    {
    	if ( recordField.getEntry().getIdEntry(  ) == nIdEntry )
		{
			int nIdFieldFound = recordField.getField(  ) == null ? 0 : recordField.getField(  ).getIdField(  );
			
			if ( nIdFieldFound == nIdField )
			{
				return true;
			}
		}
    	
    	return false;
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
    		if ( recordField.getField() == null && recordField.getFile() == null && recordField.getValue() == null )
    		{
   				itRecordFields.remove(  );
    		}
    	}
    }
}
