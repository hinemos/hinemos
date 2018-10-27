/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.inquiry.factory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InquiryTargetCommandNotFound;
import com.clustercontrol.fault.InquiryTargetCreating;
import com.clustercontrol.fault.InquiryTargetNotDownloadable;
import com.clustercontrol.fault.InquiryTargetNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.inquiry.bean.InquiryTarget;
import com.clustercontrol.inquiry.bean.TargetStatus;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 
 * 遠隔管理機能を管理する Session Bean です。<BR>
 * 
 */
public class InquiryControllerBean {
	// Logger
	private static Logger logger = Logger.getLogger(InquiryControllerBean.class);
	
	// 成果物配置ディレクトリ
	private static final Path artifact_dir;
	
	// コマンドのパラメータ名
	private static final String PARAM_HinemosScript = "HinemosScript";
	private static final String PARAM_HinemosHome = "HinemosHome";
	private static final String PARAM_HinemosData = "HinemosData";
	private static final String PARAM_Output = "Output";
	
	static {
		// OS によって、実行方法を変える。
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			String hinemosData = System.getProperty("hinemos.manager.data.dir");
			artifact_dir = Paths.get(hinemosData, "inquiry\\artifacts\\").toAbsolutePath();
		} else {
			String hinemosHome = System.getProperty("hinemos.manager.home.dir");
			artifact_dir = Paths.get(hinemosHome, "var/inquiry/artifacts/").toAbsolutePath();
		}
	}
	
	private static ExecutorService worker = new MonitoredThreadPoolExecutor(
			0, Integer.MAX_VALUE, 0, TimeUnit.MILLISECONDS,
			new SynchronousQueue<Runnable>(), 
			new ThreadFactory() {
				private AtomicInteger countor = new AtomicInteger();
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, String.format("Thread-InquiryWorker-%d", countor.incrementAndGet()));
				}
		});
	
	// 作成開始時間のキャッシュ
	private static Map<String, Long> startTimeCache = new HashMap<>();
	
	// 作成中のファイル名をキャッシュ
	private static Map<String, String> creatingFiles = new HashMap<>();
	
	/*
	 * ダウンロード実施。
	 */
	public DataHandler download(String id) throws InvalidRole, HinemosUnknown, InquiryTargetNotDownloadable, InquiryTargetNotFound {
		synchronized(creatingFiles) {
			InquiryTarget target = getInquiryTarget(id);
			
			// 実行中か確認。
			if (!TargetStatus.downloadable.equals(target.getStatus())) {
				logger.warn(String.format("prepare() : Now creating. targetId=%s", id));
				throw new InquiryTargetNotDownloadable(MessageConstant.MESSAGE_INQUIRY_TARGET_NOT_DOWNLOADABLE.getMessage(id));
			}
			
			try {
				Path path = artifact_dir.resolve(target.getFileName());
				
				// OS によって、実行方法を変える。
				String url;
				if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
					url = "file://" + "/" + path.toString().replace('\\', '/');
				} else {
					url = "file://" + path.toString();
				}
				
				return new DataHandler(new URLDataSource(new URL(url)));
			} catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	/*
	 * 対象を作成。
	 */
	public void prepare(String targetId) throws InvalidRole, HinemosUnknown, InquiryTargetNotFound, InquiryTargetCreating, InquiryTargetCommandNotFound {
		synchronized(creatingFiles) {
			InquiryTarget target = getInquiryTarget(targetId);
			
			// 実行中か確認。
			if (TargetStatus.creating.equals(target.getStatus())) {
				logger.warn(String.format("prepare() : Now creating. targetId=%s", targetId));
				throw new InquiryTargetCreating(MessageConstant.MESSAGE_INQUIRY_TARGET_CREATING1.getMessage(target.getId()));
			}
			
			if (target.getFileName() != null && !target.getFileName().isEmpty()) {
				// 同名のファイルが処理中か？
				Optional<Map.Entry<String, String>> s = creatingFiles.entrySet().stream()
					.filter(e -> !target.getId().equals(e.getKey()) && target.getFileName().equals(e.getValue())).findFirst();
				
				// 別の項目で、同名のファイルに対する処理が実施されていたら処理中止。
				if (s.isPresent()) {
					logger.warn(String.format("prepare() : Now creating to fileName of other target. targetId=%s, filename=%s", s.get().getKey(), s.get().getValue()));
					throw new InquiryTargetCreating(MessageConstant.MESSAGE_INQUIRY_TARGET_CREATING2.getMessage(s.get().getKey(), s.get().getValue()));
				}
			}
			
			if (target.getFileName() == null || target.getFileName().isEmpty()) {
				creatingFiles.put(targetId, "");
			} else {
				creatingFiles.put(targetId, target.getFileName());
			}
			startTimeCache.put(targetId, System.currentTimeMillis());
			
			// ダウンロード用の情報を作成。
			worker.execute(()-> {
				try {
					// OS によって、実行方法を変える。
					Map<String, String> params = new HashMap<>();
					if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
						String home = System.getProperty("hinemos.manager.home.dir");
						params.put(PARAM_HinemosHome, Paths.get(home).toAbsolutePath().toString());
						params.put(PARAM_HinemosScript, Paths.get(home, "sbin\\inquiry").toAbsolutePath().toString());
						params.put(PARAM_HinemosData, Paths.get(System.getProperty("hinemos.manager.data.dir")).toAbsolutePath().toString());
						
						if (target.getFileName() != null && !target.getFileName().isEmpty()) {
							params.put(PARAM_Output, artifact_dir.resolve(target.getFileName()).toAbsolutePath().toString());
						}
					} else {
						String home = System.getProperty("hinemos.manager.home.dir");
						params.put(PARAM_HinemosHome, Paths.get(home).toAbsolutePath().toString());
						params.put(PARAM_HinemosScript, Paths.get(home, "sbin/inquiry").toAbsolutePath().toString());
						
						if (target.getFileName() != null && !target.getFileName().isEmpty()) {
							params.put(PARAM_Output, artifact_dir.resolve(target.getFileName()).toAbsolutePath().toString());
						}
					}
					
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("prepare() : targetId=%s, params=%s", targetId, params));
					}
					
					StringBinder strbinder = new StringBinder(params);
					String command = strbinder.bindParam(target.getCommand());
					
					if (target.getFileName() != null && !target.getFileName().isEmpty()) {
						Path path = artifact_dir.resolve(target.getFileName());
						
						// 実施前にファイルが存在する場合は、削除。
						if (Files.exists(path)) {
							Files.delete(path);
						}
					}
					
					// 出力先ディレクトリが存在しない場合は作成。
					if (!Files.exists(artifact_dir)) {
						Files.createDirectories(artifact_dir);
					}
					
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("prepare() : targetId=%s, command=%s", targetId, command));
					}
					
					Process p = Runtime.getRuntime().exec(command);
					
					// プロセスの正常終了まで待機させる
					p.waitFor();
					
					if (p.exitValue() == 0) {
						if (logger.isDebugEnabled()) {
							try (InputStreamReader br = new InputStreamReader(p.getInputStream())) {
								String message = getMessage(br);
								logger.debug(String.format("prepare() : targetId=%s, command=%s, message=%s", target.getId(), command, message));
							}
						}
						
						if ((target.getFileName() == null || target.getFileName().isEmpty()) || Files.exists(artifact_dir.resolve(target.getFileName()))) {
							// 成功
							if (logger.isDebugEnabled()) {
								try (InputStreamReader br = new InputStreamReader(p.getErrorStream())) {
									String error = getMessage(br);
									logger.debug(String.format("prepare() : targetId=%s, command=%s, error=%s", target.getId(), command, error));
								}
							}
							
							String messageOrg = String.format("A command has executed successfully. targetId=%s, command=%s", target.getId(), command.toString());
							logger.info(messageOrg);
							
							AplLogger.put(PriorityConstant.TYPE_INFO, HinemosModuleConstant.INQUIRY, MessageConstant.MESSAGE_INQUIRY_COMMAND_EXECUTED_SUCCESSFULLY, new String[]{target.getId(), command}, messageOrg);
						} else {
							// 0 が返ったが、ファイルが存在しない。
							String error;
							try (InputStreamReader br = new InputStreamReader(p.getErrorStream())) {
								error = getMessage(br);
							}
							
							String messageOrg = String.format("Not found an artifact. targetId=%s, fileName=%s, error=%s", target.getId(), target.getFileName(), error);
							logger.warn(messageOrg);
							
							AplLogger.put(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.INQUIRY, MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED, new String[]{}, messageOrg);
						}
					} else {
						// 失敗。
						if (logger.isDebugEnabled()) {
							try (InputStreamReader br = new InputStreamReader(p.getInputStream())) {
								String message = getMessage(br);
								logger.debug(String.format("prepare() : targetId=%s, command=%s, message=%s", target.getId(), command, message));
							}
						}
						
						String error;
						try (InputStreamReader br = new InputStreamReader(p.getErrorStream())) {
							error = getMessage(br);
						}
						
						String messageOrg = String.format("failed to execute a command. targetId=%s, command=%s, error=%s", target.getId(), command, error);
						logger.warn(messageOrg);
						
						AplLogger.put(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.INQUIRY, MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED, new String[]{}, messageOrg);
						
						if (target.getFileName() != null && !target.getFileName().isEmpty()) {
							Path path = artifact_dir.resolve(target.getFileName());
							
							// 失敗したので、もしファイルがあったら削除
							if (Files.exists(path)) {
								try {
									Files.delete(path);
								} catch (IOException e) {
									logger.warn(String.format(e.getMessage()));
								}
							}
						}
					}
				} catch (Exception e) {
					// 原因不明の例外が発生。
					if (target.getFileName() != null && !target.getFileName().isEmpty()) {
						Path path = artifact_dir.resolve(target.getFileName());
						if (Files.exists(path)) {
							try {
								Files.delete(path);
							} catch (IOException e1) {
								logger.warn(String.format(e1.getMessage()));
							}
						}
					}
					
					logger.warn(e.getMessage(), e);
					
					String messageOrg;
					try (StringWriter sw = new StringWriter()) {
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						pw.flush();
						messageOrg = String.format("UnexpectedError. targetId=%s\n%s", target.getId(), sw.toString());
					} catch(IOException e1) {
						messageOrg = String.format("fail to print stack. targetId=%s", target.getId());
					}
					
					AplLogger.put(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.INQUIRY, MessageConstant.MESSAGE_UNEXPECTED_EXCEPTION_OCCURRED, new String[]{}, messageOrg);
					
					synchronized(creatingFiles) {
						startTimeCache.remove(targetId);
					}
				} finally {
					synchronized(creatingFiles) {
						creatingFiles.remove(targetId);
					}
				}
			});
		}
	}
	
	/*
	 * コマンド実行結果のストリームからメッセージを取得。
	 */
	private static String getMessage(InputStreamReader reader) throws IOException {
		// 1024 文字以内で、メッセージを取得する。
		CharBuffer buffer = CharBuffer.allocate(1024);
		reader.read(buffer);
		buffer.flip();
		return buffer.toString();
	}
	
	/*
	 * コンテンツ情報の一覧取得。
	 */
	public List<InquiryTarget> getInquiryTargetList() throws InvalidRole, HinemosUnknown {
		Long number = HinemosPropertyCommon.inquiry_max_id_number.getNumericValue() + 1;
		
		List<InquiryTarget> targets = new ArrayList<>();
		for (int i = 0; i < number; ++i) {
			InquiryTarget target = createInquiryTarget(Integer.toString(i));
			if (target != null) {
				targets.add(target);
			}
		}
		return targets;
	}
	
	/*
	 * 指定した名前のコンテンツ情報取得。
	 */
	public InquiryTarget getInquiryTarget(String targetId) throws InvalidRole, HinemosUnknown, InquiryTargetNotFound {
		InquiryTarget target = createInquiryTarget(targetId);
		if (target == null) {
			logger.warn(String.format("getInquiryTarget() : Not found a target. targetId=%s", targetId));
			throw new InquiryTargetNotFound(MessageConstant.MESSAGE_INQUIRY_TARGET_NOT_FOUND.getMessage(targetId));
		}
		return target;
	}
	
	/*
	 * 対象情報を作成。
	 */
	private InquiryTarget createInquiryTarget(String targetId) throws InvalidRole, HinemosUnknown {
		String displayname = HinemosPropertyCommon.inquiry_displayname_$.getStringValue(targetId, null);
		String command = HinemosPropertyCommon.inquiry_command_$.getStringValue(targetId, null);
		
		if (displayname == null || command == null || command.isEmpty()) {
			return null;
		}
		
		InquiryTarget target = new InquiryTarget();
		target.setId(targetId);
		target.setDisplayName(displayname);
		target.setCommand(command);
		target.setFileName(HinemosPropertyCommon.inquiry_file_$.getStringValue(targetId, null));
		
		target.setStatus(TargetStatus.empty);
		
		String creatingFile;
		synchronized(creatingFiles) {
			creatingFile = creatingFiles.get(targetId);
			// 作成開始時間を設定。
			target.setStarTime(startTimeCache.get(targetId));
		}
		
		// 作成中か確認。
		if (creatingFile == null) {
			if (target.getFileName() != null && !target.getFileName().isEmpty()) {
				// コンテンツが作成済みか？
				Path path = artifact_dir.resolve(target.getFileName());
				if (Files.exists(path) && !Files.isDirectory(path)) {
					// 作成済みならダウンロード可能。
					target.setStatus(TargetStatus.downloadable);
					try {
						// ファイルのタイムスタンプを作成完了時刻にする。
						FileTime time = Files.getLastModifiedTime(path);
						target.setEndTime(time.toMillis());
					} catch (IOException e) {
						logger.warn(String.format("createInquiryTarget() : fail to get last modified time. %s", e.getMessage()));
					}
				}
			}
		} else {
			target.setStatus(TargetStatus.creating);
		}
		return target;
	}
}