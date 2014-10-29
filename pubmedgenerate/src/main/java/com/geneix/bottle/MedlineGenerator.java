package com.geneix.bottle;

import com.google.common.base.Charsets;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by andrew on 27/10/14.
 */
public class MedlineGenerator {
    private final static String[] FIELDS = new String[]{
            "PMID", "OWN", "STAT", "DA", "IS", "IS", "VI", "IP", "DP",
            "TI", "PG", "LID", "AB", "FAU", "FAU", "FAU", "FAU", "FAU", "LA", "PT", "PL", "TA",
            "JT", "JID", "SB", "OTO", "OT", "OT", "OT", "OT", "OT", "EDAT", "MHDA", "CRDT",
            "AID", "PST", "SO"};
    private final RandomDataGenerator DATA_GENERATOR = new RandomDataGenerator();
    private final Map<String, BaseFieldModel> fieldModels = new TreeMap<>();

    public MedlineGenerator(String seed) {
        String[] fields = seed.split("\\r?\\n");

        for (String fieldData : fields) {
            if (fieldData.length() < 4) {
                continue;
            }
            final Scanner scanner = new Scanner(fieldData);
            scanner.useDelimiter("\\t");
            String fieldName = scanner.next();
            scanner.useDelimiter("; ");
            scanner.findInLine("\t");



            MedlineFieldDefinition defn = MedlineFieldDefinitions.getDefinition(fieldName);

            MedlineFieldDefinition.FieldType fieldType = defn != null ? defn.type : MedlineFieldDefinition.FieldType.SINGLE_TEXT_VALUE;

            BaseFieldModel fieldModel = null;

            Iterable<Pair<Long,String>> scannerIterator = new Iterable<Pair<Long,String>>() {
                @Override
                public Iterator<Pair<Long, String>> iterator() {
                    return new Iterator<Pair<Long, String>>() {
                        @Override
                        public boolean hasNext() {
                            return scanner.hasNext();
                        }

                        @Override
                        public Pair<Long,String> next() {
                            String value = "";
                            String[] data;
                            do {
                                String next = scanner.next();
                                data= next.split("\\t");
                                if (data.length > 2) {
                                    throw new IllegalStateException(String.format("Cannot parse word: '%s'", value+next));
                                }
                                value+=data[0];
                            } while (data.length < 2);
                            return new Pair<>(Long.parseLong(data[1]), value);
                        }

                        @Override
                        public void remove() {
                            throw new NotImplementedException();
                        }
                    };
                }
            };

            switch (fieldType) {

                case ARRAY_TEXT_VALUES:
                case SINGLE_TEXT_VALUE:
                case WORDS:
                    SimpleFieldModel model = new SimpleFieldModel(fieldName, fieldType);
                    for (Pair<Long, String> pair : scannerIterator) {
                        model.addValue(pair.getKey(), pair.getValue());
                    }
                    fieldModel = model;
                    break;
                case SINGLE_OBJECT_VALUE:
                    ObjectFieldModel objectModel = new ObjectFieldModel(fieldName);
                    for (Pair<Long, String> pair : scannerIterator) {
                        long weight = pair.getKey();
                        String propertyData = pair.getValue();
                        int firstColonIndex = propertyData.indexOf(':');
                        String propertyName = propertyData.substring(0, firstColonIndex);
                        String propertyValue = propertyData.substring(firstColonIndex);
                        objectModel.addValue(propertyName, weight, propertyValue);
                    }
                    fieldModel = objectModel;
                    break;
            }

            fieldModels.put(fieldName, fieldModel);
        }
    }

    public void generateEntries(OutputStream out, int count) throws IOException {
        int i = 0;
        for(; i< count;i++){
            out.write("\n".getBytes(Charsets.UTF_8));
            out.write(generateEntry().getBytes(Charsets.UTF_8));
        }
        out.flush();
    }

    public String generateEntry() {
        StringBuilder sb = new StringBuilder();
        for (String field : FIELDS) {
            BaseFieldModel fieldModel = fieldModels.get(field);
            if (fieldModel == null) {
                throw new NoSuchElementException("Unknown field " + field);
            }

            MedlineFieldDefinition defn = fieldModel.getDefinition();
            int N = defn == null ? 1 : defn.getGeneratedFieldSize();
            switch (defn == null ? MedlineFieldDefinition.FieldType.SINGLE_TEXT_VALUE : defn.type) {
                case WORDS:
                    sb.append(fieldModel.selectValue(N).toString());
                    break;
                case SINGLE_TEXT_VALUE:
                case SINGLE_OBJECT_VALUE:
                case ARRAY_TEXT_VALUES:
                    sb.append(fieldModel.selectValue(1).toString());
                    break;
            }
        }
        return sb.toString();
    }

    private class SimpleFieldModel extends BaseFieldModel {
        final NavigableMap<Long, String> model = new TreeMap<>();
        Long totalWeight = 0L;

        SimpleFieldModel(@Nonnull String fieldName, @Nullable MedlineFieldDefinition.FieldType fieldType) {
            super(fieldType, fieldName);
        }

        @Override
        public MedlineField selectValue(int n) {
            MedlineField.Builder builder = getNewBuilder();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                sb.append(model.ceilingEntry(DATA_GENERATOR.nextLong(0, totalWeight)).getValue());
                if (i < n) {
                    sb.append(" ");
                }
            }
            builder.addProperty(fieldName, sb.toString());
            return builder.build();
        }

        public void addValue(long weight, String value) {
            totalWeight += weight;
            model.put(totalWeight, value);
        }
    }

    private final class ObjectFieldModel extends BaseFieldModel {
        final Map<String, SimpleFieldModel> model = new HashMap<>();

        ObjectFieldModel(String fieldName) {
            super(MedlineFieldDefinition.FieldType.SINGLE_OBJECT_VALUE, fieldName);
        }

        public void addValue(String propertyName, long weight, String value) {
            if (model.containsKey(propertyName)) {
                model.get(propertyName).addValue(weight, value);
            } else {
                SimpleFieldModel propertyModel = new SimpleFieldModel(propertyName, MedlineFieldDefinition.FieldType.SINGLE_TEXT_VALUE);
                propertyModel.addValue(weight, value);
                model.put(propertyName, propertyModel);
            }
        }

        @Override
        public MedlineField selectValue(int n) {
            MedlineField.Builder builder = getNewBuilder();

            for (Map.Entry<String, SimpleFieldModel> s : model.entrySet()) {
                for (Map.Entry<String, String> kvp : s.getValue().selectValue(1).getEntries()) {
                    builder.addProperty(kvp.getKey(), kvp.getValue());
                }
            }

            return builder.build();
        }
    }
}
