package service.mem;

import service.HistoryManager;
import service.TaskManager;
import service.mem.exception.NotFoundException;
import util.Managers;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int taskCounter;
    protected final HashMap<Integer, Task> tasksMap;
    protected final HashMap<Integer, Subtask> subtasksMap;
    protected final HashMap<Integer, Epic> epicsMap;
    protected final TreeSet<Task> prioritizedTasksSet;
    protected HistoryManager historyManager = Managers.getDefaultHistory();

    public boolean isTimeIntersection(Task taskToValidate) {
        Instant toValidateStartTime = taskToValidate.getStartTime();
        if (toValidateStartTime == null) {
            return false;
        }
        Instant toValidateEndTime = taskToValidate.getEndTime();

        for (Task task : getPrioritizedTasks()) {
            if (task.getStartTime() == null || task.getId() == taskToValidate.getId()) {
                continue;
            }
            Instant startTime = task.getStartTime();
            Instant endTime = task.getEndTime();
            if (toValidateStartTime.isAfter(endTime) || toValidateEndTime.isBefore(startTime)) {
                continue;
            } else {
                return true;
            }
        }
        return false;
    }

    public InMemoryTaskManager() {
        this.taskCounter = 1;
        this.tasksMap = new HashMap<>();
        this.subtasksMap = new HashMap<>();
        this.epicsMap = new HashMap<>();
        this.prioritizedTasksSet = new TreeSet<>(new StartTimeComparator());
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasksSet);
    }

    @Override
    public boolean containsTask(int id) {
        return tasksMap.containsKey(id);
    }

    @Override
    public boolean containsSubtask(int id) {
        return subtasksMap.containsKey(id);
    }

    @Override
    public boolean containsEpic(int id) {
        return epicsMap.containsKey(id);

    }

    public static class StartTimeComparator implements Comparator<Task> {

        @Override
        public int compare(Task o1, Task o2) {
            Instant instant1 = o1.getStartTime();
            Instant instant2 = o2.getStartTime();
            if (instant1 == null && instant2 == null) {
                return o1.getId() - o2.getId();
            } else if (instant1 == null) {
                return 1;
            } else if (instant2 == null) {
                return -1;
            } else {
                return instant1.compareTo(instant2);
            }
        }
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
    public Task createTask(Task thatTask) {
        if (thatTask == null) {
            throw new IllegalArgumentException("Task and cannot be null");
        }
        thatTask.setId(getNewId());
        if (isTimeIntersection(thatTask)) {
            throw new IllegalArgumentException("Task is not created - execution time cannot coincide with other tasks");
        }


        Task thisTask = new Task(
                thatTask.getId(),
                thatTask.getName(),
                thatTask.getStatus(),
                thatTask.getDescription(),
                thatTask.getStartTime(),
                thatTask.getDurationInMinutes()
        );
        tasksMap.put(thisTask.getId(), thisTask);
        prioritizedTasksSet.add(thisTask);
        return thisTask;
    }

    @Override
    public Task updateTask(Task thatTask) {
        int commonId = thatTask.getId();
        Task thisTask = tasksMap.get(commonId);
        if (thisTask == null) {
            throw new NotFoundException(String.format("Error in createTask - Task is not found by ID: %d." +
                    " Task is not created.", commonId));
        }

        if (isTimeIntersection(thatTask)) {
            throw new IllegalArgumentException("Task is not created - execution time cannot coincide with other tasks");
        }
        prioritizedTasksSet.remove(thisTask);
        thisTask.setName(thatTask.getName());
        thisTask.setDescription(thatTask.getDescription());
        thisTask.setStatus(thatTask.getStatus());
        thisTask.setStartTime(thatTask.getStartTime());
        thisTask.setDurationInMinutes(thatTask.getDurationInMinutes());
        tasksMap.put(commonId, thisTask);
        prioritizedTasksSet.add(thisTask);
        return thisTask;
    }

    @Override
    public Subtask createSubtask(Subtask thatSubtask) {
        int epicId = thatSubtask.getEpicId();
        Epic commonEpic = epicsMap.get(epicId);
        if (commonEpic == null) {
            throw new NotFoundException(String.format("Error in createSubtask - Subtask's Epic is not found by Epic ID: %d." +
                    " Subtask is not created.", epicId));
        }
        thatSubtask.setId(getNewId());
        if (isTimeIntersection(thatSubtask)) {
            throw new IllegalArgumentException("Subtask is not created - execution time cannot coincide with other subtasks");
        }

        Subtask thisSubtask = new Subtask(
                thatSubtask.getId(),
                thatSubtask.getName(),
                thatSubtask.getStatus(),
                thatSubtask.getDescription(),
                thatSubtask.getStartTime(),
                thatSubtask.getDurationInMinutes(),
                epicId
        );
        subtasksMap.put(thisSubtask.getId(), thisSubtask);
        prioritizedTasksSet.add(thisSubtask);

        commonEpic.addSubtaskId(thisSubtask.getId());
        updateEpicStatus(epicId);
        calcEpicEndTime(commonEpic);
        return thisSubtask;
    }

    @Override
    public Subtask updateSubtask(Subtask thatSubtask) {
        Subtask thisSubtask = subtasksMap.get(thatSubtask.getId());
        if (thisSubtask == null) {
            throw new NotFoundException(String.format("Error in updateSubtask - subtask is not found by subtask ID: %d." +
                    " Subtask didn't updated.", thatSubtask.getId()));
        }

        int thisSubtaskId = thisSubtask.getId();
        int thatSubtaskEpicId = thatSubtask.getEpicId();
        Epic epicOfThatSubtask = epicsMap.get(thatSubtaskEpicId);
        if (epicOfThatSubtask == null) {
            throw new NotFoundException(String.format("Error in updateSubtask - Epic is not found by Subtask's Epic ID: %d. " +
                    "Subtask didn't updated.", thatSubtaskEpicId));
        }

        if (isTimeIntersection(thatSubtask)) {
            throw new IllegalArgumentException("Task is not created - execution time cannot coincide with other tasks");
        }

        int thisSubtaskEpicId = thisSubtask.getEpicId();
        Epic epicOfThisSubtask = epicsMap.get(thisSubtaskEpicId);
        if (epicOfThisSubtask != epicOfThatSubtask) {
            epicOfThisSubtask.removeSubtaskId(thisSubtaskId);
            updateEpicStatus(thisSubtaskEpicId);
            calcEpicEndTime(epicOfThisSubtask);
        }
        prioritizedTasksSet.remove(thisSubtask);
        thisSubtask.setEpicId(thatSubtaskEpicId);
        thisSubtask.setName(thatSubtask.getName());
        thisSubtask.setDescription(thatSubtask.getDescription());
        thisSubtask.setStatus(thatSubtask.getStatus());
        thisSubtask.setStartTime(thatSubtask.getStartTime());
        thisSubtask.setDurationInMinutes(thatSubtask.getDurationInMinutes());
        subtasksMap.put(thisSubtaskId, thisSubtask);
        prioritizedTasksSet.add(thisSubtask);

        epicOfThatSubtask.addSubtaskId(thisSubtaskId);
        updateEpicStatus(thatSubtaskEpicId);
        calcEpicEndTime(epicOfThatSubtask);
        return thisSubtask;
    }

    @Override
    public Epic createEpic(Epic thatEpic) {
        thatEpic.setId(getNewId());
        Epic thisEpic = new Epic(
                thatEpic.getId(),
                thatEpic.getName(),
                thatEpic.getDescription()
        );
        epicsMap.put(thisEpic.getId(), thisEpic);
        return thisEpic;
    }

    @Override
    public Epic updateEpic(Epic thatEpic) {
        int commonId = thatEpic.getId();
        Epic thisEpic = epicsMap.get(commonId);
        if (thisEpic == null) {
            throw new NotFoundException(String.format("Error in updateEpic - epic is not found by ID :%d. " +
                    "Epic is not updated.", commonId));
        }
        thisEpic.setName(thatEpic.getName());
        thisEpic.setDescription(thatEpic.getDescription());
        epicsMap.put(commonId, thisEpic);
        updateEpicStatus(commonId);
        calcEpicEndTime(thisEpic);
        return thisEpic;
    }

    protected void updateEpicStatus(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        if (thisEpic == null) {
            System.out.printf("Error in updateEpicStatus - epic ID: %d is not found\n", epicId);
        }
        ArrayList<Integer> subtasksIdList = new ArrayList<>(thisEpic.getSubtasksIdList());
        int statusQuantityCounter = 0;
        int statusNewCounter = 0;
        int statusDoneCounter = 0;

        for (int id : subtasksIdList) {
            Subtask subTask = subtasksMap.get(id);
            Status status = subTask.getStatus();
            switch (status) {
                case NEW:
                    statusNewCounter++;
                    break;
                case DONE:
                    statusDoneCounter++;
                    break;
            }
            statusQuantityCounter++;
        }

        if ((statusQuantityCounter == 0) || (statusNewCounter == statusQuantityCounter)) {
            thisEpic.setStatus(Status.NEW);
        } else if (statusDoneCounter == statusQuantityCounter) {
            thisEpic.setStatus(Status.DONE);
        } else {
            thisEpic.setStatus(Status.IN_PROGRESS);
        }
        epicsMap.put(thisEpic.getId(), thisEpic);
    }

    public void calcEpicEndTime(Epic thisEpic) {
        if (thisEpic == null) {
            System.out.println("Error in calcEpicEndTime - epic ID is not found");
        }
        ArrayList<Integer> subtasksIdList = new ArrayList<>(thisEpic.getSubtasksIdList());
        if (subtasksIdList.size() == 0) {
            thisEpic.setStartTime(null);
            thisEpic.setDurationInMinutes(0);
            thisEpic.setEndTime(null);
            return;
        }
        List<Subtask> subtasksWithStartTime = subtasksIdList.stream().
                map(subtasksMap::get).
                filter(Objects::nonNull).
                filter(subtask -> subtask.getStartTime() != null).
                collect(Collectors.toList());

        if (subtasksWithStartTime.size() == 0) {
            return;
        }
        Instant earliestStartTime = subtasksWithStartTime.get(0).getStartTime();
        Duration overallDuration = Duration.ofMinutes(0);
        Instant latestEndTime = earliestStartTime.plus(Duration.ofMinutes(subtasksWithStartTime.get(0).getDurationInMinutes()));

        for (Subtask subtask : subtasksWithStartTime) {
            Instant startTime = subtask.getStartTime();
            Duration duration = Duration.ofMinutes(subtask.getDurationInMinutes());
            Instant endTime = startTime.plus(duration);

            if (startTime.isBefore(earliestStartTime)) {
                earliestStartTime = startTime;
            }

            overallDuration = overallDuration.plus(duration);

            if (endTime.isAfter(latestEndTime)) {
                latestEndTime = endTime;
            }
        }
        thisEpic.setStartTime(earliestStartTime);
        thisEpic.setDurationInMinutes(overallDuration.toMinutes());
        thisEpic.setEndTime(latestEndTime);
    }


    @Override
    public List<Subtask> getSubtasksListByEpicId(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        if (thisEpic == null) {
            throw new NotFoundException(String.format("Error in getSubtasksListById - epic is not found " +
                    "by epic ID: %d.", epicId));
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
        if (task == null) {
            throw new NotFoundException("Task is not found by ID: " + taskId);
        } else {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int subtaskId) {
        Subtask subtask = subtasksMap.get(subtaskId);
        if (subtask == null) {
            throw new NotFoundException("Subtask is not found by ID: " + subtaskId);
        } else {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = epicsMap.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Epic is not found by ID: " + epicId);
        } else {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public void deleteTaskById(int taskId) {
        Task task = tasksMap.get(taskId);
        if (task == null) {
            throw new NotFoundException("Can't delete. No matches with id: " + taskId);
        }

        prioritizedTasksSet.remove(tasksMap.get(taskId));
        tasksMap.remove(taskId);
        historyManager.remove(taskId);
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        Subtask subtask = subtasksMap.remove(subtaskId);
        if (subtask == null) {
            throw new NotFoundException("Can't delete. No matches with id: " + subtaskId);
        }
        prioritizedTasksSet.remove(subtask);
        int epicIdOfSubtask = subtask.getEpicId();
        Epic epic = epicsMap.get(epicIdOfSubtask);
        epic.removeSubtaskId(subtaskId);
        updateEpicStatus(epicIdOfSubtask);
        calcEpicEndTime(epic);
        historyManager.remove(subtaskId);

    }

    @Override
    public void deleteEpicById(int epicId) throws NotFoundException{
        Epic epic = epicsMap.remove(epicId);
        if (epic == null) {
            throw new NotFoundException("Can't delete. No matches with id: " + epicId);
        }
        prioritizedTasksSet.remove(epic);
        List<Integer> subtasksIdList = epic.getSubtasksIdList();
        for (Integer subTaskId : subtasksIdList) {
            historyManager.remove(subTaskId);
            subtasksMap.remove(subTaskId);
        }

        historyManager.remove(epicId);

    }

    @Override
    public void deleteAllEpics() {
        for (Map.Entry<Integer, Epic> entry : epicsMap.entrySet()) {
            historyManager.remove(entry.getKey());
            prioritizedTasksSet.remove(entry.getValue());
        }
        epicsMap.clear();
        for (Map.Entry<Integer, Subtask> entry : subtasksMap.entrySet()) {
            historyManager.remove(entry.getKey());
            prioritizedTasksSet.remove(entry.getValue());
        }
        subtasksMap.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Map.Entry<Integer, Subtask> entry : subtasksMap.entrySet()) {
            historyManager.remove(entry.getKey());
            prioritizedTasksSet.remove(entry.getValue());
        }

        subtasksMap.clear();
        for (Epic epic : epicsMap.values()) {
            epic.clearSubtaskIdList();
            epic.setStatus(Status.NEW);
            epic.setStartTime(null);
            epic.setDurationInMinutes(0);
            epic.setEndTime(null);
        }

    }

    @Override
    public void deleteAllTasks() {
        for (Map.Entry<Integer, Task> entry : tasksMap.entrySet()) {
            historyManager.remove(entry.getKey());
            prioritizedTasksSet.remove(entry.getValue());
        }
        tasksMap.clear();
    }
}
