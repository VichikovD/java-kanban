package util;

import service.*;
import service.file.FileBackedTasksManager;
import service.mem.InMemoryHistoryManager;
import service.mem.InMemoryTaskManager;

public class Managers {
    private Managers() {
    }

    public static HistoryManager getDefaultHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        return historyManager;
    }

    public static TaskManager getDefaults() {
        TaskManager taskManager = new FileBackedTasksManager("AutoSave.csv");
        return taskManager;
    }

}
