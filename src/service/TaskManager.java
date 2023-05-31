package service;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int taskCounter = 1;
    private HashMap<Integer, Task> tasksMap = new HashMap<>();
    private HashMap<Integer, Subtask> subtasksMap = new HashMap<>();
    private HashMap<Integer, Epic> epicsMap = new HashMap<>();

    public int getNewId() {
        return taskCounter++;
    }

    public void createTask(Task task) {
        task.setId(getNewId());
        tasksMap.put(task.getId(), task);
    }

    public void updateTask(Task task) {    //  Я просто подумал, что проверка на ID не нужна, т.к. в условии  было "Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра."
        if (tasksMap.containsKey(task.getId())) {
            tasksMap.put(task.getId(), task);
        }
    }

    public void createSubtask(Subtask subtask) {
        subtask.setId(getNewId());
        if (!epicsMap.containsKey(subtask.getEpicId())) {
            System.out.println("Подзадача не создана, т.к. не существет указанный в ней Эпик.");
        } else {
            subtasksMap.put(subtask.getId(), subtask);
            Epic epicOfThisSubtask = epicsMap.get(subtask.getEpicId());
            epicOfThisSubtask.addSubtaskId(subtask.getId());
            checkAndCorrectEpicStatus(epicOfThisSubtask.getId());
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasksMap.containsKey(subtask.getId())) {
            if (!epicsMap.containsKey(subtask.getEpicId())) {
                System.out.println("Подзадача не обновлена, т.к. не существет указанный в ней Эпик.");
            } else {
                subtasksMap.put(subtask.getId(), subtask);
                Epic epicOfThisSubTask = epicsMap.get(subtask.getEpicId());
                epicOfThisSubTask.addSubtaskId(subtask.getId());
                checkAndCorrectEpicStatus(epicOfThisSubTask.getId());
            }
        } else {
            System.out.println("Подзадача не обновлена, т.к. под данным ID нет подзадачи.");
        }
    }

    public void createEpic(Epic thatEpic) {
        Epic thisEpic = new Epic();
        thisEpic.setName(thatEpic.getName());
        thisEpic.setDescription(thatEpic.getDescription());
        thisEpic.setId(getNewId());
        epicsMap.put(thisEpic.getId(), thisEpic);
        thatEpic.setId(thisEpic.getId());
        checkAndCorrectEpicStatus(thisEpic.getId());
    }

    public void updateEpic(Epic thatEpic) {
        if (epicsMap.containsKey(thatEpic.getId())) {
            Epic thisEpic = epicsMap.get(thatEpic.getId());
            thisEpic.setName(thatEpic.getName());
            thisEpic.setDescription(thatEpic.getDescription());
            epicsMap.put(thisEpic.getId(), thisEpic);
            checkAndCorrectEpicStatus(thisEpic.getId());
        }
    }

    public void checkAndCorrectEpicStatus(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        ArrayList<Integer> subtasksIdList = new ArrayList<>();
        ArrayList<String> subtasksStatusList = new ArrayList<>();
        if (thisEpic.getSubtasksIdList() != null) {
            subtasksIdList = thisEpic.getSubtasksIdList();
        }

        for (int id : subtasksIdList) {
            Subtask subTask = subtasksMap.get(id);
            String subTaskStatus = subTask.getStatus();
            subtasksStatusList.add(subTaskStatus);
        }

        int statusNewCounter = 0;
        int statusDoneCounter = 0;
        for (String status : subtasksStatusList) {
            switch (status) {
                case ("NEW"):
                    statusNewCounter++;
                    break;
                case ("DONE") :
                    statusDoneCounter++;
                    break;
            }
        }

        if ((subtasksStatusList.size() == 0) || (statusNewCounter == subtasksStatusList.size())) {
            thisEpic.setStatus("NEW");
        } else if (statusDoneCounter == subtasksStatusList.size()) {
            thisEpic.setStatus("DONE");
        } else {
            thisEpic.setStatus("IN_PROGRESS");
        }
        epicsMap.put(thisEpic.getId(), thisEpic);
    }

    public List<Subtask> getSubtasksListByEpicId(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        if (thisEpic != null) {
            ArrayList<Integer> subtasksIdList = thisEpic.getSubtasksIdList();
            for (int id : subtasksIdList) {
                Subtask subTask = subtasksMap.get(id);
                subtasksList.add(subTask);
            }
        }

        return subtasksList;
    }

    public List<Task> getAllTasks() {
        ArrayList<Task> allTasksList = new ArrayList<>();
        for (Task task : tasksMap.values()) {
            allTasksList.add(task);
        }
        return allTasksList;
    }
    public List<Subtask> getAllSubtasks() {
        ArrayList<Subtask> allSubtasksList = new ArrayList<>();
        for (Subtask subTask : subtasksMap.values()) {
            allSubtasksList.add(subTask);
        }
        return allSubtasksList;
    }

    public List<Epic> getAllEpics() {
        ArrayList<Epic> allEpicsList = new ArrayList<>();
        for (Epic epic : epicsMap.values()) {
            allEpicsList.add(epic);
        }
        return allEpicsList;
    }

    public Task getTaskById(int taskId) {
        return tasksMap.get(taskId);
    }

    public Subtask getSubtaskById(int subtaskId) {
        return subtasksMap.get(subtaskId);
    }

    public Epic getEpicById(int epicId) {
        return epicsMap.get(epicId);
    }

    public void deleteTaskById(int taskId) {
        tasksMap.remove(taskId);
    }

    public void deleteSubtaskById(int subtaskId) {
        if (subtasksMap.containsKey(subtaskId)) {
            Subtask subtask = subtasksMap.get(subtaskId);
            int epicIdOfSubtask = subtask.getEpicId();
            Epic epic = epicsMap.get(epicIdOfSubtask);
            epic.removeSubtaskId(subtaskId);
            subtasksMap.remove(subtaskId);
            checkAndCorrectEpicStatus(epic.getId());
        } else {
            System.out.println("Can't delete. No matches with id: " + subtaskId);
        }
    }

    public void deleteEpicById(int epicId) {
        if (epicsMap.containsKey(epicId)) {
            Epic epic = epicsMap.get(epicId);
            ArrayList<Integer> subtasksIdList = epic.getSubtasksIdList();
            for (Integer subTaskId : subtasksIdList) {    //  Если у сабтасков не может быть несколько эпиков, это работает.
                subtasksMap.remove(subTaskId);
            }
            epicsMap.remove(epicId);
        } else {
            System.out.println("Can't delete. No matches with id: " + epicId);
        }
    }

    public void deleteAllEpics(){
        epicsMap.clear();
        subtasksMap.clear();
    }

    public void deleteAllSubtasks() {
        subtasksMap.clear();
        for (Epic epic : epicsMap.values()) {
            epic.clearSubtaskIdList();
            checkAndCorrectEpicStatus(epic.getId());
        }

    }

    public void deleteAllTasks() {
        tasksMap.clear();
    }
}
