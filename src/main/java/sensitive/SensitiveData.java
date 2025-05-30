package sensitive;

import orm.AbstractKeyValue;

import java.util.Objects;

public class SensitiveData extends AbstractKeyValue {

    private Sensitive type;

    private String name;

    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sensitive getType() {
        return type;
    }

    public void setType(Sensitive type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SensitiveData(Sensitive type, String name, String value) {
        this.type = type;
        this.name = name;
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

        SensitiveData that = (SensitiveData) o;

        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(value, that.value);
    }
}
