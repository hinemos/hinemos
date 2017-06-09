/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform.infra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.activation.DataHandler;
import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.platform.HinemosPropertyDefault;

/**
 * InfraJdbcExecutorクラスの環境差分（windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class InfraJdbcExecutorSupport {
	private static final Log log = LogFactory.getLog(InfraJdbcExecutorSupport.class);
	private static final String MAX_FILE_KEY = "infra.max.file.size";
	
	@SuppressWarnings("unchecked")
	public static void execInsertFileContent(String fileId, DataHandler handler) throws HinemosUnknown, InfraFileTooLarge {
		Connection conn = null;
		
		boolean update = false;
		JpaTransactionManager tm = null;
		FileOutputStream fos = null;
		FileInputStream fis = null;
		PreparedStatement pst = null;
		File tempFile = null;
		try {
			tm = new JpaTransactionManager();
			
			EntityManager em = tm.getEntityManager();
			
			String existSql = "SELECT file_id FROM binarydata.cc_infra_file_content WHERE file_id = '" + fileId +"'";
			List<Object> list = (List<Object>)em.createNativeQuery(existSql).getResultList();
			update = (list.size() > 0);
			
			conn = tm.getEntityManager().unwrap(java.sql.Connection.class);
			conn.setAutoCommit(false);
			
			String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr("infra.export.dir", HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.INFRA_EXPORT_DIR));
			tempFile = new File(exportDirectory + fileId);
			fos = new FileOutputStream(tempFile);
			handler.writeTo(fos);
			
			long fileLength = tempFile.length();
			int maxSize = HinemosPropertyUtil.getHinemosPropertyNum(MAX_FILE_KEY , Long.valueOf(1024 * 1024 * 64)).intValue(); // 64MB
			if(fileLength > maxSize) {
				throw new InfraFileTooLarge(String.format("File size is larger than the limit size(%d)", maxSize));
			}
			
			fis = new FileInputStream(tempFile);
			if (update) {
				pst = conn.prepareStatement("UPDATE binarydata.cc_infra_file_content SET file_content = ? WHERE file_id = ?");
				pst.setBinaryStream(1, fis, (int)fileLength);
				pst.setString(2, fileId);
				
			} else {
				pst = conn.prepareStatement("INSERT INTO binarydata.cc_infra_file_content VALUES (?,?)");
				pst.setString(1, fileId);
				pst.setBinaryStream(2, fis, (int)fileLength);
			}
			pst.execute();
			
			if (! tm.isNestedEm()) {
				conn.commit();
			}
		} catch (InfraFileTooLarge e) {
			log.warn(e.getMessage());
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.warn(e1);
				}
			throw e;
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
				try {
					if (conn != null)
						conn.rollback();
				} catch (SQLException e1) {
					log.warn(e1);
				}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
			if (tm != null) {
				tm.close();
			}
			if (tempFile == null){
				log.debug("Fail to delete. tempFile is null");
			} else if (!tempFile.delete()) {
				log.debug("Fail to delete " + tempFile.getAbsolutePath());
			}
		}
	}
	
	public static String execSelectFileContent(String fileId, String fileName) throws HinemosUnknown {
		Connection conn = null;
		
		JpaTransactionManager tm = null;
		Statement st = null;
		ResultSet rs = null;
		OutputStream fos = null;
		try {
			tm = new JpaTransactionManager();
			tm.begin();
			conn = tm.getEntityManager().unwrap(java.sql.Connection.class);
			conn.setAutoCommit(false);

			String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr("infra.export.dir",
					HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.INFRA_EXPORT_DIR));
			String filepath = exportDirectory + "/" + fileName;
			
			st = conn.createStatement();
			
			int total = 0;
			rs = st.executeQuery("select file_content from binarydata.cc_infra_file_content where file_id = '" + fileId +"'");
			if (rs.next()){
				InputStream is = rs.getBinaryStream(1);
				int read;
				byte[] buf = new byte[1024*1024];
				fos = Files.newOutputStream(Paths.get(filepath));
				
				while ((read = is.read(buf)) != -1) {
					fos.write(buf, 0 , read);
				}
				total += read;
			}
			
			rs.close();
			
			if (total == 0) {
				String message = "error in the binary format file parsing (read file length)";
				log.warn(message);
				throw new HinemosUnknown(message);
			}
			
			if (! tm.isNestedEm()) {
				conn.commit();
			}
			tm.commit();
			
			return filepath;
		} catch (SQLException | IOException | RuntimeException e) {
			log.warn(e.getMessage(), e);
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.warn(e1);
				}
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
			if (tm != null) {
				tm.close();
			}
		}
	}
}
