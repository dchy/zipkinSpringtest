package com.rxjy.brave.adapter;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by 11019 on 17.9.8.
 */
public class CustomServerResponseAdapter implements ServerResponseAdapter {

    private final String ex;

    public CustomServerResponseAdapter(String ex) {
        this.ex=ex;
    }

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        return Collections.singleton(KeyValueAnnotation.create("error", ex));
    }
}
