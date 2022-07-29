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
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
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
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.InvalidStateException;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.IResource;
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.ControlUtil;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


public class CreateJobWizard extends Wizard {
	
	private static final Log logger = LogFactory.getLog(CreateJobWizard.class);
	
	public interface IJobDetailProvider {
		ICloudScope getCloudScope();
		IResource getResource();
		JobResourceInfoResponse.ResourceActionEnum getAction();
		String getJobId();
		String cutJobId(int num);
	}
	
	@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
	public static abstract class JobTreeItemMixin {
		@JsonIdentityReference(alwaysAsId = true)
		public abstract JobTreeItemWrapper getParent();
	}
	
	protected final String ownerRoleId;
	protected final IJobDetailProvider provider;
	protected boolean firstShowDetail;
	protected CloudRestClientWrapper endpoint;
	
	public CreateJobWizard(CloudRestClientWrapper endpoint, String title, String ownerRoleId, IJobDetailProvider provider) {
		setWindowTitle(title);
		this.ownerRoleId = ownerRoleId;
		this.provider = provider;
		this.endpoint = endpoint;
	}
	
	public CreateJobWizard(CloudRestClientWrapper endpoint, String title, IJobDetailProvider provider) {
		setWindowTitle(title);
		this.ownerRoleId = null;
		this.provider = provider;
		this.endpoint = endpoint;
	}

	@Override
	public void addPages() {
		addPage(new JobTreePage(provider.getCloudScope().getCloudScopes().getHinemosManager().getManagerName(), ownerRoleId));
		addPage(new JobDetailPage());
	}

