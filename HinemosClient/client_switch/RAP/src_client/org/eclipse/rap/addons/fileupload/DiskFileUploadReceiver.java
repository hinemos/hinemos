/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.addons.fileupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * A file upload receiver that stores received files on disk.
 */
public class DiskFileUploadReceiver extends FileUploadReceiver {

  private static final String DEFAULT_CONTENT_TYPE_FILE_NAME = "content-type.tmp";
  private static final String DEFAULT_TARGET_FILE_NAME = "upload.tmp";
  private static final String TEMP_DIRECTORY_PREFIX = "fileupload_";

  private List<File> targetFiles;
  private File contentTypeFile;

  public DiskFileUploadReceiver() {
    targetFiles = new ArrayList<File>();
  }

  @Override
  public void receive( InputStream dataStream, FileDetails details ) throws IOException {
    File targetFile = createTargetFile( details );
    FileOutputStream outputStream = new FileOutputStream( targetFile );
    try {
      copy( dataStream, outputStream );
    } finally {
      dataStream.close();
      outputStream.close();
    }
    targetFiles.add( targetFile );
    contentTypeFile = createContentTypeFile( targetFile, details );
    if( contentTypeFile != null ) {
      PrintWriter pw = new PrintWriter( contentTypeFile );
      pw.print( details.getContentType() );
      pw.close();
    }
  }

  /**
   * Obtains the content type provided by the client when the given file was uploaded. This method
   * does not look at the uploaded file contents to determine the content type.
   *
   * @param uploadedFile - the file that was uploaded and handled by an instance of
   *        DiskFileUploadReceiver.
   * @return the content type of the uploaded file or <code>null</code> if the content type could
   *         not be found.
   */
  public static String getContentType( File uploadedFile ) {
    String contentType = null;
    BufferedReader br = null;
    if( uploadedFile.exists() ) {
      File cTypeFile = new File( uploadedFile.getParentFile(), DEFAULT_CONTENT_TYPE_FILE_NAME );
      if( cTypeFile.exists() ) {
        try {
          br = new BufferedReader( new InputStreamReader( new FileInputStream( cTypeFile ) ) );
          contentType = br.readLine();
        } catch( IOException e ) {
          e.printStackTrace();
        } finally {
          try {
            if( br != null ) {
              br.close();
            }
          } catch( IOException ce ) {
            ce.printStackTrace();
          }
        }
      }
    }
    return contentType;
  }

  /**
   * Returns an array with files that the received data has been saved to.
   *
   * @return the array with target files or empty array if no files have been stored yet
   */
  public File[] getTargetFiles() {
    return targetFiles.toArray( new File[ 0 ] );
  }

  /**
   * Creates a file to save the received data to. Subclasses may override.
   *
   * @param details the details of the uploaded file like file name, content-type and size
   * @return the file to store the data in
   */
  protected File createTargetFile( FileDetails details ) throws IOException {
    String fileName = DEFAULT_TARGET_FILE_NAME;
    if( details != null && details.getFileName() != null ) {
      fileName = details.getFileName();
    }
    File result = new File( createTempDirectory(), fileName );
    result.createNewFile();
    return result;
  }

  /**
   * Creates a file to save the content-type. Subclasses may override.
   *
   * @param uploadedFile the file that contains uploaded data
   * @param details the details of the uploaded file like file name, content-type and size
   * @return the file to store the content-type data in
   */
  protected File createContentTypeFile( File uploadedFile, FileDetails details )
      throws IOException {
    String fileName = DEFAULT_CONTENT_TYPE_FILE_NAME;
    File result = null;
    if( details != null && details.getContentType() != null ) {
      result = new File( uploadedFile.getParentFile(), fileName );
      result.createNewFile();
    }
    return result;
  }


  private static File createTempDirectory() throws IOException {
    File result = File.createTempFile( TEMP_DIRECTORY_PREFIX, "" );
    result.delete();
    if( result.mkdir() ) {
      result.deleteOnExit();
    } else {
      throw new IOException( "Unable to create temp directory: " + result.getAbsolutePath() );
    }
    return result;
  }

  private static void copy( InputStream inputStream, OutputStream outputStream )
    throws IOException
  {
    byte[] buffer = new byte[ 8192 ];
    boolean finished = false;
    while( !finished ) {
      int bytesRead = inputStream.read( buffer );
      if( bytesRead != -1 ) {
        outputStream.write( buffer, 0, bytesRead );
      } else {
        finished = true;
      }
    }
  }

}
