package org.imogene.android.widget.field.edit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.imogene.android.Constants.Extras;
import org.imogene.android.common.entity.ImogBean;
import org.imogene.android.util.IntentUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import fr.medes.android.database.sqlite.stmt.Where;
import fr.medes.android.util.Arrays;

public class RelationManyFieldEdit extends RelationFieldEdit<List<Uri>> {

	public RelationManyFieldEdit(Context context, AttributeSet attrs, int layoutId) {
		super(context, attrs, layoutId);
	}

	public RelationManyFieldEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean isEmpty() {
		final List<Uri> list = getValue();
		return list == null || list.size() == 0;
	}

	@Override
	public boolean isValid() {
		if (isRequired()) {
			final List<Uri> value = getValue();
			return value != null && !value.isEmpty();
		}
		return true;
	}

	@Override
	public String getFieldDisplay() {
		final List<Uri> uris = getValue();
		if (uris != null && !uris.isEmpty()) {
			int size = uris.size();
			String fmt = getResources().getString(mDisplayRes);
			return MessageFormat.format(fmt, size);
		} else {
			return getEmptyText();
		}
	}

	@Override
	protected void dispatchClick(View v) {
		if (isReadOnly()) {
			final List<Uri> list = getValue();

			if (list == null || list.size() == 0) {
				return;
			}

			final int size = list.size();
			if (size == 1) {
				startActivity(new Intent(Intent.ACTION_VIEW, list.get(0)));
			} else {
				Intent intent = new Intent(Intent.ACTION_VIEW, mContentUri);
				long[] ids = new long[size];
				for (int i = 0; i < size; i++) {
					ids[i] = Long.parseLong(list.get(i).getLastPathSegment());
				}
				Where where = new Where();
				where.in(ImogBean.Columns._ID, ids);
				IntentUtils.putWhereExtras(intent, where);
				startActivity(intent);
			}
			return;
		}
		super.dispatchClick(v);
	}

	@Override
	protected void onPrepareIntent(Intent intent) {
		final List<Uri> list = getValue();
		if (list != null && !list.isEmpty()) {
			intent.putParcelableArrayListExtra(Extras.EXTRA_SELECTED, Arrays.asArrayList(list));
		}
		intent.putExtra(Extras.EXTRA_MULTIPLE, true);
	}

	@Override
	protected Where onPrepareWhere() {
		if (mHasReverse && mOppositeCardinality == 1) {
			return new Where().eq(mOppositeRelationField, getFieldManager().getId()).or()
					.isNull(mOppositeRelationField);
		}
		return null;
	}

	@Override
	public Where onCreateConstraint(String column) {
		final List<Uri> uris = getValue();
		if (uris != null && uris.size() > 0) {
			Object[] ids = new Object[uris.size()];
			for (int i = 0; i < uris.size(); i++) {
				ids[i] = uris.get(i).getLastPathSegment();
			}
			return new Where().in(column, ids);
		}
		showToastUnset();
		return null;
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == mRequestCode && resultCode != Activity.RESULT_CANCELED) {
			final ArrayList<Uri> result = data.getParcelableArrayListExtra(Extras.EXTRA_SELECTED);
			setValue(result);
			return true;
		}
		return false;
	}

}
