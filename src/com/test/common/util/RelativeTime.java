/***************************************************************
/   Copyright (c) 1998-2010 by Progress Software Corporation
/   All rights reserved.  No part of this program or document
/   may be  reproduced in  any form  or by  any means without
/   permission in writing from Progress Software Corporation.
/ *************************************************************
/ 
/   RelativeTime.java
/ 
/   Contains utility methods that pertain to the formatting of
/	a relative time.
/ *************************************************************/

package com.progress.common.util;

public class RelativeTime
{
    private long m_millsTime;

    public static final int FORMAT_DASH  = 1;
    public static final int FORMAT_SPACE = 2;
    public static final int FORMAT_WORDS = 4;
    public static final int FORMAT_UNIX  = 8;
    public static final int FORMAT_SHORT = 16;
    public static final int FORMAT_COLON = 32;

    private static final int FORMAT_DEFAULT = FORMAT_DASH;    

    private static final long MILLS_SEC  = 1000;
    private static final long MILLS_MIN  = 60 * MILLS_SEC;
    private static final long MILLS_HOUR = 60 * MILLS_MIN;
    private static final long MILLS_DAY  = 24 * MILLS_HOUR;

    private String m_dayLabel = "day";
    private String m_daysLabel = "days";
    private String m_hourLabel = "hour";
    private String m_hoursLabel = "hours";
    private String m_minuteLabel = "minute";
    private String m_minutesLabel = "minutes";
    private String m_secondLabel = "second";
    private String m_secondsLabel = "seconds";

    public RelativeTime()
    {
        m_millsTime = 0;
    }

    public RelativeTime(long mills)
    {
        m_millsTime = mills;
    }

    public RelativeTime(long days, long hours, long min, long sec)
    {
        m_millsTime = sec * MILLS_SEC +
                      min * MILLS_MIN +
                      hours * MILLS_HOUR +
                      days * MILLS_DAY;
    }

    public String format()
    {
        return format(m_millsTime, FORMAT_DEFAULT);
    }

    public String format(long val)
    {
        return format(val, FORMAT_DEFAULT);
    }

    public String format(int format)
    {
        return format(m_millsTime, format);
    }

    public String format(long val, int format)
    {
        String result;
        String daysS;
        String hoursS;
        String minS;
        String secS;
        String millsS;
        long days = 0;
        long hours = 0;
        long min = 0;
        long sec = 0;
        long mills = val;

        if (mills > 0)
        {
            days = mills / MILLS_DAY;
            mills -= (days * MILLS_DAY);

            hours = mills / MILLS_HOUR;
            mills -= (hours * MILLS_HOUR);

            min = mills / MILLS_MIN;
            mills -= (min * MILLS_MIN);

            sec = mills / MILLS_SEC;
            mills -= (sec * MILLS_SEC);
        }

        daysS = Long.toString(days);                
/*
        if (days < 10)
            daysS = "0" + daysS;
        if (days < 100)
            daysS = "0" + daysS;
*/ 
        hoursS = Long.toString(hours);
        if (hours < 10)
            hoursS = "0" + hoursS;

        minS = Long.toString(min);
        if (min < 10)
            minS = "0" + minS;

        secS = Long.toString(sec);
        if (sec < 10)
            secS = "0" + secS;

        millsS = Long.toString(mills);
        if (mills < 10)
            millsS = "0" + millsS;
        if (mills < 100)
            millsS = "0" + millsS;

        switch (format)
        {
            case FORMAT_COLON:    // 2:04:06:08
                result = (days > 0 ? daysS + ":" : "") 
                       + hoursS + ":" + minS + ":" + secS 
                    // + (mills > 0 ? "." + millsS : "")
                       ;
                break;

            case FORMAT_DASH:    // 2-04:06:08
                result = (days > 0 ? daysS + "-" : "") 
                       + hoursS + ":" + minS + ":" + secS 
                    // + (mills > 0 ? "." + millsS : "")
                       ;
                break;

            case FORMAT_SPACE:    // 2 04:06:08
                result = (days > 0 ? daysS + " " : "") 
                       + hoursS + ":" + minS + ":" + secS 
                    // + (mills > 0 ? "." + millsS : "")
                       ;
                break;

            case FORMAT_WORDS:    // 2 days 4 hours 6 minutes 8 seconds
                result = days + " " + (days != 1 ? m_daysLabel : m_dayLabel) + " " +
                         hours + " " + (hours != 1 ? m_hoursLabel : m_hourLabel) + " " +
                         min + " " + (min != 1 ? m_minutesLabel : m_minuteLabel) + " " +
                         sec + " " + (sec != 1 ? m_secondsLabel : m_secondLabel);
                break;

            case FORMAT_UNIX:     // 2 days  4:06
                result = days + " " + m_daysLabel + ",  " + hours + ":" + minS;
                break;

            case FORMAT_SHORT:    // 2d 4h 6m 8s
                result = days + m_daysLabel.substring(0, 1) + " " 
                       + hours + m_hoursLabel.substring(0, 1) + " " 
                       + min + m_minuteLabel.substring(0, 1) + " " 
                       + sec + m_secondsLabel.substring(0, 1)
                       ;
                break;

            default:
                result = Long.toString(val);
                break;
        }
        return result;
    }

    public String toString()
    {
        return Long.toString(m_millsTime);
    }

    public long getTime()
    {
        return m_millsTime;
    }

    public void setTime(long mills)
    {
        m_millsTime = mills;
    }

    public void setTime(long days, long hours, long min, long sec)
    {
        m_millsTime = sec * MILLS_SEC +
                      min * MILLS_MIN +
                      hours * MILLS_HOUR +
                      days * MILLS_DAY;
    }

    public void setDayLabels(String singular, String plural)
    {
        if (singular != null && singular.length() > 0)
            m_dayLabel = new String(singular.trim());
        if (plural != null && plural.length() > 0)
            m_daysLabel = new String(plural.trim());
    }

    public void setHourLabels(String singular, String plural)
    {
        if (singular != null && singular.length() > 0)
            m_hourLabel = new String(singular.trim());
        if (plural != null && plural.length() > 0)
            m_hoursLabel = new String(plural.trim());
    }

    public void setMinuteLabels(String singular, String plural)
    {
        if (singular != null && singular.length() > 0)
            m_minuteLabel = new String(singular.trim());
        if (plural != null && plural.length() > 0)
            m_minutesLabel = new String(plural.trim());
    }

    public void setSecondLabels(String singular, String plural)
    {
        if (singular != null && singular.length() > 0)
            m_secondLabel = new String(singular.trim());
        if (plural != null && plural.length() > 0)
            m_secondsLabel = new String(plural.trim());
    }
}
