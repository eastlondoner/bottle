package com.geneix.bottle;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Created by Andrew on 24/10/2014.
 */
public class MedlineFieldDefinition {
    private final Set<String> otherKeys;
    private final String primaryKey;
    public final FieldType type;
    private final int generatedFieldSize;


    public MedlineFieldDefinition(String primaryKey, Set<String> otherKeys, @Nonnull FieldType type, int generatedFieldSize){
        this.primaryKey = primaryKey;
        this.otherKeys = otherKeys;
        this.type = type;
        this.generatedFieldSize = generatedFieldSize;
        if(!MedlineFieldDefinitions.addField(this)){
            throw new IllegalStateException("Cannot have two medline fields with the same key");
        }
    }

    public String getPrimaryKey(){
        return primaryKey;
    }

    public boolean matchesOtherFields(String fieldKey){
        return otherKeys.contains(fieldKey);
    }

    public int getGeneratedFieldSize() {
        return generatedFieldSize;
    }

    public static enum FieldType {
        WORDS,
        SINGLE_TEXT_VALUE,
        SINGLE_OBJECT_VALUE,
        ARRAY_TEXT_VALUES
    }
}
