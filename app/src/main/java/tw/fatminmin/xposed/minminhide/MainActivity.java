package tw.fatminmin.xposed.minminhide;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private SharedPreferences pref;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = getSharedPreferences(getPackageName() + "_preferences", MODE_WORLD_READABLE);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
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
    public static class PlaceholderFragment extends Fragment {

        private ListView listView;
        private CheckBoxAdapter mAdapter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            listView = (ListView) rootView.findViewById(R.id.mListView);
            mAdapter = new CheckBoxAdapter(getActivity(), getAppList());
            listView.setAdapter(mAdapter);

            return rootView;
        }

        private List<Map<String, Object>> getAppList() {

            Context activity = getActivity();

            PackageManager pm = activity.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);


            List<Map<String, Object>> itemList = new ArrayList<>();
            for(ApplicationInfo info : apps) {

                if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

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
    }
}
