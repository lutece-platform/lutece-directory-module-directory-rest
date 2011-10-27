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
package fr.paris.lutece.plugins.directory.modules.rest.util.constants;


/**
 *
 * DirectoryRestConstants
 *
 */
public final class DirectoryRestConstants
{
    // CONSTANTS
    public static final String SLASH = "/";
    public static final String STATUS_SUCCESS = "SUCCESS";

    // PATHS
    public static final String PATH_WADL = "wadl";
    public static final String PATH_ID_DIRECTORY = "{id_directory}";
    public static final String PATH_RECORD = "/record/";
    public static final String PATH_RECORDS = "/records/";
    public static final String PATH_ID_DIRECTORY_RECORD = "{id_directory_record}";

    // PARAMETERS
    public static final String PARAMETER_ID_DIRECTORY = "id_directory";
    public static final String PARAMETER_ID_DIRECTORY_RECORD = "id_directory_record";

    // PROPERTIES
    public static final String PROPERTY_FIELD_WORKFLOW_PREFIX = "directory-rest.entry.workflow.";

    // MESSAGES
    public static final String MESSAGE_DIRECTORY_REST = "Directory Rest - ";
    public static final String MESSAGE_RECORD_NOT_FOUND = "Record not found, ID : ";

    // MARKS
    public static final String MARK_BASE_URL = "base_url";

    // TAGS
    public static final String TAG_ID = "Id";
    public static final String TAG_WIDTH = "Width";
    public static final String TAG_HEIGHT = "Height";
    public static final String TAG_RESPONSE = "Response";
    public static final String TAG_RECORDS = "Records";
    public static final String TAG_RECORD = "Record";
    public static final String TAG_STATUS = "Status";
    public static final String TAG_DIRECTORIES = "Directories";
    public static final String TAG_DIRECTORY = "Directory";
    public static final String TAG_TITLE = "Title";
    public static final String TAG_DESCRIPTION = "Description";
    public static final String TAG_ROLE = "Role";
    public static final String TAG_WORKGROUP = "Workgroup";
    public static final String TAG_ID_WORKFLOW = "IdWorkflow";
    public static final String TAG_IS_ENABLE = "IsEnable";
    public static final String TAG_RECORD_FIELDS = "RecordFields";

    // TEMPLATES
    public static final String TEMPLATE_WADL = "admin/plugins/directory/modules/rest/wadl.xml";

    /**
     * Private constructor
     */
    private DirectoryRestConstants(  )
    {
    }
}
