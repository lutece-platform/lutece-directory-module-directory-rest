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
package fr.paris.lutece.plugins.directory.modules.rest.service;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.utils.DirectoryErrorException;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;

import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;


/**
 *
 * IDirectoryRestService
 *
 */
public interface IDirectoryRestService
{
    // GET

    /**
     * Gets the record
     * @param nIdRecord resource id
     * @param request the HTTP request
     * @return the record
     * @throws DirectoryRestException if occurs
     * @throws DirectoryErrorException if occurs
     */
    Record getRecord( int nIdRecord, HttpServletRequest request )
        throws DirectoryRestException, DirectoryErrorException;

    /**
     * Gets the record
     * @param nIdRecord resource id
     * @param listIdsEntry the list of ids entry
     * @return the record
     * @throws DirectoryRestException if occurs
     * @throws DirectoryErrorException if occurs
     */
    Record getRecord( int nIdRecord, List<Integer> listIdsEntry )
        throws DirectoryRestException, DirectoryErrorException;

    /**
     * Finds the list
     * @param nIdDirectory the directory id
     * @param request the HTTP request
     * @return the record list
     * @throws DirectoryRestException if occurs
     * @throws DirectoryErrorException if occurs
     */
    List<Record> getRecordsList( int nIdDirectory, HttpServletRequest request )
        throws DirectoryRestException, DirectoryErrorException;

    /**
     * Get the list of entries from the record
     * @param record the record
     * @return a list of {@link IEntry}
     */
    List<IEntry> getEntries( Record record );

    /**
     * Get the ids entry from the parameters of the request.
     * <br />
     * If there is no ids entry in the parameter, then get all entries.
     * @param nIdDirectory the id directory
     * @param request the HTTP request
     * @return the list of id entry
     */
    List<Integer> getIdsEntry( int nIdDirectory, HttpServletRequest request );

    /**
     * Get all entries
     * @param nIdDirectory the id directory
     * @return the list of id entry
     */
    List<Integer> getIdsEntry( int nIdDirectory );

    /**
     * Get the directory given the id directory
     * @param nIdDirectory the id directory
     * @return the directory
     */
    Directory getDirectory( int nIdDirectory );

    /**
     * Get the list of directories
     * @return the list of directories
     */
    List<Directory> getDirectoriesList(  );

    /**
     * Get the map of id entry - List de Record Field
     * @param record the record
     * @return the map
     */
    Map<String, List<RecordField>> getMapIdEntryListRecordField( Record record );

    // ACTION

    /**
     * Add a record in a directory with the parameters given if they match
     * @param strIdDirectory the directory id
     * @param request the HTTP request
     * @return the record
     * @throws DirectoryErrorException exception if there is an error
     */
    Record addToDirectory( String strIdDirectory, ServletRequest request )
        throws DirectoryErrorException;

    /**
     * Creates or updates the record
     * @param request the request
     * @return the record created or updated
     * @throws DirectoryErrorException if occurs
     * @throws DirectoryRestException if occurs
     */
    Record insertOrCompleteRecord( HttpServletRequest request )
        throws DirectoryErrorException, DirectoryRestException;

    /**
     * delete the record
     * @param nIdRecord the id record
     * @param request the HTTP request
     * @return the record deleted
     * @throws DirectoryErrorException if occurs
     * @throws DirectoryRestException if occurs
     */
    String deleteRecord( int nIdRecord, HttpServletRequest request )
        throws DirectoryErrorException, DirectoryRestException;

    /**
     * Gets record fields values from the request and complete the record for asynchronous record creation.
     * This method is <b>NOT</b> a modification of the record.
     * @param nIdRecord the id of the record to complete
     * @param request the request
     * @return the stored record
     * @throws DirectoryErrorException if a directory exception occurs
     * @throws DirectoryRestException if a rest exception occurs
     */
    Record completeRecord( int nIdRecord, ServletRequest request )
        throws DirectoryErrorException, DirectoryRestException;

    /**
     * Update record
     * @param request the HTTP request
     * @return the record
     * @throws DirectoryErrorException exception if there is an error
     * @throws DirectoryRestException exception if there is an error
     */
    Record updateRecord( HttpServletRequest request ) throws DirectoryRestException, DirectoryErrorException;

    /**
     * Convert the HTTP request to a {@link MultipartHttpServletRequest}
     * @param request the HTTP request
     * @return a {@link MultipartHttpServletRequest}, null if the content is not multipart
     * @throws SizeLimitExceededException exception if the file size is too big
     * @throws FileUploadException exception if an unknown error has occurred
     */
    MultipartHttpServletRequest convertRequest( HttpServletRequest request )
        throws SizeLimitExceededException, FileUploadException;
}
