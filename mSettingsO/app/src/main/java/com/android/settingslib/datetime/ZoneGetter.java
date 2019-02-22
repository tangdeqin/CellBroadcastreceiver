/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.settingslib.datetime;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.icu.text.TimeZoneFormat;
import android.icu.text.TimeZoneNames;
import android.support.v4.text.BidiFormatter;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;

import com.android.settings.R;

import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
//Begin added by yeqing.lv for XR6619997 on 2018/07/25
import android.os.SystemProperties;
import java.util.Calendar;
import java.text.SimpleDateFormat;
//End added by yeqing.lv for XR6619997 on 2018/07/25

/**
 * ZoneGetter is the utility class to get time zone and zone list, and both of them have display
 * name in time zone. In this class, we will keep consistency about display names for all
 * the methods.
 *
 * The display name chosen for each zone entry depends on whether the zone is one associated
 * with the country of the user's chosen locale. For "local" zones we prefer the "long name"
 * (e.g. "Europe/London" -> "British Summer Time" for people in the UK). For "non-local"
 * zones we prefer the exemplar location (e.g. "Europe/London" -> "London" for English
 * speakers from outside the UK). This heuristic is based on the fact that people are
 * typically familiar with their local timezones and exemplar locations don't always match
 * modern-day expectations for people living in the country covered. Large countries like
 * China that mostly use a single timezone (olson id: "Asia/Shanghai") may not live near
 * "Shanghai" and prefer the long name over the exemplar location. The only time we don't
 * follow this policy for local zones is when Android supplies multiple olson IDs to choose
 * from and the use of a zone's long name leads to ambiguity. For example, at the time of
 * writing Android lists 5 olson ids for Australia which collapse to 2 different zone names
 * in winter but 4 different zone names in summer. The ambiguity leads to the users
 * selecting the wrong olson ids.
 *
 */
public class ZoneGetter {
    private static final String TAG = "ZoneGetter";

    public static final String KEY_ID = "id";  // value: String

    /**
     * @deprecated Use {@link #KEY_DISPLAY_LABEL} instead.
     */
    @Deprecated
    public static final String KEY_DISPLAYNAME = "name";  // value: String

    public static final String KEY_DISPLAY_LABEL = "display_label"; // value: CharSequence

    /**
     * @deprecated Use {@link #KEY_OFFSET_LABEL} instead.
     */
    @Deprecated
    public static final String KEY_GMT = "gmt";  // value: String
    public static final String KEY_OFFSET = "offset";  // value: int (Integer)
    public static final String KEY_OFFSET_LABEL = "offset_label";  // value: CharSequence

    private static final String XMLTAG_TIMEZONE = "timezone";
    
    private ZoneGetter() {}
    // Begin added by yeqing.lv for XR6619997 on 2018/07/25
 	private static final int HOURS_1 = 60 * 60000;
 	private static BidiFormatter mBidiFormatter = BidiFormatter.getInstance();

 	public static List<HashMap<String, Object>> getZones(Context context) {
 		final List<HashMap<String, Object>> myData = new ArrayList<HashMap<String, Object>>();
 		final long date = Calendar.getInstance().getTimeInMillis();
 		String[] ids = context.getResources().getStringArray(
 				R.array.timezone_values_new);
 		String[] labels = context.getResources().getStringArray(
 				R.array.timezone_label_new);
 		int shortlenth = ids.length;
 		if (ids.length != labels.length) {
 			shortlenth = ((ids.length > labels.length) ? labels.length
 					: ids.length);
 		}
 		for (int i = 0; i < shortlenth; i++) {
 			addItem(myData, ids[i], labels[i], date);
 		}
        //modify by yeqing.lv for XR7219033 on 2018-12-13 begin
 		//boolean isATT = SystemProperties.getBoolean("def_settings_add_timezone_for_ATT", true);
        boolean isATT = context.getResources().getBoolean(R.bool.def_settings_add_timezone_for_ATT);
        //modify by yeqing.lv for XR7219033 on 2018-12-13 end
 		if (isATT) {
 			String[] ATT_ids = context.getResources().getStringArray(
 					R.array.timezone_values_ATT);
 			String[] ATT_labels = context.getResources().getStringArray(
 					R.array.timezone_label_ATT);
 			int length = ATT_ids.length;
 			if (ATT_ids.length != ATT_labels.length) {
 				Log.wtf(TAG, "ids length and labels length are not equal.");
 				length = ((ATT_ids.length > ATT_labels.length) ? ATT_labels.length
 						: ATT_ids.length);
 			}
 			for (int i = 0; i < length; i++) {
 				addItem(myData, ATT_ids[i], ATT_labels[i], date);
 			}
 		}
 		return myData;
 	}

