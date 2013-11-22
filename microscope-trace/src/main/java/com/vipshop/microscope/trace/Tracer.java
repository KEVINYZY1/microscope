package com.vipshop.microscope.trace;

import com.vipshop.microscope.trace.span.Category;

public class Tracer {
	
	/**
	 * Handle common method opeations.
	 * 
	 * @param spanName the name of method
	 * @param category the category of service
	 */
	public static void clientSend(String spanName, Category category){
		TraceFactory.getTrace().clientSend(spanName, category);
	}
	
	/**
	 * Handle MyBatis/Hibernate/SQL/database operations.
	 * 
	 * @param name the name of method
	 * @param serverIP the database name where sql execute
	 * @param category the category of service
	 */
	public static void clientSend(String name, String serverIP, Category category) {
		TraceFactory.getTrace().clientSend(name, serverIP, category);
	}

	/**
	 * Handle corss-JVM operations.(httpclient request)
	 * 
	 * @param traceId the traceId from client
	 * @param spanId  the spanId from client
	 * @param name the name of method
	 * @param category the category of service
	 */
	public static void clientSend(String traceId, String spanId, String name, Category category){
		TraceFactory.getTrace(traceId, spanId).clientSend(name, category);
	}
	
	/**
	 * Complete a span.
	 */
	public static void clientReceive() {
		TraceFactory.getTrace().clientReceive();
	}

	/**
	 * Set result code.
	 * 
	 * If exception happens. set ResultCode = EXCEPTION.
	 * 
	 * @param e
	 */
	public static void setResultCode(String result) {
		Trace trace = TraceFactory.getContext();
		if (trace != null) {
			trace.setResutlCode(result);
		}
	}
	
	/**
	 * Record a Key-Value for debug.
	 * 
	 * @param key key String
	 * @param value value String
	 */
	public static void record(String key, String value) {
		Trace trace = TraceFactory.getContext();
		if (trace != null) {
			trace.record(key, value);
		}
	}
	
	/**
	 * Asyn thread invoke.
	 * 
	 * Get trace object from {@code ThreadLocal}.
	 * 
	 * @return
	 */
	public static Trace getContext() {
		return TraceFactory.getContext();
	}
	
	/**
	 * Asyn thread invoke.
	 * 
	 * Set trace object to {@code ThreadLocal}
	 * with the new Thread.
	 * 
	 * @param trace
	 */
	public static void setContext(Trace trace) {
		TraceFactory.setContext(trace);
	}
	
	/**
	 * Clean {@code ThreadLocal}
	 */
	public static void cleanContext() {
		TraceFactory.cleanContext();
	}
	
	/**
	 * Get traceId from {@code ThreadLocal}
	 * 
	 * @return traceId
	 */
	public static String getTraceId() {
		return TraceFactory.getTraceIdFromThreadLocal();
	}
	
	/**
	 * Get spanId from {@code ThreadLocal}
	 * 
	 * @return spanId
	 */
	public static String getSpanId() {
		return TraceFactory.getSpanIdFromThreadLocal();
	}
	
}
