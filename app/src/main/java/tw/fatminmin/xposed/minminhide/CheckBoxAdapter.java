package tw.fatminmin.xposed.minminhide;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class CheckBoxAdapter extends BaseAdapter {
	

	private List<Map<String, Object>> mItemList;
	private Context mContext;
	private LayoutInflater mInflater;
	private SharedPreferences pref;
    private String currentPkgName;
	
	public CheckBoxAdapter(Context context, List<Map<String, Object>> itemList, String pkgName) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mItemList = itemList;
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        currentPkgName = pkgName;
	}
	
	@Override
	public int getCount() {
		return mItemList.size();
	}

	@Override
	public Object getItem(int position) {
		return mItemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mItemList.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.preference_checkbox, null);
		}
		
		final TextView title = (TextView) convertView.findViewById(R.id.title);
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.chkCheckBox);
		
		final String sTitle = (String) mItemList.get(position).get("title");
		final String key = (String) mItemList.get(position).get("key");
		final Drawable dIcon = (Drawable) mItemList.get(position).get("icon");

        final String pref_key = key + ":" + currentPkgName;
		
		title.setText(sTitle);
		icon.setImageDrawable(dIcon);
		
		if(pref.getBoolean(pref_key, false)) {
		    checkBox.setChecked(true);
		}
		else {
		    checkBox.setChecked(false);
		}

		
		checkBox.setOnClickListener(new View.OnClickListener() {
			
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                
                boolean value = cb.isChecked();
                pref.edit()
                    .putBoolean(pref_key, value)
                    .commit();
            }
		});
		
		return convertView;
	}
	
}
