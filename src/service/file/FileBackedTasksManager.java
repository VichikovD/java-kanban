package service.file;

import model.*;
import service.mem.InMemoryTaskManager;
import service.file.exception.ManagerLoadException;
import service.file.exception.ManagerSaveException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private Path path = null;

    public FileBackedTasksManager(String path) {
        this.path = Paths.get(path);
    }

    public FileBackedTasksManager() {

    }

    @Override
    public Task createTask(Task thatTask) {
        super.createTask(thatTask);
        save();
        int id = thatTask.getId();
        return tasksMap.get(id);
    }

    @Override
    public Task updateTask(Task thatTask) {
        super.updateTask(thatTask);
        save();
        int id = thatTask.getId();
        return tasksMap.get(id);
    }

    @Override
    public Subtask createSubtask(Subtask thatSubtask) {
        super.createSubtask(thatSubtask);
        save();
        int id = thatSubtask.getId();
        return subtasksMap.get(id);
    }

    @Override
    public Subtask updateSubtask(Subtask thatSubtask) {
        super.updateSubtask(thatSubtask);
        save();
        int id = thatSubtask.getId();
        return subtasksMap.get(id);
    }

    @Override
    public Epic createEpic(Epic thatEpic) {
        super.createEpic(thatEpic);
        save();
        int id = thatEpic.getId();
        return epicsMap.get(id);
    }

    @Override
    public Epic updateEpic(Epic thatEpic) {
        super.updateEpic(thatEpic);
        save();
        int id = thatEpic.getId();
        return epicsMap.get(id);
    }

    @Override
    public Task getTaskById(int taskId) {
        Task task = super.getTaskById(taskId);
        save();
        return task;
    }

    @Override
    public Subtask getSubtaskById(int subtaskId) {
        Subtask subtask = super.getSubtaskById(subtaskId);
        save();
        return subtask;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = super.getEpicById(epicId);
        save();
        return epic;
    }

    @Override
    public void deleteTaskById(int taskId) {
        super.deleteTaskById(taskId);
        save();
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        super.deleteSubtaskById(subtaskId);
        save();
    }

    @Override
    public void deleteEpicById(int epicId) {
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    public void save() {
        String lSeparator = System.lineSeparator();
        StringBuilder stringToSave = new StringBuilder(CSVConverter.HEADER + lSeparator);
        for (Task task : tasksMap.values()) {
            stringToSave.append(CSVConverter.taskToString(task)).append(lSeparator);
        }
        for (Epic epic : epicsMap.values()) {
            stringToSave.append(CSVConverter.taskToString(epic)).append(lSeparator);
        }
        for (Subtask subtask : subtasksMap.values()) {
            stringToSave.append(CSVConverter.taskToString(subtask)).append(lSeparator);
        }
        stringToSave.append(lSeparator).append(CSVConverter.historyToString(historyManager));
        try {
            Files.writeString(path, stringToSave.toString());
        } catch (IOException exc) {
            throw new ManagerSaveException("File name - " + path.getFileName().toString(), exc);
        }
    }

    public static FileBackedTasksManager load(String path) {
        FileBackedTasksManager taskManager = new FileBackedTasksManager(path);
        List<String> linesFromFile;
        try {
            linesFromFile = Files.readAllLines(Paths.get(path));
        } catch (IOException exc) {
            String lSeparator = System.lineSeparator();
            throw new ManagerLoadException("File name - " + path, exc);
        }
        int emptyLineIndex = 0;
        Map<Integer, Task> allTasksMap = new HashMap<>();
        int maxId = 0;

        for (int i = 1; i < linesFromFile.size(); i++) {
            String line = linesFromFile.get(i);
            if (line.isEmpty()) {
                emptyLineIndex = i;
                break;
            }
            Task task = CSVConverter.stringToTask(line);

            int id = task.getId();
            if (maxId < id) {
                maxId = id;
            }
            switch (task.getTasksType()) {
                case TASK:
                    taskManager.tasksMap.put(id, task);
                    allTasksMap.put(id, task);
                    taskManager.prioritizedTasksSet.add(task);
                    break;
                case EPIC:
                    Epic epic = (Epic) task;
                    taskManager.epicsMap.put(id, epic);
                    allTasksMap.put(id, epic);
                    break;
                case SUBTASK:
                    Subtask subtask = (Subtask) task;
                    taskManager.subtasksMap.put(id, subtask);
                    allTasksMap.put(id, subtask);
                    taskManager.prioritizedTasksSet.add(subtask);
                    break;
            }
        }
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            Epic epicOfThisSubtask = taskManager.epicsMap.get(subtask.getEpicId());
            epicOfThisSubtask.addSubtaskId(subtask.getId());
        }

        for (Epic epic : taskManager.getAllEpics()) {
            taskManager.calcEpicEndTime(epic);
        }

        int historyLineNumber = emptyLineIndex + 1;
        if (linesFromFile.size() == historyLineNumber) {
            historyLineNumber = linesFromFile.size() - 1;
        }
        if (linesFromFile.get(historyLineNumber).equals("")) {
            return taskManager;
        }
        String historyLine = linesFromFile.get(historyLineNumber);
        int[] historyElements = CSVConverter.stringToIdArray(historyLine);

        for (int id : historyElements) {
            taskManager.historyManager.add(allTasksMap.get(id));
        }
        taskManager.setTaskCounter(maxId);
        return taskManager;
    }
}
