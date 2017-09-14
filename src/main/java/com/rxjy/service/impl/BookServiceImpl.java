package com.rxjy.service.impl;

import com.rxjy.bean.Book;
import com.rxjy.service.BookService;
import org.springframework.stereotype.Service;

/**
 * Created by 11019 on 17.9.8.
 */
@Service("bookService")
public class BookServiceImpl implements BookService {
    public Book query() {
        Book book = new Book();
        book.setId("1");
        book.setName("java");
        book.setPrice("100");

        return book;
    }
}
