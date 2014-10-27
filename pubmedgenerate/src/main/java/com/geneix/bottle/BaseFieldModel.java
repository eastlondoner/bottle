package com.geneix.bottle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by andrew on 27/10/14.
 */
public abstract class BaseFieldModel implements com.geneix.bottle.IFieldModel {
    final String fieldName;
    final MedlineFieldDefinition.FieldType fieldType;

    public BaseFieldModel(@Nullable MedlineFieldDefinition.FieldType fieldType, @Nonnull String fieldName) {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
    }

    protected MedlineField.Builder getNewBuilder() {
        MedlineField.Builder builder = MedlineField.builder(fieldName);
        if (fieldType != null) {
            builder.setType(fieldType);
        }
        return builder;
    }

    public MedlineFieldDefinition getDefinition(){
        return MedlineFieldDefinitions.getDefinition(fieldName);
    }
}
