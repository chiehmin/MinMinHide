package tw.fatminmin.xposed.minminhide;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    static public SharedPreferences pref;
    private Handler handler = new Handler();
    private PlaceholderFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = getSharedPreferences(getPackageName() + "_preferences", MODE_WORLD_READABLE);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            mFragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_restart_launcher) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    restartLauncher();
                }
            });
            return true;
        }
        else if(id == R.id.action_show_system_app) {

            boolean value = !pref.getBoolean(Common.KEY_SHOW_SYSTEM_APP, false);

            pref.edit()
                .putBoolean(Common.KEY_SHOW_SYSTEM_APP, value)
                .commit();
            mFragment.refresh(value);
        }

        return super.onOptionsItemSelected(item);
    }

    private void restartLauncher()
    {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        for (RunningAppProcessInfo process : processes) {
            if(!process.processName.equals(MainActivity.class.getPackage().getName())) {
                am.killBackgroundProcesses(process.processName);
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
                                            implements AdapterView.OnItemClickListener {

        private ListView listView;
        private MainAdapter mAdapter;
        private LayoutInflater mInflater;


        public PlaceholderFragment() {
        }

        private void refresh(boolean showSystemApp) {
            mAdapter = new MainAdapter(getActivity(), getAppList(showSystemApp));
            listView.setAdapter(mAdapter);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            mInflater = inflater;

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            listView = (ListView) rootView.findViewById(R.id.mListView);
            listView.setOnItemClickListener(this);

            refresh(pref.getBoolean(Common.KEY_SHOW_SYSTEM_APP, false));

            return rootView;
        }

        private List<Map<String, Object>> getAppList(boolean showSystemApps) {

            Context activity = getActivity();

            PackageManager pm = activity.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);


            List<Map<String, Object>> itemList = new ArrayList<>();
            for(ApplicationInfo info : apps) {

                if(showSystemApps || (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                    Map<String, Object> map = new HashMap<>();

                    map.put("title", pm.getApplicationLabel(info));
                    map.put("key", info.packageName);
                    map.put("icon", pm.getApplicationIcon(info));

                    itemList.add(map);
                }
            }

            Collections.sort(itemList, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                    String s1 = (String) lhs.get("title");
                    String s2 = (String) rhs.get("title");
                    return s1.compareToIgnoreCase(s2);
                }
            });

            return itemList;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final String pkgName = mAdapter.getKey(position);

            View subSettings = mInflater.inflate(R.layout.subsettings, null);
            CheckBox ckbHideFromSystem = (CheckBox) subSettings.findViewById(R.id.hide_from_system);
            ListView sub_listView = (ListView) subSettings.findViewById(R.id.subsettings_listview);
            CheckBoxAdapter sub_adapter = new CheckBoxAdapter(getActivity(), getAppList(false), pkgName);
            sub_listView.setAdapter(sub_adapter);

            final String pref_key = pkgName + Common.KEY_HIDE_FROM_SYSTEM;
            ckbHideFromSystem.setChecked(pref.getBoolean(pref_key, false));
            ckbHideFromSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    pref.edit()
                         .putBoolean(pref_key, isChecked)
                         .commit();
                }
            });

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_hide_app)
                    .setView(subSettings)
                    .show();

        }
    }
}
