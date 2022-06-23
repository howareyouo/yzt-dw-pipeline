package cn.yizhi.yzt.pipeline.jdbc;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.connector.jdbc.utils.JdbcTypeUtil;
import org.apache.flink.shaded.guava30.com.google.common.base.CaseFormat;
import org.apache.flink.types.Row;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;


/**
 * POJO和Row的相互转换
 *
 * @author zjzhang
 */
public class PojoTypes<T> {
    private final Class<T> pojoClass;
    private final List<String> fields;
    private final List<String> pkFields;
    private final List<String> keyFields;
    private final LinkedHashMap<String, TypeInformation<?>> fieldTypes;

    private PojoTypes(Class<T> pojoClass) {
        this.pojoClass = pojoClass;
        this.fields = new ArrayList<>();
        this.pkFields = new ArrayList<>();
        this.keyFields = new ArrayList<>();
        this.fieldTypes = new LinkedHashMap<>();

        this.extractFieldsInfo();
    }

    public static <T> PojoTypes<T> of(Class<T> pojoClass) {
        return new PojoTypes<T>(pojoClass);
    }


    public String[] fieldNames() {
        return this.fields.toArray(new String[0]);
    }

    public Map<String, TypeInformation<?>> getFieldTypes() {
        return this.fieldTypes;
    }

    public String[] getKeyFields() {
        return this.keyFields.toArray(new String[0]);
    }

    public String[] getPkFields() {
        return this.pkFields.toArray(new String[0]);
    }

    public int[] jdbcTypesArray() {
        List<Integer> types = new ArrayList<>();

        this.fieldTypes.forEach((name, fieldType) -> {
            // internal API, may change
            types.add(JdbcTypeUtil.typeInformationToSqlType(fieldType));
        });

        return types.stream().mapToInt(i -> i).toArray();
    }


    public int[] jdbcKeyTypesArray() {
        List<Integer> types = new ArrayList<>();

        this.keyFields.forEach(name -> {
            // internal API, may change
            TypeInformation<?> fieldType = this.fieldTypes.get(name);
            types.add(JdbcTypeUtil.typeInformationToSqlType(fieldType));
        });

        return types.stream().mapToInt(i -> i).toArray();
    }

    public RowTypeInfo toRowTypeInfo() {
        List<TypeInformation<?>> types = new ArrayList<>();
        this.fields.forEach(f -> {
            types.add(this.fieldTypes.get(f));
        });

        RowTypeInfo rowTypeInfo = new RowTypeInfo(types.toArray(new TypeInformation<?>[0]), fieldNames());

        //System.out.println("row type info for " + this.pojoClass.getName());
        //System.out.println(rowTypeInfo);

        return rowTypeInfo;
    }

    public Row keyFieldRow(Row row) {
        Row r = new Row(keyFields.size());

        for (int i = 0; i < this.keyFields.size(); i++) {
            int j = this.fields.indexOf(this.keyFields.get(i));

            r.setField(i, row.getField(j));
        }

        return r;
    }

    public Row toRow(T pojo) throws Exception {
        Row row = new Row(this.fields.size());
        // Field[] pojoFields = this.pojoClass.getDeclaredFields();
        List<Field> pojoFields = getAllFields(this.pojoClass);
        for (int i = 0; i < this.fields.size(); i++) {
            String mappedName = this.fields.get(i);

            Field foundField = pojoFields.stream()
                .filter(field -> {
                    String convertedName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());

                    return convertedName.equals(mappedName);
                }).findFirst().orElse(null);

            if (foundField == null) {
                row.setField(i, null);
                continue;
            }

            boolean isAccessible = foundField.isAccessible();
            if (!isAccessible) {
                foundField.setAccessible(true);
            }

            row.setField(i, foundField.get(pojo));
            foundField.setAccessible(isAccessible);
        }

        return row;
    }

    public T fromRow(Row row) throws Exception {
        T ins = this.pojoClass.getDeclaredConstructor().newInstance();

        List<Field> pojoFields = getAllFields(this.pojoClass);
        for (Field field : pojoFields) {
            boolean isAccessible = field.canAccess(ins);
            if (!isAccessible) {
                field.setAccessible(true);
            }

            String jdbcFieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
            int rowIndex = this.fields.indexOf(jdbcFieldName);

            field.set(ins, row.getField(rowIndex));
            field.setAccessible(isAccessible);
        }

        return ins;
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private void extractFieldsInfo() {
        List<Field> pojoFields = getAllFields(this.pojoClass);
        // 保证稳定的字段顺序输出
        Collections.sort(pojoFields, Comparator.comparing(Field::getName));

        for (Field field : pojoFields) {
            String mappedName;

            //忽悠这个字段写入库
            Ignore ignore = field.getAnnotation(Ignore.class);
            if (ignore != null) {
                continue;
            }


            Column annotation = field.getAnnotation(Column.class);
            if (annotation != null) {
                mappedName = annotation.name();
                Column.FieldType fieldType = annotation.type();
                switch (fieldType) {
                    case NORMAL:
                        fields.add(mappedName);
                        break;
                    case PK:
                        pkFields.add(mappedName);
                        fields.add(mappedName);
                        break;
                    case KEY:
                        keyFields.add(mappedName);
                        fields.add(mappedName);
                        break;
                    default:
                        fields.add(mappedName);
                }
            } else {
                mappedName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
                this.fields.add(mappedName);
            }

            Class fieldClass = field.getType();
            if (String.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.STRING);
            } else if (Integer.class.equals(fieldClass) || int.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.INT);
            } else if (Long.class.equals(fieldClass) || long.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.BIG_INT);
            } else if (Short.class.equals(fieldClass) || short.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.SHORT);
            } else if (BigDecimal.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.BIG_DEC);
            } else if (Boolean.class.equals(fieldClass) || boolean.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.BOOLEAN);
            } else if (Timestamp.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.SQL_TIMESTAMP);
            } else if (Date.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.SQL_DATE);
            } else if (Time.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.SQL_TIME);
            } else if (Float.class.equals(fieldClass) || float.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.FLOAT);
            } else if (Double.class.equals(fieldClass) || double.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.DOUBLE);
            } else if (Instant.class.equals(fieldClass)) {
                fieldTypes.put(mappedName, Types.INSTANT);
            } else {
                throw new RuntimeException("不支持的类型: " + fieldClass);
            }
        }
    }


}
