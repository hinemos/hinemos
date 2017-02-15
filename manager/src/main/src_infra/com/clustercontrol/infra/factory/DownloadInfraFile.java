/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.factory;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.util.InfraJdbcExecutor;

/**
 * ファイルをダウンロード
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class DownloadInfraFile {
	private static Logger m_log = Logger.getLogger( DownloadInfraFile.class );
	
	public DataHandler download(String fileId, String fileName) throws InfraFileNotFound, HinemosUnknown, IOException {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		InfraFileInfo entity = em.find(InfraFileInfo.class, fileId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			InfraFileNotFound e = new InfraFileNotFound("InfraFileEntity.findByPrimaryKey" + ", fileId = " + fileId);
			m_log.info("download() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		String filename = InfraJdbcExecutor.selectFileContent(fileId, fileName);
		FileDataSource fileData = new FileDataSource(filename);
		return new DataHandler(fileData);
	}
}
