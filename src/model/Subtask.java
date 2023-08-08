package model;

import java.time.Instant;

public class Subtask extends Task {
    public Subtask() {
        super();
        tasksType = TasksType.SUBTASK;
    }

    public Subtask(Integer id, String name, Status status, String description, Instant startTime, long durationInMinutes, Integer epicId) {
        super(id, name, status, description, startTime, durationInMinutes);
        this.tasksType = TasksType.SUBTASK;
        this.epicId = epicId;
    }

    public Subtask(String name, Status status, String description, Instant startTime, long durationInMinutes, Integer epicId) {
        super(name, status, description, startTime, durationInMinutes);
        this.tasksType = TasksType.SUBTASK;
        this.epicId = epicId;
    }

    @Override
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + id +
                ", type='" + tasksType + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", startTime='" + startTime +
                ", durationInMinutes='" + durationInMinutes +
                '}';
    }
}
