package com.geneix.scorpio.testframework;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.TypeLiteral;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
* Created by Andrew on 20/11/2014.
*/
class ExampleGetEndpoint implements Endpoint<Empty, Pizza> {

    @Override
    public Multimap<String, String> getParameters() {
        ArrayListMultimap<String, String> params = ArrayListMultimap.create();
        params.put("size", "large");
        return params;
    }

    @Override
    public Multimap<String, String> getHeaders() {
        ArrayListMultimap<String, String> headers = ArrayListMultimap.create();
        headers.put("Accepts", "text/json");
        return headers;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public TypeLiteral<Empty> getRequestType() {
        return new TypeLiteral<Empty>() {
        };
    }

    @Override
    public TypeLiteral<Pizza> getResponseType() {
        return new TypeLiteral<Pizza>() {
        };
    }

    @Override
    public String getUriPattern() {
        return "/pizza/(?<flavour>[^/]+)";
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.OK_200;
    }


    @Override
    public Empty getRequestBody() {
        return null;
    }
}
