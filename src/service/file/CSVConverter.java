package service.file;

import model.*;
import service.HistoryManager;

import java.util.List;

public class CSVConverter {
    public static final String HEADER = "id,type,name,status,description,epic" + System.lineSeparator();

    public static int[] stringToIdArray(String line) {
        String[] lineElements = line.split(",");
        int[] result = new int[lineElements.length];
        for (int i = 0; i < lineElements.length; i++) {
            result[i] = Integer.parseInt(lineElements[i]);
        }
        return result;
    }

    public static String historyToString(HistoryManager manager) {
        List<Task> historyList = manager.getHistory();
        StringBuilder tasksIdLine = new StringBuilder();
        int counter = 0;
        for (Task task : historyList) {
            tasksIdLine.append(task.getId());
            counter += 1;
            if (historyList.size() != counter) {
                tasksIdLine.append(",");
            }
        }
        return tasksIdLine.toString();
    }

    public static String taskToString(Task task) {
        String result = String.format("%s,%s,%s,%s,%s,", task.getId(), task.getTasksType().toString(), task.getName(),
                task.getStatus(), task.getDescription());
        if (task.getEpicId() != null) {
            result += task.getEpicId();
        }
        return result;
    }

    public static Task stringToTask(String value) {
        String[] data = value.split(",");
        TasksType type = TasksType.valueOf(data[1]);
        Task task = null;
        switch (type) {
            case TASK:
                task = new Task();
                break;
            case SUBTASK:
                task = new Subtask();
                task.setEpicId(Integer.parseInt(data[5]));
                break;
            case EPIC:
                task = new Epic();
                break;
        }

        task.setId(Integer.parseInt(data[0]));
        task.setName(data[2]);
        task.setStatus(Status.valueOf(data[3]));
        task.setDescription(data[4]);
        return task;
    }
}
