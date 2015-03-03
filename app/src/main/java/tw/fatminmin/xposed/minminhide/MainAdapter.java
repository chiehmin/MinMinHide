package tw.fatminmin.xposed.minminhide;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class MainAdapter extends BaseAdapter {


    private List<Map<String, Object>> mItemList;
    private Context mContext;
    private LayoutInflater mInflater;

    public MainAdapter(Context context, List<Map<String, Object>> itemList) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mItemList = itemList;
    }

    public String getKey(int position)
    {
        return (String) mItemList.get(position).get("key");
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
            convertView = mInflater.inflate(R.layout.list_item, null);
        }

        final TextView title = (TextView) convertView.findViewById(R.id.main_title);
        final ImageView icon = (ImageView) convertView.findViewById(R.id.main_icon);

        final String sTitle = (String) mItemList.get(position).get("title");
        final String key = (String) mItemList.get(position).get("key");
        final Drawable dIcon = (Drawable) mItemList.get(position).get("icon");

        title.setText(sTitle);
        icon.setImageDrawable(dIcon);


        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = mContext.getPackageManager().getLaunchIntentForPackage(key);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);
            }
        });

        return convertView;
    }

}
