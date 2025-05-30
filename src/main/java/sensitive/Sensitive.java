package sensitive;

public enum Sensitive {

    Name,
    Phone,
    IdCard,
    BankCard,
    Amount,
    IdAddres;

    public boolean isIdType() {
        return Phone.equals(Sensitive.valueOf(this.name())) ||
                IdCard.equals(Sensitive.valueOf(this.name())) ||
                BankCard.equals(Sensitive.valueOf(this.name()));
    }
}
