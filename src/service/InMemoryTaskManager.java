package service;

import util.Managers;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int taskCounter = 1;
    private HashMap<Integer, Task> tasksMap = new HashMap<>();
    private HashMap<Integer, Subtask> subtasksMap = new HashMap<>();
    private HashMap<Integer, Epic> epicsMap = new HashMap<>();

    HistoryManager historyManager = Managers.getDefaultHistory();

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public int getNewId() {
        return taskCounter++;
    }

    @Override
    public void createTask(Task task) {
        task.setId(getNewId());
        tasksMap.put(task.getId(), task);
    }

    @Override
    public void updateTask(Task task) {
        if (!tasksMap.containsKey(task.getId())) {
            System.out.println("Error in updateTask - given task id is not in tasksMap keys");
            return;
        }
        tasksMap.put(task.getId(), task); // ���� � ���� ��� ����� id task, �� ��� �������� �� ��� ������� � ������
                                            // �������� � ���� �� ����� �����, ������ ������ Update ������������ �� �
                                            // Create.
    }

    @Override
    public void createSubtask(Subtask thatSubtask) {
        thatSubtask.setId(getNewId());
        if (!epicsMap.containsKey(thatSubtask.getEpicId())) {
            System.out.println("Error in createSubtask - Epic is not found by Epic ID. Subtask did't created.");
            return;
        }

        Subtask thisSubtask = new Subtask();
        thisSubtask.setName(thatSubtask.getName());
        thisSubtask.setDescription(thatSubtask.getDescription());
        thisSubtask.setId(thatSubtask.getId());
        thisSubtask.setEpicId(thatSubtask.getEpicId());
        thisSubtask.setStatus(thatSubtask.getStatus());
        subtasksMap.put(thisSubtask.getId(), thisSubtask);

        Epic epicOfThisSubtask = epicsMap.get(thisSubtask.getEpicId());
        epicOfThisSubtask.addSubtaskId(thisSubtask.getId());
        checkAndCorrectEpicStatus(epicOfThisSubtask.getId());
    }

    @Override
    public void updateSubtask(Subtask thatSubtask) {
        if (!subtasksMap.containsKey(thatSubtask.getId())) {
            System.out.println("Error in updateSubtask - subtask is not found by subtask ID. Subtask didn't updated.");
            return;
        }

        if (!epicsMap.containsKey(thatSubtask.getEpicId())) {
            System.out.println("Error in updateSubtask - Epic is not found by Subtask's Epic ID. Subtask did't updated.");
            return;
        }

        Subtask thisSubtask = subtasksMap.get(thatSubtask.getId());
        Epic epicOfThisSubtask = epicsMap.get(thisSubtask.getEpicId());
        Epic epicOfThatSubTask = epicsMap.get(thatSubtask.getEpicId());
        if (epicOfThisSubtask != epicOfThatSubTask) {
            epicOfThisSubtask.removeSubtaskId(thisSubtask.getId());
            checkAndCorrectEpicStatus(epicOfThisSubtask.getId());
        }

        thisSubtask.setName(thatSubtask.getName());
        thisSubtask.setDescription(thatSubtask.getDescription());
        thisSubtask.setEpicId(thatSubtask.getEpicId());
        thisSubtask.setStatus(thatSubtask.getStatus());
        subtasksMap.put(thisSubtask.getId(), thisSubtask);

        epicOfThatSubTask.addSubtaskId(thisSubtask.getId());
        checkAndCorrectEpicStatus(epicOfThatSubTask.getId());
    }

    @Override
    public void createEpic(Epic thatEpic) {
        Epic thisEpic = new Epic();
        thisEpic.setName(thatEpic.getName());
        thisEpic.setDescription(thatEpic.getDescription());
        thisEpic.setId(getNewId());
        epicsMap.put(thisEpic.getId(), thisEpic);
        thatEpic.setId(thisEpic.getId());
    }

    @Override
    public void updateEpic(Epic thatEpic) {
        Epic thisEpic = epicsMap.get(thatEpic.getId());
        if (thisEpic == null) {
            return;
        }
        thisEpic.setName(thatEpic.getName());
        thisEpic.setDescription(thatEpic.getDescription());
        epicsMap.put(thisEpic.getId(), thisEpic);
        checkAndCorrectEpicStatus(thisEpic.getId());
    }

    @Override
    public void checkAndCorrectEpicStatus(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        if (thisEpic == null) {
            System.out.println("Error in checkAndCorrectEpicStatus - epic ID is not found");
            return;
        }
        ArrayList<Integer> subtasksIdList = new ArrayList<>(thisEpic.getSubtasksIdList());
        ArrayList<Status> subtasksStatusList = new ArrayList<>();
        for (int id : subtasksIdList) {
            Subtask subTask = subtasksMap.get(id);
            Status subTaskStatus = subTask.getStatus();
            subtasksStatusList.add(subTaskStatus);
        }

        int statusNewCounter = 0;
        int statusDoneCounter = 0;
        for (Status status : subtasksStatusList) {
            switch (status) {
            case NEW:
                statusNewCounter++;
                break;
            case DONE:
                statusDoneCounter++;
                break;
            }
        }

        if ((subtasksStatusList.size() == 0) || (statusNewCounter == subtasksStatusList.size())) {
            thisEpic.setStatus(Status.NEW);
        } else if (statusDoneCounter == subtasksStatusList.size()) {
            thisEpic.setStatus(Status.DONE);
        } else {
            thisEpic.setStatus(Status.IN_PROGRESS);
        }
        epicsMap.put(thisEpic.getId(), thisEpic);
    }

    @Override
    public List<Subtask> getSubtasksListByEpicId(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        if (thisEpic == null) {
            return subtasksList;
        }

        List<Integer> subtasksIdList = thisEpic.getSubtasksIdList();
        for (int id : subtasksIdList) {
            Subtask subTask = subtasksMap.get(id);
            subtasksList.add(subTask);
        }
        return subtasksList;
    }

    @Override
    public List<Task> getAllTasks() {
        ArrayList<Task> allTasksList = new ArrayList<>(tasksMap.values());
        return allTasksList;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        ArrayList<Subtask> allSubtasksList = new ArrayList<>(subtasksMap.values());
        return allSubtasksList;
    }

    @Override
    public List<Epic> getAllEpics() {
        ArrayList<Epic> allEpicsList = new ArrayList<>(epicsMap.values());
        return allEpicsList;
    }

    @Override
    public Task getTaskById(int taskId) {
        Task task = tasksMap.get(taskId);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int subtaskId) {
        Subtask subtask = subtasksMap.get(subtaskId);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = epicsMap.get(epicId);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public void deleteTaskById(int taskId) {
        tasksMap.remove(taskId);
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        if (!subtasksMap.containsKey(subtaskId)) {
            System.out.println("Can't delete. No matches with id: " + subtaskId);
            return;
        }

        Subtask subtask = subtasksMap.get(subtaskId);
        int epicIdOfSubtask = subtask.getEpicId();
        Epic epic = epicsMap.get(epicIdOfSubtask);
        epic.removeSubtaskId(subtaskId);
        subtasksMap.remove(subtaskId);
        checkAndCorrectEpicStatus(epic.getId());
    }

    @Override
    public void deleteEpicById(int epicId) {
        if (!epicsMap.containsKey(epicId)) {
            System.out.println("Can't delete. No matches with id: " + epicId);
            return;
        }
        Epic epic = epicsMap.get(epicId);
        List<Integer> subtasksIdList = epic.getSubtasksIdList();
        for (Integer subTaskId : subtasksIdList) {
            subtasksMap.remove(subTaskId);
        }
        epicsMap.remove(epicId);
    }

    @Override
    public void deleteAllEpics() {
        epicsMap.clear();
        subtasksMap.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasksMap.clear();
        for (Epic epic : epicsMap.values()) {
            epic.clearSubtaskIdList();
            epic.setStatus(Status.NEW);
        }

    }

    @Override
    public void deleteAllTasks() {
        tasksMap.clear();
    }
}
