package com.rxjy.zipkintestweb;

import com.rxjy.bean.Book;
import com.rxjy.logUtil.Log;
import com.rxjy.logUtil.LogFactory;
import com.rxjy.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by 11019 on 17.9.8.
 */
@EnableWebMvc
@Controller
@RequestMapping("/")
public class BookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private RestTemplate template;

    private static final Log log = LogFactory.get();

    @RequestMapping("/query")
    @ResponseBody
    public String query(){
        Book query = bookService.query();
        log.info("this is {}","log");
//        String louie = template.getForObject("http://localhost:8080/p?name={0}", String.class, "louie");
//        List list = null;
//        list.get(3);
        return query.getName();
    }
}
