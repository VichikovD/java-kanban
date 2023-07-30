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
    private final Path path;

    public FileBackedTasksManager(String path) {
        this.path = Paths.get(path);
    }

    @Override
    public void createTask(Task thatTask) {
        super.createTask(thatTask);
        save();
    }

    @Override
    public void updateTask(Task thatTask) {
        super.updateTask(thatTask);
        save();
    }

    @Override
    public void createSubtask(Subtask thatSubtask) {
        super.createSubtask(thatSubtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask thatSubtask) {
        super.updateSubtask(thatSubtask);
        save();
    }

    @Override
    public void createEpic(Epic thatEpic) {
        super.createEpic(thatEpic);
        save();
    }

    @Override
    public void updateEpic(Epic thatEpic) {
        super.updateEpic(thatEpic);
        save();
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
            throw new ManagerSaveException(exc.getMessage(), exc, path.getFileName().toString());
        }
    }

    public static FileBackedTasksManager loadFromFile(String path) {
        FileBackedTasksManager taskManager = new FileBackedTasksManager(path);
        List<String> linesFromFile;
        try {
            linesFromFile = Files.readAllLines(Paths.get(path));
        } catch (IOException exc) {
            throw new ManagerLoadException(exc.getMessage(), exc, taskManager.path.getFileName().toString());
        }
        int emptyLineIndex = 0;
        Map<Integer, Task> allTasksMap = new HashMap<>();
        int maxId = 0;

        for (int i = 1; i < linesFromFile.size(); i++) {
            String line = linesFromFile.get(i);
            if (line.equals("")) {
                emptyLineIndex = i;
                break;
            }
            String[] taskElements = line.split(",");
            TasksType taskType = TasksType.valueOf(taskElements[1]);
            int id = Integer.parseInt(taskElements[0]);
            if (maxId < id) {
                maxId = id;
            }
            switch (taskType) {
                case TASK:
                    Task task = CSVConverter.stringToTask(linesFromFile.get(i));
                    taskManager.tasksMap.put(task.getId(), task);
                    allTasksMap.put(task.getId(), task);
                    break;
                case EPIC:
                    Epic epic = (Epic) CSVConverter.stringToTask(linesFromFile.get(i));
                    taskManager.epicsMap.put(epic.getId(), epic);
                    allTasksMap.put(epic.getId(), epic);
                    break;
                case SUBTASK:
                    Subtask subtask = (Subtask) CSVConverter.stringToTask(linesFromFile.get(i));
                    taskManager.subtasksMap.put(subtask.getId(), subtask);
                    allTasksMap.put(subtask.getId(), subtask);
                    break;
            }
        }
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            Epic epicOfThisSubtask = taskManager.epicsMap.get(subtask.getEpicId());
            epicOfThisSubtask.addSubtaskId(subtask.getId());
        }

        int historyLineNumber = emptyLineIndex + 1;
        String historyLine = linesFromFile.get(historyLineNumber);
        int[] historyElements = CSVConverter.stringToIdArray(historyLine);

        for (int id : historyElements) {
            taskManager.historyManager.add(allTasksMap.get(id));
        }
        taskManager.setTaskCounter(maxId);
        return taskManager;
    }

}
