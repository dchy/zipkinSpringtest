package com.rxjy.zipkintestweb;

import com.rxjy.bean.Book;
import com.rxjy.logUtil.Log;
import com.rxjy.logUtil.LogFactory;
import com.rxjy.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by 11019 on 17.9.13.
 */
@Controller
@RequestMapping("/")
public class PriceController {


    @Autowired
    private BookService bookService;

    private static final Log log = LogFactory.get();

        @RequestMapping("/p")
        @ResponseBody
        public String pricequery(){
            Book query = bookService.query();
            log.info("this is {}","log");
            return query.getName();
        }
}
