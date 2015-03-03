package tw.fatminmin.xposed.minminhide;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.util.Log;

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

    private String getCallingName(Object thiz)
    {
        int uid = Binder.getCallingUid();
        String packageName = (String) XposedHelpers.callMethod(thiz, "getNameForUid", uid);

        return packageName;
    }

    private boolean shouldBlock(Object thiz, String callingName, String queryName)
    {
        String key = callingName + ":" + queryName;
        String key_hide_from_system = queryName + Common.KEY_HIDE_FROM_SYSTEM;

        if(pref.getBoolean(key, false))
        {
            return true;
        }
        if(pref.getBoolean(key_hide_from_system, false))
        {

            // block system processes like android.uid.systemui:10015
            if(callingName.contains(":")) {
                return true;
            }

            // public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId)
            // need to bypass enforceCrossUserPermission
            ApplicationInfo info = (ApplicationInfo) XposedHelpers.callMethod(thiz, "ApplicationInfo", callingName,
                    0, Binder.getCallingUid());
            if((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
        }

        return false;
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

                    pref.reload();

                    // android.content.pm.ParceledListSlice
                    Object pList = param.getResult();


                    List<ApplicationInfo> mList = (List<ApplicationInfo>) XposedHelpers.getObjectField(pList, "mList");
                    List<ApplicationInfo> result = new ArrayList<>();


                    for (ApplicationInfo info : mList)
                    {
                        if (shouldBlock(param.thisObject, getCallingName(param.thisObject), info.packageName))
                        {
                            continue;
                        }
                        result.add(info);
                    }

                    XposedHelpers.setObjectField(pList, "mList", result);

                }
            });
            XposedBridge.hookAllMethods(clsPMS, "getInstalledPackages", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    pref.reload();

                    // android.content.pm.ParceledListSlice
                    Object pList = param.getResult();

                    List<PackageInfo> mList = (List<PackageInfo>) XposedHelpers.getObjectField(pList, "mList");
                    List<PackageInfo> result = new ArrayList<>();

                    for (PackageInfo info : mList)
                    {
                        if (shouldBlock(param.thisObject, getCallingName(param.thisObject), info.packageName))
                        {
                            continue;
                        }
                        result.add(info);
                    }

                    XposedHelpers.setObjectField(pList, "mList", result);

                }
            });
            XposedBridge.hookAllMethods(clsPMS, "queryIntentActivities", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    pref.reload();

                    List<ResolveInfo> mList = (List<ResolveInfo>) param.getResult();
                    List<ResolveInfo> result = new ArrayList<>();

                    for(ResolveInfo info : mList)
                    {
                        if (shouldBlock(param.thisObject, getCallingName(param.thisObject), info.activityInfo.packageName))
                        {
                            continue;
                        }
                        result.add(info);

                    }

                    param.setResult(result);
                }
            });

            XposedBridge.hookAllMethods(clsPMS, "queryIntentActivityOptions", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    pref.reload();

                    List<ResolveInfo> mList = (List<ResolveInfo>) param.getResult();
                    List<ResolveInfo> result = new ArrayList<>();

                    for(ResolveInfo info : mList)
                    {
                        if (shouldBlock(param.thisObject, getCallingName(param.thisObject), info.activityInfo.packageName))
                        {
                            continue;
                        }
                        result.add(info);

                    }

                    param.setResult(result);
                }
            });
        }

    }


}
