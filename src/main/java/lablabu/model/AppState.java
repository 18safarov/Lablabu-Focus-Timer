package lablabu.model;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private List<Category> categories = new ArrayList<>();
    private List<Session> sessions = new ArrayList<>();
    private int streak = 0;
    private String lastSessionDate = "";
    private String theme = "dark";
    private String selectedCategory = "";

    // Custom Timer settings (in minutes)
    private int pomodoroWorkMinutes = 30;
    private int pomodoroBreakMinutes = 10;

    public AppState() {
        categories.add(new Category("English", "#79f5b0"));
        categories.add(new Category("Coding", "#65f7a1"));
        categories.add(new Category("AI", "#9feacb"));
        categories.add(new Category("Math", "#ffb86b"));
    }

    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }

    public List<Session> getSessions() { return sessions; }
    public void setSessions(List<Session> sessions) { this.sessions = sessions; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public String getLastSessionDate() { return lastSessionDate; }
    public void setLastSessionDate(String lastSessionDate) { this.lastSessionDate = lastSessionDate; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getSelectedCategory() { return selectedCategory; }
    public void setSelectedCategory(String selectedCategory) { this.selectedCategory = selectedCategory; }

    public int getPomodoroWorkMinutes() { return pomodoroWorkMinutes; }
    public void setPomodoroWorkMinutes(int minutes) { this.pomodoroWorkMinutes = minutes; }

    public int getPomodoroBreakMinutes() { return pomodoroBreakMinutes; }
    public void setPomodoroBreakMinutes(int minutes) { this.pomodoroBreakMinutes = minutes; }

    public void addCategory(Category category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    public void removeCategory(String categoryName) {
        categories.removeIf(c -> c.getName().equals(categoryName));
    }

    public Category getCategoryByName(String name) {
        return categories.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
