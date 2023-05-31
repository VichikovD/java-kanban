package model;

import java.util.ArrayList;

public class Epic extends Task{
    private ArrayList<Integer> subtasksIdList = new ArrayList<>();

    public ArrayList<Integer> getSubtasksIdList() {
        return subtasksIdList;
    }

    public void addSubtaskId(Integer subTasksId) {
        if (!subtasksIdList.contains(subTasksId)){
            this.subtasksIdList.add(subTasksId);
        }
    }

    public void removeSubtaskId(Integer subTasksId) {
        if (subtasksIdList.contains(subTasksId)){
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
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                '}';
    }
}
