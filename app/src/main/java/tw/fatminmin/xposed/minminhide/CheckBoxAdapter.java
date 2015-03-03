package tw.fatminmin.xposed.minminhide;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
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
	
	public CheckBoxAdapter(Context context, List<Map<String, Object>> itemList) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mItemList = itemList;
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
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
		
		title.setText(sTitle);
		icon.setImageDrawable(dIcon);
		
		if(pref.getBoolean(key, false)) {
		    checkBox.setChecked(true);
		}
		else {
		    checkBox.setChecked(false);
		}
		
		icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = mContext.getPackageManager().getLaunchIntentForPackage(key);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);
            }
        });

		
		checkBox.setOnClickListener(new View.OnClickListener() {
			
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                
                boolean value = cb.isChecked();
                pref.edit()
                    .putBoolean(key, value)
                    .commit();
            }
		});
		
		return convertView;
	}
	
}
