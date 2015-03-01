package tw.fatminmin.xposed.minminhide;


import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookLoadPackage {


    final static String PMS = "com.android.server.pm.PackageManagerService";

    private String getCallingName(Object obj)
    {
        int uid = Binder.getCallingUid();
        String packageName = (String) XposedHelpers.callMethod(obj, "getNameForUid", uid);

        return packageName;
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

                    if(getCallingName(param.thisObject).equals("tw.fatminmin.xposed.minminguard"))
                        return;

                    // android.content.pm.ParceledListSlice
                    Object pList = param.getResult();


                    List<ApplicationInfo> mList = (List<ApplicationInfo>) XposedHelpers.getObjectField(pList, "mList");
                    List<ApplicationInfo> result = new ArrayList<>();


                    for (ApplicationInfo info : mList)
                    {
                        if (!info.packageName.equals("com.fc2.fc2video_ad"))
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

                    if(getCallingName(param.thisObject).equals("tw.fatminmin.xposed.minminguard"))
                        return;

                    // android.content.pm.ParceledListSlice
                    Object pList = param.getResult();

                    List<PackageInfo> mList = (List<PackageInfo>) XposedHelpers.getObjectField(pList, "mList");
                    List<PackageInfo> result = new ArrayList<>();

                    for (PackageInfo info : mList)
                    {
                        if (!info.packageName.equals("com.fc2.fc2video_ad"))
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

                    if(getCallingName(param.thisObject).equals("tw.fatminmin.xposed.minminguard"))
                        return;

                    List<ResolveInfo> mList = (List<ResolveInfo>) param.getResult();
                    List<ResolveInfo> result = new ArrayList<>();

                    for(ResolveInfo info : mList)
                    {
                        if (!info.activityInfo.packageName.equals("com.fc2.fc2video_ad"))
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
