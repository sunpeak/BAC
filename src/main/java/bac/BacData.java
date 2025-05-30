package bac;

import orm.AbstractKeyValue;

import java.util.Objects;

public class BacData extends AbstractKeyValue {
    private String name;

    private String value;

    public BacData(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BacData bacData = (BacData) o;

        if (!Objects.equals(name, bacData.name)) return false;
        return Objects.equals(value, bacData.value);
    }
}