 	private static void addItem(List<HashMap<String, Object>> myData,
 			String id, String displayName, long date) {
 		final HashMap<String, Object> map = new HashMap<String, Object>();
 		map.put(KEY_ID, id);
 		map.put(KEY_DISPLAYNAME, mBidiFormatter.unicodeWrap(displayName));
 		map.put(KEY_DISPLAY_LABEL, mBidiFormatter.unicodeWrap(displayName));
 		final TimeZone tz = TimeZone.getTimeZone(id);
 		final int offset = tz.getOffset(date);
 		final int p = Math.abs(offset);
 		final StringBuilder name = new StringBuilder();
 		name.append("GMT");

 		if (offset < 0) {
 			name.append('-');
 		} else {
 			name.append('+');
 		}

 		name.append(p / (HOURS_1));
 		name.append(':');

 		int min = p / 60000;
 		min %= 60;

 		if (min < 10) {
 			name.append('0');
 		}
 		name.append(min);

 		map.put(KEY_GMT, mBidiFormatter.unicodeWrap(name.toString()));
 		map.put(KEY_OFFSET_LABEL, mBidiFormatter.unicodeWrap(name.toString()));
 		map.put(KEY_OFFSET, offset);

 		myData.add(map);
 	}
    //remove by yeqing.lv for XR7219033 on 2018-12-13
 	//private static boolean isRuTZ = SystemProperties.getBoolean("def_timezone_ru_flag", true);
 	private static final HashMap<String, String> mCityZoneMap = new HashMap<String, String>() {
 		{
 			put("Europe/Kaliningrad", "1");
 			put("Europe/Moscow", "2");
 			put("Europe/Simferopol", "2");
 			put("Europe/Volgograd", "2");
 			put("Europe/Samara", "3");
 			put("Asia/Yekaterinburg", "4");
 			put("Asia/Omsk", "5");
 			put("Asia/Novosibirsk", "6");
 			put("Asia/Krasnoyarsk", "6");
 			put("Asia/Irkutsk", "7");
 			put("Asia/Yakutsk", "8");
 			put("Asia/Chita", "8");
 			put("Asia/Vladivostok", "9");
 			put("Asia/Sakhalin", "10");
 			put("Asia/Magadan", "10");
 			put("Asia/Kamchatka", "11");
 			put("Asia/Anadyr", "11");
 		}
 	};

 	// End added by yeqing.lv for XR6619997 on 2018/07/25

    public static CharSequence getTimeZoneOffsetAndName(Context context, TimeZone tz, Date now) {
        Locale locale = context.getResources().getConfiguration().locale;
        TimeZoneFormat tzFormatter = TimeZoneFormat.getInstance(locale);
        CharSequence gmtText = getGmtOffsetText(tzFormatter, locale, tz, now);
        TimeZoneNames timeZoneNames = TimeZoneNames.getInstance(locale);
        String zoneNameString = getZoneLongName(timeZoneNames, tz, now);
        if (zoneNameString == null) {
            return gmtText;
        }

        // Begin modify by yeqing.lv for XR6619997 on 2018/07/25
     	// We don't use punctuation here to avoid having to worry about
     	// localizing that too!
     	//return TextUtils.concat(gmtText, " ", zoneNameString);
     	SimpleDateFormat zoneNameFormatter = new SimpleDateFormat("ZZZZ,zzzz");
     	zoneNameFormatter.setTimeZone(tz);
     	zoneNameString = zoneNameFormatter.format(now);
        //add by yeqing.lv for XR7219033 on 2018-12-13 begin
        boolean isRuTZ = context.getResources().getBoolean(R.bool.def_timezone_ru_flag);
        //add by yeqing.lv for XR7219033 on 2018-12-13 end
     	Log.i(TAG, "isRuTZ====" + isRuTZ);
     	if (isRuTZ) {
     		String id = tz.getID();
     		Log.w(TAG, "getTimeZoneText tz=" + tz + ", text=" + zoneNameString
     				+ ", id=" + tz.getID());
     		if (mCityZoneMap.containsKey(id)) {
     			String[] russianTimezoneNames = context.getResources()
     					.getStringArray(R.array.russian_timezonename);
     			zoneNameString = zoneNameString.substring(0,
     					zoneNameString.indexOf(",") + 1) + " "+ russianTimezoneNames[Integer.parseInt(mCityZoneMap.get(id)) - 1];
     			}
     		}
            //Begin added by yeqing.lv for XR6309715 on 2018/07/25
            if (zoneNameString.contains("Москва, стандартное время")) {
                zoneNameString = zoneNameString.substring(0, zoneNameString.indexOf(",") + 1)+ " " + "Минское время";
            }
            // End added by yeqing.lv for XR6309715 on 2018/07/25
     		return zoneNameString;
     		// End modify by yeqing.lv for XR6619997 on 2018/07/25
    }

