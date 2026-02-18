package lablabu.model;

public class Category {
    private String name;
    private String color; // Hex color for visualization

    public Category(String name) {
        this.name = name;
        this.color = "#65f7a1"; // Default green
    }

    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
