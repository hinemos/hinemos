/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.figure;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ScalableLayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.openapitools.client.model.JobDetailInfoResponse;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;
import org.openapitools.client.model.JobmapIconImageInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.EndStatusImageConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.jobmanagement.bean.StatusImageConstant;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobmapIconImageUtil;
import com.clustercontrol.jobmap.composite.JobMapComposite;
import com.clustercontrol.jobmap.editpart.MapViewController;
import com.clustercontrol.jobmap.util.JobmapIconImageCacheEntry;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * アイコン(ノード)画像のクラス
 * @since 1.0.0
 */
public class JobFigure extends Figure implements ISelection {

	// ログ
	private static Log m_log = LogFactory.getLog( JobFigure.class );

	private JobTreeItemWrapper m_jobTreeItem;
	public static final int textHeight = 35;
	public static final int jobnetBorder = 10;
	public static final int lineWidth = 2;
	private final static String fontStr = "MS UI Gothic";
	// フォント
	private final static Font jobNetFont = new Font(
			Display.getCurrent(), fontStr, 10,  SWT.BOLD);

	// 3つのレイヤーで構成する。
	// layerStackの上にlayerToolbar。その上にlayerXY。
	private ScalableLayeredPane m_layerXY; //背景(ジョブネットで利用)

	private GRoundedRectangle m_background;

	private Layer m_baseLayer = null;

	private final Rectangle zeroRectangle;

	private final JobMapEditorView m_editorView;

	// 待ち条件の時計アイコンイメージ
	private static Image waitImage;
	// 待ち条件の時計＋横断ジョブアイコンイメージ
	private static Image waitDoubleImage;
	// 待ち条件の横断ジョブアイコンイメージ
	private static Image waitCrossingJobImage;
	// 待ち条件群のアイコンイメージ
	private static Image waitGroupImage;

	private ImageFigure m_collapseExpandImageFigure;

	private String m_collapseExpandImageId;

	private Point m_position;

	private boolean m_collapse;

	private Dimension m_size = new Dimension();

	private JobMapComposite m_jobMapComposite;

	private MapViewController m_controller;

	private String m_managerName;

	private ImageFigure m_iconImageFigure;

	private JobmapImageCacheUtil m_iconCache;

	public JobFigure(String managerName, JobTreeItemWrapper item, JobMapEditorView editorView, JobMapComposite jobMapComposite, boolean collapse){
		this.setFocusTraversable(true);
		this.setRequestFocusEnabled(true);
		// 背景色を白に設定する
		this.setBackgroundColor(ColorConstantsWrapper.white());
		this.m_jobTreeItem = item;
		this.m_editorView = editorView;
		this.m_jobMapComposite = jobMapComposite;
		this.m_controller = new MapViewController(jobMapComposite);
		this.m_collapse = collapse;
		this.zeroRectangle = new Rectangle(new Point(0, 0), new Dimension(-1, -1));
		this.m_managerName = managerName;

		// 設定情報の取得
		this.m_controller.applySetting();
		
		//アイコンキャッシュの取得
		m_iconCache = JobmapImageCacheUtil.getInstance();

		// アイコンイメージの更新
		updateIconImage();
	}

	public void setJob(JobTreeItemWrapper item){
		m_jobTreeItem = item;
	}

