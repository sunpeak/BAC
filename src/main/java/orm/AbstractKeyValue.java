package orm;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractKeyValue {

    public abstract String getName();

    public abstract String getValue();

    public abstract String toString();

    public abstract boolean equals(Object o);

    public static <T extends AbstractKeyValue> String genStr4List(List<T> list) {
        List<AbstractKeyValue> kvs = list.stream().distinct().sorted(Comparator.comparing(AbstractKeyValue::toString)).collect(Collectors.toList());

        StringBuilder encodedUrl = new StringBuilder();
        for (AbstractKeyValue entry : kvs) {
            if (encodedUrl.length() > 0) {
                encodedUrl.append("&");
            }
            encodedUrl.append(entry.toString());
        }
        return encodedUrl.toString();
    }

}
