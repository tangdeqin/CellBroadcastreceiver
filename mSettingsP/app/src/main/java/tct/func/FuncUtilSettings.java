package tct.func;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.android.settings.R;
import android.os.Build;
import android.os.SystemProperties;

import tct.func.util.XMLParserUtils;

public class FuncUtilSettings {

    public static final String TAG = "FuncUtilSettings";


    public static final int TORCH_ID = 0;
    public static final int START_SOUND_RECORD_ID = 1;
    public static final int SET_TIMER_ID = 2;
    public static final int CALCULATOR_ID = 3;
    public static final int GOOGLE_VOICE_SEARCH_ID = 4;
    public static final int COMPOSE_MESSAGE_ID = 5;
    public static final int COMPOSE_EMAIL_ID = 6;
    public static final int CALL_A_CONTACT_ID = 7;
    public static final int ADD_CONTACT_ID = 8;
    public static final int ADD_EVENT_ID = 9;
    public static final int NAVIGATE_HOME_ID = 10;
    public static final int SET_ALARM_ID = 11;
    public static final int STOP_WATCH = 12;

    public static final int RECENT_CALLS_ID = 13;
    public static final int WALLSHUFFLE_SETTINGS_ID = 14;
    public static final int YAHOO_SEARCH_ID = 15;
    public static final int FUNC_SETTINGS_ID = 16;
    public static final int START_MUSIC_PLAYLIST_ID = 17;
    public static final int CAMERA_ID =18;
    public static final int SELFIE_ID = 19;
    //public static final int RECORD_A_MICRO_VIDEO = 19;

    public static int TOTAL_ITEMS = 13;

    public static final int LEN = 5;

    public static final int GOFuncAppsListActivity = 2;

    public static final String UNINSTALLACTION = "com.android.settings.func_uninstallappaction";

    public static List<FuncSettings.ShortcutsItem> mShortcutsItems = null;

    public static Drawable getIconById(Context context, int scId) {
        Drawable mDrawable = null;
        FuncSettings.ShortcutsItem shortcutsItem =  getShortcutsItemById(context, scId);
        if (shortcutsItem != null && shortcutsItem.getIconId() != -1) {
            mDrawable = context.getDrawable(shortcutsItem.getIconId());
        }

        return mDrawable;
    }

    public static String getNameById(Context context, int scId) {
        String mName = "";
        FuncSettings.ShortcutsItem shortcutsItem =  getShortcutsItemById(context, scId);
        if (shortcutsItem != null) {
            mName = shortcutsItem.getName();
        }

        return mName;
    }

    public static String getItemPkgName(Context context, int scId) {
        String pkgName = null;
        FuncSettings.ShortcutsItem shortcutsItem = null;
        shortcutsItem = getShortcutsItemById(context, scId);
        if (shortcutsItem != null) {
            pkgName = shortcutsItem.getPackageName();
        }

        return pkgName;
    }

    public static int[] DB2int(String s) {

        String[] sp = s.split(";");
        int[] num = new int[sp.length];
        for (int i = 0; i < sp.length; ++i) {
            num[i] = Integer.valueOf(sp[i]);
        }
        return num;
    }

    public static String int2DB(int[] num) {
        String s = "";
        for (int i = 0; i < num.length; ++i) {
            s += String.valueOf(num[i]) + ";";
        }
        String result = s.substring(0, s.length() - 1);
        return result;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getDisplayHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;

        return displayHeight;
    }

