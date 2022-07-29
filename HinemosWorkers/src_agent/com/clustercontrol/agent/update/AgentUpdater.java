/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.DownLoadAgentLibRequest;
import org.openapitools.client.model.GetAgentLibMapResponse;
import org.openapitools.client.model.SetAgentProfileRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentRestClientWrapper;
import com.clustercontrol.fault.AgentLibFileNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.HinemosTime;

/**
 * リモートアップデートを実行するためのクラスです。
 * 
 * @since 6.2.0
 */
public class AgentUpdater {
	// ライブラリファイル配置ディレクトリ (AGENT_HOMEからの相対)
	private static final String LIBRARY_DIR = "lib";
	// バックアップファイル配置ディレクトリ (従来の仕様に倣い、ライブラリファイル配置ディレクトリの子とする)
	private static final String BACKUP_DIR = "lib/backup";
	// ダウンロードファイル配置ディレクトリ
	private static final String DOWNLOAD_DIR = "download";

	// Windowsのアーカイブファイル名パターン
	private static final String ARCHIVE_FILENAME_REGEX_WINDOWS = ".*-windows(-.*)?\\.zip";
	// Linuxのアーカイブファイルパターン
	private static final String ARCHIVE_FILENAME_REGEX_LINUX = ".*-linux(-.*)?\\.(tar\\.gz|zip)";
	// OS別ディレクトリのパターン
	private static final String PLATFORM_DIRNAME_REGEX = "LINUX|WINDOWS";

	// 更新ログのファイル名
	private static final String UPDATE_LOG_FILENAME = "agent_update.log";
	
	private static final Log log = LogFactory.getLog(AgentUpdater.class);
	
	// ライブラリファイル配置ディレクトリ
	private final Path libPath;
	// バックアップ先ディレクトリ
	private final Path backupPath;
	// ダウンロード先ディレクトリ
	private final Path downloadPath;
	// アップデート用のライブラリファイルのダウンロードが完了して、再起動待ちの間はtrueになる(一回の起動時に複数回ダウンロードしないようにする）
	private boolean waitingRestart;
	// ファイルパスとMD5の組
	private Map<String, String> agentMap;
	
	// 外部依存動作をモックへ置換できるように分離
	private External external;
	static class External {
		String getAgentHome() {
			return Agent.getAgentHome();
		}

		void walk(Path basePath, FileVisitor<Path> visitor) throws IOException {
			Files.walkFileTree(basePath, visitor);
		}

