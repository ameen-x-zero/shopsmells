package shop;

public class Address {
    private final String street;
    private final String city;
    private final String zip;

    public Address(String street, String city, String zip) {
        this.street = street;
        this.city = city;
        this.zip = zip;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getZip() {
        return zip;
    }

    public boolean isComplete() {
        return !StringUtil.isBlank(street) && !StringUtil.isBlank(city) && !StringUtil.isBlank(zip);
    }
}
