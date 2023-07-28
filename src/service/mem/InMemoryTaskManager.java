package service.mem;

import service.HistoryManager;
import service.TaskManager;
import util.Managers;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int taskCounter;
    protected final HashMap<Integer, Task> tasksMap;
    protected final HashMap<Integer, Subtask> subtasksMap;
    protected final HashMap<Integer, Epic> epicsMap;

    protected HistoryManager historyManager = Managers.getDefaultHistory();

    public InMemoryTaskManager() {
        this.taskCounter = 1;
        this.tasksMap = new HashMap<>();
        this.subtasksMap = new HashMap<>();
        this.epicsMap = new HashMap<>();
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


    public int getNewId() {
        return taskCounter++;
    }
    protected void setTaskCounter(int num) {
        this.taskCounter = num + 1;
    }

    @Override
    public void createTask(Task thatTask) {
        thatTask.setId(getNewId());
        Task thisTask = new Task();
        thisTask.setDescription(thatTask.getDescription());
        thisTask.setStatus(thatTask.getStatus());
        thisTask.setId(thatTask.getId());
        thisTask.setName(thatTask.getName());
        tasksMap.put(thisTask.getId(), thisTask);
    }

    @Override
    public void updateTask(Task thatTask) {
        Task thisTask = tasksMap.get(thatTask.getId());
        if (thisTask == null) {
            System.out.println("Error in updateTask - given task id is not in tasksMap keys");
            return;
        }
        thisTask.setName(thatTask.getName());
        thisTask.setDescription(thatTask.getDescription());
        thisTask.setStatus(thatTask.getStatus());
        tasksMap.put(thisTask.getId(), thisTask);
    }

    @Override
    public void createSubtask(Subtask thatSubtask) {
        thatSubtask.setId(getNewId());
        Epic epicOfThatSubtask = epicsMap.get(thatSubtask.getEpicId());
        if (epicOfThatSubtask == null) {
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

        epicOfThatSubtask.addSubtaskId(thisSubtask.getId());
        updateEpicStatus(thisSubtask.getEpicId());
    }

    @Override
    public void updateSubtask(Subtask thatSubtask) {
        Subtask thisSubtask = subtasksMap.get(thatSubtask.getId());
        if (thisSubtask == null) {
            System.out.println("Error in updateSubtask - subtask is not found by subtask ID. Subtask didn't updated.");
            return;
        }

        Epic epicOfThatSubTask = epicsMap.get(thatSubtask.getEpicId());
        if (epicOfThatSubTask == null) {
            System.out.println("Error in updateSubtask - Epic is not found by Subtask's Epic ID. Subtask did't updated.");
            return;
        }

        Epic epicOfThisSubtask = epicsMap.get(thisSubtask.getEpicId());
        if (epicOfThisSubtask != epicOfThatSubTask) {
            epicOfThisSubtask.removeSubtaskId(thisSubtask.getId());
            updateEpicStatus(epicOfThisSubtask.getId());
        }

        thisSubtask.setName(thatSubtask.getName());
        thisSubtask.setDescription(thatSubtask.getDescription());
        thisSubtask.setEpicId(thatSubtask.getEpicId());
        thisSubtask.setStatus(thatSubtask.getStatus());
        subtasksMap.put(thisSubtask.getId(), thisSubtask);

        epicOfThatSubTask.addSubtaskId(thisSubtask.getId());
        updateEpicStatus(epicOfThatSubTask.getId());
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
        updateEpicStatus(thisEpic.getId());
    }

    protected void updateEpicStatus(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        if (thisEpic == null) {
            System.out.println("Error in updateEpicStatus - epic ID is not found");
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
        if (tasksMap.remove(taskId) == null) {
            System.out.println("Can't delete. No matches with id: " + taskId);
            return;
        }
        historyManager.remove(taskId);
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        Subtask subtask = subtasksMap.remove(subtaskId);
        if (subtask == null) {
            System.out.println("Can't delete. No matches with id: " + subtaskId);
            return;
        }

        int epicIdOfSubtask = subtask.getEpicId();
        Epic epic = epicsMap.get(epicIdOfSubtask);
        epic.removeSubtaskId(subtaskId);
        updateEpicStatus(epic.getId());
        historyManager.remove(subtaskId);

    }

    @Override
    public void deleteEpicById(int epicId) {
        Epic epic = epicsMap.remove(epicId);
        if (epic == null) {
            System.out.println("Can't delete. No matches with id: " + epicId);
            return;
        }
        List<Integer> subtasksIdList = epic.getSubtasksIdList();
        for (Integer subTaskId : subtasksIdList) {
            historyManager.remove(subTaskId);
            subtasksMap.remove(subTaskId);
        }

        historyManager.remove(epicId);

    }

    @Override
    public void deleteAllEpics() {
        for (int epicId : epicsMap.keySet()) {
            historyManager.remove(epicId);
        }
        epicsMap.clear();
        for (int subtaskId : subtasksMap.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasksMap.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (int subtaskId : subtasksMap.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasksMap.clear();
        for (Epic epic : epicsMap.values()) {
            epic.clearSubtaskIdList();
            epic.setStatus(Status.NEW);
        }

    }

    @Override
    public void deleteAllTasks() {
        for (int taskId : tasksMap.keySet()) {
            historyManager.remove(taskId);
        }
        tasksMap.clear();
    }
}
