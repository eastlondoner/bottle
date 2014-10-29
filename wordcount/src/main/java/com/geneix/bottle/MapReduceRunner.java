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

        //Sometimes hadoop passes in the name of this class, sometimes it doesnt, I don't entirely understand why..
        if(classToRun.contains("MapReduceRunner")){
            classToRun = args[1];
            argsToPass = Arrays.copyOfRange(args,1, args.length);
        } else {
            argsToPass = args;
        }

        Method main = Class.forName(classToRun).getMethod("main", String[].class);
        main.invoke(null, (Object)argsToPass);
    }
}