	/**
	 * アイコンイメージの更新
	 */
	public void updateIconImage() {

		if (isIconImageJob()) {
			JobmapIconImageCacheEntry cacheEntry = null;
			JobmapIconImageInfoResponse jobmapIconImage = null;
			byte[] filedata = null;
			if (this.m_jobTreeItem.getData().getIconId() != null
					&& !this.m_jobTreeItem.getData().getIconId().equals("")) {
				try {
					cacheEntry
						= m_iconCache.getJobmapIconImageCacheEntry(this.m_managerName, this.m_jobTreeItem.getData().getIconId());
					jobmapIconImage = cacheEntry.getJobmapIconImage();
					filedata = cacheEntry.getFiledata();
				} catch (IconFileNotFound e) {
					jobmapIconImage = null;
				} catch (InvalidRole e) {
					// アクセス権なしの場合、エラーダイアログを表示する
					if (ClientSession.isDialogFree()) {
						ClientSession.occupyDialog();
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
						ClientSession.freeDialog();
					}
					return;
				} catch (InvalidUserPass e) {
					if (ClientSession.isDialogFree()) {
						ClientSession.occupyDialog();
						MessageDialog.openError(
								null,
								Messages.getString("failed"),
								Messages.getString("message.job.140") + " " + HinemosMessage.replace(e.getMessage()));
						ClientSession.freeDialog();
					}
					return;
				} catch (InvalidSetting e) {
					if (ClientSession.isDialogFree()) {
						ClientSession.occupyDialog();
						MessageDialog.openError(
								null,
								Messages.getString("failed"),
								Messages.getString("message.job.140") + " " + HinemosMessage.replace(e.getMessage()));
						ClientSession.freeDialog();
					}
					return;
				} catch (Exception e) {
					m_log.warn("action(), " + HinemosMessage.replace(e.getMessage()), e);
					if (ClientSession.isDialogFree()) {
						ClientSession.occupyDialog();
						MessageDialog.openError(
								null,
								Messages.getString("failed"),
								Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
						ClientSession.freeDialog();
					}
					return;
				}
			}
			
			// iconIdが未指定または指定されたiconIdの画像が存在しなかった場合はデフォルトアイコンで表示する
			if (jobmapIconImage == null) {
				if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET
						|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOBNET) {
					cacheEntry 
						= m_iconCache.getJobmapIconImageDefaultJobnet(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.APPROVALJOB) {
					cacheEntry = m_iconCache.getJobmapIconImageDefaultApproval(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.MONITORJOB) {
					cacheEntry = m_iconCache.getJobmapIconImageDefaultMonitor(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.FILEJOB) {
					cacheEntry = m_iconCache.getJobmapIconImageDefaultFile(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB) {
					cacheEntry = m_iconCache.getJobmapIconImageDefaultFileCheck(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
					cacheEntry = m_iconCache.getJobmapIconImageDefaultResource(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB) {
					cacheEntry = m_iconCache.getJobmapIconImageDefaultJobLinkSend(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB) {
					cacheEntry = m_iconCache.getJobmapIconImageDefaultJobLinkRcv(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.RPAJOB) {
					cacheEntry = m_iconCache.getJobmapIconImageDefaultRpa(this.m_managerName);
				} else {
					cacheEntry 
						= m_iconCache.getJobmapIconImageDefaultJob(this.m_managerName);
				}
				if (cacheEntry != null) {
					jobmapIconImage = cacheEntry.getJobmapIconImage();
					filedata = cacheEntry.getFiledata();
				}
			}
			if (jobmapIconImage == null) {
				if (ClientSession.isDialogFree()) {
					ClientSession.occupyDialog();
					String iconId = "";
					if (this.m_jobTreeItem.getData().getIconId() == null
							|| this.m_jobTreeItem.getData().getIconId().equals("")) {
						if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET
								|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOBNET) {
							iconId = m_iconCache.getJobmapIconIdDefaultJobnet(this.m_managerName);
						} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.APPROVALJOB) {
							iconId = m_iconCache.getJobmapIconIdDefaultApproval(this.m_managerName);
						} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.MONITORJOB) {
							iconId = m_iconCache.getJobmapIconIdDefaultMonitor(this.m_managerName);
						} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.FILEJOB) {
							iconId = m_iconCache.getJobmapIconIdDefaultFile(this.m_managerName);
						} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB) {
							iconId = m_iconCache.getJobmapIconIdDefaultFileCheck(this.m_managerName);
						} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
							iconId = m_iconCache.getJobmapIconIdDefaultResource(this.m_managerName);
						} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB) {
							iconId = m_iconCache.getJobmapIconIdDefaultJobLinkSend(this.m_managerName);
						} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB) {
							iconId = m_iconCache.getJobmapIconIdDefaultJobLinkRcv(this.m_managerName);
						} else if (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.RPAJOB) {
							iconId = m_iconCache.getJobmapIconIdDefaultRpa(this.m_managerName);
						} else {
							iconId
								= m_iconCache.getJobmapIconIdDefaultJob(this.m_managerName);
						}
					} else {
						iconId = this.m_jobTreeItem.getData().getIconId();
					}
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.job.148", new String[]{iconId}));
					ClientSession.freeDialog();
				}
				return;
			}
			m_iconImageFigure = new ImageFigure(m_iconCache.loadGraphicImage(jobmapIconImage, filedata));
		}
	}

