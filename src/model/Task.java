package model;

import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Integer id;
    private Status status;
    TasksType tasksType;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Task() {
        tasksType = TasksType.TASK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                '}';
    }

    public String toString(Task task) {
        return String.format("%s,%s,%s,%s,%s", id, tasksType.toString(), name, status, description);
    }

    public static Task taskFromStringArray(String[] data) {
        Task task = new Task();

        task.setId(Integer.parseInt(data[0]));
        task.setName(data[2]);
        task.setStatus(Status.getStatusByString(data[3]));
        task.setDescription(data[4]);

        return task;
    }
}
