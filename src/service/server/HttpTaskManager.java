package service.server;

import com.google.gson.*;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.file.FileBackedTasksManager;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpTaskManager extends FileBackedTasksManager {
    /*public static void main(String[] args) {
        HttpTaskManager httpTaskManager = new HttpTaskManager();

        httpTaskManager.resetAndLoadFromKVServer();
        *//*httpTaskManager.getTaskById(1);
        httpTaskManager.getTaskById(4);
        httpTaskManager.getTaskById(1);*//*
        System.out.println(httpTaskManager.getHistory());
        System.out.println(httpTaskManager.getAllTasks());
        System.out.println(httpTaskManager.getAllSubtasks());
        System.out.println(httpTaskManager.getAllEpics());
        System.out.println(httpTaskManager.getPrioritizedTasks());
    }*/
    KVTaskClient kvTaskClient;
    Gson gson;
    public HttpTaskManager() {
        this.kvTaskClient = new KVTaskClient("http://localhost:8010/");
        this.gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
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
        kvTaskClient.put("1", gson.toJson(allTasks));
        kvTaskClient.put("2", gson.toJson(allSubtasks));
        kvTaskClient.put("3",gson.toJson(allEpics));
        kvTaskClient.put("4", gson.toJson(historyIdList));
    }

    public void resetAndLoadFromKVServer() {
        tasksMap.clear();
        subtasksMap.clear();
        epicsMap.clear();
        prioritizedTasksSet.clear();
        int maxId = 0;
        Map<Integer, Task> allTasksMap = new HashMap<>();
        JsonElement jsonTasksListElement = JsonParser.parseString(kvTaskClient.load("1"));
        if (!jsonTasksListElement.isJsonNull()) {
            JsonArray jsonTasksArray = jsonTasksListElement.getAsJsonArray();
            for (JsonElement jsonTask : jsonTasksArray) {
                Task task = gson.fromJson(jsonTask, Task.class);
                int taskId = task.getId();
                if (taskId > maxId) {
                    maxId = taskId;
                }
                tasksMap.put(taskId, task);
                prioritizedTasksSet.add(task);
                allTasksMap.put(taskId, task);
            }
        }

        jsonTasksListElement = JsonParser.parseString(kvTaskClient.load("2"));
        if (!jsonTasksListElement.isJsonNull()) {
            JsonArray jsonSubtasksArray = jsonTasksListElement.getAsJsonArray();
            for (JsonElement jsonSubtask : jsonSubtasksArray) {
                Subtask subtask = gson.fromJson(jsonSubtask, Subtask.class);
                int subtaskId = subtask.getId();
                if (subtaskId > maxId) {
                    maxId = subtaskId;
                }
                subtasksMap.put(subtaskId, subtask);
                prioritizedTasksSet.add(subtask);
                allTasksMap.put(subtaskId, subtask);
            }
        }

        jsonTasksListElement = JsonParser.parseString(kvTaskClient.load("3"));
        if (!jsonTasksListElement.isJsonNull()) {
            JsonArray jsonEpicsArray = jsonTasksListElement.getAsJsonArray();
            for (JsonElement jsonEpic : jsonEpicsArray) {
                Epic epic = gson.fromJson(jsonEpic, Epic.class);
                int epicId = epic.getId();
                if (epicId > maxId) {
                    maxId = epicId;
                }
                epicsMap.put(epicId, epic);
                allTasksMap.put(epicId, epic);
            }
        }

        JsonArray jsonTasksIdHistoryArray = JsonParser.parseString(kvTaskClient.load("4")).getAsJsonArray();
        if (!jsonTasksIdHistoryArray.isJsonNull()) {
            for (JsonElement jsonTasksIdHistory : jsonTasksIdHistoryArray) {
                int id = jsonTasksIdHistory.getAsInt();
                historyManager.add(allTasksMap.get(id));
            }
        }
        setTaskCounter(maxId);

    }
}
