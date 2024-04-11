/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.hinemosagent.bean.AgentJavaArch;
import com.clustercontrol.hinemosagent.bean.AgentJavaOs;
import com.clustercontrol.hinemosagent.bean.AgentLibMd5s;
import com.clustercontrol.util.Singletons;

/**
 * マネージャサーバに配置されているエージェントライブラリファイルを管理します。
 * <p>
 * インスタンスは{@link Singletons#get(Class)}から取得します。
 */
public class AgentLibraryManager {

	private static final Log log = LogFactory.getLog(AgentLibraryManager.class); 

	// エージェントライブラリファイルの配置場所
	private static final Path agentLibsRootPath = Paths
			.get(System.getProperty("hinemos.manager.home.dir", "/opt/hinemos/"), "lib", "agent");

	// OSキーワード: Linux
	private static final String OS_WORD_LINUX = "linux";
	// OSキーワード: Windows
	private static final String OS_WORD_WINDOWS = "windows";
	
	// アーキ キーワード: x86-64ビット
	private static final String ARCH_WORD_X86_64 = "x86_64";
	// アーキ キーワード: x86-32ビット
	private static final String ARCH_WORD_X86_32 = "i686";
	
	// OSキーワード -> OS列挙値
	private static Map<String, AgentJavaOs> word2Os;
	// アーキ キーワード -> アーキ列挙値
	private static Map<String, AgentJavaArch> word2Arch;

	static {
		initialize();
	}
	
	static void initialize() {
		word2Os = new ConcurrentHashMap<>();
		for (AgentJavaOs os : AgentJavaOs.values()) {
			switch (os) {
			case LINUX:
				word2Os.put(OS_WORD_LINUX, os); break;
			case WINDOWS:
				word2Os.put(OS_WORD_WINDOWS, os); break;
			case OTHERS:
				break;
			case UNKNOWN:
				break;
			}
		}
		
		word2Arch = new ConcurrentHashMap<>();
		for (AgentJavaArch arch : AgentJavaArch.values()) {
			switch (arch) {
			case X64:
				word2Arch.put(ARCH_WORD_X86_64, arch); break;
			case X86:
				word2Arch.put(ARCH_WORD_X86_32, arch); break;
			case OTHERS:
				break;
			case UNKNOWN:
				break;
			}
		}
	}
	
	// マネージャサーバ上にあるエージェントライブラリのファイルリスト (AGENT_LIB_DIR からの相対パス -> ファイル情報)
	// !!! refresh() 以外で書き換えるのは禁止 !!!
	private Map<Path, ManagedFile> managedFiles;
	private static class ManagedFile {
		final Path path;
		final String md5;
		final FileTime lastModifiedTime;
		final long size;
		final AgentJavaOs targetOs;		// null = 特定ターゲットなし
		final AgentJavaArch targetArch;	// null = 特定ターゲットなし
	
		ManagedFile(Path path, String md5, BasicFileAttributes attr) {
			this.path = path;
			this.md5 = md5;
			this.lastModifiedTime = attr.lastModifiedTime();
			this.size = attr.size();

			// 特定ターゲットがある("xxxx-OS{-Arch}.xxx"のフォーマットに沿っている)かどうか、ファイル名から判定する。
			AgentJavaOs os = null;
			AgentJavaArch arch = null;
			
			Path filename = path.getFileName();
			if (filename == null) {
				throw new IllegalArgumentException("No filename. path=" + path.toString());
			}
			String[] splitted = filename.toString().split("-");
			int idx = splitted.length - 1;
			if (idx > 0) {
				String word = splitted[idx].split("\\.")[0].toLowerCase();
				arch = word2Arch.get(word);
				if (arch != null) {
					word = splitted[idx - 1].toLowerCase();
				}
				os = word2Os.get(word);
			}
			this.targetOs = os;
			this.targetArch = arch;
		}

		// 指定された環境のエージェントが本ファイルの対象かどうか。
		boolean isTarget(AgentJavaOs agentOs, AgentJavaArch agentArch) {
			if (targetOs != null && targetOs != agentOs) return false;
			if (targetArch != null && targetArch != agentArch) return false;
			return true;
		}
		
		@Override
		public String toString() {
			return path + " [" + Arrays.toString(new Object[] { md5, lastModifiedTime, size, targetOs, targetArch })
					+ "]";
		}
	}

	// 最後にrefreshした時間
	private long lastRefreshedTime;

	// 外部依存動作をモックへ置換できるように分離
	private External external;
	static class External {

