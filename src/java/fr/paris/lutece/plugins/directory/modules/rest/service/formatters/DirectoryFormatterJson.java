/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
import fr.paris.lutece.plugins.rest.util.json.JSONUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import java.util.List;


/**
 *
 * Format directory output to the JSON format
 * Example of the formatted directories list JSON :
 * <br />
 * <code>
 * [
 *         {
 *                 "Id":1,
 *                 "Title":"directoryTitle",
 *                 "Description":"directoryDescription",
 *                 "IsEnable":true,
 *                 "Role":"none",
 *                 "Workgroup":"all",
 *                 "IdWorkflow":1
 *         },
 *         {
 *                 ...
 *         },
 *         ...
 * ]
 * </code>
 *
 */
public class DirectoryFormatterJson implements IFormatter<Directory>
{
    /**
    * {@inheritDoc }
    */
    @Override
    public String formatError( String strCode, String strMessage )
    {
        return JSONUtil.formatError( strMessage, DirectoryUtils.convertStringToInt( strCode ) );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String format( Directory directory )
    {
        JSONObject jsonObject = new JSONObject(  );

        if ( directory != null )
        {
            jsonObject.element( DirectoryRestConstants.TAG_ID, directory.getIdDirectory(  ) );
            jsonObject.element( DirectoryRestConstants.TAG_TITLE, directory.getTitle(  ) );
            jsonObject.element( DirectoryRestConstants.TAG_DESCRIPTION, directory.getDescription(  ) );
            jsonObject.element( DirectoryRestConstants.TAG_IS_ENABLE, directory.isEnabled(  ) );

            if ( StringUtils.isNotBlank( directory.getRoleKey(  ) ) )
            {
                jsonObject.element( DirectoryRestConstants.TAG_ROLE, directory.getRoleKey(  ) );
            }

            if ( StringUtils.isNotBlank( directory.getWorkgroup(  ) ) )
            {
                jsonObject.element( DirectoryRestConstants.TAG_WORKGROUP, directory.getWorkgroup(  ) );
            }

            if ( directory.getIdWorkflow(  ) != DirectoryUtils.CONSTANT_ID_NULL )
            {
                jsonObject.element( DirectoryRestConstants.TAG_ID_WORKFLOW, directory.getIdWorkflow(  ) );
            }
        }

        return jsonObject.toString(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format( List<Directory> listDirectories )
    {
        JSONArray jsonArray = new JSONArray(  );

        for ( Directory directory : listDirectories )
        {
            jsonArray.element( format( directory ) );
        }

        return jsonArray.toString(  );
    }
}
