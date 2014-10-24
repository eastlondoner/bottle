package com.geneix.bottle;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import static com.geneix.bottle.MedlineFieldDefinition.FieldType.*;

/**
 * Created by Andrew on 24/10/2014.
 */
public class MedlineFieldDefinitions {
    private final static Map<String,MedlineFieldDefinition> allDefinitions = new HashMap<>(25);
    public static boolean addField(MedlineFieldDefinition field){
        return allDefinitions.put(field.getPrimaryKey(),field) == null;
    }

    public static MedlineFieldDefinition getDefinition(String primaryKey) {
        return allDefinitions.get(primaryKey);
    }

    //TODO: REMOVE STATIC INITIALIZER
    //Load the data from somewhere else
    static {
        new MedlineFieldDefinition("FAU", new TreeSet<String>(Arrays.asList("AU", "AUID", "AD")), SINGLE_OBJECT_VALUE);
        new MedlineFieldDefinition("PHST", new TreeSet<String>(Arrays.asList("PHST")), ARRAY_TEXT_VALUES);
        new MedlineFieldDefinition("IS", new TreeSet<String>(Arrays.asList("IS")),ARRAY_TEXT_VALUES);
        new MedlineFieldDefinition("AID", new TreeSet<String>(Arrays.asList("AID")),ARRAY_TEXT_VALUES);
        new MedlineFieldDefinition("AB", new TreeSet<String>(),WORDS);
    }
}
