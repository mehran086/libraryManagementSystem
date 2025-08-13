package library.domain;

import java.util.Objects;

public abstract class User {
    private static int counter = 2000;
    private final String id;
    private String name;
    public abstract int getBorrowLimit();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User(String name) {
        this.id = generateId();
        this.name = name;
    }
    private static String generateId() {
        return "USR-" + (counter++);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
