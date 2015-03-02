package com.duckduckgo.mobile.android.dialogs.menuDialogs;

import android.content.Context;

import com.duckduckgo.mobile.android.R;
import com.duckduckgo.mobile.android.adapters.menuAdapters.HistorySearchMenuAdapter;
import com.duckduckgo.mobile.android.objects.history.HistoryObject;
import com.duckduckgo.mobile.android.util.builders.OptionsDialogBuilder;


/*
Shows a dialog to alert the user the feedrequest failed, asking him to try again.
 */
public final class HistorySearchMenuDialog extends OptionsDialogBuilder {
	public HistorySearchMenuDialog(final Context context, HistoryObject historyObject) {
        super(context, R.string.SearchOptionsTitle);
        setContextAdapter(new HistorySearchMenuAdapter(context, R.layout.temp_dialog_item/*android.R.layout.select_dialog_item*/, android.R.id.text1, historyObject));
	}
}