	/**
	 * 配下の階層数を返す。
	 */
	public static int getDepth(JobTreeItemWrapper item) {
		int ret = 0;
		if (item.getChildren() == null || item.getChildren().size() == 0) {
			return ret;
		}
		for (JobTreeItemWrapper child : item.getChildren()) {
			int childDepth = getDepth(child);
			if (ret < childDepth) {
				ret = childDepth;
			}
		}
		ret ++;
		return ret;
	}

	/**
	 * ジョブ情報の描画
	 */
	public void draw() throws HinemosUnknown {
		// 上書きのため、一度全部消す。
		this.removeAll();

		// レイアウトを設定する
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		layout.setStretchMinorAxis(false);
		this.setLayoutManager(layout);

		// 背景を作成する
		if (isIconImageJob()) {
		
			if(m_iconImageFigure==null){
				//アイコン情報が取得できていない場合、HinemosUnknownとする
				m_log.warn("draw() . m_iconImageFigure is null. can't execute for draw ");
				throw new HinemosUnknown(com.clustercontrol.jobmap.messages.Messages.getString("message.icon.notfound.error"));
			}

			// 最上位レイヤー
			m_baseLayer = new Layer();
			m_baseLayer.setLayoutManager(new FlowLayout(false));
			m_baseLayer.setSize(JobmapIconImageUtil.ICON_WIDTH + 8, JobmapIconImageUtil.ICON_HEIGHT + SizeConstant.SIZE_TEXT_HEIGHT + 8);

			//layerStackは一番下のレイヤー。丸い四角を描画。
			Layer layerStack = new Layer();
			layerStack.setLayoutManager(new StackLayout());
			layerStack.setPreferredSize(JobmapIconImageUtil.ICON_WIDTH + 8, JobmapIconImageUtil.ICON_HEIGHT + 8);

			m_background = new GRoundedRectangle();
			m_background.setSize(layerStack.getSize());
			m_size.setSize(layerStack.getSize());
			layerStack.add(m_background);
			// ジョブイメージ
			layerStack.add(m_iconImageFigure);
			// 参照ジョブ・参照ジョブネット
			if (m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOB
					|| m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOBNET) {
				ImageFigure referImageFigure = new ImageFigure(ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_REFER)
						, PositionConstants.SOUTH_WEST);
				layerStack.add(referImageFigure);
			}
			// 同時実行制御キュー
			else {
				ImageFigure queueImage = getQueueIcon();
				if (queueImage != null) {
					queueImage.setAlignment(PositionConstants.SOUTH_WEST);
					layerStack.add(queueImage);
				}
			}
			// ジョブネット
			if (m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET) {
				Layer layerTitle = new Layer();
				layerTitle.setLayoutManager(new BorderLayout());

				m_collapseExpandImageFigure = new ImageFigure(ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_EXPAND), 
						PositionConstants.NORTH_EAST);
				layerTitle.add(m_collapseExpandImageFigure, BorderLayout.RIGHT);
				layerStack.add(layerTitle);
				m_collapseExpandImageFigure.addMouseListener(new MouseListener() {
					@Override
					public void mouseDoubleClicked(MouseEvent arg0) {
					}

					@Override
					public void mousePressed(MouseEvent arg0) {
						collapseOrExpand(ClusterControlPlugin.IMG_COLLAPSE.equals(m_collapseExpandImageId));
					}

					@Override
					public void mouseReleased(MouseEvent arg0) {
					}
				});
			}
			m_baseLayer.add(layerStack);

			// 待ちアイコンを配置
			ImageFigure imageFigure = getWaitingIcon();
			if (imageFigure != null) {
				m_layerXY = new ScalableLayeredPane();
				m_layerXY.setLayoutManager(new XYLayout());
				m_layerXY.add(imageFigure);
				m_layerXY.setConstraint(imageFigure, zeroRectangle);
				layerStack.add(m_layerXY);
			}

