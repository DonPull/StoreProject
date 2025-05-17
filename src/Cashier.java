import java.io.Serializable;

public class Cashier implements Serializable{
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String name;
    private final double salary;

    public Cashier(String id, String name, double salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getSalary() { return salary; }
}