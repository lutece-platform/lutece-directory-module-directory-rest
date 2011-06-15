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

import fr.paris.lutece.plugins.directory.business.Field;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.util.string.StringUtil;

import net.sf.json.JSONObject;

import java.util.List;


/**
 * Format record output to the JSON format
 */
public class RecordFormaterJson implements RecordFormater
{
    private static final String TAG_ID = "Id";
    private static final String TAG_WIDTH = "Width";
    private static final String TAG_HEIGHT = "Height";

    /**
     * {@inheritDoc }
     */
    public String formatRecord( Record record )
    {
        JSONObject jsonObject = new JSONObject(  );

        // FIXME use a better structure for this JSON object. A field could be named "Id" and would cause an undeterministic behaviour.
        jsonObject.element( TAG_ID, record.getIdRecord(  ) );

        for ( RecordField field : record.getListRecordField(  ) )
        {
            formatRecordField( jsonObject, field );
        }

        return jsonObject.toString(  );
    }

    /**
     * {@inheritDoc }
     */
    public String formatError( String strCode, String strMessage )
    {
        return "Error : " + strMessage + " [" + strCode + "]";
    }

    public String formatRecordsList( List<Record> listRecords )
    {
        StringBuffer sbXml = new StringBuffer(  );
        sbXml.append( "[" );

        boolean bFirst = true;

        for ( Record record : listRecords )
        {
            if ( bFirst )
            {
                bFirst = false;
            }
            else
            {
                sbXml.append( "," );
            }

            sbXml.append( formatRecord( record ) );
        }

        sbXml.append( "]" );

        return sbXml.toString(  );
    }

    private void formatRecordField( JSONObject jsonObject, RecordField recordField )
    {
        IEntry entry = recordField.getEntry(  );

        if ( entry != null )
        {
            if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg )
            {
                Field field = recordField.getField(  );
                JSONObject json = new JSONObject(  );

                if ( field != null )
                {
                    JSONObject jsonField = new JSONObject(  );

                    // For Entry type Image, we put the ID file instead of the ID of the record field
                    if ( recordField.getFile(  ) != null )
                    {
                        jsonField.element( TAG_ID, recordField.getFile(  ).getIdFile(  ) );
                    }

                    if ( ( field.getWidth(  ) != 0 ) && ( field.getHeight(  ) != 0 ) )
                    {
                        jsonField.element( TAG_WIDTH, field.getWidth(  ) );
                        jsonField.element( TAG_HEIGHT, field.getHeight(  ) );
                    }

                    json.accumulate( field.getValue(  ), jsonField );
                }

                jsonObject.accumulate( StringUtil.replaceAccent( recordField.getEntry(  ).getTitle(  ) ), json );
            }
            else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeFile )
            {
                JSONObject jsonField = new JSONObject(  );

                // For Entry type Image, we put the ID file
                if ( recordField.getFile(  ) != null )
                {
                    jsonField.element( TAG_ID, recordField.getFile(  ).getIdFile(  ) );
                }

                jsonObject.accumulate( StringUtil.replaceAccent( recordField.getEntry(  ).getTitle(  ) ), jsonField );
            }
            else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeCheckBox )
            {
                JSONObject jsonField = new JSONObject(  );
                jsonField.element( recordField.getValue(  ), recordField.getField(  ).getTitle(  ) );
                jsonObject.accumulate( StringUtil.replaceAccent( recordField.getEntry(  ).getTitle(  ) ), jsonField );
            }
            else
            {
                jsonObject.element( StringUtil.replaceAccent( recordField.getEntry(  ).getTitle(  ) ),
                    recordField.getValue(  ) );
            }
        }
    }
}
