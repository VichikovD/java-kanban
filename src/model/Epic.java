package model;

import java.util.ArrayList;

public class Epic extends Task{
    ArrayList<Integer> subTasksIdList = new ArrayList<>();

    public ArrayList<Integer> getSubTasksIdList() {
        return subTasksIdList;
    }

    public void addSubtaskId(Integer subTasksId) {
        if (!subTasksIdList.contains(subTasksId)){
            this.subTasksIdList.add(subTasksId);
        }
    }


    @Override
    public String toString() {
        return "Epic{" +
                "subTasksIdList=" + subTasksIdList +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                '}';
    }
}
