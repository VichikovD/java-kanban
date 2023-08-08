package service.mem;

import service.HistoryManager;
import service.TaskManager;
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
        boolean isIntersected = false;
        Instant toValidateStartTime = taskToValidate.getStartTime();
        if (toValidateStartTime == null) {
            return isIntersected;
        }
        Instant toValidateEndTime = taskToValidate.getEndTime();
        List<Task> noNullPrioritizedTasks = getPrioritizedTasks().stream().
                filter(task -> task.getStartTime() != null).
                collect(Collectors.toList());
        for (Task task : noNullPrioritizedTasks) {
            if (task.getId() == taskToValidate.getId()) {
                continue;
            }
            Instant startTime = task.getStartTime();
            Instant endTime = task.getEndTime();
            if (
                    (toValidateStartTime.isAfter(startTime) && toValidateStartTime.isBefore(endTime))
                    || (toValidateEndTime.isAfter(startTime) && toValidateEndTime.isBefore(endTime))
                    || (startTime.isAfter(toValidateStartTime) && startTime.isBefore(toValidateEndTime))
                    || (endTime.isAfter(toValidateStartTime) && endTime.isBefore(toValidateEndTime))
                    || (toValidateStartTime.equals(startTime) && toValidateEndTime.equals(endTime))
                    || (toValidateEndTime.equals(startTime))
                    || (toValidateStartTime.equals(endTime))
            ) {
                isIntersected = true;
            }
            if (isIntersected) {
                return isIntersected;
            }
        }
        return isIntersected;
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
    public void createTask(Task thatTask) {
        Instant startTime = thatTask.getStartTime();
        thatTask.setId(getNewId());
        if (isTimeIntersection(thatTask)) {
            System.out.println("Task is not created - execution time cannot coincide with other tasks");
            taskCounter--;
            return;
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
    }

    @Override
    public void updateTask(Task thatTask) {
        Task thisTask = tasksMap.get(thatTask.getId());
        if (thisTask == null) {
            System.out.println("Error in updateTask - given task id is not in tasksMap keys");
            return;
        }

        if (isTimeIntersection(thatTask)) {
            System.out.println("Task is not created - execution time cannot coincide with other tasks");
            return;
        }
        prioritizedTasksSet.remove(thisTask);
        thisTask.setName(thatTask.getName());
        thisTask.setDescription(thatTask.getDescription());
        thisTask.setStatus(thatTask.getStatus());
        thisTask.setStartTime(thatTask.getStartTime());
        thisTask.setDurationInMinutes(thatTask.getDurationInMinutes());
        tasksMap.put(thisTask.getId(), thisTask);
        prioritizedTasksSet.add(thisTask);
    }

    @Override
    public void createSubtask(Subtask thatSubtask) {
        int epicId = thatSubtask.getEpicId();
        Epic epicOfThatSubtask = epicsMap.get(epicId);
        if (epicOfThatSubtask == null) {
            System.out.println("Error in createSubtask - Epic is not found by Epic ID. Subtask did't created.");
            return;
        }
        thatSubtask.setId(getNewId());
        if (isTimeIntersection(thatSubtask)) {
            System.out.println("Subtask is not created - execution time cannot coincide with other subtasks");
            taskCounter--;
            return;
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

        epicOfThatSubtask.addSubtaskId(thisSubtask.getId());
        updateEpicStatus(epicId);
        getEpicEndTime(epicId);
    }

    @Override
    public void updateSubtask(Subtask thatSubtask) {
        Subtask thisSubtask = subtasksMap.get(thatSubtask.getId());
        if (thisSubtask == null) {
            System.out.println("Error in updateSubtask - subtask is not found by subtask ID. Subtask didn't updated.");
            return;
        }

        int thisSubtaskId = thisSubtask.getId();
        int thatSubtaskEpicId = thatSubtask.getEpicId();
        Epic epicOfThatSubTask = epicsMap.get(thatSubtaskEpicId);
        if (epicOfThatSubTask == null) {
            System.out.println("Error in updateSubtask - Epic is not found by Subtask's Epic ID. Subtask didn't updated.");
            return;
        }

        if (isTimeIntersection(thatSubtask)) {
            System.out.println("Subtask is not created - execution time cannot coincide with other subtasks");
            return;
        }

        int thisSubtaskEpicId = thisSubtask.getEpicId();
        Epic epicOfThisSubtask = epicsMap.get(thisSubtaskEpicId);
        if (epicOfThisSubtask != epicOfThatSubTask) {
            epicOfThisSubtask.removeSubtaskId(thisSubtaskId);
            updateEpicStatus(thisSubtaskEpicId);
            getEpicEndTime(thisSubtaskEpicId);
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

        epicOfThatSubTask.addSubtaskId(thisSubtaskId);
        updateEpicStatus(thatSubtaskEpicId);
        getEpicEndTime(thatSubtaskEpicId);
    }

    @Override
    public void createEpic(Epic thatEpic) {
        thatEpic.setId(getNewId());
        Epic thisEpic = new Epic(
                thatEpic.getId(),
                thatEpic.getName(),
                thatEpic.getDescription()
        );
        epicsMap.put(thisEpic.getId(), thisEpic);
        thatEpic.setId(thisEpic.getId());
    }

    @Override
    public void updateEpic(Epic thatEpic) {
        int comonId = thatEpic.getId();
        Epic thisEpic = epicsMap.get(comonId);
        if (thisEpic == null) {
            return;
        }
        thisEpic.setName(thatEpic.getName());
        thisEpic.setDescription(thatEpic.getDescription());
        epicsMap.put(comonId, thisEpic);
        updateEpicStatus(comonId);
        getEpicEndTime(comonId);
    }

    protected void updateEpicStatus(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        if (thisEpic == null) {
            System.out.println("Error in updateEpicStatus - epic ID is not found");
            return;
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

    public void getEpicEndTime(int epicId) {
        Epic thisEpic = epicsMap.get(epicId);
        if (thisEpic == null) {
            System.out.println("Error in getEpicEndTime - epic ID is not found");
            return;
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
        prioritizedTasksSet.remove(tasksMap.get(taskId));
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
        prioritizedTasksSet.remove(subtask);
        int epicIdOfSubtask = subtask.getEpicId();
        Epic epic = epicsMap.get(epicIdOfSubtask);
        epic.removeSubtaskId(subtaskId);
        updateEpicStatus(epicIdOfSubtask);
        getEpicEndTime(epicIdOfSubtask);
        historyManager.remove(subtaskId);

    }

    @Override
    public void deleteEpicById(int epicId) {
        Epic epic = epicsMap.remove(epicId);
        if (epic == null) {
            System.out.println("Can't delete. No matches with id: " + epicId);
            return;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryTaskManager that = (InMemoryTaskManager) o;
        return Objects.equals(tasksMap, that.tasksMap) && Objects.equals(subtasksMap, that.subtasksMap) && Objects.equals(epicsMap, that.epicsMap) && Objects.equals(prioritizedTasksSet, that.prioritizedTasksSet) && Objects.equals(historyManager, that.historyManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tasksMap, subtasksMap, epicsMap, prioritizedTasksSet, historyManager);
    }
}
