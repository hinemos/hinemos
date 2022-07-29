package com.clustercontrol.agent.update;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.util.RpaWindowsUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

public class RpaExecuterUpdater {
	
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RpaExecuterUpdater.class);
	
	static final String RPA_EXECUTER_LIB_SET_DIR_PATH_SUFFIX = "rpalib";
	static final String RPA_EXECUTER_BAK_DIR_PATH_SUFFIX = "rpalib/backup";
	static final String RPA_EXECUTER_LIB_TMP_DIR_PATH_SUFFIX = "rpalib/tmp";
	static final String RPA_EXECUTER_LIB_DWN_DIR_PATH_SUFFIX = "download/rpalib/lib";
	static final String RPA_EXECUTER_BIN_SET_DIR_PATH_SUFFIX = "bin";
	static final String RPA_EXECUTER_BIN_DWN_DIR_PATH_SUFFIX = "download/rpalib/bin";
	static final String RPA_EXECUTER_BASE_DWN_DIR_PATH_SUFFIX = "download/rpalib";
	static final String RPA_EXECUTER_SCRIPT_NAME_SUFFIX = "RpaScenarioExecutor.bat";

	/** エグゼキューター向けLib設置パス **/
	private Path executerLibSetDir =null;
	/** エグゼキューター向けBin設置パス **/
	private Path executerBinSetDir =null;
	/** エグゼキューター向けFileバックアップパス **/
	private Path executerFileBakDir =null;
	/** エグゼキューター向けLib入れ替え前待機パス **/
	private Path executerLibTmpDir =null;
	/** エグゼキューター向けLibダウンロードパス **/
	private Path executerLibDwnDir =null;
	/** エグゼキューター向けBinダウンロードパス **/
	private Path executerBinDwnDir =null;
	/** エグゼキューター向け基底ダウンロードパス **/
	private Path executerBaseDwnDir =null;
	
	// 外部依存動作をモックへ置換できるように分離
	private External external;
	static class External {
		String getAgentHome() {
			return Agent.getAgentHome();
		}

		List<File> getExecuterLib(Path path) throws IOException {
			List<File> ret  = new ArrayList<File>(); 
			File[] list = (new File(path.toString())).listFiles();
			if(list != null ){
				for(File rec : list){
					if(rec.isFile()){
						ret.add(rec);
					}
				}
			}
			return ret;
		}

		List<File> getExecuterBin(Path path) throws IOException {
			List<File> ret  = new ArrayList<File>(); 
			File[] list = (new File(path.toString())).listFiles();
			if(list != null ){
				for(File rec : list){
					if( rec.getName().endsWith(RPA_EXECUTER_SCRIPT_NAME_SUFFIX)){
						ret.add(rec);
					}
				}
			}
			return ret;
		}

		void deleteIfExist(Path path) throws IOException {
			Files.deleteIfExists(path);
		}
		
		void copyIfExist(Path src, Path dst) throws IOException {
			if (Files.exists(src)) {
				Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
			}
		}

		void moveIfExist(Path src, Path dst) throws IOException {
			if (Files.exists(src)) {
				Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
			}
		}

		void mkdirIfNoting(Path path) throws IOException {
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}
		}
		
		String getOsName() {
			return System.getProperty("os.name");
		}
		
		void writeFile(Path path, byte[] data) throws IOException {
			Files.write(path, data);
		}
	}

	
	/**
	 * コンストラクタです。
	 * 通常、唯一のインスタンスのみを生成します。
	 */
	public RpaExecuterUpdater() {
		this(new External());
	}
	RpaExecuterUpdater(External external) {
		this.external = external;
		executerLibSetDir = Paths.get(external.getAgentHome(), RPA_EXECUTER_LIB_SET_DIR_PATH_SUFFIX);
		executerFileBakDir = Paths.get(external.getAgentHome(), RPA_EXECUTER_BAK_DIR_PATH_SUFFIX);
		executerLibTmpDir = Paths.get(external.getAgentHome(), RPA_EXECUTER_LIB_TMP_DIR_PATH_SUFFIX);
		executerLibDwnDir = Paths.get(external.getAgentHome(), RPA_EXECUTER_LIB_DWN_DIR_PATH_SUFFIX);
		executerBinSetDir = Paths.get(external.getAgentHome(), RPA_EXECUTER_BIN_SET_DIR_PATH_SUFFIX);
		executerBinDwnDir = Paths.get(external.getAgentHome(), RPA_EXECUTER_BIN_DWN_DIR_PATH_SUFFIX);
		executerBaseDwnDir = Paths.get(external.getAgentHome(), RPA_EXECUTER_BASE_DWN_DIR_PATH_SUFFIX);
	}
	
	private void backupExecuterLib() throws IOException{
		external.mkdirIfNoting(executerFileBakDir);
		RpaWindowsUtil.setEveryoneFullAccessPriv(executerFileBakDir.toFile());
		// エグゼキューターの向けのjarをバックアップする（更新対象がある場合のみ）
		List<File> fileList = external.getExecuterLib(executerLibSetDir);
		for ( File rec : fileList){
			Path src =Paths.get(executerLibSetDir.toString() , rec.getName() );
			Path dst =Paths.get(executerFileBakDir.toString() , rec.getName() );
			
			external.copyIfExist(src, dst);
			if (Files.exists(dst)) {
				//一般ユーザによる削除向けにアクセス権調整
				RpaWindowsUtil.setEveryoneFullAccessPriv(dst.toFile());
			}
		}
	}	

	private void backupExecuterScript() throws IOException{
		
		external.mkdirIfNoting(executerFileBakDir);
		// エグゼキューターの向けのスクリプトをバックアップする（更新対象がある場合のみ）
		List<File> fileList = external.getExecuterBin(executerLibSetDir);
		for ( File rec : fileList){
			Path src =Paths.get(executerLibSetDir.toString() , rec.getName() );
			Path dst =Paths.get(executerFileBakDir.toString() , rec.getName() );
			external.copyIfExist(src, dst);
		}
	}

	private boolean updateExecuterScript() throws IOException{
		// エグゼキューターの向けのスクリプトを上書きする
		boolean ret = false; 
		List<File> fileList = external.getExecuterBin(executerBinDwnDir);
		for ( File rec : fileList){
			Path src =Paths.get(executerBinDwnDir.toString() , rec.getName() );
			Path dst =Paths.get(executerBinSetDir.toString() , rec.getName() );
			external.moveIfExist(src, dst);
			ret = true;
		}
		external.deleteIfExist(executerBinDwnDir);
		return ret;
	}

	private boolean settingExecuterLib() throws IOException{
		// エグゼキューターの向けのjarをアップデート用の待機パスに設置する。(一般ユーザ削除向けにアクセス権調整あり)
		external.mkdirIfNoting(executerLibTmpDir);
		RpaWindowsUtil.setEveryoneFullAccessPriv(executerLibTmpDir.toFile());
		boolean ret = false; 
		List<File> fileList = external.getExecuterLib(executerLibDwnDir);
		for ( File rec : fileList){
			Path src =Paths.get(executerLibDwnDir.toString() , rec.getName() );
			Path dst =Paths.get(executerLibTmpDir.toString() , rec.getName() );
			external.moveIfExist(src, dst);
			RpaWindowsUtil.setEveryoneFullAccessPriv(dst.toFile());
			ret = true;
		}
		external.deleteIfExist(executerLibDwnDir);

		// エグゼキューターの向けのjar を 一般ユーザでも上書きできるようにアクセス権を調整
		//  （一般ユーザによるエグゼキューター再起動によってアップデート用の待機パスから本番パスへ反映する場合を考慮）
		List<File> libList = external.getExecuterLib(executerLibSetDir);
		for ( File rec : libList){
			RpaWindowsUtil.setEveryoneFullAccessPriv(rec);
		}
		return ret;
	}
	
	private void deleteBackupScript() throws IOException{
		// エグゼキューターの向けのスクリプトのバックアップを削除する
		List<File> fileList = external.getExecuterBin(executerFileBakDir);
		for ( File rec : fileList){
			Path src =Paths.get(executerFileBakDir.toString() , rec.getName() );
			external.deleteIfExist(src);
		}
	}
	
	private void sendMessageIfExecuterExist(SendQueue sendQueue) throws IOException{
		m_log.debug("sendMessageIfExecuterExist() : start");

		// エグゼキューターの起動中チェックして必要なら 警告メッセージを送信
		Boolean isRunning = null;
		try {
			isRunning = RpaWindowsUtil.isRpaExecutorRunnning();
		} catch (HinemosUnknown | InterruptedException e) {
			m_log.warn("sendMessageIfExecuterExist() : command failed. " + e.getMessage(), e);
			return;
		}
		m_log.debug("sendMessageIfExecuterExist() : executor is running=" + isRunning);
		if(isRunning != null && isRunning){
			MessageSendableObject sendme = new MessageSendableObject();
			sendme.body = new AgtOutputBasicInfoRequest();
			sendme.body.setPluginId("AGT_UPDATE_CONFFILE");
			sendme.body.setPriority(PriorityConstant.TYPE_WARNING);
			sendme.body.setApplication(MessageConstant.AGENT.getMessage());
			sendme.body.setMessage(MessageConstant.MESSAGE_RPA_EXECUTER_UPDATE_IN_RUNNING_MSG.getMessage());
			sendme.body.setMessageOrg(MessageConstant.MESSAGE_RPA_EXECUTER_UPDATE_IN_RUNNING_ORGMSG.getMessage());
			sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
			sendme.body.setMonitorId("SYS");
			sendme.body.setFacilityId(""); // マネージャがセットする。
			sendme.body.setScopeText(""); // マネージャがセットする。
			sendQueue.put(sendme);
			m_log.debug("sendMessageIfExecuterExist() : sendQueue.put() sendme=" + sendme);
		}
	}
	/**
	 * エグゼキューター向けのファイルの更新<br>
	 * <br>
	 * エグゼキューター向けのBin（スクリプト）の変更とJarの変更準備（アップデート用の待機パスへの設置とバックアップ）を行う。<br>
	 * エグゼキューター向けのJarの実行環境への反映は、エグゼキューター再起動時に起動スクリプトが行い、バックアップも削除する。<br>
	 * 変更用ファイルのダウンロードは com.clustercontrol.agent.update.AgentUpdater.download()に依存している。<br>
	 * エグゼキューター向の変更があった場合にエグゼキューターが起動中ならマネージャへ警告通知を出す。<br>
	 * @param sendQueue
	 * @throws IOException
	 */

	public void updateJarAndScript(SendQueue sendQueue) throws IOException{
		
		backupExecuterScript();
		boolean updateScp =updateExecuterScript();
		deleteBackupScript();
		backupExecuterLib();
		boolean updateJar =settingExecuterLib();
		external.deleteIfExist(executerBaseDwnDir);
		if( updateScp || updateJar ){
			sendMessageIfExecuterExist(sendQueue);
		} 
	}	
	
}
