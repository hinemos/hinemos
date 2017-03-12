
package com.clustercontrol.jobmanagement.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * JobEditStateユーティリティクラス
 *
 * 以下を提供します。<BR>
 * <li>編集ロックに関するユーティリティ
 * <li>ジョブ登録時に更新する必要があるジョブユニットを管理するユーティリティ
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class JobEditStateUtil{
	/** Logger */
	private static Log m_log = LogFactory.getLog( JobEditStateUtil.class );

	/** <managerName, JobEditState> */
	private ConcurrentHashMap<String, JobEditState> jobEditStateMap = new ConcurrentHashMap<>();

	/** Private Constructor */
	private JobEditStateUtil(){}

	/** Get singleton */
	private static JobEditStateUtil getInstance(){
		return SingletonUtil.getSessionInstance( JobEditStateUtil.class );
	}

	public static JobEditState getJobEditState( String managerName ){
		ConcurrentHashMap<String, JobEditState> jobEditStateMap = getInstance().jobEditStateMap;
		JobEditState state = jobEditStateMap.get( managerName );
		if( null == state ){
			state = new JobEditState(managerName);
			jobEditStateMap.put( managerName, state );
		}
		return state;
	}
	
	public static Set<String> getManagerList() {
		ConcurrentHashMap<String, JobEditState> jobEditStateMap = getInstance().jobEditStateMap;
		return jobEditStateMap.keySet();
	}

	public static boolean existEditing () {
		ConcurrentHashMap<String, JobEditState> jobEditStateMap = getInstance().jobEditStateMap;
		for (JobEditState state : jobEditStateMap.values()) {
			if (state.isEditing()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Clear job lock states
	 */
	public static void release(String managerName){
		JobEditState state = getInstance().jobEditStateMap.get(managerName);
		if (state != null) {
			state.releaseEditLock();
		}
		getInstance().jobEditStateMap.remove(managerName);
	}
	

	/**
	 * 編集ロックの状態をすべてクリアする
	 */
	public static void releaseAll( ){
		m_log.debug("clearEditStateAll()");

		for( Entry<String, JobEditState> entry : getInstance().jobEditStateMap.entrySet() ){
			entry.getValue().clearEditStateAll();
		}
	}

	/**
	 * ジョブツリーをキャッシュをもとに生成する
	 * キャッシュはJobEditStateにマネージャごとに保持している
	 * 
	 * @return JobTreeItem
	 */
	public static JobTreeItem getJobTreeItem(){
		JobTreeItem tree = new JobTreeItem();
		JobInfo treeInfo = new JobInfo();
		treeInfo.setJobunitId( "" );
		treeInfo.setId( "" );
		treeInfo.setName( JobConstant.STRING_COMPOSITE );
		treeInfo.setType( JobConstant.TYPE_COMPOSITE );
		tree.setData(treeInfo);
		
		// ジョブツリーのトップを生成
		JobTreeItem top = new JobTreeItem();
		JobInfo itemInfo = new JobInfo();
		itemInfo.setJobunitId("");
		itemInfo.setId("");
		itemInfo.setName(MessageConstant.JOB.getMessage());
		itemInfo.setType(JobConstant.TYPE_COMPOSITE);
		top.setData(itemInfo);
		top.setParent(tree);
		tree.getChildren().add(top);
		
		boolean isAllNull = true; //すべてのマネージャのジョブツリーのキャッシュがないことを判定するフラグ
		for( Entry<String, JobEditState> entry : getInstance().jobEditStateMap.entrySet() ){
			JobTreeItem child = entry.getValue().getJobTree();
			if (child != null) {
				isAllNull = false;
				child.setParent(top);
				top.getChildren().add(child);
			}
		}
		if (isAllNull) {
			return null;
		}
		
		return tree;
	}

	/**
	 * ジョブツリーのキャッシュを再作成し、それをもとにジョブツリーを生成する
	 *
	 * @param ownerRoleId
	 * @param m_treeOnly
	 * @return
	 */
	public static JobTreeItem updateJobTree( String ownerRoleId, boolean m_treeOnly ){
		// Update sub trees
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for( String managerName : EndpointManager.getActiveManagerSet() ){
			try{
				getJobEditState(managerName).updateJobTree(ownerRoleId, m_treeOnly);
			} catch (InvalidRole_Exception e ){
				// アクセス権なしの場合、エラーダイアログを表示する
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e ){
				m_log.warn("update() getJobTree, " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		
		JobTreeItem tree = getJobTreeItem();
		return tree;
	}
	
}
