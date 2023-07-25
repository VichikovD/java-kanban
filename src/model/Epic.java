package model;

import java.util.List;
import java.util.ArrayList;

public class Epic extends Task {
    private List<Integer> subtasksIdList = new ArrayList<>();

    public Epic() {
        super();
        setStatus(Status.NEW);
        tasksType = TasksType.EPIC;
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

    public static Epic epicFromStringArray(String[] data) {
        Epic epic = new Epic();

        epic.setId(Integer.parseInt(data[0]));
        epic.setName(data[2]);
        epic.setStatus(Status.getStatusByString(data[3]));
        epic.setDescription(data[4]);

        return epic;
    }


}
