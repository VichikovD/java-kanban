package model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class Task {
    protected String name;
    protected String description;
    protected Integer id;
    protected Status status;
    protected Instant startTime;
    protected long durationInMinutes;
    protected Integer epicId;

    protected TasksType tasksType;

    public Task() {
        tasksType = TasksType.TASK;
    }

    public Task(Integer id, String name, Status status, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        startTime = null;
        durationInMinutes = 0;
        epicId = null;
        this.tasksType = TasksType.TASK;
    }

    public Task(String name, Status status, String description, Instant startTime, long durationInMinutes) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.durationInMinutes = durationInMinutes;
        this.epicId = null;
        this.tasksType = TasksType.TASK;
    }

    public Task(Integer id, String name, Status status, String description, Instant startTime, long durationInMinutes) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.durationInMinutes = durationInMinutes;
        this.epicId = null;
        this.tasksType = TasksType.TASK;
    }
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }
    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public long getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(long durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }
    public Instant getEndTime() {
        Duration duration = Duration.ofMinutes(durationInMinutes);
        Instant endTime = startTime.plus(duration);
        return endTime;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public TasksType getTasksType() {
        return tasksType;
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

    public void setEpicId(int epicId) {
        this.epicId = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return durationInMinutes == task.durationInMinutes && Objects.equals(name, task.name) && Objects.equals(description, task.description) && Objects.equals(id, task.id) && status == task.status && Objects.equals(startTime, task.startTime) && Objects.equals(epicId, task.epicId) && tasksType == task.tasksType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status, startTime, durationInMinutes, epicId, tasksType);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", type='" + tasksType + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", startTime='" + startTime +
                ", durationInMinutes='" + durationInMinutes +
                '}';
    }
}