		Stream<Path> walk(Path basePath) throws IOException {
			return Files.walk(basePath, FileVisitOption.FOLLOW_LINKS);
		}
		
		BasicFileAttributes readAttributes(Path path) throws IOException {
			return Files.readAttributes(path, BasicFileAttributes.class);
		}
		
		InputStream createInputStream(Path path) throws IOException {
			return new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ));
		}
		
		boolean exists(Path path) {
			return Files.exists(path);
		}
		
		String calcMd5(InputStream is) throws IOException {
			return DigestUtils.md5Hex(is);
		}
		
		int getCoolDownTime() {
			return HinemosPropertyCommon.repository_agentupdate_cache_cooldown.getIntegerValue();
		}
	}

	/**
	 * コンストラクタ。
	 * <p>
	 * {@link Singletons#get(Class)} を使用してください。
	 */
	public AgentLibraryManager() {
		this(new External());
	}
	
	AgentLibraryManager(External external) {
		this.external = external;
		
		refresh();
	}

	/**
	 * 管理しているファイルの情報を最新にします。
	 * <p>
	 * 前回更新時から、ファイルサイズまたは更新日時に変更のあったファイルのみ、情報を更新します。
	 * <p>
	 * {@code Singletons.get(AgentLibraryManager.class).refresh()}のような呼び出しにおいて、
	 * コンストラクタとrefreshの2回連続での意識しない呼び出しを防ぐため、5秒間のクールダウンを設けています。
	 * クールダウン期間中に本メソッドを連続して呼び出したとしても、何も行いません。
	 */
	public void refresh() {
		// 同時に実行できるのは1スレッドのみ
		synchronized (this) {
			long startTime = System.currentTimeMillis();
		
			// クールダウン期間中は何もしない。
			// (システム時刻が大きく過去へ巻き戻された場合を考慮して、絶対値で判定する。)
			if (managedFiles != null && Math.abs(startTime - lastRefreshedTime) < external.getCoolDownTime()) {
				log.info("refresh: Cooling down now.");
				return;
			}

			// 別スレッドでファイルリストを参照している可能性を考慮して、新しい情報はローカル変数上で構築し、最後に置き換える。
			Map<Path, ManagedFile> newManagedFiles = new ConcurrentHashMap<>();
			Path basePath = agentLibsRootPath;
			AtomicLong calcCount = new AtomicLong(0); // MD5を計算したファイルの数
			try (Stream<Path> found = external.walk(basePath)) { // Files.find を使うこともできるが、テストでmatcherの扱いが非常に難しいので使用しない。
				found.forEach(path -> {
					// ファイルの属性を取得する。
					// 読めない場合は無理に処理を継続しようとせず、RuntimeExceptionで包んで外に出す。
					BasicFileAttributes attr;
					try {
						attr = external.readAttributes(path);
					} catch (IOException e) {
						throw new RuntimeException("Failed to read attributes. file=" + path, e);
					}
					Path relPath = basePath.relativize(path);
					// ディレクトリは除外
					if (attr.isDirectory()) return;
					// 前回と比べて、サイズと更新日時が同じ場合、MD5の計算は行わず、前回値を流用する。
					if (managedFiles != null) {
						ManagedFile oldItem = managedFiles.get(relPath);
						if (oldItem != null && attr.size() == oldItem.size
								&& attr.lastModifiedTime().equals(oldItem.lastModifiedTime)) {
							newManagedFiles.put(relPath, oldItem);
							return;
						}
					}
					// MD5算出
					String md5;
					try (InputStream is = external.createInputStream(path)) {
						md5 = external.calcMd5(is);
					} catch (IOException e) {
						throw new RuntimeException("Failed to calculate MD5. file=" + path, e);
					}
					// 新アイテム
					ManagedFile newItem = new ManagedFile(relPath, md5, attr);
					newManagedFiles.put(relPath, newItem);
					calcCount.incrementAndGet();
					log.debug("refresh: " + newItem);
				});
			} catch (Exception e) {
				log.warn("refresh: Error occured during filesystem traversing.", e);
				// 中途半端にファイルリストが構築されてしまうと、エージェントがそれをもとに誤った更新を行ってしまう可能性がある。
				// ファイルリストを空にすることで、少なくともエージェントが誤った更新を行うことはない。
				newManagedFiles.clear();
			}
			managedFiles = newManagedFiles;
			long now = System.currentTimeMillis();
			lastRefreshedTime = now;
			log.info(String.format("refresh: Elapsed=%dms, Size=%d, Calculated=%d.", (now - startTime),
					newManagedFiles.size(), calcCount.longValue()));
		}
	}

	/**
	 * 指定されたエージェントに適した、マネージャが保持しているエージェントのライブラリファイルの一覧を返します。
	 * <p>
	 * エージェントのバージョンに対して、以下のような動作の違いがあります。
	 * <ul>
	 * <li>Java情報が送信されてきている場合、 HinemosJava等のアーカイブを含む一覧を返します。
	 * <li>Java情報が送信されてきていない場合は、空の一覧を返します。
	 * </ul>
	 * 
	 * @param facilityIdList エージェントに紐付けられたファシリティIDのリスト。nullあるいは空の場合、ファシリティIDが不明。
	 * @return ライブラリファイルの一覧。
	 */
	public AgentLibMd5s getAgentLibMd5s(List<String> facilityIds) {
		AgentLibMd5s ret = new AgentLibMd5s();
		
		if (facilityIds == null || facilityIds.isEmpty()) {
			log.info("getAgentLibMd5s: Empty facilityIds.");
			return ret;
		}
		
		// バージョン判定
		AgentProfile agentProfile = Singletons.get(AgentProfiles.class).getProfile(facilityIds);
		if (agentProfile == null) {
			log.info("getAgentLibMd5s: No profile. facilityIds=" + String.join(",", facilityIds));
			return ret;
		}
		

		if (agentProfile.getJavaInfo() == null) {
			log.info("getAgentLibMd5s: No javainfo. facilityIds=" + String.join(",", facilityIds));
			// Java情報が送信されてきていない場合は、空の一覧を返す (本来はありえない状況のはず)
			return ret;
		}

		// ファイルリストから対象外OS/アーキのファイルを除去 & エージェント側パスに変換
		AgentJavaOs agentOs = agentProfile.getJavaOs();
		AgentJavaArch agentArch = agentProfile.getJavaArch();
		for (ManagedFile it : managedFiles.values()) {
			if (it.isTarget(agentOs, agentArch)) {
				ret.setMd5(it.path.toString(), it.md5);
			}
		}

		return ret;
	}

	/**
	 * 指定されたライブラリファイルを返します。
	 * 
	 * @param libPath 取得したいライブラリファイルのパス (配置場所からの相対パス)。
	 * @return Fileオブジェクト。当該ライブラリファイルが存在しない場合はnull。
	 */
	public File getFile(String libPath) {
		Path path = agentLibsRootPath.resolve(libPath);
		if (!external.exists(path)) {
			log.debug("getFile: Not found. path=" + libPath);
			return null;
		}
		return path.toFile();
	}

	/**
	 * 指定されたエージェントのライブラリファイルが、管理しているライブラリファイルと比較して最新であるかどうかを返します。
	 * 
	 * @param profile エージェントのプロファイル。
	 * @return 最新である場合はtrue、そうでなければfalse。
	 */
	public boolean isLatest(AgentProfile profile) {
		if (profile == null) {
			log.info("isLatest: Profile is null.");
			return false;
		}

		AgentJavaOs agentOs = profile.getJavaOs();
		AgentJavaArch agentArch = profile.getJavaArch();
		AgentLibMd5s agentLibs = profile.getLibMd5s();

		// Java環境の特定ができていないならば最新ではない
		if (agentOs == AgentJavaOs.UNKNOWN || agentArch == AgentJavaArch.UNKNOWN) {
			log.debug("isLatest: Uknown OS/arch.");
			return false;
		}

		// マネージャ管理下のライブラリファイルを基準に比較を行う。
		// ※ エージェント側に余分なファイルがあっても差分として検知できないが、
		//   現行仕様ではアップデート機能による"既存ライブラリファイルの削除"は許されていないため、問題ない。
		for (ManagedFile mgr : managedFiles.values()) {
			// 対象外ファイルなら除外
			if (!mgr.isTarget(agentOs, agentArch)) continue;
			// 比較
			String agentMd5 = agentLibs.getMd5(mgr.path.toString());
			if (agentMd5 == null) {
				// マネージャにあるのにエージェントが持っていないファイルがある。
				log.debug("isLatest: Missing file. path=" + mgr.path.toString());
				return false;
			}
			if (!agentMd5.equals(mgr.md5)) {
				// MD5が一致しない
				log.debug("isLatest: MD5 mismatch. path=" + mgr.path.toString());
				return false;
			}
		}
		return true;
	}

	/**
	 * 管理下のファイル総数を返します。
	 */
	public int getManagedFileCount() {
		return managedFiles.size();
	}
}
