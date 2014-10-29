package com.geneix.bottle;

import java.lang.reflect.Method;

/**
 * Created by andrew on 20/10/14.
 */
public class MapReduceRunner {
    public static void main(String[] args) throws Exception {

        Method main = Class.forName(args[0], false, null).getMethod("main", String[].class);
        main.invoke(null, (Object)args);
    }
}
