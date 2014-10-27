package com.geneix.bottle;

import com.google.common.collect.*;
import org.apache.commons.lang.WordUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * Created by Andrew on 24/10/2014.
 * This class is not thread safe
 */
public class MedlineField implements Writable {

    private String fieldName;
    private MedlineFieldDefinition.FieldType type;
    private ImmutableMultimap<String, String> properties; //Not thread safe!

    /*
    public ImmutableMultimap<String,String> properties(){
        return properties;
    }
    */

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public String name() {
        return fieldName;
    }

    public MedlineFieldDefinition.FieldType getType() {
        return type;
    }

    public ImmutableCollection<Map.Entry<String, String>> getEntries(){
        return properties.entries();
    }

    public String getValuesAsString() {
        //TODO: immutable so consider memoizing
        return StringUtils.join(" ", properties.values());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text text;
        text = new Text(fieldName);
        text.write(out);
        WritableUtils.writeEnum(out, type);

        ImmutableCollection<Map.Entry<String, String>> entries = properties.entries();
        //Work Int
        out.writeInt(entries.size());

        for (Map.Entry<String, String> entry : entries) {
            text.set(entry.getKey());
            text.write(out);
            text.set(entry.getValue());
            text.write(out);
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        Text text = new Text();
        text.readFields(in);
        Builder builder = new Builder(text.toString());
        builder.setType(WritableUtils.readEnum(in, MedlineFieldDefinition.FieldType.class));

        int propertiesLength = in.readInt();

        for (int i = 0; i < propertiesLength; i++) {
            text.readFields(in);
            String key = text.toString();
            text.readFields(in);
            String value = text.toString();
            builder.addProperty(key, value);
        }
        builder.apply(this);
    }

    public static class Builder {

        public Builder(String name) {
            fieldName = name;
        }

        private final String fieldName;
        private MedlineFieldDefinition.FieldType type;
        private ImmutableMultimap.Builder<String, String> properties = ImmutableMultimap.builder();

        public Builder addProperty(String propertyKey, String propertyValue) {
            properties.put(propertyKey, propertyValue);
            return this;
        }

        public Builder setType(@Nonnull MedlineFieldDefinition.FieldType type) {
            if (this.type != null) {
                throw new IllegalStateException("Cannot set the field type multiple times");
            }
            this.type = type;
            return this;
        }


        //special private instance method to deal with Hadoop Framework reuse of class instances
        private void apply(MedlineField existingInstance) {
            if (fieldName == null || fieldName.isEmpty()) {
                throw new IllegalStateException("fieldName cannot be null or empty");
            }
            existingInstance.type = type;
            existingInstance.fieldName = fieldName;
            existingInstance.properties = properties.build();
        }

        public MedlineField build() {
            if (fieldName == null || fieldName.isEmpty()) {
                throw new IllegalStateException("fieldName cannot be null or empty");
            }
            if (type == null) {
                type = MedlineFieldDefinition.FieldType.SINGLE_TEXT_VALUE;
            }
            return new MedlineField(fieldName, properties.build(), type);
        }
    }

    public MedlineField() {
        //Empty constructor for Hadoop
    }

    public MedlineField(String fieldName, ImmutableMultimap<String, String> properties, MedlineFieldDefinition.FieldType type) {
        this.fieldName = fieldName;
        this.properties = properties;
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : properties.entries()) {
            String key = entry.getKey();
            int paddingRequired = 4 - key.length();
            sb.append(key);
            for (int i = 0; i < paddingRequired; i++) {
                sb.append(' ');
            }
            sb.append("- ").append(WordUtils.wrap(entry.getValue(), 81, "\n      ", false));
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MedlineField that = (MedlineField) o;

        if (!fieldName.equals(that.fieldName)) return false;
        if (type != that.type) return false;
        if (!properties.equals(that.properties)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + properties.hashCode();
        result = 31 * result + type.name().hashCode();
        return result;
    }

}
