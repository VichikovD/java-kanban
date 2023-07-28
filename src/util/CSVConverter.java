package util;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class CSVConverter {
    public static final String HEADER = "id,type,name,status,description,epic\n";
    public static int[] stringToIdArray(String line) {
        String[] lineElements = line.split(",");
        int[] result = new int[lineElements.length];
        for (int i = 0; i < lineElements.length; i++) {
            result[i] = Integer.parseInt(lineElements[i]);
        }
        return result;
    }
    public static String taskToString(Task task) {
        return String.format("%s,%s,%s,%s,%s", task.getId(), task.getTasksType().toString(), task.getName(),
                task.getStatus(), task.getDescription());

    }

    public static Task stringToTask(String value) {
        Task task = new Task();
        String[] data = value.split(",");

        task.setId(Integer.parseInt(data[0]));
        task.setName(data[2]);
        task.setStatus(Status.valueOf(data[3]));
        task.setDescription(data[4]);

        return task;
    }

    public static String subtaskToString(Subtask subtask) {
        return taskToString(subtask) + "," + subtask.getEpicId();
    }

    public static Subtask stringToSubtask(String value) {
        Subtask subtask = new Subtask();
        String[] data = value.split(",");

        subtask.setId(Integer.parseInt(data[0]));
        subtask.setName(data[2]);
        subtask.setStatus(Status.valueOf(data[3]));
        subtask.setDescription(data[4]);
        subtask.setEpicId(Integer.parseInt(data[5]));

        return subtask;
    }

    public static Epic stringToEpic(String value) {
        Epic epic = new Epic();
        String[] data = value.split(",");

        epic.setId(Integer.parseInt(data[0]));
        epic.setName(data[2]);
        epic.setStatus(Status.valueOf(data[3]));
        epic.setDescription(data[4]);

        return epic;
    }
}