    public static List<Map<String, Object>> getZonesList(Context context) {
        final Locale locale = context.getResources().getConfiguration().locale;
        final Date now = new Date();
        final TimeZoneNames timeZoneNames = TimeZoneNames.getInstance(locale);
        final ZoneGetterData data = new ZoneGetterData(context);

        // Work out whether the display names we would show by default would be ambiguous.
        final boolean useExemplarLocationForLocalNames =
                shouldUseExemplarLocationForLocalNames(data, timeZoneNames);

        // Generate the list of zone entries to return.
        List<Map<String, Object>> zones = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < data.zoneCount; i++) {
            TimeZone tz = data.timeZones[i];
            CharSequence gmtOffsetText = data.gmtOffsetTexts[i];

            CharSequence displayName = getTimeZoneDisplayName(data, timeZoneNames,
                    useExemplarLocationForLocalNames, tz, data.olsonIdsToDisplay[i]);
            if (TextUtils.isEmpty(displayName)) {
                displayName = gmtOffsetText;
            }

            int offsetMillis = tz.getOffset(now.getTime());
            Map<String, Object> displayEntry =
                    createDisplayEntry(tz, gmtOffsetText, displayName, offsetMillis);
            zones.add(displayEntry);
        }
        return zones;
    }

    private static Map<String, Object> createDisplayEntry(
            TimeZone tz, CharSequence gmtOffsetText, CharSequence displayName, int offsetMillis) {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_ID, tz.getID());
        map.put(KEY_DISPLAYNAME, displayName.toString());
        map.put(KEY_DISPLAY_LABEL, displayName);
        map.put(KEY_GMT, gmtOffsetText.toString());
        map.put(KEY_OFFSET_LABEL, gmtOffsetText);
        map.put(KEY_OFFSET, offsetMillis);
        return map;
    }

    private static List<String> readTimezonesToDisplay(Context context) {
        List<String> olsonIds = new ArrayList<String>();
        try (XmlResourceParser xrp = context.getResources().getXml(R.xml.timezones)) {
            while (xrp.next() != XmlResourceParser.START_TAG) {
                continue;
            }
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return olsonIds;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String olsonId = xrp.getAttributeValue(0);
                    olsonIds.add(olsonId);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Ill-formatted timezones.xml file");
        } catch (java.io.IOException ioe) {
            Log.e(TAG, "Unable to read timezones.xml file");
        }
        return olsonIds;
    }

    private static boolean shouldUseExemplarLocationForLocalNames(ZoneGetterData data,
            TimeZoneNames timeZoneNames) {
        final Set<CharSequence> localZoneNames = new HashSet<>();
        final Date now = new Date();
        for (int i = 0; i < data.zoneCount; i++) {
            final String olsonId = data.olsonIdsToDisplay[i];
            if (data.localZoneIds.contains(olsonId)) {
                final TimeZone tz = data.timeZones[i];
                CharSequence displayName = getZoneLongName(timeZoneNames, tz, now);
                if (displayName == null) {
                    displayName = data.gmtOffsetTexts[i];
                }
                final boolean nameIsUnique = localZoneNames.add(displayName);
                if (!nameIsUnique) {
                    return true;
                }
            }
        }

        return false;
    }

    private static CharSequence getTimeZoneDisplayName(ZoneGetterData data,
            TimeZoneNames timeZoneNames, boolean useExemplarLocationForLocalNames, TimeZone tz,
            String olsonId) {
        final Date now = new Date();
        final boolean isLocalZoneId = data.localZoneIds.contains(olsonId);
        final boolean preferLongName = isLocalZoneId && !useExemplarLocationForLocalNames;
        String displayName;

        if (preferLongName) {
            displayName = getZoneLongName(timeZoneNames, tz, now);
        } else {
            // Canonicalize the zone ID for ICU. It will only return valid strings for zone IDs
            // that match ICUs zone IDs (which are similar but not guaranteed the same as those
            // in timezones.xml). timezones.xml and related files uses the IANA IDs. ICU IDs are
            // stable and IANA IDs have changed over time so they have drifted.
            // See http://bugs.icu-project.org/trac/ticket/13070 / http://b/36469833.
            String canonicalZoneId = android.icu.util.TimeZone.getCanonicalID(tz.getID());
            if (canonicalZoneId == null) {
                canonicalZoneId = tz.getID();
            }
            displayName = timeZoneNames.getExemplarLocationName(canonicalZoneId);
            if (displayName == null || displayName.isEmpty()) {
                // getZoneExemplarLocation can return null. Fall back to the long name.
                displayName = getZoneLongName(timeZoneNames, tz, now);
            }
        }

        return displayName;
    }

    /**
     * Returns the long name for the timezone for the given locale at the time specified.
     * Can return {@code null}.
     */
    private static String getZoneLongName(TimeZoneNames names, TimeZone tz, Date now) {
        final TimeZoneNames.NameType nameType =
                tz.inDaylightTime(now) ? TimeZoneNames.NameType.LONG_DAYLIGHT
                        : TimeZoneNames.NameType.LONG_STANDARD;
        return names.getDisplayName(tz.getID(), nameType, now.getTime());
    }

    private static void appendWithTtsSpan(SpannableStringBuilder builder, CharSequence content,
            TtsSpan span) {
        int start = builder.length();
        builder.append(content);
        builder.setSpan(span, start, builder.length(), 0);
    }

    // Input must be positive. minDigits must be 1 or 2.
    private static String formatDigits(int input, int minDigits, String localizedDigits) {
        final int tens = input / 10;
        final int units = input % 10;
        StringBuilder builder = new StringBuilder(minDigits);
        if (input >= 10 || minDigits == 2) {
            builder.append(localizedDigits.charAt(tens));
        }
        builder.append(localizedDigits.charAt(units));
        return builder.toString();
    }

    /**
     * Get the GMT offset text label for the given time zone, in the format "GMT-08:00". This will
     * also add TTS spans to give hints to the text-to-speech engine for the type of data it is.
     *
     * @param tzFormatter The timezone formatter to use.
     * @param locale The locale which the string is displayed in. This should be the same as the
     *               locale of the time zone formatter.
     * @param tz Time zone to get the GMT offset from.
     * @param now The current time, used to tell whether daylight savings is active.
     * @return A CharSequence suitable for display as the offset label of {@code tz}.
     */
    private static CharSequence getGmtOffsetText(TimeZoneFormat tzFormatter, Locale locale,
            TimeZone tz, Date now) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();

        final String gmtPattern = tzFormatter.getGMTPattern();
        final int placeholderIndex = gmtPattern.indexOf("{0}");
        final String gmtPatternPrefix, gmtPatternSuffix;
        if (placeholderIndex == -1) {
            // Bad pattern. Replace with defaults.
            gmtPatternPrefix = "GMT";
            gmtPatternSuffix = "";
        } else {
            gmtPatternPrefix = gmtPattern.substring(0, placeholderIndex);
            gmtPatternSuffix = gmtPattern.substring(placeholderIndex + 3); // After the "{0}".
        }

        if (!gmtPatternPrefix.isEmpty()) {
            appendWithTtsSpan(builder, gmtPatternPrefix,
                    new TtsSpan.TextBuilder(gmtPatternPrefix).build());
        }

        int offsetMillis = tz.getOffset(now.getTime());
        final boolean negative = offsetMillis < 0;
        final TimeZoneFormat.GMTOffsetPatternType patternType;
        if (negative) {
            offsetMillis = -offsetMillis;
            patternType = TimeZoneFormat.GMTOffsetPatternType.NEGATIVE_HM;
        } else {
            patternType = TimeZoneFormat.GMTOffsetPatternType.POSITIVE_HM;
        }
        final String gmtOffsetPattern = tzFormatter.getGMTOffsetPattern(patternType);
        final String localizedDigits = tzFormatter.getGMTOffsetDigits();

        final int offsetHours = (int) (offsetMillis / DateUtils.HOUR_IN_MILLIS);
        final int offsetMinutes = (int) (offsetMillis / DateUtils.MINUTE_IN_MILLIS);
        final int offsetMinutesRemaining = Math.abs(offsetMinutes) % 60;

        for (int i = 0; i < gmtOffsetPattern.length(); i++) {
            char c = gmtOffsetPattern.charAt(i);
            if (c == '+' || c == '-' || c == '\u2212' /* MINUS SIGN */) {
                final String sign = String.valueOf(c);
                appendWithTtsSpan(builder, sign, new TtsSpan.VerbatimBuilder(sign).build());
            } else if (c == 'H' || c == 'm') {
                final int numDigits;
                if (i + 1 < gmtOffsetPattern.length() && gmtOffsetPattern.charAt(i + 1) == c) {
                    numDigits = 2;
                    i++; // Skip the next formatting character.
                } else {
                    numDigits = 1;
                }
                final int number;
                final String unit;
                if (c == 'H') {
                    number = offsetHours;
                    unit = "hour";
                } else { // c == 'm'
                    number = offsetMinutesRemaining;
                    unit = "minute";
                }
                appendWithTtsSpan(builder, formatDigits(number, numDigits, localizedDigits),
                        new TtsSpan.MeasureBuilder().setNumber(number).setUnit(unit).build());
            } else {
                builder.append(c);
            }
        }

        if (!gmtPatternSuffix.isEmpty()) {
            appendWithTtsSpan(builder, gmtPatternSuffix,
                    new TtsSpan.TextBuilder(gmtPatternSuffix).build());
        }

        CharSequence gmtText = new SpannableString(builder);

        // Ensure that the "GMT+" stays with the "00:00" even if the digits are RTL.
        final BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL;
        gmtText = bidiFormatter.unicodeWrap(gmtText,
                isRtl ? TextDirectionHeuristicsCompat.RTL : TextDirectionHeuristicsCompat.LTR);
        return gmtText;
    }

    private static final class ZoneGetterData {
        public final String[] olsonIdsToDisplay;
        public final CharSequence[] gmtOffsetTexts;
        public final TimeZone[] timeZones;
        public final Set<String> localZoneIds;
        public final int zoneCount;

        public ZoneGetterData(Context context) {
            final Locale locale = context.getResources().getConfiguration().locale;
            final TimeZoneFormat tzFormatter = TimeZoneFormat.getInstance(locale);
            final Date now = new Date();
            final List<String> olsonIdsToDisplayList = readTimezonesToDisplay(context);

            // Load all the data needed to display time zones
            zoneCount = olsonIdsToDisplayList.size();
            olsonIdsToDisplay = new String[zoneCount];
            timeZones = new TimeZone[zoneCount];
            gmtOffsetTexts = new CharSequence[zoneCount];
            for (int i = 0; i < zoneCount; i++) {
                final String olsonId = olsonIdsToDisplayList.get(i);
                olsonIdsToDisplay[i] = olsonId;
                final TimeZone tz = TimeZone.getTimeZone(olsonId);
                timeZones[i] = tz;
                gmtOffsetTexts[i] = getGmtOffsetText(tzFormatter, locale, tz, now);
            }

            // Create a lookup of local zone IDs.
            localZoneIds = new HashSet<String>();
            for (String olsonId : libcore.icu.TimeZoneNames.forLocale(locale)) {
                localZoneIds.add(olsonId);
            }
        }
    }
}