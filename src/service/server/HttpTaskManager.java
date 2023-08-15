package service.server;

import com.google.gson.*;
import model.Epic;
import model.Subtask;
import model.Task;
import service.file.FileBackedTasksManager;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

import java.net.ConnectException;
import java.time.Instant;
import java.util.*;

public class HttpTaskManager extends FileBackedTasksManager {
    private static final String TASKS_KEY = "1";
    private static final String SUBTASKS_KEY = "2";
    private static final String EPICS_KEY = "3";
    private static final String HISTORY_KEY = "4";
    KVTaskClient kvTaskClient;
    Gson gson;

    public HttpTaskManager() throws ConnectException {
        this.kvTaskClient = new KVTaskClient("http://localhost:8010/");
        this.gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }

    public HttpTaskManager(String url) throws ConnectException {
        this.kvTaskClient = new KVTaskClient(url);
        this.gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
        resetAndLoadFromKVServer();
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
        } catch (ConnectException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public void resetAndLoadFromKVServer() throws ConnectException {
        tasksMap.clear();
        subtasksMap.clear();
        epicsMap.clear();
        prioritizedTasksSet.clear();
        int maxId = 0;
        Map<Integer, Task> allTasksMap = new HashMap<>();

        JsonElement jsonTasksListElement = JsonParser.parseString(kvTaskClient.load(TASKS_KEY));
        Type tasksLististType = new TypeToken<List<Task>>() {}.getType();
        List<Task> tasksList = gson.fromJson(jsonTasksListElement, tasksLististType);
        for (Task task : tasksList) {
            int taskId = task.getId();
            if (taskId > maxId) {
                maxId = taskId;
            }
            tasksMap.put(taskId, task);
            prioritizedTasksSet.add(task);
            allTasksMap.put(taskId, task);
        }


        jsonTasksListElement = JsonParser.parseString(kvTaskClient.load(SUBTASKS_KEY));
        Type subtasksListType = new TypeToken<List<Subtask>>() {}.getType();
        List<Subtask> subtasksList = gson.fromJson(jsonTasksListElement, subtasksListType);
        for (Subtask subtask : subtasksList) {
            int subtaskId = subtask.getId();
            if (subtaskId > maxId) {
                maxId = subtaskId;
            }
            subtasksMap.put(subtaskId, subtask);
            prioritizedTasksSet.add(subtask);
            allTasksMap.put(subtaskId, subtask);
        }


        jsonTasksListElement = JsonParser.parseString(kvTaskClient.load(EPICS_KEY));
        Type EpicsListType = new TypeToken<List<Epic>>() {}.getType();
        List<Epic> epicsList = gson.fromJson(jsonTasksListElement, EpicsListType);
        for (Epic epic : epicsList) {
                int epicId = epic.getId();
                if (epicId > maxId) {
                    maxId = epicId;
                }
                epicsMap.put(epicId, epic);
                allTasksMap.put(epicId, epic);
            }


        JsonArray jsonTasksIdHistoryArray = JsonParser.parseString(kvTaskClient.load(HISTORY_KEY)).getAsJsonArray();
        if (!jsonTasksIdHistoryArray.isJsonNull()) {
            for (JsonElement jsonTasksIdHistory : jsonTasksIdHistoryArray) {
                int id = jsonTasksIdHistory.getAsInt();
                historyManager.add(allTasksMap.get(id));
            }
        }
        setTaskCounter(maxId);

    }
}