		InputStream newInputStream(Path path) throws IOException {
			return new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ));
		}

		String calcMd5(InputStream is) throws IOException {
			return DigestUtils.md5Hex(is);
		}
		
		void sendAgentProfile(Map<String, String> agentMap)
				throws InvalidRole, InvalidUserPass, InvalidSetting, RestConnectFailed, HinemosUnknown {
			SetAgentProfileRequest req = new SetAgentProfileRequest();
			req.setAgentInfo(Agent.getAgentInfoRequest());
			req.setJavaInfo(Agent.getJavaInfo());
			req.setMd5Map(agentMap);
			AgentRestClientWrapper.setAgentProfile(req);
		}
		
		Map<String, String> getAgentLibMap()
				throws InvalidRole, InvalidUserPass, InvalidSetting, RestConnectFailed, HinemosUnknown {
			GetAgentLibMapResponse res = AgentRestClientWrapper.getAgentLibMap(Agent.getAgentInfoRequest());
			return res.getMd5Map();
		}
	
		InputStream downloadAgentLib(String path)
				throws InvalidRole, InvalidUserPass, InvalidSetting, AgentLibFileNotFound,
				RestConnectFailed, HinemosUnknown, FileNotFoundException {
			DownLoadAgentLibRequest req = new DownLoadAgentLibRequest();
			req.setAgentInfo(Agent.getAgentInfoRequest());
			req.setLibPath(path);
			File res = AgentRestClientWrapper.downloadAgentLib(req);
			return new FileInputStream(res);
		}

		void pipeStream(InputStream inp, OutputStream out) throws IOException {
			IOUtils.copy(inp, out);
		}

		void createDirectories(Path dir) throws IOException {
			Files.createDirectories(dir);
		}

		OutputStream newOutputStream(Path path) throws IOException {
			return new BufferedOutputStream(Files.newOutputStream(path));
		}
		
		void delete(Path path) throws IOException {
			Files.delete(path);
		}
		
		void copy(Path src, Path dst) throws IOException {
			Files.copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES);
		}
		
		String getOsName() {
			return System.getProperty("os.name");
		}
		
		void extractTar(Path tarFile, Path destDir) throws ExtractFailureException {
			new TarExtractor().execute(tarFile, destDir);
		}

		void extractZip(Path zipFile, Path destDir) throws ExtractFailureException {
			new ZipExtractor().execute(zipFile, destDir);
		}
		
		void writeFile(Path path, byte[] data) throws IOException {
			Files.write(path, data);
		}
	}
	
	/**
	 * コンストラクタです。
	 * 通常、唯一のインスタンスのみを生成します。
	 */
	public AgentUpdater() {
		this(new External());
	}

	AgentUpdater(External external) {
		this.external = external;

		libPath = Paths.get(external.getAgentHome(), LIBRARY_DIR);
		backupPath = Paths.get(external.getAgentHome(), BACKUP_DIR);
		downloadPath = Paths.get(external.getAgentHome(), DOWNLOAD_DIR);
		waitingRestart = false;
		agentMap = new HashMap<>();

		try {
			external.walk(libPath, new LibMapVisitor());
		} catch (Throwable e) {
			log.warn("ctor: Failed to enumerate library files.", e);
		}
	}

	/**
	 * Hinemosマネージャへプロファイル情報を送信します。
	 * 
	 * @throws Exception 何らかの理由でプロファイル情報の送信に失敗した。
	 */
	public void sendProfile() throws Exception {
		if (agentMap == null) {
			log.warn("sendProfile: Cannot send a profile due to initialization error.");
			return;
		}
		log.info("sendProfile: Sending.");
		external.sendAgentProfile(agentMap);
	}

	/**
	 * ダウンロード完了時に作成したバックアップファイルを削除しようと試みます。
	 * <p>
	 * 削除に失敗した場合も、通常通りに制御を戻します。
	 */
	public void sweepBackup() {
		try {
			external.walk(backupPath, new SweepingVisitor());
		} catch (Exception e) {
			// 削除失敗はログ出力だけして握りつぶす。
			log.warn("sweepBackup: Failed to sweep directory=" + backupPath, e);
		}
	}
	
	/**
	 * ライブラリファイルのMD5一覧を返します。
	 * 
	 * @return キーがファイルパス(libディレクトリをルートとする相対、セパレータはOS依存)、値がMD5である、Map。
	 *		   返却したMapへの変更は、本インスタンスの内部情報に反映されます。
	 */
	public Map<String, String> getLibMap() {
		return agentMap;
	}
	
	/**
	 * Hinemosマネージャから更新されたライブラリファイル(MD5の違うもの)をダウンロードします。
	 * 
	 * @return 更新ファイルをダウンロードして再起動待ちとなった場合はtrue、そうでなければfalse。 
	 * @throws Exception 何らかの理由により、ダウンロードを完遂できなかった。
	 */
	public boolean download() throws Exception {
		if (waitingRestart) {
			log.info("download: Already done.");
			return false;
		}

		log.info("download: Start.");
		
		// マネージャからリストを取得する。
		log.info("download: Getting file list from HinemosManager.");
		Map<String, String> managerMap = external.getAgentLibMap();

		// ダウンロードディレクトリの掃除
		external.walk(downloadPath, new SweepingVisitor());

		// ファイルダウンロード実施
		List<Path> downloadedFiles = new ArrayList<>();
		List<String> backupCandidates = new ArrayList<>();
		UpdateLog updateLog = new UpdateLog(); 
		for (Entry<String, String> entry : managerMap.entrySet()) {
			String mgrPath = entry.getKey();
			String mgrMd5 = entry.getValue();
			// ファイルリストのパスはマネージャ側OSの表現となっているので、エージェント側でパスとして使用するためには変換が必要。
			String agtPath = mgrPath.replace("/", File.separator).replace("\\", File.separator);
			String agtMd5 = agentMap.get(agtPath);
			log.info(String.format("download: [%s:%s] %s",
					(agtMd5 == null) ? "--------------------------------" : agtMd5, mgrMd5, agtPath));
			
			// MD5が同じならダウンロードしない
			if (mgrMd5.equals(agtMd5)) {
				log.info("download: - Same.");
				continue;
			}

			// ダウンロードして、セーブ領域へファイル作成
			Path savefile = downloadPath.resolve(agtPath);
			Path saveDir = savefile.getParent();
			if (saveDir == null) { // ありえないが、FindBugsが検知してしまうので…
				throw new IllegalStateException(savefile.toString());
			}
			try (InputStream inpStream = external.downloadAgentLib(mgrPath)) {
				external.createDirectories(saveDir);
				try (OutputStream outStream = external.newOutputStream(savefile)) {
					external.pipeStream(inpStream, outStream);
				}
			}
			downloadedFiles.add(savefile);
			log.info("download: - Downloaded.");
			
			// 既存ファイルがあるならバックアップ候補へ追加
			if (agtMd5 != null) {
				backupCandidates.add(agtPath);
			}

			// 更新ログ
			updateLog.add(agtMd5 == null ? "NEW" : "UPDATE", agtPath);
		}

		// 1つでもダウンロードできたら...
		if (downloadedFiles.size() > 0) {
			// ポストプロセス実行
			postprocess(downloadedFiles, updateLog);
		
			// 既存ファイルのバックアップを行う。
			log.info("download: Backup start.");
			Path currentBackupPath = backupPath.resolve(formatTimestamp());
			external.createDirectories(currentBackupPath);
			updateLog.save(currentBackupPath);

			if (backupCandidates.size() > 0) {
				for (String pathStr : backupCandidates) {
					Path src = libPath.resolve(pathStr);
					Path dst = currentBackupPath.resolve(pathStr);
					log.info("download: Copying " + src.toString() + " -> " + dst.toString());
					Path dstDir = dst.getParent();
					if (dstDir == null) {
						// ありえないが、FindBugsが検知してしまうので…
						throw new IllegalStateException(dst.toString());
					}
					external.createDirectories(dstDir);
					external.copy(src, dst);
				}
			}

			// ここへ到達した(＝例外で抜けること無く最後まで処理が行われた)場合、再起動待ちとなる
			waitingRestart = true;
			log.info("download: Finished, waiting for restart.");
		} else {
			log.info("download: Nothing to update.");
		}

		return waitingRestart;
	}
	
	// 現在時刻をフォーマット
	private static String formatTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		return sdf.format(HinemosTime.getDateInstance());
	}
	
	// ダウンロードしたファイルへポストプロセスを実施する。
	private void postprocess(List<Path> paths, UpdateLog updateLog) throws Exception {
		// OSを判定 (TODO: OS判定はいろいろなところに似た処理があるのでまとめるべき。)
		boolean windows = false;
		String osName = external.getOsName();
		if (osName != null) {
			windows = osName.toLowerCase().contains("windows");
		}
		log.info("postprocess: Detected OS=" + (windows ? "Windows" : "Not Windows(Linux)"));

		// 辞書順にソートして実行する。
		// (PathのcompareToは実装依存なのでtoStringしている。)
		// 例えば、以下の2つのファイルはこの記載順に展開される。
		//	1. test-LINUX.x86_64.tar.gz
		//	2. test_patch20181016-LINUX.x86_64.tar.gz
		Collections.sort(paths, new Comparator<Path>() {
			@Override
			public int compare(Path o1, Path o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		for (Path path : paths) {
			Path filename = path.getFileName();
			if (filename == null) {
				throw new IllegalArgumentException("No filename. path=" + path);
			}
			String filenameStr = filename.toString().toLowerCase();
			if ((windows && filenameStr.matches(ARCHIVE_FILENAME_REGEX_WINDOWS))
					|| (!windows && filenameStr.matches(ARCHIVE_FILENAME_REGEX_LINUX))) {
				if (filenameStr.endsWith(".tar.gz")) {
					external.extractTar(path, path.getParent());
					updateLog.add("EXTRACT", downloadPath.relativize(path).toString());
				} else if (filenameStr.endsWith(".zip")) {
					external.extractZip(path, path.getParent());
					updateLog.add("EXTRACT", downloadPath.relativize(path).toString());
				}
			}
		}
	}

	// ライブラリファイルのMapを構築するVisitor。
	// (個別にテストするのでデフォルトスコープへ公開)
	class LibMapVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
			// バックアップディレクトリ配下はスキップ
			if (path.equals(backupPath)) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			// OS別ディレクトリ配下はスキップ
			Path filename = path.getFileName();
			if (filename == null) {
				// ありえないが、FindBugsが検知してしまうので…
				throw new IllegalArgumentException(path.toString());
			}
			if (filename.toString().matches(PLATFORM_DIRNAME_REGEX)) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
			// MD5算出
			try (InputStream is = external.newInputStream(path)) {
				String md5 = external.calcMd5(is);
				// 相対パスにして投入
				String relPathStr = libPath.relativize(path).toString();
				agentMap.put(relPathStr, md5);
				log.debug(String.format("LibMapVisitor: %s (%s)", relPathStr, md5));
			} catch (IOException e) {
				log.warn("LibMapVisitor: Failed to calculate MD5. file=" + path.toString(), e);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
			String relPath = libPath.relativize(path).toString();
			log.warn(String.format("LibMapVisitor: %s (%s)", relPath, exc.getMessage()));
			return FileVisitResult.CONTINUE;
		}
	}
	
	// ディレクトリを一掃するVisitor。
	// (個別にテストするのでデフォルトスコープへ公開)
	class SweepingVisitor extends SimpleFileVisitor<Path> {
		private Path root = null;
		private Path agentHome = Paths.get(external.getAgentHome());
		
		@Override
		public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
			if (root == null) {
				log.info("SweepingVisitor: Sweeping " + path);
				root = path;
				// 階層数が2以上でないとおかしい
				if (root.getNameCount() < 2) {
					throw new IllegalArgumentException("Target path=" + root + " is too shallow.");
				}
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
			// 最上位は削除しない
			if (path.equals(root)) return FileVisitResult.CONTINUE;

			return delete(path);
		}
		
		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
			if (root == null) {
				throw new IllegalStateException("Not start with directory. path=" + path);
			}

			return delete(path);
		}

		private FileVisitResult delete(Path path) throws IOException {
			log.info("SweepingVisitor: Deleting " + path.toString());
			// 万が一でも変なところを削除してしまわないように、AGENT_HOME配下かどうかをチェックする。
			if (!path.startsWith(agentHome)) {
				throw new IllegalArgumentException("Path=" + path + " to delete must be below " + agentHome);
			}
			external.delete(path);
			return FileVisitResult.CONTINUE;
		}
	}
	
	// 更新ログファイル
	private class UpdateLog {
		private StringBuilder content = new StringBuilder();
		
		void add(String operation, String path) {
			content.append(String.format("%-8s : ", operation)).append(path).append(System.lineSeparator());
		}
		
		void save(Path dir) throws IOException {
			external.writeFile(dir.resolve(UPDATE_LOG_FILENAME), content.toString().getBytes(StandardCharsets.UTF_8));
			log.info("UpdateLog:\n" + content.toString());
		}
	}
}
