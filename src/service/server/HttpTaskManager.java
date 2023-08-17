package service.server;

import com.google.gson.*;
import model.Epic;
import model.Subtask;
import model.Task;
import service.file.FileBackedTasksManager;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

import java.time.Instant;
import java.util.*;

public class HttpTaskManager extends FileBackedTasksManager {
    private static final String TASKS_KEY = "tasks";
    private static final String SUBTASKS_KEY = "subtasks";
    private static final String EPICS_KEY = "epics";
    private static final String HISTORY_KEY = "history";
    KVTaskClient kvTaskClient;
    Gson gson;

    public HttpTaskManager() {
        this.kvTaskClient = new KVTaskClient("http://localhost:8010/");
        this.gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }

    public HttpTaskManager(String url) {
        this.kvTaskClient = new KVTaskClient(url);
        this.gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
        load();
    }

    @Override
    public void save() {
        List<Task> allTasks = getAllTasks();
        List<Subtask> allSubtasks = getAllSubtasks();
        List<Epic> allEpics = getAllEpics();
        List<Integer> historyIdList = new ArrayList<>();
        for (Task task : getHistory()) {
            historyIdList.add(task.getId());
        }
        try {
            kvTaskClient.put(TASKS_KEY, gson.toJson(allTasks));
            kvTaskClient.put(SUBTASKS_KEY, gson.toJson(allSubtasks));
            kvTaskClient.put(EPICS_KEY, gson.toJson(allEpics));
            kvTaskClient.put(HISTORY_KEY, gson.toJson(historyIdList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        tasksMap.clear();
        subtasksMap.clear();
        epicsMap.clear();
        prioritizedTasksSet.clear();
        int maxId = 0;
        Map<Integer, Task> allTasksMap = new HashMap<>();

        try {
            String jsonTasksList = kvTaskClient.load(TASKS_KEY);
            Type tasksLististType = new TypeToken<List<Task>>() {
            }.getType();
            List<Task> tasksList = gson.fromJson(jsonTasksList, tasksLististType);
            for (Task task : tasksList) {
                int taskId = task.getId();
                if (taskId > maxId) {
                    maxId = taskId;
                }
                tasksMap.put(taskId, task);
                prioritizedTasksSet.add(task);
                allTasksMap.put(taskId, task);
            }


            jsonTasksList = kvTaskClient.load(SUBTASKS_KEY);
            Type subtasksListType = new TypeToken<List<Subtask>>() {
            }.getType();
            List<Subtask> subtasksList = gson.fromJson(jsonTasksList, subtasksListType);
            for (Subtask subtask : subtasksList) {
                int subtaskId = subtask.getId();
                if (subtaskId > maxId) {
                    maxId = subtaskId;
                }
                subtasksMap.put(subtaskId, subtask);
                prioritizedTasksSet.add(subtask);
                allTasksMap.put(subtaskId, subtask);
            }


            jsonTasksList = kvTaskClient.load(EPICS_KEY);
            Type EpicsListType = new TypeToken<List<Epic>>() {
            }.getType();
            List<Epic> epicsList = gson.fromJson(jsonTasksList, EpicsListType);
            for (Epic epic : epicsList) {
                int epicId = epic.getId();
                if (epicId > maxId) {
                    maxId = epicId;
                }
                epicsMap.put(epicId, epic);
                allTasksMap.put(epicId, epic);
            }


            String jsonTasksIdHistoryArray = kvTaskClient.load(HISTORY_KEY);
            Type integerListType = new TypeToken<List<Integer>>() {
            }.getType();
            List<Integer> tasksIdHistoryList = gson.fromJson(jsonTasksIdHistoryArray, integerListType);
            for (Integer id : tasksIdHistoryList) {
                historyManager.add(allTasksMap.get(id));
            }
            setTaskCounter(maxId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
