package com.rxjy.brave.adapter;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerRequestAdapter;
import com.github.kristofa.brave.SpanId;
import com.github.kristofa.brave.TraceData;
import com.github.kristofa.brave.http.BraveHttpHeaders;
import com.github.kristofa.brave.http.SpanNameProvider;
import zipkin.TraceKeys;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.kristofa.brave.IdConversion.convertToLong;

public class CustomServerRequestAdapter  implements ServerRequestAdapter {
    private final HttpServletRequest request;
//    private final SpanNameProvider spanNameProvider;


    public CustomServerRequestAdapter(HttpServletRequest request, SpanNameProvider spanNameProvider) {
        this.request = request;
//        this.spanNameProvider = spanNameProvider;
    }

    @Override
    public TraceData getTraceData() {
        String sampled = request.getHeader(BraveHttpHeaders.Sampled.getName());
        String parentSpanId = request.getHeader(BraveHttpHeaders.ParentSpanId.getName());
        String traceId = request.getHeader(BraveHttpHeaders.TraceId.getName());
        String spanId = request.getHeader(BraveHttpHeaders.SpanId.getName());

        //官方抽样价值为1，虽然有些旧仪器发送真实
        // Official sampled value is 1, though some old instrumentation send true
        Boolean parsedSampled = sampled != null
            ? sampled.equals("1") || sampled.equalsIgnoreCase("true")
            : null;

        if (traceId != null && spanId != null) {
            return TraceData.create(getSpanId(traceId, spanId, parentSpanId, parsedSampled));
        } else if (parsedSampled == null) {
            return TraceData.EMPTY;
        } else if (parsedSampled.booleanValue()) {
            // Invalid: The caller requests the trace to be sampled, but didn't pass IDs
            //无效：呼叫者请求跟踪被采样，但没有通过ID
            return TraceData.EMPTY;
        } else {
            return TraceData.NOT_SAMPLED;
        }
    }

    //span名称获取
    @Override
    public String getSpanName() {
        String method = request.getMethod();
        return method;

    }

    @Override
    public Collection<KeyValueAnnotation> requestAnnotations() {
        List<KeyValueAnnotation> kvs = new ArrayList<KeyValueAnnotation>();
        KeyValueAnnotation uriAnnotation = null;
    	Map<String, String[]> params = this.request.getParameterMap();
    	for(String key:params.keySet()){
    		KeyValueAnnotation kv = KeyValueAnnotation.create(key, params.get(key)[0]);
    		kvs.add(kv);
    	}

    	//获取Web服务器名字
        uriAnnotation = KeyValueAnnotation.create(
                TraceKeys.HTTP_HOST, request.getServerName().toString());
        if(uriAnnotation!=null&&!uriAnnotation.equals("")){
            kvs.add(uriAnnotation);
        }

        //获取请求URL
        uriAnnotation = KeyValueAnnotation.create(
                TraceKeys.HTTP_PATH, request.getRequestURI().toString());
        if(uriAnnotation!=null&&!uriAnnotation.equals("")){
            kvs.add(uriAnnotation);
        }

        //获取客户端ip
        uriAnnotation = KeyValueAnnotation.create(
                TraceKeys.HTTP_URL, request.getRequestURL().toString());
        if(uriAnnotation!=null&&!uriAnnotation.equals("")){
            kvs.add(uriAnnotation);
        }


        return kvs;
    }

    static SpanId getSpanId(String traceId, String spanId, String parentSpanId, Boolean sampled) {
        return SpanId.builder()
            .traceIdHigh(traceId.length() == 32 ? convertToLong(traceId, 0) : 0)
            .traceId(convertToLong(traceId))
            .spanId(convertToLong(spanId))
            .sampled(sampled)
            .parentId(parentSpanId == null ? null : convertToLong(parentSpanId)).build();
   }
}
