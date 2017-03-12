/**********************************************************************
 * Copyright (C) 2006 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package org.eclipse.swt.widgets;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Shell;

/**
 * Single-sourcing implementation for FileDialog Widget (RAP)
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class FileDialog{

	private File tmpFile;

	private String[] filterExtensions = new String[0];
	private String filterPath = null;
	private String fileName = null;

	public FileDialog(Shell parent, int style) {
	}


	/**
	 * Show Selection dialog and upload the files selected
	 */
	public String open() {
		try {
			String prefix = null != this.fileName ? this.fileName : "file";
			String suffix = this.getExt();
			if( prefix.endsWith(suffix) ){
				prefix = prefix.replaceFirst( Pattern.quote(suffix)+"$", "");
			}

			File dir = null != this.filterPath ? new File(this.filterPath) : null;

			this.tmpFile = File.createTempFile(prefix, suffix, dir);

			this.fileName = this.tmpFile.getName();
			this.filterPath = this.tmpFile.getParent();

			return tmpFile.getAbsolutePath();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Set the file extensions which the dialog will use to filter the files it
	 * shows to the argument, which may be null.
	 * <p>
	 * The strings are platform specific. For example, on some platforms, an
	 * extension filter string is typically of the form "*.extension", where
	 * "*.*" matches all files. For filters with multiple extensions, use
	 * semicolon as a separator, e.g. "*.jpg;*.png".
	 * </p>
	 */
	public void setFilterExtensions(String[] extensions) {
		filterExtensions = extensions;
	}

	/**
	 * Set the download name.
	 * 
	 * @param string the download name
	 */
	public void setFileName (String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Returns the filename.
	 * 
	 * @return the filename
	 */
	public String getFileName () {
		return this.fileName;
	}

	/**
	 * Returns the directory path
	 * 
	 * @return the directory path
	 */
	public String getFilterPath() {
		return this.filterPath;
	}

	/**
	 * Returns the filename.
	 * 
	 * @return the filename
	 */
	public String getExt () {
		if (0 < this.filterExtensions.length) {
			return this.filterExtensions[0].replaceFirst("^\\*", "");
		}else{
			return "";
		}
	}
}
