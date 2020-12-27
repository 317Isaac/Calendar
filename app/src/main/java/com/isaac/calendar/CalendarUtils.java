package com.isaac.calendar;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;

import java.util.TimeZone;

/**
 * created by Isaac on 2020/12/9
 */
public class  CalendarUtils {

        private static String CALENDAR_URL = "content://com.android.calendar/calendars";
        private static String CALENDAR_EVENT_URL = "content://com.android.calendar/events";
        private static String CALENDAR_REMINDER_URL = "content://com.android.calendar/reminders";

        private static String CALENDARS_NAME = "mang";
        private static String CALENDARS_ACCOUNT_NAME = "mang@mmd.com";
        private static String CALENDARS_ACCOUNT_TYPE = "com.android.mang";
        private static String CALENDARS_DISPLAY_NAME = "mang";

        public static long MYeventID = 0L;

    private static int checkCalendarAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDAR_URL),
                null, null, null, null);
        try {
            if (userCursor == null) { // 查询返回空值
                return -1;
            }
            int count = userCursor.getCount();
            if (count > 0) { // 存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    /**
         * 增加日历提醒
         *
         * @param context
         */
        public static void addCalendar(final Context context ) {
            int calId = checkCalendarAccount(context); // 获取日历账户的id
            if (calId < 0) { // 获取账户id失败直接返回，添加日历事件失败
                return;
            }
            addNewRemindEvent(context, calId);
        }

        /**
         * 删除日历提醒
         */
        public static void searchCalendar(Context context) {
            if (context == null) {
                return;
            }
            String selection = "(" + CalendarContract.Reminders.EVENT_ID + " = ?" +")";
            String[] ARGS = new String[]{String.valueOf(MYeventID)};
            Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALENDAR_EVENT_URL), null, selection, ARGS, null);
            try {
                if (eventCursor == null) { //查询返回空值
                    return;
                }
                if (eventCursor.getCount() > 0) {
                    Log.d("isaac", "yes");
                }

            } finally {
                if (eventCursor != null) {
                    eventCursor.close();
                }
            }

        }

    private static boolean addNewRemindEvent(Context context, int calId) {
//      当前时间+24小时，获取明日day  20201208

        long startTime = System.currentTimeMillis();
        long endTimeSP = System.currentTimeMillis()+10000;

        long eventId;
        try {
            /** 插入日程 */
            ContentValues eventValues = new ContentValues();
            eventValues.put(CalendarContract.Events.DTSTART, startTime);
            eventValues.put(CalendarContract.Events.DTEND, endTimeSP);
            eventValues.put(CalendarContract.Events.TITLE, "aaaaa");//日历小标题：签到提醒
            eventValues.put(CalendarContract.Events.DESCRIPTION, context.getString(R.string.app_name));
            eventValues.put(CalendarContract.Events.CALENDAR_ID, calId);
            //设置有闹钟提醒，提醒如下
            eventValues.put(CalendarContract.Events.HAS_ALARM, 1); // 0 for false, 1 for true
            eventValues.put(CalendarContract.Events.EVENT_LOCATION, "context.getString(R.string.task_sign_hint_descrip)");//详细中的描述；每日连续签到，可或得更多金币哦

            TimeZone tz = TimeZone.getDefault(); // 获取默认时区
            eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());

            Uri eUri = context.getContentResolver().insert(Uri.parse(CALENDAR_EVENT_URL), eventValues);
            eventId = ContentUris.parseId(eUri);
            if (eventId == 0) { // 插入失败
                return false;
            }
            MYeventID = eventId;
            /** 设置插入提醒 - 依赖插入日程成功 */
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            // 提前5分钟有提醒(提前0分钟时，值为0)
            reminderValues.put(CalendarContract.Reminders.MINUTES, 5); // 提前提醒
            reminderValues.put(CalendarContract.Reminders.METHOD,
                    CalendarContract.Reminders.METHOD_ALERT);
            Uri rUri = context.getContentResolver().insert(Uri.parse(CALENDAR_REMINDER_URL),
                    reminderValues);
            if (rUri == null || ContentUris.parseId(rUri) == 0) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