	@Override
	public boolean performFinish() {
		JobDetailPage detailPage = (JobDetailPage)getPage(JobDetailPage.pageName);
		JobTreePage jobPage = (JobTreePage)getPage(JobTreePage.pageName);
		
		JobTreeItemWrapper parent = jobPage.getSelectedItem();
		
		JobTreeItemWrapper item = new JobTreeItemWrapper();
		
		item.setData(JobTreeItemUtil.getNewJobInfo(parent.getData().getJobunitId(), JobInfoWrapper.TypeEnum.RESOURCEJOB));

		IHinemosManager manager = provider.getCloudScope().getCloudScopes().getHinemosManager();
		// リソース制御ジョブ情報の作成
		JobResourceInfoResponse resourceJobInfo = new JobResourceInfoResponse();
		item.getData().setResource(resourceJobInfo);
		resourceJobInfo.setResourceCloudScopeId(provider.getCloudScope().getId());
		resourceJobInfo.setResourceLocationId(provider.getResource().getLocation().getId());
		resourceJobInfo.setResourceAction(provider.getAction());
		resourceJobInfo.setResourceSuccessValue(ResourceJobConstant.SUCCESS_VALUE);
		resourceJobInfo.setResourceFailureValue(ResourceJobConstant.FAILURE_VALUE);

		// リソース制御ジョブ情報：対象インスタンス、対象ストレージ、通知先スコープセット
		if (provider.getResource() instanceof IInstance) {
			IInstance instance = (IInstance) provider.getResource();
			
			resourceJobInfo.setResourceStatusConfirmTime(ResourceJobConstant.STATUS_CONFIRM_TIME);
			resourceJobInfo.setResourceStatusConfirmInterval(ResourceJobConstant.STATUS_CONFIRM_INTERVAL);

			Boolean findNode = false;
			if (instance.getFacilityId() != null && !instance.getFacilityId().equals("")) {
				RepositoryRestClientWrapper repositoryWrapper = RepositoryRestClientWrapper.getWrapper(manager.getManagerName());
				try {
					findNode = repositoryWrapper.isNode(instance.getFacilityId()).getIsNode();
				} catch (FacilityNotFound | InvalidRole e) {
					logger.debug("performFinish() not found facilityId" + e.getMessage());
				} catch (InvalidUserPass | RestConnectFailed | HinemosUnknown e) {
					logger.error(e.getMessage(), e);

					// 失敗報告ダイアログを生成
					ControlUtil.openError(e, CloudStringConstants.msgErrorFinishCreateJob);
					JobTreeItemUtil.removeChildren(jobPage.getSelectedItem(), item);
					return false;
				}
			}
			if (findNode) {
				resourceJobInfo.setResourceType(JobResourceInfoResponse.ResourceTypeEnum.COMPUTE_FACILITY_ID);
				resourceJobInfo.setResourceTargetId(instance.getFacilityId());
				resourceJobInfo.setResourceLocationId("");
				resourceJobInfo.setResourceNotifyScope(instance.getFacilityId());
			} else {
				resourceJobInfo.setResourceType(JobResourceInfoResponse.ResourceTypeEnum.COMPUTE_COMPUTE_ID);
				resourceJobInfo.setResourceTargetId(instance.getId());
				resourceJobInfo.setResourceNotifyScope(provider.getCloudScope().getNodeId().replaceFirst("_Node$", ""));
			}

		} else if (provider.getResource() instanceof IStorage) {
			IStorage storage = (IStorage) provider.getResource();
			resourceJobInfo.setResourceType(JobResourceInfoResponse.ResourceTypeEnum.STORAGE);
			resourceJobInfo.setResourceTargetId(storage.getId());
			resourceJobInfo.setResourceNotifyScope(provider.getCloudScope().getNodeId().replaceFirst("_Node$", ""));
		}

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

		item.getData().setWaitRule(JobTreeItemUtil.getNewJobWaitRuleInfo());

		item.getData().setNormalPriority(JobInfoWrapper.NormalPriorityEnum.INFO);
		item.getData().setPropertyFull(true);
		item.getData().setWarnPriority(JobInfoWrapper.WarnPriorityEnum.WARNING);
		
		JobTreeItemUtil.addChildren(parent, item);
		item.getData().setOwnerRoleId(parent.getData().getOwnerRoleId());
		item.getData().setId(detailPage.getJobId());
		item.getData().setJobunitId(parent.getData().getJobunitId());
		item.getData().setName(detailPage.getJobName());
		item.getData().setBeginPriority(JobInfoWrapper.BeginPriorityEnum.INFO);

		try {
			while(parent != null && parent.getData().getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
				parent = parent.getParent();
			}
			
			if (parent == null)
				throw new InvalidStateException();
			
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(manager.getManagerName());
			ReplaceJobunitRequest request = new ReplaceJobunitRequest();
			request.setJobTreeItem(JobTreeItemUtil.getRequestFromItem(JobUtil.getTopJobUnitTreeItem(parent)));
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
						JobTreePage jobTreePage = (JobTreePage)getPage(JobTreePage.pageName);
						JobTreeItemWrapper parent =jobTreePage.getSelectedItem();

						JobTreeItemWrapper top = getTopJobTreeItem(parent);

						int count = 0;
						String jobIdOrigine = provider.getJobId().replace(" ", "_").replaceAll("[^0-9a-zA-Z_\\-\\.@]", "");
						String jobId = jobIdOrigine;
						while (true) {
							JobTreeItemWrapper matched = findJobTreeItem(top, parent.getData().getJobunitId(), jobId);
							if (matched == null)
								break;
							jobId = jobIdOrigine + "-" + ++count;
							//jobidのチェック後切り詰め
							if (jobId.length() > CloudUtil.jobIdMaxLength) {
								jobIdOrigine = provider.cutJobId(String.valueOf(count).length()+1).replace(" ", "_").replaceAll("[^0-9a-zA-Z_\\-\\.@]", "");
								jobId = jobIdOrigine + "-" + count;
							}
							
						}
						
						JobDetailPage detailPage = (JobDetailPage)nextPage;
						detailPage.setJobId(jobId);
						detailPage.setJobName(jobId);
						
						firstShowDetail = true;
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
