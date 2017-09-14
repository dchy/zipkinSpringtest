package com.rxjy.brave.webmvc;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.SpanNameProvider;
import com.rxjy.brave.util.CommonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

/**
 * Created by 11019 on 17.9.13.
 */
@Configuration
@Order(value = 1)
public class HttpTraceBeans {

    @Bean
    RestTemplate template(){
        return new RestTemplate();
    }

    /** Configuration for how to send spans to Zipkin */
    /**发送器配置*/
    @Bean
    Sender sender() {
        return OkHttpSender.create("http://localhost:11008/api/v1/spans");
    }


    /** 配置如何缓冲跨越Zipkin消息 */
    /** Configuration for how to buffer spans into messages for Zipkin */
    @Bean
    Reporter<Span> reporter() {

        //取消注释将以日志形式打印在控制台上
        //return new LoggingReporter();
        // uncomment to actually send to zipkin!
        //实际发送到Zipkin！
        return AsyncReporter.builder(sender()).build();
    }


    @Bean
    Brave brave() {

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
        Brave brave = builder.reporter(reporter()).build();
        return brave;
    }

    //span命名提供者，默认为http方法
    // decide how to name spans. By default they are named the same as the http method.
    @Bean
    SpanNameProvider spanNameProvider() {
        return new DefaultSpanNameProvider();
    }
}