    // porting from defect 987432 begin
    public static Drawable getAppShortcutsDrawable(Context context,
            String packagename, String mainClassName) {
        Drawable drawable = null;
        try {
            ActivityInfo info = context.getPackageManager().getActivityInfo(
                    new ComponentName(packagename, mainClassName),
                    PackageManager.GET_META_DATA);
            String title = info.loadLabel(context.getPackageManager())
                    .toString();
            drawable = info.loadIcon(context.getPackageManager());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    public static String getAppShortcutsName(Context context,
            String packagename, String mainClassName) {
        String title = "";
        try {
            ActivityInfo info = context.getPackageManager().getActivityInfo(new ComponentName(packagename,mainClassName),
                    PackageManager.GET_META_DATA);
            title = info.loadLabel(context.getPackageManager()).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }
    // porting from defect 987432 end

    //Added by yongjun.zou for XR P10024995 on 2018/10/26 begin
    public static boolean whichFuncAppDeleteFromPlf(Context context, String name) {
        String appNameId = context.getResources().getString(R.string.def_func_which_app_delete);
        String[] appIds = null;

        if (!TextUtils.isEmpty(appNameId) && !"@".equals(appNameId)) {
            if (appNameId.contains("@")) {
                appIds = appNameId.split("@");
            } else {
                appIds = new String[]{appNameId};
            }
        }

        if (appIds == null) {
            return false;
        }

        try {
            for (int i = 0; i < appIds.length; i++) {
                if (name.equalsIgnoreCase(appIds[i])) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    //Added by yongjun.zou for XR P10024995 on 18/10/26 end

    //Added by yongjun.zou for defect 5859475 on 2018/01/13 begin
    public static List<FuncSettings.ShortcutsItem> removeDisableAppFromCloosedList(Context context, List<FuncSettings.ShortcutsItem> choosedList) {
        Iterator<FuncSettings.ShortcutsItem> iterator = choosedList.iterator();
        while (iterator.hasNext()) {
            FuncSettings.ShortcutsItem tempMap = iterator.next();
            int id = tempMap.getId();
            if ((id < 0 || id >= TOTAL_ITEMS) && !checkAPPEnable(context, (String)tempMap.getPackageName())) {
                iterator.remove();
                Log.d(TAG,"removeDisableAppFromCloosedList, id = " + id);
            }
        }

        return choosedList;
    }

    public static boolean checkAPPEnable(Context context, String packageName) {
        //Begin modified by yongjun.zou for XR P10024995 on 2018/10/26
        if ("".equals(packageName))
            return true;
        //End modified by yongjun.zou for XR P10024995 on 2018/10/26
        try {
            int appEnabled = context.getPackageManager()
                    .getApplicationEnabledSetting(packageName);
            if (appEnabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || appEnabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                Log.d(TAG, "checkAPPEnable [" + packageName + "] disable");
                return false;
            }
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "checkAPPEnable exception[" + packageName + "]");
            return false;
        }
        return true;
    }
    //Added by yongjun.zou for defect 5859475 on 2018/01/13 end

    public static String buildTotalListJson(Context context) {

        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < 12; i++) {
                JSONObject jsondataObj = new JSONObject();
                jsondataObj.put("id", i);
                /* modified for defect 1018427
                jsondataObj.put("name", getNameById(context, i));

                jsondataObj.put("name", ShortcutsUtil.PACKAGE_MAP_LIST[i]);
                */
                jsondataObj.put("name", getItemPkgName(context, i));
                if (i < 6) {
                    jsondataObj.put("selected", true);
                } else if (i == 6) {
                    jsondataObj.put("id", -1);
                    jsondataObj.put("selected", true);
                    jsondataObj.put("name", "com.tct.weather");
                } else {
                    jsondataObj.put("selected", false);
                }
                jsonArray.put(jsondataObj);
            }
            jsonObj.put("objlist", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String list = jsonObj.toString();
        return list;
    }

    public static List<Map<String, Object>> parseTotalListJson(String json) {
        List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
        try {
            JSONObject jsonObj = new JSONObject(json);
            JSONArray jsonArray = jsonObj.getJSONArray("objlist");
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                JSONObject jsondataObj = jsonArray.optJSONObject(i);
                int id = jsondataObj.getInt("id");
                String name = jsondataObj.getString("name");
                boolean selected = jsondataObj.getBoolean("selected");
                map.put("id", id);
                map.put("name", name);
                map.put("selected", selected);
                listData.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listData;
    }

    public static List<FuncSettings.ShortcutsItem> parseToChoosedLists(String json) {
        List<FuncSettings.ShortcutsItem> choosedLists = new ArrayList<FuncSettings.ShortcutsItem>();
        try {
            JSONObject jsonObj = new JSONObject(json);
            JSONArray jsonArray = jsonObj.getJSONArray("objlist");
            Log.d(TAG,"parseToChoosedLists,jsonArray.length()"+jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d(TAG,"parseToChoosedLists222");
                JSONObject jsondataObj = jsonArray.optJSONObject(i);
                Log.d(TAG,"parseToChoosedLists333,toString:"+jsondataObj.toString());
                int id = jsondataObj.getInt("id");
                String name = jsondataObj.getString("name");
                String mainclassName = jsondataObj.getString("mainclassName");
                String packageName = jsondataObj.getString("packageName");
                boolean selected = jsondataObj.getBoolean("selected");
                FuncSettings.ShortcutsItem item = new FuncSettings.ShortcutsItem();
                item.setId(id);
                item.setMainClassName(mainclassName);
                item.setPackageName(packageName);
                item.setName(name);
                item.setSlected(selected);
                if (selected) {
                    choosedLists.add(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG,"parseToChoosedLists exception:"+e);
        }
        return choosedLists;
    }

    public static List<FuncSettings.ShortcutsItem> parseToalternativeShortcutslists(
            String json) {
        List<FuncSettings.ShortcutsItem> alternativeShortcutslists = new ArrayList<FuncSettings.ShortcutsItem>();
        try {
            JSONObject jsonObj = new JSONObject(json);
            JSONArray jsonArray = jsonObj.getJSONArray("objlist");
            Log.d(TAG,"parseToalternativeShortcutslists,jsonArray.length()"+jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                JSONObject jsondataObj = jsonArray.optJSONObject(i);
                int id = jsondataObj.getInt("id");
                String name = jsondataObj.getString("name");
                String mainclassName = jsondataObj.getString("mainclassName");
                String packageName = jsondataObj.getString("packageName");
                boolean selected = jsondataObj.getBoolean("selected");
                FuncSettings.ShortcutsItem item = new FuncSettings.ShortcutsItem();
                item.setId(id);
                item.setName(name);
                item.setMainClassName(mainclassName);
                item.setPackageName(packageName);
                item.setSlected(selected);
                Log.d(TAG,"parseToalternativeShortcutslists22,id :"+id+
                ",selected :"+selected);

                if (!selected) {
                    alternativeShortcutslists.add(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return alternativeShortcutslists;
    }

    public static List<Map<String, String>> parseChoosedJsonListForApp(Context context){
        //String jsonStr = Settings.System.getString(context.getContentResolver(), "total_list");
        String jsonStr = FuncUtilSettings.readFromSettingsDatabase(context.getContentResolver(), FuncConstant.FUNC_TOTAL_LIST);
        List<Map<String, String>> mainClassList = new ArrayList<Map<String, String>>(); 
        Log.d(TAG, "jsonStr--->"+jsonStr);
       //Added by jinlong.lu for Defect 3379696 on 16-11-8 begin
        if(jsonStr ==null) return mainClassList;
       //Added by jinlong.lu for Defect 3379696 on 16-11-8 end
        try{
            JSONObject jsonObj = new JSONObject(jsonStr);
            if(null != jsonObj){
                JSONArray jsonArray = jsonObj.getJSONArray("objlist");
                if (jsonArray != null) { //Added by jinlong.lu for Defect 3379696 on 16-11-8 end
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonDataObj = jsonArray.getJSONObject(i);
                        int id = jsonDataObj.getInt("id");
                        if (id == -1) {
                            String pkgName = jsonDataObj.getString("packageName");
                            String className = jsonDataObj.getString("mainclassName");
                            Map<String, String> mainClass = new HashMap<String, String>();
                            mainClass.put("pkg", pkgName);
                            mainClass.put("class", className);
                            if (null != mainClass) {
                                mainClassList.add(mainClass);
                            }
                        }
                    }
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        Log.d(TAG,"mainClassList.size--->"+mainClassList.size());
        return mainClassList;
    }

    public static String saveToChoosedlist(Context context,
                                           List<FuncSettings.ShortcutsItem> totalLists) {
        JSONObject jsonObj = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < totalLists.size(); i++) {
                JSONObject jsondataObj = new JSONObject();
                int id = (int) totalLists.get(i).getId();
                String mainclassName = (String) totalLists.get(i).getMainClassName();
                jsondataObj.put("id", (int) totalLists.get(i).getId());
                jsondataObj.put("name", totalLists.get(i).getName());
                if (id != -1) {
                    jsondataObj.put("packageName", getItemPkgName(context, id));
                } else {
                    jsondataObj.put("packageName", totalLists.get(i).getPackageName());
                }
                jsondataObj.put("mainclassName", mainclassName);
                jsonArray.put(jsondataObj);
            }
            jsonObj.put("objlist", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String choosedlist = jsonObj.toString();
        return choosedlist;
    }

    public static String saveTotalListJson(Context context,
                                           List<FuncSettings.ShortcutsItem> totalLists) {
        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (FuncSettings.ShortcutsItem shortcutsItem : totalLists){
                JSONObject jsondataObj = new JSONObject();
                int id = shortcutsItem.getId();
                String itemName = shortcutsItem.getName();
                String mainclassName = shortcutsItem.getMainClassName();
                boolean selected = shortcutsItem.isSlected();

                jsondataObj.put("id", id);
                jsondataObj.put("mainclassName", mainclassName);
                jsondataObj.put("name",itemName);
                if (id != -1) {
                    jsondataObj.put("packageName", getItemPkgName(context, id));
                } else {
                    jsondataObj.put("packageName", shortcutsItem.getPackageName());
                }
                jsondataObj.put("selected", selected);
                jsonArray.put(jsondataObj);
            }
            jsonObj.put("objlist", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String list = jsonObj.toString();
        return list;
    }

    public static void saveInSettingsDatabase(ContentResolver cr,String dataName,String value){
        try {
            Settings.System.putString(cr, dataName,value);
        }catch (Exception e){
            e.printStackTrace();
            Log.i(TAG, "saveInSettingsDatabase,  exception: "+e.toString());
        }
        Log.i(TAG, "saveInSettingsDatabase,  dataName: "+dataName+",value :"+value);
        Log.i(TAG, "saveInSettingsDatabase, call stack: "+android.os.Debug.getCallers(5));
    }

    public static String readFromSettingsDatabase(ContentResolver cr,String dataName){
        String value = null;
        try {
            value = Settings.System.getString(cr, dataName);
        }catch (Exception e){
            e.printStackTrace();
            Log.i(TAG, "readFromSettingsDatabase,  exception: "+e.toString());
        }
        Log.i(TAG, "readFromSettingsDatabase,call stack: "+android.os.Debug.getCallers(5));
        return value;
    }

    public static FuncSettings.ShortcutsItem getShortcutsItemById(Context context, int scId) {
        FuncSettings.ShortcutsItem shortcutsItem = null;
        List<FuncSettings.ShortcutsItem> shortcutsItems = getDefaultShortcutsItem(context, false);
        if (shortcutsItems == null) return null;

        for (FuncSettings.ShortcutsItem shortcutsItem1 : shortcutsItems) {
            if (shortcutsItem1.getId() == scId) {
                return shortcutsItem1;
            }
        }
        return shortcutsItem;
    }



    /***************
     * get default shortcuts info
     *
     * @return ShortcutsItem list
     */
    public static List<FuncSettings.ShortcutsItem> getDefaultShortcutsItem(Context context, boolean forceInit) {
        if (context == null) { return null; }

        if (mShortcutsItems == null || forceInit == true) {
            mShortcutsItems = new ArrayList<FuncSettings.ShortcutsItem>();
            try {
                XmlResourceParser parser = null;
                final Resources res = context.getResources();
                parser = res.getXml(R.xml.tct_default_shortcuts_item);
                XMLParserUtils.beginDocument(parser, FuncConstant.DEFAULT_SHORTCUTS_DOC);
                final int depth = parser.getDepth();
                int type;
                int id = -1;
                String name = null;
                String icon = null;
                int nameId = -1;
                int iconId = -1;
                String mainClassName = null;
                String packageName = null;
                boolean selected = false;
                type = parser.getEventType();
                while (/*((type = parser.next()) != XmlPullParser.END_TAG || parser
                        .getDepth() > depth) &&*/ type != XmlPullParser.END_DOCUMENT) {
                    switch (type) {
                        case  XmlPullParser.START_TAG:
                            if (FuncConstant.DEFAULT_SHORTCUTS_SINGLE_ITEM.equals(parser.getName())
                                    || FuncConstant.DEFAULT_SHORTCUTS_MULTIPLE_ITEM.equals(parser.getName())) {
                                id = XMLParserUtils.getAttributeIntValue(parser, FuncConstant.DEFAULT_FUNCSHORTCUTS_NAME_SPACE, FuncConstant.ATTR_SHORTCUTS_ID);
                                name = XMLParserUtils.getAttributeValue(parser, FuncConstant.DEFAULT_FUNCSHORTCUTS_NAME_SPACE, FuncConstant.ATTR_SHORTCUTS_NAME);

                                if (whichFuncAppDeleteFromPlf(context, name)) {
                                    Log.d(TAG, "getDefaultShortcutsItem remove_by_plf id = " + id);
                                    type = parser.next();
                                    continue;
                                }

                                if (FuncConstant.DEFAULT_SHORTCUTS_SINGLE_ITEM.equals(parser.getName())) {
                                    packageName = XMLParserUtils.getAttributeValue(parser, FuncConstant.DEFAULT_FUNCSHORTCUTS_NAME_SPACE, FuncConstant.ATTR_SHORTCUTS_PACKAGENAME);
                                    if (!checkAPPEnable(context, packageName)) {
                                        Log.d(TAG, "getDefaultShortcutsItem remove id = " + id);
                                        packageName = null;
                                    }
                                } else if (FuncConstant.DEFAULT_SHORTCUTS_MULTIPLE_ITEM.equals(parser.getName())){
                                    int packageNameCount = XMLParserUtils.getAttributeIntValue(parser, FuncConstant.DEFAULT_FUNCSHORTCUTS_NAME_SPACE, FuncConstant.ATTR_SHORTCUTS_PACKAGENAME_COUNT);
                                    for(int i = 0; i < packageNameCount; i++) {
                                        String packageNameId = FuncConstant.ATTR_SHORTCUTS_PACKAGENAME + i;
                                        packageName = XMLParserUtils.getAttributeValue(parser, FuncConstant.DEFAULT_FUNCSHORTCUTS_NAME_SPACE, packageNameId);
                                        if (!checkAPPEnable(context, packageName)) {
                                            Log.d(TAG, "getDefaultShortcutsItem remove multiple id = " + id + "; packageName = " + packageName);
                                            packageName = null;
                                            continue;
                                        }
                                        if (checkAPPEnable(context, packageName)) {
                                            break;
                                        }
                                    }
                                }
                                icon = XMLParserUtils.getAttributeValue(parser, FuncConstant.DEFAULT_FUNCSHORTCUTS_NAME_SPACE, FuncConstant.ATTR_SHORTCUTS_ICON);
                                mainClassName = XMLParserUtils.getAttributeValue(parser, FuncConstant.DEFAULT_FUNCSHORTCUTS_NAME_SPACE, FuncConstant.ATTR_SHORTCUTS_MAINCLASS);
                                selected = XMLParserUtils.getAttributeBoolean(parser, FuncConstant.DEFAULT_FUNCSHORTCUTS_NAME_SPACE, FuncConstant.ATTR_SHORTCUTS_SELECTED);
//                                Log.d(TAG, "get  default shortcuts item from xml , id :" + id
//                                        + ", name: " + name + ", nameId: " + nameId
//                                        + ", iconId: " + iconId + ", mainClassName :" + mainClassName
//                                        + ", selected :" + selected);
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (FuncConstant.DEFAULT_SHORTCUTS_SINGLE_ITEM.equals(parser.getName())
                                    || FuncConstant.DEFAULT_SHORTCUTS_MULTIPLE_ITEM.equals(parser.getName())) {
                                if (packageName != null) {
                                    nameId = getResId(name);
                                    iconId = getResId(icon);
                                    name = getResString(context, name, nameId) ;
                                    mShortcutsItems.add(new FuncSettings.ShortcutsItem(id, name, iconId, mainClassName, packageName, selected));
                                }
                            }
                            break;
                        default:

                    }

                    type = parser.next();
                }
                return mShortcutsItems;
            } catch (XmlPullParserException e) {
                Log.w(TAG, "get xml info exception parsing default picture.", e);
                return null;
            } catch (IOException e) {
                Log.w(TAG, "et xml info exception parsing default picture.", e);
                return null;
            }
        }
        return mShortcutsItems;
    }

    private static int getResId(String res) {
        int resId = -1;
        if (res.contains("@")) {
            res = res.substring(1);//name.split("@")[1];
            resId = Integer.parseInt(res);
        }
        return resId;
    }

    private static String getResString(Context context, String name, int resId) {
        String string = name;
        if (resId != -1)
            string = context.getString(resId);
        return string;
    }
}