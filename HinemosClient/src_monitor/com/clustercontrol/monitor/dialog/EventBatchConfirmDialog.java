/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.dialog;

import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.EventFilterConditionRequest;

import com.clustercontrol.bean.Property;
import com.clustercontrol.filtersetting.bean.EventFilterContext;
import com.clustercontrol.filtersetting.composite.EventFilterComposite.PropertyConverter;
import com.clustercontrol.filtersetting.util.EventFilterHelper;
import com.clustercontrol.filtersetting.util.EventFilterHelper.PropertyConversionType;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.util.Messages;

/**
 * 監視[一括確認]ダイアログ
 */
public class EventBatchConfirmDialog extends EventFilterDialog {

	public EventBatchConfirmDialog(Shell parent, EventFilterContext context) {
		super(parent, context);
	}

	@Override
	protected String getTitle() {
		return Messages.getString("dialog.monitor.confirm.all");
	}

	private static class BatchConfirmPropertyConverter implements PropertyConverter {
		@Override
		public Property convertConditionToProperty(EventFilterConditionRequest cnd,
				MultiManagerEventDisplaySettingInfo eventDspSetting, String targetManagerName) {
			return EventFilterHelper.convertConditionToProperty(cnd, eventDspSetting, targetManagerName,
					PropertyConversionType.BATCH_CONFIRM);
		}

		@Override
		public EventFilterConditionRequest convertPropertyToCondition(Property property) {
			return EventFilterHelper.convertPropertyToCondition(property, PropertyConversionType.BATCH_CONFIRM);
		}
	}

	@Override
	protected PropertyConverter createPropertyConverter() {
		return new BatchConfirmPropertyConverter();
	}
}
