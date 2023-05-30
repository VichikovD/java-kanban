package service;
import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    static int taskCounter = 1;
    Task task = new Task();
    SubTask subTask = new SubTask();
    Epic epic = new Epic();
    public HashMap<Integer, Task> tasksMap = new HashMap<>();
    public HashMap<Integer, SubTask> subTasksMap = new HashMap<>();
    public HashMap<Integer, Epic> epicsMap = new HashMap<>();

    public int getNewId() {
        return taskCounter++;
    }

    public void createTask(Task task) {
        task.setId(getNewId());
        updateTask(task);
    }

    public void updateTask(Task task) {
        if (task.getClass().equals(this.task.getClass())) {
            tasksMap.put(task.getId(), task);
        } else if (task.getClass().equals(this.subTask.getClass())) {
            SubTask newSubtask = (SubTask) task;
            if (!epicsMap.containsKey(newSubtask.getEpicId())) {
                System.out.println("Подзадача не обновлена, т.к. не существет указанный в ней Эпик.");
            } else {
                subTasksMap.put(newSubtask.getId(), newSubtask);
                Epic epicOfThisSubTask = epicsMap.get(newSubtask.getEpicId());
                epicOfThisSubTask.addSubtaskId(newSubtask.getId());
                checkAndCorrectEpicStatus(epicOfThisSubTask.getId());
            }
        } else if (task.getClass().equals(this.epic.getClass())) {
            epicsMap.put(task.getId(), (Epic) task);
            checkAndCorrectEpicStatus(task.getId());
        }
    }

    public void checkAndCorrectEpicStatus(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        ArrayList<Integer> subTasksIdList = new ArrayList<>();
        ArrayList<String> subTasksStatusList = new ArrayList<>();
        if (thisEpic.getSubTasksIdList() != null) {
            subTasksIdList = thisEpic.getSubTasksIdList();
        }

        for (int id : subTasksIdList) {
            SubTask subTask = subTasksMap.get(id);
            String subTaskStatus = subTask.getStatus();
            subTasksStatusList.add(subTaskStatus);
        }

        int statusNewCounter = 0;
        int statusDoneCounter = 0;
        for (String status : subTasksStatusList) {
            switch (status) {
                case ("NEW"):
                    statusNewCounter++;
                    break;
                case ("DONE") :
                    statusDoneCounter++;
                    break;
            }
        }

        if ((subTasksStatusList.size() == 0) || (statusNewCounter == subTasksStatusList.size())) {
            thisEpic.setStatus("NEW");
        } else if (statusDoneCounter == subTasksStatusList.size()) {
            thisEpic.setStatus("DONE");
        } else {
            thisEpic.setStatus("IN_PROGRESS");
        }
    }

    public List<SubTask> getSubTasksListByEpicId(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        ArrayList<SubTask> subTasksList = new ArrayList<>();
        if (thisEpic != null) {
            ArrayList<Integer> subTasksIdList = thisEpic.getSubTasksIdList();
            for (int id : subTasksIdList) {
                SubTask subTask = subTasksMap.get(id);
                subTasksList.add(subTask);
            }
        }

        return subTasksList;
    }

    public List getAllTasksList() {
        ArrayList<Task> allTasksList = new ArrayList<>();
        for (Task task : tasksMap.values()) {
            allTasksList.add(task);
        }
        for (SubTask subTask : subTasksMap.values()) {
            allTasksList.add(subTask);
        }
        for (Epic epic : epicsMap.values()) {
            allTasksList.add(epic);
        }

        return allTasksList;
    }

    public void clearTaskTracker() {
        tasksMap.clear();
        subTasksMap.clear();
        epicsMap.clear();
    }

    public Task getTaskById(int taskId) {
        if (tasksMap.containsKey(taskId)) {
            return tasksMap.get(taskId);
        } else if (subTasksMap.containsKey(taskId)) {
            return subTasksMap.get(taskId);
        } else if (epicsMap.containsKey(taskId)) {
            return epicsMap.get(taskId);
        } else {
            System.out.println("No matches with id: " + taskId);
            return new Task();
        }
    }

    public void deleteTaskById(int taskId) {
        if (tasksMap.containsKey(taskId)) {
            tasksMap.remove(taskId);
        } else if (subTasksMap.containsKey(taskId)) {
            SubTask subTask = subTasksMap.get(taskId);
            int epicIdOfSubTask = subTask.getEpicId();
            Epic epic = epicsMap.get(epicIdOfSubTask);
            ArrayList<Integer> subTasksIdList = epic.getSubTasksIdList();
            subTasksIdList.remove(subTasksIdList.indexOf(taskId));
            subTasksMap.remove(taskId);
        } else if (epicsMap.containsKey(taskId)) {
            Epic epic = epicsMap.get(taskId);
            ArrayList<Integer> subTasksIdList = epic.getSubTasksIdList();
            for (Integer subTaskId : subTasksIdList) {
                subTasksMap.remove(subTaskId);
            }
            epicsMap.remove(taskId);
        } else {
            System.out.println("No matches with id: " + taskId);
        }
    }
}
