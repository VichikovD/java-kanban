import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Epic;
import model.Subtask;
import model.Task;
import service.TaskManager;
import model.Status;
import service.mem.exception.NotFoundException;
import service.server.HttpTaskManager;
import service.server.InstantAdapter;
import service.server.KVServer;
import util.Managers;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class Main {
    public static void main(String[] args) throws IOException {
        KVServer kvServer = new KVServer();
        kvServer.start();
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .setPrettyPrinting()
                .create();

        TaskManager manager1 = Managers.getDefaults();

        System.out.println(manager1.getAllTasks());
        System.out.println(manager1.getAllSubtasks());
        System.out.println(manager1.getAllEpics());
        System.out.println(manager1.getHistory());
        System.out.println();

        Task task1 = new Task();
        task1.setName("T1");
        task1.setStatus(Status.NEW);
        task1.setDescription("Description T1");
        task1.setStartTime(Instant.parse("2023-01-01T00:00:00.000Z"));
        task1.setDurationInMinutes(Duration.ofDays(31).toMinutes());
        manager1.createTask(task1);

        Task task2 = new Task();
        task2.setName("T2");
        task2.setStatus(Status.DONE);
        task2.setDescription("Description T2");
        task2.setStartTime(Instant.parse("2023-01-01T00:00:00.000Z"));
        task2.setDurationInMinutes(0);
        try {
            manager1.createTask(task2);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        Epic epic1 = new Epic();
        epic1.setName("E1");
        epic1.setDescription("Description E1");
        manager1.createEpic(epic1);

        try {
            manager1.getTaskById(2);
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
        }

        Subtask subTask1 = new Subtask();
        subTask1.setName("S1");
        subTask1.setId(1);
        subTask1.setDescription("Description S1");
        subTask1.setStatus(Status.DONE);
        subTask1.setStartTime(Instant.parse("2023-04-01T00:00:00.000Z"));
        subTask1.setDurationInMinutes(Duration.ofDays(31).toMinutes());
        subTask1.setEpicId(3);
        try {
            manager1.createSubtask(subTask1);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        manager1.getSubtaskById(4);

        HttpTaskManager manager2 = (HttpTaskManager) Managers.getDefaults();
        manager2.load();

        System.out.println("Task maps are identical: " + manager1.getAllTasks().equals(manager2.getAllTasks()));
        System.out.println("Subtask maps are identical: " + manager1.getAllSubtasks().equals(manager2.getAllSubtasks()));
        System.out.println("Epic maps are identical: " + manager1.getAllEpics().equals(manager2.getAllEpics()));
        System.out.println("Histories are identical: " + manager1.getHistory().equals(manager2.getHistory()));
        System.out.println("Prioritized are identical: " + manager1.getPrioritizedTasks().equals(manager2.getPrioritizedTasks()));
        System.out.println();

        System.out.println(manager1.getAllTasks());
        System.out.println(manager1.getAllSubtasks());
        System.out.println(manager1.getAllEpics());
        System.out.println(manager1.getPrioritizedTasks());
        System.out.println();


        Subtask subTask2 = new Subtask();
        subTask2.setId(1);
        subTask2.setDescription("Description S2");
        subTask2.setName("S2");
        subTask2.setStatus(Status.NEW);
        subTask2.setStartTime(null);
        subTask2.setDurationInMinutes(0);
        subTask2.setEpicId(3);
        manager1.createSubtask(subTask2);

        Subtask subTask3 = new Subtask();
        subTask3.setDescription("Description S3");
        subTask3.setId(1);
        subTask3.setName("S3");
        subTask3.setStatus(Status.IN_PROGRESS);
        subTask3.setStartTime(Instant.parse("2023-05-04T00:00:00.001Z"));
        subTask3.setDurationInMinutes(Duration.ofDays(31).toMinutes());
        subTask3.setEpicId(3);
        manager1.createSubtask(subTask3);

        Epic epic2 = new Epic();
        epic2.setDescription("Description E2");
        epic2.setName("E2");
        manager1.createEpic(epic2);

        System.out.println(manager1.getAllTasks());
        System.out.println(manager1.getAllSubtasks());
        System.out.println(manager1.getAllEpics());
        System.out.println(manager1.getPrioritizedTasks());
        System.out.println();

        epic1.setDescription("New Description");
        epic1.setName("New Name");
        manager1.updateEpic(epic1);

        subTask2.setDurationInMinutes((Duration.ofDays(31).toMinutes()));
        subTask2.setStartTime(Instant.parse("2020-07-01T00:00:00.000Z"));
        manager1.updateSubtask(subTask2);
        manager1.deleteTaskById(1);

        System.out.println(manager1.getAllTasks());
        System.out.println(manager1.getAllSubtasks());
        System.out.println(manager1.getAllEpics());
        System.out.println(manager1.getPrioritizedTasks());
        System.out.println();
        System.out.println(manager1.getHistory());
        System.out.println("");

        manager1.getEpicById(3);
        manager1.getEpicById(7);
        try {
            manager1.getTaskById(1);
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
        }
        manager1.getSubtaskById(5);
        manager1.getEpicById(3);
        manager1.getEpicById(7);
        manager1.getSubtaskById(4);
        manager1.getSubtaskById(6);
        manager1.getSubtaskById(5);

        System.out.println(manager1.getHistory());
        System.out.println("");

        manager2.load();

        subTask2.setEpicId(6);
        try {
            manager1.updateSubtask(subTask2);
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(manager2.getAllTasks());
        System.out.println(manager2.getAllSubtasks());
        System.out.println(manager2.getAllEpics());
        System.out.println(manager2.getPrioritizedTasks());
        System.out.println();

        manager2.load();

        System.out.println("Task maps are identical :" + manager1.getAllTasks().equals(manager2.getAllTasks()));
        System.out.println("Subtask maps are identical :" + manager1.getAllSubtasks().equals(manager2.getAllSubtasks()));
        System.out.println("Epic maps are identical :" + manager1.getAllEpics().equals(manager2.getAllEpics()));
        System.out.println("Histories are identical: " + manager1.getHistory().equals(manager2.getHistory()));
        System.out.println("Prioritized are identical: " + manager1.getPrioritizedTasks().equals(manager2.getPrioritizedTasks()));
        kvServer.stop();
    }
}
