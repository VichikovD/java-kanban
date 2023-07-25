package model;

public enum TasksType {
    TASK("TASK"),
    SUBTASK("SUBTASK"),
    EPIC("EPIC");

    private final String type;

    TasksType (String type) {
        this.type = type;
    }
    /*@Override
    public String toString() {
        return getStringByType();
    }

    public String getStringByType() {
        return type;
    }*/
    public static TasksType getTypeByString(String value) {
        for (TasksType type : values()) {
            if (type.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
