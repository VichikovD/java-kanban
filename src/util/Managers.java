package util;

import service.*;

public class Managers {
    private Managers() {
    }

    public static InMemoryTaskManager getInMemoryTaskManager() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        return historyManager;
    }

    public static FileBackedTasksManager getFileBackedTaskManager() {
        FileBackedTasksManager taskManager = new FileBackedTasksManager("AutoSave.txt");
        return taskManager;
    }

}
