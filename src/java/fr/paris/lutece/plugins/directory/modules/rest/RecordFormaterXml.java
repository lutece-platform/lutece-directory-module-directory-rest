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
import fr.paris.lutece.util.xml.XmlUtil;

import freemarker.template.utility.StringUtil;

import java.util.List;


/**
 * Format record output to the XML format
 */
public class RecordFormaterXml implements RecordFormater
{
    private static final String TAG_RESPONSE = "Response";
    private static final String TAG_RECORDS = "Records";
    private static final String TAG_RECORD = "Record";
    private static final String TAG_ID = "Id";
    private static final String TAG_STATUS = "Status";
    private static final String TAG_ERROR = "Error";
    private static final String TAG_CODE = "Code";
    private static final String TAG_MESSAGE = "Message";
    private static final String TAG_WIDTH = "Width";
    private static final String TAG_HEIGHT = "Height";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";
    private static final String HEADER_XML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n";

    /**
     * {@inheritDoc }
     */
    public String formatRecord( Record record )
    {
        StringBuffer sbXml = new StringBuffer( HEADER_XML );
        XmlUtil.beginElement( sbXml, TAG_RESPONSE );
        XmlUtil.addElement( sbXml, TAG_STATUS, STATUS_SUCCESS );
        XmlUtil.beginElement( sbXml, TAG_RECORD );
        // FIXME use a better structure for this XML. A field could be named "Id" and would cause an undeterministic behaviour.
        XmlUtil.addElement( sbXml, TAG_ID, record.getIdRecord(  ) );

        IEntry previousEntry = null;

        for ( RecordField field : record.getListRecordField(  ) )
        {
            getRecordFieldXml( sbXml, field, previousEntry );
            previousEntry = field.getEntry(  );
        }

        // We close the element if the last entry is a type Img
        if ( ( previousEntry != null ) &&
                previousEntry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg )
        {
            String strPreviousTag = fr.paris.lutece.util.string.StringUtil.replaceAccent( previousEntry.getTitle(  ) );
            XmlUtil.endElement( sbXml, strPreviousTag );
        }

        XmlUtil.endElement( sbXml, TAG_RECORD );
        XmlUtil.endElement( sbXml, TAG_RESPONSE );

        return sbXml.toString(  );
    }

    public String formatError( String strCode, String strMessage )
    {
        StringBuffer sbXml = new StringBuffer( HEADER_XML );
        XmlUtil.beginElement( sbXml, TAG_RESPONSE );
        XmlUtil.addElement( sbXml, TAG_STATUS, STATUS_ERROR );
        XmlUtil.beginElement( sbXml, TAG_ERROR );
        XmlUtil.addElement( sbXml, TAG_CODE, strCode );
        XmlUtil.addElement( sbXml, TAG_MESSAGE, strMessage );
        XmlUtil.endElement( sbXml, TAG_ERROR );
        XmlUtil.endElement( sbXml, TAG_RESPONSE );

        return sbXml.toString(  );
    }

    public String formatRecordsList( List<Record> listRecords )
    {
        StringBuffer sbXml = new StringBuffer( HEADER_XML );
        XmlUtil.beginElement( sbXml, TAG_RESPONSE );
        XmlUtil.addElement( sbXml, TAG_STATUS, STATUS_SUCCESS );
        XmlUtil.beginElement( sbXml, TAG_RECORDS );

        for ( Record record : listRecords )
        {
            XmlUtil.beginElement( sbXml, TAG_RECORD );
            // FIXME use a better structure for this XML. A field could be named "Id" and would cause an undeterministic behaviour.
            XmlUtil.addElement( sbXml, TAG_ID, record.getIdRecord(  ) );

            IEntry previousEntry = null;

            for ( RecordField field : record.getListRecordField(  ) )
            {
                getRecordFieldXml( sbXml, field, previousEntry );
                previousEntry = field.getEntry(  );
            }

            // We close the element if the last entry is a type Img
            if ( ( previousEntry != null ) &&
                    previousEntry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg )
            {
                String strPreviousTag = fr.paris.lutece.util.string.StringUtil.replaceAccent( previousEntry.getTitle(  ) );
                XmlUtil.endElement( sbXml, strPreviousTag );
            }

            XmlUtil.endElement( sbXml, TAG_RECORD );
        }

        XmlUtil.endElement( sbXml, TAG_RECORDS );
        XmlUtil.endElement( sbXml, TAG_RESPONSE );

        return sbXml.toString(  );
    }

    /**
     * Get the record field XML
     * @param sbXml the xml
     * @param recordField the record field
     */
    private void getRecordFieldXml( StringBuffer sbXml, RecordField recordField, IEntry previousEntry )
    {
        IEntry entry = recordField.getEntry(  );

        if ( entry != null )
        {
            String strTag = fr.paris.lutece.util.string.StringUtil.replaceAccent( entry.getTitle(  ) );
            closePreviousElement( sbXml, entry, previousEntry );

            if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg )
            {
                if ( ( previousEntry == null ) || ( entry.getIdEntry(  ) != previousEntry.getIdEntry(  ) ) )
                {
                    XmlUtil.beginElement( sbXml, strTag );
                }

                Field field = recordField.getField(  );

                if ( field != null )
                {
                    XmlUtil.beginElement( sbXml, field.getValue(  ) );

                    // For Entry type Image, we put the ID file instead of the ID of the record field
                    if ( recordField.getFile(  ) != null )
                    {
                        XmlUtil.addElement( sbXml, TAG_ID, recordField.getFile(  ).getIdFile(  ) );
                    }

                    if ( ( field.getWidth(  ) != 0 ) && ( field.getHeight(  ) != 0 ) )
                    {
                        XmlUtil.addElement( sbXml, TAG_WIDTH, field.getWidth(  ) );
                        XmlUtil.addElement( sbXml, TAG_HEIGHT, field.getHeight(  ) );
                    }

                    XmlUtil.endElement( sbXml, field.getValue(  ) );
                }
            }
            else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeFile )
            {
                String strValue = StringUtil.XMLEnc( recordField.getEntry(  ).getTitle(  ) );
                XmlUtil.beginElement( sbXml, strValue );

                // For Entry type File, we put the ID file
                if ( recordField.getFile(  ) != null )
                {
                    XmlUtil.addElement( sbXml, TAG_ID, recordField.getFile(  ).getIdFile(  ) );
                }

                XmlUtil.endElement( sbXml, strValue );
            }
            else
            {
                String strValue = StringUtil.XMLEnc( recordField.getValue(  ) );
                XmlUtil.addElement( sbXml, strTag, strValue );
            }
        }
    }

    /**
     * Close the previous element if
     * <ul>
     * <li>the entry is not the first entry</li>
     * <li>the current entry and the previous entry are not the same</li>
     * <li>the previous entry is a type image</li>
     * </ul>
     * @param sbXml the xml
     * @param entry the current entry
     * @param previousEntry the previous entry
     */
    private void closePreviousElement( StringBuffer sbXml, IEntry entry, IEntry previousEntry )
    {
        if ( ( previousEntry != null ) && ( entry.getIdEntry(  ) != previousEntry.getIdEntry(  ) ) &&
                previousEntry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg )
        {
            String strPreviousTag = fr.paris.lutece.util.string.StringUtil.replaceAccent( previousEntry.getTitle(  ) );
            XmlUtil.endElement( sbXml, strPreviousTag );
        }
    }
}
