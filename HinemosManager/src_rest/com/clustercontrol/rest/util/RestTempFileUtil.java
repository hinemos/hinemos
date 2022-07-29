/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;

/**
 * RESTのファイルダウンロードAPIにおいて一時ファイルの作成とダウンロード処理を実施するクラス
 * RESTのファイルダウンロードAPIでは必ず本クラスを利用すること
 * 
 */
public class RestTempFileUtil {

	private static Log m_log = LogFactory.getLog(RestTempFileUtil.class);

    /**
     * {@link RestTempFileUtil#getTempFileStream(File, BiConsumer)} の finisher へ渡されるパラメータです。
     */
    public static class TempFileStreamFinisherParams {
        /** 送信対象ファイル */
        public File file = null;
        /** 発生した例外 (送信が正常終了した場合は null) */
        public Exception exception = null;

		//findbugs対応 リフレクションで値が参照される前提のメンバーなので本来不要だが、 getterを追加
		public File getFile() {
			return this.file;
		}
		public Exception getException() {
			return this.exception;
		}

    }

    /**
     * createTempFile() で作成した一時ファイルの出力ストリームを取得する。
     * 処理の最後に一時ファイルの削除処理を実施する
     * 
     * @param File file ダウンロードする一時ファイル
     * @return StreamingOutput
     * @throws HinemosUnknown
     */
    public static StreamingOutput getTempFileStream(File file) throws HinemosUnknown {
        return getTempFileStream(file, null, true, null);
    }

    /**
     * createTempFile() で作成した一時ファイルの出力ストリームを取得する。
     * 
     * @param File file ダウンロードする一時ファイル
     * @param boolean isDelete ファイル削除有無
     * @return StreamingOutput
     * @throws HinemosUnknown
     */
    public static StreamingOutput getTempFileStream(File file, boolean isDelete) throws HinemosUnknown {
        return getTempFileStream(file, null, isDelete, null);
    }
    

    /**
     * createTempFile() で作成した一時ファイルの出力ストリームを取得する。
     * 処理の最後に一時ファイルの削除処理を実施する
     * 
     * @param File file ダウンロードする一時ファイル
     * @return StreamingOutput
     * @throws HinemosUnknown
     */
    public static StreamingOutput getTempFileStream(File file, File dir) throws HinemosUnknown {
        return getTempFileStream(file, dir, true, null);
    }

	/**
     * RESTのファイルダウンロードAPIでは必ず本メソッドを利用すること。
     * 
     * createTempFile() で作成した一時ファイルの出力ストリームを取得する。
     * 処理の最後に一時ファイルの削除処理を実施する
     * 
     * @param File file ダウンロードする一時ファイル
     * @param File dir 削除対象のディレクトリ(nullの場合、fileが削除対象となる)
     * @param boolean isDelete ファイル削除有無
     * @param finisher ファイル内容を送信完了した、あるいはエラーにより送信を中止した際に呼び出したいコールバックです。null なら何もしません。
     * @return StreamingOutput
     * @throws HinemosUnknown
     */
    public static StreamingOutput getTempFileStream(File file, File dir, boolean isDelete, Consumer<TempFileStreamFinisherParams> finisher) throws HinemosUnknown {
		StreamingOutput stream = null;
		try {
			stream = new StreamingOutput() {
				@Override
				public void write(OutputStream out) throws IOException {
                    TempFileStreamFinisherParams finParams = null; // コールバックへの連携で使用
                    if (finisher != null) {
                        finParams = new TempFileStreamFinisherParams();
                        finParams.file = file;
                    }

                    try (FileInputStream fis = new FileInputStream(file)) {
						byte[] buff = new byte[1024 * 1024];
						int len = 0;
						while ((len = fis.read(buff)) >= 0) {
							out.write(buff, 0, len);
						}
						out.flush();
					} catch (Exception e) {
                        if (finParams != null) {
                            finParams.exception = e;
                        }
                        m_log.warn("getTempFileStream: Error. " + e.getMessage());
						throw new IOException("Stream error: " + e.getMessage());
					} finally {
                        if (finisher != null) {
                            try {
                                finisher.accept(finParams);
                            } catch (Exception ignored) {
                                m_log.warn("getTempFileStream: Finisher Error. ", ignored);
                            }
                        }
                        if(isDelete) {
                        	if(dir != null) {
                        		// dir が渡された場合はディレクトリごと削除する
                        		recursiveDelete(dir);
                        	} else {
								// findbugs対応 戻り値をチェックして失敗時はログを出力する
								boolean ret = file.delete();
								if (!ret) {
									m_log.warn("getTempFileStream: delete Error. path=" + file.getPath());
								}
                        	}
                        }
					}
				}
			};
		} catch (Exception e) {
			throw new HinemosUnknown(e.getMessage());
		}
		return stream;
	}

	/**
	 * 一時ファイルを作成する。
	 * 
	 * @param RestTempFileType type
	 * @return Path 一時ファイルのパス
	 * @throws IOException
	 */
	public static Path createTempFile(RestTempFileType type) throws IOException {

		return Files.createTempFile(Paths.get(type.getDir()), type.getPrefix(), null);
	}

	/**
	 * 一時ディレクトリを作成する。
	 * 
	 * @param RestTempFileType type
	 * @return Path 一時ディレクトリのパス
	 * @throws IOException
	 */
	public static Path createTempDirectory(RestTempFileType type) throws IOException {

		return Files.createTempDirectory(Paths.get(type.getDir()), type.getPrefix());
	}
	
	/**
	 * 一時ファイルを削除する
	 */
	public static void deleteTempFile() {
		Set<String> dirSet = new HashSet<>();
		for(RestTempFileType type : RestTempFileType.values()) {
			dirSet.add(type.getDir());
		}
		
		for(String dirStr : dirSet) {
			File[] files = new File(dirStr).listFiles();
			if(files == null) {
				continue;
			}
			
			for (File f : files) {
				if(!f.isFile()) {
					continue;
				}
				m_log.info("[dir, file] = [" + dirStr +", " + f.getName() + "]");
				if(!f.delete()) {
					m_log.warn("failed to delete file = " + f.getName());
				}
			}
		}
	}
	
	private static void recursiveDelete(File dir) {
		if (!dir.exists()) {
			return;
		}
		if (dir.isDirectory() ) {
			//findbugs 対応 nullチェック追加
			File[] files = dir.listFiles();
			if (files != null) {
				for (File child : files) {
					recursiveDelete(child);
				}
			}
		}
		// findbugs対応 戻り値をチェックして失敗時はログを出力する
		boolean ret = dir.delete();
		if (!ret) {
			m_log.warn("recursiveDelete: delete Error. path=" + dir.getPath());
		}
	}
	
}
