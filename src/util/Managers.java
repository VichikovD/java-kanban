package util;

import service.*;
import service.file.FileBackedTasksManager;
import service.mem.InMemoryHistoryManager;
import service.server.HttpTaskManager;

import java.net.ConnectException;

public class Managers {
    private Managers() {
    }

    public static HistoryManager getDefaultHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        return historyManager;
    }

    public static TaskManager getDefaults() {
        TaskManager taskManager = new HttpTaskManager();
        return taskManager;
    }

}
