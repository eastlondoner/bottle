package com.geneix.bottle;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by andrew on 20/10/14.
 */
public class MapReduceRunner {
    public static void main(String[] args) throws Exception {

        String classToRun = args[0];

        String[] argsToPass;
        if(classToRun.contains("MapReduceRunner")){
            argsToPass = Arrays.copyOfRange(args,1, args.length);
        } else {
            argsToPass = args;
        }
        Method main = Class.forName(classToRun, false, null).getMethod("main", String[].class);
        main.invoke(null, (Object)argsToPass);
    }
}
