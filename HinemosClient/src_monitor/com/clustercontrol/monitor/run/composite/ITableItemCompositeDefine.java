package com.clustercontrol.monitor.run.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.run.util.TableItemManager;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;

public interface ITableItemCompositeDefine<T> {
	static final int ADD = 1;
	static final int MODIFY = 2;
	static final int DELETE = 4;
	static final int COPY = 8;
	static final int UP = 16;
	static final int DOWN = 32;
	static final int SPACE = 64;
	static final int MULTI = 128;

	T getCurrentCreatedItem();
	CommonDialog createDialog(Shell shell);
	CommonDialog createDialog(Shell shell, T item);
	ArrayList<?> getTableDefine();
	TableItemManager<T> getTableItemInfoManager();
	void initTableItemInfoManager();
	void initTableItemInfoManager(List<T> items);

	int getButtonOptions();

	String getItemsIdentifier(T item);

	CommonTableLabelProvider<T> getLabelProvider();
	int indexOf(T item);
}
