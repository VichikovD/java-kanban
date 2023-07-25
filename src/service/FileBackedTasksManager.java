package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import util.ManagerSaveException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final Path path;
    private final String headline;

    public FileBackedTasksManager(String path) {
        this.path = Paths.get(path);
        this.headline = "id,type,name,status,description,epic\n";

    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    public int getNewId() {
        return super.getNewId();
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
    public List<Subtask> getSubtasksListByEpicId(int epicId) {
        return super.getSubtasksListByEpicId(epicId);
    }

    @Override
    public List<Task> getAllTasks() {
        return super.getAllTasks();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return super.getAllSubtasks();
    }

    @Override
    public List<Epic> getAllEpics() {
        return super.getAllEpics();
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
        String stringToSave = headline;
        for (Task task : tasksMap.values()) {
            stringToSave += task.toString(task) + "\n";
        }
        for (Epic epic : epicsMap.values()) {
            stringToSave += epic.toString(epic) + "\n";
        }
        for (Subtask subtask : subtasksMap.values()) {
            stringToSave += subtask.toString(subtask) + "\n";
        }
        stringToSave += "\n" + InMemoryHistoryManager.historyToString(historyManager);
        try {
            Files.writeString(path, stringToSave);
        } catch (IOException exc) {
            throw new ManagerSaveException(exc.getMessage());
        }
    }

    static FileBackedTasksManager loadFromFile(String path) {
        FileBackedTasksManager taskManager = new FileBackedTasksManager("AutoSave.txt");
        String fullFile = null;
        try {
            fullFile = Files.readString(Paths.get(path));
        } catch (IOException exc) {
            System.out.println(exc.getMessage());
        }

        if (fullFile.equals("")) {
            return new FileBackedTasksManager("AutoSave.txt");
        }

        String[] linesFromFile = fullFile.split("\n");
        System.out.println();
        Map<Integer, Task> allTasksMap = new HashMap<>();
        for (int i = 1; i < linesFromFile.length - 2; i++) {
            String[] taskElements = linesFromFile[i].split(",");
            switch (taskElements[1]) {
                case ("TASK"):
                    Task task = Task.taskFromStringArray(taskElements);
                    taskManager.tasksMap.put(task.getId(), task);
                    allTasksMap.put(task.getId(), task);
                    break;
                case ("EPIC"):
                    Epic epic = Epic.epicFromStringArray(taskElements);
                    taskManager.epicsMap.put(epic.getId(), epic);
                    allTasksMap.put(epic.getId(), epic);
                    break;
                case ("SUBTASK"):
                    Subtask subtask = Subtask.subtaskFromStringArray(taskElements);
                    taskManager.subtasksMap.put(subtask.getId(), subtask);

                    Epic epicOfThisSubtask = taskManager.epicsMap.get(subtask.getEpicId());
                    epicOfThisSubtask.addSubtaskId(subtask.getId());
                    allTasksMap.put(subtask.getId(), subtask);
                    break;
            }

        }
        int lastLine = linesFromFile.length - 1;
        String[] historyElements = linesFromFile[lastLine].split(",");
        if (historyElements[0].equals("")) {
            return taskManager;
        }
        for (String idString : historyElements) {
            int id = Integer.parseInt(idString);
            taskManager.historyManager.add(allTasksMap.get(id));
        }
        allTasksMap = null;
        return taskManager;
    }


    public static void main(String[] args) {
        FileBackedTasksManager manager1 = FileBackedTasksManager.loadFromFile("AutoSave.txt");

        System.out.println(manager1.getAllTasks());
        System.out.println(manager1.getAllSubtasks());
        System.out.println(manager1.getAllEpics());
        System.out.println();
        System.out.println(manager1.getHistory());

        Task task1 = new Task();
        task1.setDescription("Description T1");
        task1.setName("T1");
        task1.setStatus(Status.NEW);
        manager1.createTask(task1);

        Task task2 = new Task();
        task2.setDescription("Description T2");
        task2.setName("T2");
        task2.setStatus(Status.DONE);
        manager1.createTask(task2);

        Epic epic1 = new Epic();
        epic1.setDescription("Description E1");
        epic1.setName("E1");
        manager1.createEpic(epic1);
        System.out.println(manager1.getAllEpics());
        System.out.println();

        manager1.getTaskById(2);


        Subtask subTask1 = new Subtask();
        subTask1.setDescription("Description S1");
        subTask1.setName("S1");
        subTask1.setStatus(Status.DONE);
        subTask1.setEpicId(3);
        manager1.createSubtask(subTask1);

        manager1.getSubtaskById(4);


        Subtask subTask2 = new Subtask();
        subTask2.setDescription("Description S2");
        subTask2.setName("S2");
        subTask2.setStatus(Status.NEW);
        subTask2.setEpicId(3);
        manager1.createSubtask(subTask2);

        Subtask subTask3 = new Subtask();
        subTask3.setDescription("Description S3");
        subTask3.setName("S3");
        subTask3.setStatus(Status.IN_PROGRESS);
        subTask3.setEpicId(3);
        manager1.createSubtask(subTask3);

        Epic epic2 = new Epic();
        epic2.setDescription("Description E2");
        epic2.setName("E2");
        manager1.createEpic(epic2);

        System.out.println(manager1.getAllTasks());
        System.out.println(manager1.getAllSubtasks());
        System.out.println(manager1.getAllEpics());
        System.out.println();

        manager1.getEpicById(3);
        manager1.getEpicById(7);
        manager1.getTaskById(1);
        manager1.getSubtaskById(5);
        manager1.getEpicById(3);
        manager1.getEpicById(7);
        manager1.getTaskById(2);
        manager1.getSubtaskById(4);
        manager1.getSubtaskById(6);
        manager1.getSubtaskById(5);
        
        System.out.println(manager1.getHistory());
        System.out.println("");

        FileBackedTasksManager manager2 = FileBackedTasksManager.loadFromFile("AutoSave.txt");

        System.out.println("Task maps are identical :" + manager1.tasksMap.equals(manager2.tasksMap));
        System.out.println("Subtask maps are identical :" +manager1.subtasksMap.equals(manager2.subtasksMap));
        System.out.println("Epic maps are identical :" +manager1.epicsMap.equals(manager2.epicsMap));

    }
}
