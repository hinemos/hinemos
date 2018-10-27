/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs.job;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobCommandInfo;
import com.clustercontrol.ws.jobmanagement.JobEndStatusInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.InvalidStateException;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.ControlUtil;


public class CreateGroupJobWizard extends Wizard {
	
	private static final Log logger = LogFactory.getLog(CreateGroupJobWizard.class);
	
	public interface IJobDetailProvider {
		ICloudScope getCloudScope();
		String getCommand(CloudEndpoint endpoint, String facilityId) throws Exception;
		String getJobName(String facilityId);
		String getJobId(String facilityId);
	}
	
	protected final String ownerRoleId;
	protected final IJobDetailProvider provider;
	
	protected boolean firstShowDetail;
	
	public CreateGroupJobWizard(String title, String ownerRoleId, IJobDetailProvider provider) {
		setWindowTitle(title);
		this.ownerRoleId = ownerRoleId;
		this.provider = provider;
	}
	
	public CreateGroupJobWizard(String title, IJobDetailProvider provider) {
		setWindowTitle(title);
		this.ownerRoleId = null;
		this.provider = provider;
	}

	@Override
	public void addPages() {
		addPage(new JobTreePage(provider.getCloudScope().getCloudScopes().getHinemosManager().getManagerName(), ownerRoleId));
		addPage(new FacilityTreePage(provider.getCloudScope().getCloudScopes().getHinemosManager().getManagerName(), ownerRoleId));
		addPage(new JobDetailPage());
	}

