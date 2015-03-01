package tw.fatminmin.xposed.minminhide;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    public static final String MY_PACKAGE_NAME = Main.class.getPackage().getName();
    public static XSharedPreferences pref;

    final static String PMS = "com.android.server.pm.PackageManagerService";

    private String getCallingName(Object obj)
    {
        int uid = Binder.getCallingUid();
        String packageName = (String) XposedHelpers.callMethod(obj, "getNameForUid", uid);

        return packageName;
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        pref = new XSharedPreferences(MY_PACKAGE_NAME);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable
    {


        if(lpparam.packageName.equals("android"))
        {
            Class<?> clsPMS = XposedHelpers.findClass(PMS, lpparam.classLoader);

            XposedBridge.hookAllMethods(clsPMS, "getInstalledApplications", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    if(getCallingName(param.thisObject).equals(MY_PACKAGE_NAME))
                        return;

                    pref.reload();

                    // android.content.pm.ParceledListSlice
                    Object pList = param.getResult();


                    List<ApplicationInfo> mList = (List<ApplicationInfo>) XposedHelpers.getObjectField(pList, "mList");
                    List<ApplicationInfo> result = new ArrayList<>();


                    for (ApplicationInfo info : mList)
                    {
                        if (!pref.getBoolean(info.packageName, false))
                        {
                            result.add(info);
                        }
                    }

                    XposedHelpers.setObjectField(pList, "mList", result);

                }
            });
            XposedBridge.hookAllMethods(clsPMS, "getInstalledPackages", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    if(getCallingName(param.thisObject).equals(MY_PACKAGE_NAME))
                        return;

                    pref.reload();

                    // android.content.pm.ParceledListSlice
                    Object pList = param.getResult();

                    List<PackageInfo> mList = (List<PackageInfo>) XposedHelpers.getObjectField(pList, "mList");
                    List<PackageInfo> result = new ArrayList<>();

                    for (PackageInfo info : mList)
                    {
                        if (!pref.getBoolean(info.packageName, false))
                        {
                            result.add(info);
                        }
                    }

                    XposedHelpers.setObjectField(pList, "mList", result);

                }
            });
            XposedBridge.hookAllMethods(clsPMS, "queryIntentActivities", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    if(getCallingName(param.thisObject).equals(MY_PACKAGE_NAME))
                        return;

                    pref.reload();

                    List<ResolveInfo> mList = (List<ResolveInfo>) param.getResult();
                    List<ResolveInfo> result = new ArrayList<>();

                    for(ResolveInfo info : mList)
                    {
                        if (!pref.getBoolean(info.activityInfo.packageName, false))
                        {
                            result.add(info);
                        }
                    }

                    param.setResult(result);
                }
            });
        }

    }


}
