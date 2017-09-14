package com.rxjy.brave.interceptor;


import com.github.kristofa.brave.*;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.HttpServerResponseAdapter;
import com.github.kristofa.brave.http.SpanNameProvider;
import com.rxjy.brave.adapter.CustomServerRequestAdapter;
import com.rxjy.brave.adapter.CustomServerResponseAdapter;
import com.rxjy.brave.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.kristofa.brave.internal.Util.checkNotNull;

@Configuration
public class CustomServletHandlerInterceptor extends HandlerInterceptorAdapter {

    static final String HTTP_SERVER_SPAN_ATTRIBUTE = CustomServletHandlerInterceptor.class.getName() + ".customserver-span";

    /**使用自定义创建跟踪拦截器*/
    /**
     * Creates a tracing interceptor with custom
     */
    public static CustomServletHandlerInterceptor create(Brave brave) {
        return new Builder(brave).build();
    }

    public static Builder builder(Brave brave) {
        return new Builder(brave);
    }

    public static final class Builder {
        final Brave brave;
        SpanNameProvider spanNameProvider = new DefaultSpanNameProvider();

        Builder(Brave brave) { // intentionally hidden
            this.brave = checkNotNull(brave, "brave");
        }

        public Builder spanNameProvider(SpanNameProvider spanNameProvider) {
            this.spanNameProvider = checkNotNull(spanNameProvider, "spanNameProvider");
            return this;
        }

        public CustomServletHandlerInterceptor build() {
            return new CustomServletHandlerInterceptor(this);
        }
    }

    private final ServerRequestInterceptor requestInterceptor;
    private final ServerResponseInterceptor responseInterceptor;
    private final ServerSpanThreadBinder serverThreadBinder;
    private final SpanNameProvider spanNameProvider;

    CustomServletHandlerInterceptor(){
        Sender sender = OkHttpSender.create("http://localhost:11008/api/v1/spans");
        Reporter<Span> reporter = AsyncReporter.builder(sender).build();

        //获取配置文件中的folderpath对应的值
        String rootDir = CommonUtil.getPath("webname.properties", "webname");
        String servicename = "hyhy.cs.web.test";
        if(rootDir!=null&&!rootDir.equals("")){
            servicename = "hyhy.cs."+rootDir+".test";
        }
        Brave.Builder builder = new Brave.Builder(servicename);

        //设置采样率为0.3
        // builder.traceSampler(Sampler.create(0.3f));

        //设置采样率为1
        builder.traceSampler(Sampler.ALWAYS_SAMPLE);
        Brave brave = builder.reporter(reporter).build();
//        Brave brave = new Brave.Builder(servicename).reporter(reporter).build();
        this.requestInterceptor = brave.serverRequestInterceptor();
        this.responseInterceptor = brave.serverResponseInterceptor();
        this.serverThreadBinder = brave.serverSpanThreadBinder();
        this.spanNameProvider = new DefaultSpanNameProvider();
    }

    @Autowired
    CustomServletHandlerInterceptor(SpanNameProvider spanNameProvider, Brave brave) {
        this(builder(brave).spanNameProvider(spanNameProvider));
    }



    CustomServletHandlerInterceptor(Builder b) { // intentionally hidden
        this.requestInterceptor = b.brave.serverRequestInterceptor();
        this.responseInterceptor = b.brave.serverResponseInterceptor();
        this.serverThreadBinder = b.brave.serverSpanThreadBinder();
        this.spanNameProvider = b.spanNameProvider;
    }

    /**
     * @deprecated please use {@link #create(Brave)} or {@link #(Brave)}
     */
    @Deprecated
    public CustomServletHandlerInterceptor(ServerRequestInterceptor requestInterceptor, ServerResponseInterceptor responseInterceptor, SpanNameProvider spanNameProvider, final ServerSpanThreadBinder serverThreadBinder) {
        this.requestInterceptor = requestInterceptor;
        this.spanNameProvider = spanNameProvider;
        this.responseInterceptor = responseInterceptor;
        this.serverThreadBinder = serverThreadBinder;
    }

    /**
     * 预处理回调方法，实现处理器的预处理（如登录检查），第三个参数为响应的处理器；
     * 返回值：true表示继续流程（如调用下一个拦截器或处理器）；
     * false表示流程中断（如登录检查失败），不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应；
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {

        requestInterceptor.handle(new CustomServerRequestAdapter(request, spanNameProvider));
        return true;
    }

    /**
     * @param request
     * @param response
     * @param handler
     */
    @Override
    public void afterConcurrentHandlingStarted(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        request.setAttribute(HTTP_SERVER_SPAN_ATTRIBUTE, serverThreadBinder.getCurrentServerSpan());
        serverThreadBinder.setCurrentSpan(null);
    }

    /**
     * 整个请求处理完毕回调方法，即在视图渲染完毕时回调，如性能监控中我们可以在此记录结束时间并输出消耗时间，还可以进行一些资源清理，
     * 类似于try-catch-finally中的finally，但仅调用处理器执行链中preHandle返回true的拦截器的afterCompletion。
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     */
    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex) {

        final ServerSpan span = (ServerSpan) request.getAttribute(HTTP_SERVER_SPAN_ATTRIBUTE);
        if (span != null) {
            serverThreadBinder.setCurrentSpan(span);
        }

        //异常获取
        if (ex != null) {
            responseInterceptor.handle(new CustomServerResponseAdapter(ex.toString()));
        } else {
            responseInterceptor.handle(new HttpServerResponseAdapter(new HttpResponse() {
                @Override
                public int getHttpStatusCode() {
                    return response.getStatus();
                }
            }));
        }

    }
}

