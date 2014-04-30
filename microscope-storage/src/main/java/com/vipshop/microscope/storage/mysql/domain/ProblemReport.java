package com.vipshop.microscope.storage.mysql.domain;

import com.vipshop.microscope.common.util.CalendarUtil;
import com.vipshop.microscope.common.util.IPAddressUtil;
import com.vipshop.microscope.common.util.TimeStampUtil;
import com.vipshop.microscope.trace.gen.Span;
import com.vipshop.microscope.trace.span.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Problem Report.
 * <p/>
 * <p>Currently we have these kinds of problem:
 * <p/>
 * <pre>
 *  Long-URL problem
 *  Long-Action problem
 *  Long-Db problem
 *  Long-Service problem
 *  Long-Cache problem
 *  Long-Method problem
 *  Long-System problem
 * </pre>
 * <p/>
 * This report are stated by 5 minute.
 *
 * @author Xu Fei
 * @version 1.0
 */
public class ProblemReport extends AbstraceReport {

    public static final Logger logger = LoggerFactory.getLogger(ProblemReport.class);

    private static final ConcurrentHashMap<String, ProblemReport> problemContainer = new ConcurrentHashMap<String, ProblemReport>();
    ;


    private String appName;
    private int appIp;

    private int proType;
    private int proTime;
    private String proDesc;

    private int proCount;

    private long traceId;

    public static boolean hasProblme(Span span) {
        return Category.hasProblem(span);
    }

    public static int getTypeZone(Span span) {
        return Category.getIntValue(span);
    }

    public static int getTimeZone(Span span) {
        return Category.getTimeZone(span);
    }

    public void analyze(CalendarUtil calendarUtil, Span span) {
//		String preKey = ProblemReport.getPrevKey(calendarUtil, span);
//		ProblemReport preReport = problemContainer.get(preKey);
//		if (preReport != null) {
//			try {
//				preReport.saveReport();
//			} catch (Exception e) {
//				logger.error("save problem report to mysql error ignore ... " + e);
//			} finally {
//				problemContainer.remove(preKey);
//			}
//		}

        // put report to hashmap by hour
        String key = this.getKey(calendarUtil, span);
        ProblemReport report = problemContainer.get(key);
        if (report == null) {
            report = new ProblemReport();
            report.updateReportInit(calendarUtil, span);
        }
        report.updateReportNext(span);
        problemContainer.put(key, report);

        // save previous report to mysql and remove form hashmap
        Set<Entry<String, ProblemReport>> entries = problemContainer.entrySet();
        for (Entry<String, ProblemReport> entry : entries) {
            String prevKey = entry.getKey();
            if (!prevKey.equals(key)) {
                ProblemReport prevReport = entry.getValue();
                try {
                    prevReport.saveReport();
                } catch (Exception e) {
                    logger.error("save problem report --> [" + prevReport + "] to mysql error ignore ... " + e);
                } finally {
                    problemContainer.remove(prevKey);
                }
            }
        }
    }


    @Override
    public void updateReportInit(CalendarUtil calendarUtil, Span span) {
        this.setDateByHour(calendarUtil);
        this.setAppName(span.getAppName());
        this.setAppIp(IPAddressUtil.stringToInt(span.getAppIp()));
        this.setProType(getTimeZone(span));
        this.setProTime(getTimeZone(span));
        this.setProDesc(span.getSpanName());
    }

    @Override
    public void updateReportNext(Span span) {
        this.setProCount(this.getProCount() + 1);
        this.setProDesc(span.getSpanName() + "#" + span.getTraceId());
    }

    @Override
    public void saveReport() {
    }

    public String getKey(CalendarUtil calendar, Span span) {
        String appName = span.getAppName();
        String appIp = span.getAppIp();
        int typeZone = ProblemReport.getTypeZone(span);
        int timeZone = ProblemReport.getTimeZone(span);
        String name = span.getSpanName();
        StringBuilder builder = new StringBuilder();
        builder.append(TimeStampUtil.timestampOfCurrentHour(calendar))
                .append("-").append(appName)
                .append("-").append(appIp)
                .append("-").append(typeZone)
                .append("-").append(timeZone)
                .append("-").append(name);
        return builder.toString();
    }

    public String getPrevKey(CalendarUtil calendar, Span span) {
        String appName = span.getAppName();
        String appIp = span.getAppIp();
        int typeZone = ProblemReport.getTypeZone(span);
        int timeZone = ProblemReport.getTimeZone(span);
        String name = span.getSpanName();
        StringBuilder builder = new StringBuilder();
        builder.append(TimeStampUtil.timestampOfPrevHour(calendar))
                .append("-").append(appName)
                .append("-").append(appIp)
                .append("-").append(typeZone)
                .append("-").append(timeZone)
                .append("-").append(name);
        return builder.toString();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getAppIp() {
        return appIp;
    }

    public void setAppIp(int appIp) {
        this.appIp = appIp;
    }

    public int getProType() {
        return proType;
    }

    public void setProType(int problemType) {
        this.proType = problemType;
    }

    public int getProTime() {
        return proTime;
    }

    public void setProTime(int timeZone) {
        this.proTime = timeZone;
    }

    public int getProCount() {
        return proCount;
    }

    public void setProCount(int count) {
        this.proCount = count;
    }

    public String getProDesc() {
        return proDesc;
    }

    public void setProDesc(String desc) {
        this.proDesc = desc;
    }

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    @Override
    public String toString() {
        return super.toString() + " ProblemReport content " +
                " [appName=" + appName + ", appIp=" + appIp +
                ", problemType=" + proType + " ,timeZone=" + proTime +
                ", count=" + proCount + ", desc=" + proDesc + ", year=" + year + ", " +
                "month=" + month + ", week=" + week + ", day=" + day + ", " +
                "hour=" + hour + ", minute=" + minute + "]";
    }

}