	@Override
	public boolean performFinish() {
		IHinemosManager manager = provider.getCloudScope().getCloudScopes().getHinemosManager();
		CloudEndpoint endpoint = manager.getEndpoint(CloudEndpoint.class);
		List<String> facilities;
		try {
			facilities = endpoint.getManagerFacilityIds();
			if (facilities.isEmpty()) {
				// 失敗報告ダイアログを生成
				MessageDialog.openError(null, Messages.getString("failed"), CloudConstants.bundle_messages.getString("message.job.manager_node.not_found"));
				return false;
			}
		} catch (CloudManagerException | com.clustercontrol.ws.xcloud.InvalidRole_Exception | com.clustercontrol.ws.xcloud.InvalidUserPass_Exception e) {
			logger.error(e.getMessage(), e);

			// 失敗報告ダイアログを生成
			ControlUtil.openError(e, CloudStringConstants.msgErrorFinishCreateJob);
			return false;
		} 
		JobDetailPage detailPage = (JobDetailPage)getPage(JobDetailPage.pageName);
		JobTreePage jobPage = (JobTreePage)getPage(JobTreePage.pageName);
		FacilityTreePage facilityPage = (FacilityTreePage)getPage(FacilityTreePage.pageName);

		JobTreeItem parent = jobPage.getSelectedItem();

		String facilityId = facilityPage.getSelectedItem().getData().getFacilityId();
		JobTreeItem item = new JobTreeItem();
		JobTreeItemUtil.addChildren(parent, item);

		item.setData(JobTreeItemUtil.getNewJobInfo(parent.getData().getJobunitId(), JobConstant.TYPE_JOB));
		item.getData().setCommand(new JobCommandInfo());
		item.getData().getCommand().setMessageRetryEndFlg(false);
		item.getData().getCommand().setMessageRetryEndValue(-1);
		item.getData().getCommand().setCommandRetry(10);
		item.getData().getCommand().setCommandRetryFlg(true);
		item.getData().getCommand().setMessageRetry(10);
		item.getData().getCommand().setProcessingMethod(0);
		item.getData().getCommand().setScope("");
		item.getData().getCommand().setSpecifyUser(false);
		item.getData().getCommand().setStopType(1);
		item.getData().getCommand().setCommandRetryFlg(false);

		JobEndStatusInfo normalEndStatus = new JobEndStatusInfo();
		normalEndStatus.setType(EndStatusConstant.TYPE_NORMAL);
		normalEndStatus.setValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		normalEndStatus.setStartRangeValue(0);
		normalEndStatus.setEndRangeValue(0);
		item.getData().getEndStatus().add(normalEndStatus);
		JobEndStatusInfo warningEndStatus = new JobEndStatusInfo();
		warningEndStatus.setType(EndStatusConstant.TYPE_WARNING);
		warningEndStatus.setValue(EndStatusConstant.INITIAL_VALUE_WARNING);
		warningEndStatus.setStartRangeValue(1);
		warningEndStatus.setEndRangeValue(1);
		item.getData().getEndStatus().add(warningEndStatus);
		JobEndStatusInfo abnormalEndStatus = new JobEndStatusInfo();
		abnormalEndStatus.setType(EndStatusConstant.TYPE_ABNORMAL);
		abnormalEndStatus.setValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		item.getData().getEndStatus().add(abnormalEndStatus);			
		item.getData().setNormalPriority(PriorityConstant.TYPE_INFO);
		item.getData().setPropertyFull(true);
		item.getData().setWarnPriority(PriorityConstant.TYPE_WARNING);

		item.getData().setWaitRule(JobTreeItemUtil.getNewJobWaitRuleInfo());

		item.getData().setOwnerRoleId(parent.getData().getOwnerRoleId());
		item.getData().setId(detailPage.getJobId());
		item.getData().setName(detailPage.getJobName());
		item.getData().getCommand().setFacilityID(facilities.get(0));
		item.getData().setBeginPriority(PriorityConstant.TYPE_INFO);
		
		try {
			item.getData().getCommand().setStartCommand(provider.getCommand(endpoint, facilityId));
			
			while(parent != null && parent.getData().getType() !=  JobConstant.TYPE_JOBUNIT) {
				parent = parent.getParent();
			}
			
			if (parent == null)
				throw new InvalidStateException();
			
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(manager.getManagerName());
			
			wrapper.registerJobunit(JobUtil.getTopJobUnitTreeItem(parent));
			JobEditState jobEditState = JobEditStateUtil.getJobEditState(manager.getManagerName());
			jobEditState.updateJobTree(ownerRoleId, false);
			
			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
				null,
				Messages.getString("successful"),
				CloudConstants.bundle_messages.getString("message.finish.job.regist"));
			
			if (PlatformUI.getWorkbench() != null) {
				IPerspectiveDescriptor descriptor = PlatformUI.getWorkbench()
						.getPerspectiveRegistry().findPerspectiveWithId("com.clustercontrol.jobmanagement.ui.JobSettingPerspective");
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setPerspective(descriptor);
				
				IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(JobListView.ID);
				if (view instanceof JobListView) {
					((JobListView) view).update();
				}
			}
			
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
	
			// 失敗報告ダイアログを生成
			ControlUtil.openError(e, CloudStringConstants.msgErrorFinishCreateJob);
			JobTreeItemUtil.removeChildren(jobPage.getSelectedItem(), item);
			return false;
		}
	}
	
	protected JobTreeItem getTopJobTreeItem(JobTreeItem item) {
		if (item.getParent() != null)
			return item.getParent();
		return item;
	}

	protected JobTreeItem findJobTreeItem(JobTreeItem item, String jobUnitId, String jobId) {
		if (item.getData().getJobunitId().equals(jobUnitId) && item.getData().getId().equals(jobId)) {
			return item;
		}
		for (JobTreeItem child: item.getChildren()) {
			JobTreeItem matched = findJobTreeItem(child, jobUnitId, jobId);
			if (matched != null)
				return matched;
		}
		return null;
	}
	
	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		super.setContainer(wizardContainer);
		if (wizardContainer instanceof WizardDialog) {
			((WizardDialog)wizardContainer).addPageChangedListener(new IPageChangedListener() {
				@Override
				public void pageChanged(PageChangedEvent event) {
					Object nextPage = event.getSelectedPage();
					if (nextPage instanceof JobDetailPage && !firstShowDetail) {
						FacilityTreePage facilityTreePage = (FacilityTreePage)getPage(FacilityTreePage.pageName);
						FacilityTreeItem facilityTreeItem = facilityTreePage.getSelectedItem();

						JobTreePage jobTreePage = (JobTreePage)getPage(JobTreePage.pageName);
						JobTreeItem parent =jobTreePage.getSelectedItem();

						JobTreeItem top = getTopJobTreeItem(parent);

						int count = 0;
						String jobIdOrigine = provider.getJobId(facilityTreeItem.getData().getFacilityId()).replace(" ", "_").replaceAll("[^0-9a-zA-Z_\\-\\.@]", "");
						String jobId = jobIdOrigine;
						while (true) {
							JobTreeItem matched = findJobTreeItem(top, parent.getData().getJobunitId(), jobId);
							if (matched == null)
								break;
							jobId = jobIdOrigine + "-" + ++count;
						}
						
						JobDetailPage detailPage = (JobDetailPage)nextPage;
						detailPage.setJobId(jobId);
						detailPage.setJobName(jobId);
						
						firstShowDetail = true;
					} else if (nextPage instanceof FacilityTreePage) {
						JobTreePage jobTreePage = (JobTreePage)getPage(JobTreePage.pageName);
						JobTreeItem jobTreeItem = jobTreePage.getSelectedItem();
						
						((FacilityTreePage)nextPage).setOwnerRole(jobTreeItem.getData().getOwnerRoleId());
					}
				}
			});
		}
	}
	
	public String getJobName() {
		JobDetailPage page = (JobDetailPage)getPage(JobDetailPage.pageName);
		return page.getJobName();
	}
	
	public String getJobId() {
		JobDetailPage page = (JobDetailPage)getPage(JobDetailPage.pageName);
		return page.getJobId();
	}
	
	public JobTreeItem getJobTreeItem() {
		JobTreePage page = (JobTreePage)getPage(JobTreePage.pageName);
		return page.getSelectedItem();
	}
	
	public FacilityTreeItem getFacilityTreeItem() {
		FacilityTreePage page = (FacilityTreePage)getPage(FacilityTreePage.pageName);
		return page.getSelectedItem();
	}
}
