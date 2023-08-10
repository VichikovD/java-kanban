package model;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtasksIdList;
    protected Instant endTime;


    public Epic() {
        super();
        setStatus(Status.NEW);
        tasksType = TasksType.EPIC;
        this.subtasksIdList = new ArrayList<>();
    }

    public Epic(int id, String name, String description) {
        super(id, name, Status.NEW, description);
        this.tasksType = TasksType.EPIC;
        this.subtasksIdList = new ArrayList<>();
        this.endTime = null;
    }

    public Epic(Integer id, String name, Status status, String description, Instant startTime, long durationInMinutes) {
        super(id, name, status, description, startTime, durationInMinutes);
        this.tasksType = TasksType.EPIC;
        this.subtasksIdList = new ArrayList<>();
    }

    public List<Integer> getSubtasksIdList() {
        return subtasksIdList;
    }

    public void addSubtaskId(Integer subTasksId) {
        if (!subtasksIdList.contains(subTasksId)) {
            this.subtasksIdList.add(subTasksId);
        }
    }

    public void removeSubtaskId(Integer subTasksId) {
        if (subtasksIdList.contains(subTasksId)) {
            this.subtasksIdList.remove(subTasksId);
        }
    }

    public void clearSubtaskIdList() {
        subtasksIdList.clear();
    }

    @Override
    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtasksIdList=" + subtasksIdList +
                ", id=" + id +
                ", type='" + tasksType + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status +
                ", description='" + description + '\'' +
                ", startTime='" + startTime +
                ", durationInMinutes='" + durationInMinutes +
                ", endTime='" + endTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasksIdList, epic.subtasksIdList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasksIdList);
    }
}
