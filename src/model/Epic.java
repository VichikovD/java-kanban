package model;

import java.util.List;
import java.util.ArrayList;

public class Epic extends Task {
    private List<Integer> subtasksIdList = new ArrayList<>();

    public Epic() {
        super();
        setStatus(Status.NEW);
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
    public String toString() {
        return "Epic{" +
                "subtasksIdList=" + subtasksIdList +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
