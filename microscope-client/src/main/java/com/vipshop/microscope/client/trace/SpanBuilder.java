package com.vipshop.microscope.client.trace;

import com.vipshop.microscope.client.storage.Storages;
import com.vipshop.microscope.common.util.IPAddressUtil;
import com.vipshop.microscope.client.Tracer;
import com.vipshop.microscope.thrift.Span;
import com.vipshop.microscope.client.storage.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A {@code SpanBuilder} responsible for build
 * span and store span to {@code Storage}.
 *
 * @author Xu Fei
 * @version 1.0
 */
public class SpanBuilder {

    private final Storage storage = Storages.getStorage();

    /**
     * The context of span.
     */
    private final SpanContext spanContext;

    /**
     * A stack used to store span by thread.
     */
    private final Stack<Span> spanStack;

    public SpanBuilder() {
        this.spanContext = new SpanContext(new SpanID());
        this.spanStack = new Stack<Span>();
    }

    public SpanBuilder(SpanContext spanContext) {
        this.spanContext = spanContext;
        this.spanStack = new Stack<Span>();
    }

    public SpanID getSpanID() {
        return spanContext.getSpanID();
    }

    /**
     * A span contains two annotations, send annotation
     * and receive annotation.
     * <p/>
     * when span trigger a send action, we create a new
     * span, and associate it with a send annotation;
     * when span trigger a receive action, we get the
     * span, and associate it with a receive annotation.
     * finally, we put the span to a queue.
     * <p/>
     * must make sure the new span is a sub span or not!
     *
     * @param spanName
     */
    public void clientSend(String spanName, SpanCategory category) {
        Span span = new Span();
        span.setAppName(Tracer.APP_NAME);
        span.setAppIp(IPAddressUtil.IPAddress());
        span.setTraceId(spanContext.getTraceId());
        span.setSpanName(spanName);
        span.setSpanType(category.getStrValue());
        span.setStartTime(System.currentTimeMillis());
        span.setResultCode(TraceStatus.OK);

		/*
		 * The topmost span in a trace has its span id 
		 * equal to trace id and parent span id is null.
		 */
        if (spanContext.isRootSpan()) {
            // set span id equal to trace id for top span.
            span.setParentId(0);
            span.setSpanId(spanContext.getTraceId());

            spanContext.setSpanId(spanContext.getTraceId());
            // make top span flag to be false.
            spanContext.setRootSpanFlagFalse();
        } else {
			/*
			 * if this coming span is a sub span.
			 * create a new span id
			 * set the parent span id
			 */
            span.setParentId(spanContext.getSpanId());
            long spanId = SpanID.createId();
            span.setSpanId(spanId);
            spanContext.setSpanId(spanId);
        }
		
		/*
		 * make the new span be the
		 * current span of trace.
		 */
        spanContext.setCurrentSpan(span);
		
		/*
		 * push the new span to stack.
		 */
        spanStack.push(span);
    }

    /**
     * This process receive annotation.
     * <p/>
     * One thing attention: after get span from stack,
     * we need reset current span.
     */
    public void clientReceive() {
        if (spanStack.isEmpty()) {
            return;
        }
		/*
    	 * remove span from stack
    	 */
        Span span = spanStack.pop();
        span.setDuration((int) (System.currentTimeMillis() - span.getStartTime()));

		/*
    	 * put span to queue
    	 */
        storage.add(span);

		/*
    	 * check stack, if span exist,
    	 * then reset current span.
    	 */
        if (!spanStack.isEmpty()) {
            spanContext.setCurrentSpan(spanStack.peek());
        } else {
            spanContext.setCurrentSpan(null);
        }
    }

    /**
     * Current don't support
     */
    public void serverReceive() {
        throw new UnsupportedOperationException();
    }

    /**
     * Current don't support
     */
    public void serverSend() {
        throw new UnsupportedOperationException();
    }


    /**
     * Set span result code.
     *
     * @param result
     */
    public void setResultCode(String result) {
        if (spanStack.isEmpty()) {
            return;
        }
		/*
    	 * get span from stack
    	 */
        Span span = spanStack.peek();
        span.setResultCode(result);
    }

    /**
     * Record key/value info to span.
     *
     * @param key
     * @param value
     */
    public void record(String key, String value) {
        if (spanStack.isEmpty()) {
            return;
        }
		
		/*
		 * if key/value equals null, return
		 */
        if (key == null || value == null) {
            return;
        }
		
		/*
    	 * get span from stack
    	 */
        try {
            Span span = spanStack.peek();
            if (span != null) {
                Map<String, String> debug = span.debug;
                if (debug == null) {
                    debug = new HashMap<String, String>();
                }
                debug.put(key, value);
                span.setDebug(debug);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "SpanBuilder [spanContext=" + spanContext + ", spanStack=" + spanStack + "]";
    }

}