			// ジョブID・ジョブ名
			Label label = new Label();
			if (this.m_controller.isLabelingId()) {
				label.setText(m_jobTreeItem.getData().getId());
			} else {
				label.setText(m_jobTreeItem.getData().getName());
			}
			label.setLabelAlignment(Label.CENTER);
			label.setSize(JobmapIconImageUtil.ICON_WIDTH, SizeConstant.SIZE_TEXT_HEIGHT);
			label.setBorder(new MarginBorder(0, 4, 0, 4));
			
			m_baseLayer.add(label);


			// 色を設定
			setBgColor();

			// ツールチップを生成
			this.setToolTip(getTooltip());

			this.add(m_baseLayer);
			this.setConstraint(m_baseLayer, zeroRectangle);
			this.setMaximumSize(m_baseLayer.getPreferredSize());
		} else {
			// ジョブネットの場合は利用するレイヤーは1つ。
			m_layerXY = new ScalableLayeredPane();
			m_layerXY.setLayoutManager(new XYLayout());

			// 丸い四角。
			m_background = new GRoundedRectangle();
			m_layerXY.add(m_background);
			
			Layer layerTitle = new Layer();
			BorderLayout borderLayout = new BorderLayout();
			layerTitle.setLayoutManager(borderLayout);
			
			// 名前とID
			Label label = new Label();
			if (this.m_controller.isLabelingId()) {
				label.setText(m_jobTreeItem.getData().getId());
			} else {
				label.setText(m_jobTreeItem.getData().getName());
			}
			label.setForegroundColor(JobMapColor.darkgray);
			label.setFont(jobNetFont);
			label.setSize(this.m_controller.getTextWidth() - 8, textHeight + 8);
			label.setBorder(new MarginBorder(0, 4, 0, 4));
			
			layerTitle.add(label, BorderLayout.CENTER);
			
			if (m_collapse) {
				m_collapseExpandImageId = ClusterControlPlugin.IMG_EXPAND;
			} else {
				m_collapseExpandImageId = ClusterControlPlugin.IMG_COLLAPSE;
			}
			m_collapseExpandImageFigure = new ImageFigure(ClusterControlPlugin.getDefault().getImageRegistry().get(m_collapseExpandImageId));
			layerTitle.add(m_collapseExpandImageFigure, BorderLayout.RIGHT);
			m_collapseExpandImageFigure.addMouseListener(new MouseListener() {

				@Override
				public void mouseDoubleClicked(MouseEvent arg0) {
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					collapseOrExpand(ClusterControlPlugin.IMG_COLLAPSE.equals(m_collapseExpandImageId));
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
				}
			});

			m_background.add(layerTitle);

			// 待ちアイコンを配置
			ImageFigure waitingIcon = getWaitingIcon();
			if (waitingIcon != null) {
				m_log.debug("wait icon");
				m_layerXY.add(waitingIcon);
				m_layerXY.setConstraint(waitingIcon, zeroRectangle);
			}

			// ジョブキューアイコンを配置
			ImageFigure queueIcon = getQueueIcon();
			if (queueIcon != null) {
				// 待ち条件アイコンの有無で表示位置調整
				Point leftTop = (waitingIcon == null) ? new Point(0, 0) : new Point(16, 0);
				m_layerXY.add(queueIcon);
				m_layerXY.setConstraint(queueIcon, new Rectangle(leftTop, new Dimension(-1, -1)));
			}
			
			// 色を設定
			setBgColor();

			// ツールチップを生成
			this.setToolTip(getTooltip());

			this.add(m_layerXY);
		}
	}

	private void collapseOrExpand(boolean collapse) {
		if (collapse) {
			m_jobMapComposite.addCollapseItem(m_jobTreeItem);
		} else {
			m_jobMapComposite.removeCollapseItem(m_jobTreeItem);
		}
		m_jobMapComposite.update();
	}

	private boolean isIconImageJob() {
		return (this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET
				&& this.m_collapse)
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOBNET
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.FILEJOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.APPROVALJOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.MONITORJOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB
				|| this.m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.RPAJOB;
	}

	private ImageFigure getWaitingIcon() {
		JobWaitRuleInfoResponse waitRule = m_jobTreeItem.getData().getWaitRule();
		if (waitRule == null) {
			return null;
		}

		List<JobObjectGroupInfoResponse> listGroup = waitRule.getObjectGroup();
		if(listGroup == null || listGroup.isEmpty()){
			return null;
		}
		boolean isTimeWaiting =false;
		boolean isCrossingJobWaiting =false;
		String date = null;
		Integer minute = null;
		ArrayList<JobObjectInfoResponse> crossingJobList = new ArrayList<JobObjectInfoResponse>();
		//待ち条件 確認
		ImageFigure waitImageFigure ;
		for(JobObjectGroupInfoResponse waitGroup : listGroup){
			if(waitGroup.getJobObjectList().size() > 1){
				if (waitGroupImage == null) {
					waitGroupImage = ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_WAIT_GROUP);
				}
				waitImageFigure = new ImageFigure(waitGroupImage);
				return waitImageFigure;
			} else {
				List<JobObjectInfoResponse> list = waitGroup.getJobObjectList();
				for (JobObjectInfoResponse jobObjectInfo : list) {
					if (jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.TIME) {
						if (jobObjectInfo.getTime() != null) {
							date = jobObjectInfo.getTime();
							isTimeWaiting = true;
						}
					} else if (jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.START_MINUTE) {
						if (jobObjectInfo.getStartMinute() != null) {
							minute = jobObjectInfo.getStartMinute();
							isTimeWaiting = true;
						}
					} else if (jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_STATUS) {
						if (jobObjectInfo.getStatus() != null) {
							crossingJobList.add(jobObjectInfo);
							isCrossingJobWaiting = true;
						}
					} else if (jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_VALUE) {
						if (jobObjectInfo.getValue() != null) {
							crossingJobList.add(jobObjectInfo);
							isCrossingJobWaiting = true;
						}
					}
				}
			}
		}
		if (isTimeWaiting == false && isCrossingJobWaiting == false) {
			return null;
		}
		//アイコン割付
		if( isTimeWaiting && isCrossingJobWaiting){
			// 両方
			if (waitDoubleImage == null) {
				waitDoubleImage = ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_WAIT_DOUBLE);
			}
			waitImageFigure = new ImageFigure(waitDoubleImage);
		}else if(isTimeWaiting) {
			// 時計のみ
			if (waitImage == null) {
				waitImage = ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_WAIT);
			}
			waitImageFigure = new ImageFigure(waitImage);
		}else{
			// 横断ジョブのみ
			if (waitCrossingJobImage == null) {
				waitCrossingJobImage = ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_WAIT_CROSS_JOB);
			}
			waitImageFigure = new ImageFigure(waitCrossingJobImage);
		}
		//ツールチップメッセージ作成（RAPでの初回描画のみ、表示ラベル横幅不足となるケースあり。調整用空白パディングを設定）
		ArrayList<String> messageArray = new ArrayList<String>();
		if (date != null) {
			messageArray.add(Messages.getString("timestamp") + ":" + date + "   ");
		}
		if (minute != null) {
			messageArray.add((Messages.getString("time.after.session.start") + ":" + minute) + "   ");
		}
		for ( JobObjectInfoResponse targetWait :  crossingJobList){
			StringBuilder message = new StringBuilder() ;
			//セッション横断条件名称
			message.append(Messages.getString("wait.rule.cross.session")+ " " + Messages.getString("wait.rule") +":"+ "   ");
			//セッション横断ジョブ依存関係
			message.append( "\n" + targetWait.getJobId() +"->" + m_jobTreeItem.getData().getId());
			//セッション横断条件値
			if (targetWait.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_STATUS) {
				message.append("\n" + Messages.getString("end.status")  + "," + EndStatusMessage.typeEnumValueToString(targetWait.getStatus().getValue()));
			}
			else {
				message.append("\n" + Messages.getString("end.value")  + "," + targetWait.getValue());
			}
			//セッション横断待ち範囲
			message.append("\n" +  Messages.getString("wait.rule.cross.session.range") +","+ targetWait.getCrossSessionRange()+ "   ");
			messageArray.add(message.toString());
		}
		//ツールチップレイアウト編集
		Panel tooltip = new Panel();
		tooltip.setLayoutManager(new FlowLayout(false));
		for (String addMessage : messageArray){
			Panel subPanel = new Panel();
			subPanel.setLayoutManager(new FlowLayout(true));
			subPanel.add(new Label(addMessage));
			tooltip.add(subPanel);
		}
		waitImageFigure.setToolTip(tooltip);
		return waitImageFigure;
	}

	private ImageFigure getQueueIcon() {
		JobWaitRuleInfoResponse waitRule = m_jobTreeItem.getData().getWaitRule();
		if (waitRule == null) return null;

		Boolean queueFlg = waitRule.getQueueFlg();
		if (queueFlg == null || !queueFlg.booleanValue()) return null;

		// アイコンイメージ
		ImageFigure figure = new ImageFigure(
				ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_QUEUE));

		// ツールチップ
		ArrayList<String> messages = new ArrayList<String>();
		messages.add(Messages.get("jobqueue.id") + ":" + waitRule.getQueueId());

		Panel tooltip = new Panel();
		tooltip.setLayoutManager(new FlowLayout(false));
		for (String message : messages){
			Panel subPanel = new Panel();
			subPanel.setLayoutManager(new FlowLayout(true));
			subPanel.add(new Label(message));
			tooltip.add(subPanel);
		}
		figure.setToolTip(tooltip);

		return figure;
	}
	
	public Dimension getBackgroundSize() {
		if (isIconImageJob()) {
			return m_baseLayer.getSize();
		} else {
			return m_background.getSize();
		}
	}
	
	public void adjustBackgroundSize() {
		if (isIconImageJob()) {
			return;
		}

		m_layerXY.setConstraint(m_background, zeroRectangle);
		calculateSize();
		m_background.setSize(m_size.width, m_size.height);
		if (!m_background.getChildren().isEmpty()) {
			//タイトルのサイズ設定
			((Layer)m_background.getChildren().get(0)).setSize(m_size.width - 6, textHeight);
		}
	}
	
	private void calculateSize() {
		if (m_collapse) {
			m_size.setSize(this.m_controller.getTextWidth(), textHeight);
			return;
		}
		
		int width = 0;
		int height = 0;
		for (Object child : m_layerXY.getChildren()) {
			if (!(child instanceof JobFigure)) {
				continue;
			}

			JobFigure childJobFigure = (JobFigure) child;
			Dimension childJobFigureSize = childJobFigure.getBackgroundSize();
			Point childJobFigurePos = childJobFigure.getPosition();
			width = Math.max(width, childJobFigurePos.x + childJobFigureSize.width);
			height = Math.max(height, childJobFigurePos.y + childJobFigureSize.height);
		}
		
		if (height == 0) {
			height = textHeight;
		} else {
			height +=  jobnetBorder;
		}

		if (width < this.m_controller.getTextWidth()) {
			width = this.m_controller.getTextWidth();
		} else {
			width +=  jobnetBorder;
		}
		
		m_size.setSize(width, height);
	}
	
	public Point getPosition() {
		return m_position;
	}

	public void setPosition(Point position) {
		m_position = position;
	}
	
	public void setBgColor(){
		if(m_background == null) {
			m_log.debug("setPriority : m_backGround is null");
			return;
		}

		Color backColor = null;
		Color foreColor = null;
		JobDetailInfoResponse detail = m_jobTreeItem.getDetail();
		JobDetailInfoResponse.EndStatusEnum endStatus = null;
		JobDetailInfoResponse.StatusEnum status = null;
		if (detail != null) {
			endStatus = detail.getEndStatus();
			status = detail.getStatus();
		}
		backColor = JobMapColor.lightgray;
		if (endStatus != null) {
			switch ( endStatus ) {
			case NORMAL:
				backColor = JobMapColor.green;
				break;
			case ABNORMAL:
				backColor = JobMapColor.red;
				break;
			case WARNING:
				backColor = JobMapColor.yellow;
				break;
			default:
				break;
			}
		} else if (status != null) {
			switch (status) {
			case RESERVING: // 保留
				backColor = JobMapColor.yellow;
				break;
			case SKIP: // スキップ
				backColor = JobMapColor.yellow;
				break;
			case RUNNING: // 実行中
				backColor = JobMapColor.blue;
				break;
			case STOPPING: // 停止処理中
				backColor = JobMapColor.blue;
				break;
			case SUSPEND: // 中断
				backColor = JobMapColor.yellow;
				break;
			case STOP: // コマンド停止
				backColor = JobMapColor.red;
				break;
			case MODIFIED: // 変更済み
				backColor = JobMapColor.green;
				break;
			case END: // 終了
				backColor = JobMapColor.green;
				break;
			case ERROR:
				backColor = JobMapColor.red;
				break;
			case WAIT:
				backColor = JobMapColor.lightgray;
				break;
			case RUNNING_QUEUE: // 実行中(キュー待機)
				backColor = JobMapColor.blue;
				break;
			case SUSPEND_QUEUE: // 中断(キュー待機)
				backColor = JobMapColor.yellow;
				break;
			default:
				break;
			}
		}

		// 枠の色は中身の色を少し濃くした色とする。
		foreColor = new Color(null,
				backColor.getRed() * 3 / 4,
				backColor.getGreen() * 3 / 4,
				backColor.getBlue() * 3 / 4);

		/*
		 * 枠は細く。
		 * 中身はpriority色。
		 */
		m_background.setLineWidth(lineWidth);
		m_background.setForegroundColor(foreColor); // ここは利用されないが、一応設定しておく。
		m_background.setDownColor(foreColor);
		m_background.setBackgroundColor(backColor); // 枠の色。
	}

	@Override
	public void repaint(){
		super.repaint();
	}

	@Override
	public void handleFocusGained(FocusEvent fe){
		m_log.debug("handleFocusGained " + fe +", jobId="+m_jobTreeItem.getData().getId());

		if (m_editorView != null) {
			m_log.debug("handleFocusGained Call setEnabledAction ");
			//ビューのアクションの有効/無効を設定 
			m_editorView.setEnabledAction(m_jobTreeItem);
		}

		// ラベルを描画
		setFocus(true);
	}

	@Override
	public void handleFocusLost(FocusEvent fe){
		m_log.debug("handleFocusLost " + fe +", jobId="+m_jobTreeItem.getData().getId());
		if (m_editorView != null) {
			m_editorView.setEnabledActionAll(false);
		}
		setFocus(false);
	}

	/**
	 *  ツールチップに内容を設定
	 * @return
	 */
	private Panel getTooltip(){
		Panel tooltip = new Panel();
		tooltip.setLayoutManager(new FlowLayout(false));

		tooltip.add(new Label(m_jobTreeItem.getData().getName() + " (" +
				m_jobTreeItem.getData().getId() + ")"));
		if (m_jobTreeItem.getData().getDescription() != null
				&& !m_jobTreeItem.getData().getDescription().equals("")) {
			tooltip.add(new Label(m_jobTreeItem.getData().getDescription()));
		}
		
		JobDetailInfoResponse detail = m_jobTreeItem.getDetail();
		// ジョブマップ[設定]の場合は実行状態等はなし。
		if (detail == null) {
			if( m_jobTreeItem.getData().getWaitRule() != null ){
				//ループジョブ設定があればツールチップに設定
				if(m_jobTreeItem.getData().getWaitRule().getJobRetryFlg()){
					StringBuilder messageBuilder = new StringBuilder();
					messageBuilder.append (Messages.getString("job.retry.count") + ":" + m_jobTreeItem.getData().getWaitRule().getJobRetry());
					if( m_jobTreeItem.getData().getWaitRule().getJobRetryEndStatus() != null ){
						messageBuilder.append("\n");
						messageBuilder.append(Messages.getString("job.retry.end.status")  + ":" +  EndStatusMessage.typeEnumValueToString(m_jobTreeItem.getData().getWaitRule().getJobRetryEndStatus().getValue()));
					}
					tooltip.add(new Label(messageBuilder.toString()));
				}
				//保留設定があればツールチップに設定
				if(m_jobTreeItem.getData().getWaitRule().getSuspend()){
					tooltip.add(new Label(Messages.getString("reserve") ));
				}
				//スキップ設定があればツールチップに設定
				if(m_jobTreeItem.getData().getWaitRule().getSkip()){
					StringBuilder messageBuilder = new StringBuilder();
					messageBuilder.append(Messages.getString("skip"));
					if( m_jobTreeItem.getData().getWaitRule().getSkipEndStatus() != null ){
						messageBuilder.append("\n");
						messageBuilder.append(Messages.getString("end.status")  + ":" +  EndStatusMessage.typeEnumValueToString(m_jobTreeItem.getData().getWaitRule().getSkipEndStatus().getValue()));
					}
					if( m_jobTreeItem.getData().getWaitRule().getSkipEndValue() != null ){
						messageBuilder.append("\n");
						messageBuilder.append(Messages.getString("end.value")  + ":" +  m_jobTreeItem.getData().getWaitRule().getSkipEndValue());
					}
					tooltip.add(new Label( messageBuilder.toString()));
				}
			}
			return tooltip;
		}

		// 実行状態
		Panel subPanel = null;
		subPanel = new Panel();
		subPanel.setLayoutManager(new FlowLayout(true));
		subPanel.add(new Label(Messages.getString("run.status") + " : "));
		JobDetailInfoResponse.StatusEnum status = null;
		status = m_jobTreeItem.getDetail().getStatus();
		if (status != null) {
			subPanel.add(new ImageFigure(StatusImageConstant.typeEnumValueToImage(status.getValue())));
			
			subPanel.add(new Label(StatusMessage.typeEnumValueToString(status.getValue())));
		}
		tooltip.add(subPanel);

		// 終了状態
		subPanel = new Panel();
		subPanel.setLayoutManager(new FlowLayout(true));
		subPanel.add(new Label(Messages.getString("end.status") + " : "));
		JobDetailInfoResponse.EndStatusEnum endStatus = null;
		endStatus = m_jobTreeItem.getDetail().getEndStatus();
		if (endStatus != null) {
			subPanel.add(new ImageFigure(EndStatusImageConstant.typeEnumValueToImage(endStatus.getValue())));
			subPanel.add(new Label(EndStatusMessage.typeEnumValueToString(endStatus.getValue())));
		}
		tooltip.add(subPanel);

		// 終了値
		subPanel = new Panel();
		subPanel.setLayoutManager(new FlowLayout(true));
		subPanel.add(new Label(Messages.getString("end.value") + " : "));
		Integer endValue = null;
		endValue = m_jobTreeItem.getDetail().getEndValue();
		if (endValue != null) {
			subPanel.add(new Label(endValue.toString()));
		}
		tooltip.add(subPanel);

		// 開始・再実行時刻
		String dateStr = "";
		if (m_jobTreeItem.getDetail().getStartDate() != null) {
			dateStr = m_jobTreeItem.getDetail().getStartDate();
		}
		tooltip.add(new Label(Messages.getString("start.rerun.time") + " : " + dateStr));

		// 終了・中断時刻
		dateStr = "";
		if (m_jobTreeItem.getDetail().getEndDate() != null) {
			dateStr = m_jobTreeItem.getDetail().getEndDate();
		}
		tooltip.add(new Label(Messages.getString("end.suspend.time") + " : " + dateStr));

		//ループジョブ設定があればツールチップに実行回数を設定
		if( m_jobTreeItem.getData().getWaitRule() != null && m_jobTreeItem.getData().getWaitRule().getJobRetryFlg() && m_jobTreeItem.getDetail().getRunCount() != null ){
			tooltip.add(new Label(Messages.getString("job.run.count") + " : " + m_jobTreeItem.getDetail().getRunCount()));
		}

		return tooltip;
	}

	/**
	 * フォーカスにより表示を変更します
	 */
	private void setFocus(boolean focus){

		JobFigure focusFigure = m_jobMapComposite.getFocusFigure();
		if(m_log.isDebugEnabled()){
			m_log.debug("setFocus : focus=" +focus + " focusFigure="+focusFigure.getJobTreeItem().getData().getId());
		}
		// フォーカスされている場合
		if(focus == true && this.equals(focusFigure)){
			m_background.setLineWidth(lineWidth * 2);
			m_jobMapComposite.emphasisConnection(focusFigure.getJobTreeItem().getData().getId());
		} else {
			m_background.setLineWidth(lineWidth);
			m_jobMapComposite.emphasisConnection("");
		}
		
	}

	public GRoundedRectangle getBackground() {
		return m_background;
	}

	public ScalableLayeredPane getLayer() {
		return m_layerXY;
	}

	public JobTreeItemWrapper getJobTreeItem() {
		return m_jobTreeItem;
	}
	
	public boolean isLockedJob() {
		JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(m_jobTreeItem));
		return editState.isLockedJobunitId(m_jobTreeItem.getData().getJobunitId());
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}
