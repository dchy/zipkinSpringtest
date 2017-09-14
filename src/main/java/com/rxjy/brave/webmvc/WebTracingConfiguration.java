package com.rxjy.brave.webmvc;

import com.github.kristofa.brave.spring.BraveClientHttpRequestInterceptor;
import com.rxjy.brave.interceptor.CustomServletHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * This adds tracing configuration to any web mvc controllers or rest template clients. This should
 * be configured last.
 * 这会将跟踪配置添加到任何Web mvc控制器或其他模板客户端。 这应该最后配置。
 */

//导入拦截器是使用javax.inject注释的，而不是自动连线
// import as the interceptors are annotation with javax.inject and not automatically wired
@Configuration
@Import({BraveClientHttpRequestInterceptor.class, CustomServletHandlerInterceptor.class})
public class WebTracingConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  private BraveClientHttpRequestInterceptor clientInterceptor;

  @Autowired
  private RestTemplate restTemplate;

  // 添加rest template拦截器
  @PostConstruct
  public void init() {
    List<ClientHttpRequestInterceptor> interceptors =
        new ArrayList<ClientHttpRequestInterceptor>(restTemplate.getInterceptors());
    interceptors.add(clientInterceptor);
    restTemplate.setInterceptors(interceptors);
  }



  @Autowired
//  private ServletHandlerInterceptor serverInterceptor;
  public CustomServletHandlerInterceptor serverInterceptor;

  // 添加Severlet拦截器
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(serverInterceptor);
  }

}
