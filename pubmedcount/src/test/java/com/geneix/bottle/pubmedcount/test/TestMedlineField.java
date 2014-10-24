package com.geneix.bottle.pubmedcount.test;

import com.geneix.bottle.MedlineField;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

/**
 * Created by Andrew on 24/10/2014.
 */
public class TestMedlineField {
    private static final Logger LOG =Logger.getLogger(TestMedlineField.class);
    @Test
    public void hashCodeWorks(){
        //Test simple equality and hashcode
        MedlineField field1 = MedlineField.builder("Foo").build();
        MedlineField field2 = MedlineField.builder("Foo").build();

        Assert.assertEquals(field1.hashCode(), field2.hashCode());
        Assert.assertEquals(field1, field2);

        //Test simple inequality and hashcode
        MedlineField field3 = MedlineField.builder("Bar").build();
        Assert.assertNotEquals(field1.hashCode(), field3.hashCode());
        Assert.assertNotEquals(field1, field3);


        //Test equality with simple property sets
        MedlineField field4 = MedlineField.builder("Bar").addProperty("Hello", "World").build();
        MedlineField field5 = MedlineField.builder("Bar").addProperty("Hello", "World").build();

        Assert.assertEquals(field4.hashCode(),field5.hashCode());
        Assert.assertEquals(field4,field5);


        //Test inequality with simple property sets
        Assert.assertNotEquals(field3.hashCode(),field4.hashCode());
        Assert.assertNotEquals(field3, field4);


        //Test equality is agnostic to ordering of construction
        MedlineField field6 = MedlineField.builder("Bar").addProperty("Hola","Mundo").addProperty("Hello", "World").build();
        MedlineField field7 = MedlineField.builder("Bar").addProperty("Hello", "World").addProperty("Hola","Mundo").build();

        Assert.assertEquals(field6.hashCode(),field7.hashCode());
        Assert.assertEquals(field6,field7);
        Assert.assertNotEquals(field5.hashCode(),field6.hashCode());
        Assert.assertNotEquals(field5, field6);


        //What happens if I add the same field with the same value twice
        MedlineField field8 = MedlineField.builder("Bar").addProperty("Hello", "World").addProperty("Hello", "World").build();

        Assert.assertNotEquals(field8.hashCode(), field5.hashCode());
        Assert.assertNotEquals(field8, field5);


        //Test equality is case sensitive on field name
        MedlineField field9 = MedlineField.builder("foo").build();

        Assert.assertNotEquals(field9.hashCode(), field1.hashCode());
        Assert.assertNotEquals(field1, field9);

        //Test equality is case sensitive on values and property names
        MedlineField field10 = MedlineField.builder("Bar").addProperty("Hello", "world").build();
        MedlineField field11 = MedlineField.builder("Bar").addProperty("hello", "World").build();

        Assert.assertNotEquals(field4.hashCode(), field10.hashCode());
        Assert.assertNotEquals(field4, field10);
        Assert.assertNotEquals(field4.hashCode(), field11.hashCode());
        Assert.assertNotEquals(field4, field11);
    }

    private void valuesAsString(){
        MedlineField field6 = MedlineField.builder("Bar").addProperty("Hola","Mundo").addProperty("Hello", "World").build();

        Assert.assertEquals("World Mundo", field6.getValuesAsString());


        MedlineField field7 = MedlineField.builder("Bar").addProperty("Hello", "World").addProperty("Hola","Mundo").build();

        Assert.assertEquals(field6.getValuesAsString(), field7.getValuesAsString());
    }

    @Test
    public void serialization() throws IOException {
        MedlineField field6 = MedlineField.builder("Bar").addProperty("Hola","Mundo").addProperty("Hello", "World").build();

        ByteArrayOutputStream outArray = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(outArray);
        field6.write(out);
        ByteArrayInputStream inArray = new ByteArrayInputStream(outArray.toByteArray());
        DataInput in = new DataInputStream(inArray);

        MedlineField deserialized = new MedlineField();
        deserialized.readFields(in);

        Assert.assertEquals(field6, deserialized);
        Assert.assertEquals(field6.hashCode(), deserialized.hashCode());
    }
}
