/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs.job;

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
import org.openapitools.client.model.JobEndStatusInfoResponse;
import org.openapitools.client.model.JobResourceInfoResponse;
import org.openapitools.client.model.ReplaceJobunitRequest;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.jobmanagement.bean.ResourceJobConstant;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.Messages;
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
		String getJobName(String facilityId);
		String getJobId(String facilityId);
		JobResourceInfoResponse.ResourceActionEnum getAction();
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
		JobDetailPage detailPage = (JobDetailPage)getPage(JobDetailPage.pageName);
		JobTreePage jobPage = (JobTreePage)getPage(JobTreePage.pageName);
		FacilityTreePage facilityPage = (FacilityTreePage)getPage(FacilityTreePage.pageName);

		JobTreeItemWrapper parent = jobPage.getSelectedItem();

		String facilityId = facilityPage.getSelectedItem().getData().getFacilityId();
		JobTreeItemWrapper item = new JobTreeItemWrapper();
		JobTreeItemUtil.addChildren(parent, item);

		item.setData(JobTreeItemUtil.getNewJobInfo(parent.getData().getJobunitId(), JobInfoWrapper.TypeEnum.RESOURCEJOB));

		// リソース制御ジョブ情報の作成
		JobResourceInfoResponse resourceJobInfo = new JobResourceInfoResponse();
		item.getData().setResource(resourceJobInfo);
		resourceJobInfo.setResourceType(JobResourceInfoResponse.ResourceTypeEnum.COMPUTE_FACILITY_ID);
		resourceJobInfo.setResourceCloudScopeId(provider.getCloudScope().getId());
		resourceJobInfo.setResourceAction(provider.getAction());
		resourceJobInfo.setResourceSuccessValue(ResourceJobConstant.SUCCESS_VALUE);
		resourceJobInfo.setResourceFailureValue(ResourceJobConstant.FAILURE_VALUE);
		resourceJobInfo.setResourceTargetId(facilityId);
		resourceJobInfo.setResourceStatusConfirmTime(ResourceJobConstant.STATUS_CONFIRM_TIME);
		resourceJobInfo.setResourceStatusConfirmInterval(ResourceJobConstant.STATUS_CONFIRM_INTERVAL);
		resourceJobInfo.setResourceNotifyScope(facilityId);

		JobEndStatusInfoResponse normalEndStatus = new JobEndStatusInfoResponse();
		normalEndStatus.setType(JobEndStatusInfoResponse.TypeEnum.NORMAL);
		normalEndStatus.setValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		normalEndStatus.setStartRangeValue(0);
		normalEndStatus.setEndRangeValue(0);
		item.getData().getEndStatus().add(normalEndStatus);
		JobEndStatusInfoResponse warningEndStatus = new JobEndStatusInfoResponse();
		warningEndStatus.setType(JobEndStatusInfoResponse.TypeEnum.WARNING);
		warningEndStatus.setValue(EndStatusConstant.INITIAL_VALUE_WARNING);
		warningEndStatus.setStartRangeValue(1);
		warningEndStatus.setEndRangeValue(1);
		item.getData().getEndStatus().add(warningEndStatus);
		JobEndStatusInfoResponse abnormalEndStatus = new JobEndStatusInfoResponse();
		abnormalEndStatus.setType(JobEndStatusInfoResponse.TypeEnum.ABNORMAL);
		abnormalEndStatus.setValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		item.getData().getEndStatus().add(abnormalEndStatus);
		item.getData().setNormalPriority(JobInfoWrapper.NormalPriorityEnum.INFO);
		item.getData().setPropertyFull(true);
		item.getData().setWarnPriority(JobInfoWrapper.WarnPriorityEnum.WARNING);

		item.getData().setWaitRule(JobTreeItemUtil.getNewJobWaitRuleInfo());

		item.getData().setOwnerRoleId(parent.getData().getOwnerRoleId());
		item.getData().setId(detailPage.getJobId());
		item.getData().setName(detailPage.getJobName());
		item.getData().setBeginPriority(JobInfoWrapper.BeginPriorityEnum.INFO);
		
		try {
			while(parent != null && parent.getData().getType() !=  JobInfoWrapper.TypeEnum.JOBUNIT) {
				parent = parent.getParent();
			}
			
			if (parent == null)
				throw new InvalidStateException();
			
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(manager.getManagerName());
			
			ReplaceJobunitRequest request = new ReplaceJobunitRequest();
			request.setJobTreeItem(JobTreeItemUtil.getRequestFromItem(JobUtil.getTopJobUnitTreeItem(parent)) );
			wrapper.replaceJobunit(parent.getData().getJobunitId(),request);
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
	
	protected JobTreeItemWrapper getTopJobTreeItem(JobTreeItemWrapper item) {
		if (item.getParent() != null)
			return item.getParent();
		return item;
	}

	protected JobTreeItemWrapper findJobTreeItem(JobTreeItemWrapper item, String jobUnitId, String jobId) {
		if (item.getData().getJobunitId().equals(jobUnitId) && item.getData().getId().equals(jobId)) {
			return item;
		}
		for (JobTreeItemWrapper child: item.getChildren()) {
			JobTreeItemWrapper matched = findJobTreeItem(child, jobUnitId, jobId);
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
						FacilityTreeItemResponse facilityTreeItem = facilityTreePage.getSelectedItem();

						JobTreePage jobTreePage = (JobTreePage)getPage(JobTreePage.pageName);
						JobTreeItemWrapper parent =jobTreePage.getSelectedItem();

						JobTreeItemWrapper top = getTopJobTreeItem(parent);

						int count = 0;
						String jobIdOrigine = provider.getJobId(facilityTreeItem.getData().getFacilityId()).replace(" ", "_").replaceAll("[^0-9a-zA-Z_\\-\\.@]", "");
						String jobId = jobIdOrigine;
						while (true) {
							JobTreeItemWrapper matched = findJobTreeItem(top, parent.getData().getJobunitId(), jobId);
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
						JobTreeItemWrapper jobTreeItem = jobTreePage.getSelectedItem();
						
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
	
	public JobTreeItemWrapper getJobTreeItem() {
		JobTreePage page = (JobTreePage)getPage(JobTreePage.pageName);
		return page.getSelectedItem();
	}
	
	public FacilityTreeItemResponse getFacilityTreeItem() {
		FacilityTreePage page = (FacilityTreePage)getPage(FacilityTreePage.pageName);
		return page.getSelectedItem();
	}
}
