package service.file;

import model.*;
import service.HistoryManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CSVConverter {
    public static final String HEADER = "id,type,name,status,description,startTime,durationInMinutes,epic";

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
        String startTime = null;
        if (task.getStartTime() == null) {
            startTime = "null";
        } else {
            startTime = task.getStartTime().toString();
        }
        String result = String.format("%s,%s,%s,%s,%s,%s,%s,", task.getId(), task.getTasksType().toString(),
                task.getName(), task.getStatus(), task.getDescription(), startTime,task.getDurationInMinutes());
        if (task.getEpicId() != null) {
            result += task.getEpicId();
        }
        return result;
    }

    public static Task stringToTask(String value) {
        String[] data = value.split(",");
        Task task = null;

        Integer id = Integer.parseInt(data[0]);
        TasksType type = TasksType.valueOf(data[1]);
        String name = data[2];
        Status status = Status.valueOf(data[3]);
        String description = data[4];
        Instant startTime = null;
        if (!data[5].equals("null")) {
            startTime = Instant.parse(data[5]);
        }
        long durationInMinutes = Long.parseLong(data[6]);
        switch (type) {
            case TASK:
                task = new Task(id, name, status, description, startTime, durationInMinutes);
                break;
            case SUBTASK:
                Integer epicId = Integer.parseInt(data[7]);
                task = new Subtask(id, name, status, description, startTime, durationInMinutes, epicId);
                break;
            case EPIC:
                task = new Epic(id, name, status, description, startTime, durationInMinutes, new ArrayList<>());
                break;
        }
        return task;
    }
}
