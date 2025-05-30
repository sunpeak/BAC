package bac;

import extension.MyExtension;
import orm.AbstractKeyValue;

import java.util.ArrayList;
import java.util.List;

public class BacUtils {

    public static List<AbstractKeyValue> genList4Str(String encodedUrl) {
        List<AbstractKeyValue> list = new ArrayList<>();
        String[] keyValues = encodedUrl.split("&");
        for (String kv : keyValues) {
            String[] kvs = kv.split("=");
            if (kvs.length >= 2) {
                String key = kvs[0];
                String value = kvs[1];
                list.add(new BacData(key, value));
            }
        }
        return list;
    }


    public static BacData collect(String field, String value) {
        String lowerCaseField = field.toLowerCase();
        if (MyExtension.DelFields.stream().anyMatch(lowerCaseField::equals)) {
            return null;
        }
        if (MyExtension.DefaultFields.stream().anyMatch(lowerCaseField::endsWith)) {
            if (matchBac(value)) {
                return new BacData(field, value);
            }
        }
        return null;
    }

    private static boolean matchBac(String str) {
        return str.length() >= 2 && str.length() <= 20 && Character.isDigit(str.charAt(str.length() - 2)) && Character.isDigit(str.charAt(str.length() - 1));
    }
}
