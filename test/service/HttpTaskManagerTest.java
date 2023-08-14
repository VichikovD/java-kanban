package service;

import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import service.server.HttpTaskManager;
import service.server.KVServer;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    KVServer kvServer;
    @Override
    public HttpTaskManager beforeEach() {
        try {
            kvServer = new KVServer();
            kvServer.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());;
        }
        return new HttpTaskManager();
    }

    @AfterEach
    public void stopKVServer() {
        kvServer.stop();
    }

    @Test
    public void savingAndLoadingNoTasks() {
        taskManager.save();

        HttpTaskManager taskManagerLoaded = new HttpTaskManager();
        taskManagerLoaded.resetAndLoadFromKVServer();

        List<Task> expectedTasksMap = List.of();
        List<Subtask> expectedSubtasksMap = List.of();
        List<Epic> expectedEpicsMap = List.of();
        List<Task> expectedPrioritizedSet = List.of();
        List<Task> expectedHistoryList = List.of();

        assertTrue(expectedTasksMap.equals(taskManagerLoaded.getAllTasks())
                        && expectedSubtasksMap.equals(taskManagerLoaded.getAllSubtasks())
                        && expectedEpicsMap.equals(taskManagerLoaded.getAllEpics())
                        && expectedHistoryList.equals(taskManagerLoaded.getHistory())
                        && expectedPrioritizedSet.equals(taskManagerLoaded.getPrioritizedTasks()),
                "Loaded not empty HttpTaskManager");
    }

    @Test
    public void savingAndLoadingThreeTasksAndHistory() {
        taskManager.createTask(new Task(1, "T1", Status.NEW, "Description T1"));

        taskManager.createEpic(new Epic(2, "E2", "Description E2"));

        taskManager.createSubtask(new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);

        HttpTaskManager taskManagerLoaded = new HttpTaskManager();
        taskManagerLoaded.resetAndLoadFromKVServer();

        List<Task> expectedTasksMap = List.of(new Task(1, "T1", Status.NEW, "Description T1", null, 0));
        List<Subtask> expectedSubtasksMap = List.of(
                new Subtask(3, "S1", Status.DONE, "Description S1", Instant.parse("2023-01-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 2));
        Epic expectedEpic = new Epic(2, "E2", Status.DONE,"Description E2",Instant.parse("2023-01-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(
                new Subtask(3, "S1", Status.DONE, "Description S1", Instant.parse("2023-01-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "T1", Status.NEW, "Description T1", null, 0));
        List<Task> expectedHistoryList = List.of(
                new Subtask(3, "S1", Status.DONE, "Description S1", Instant.parse("2023-01-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "T1", Status.NEW, "Description T1", null, 0));

        assertEquals(expectedTasksMap, taskManagerLoaded.getAllTasks());
        assertEquals(expectedSubtasksMap, taskManagerLoaded.getAllSubtasks());
        assertEquals(expectedEpicsMap, taskManagerLoaded.getAllEpics());
        assertEquals(expectedHistoryList, taskManagerLoaded.getHistory());
        assertEquals(List.of(3), taskManagerLoaded.getEpicById(2).getSubtasksIdList());
        assertEquals(expectedPrioritizedSet, taskManagerLoaded.getPrioritizedTasks());
    }

    @Test
    public void savingAndLoading3TasksWithoutHistory() {
        taskManager.createTask(new Task(1, "T1", Status.NEW, "Description T1"));

        taskManager.createEpic(new Epic(2, "E2", "Description E2"));

        taskManager.createSubtask(new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        HttpTaskManager taskManagerLoaded = new HttpTaskManager();
        taskManagerLoaded.resetAndLoadFromKVServer();

        List<Task> expectedTasksMap = List.of(new Task(1, "T1", Status.NEW, "Description T1", null, 0));
        List<Subtask> expectedSubtasksMap = List.of(
                new Subtask(3, "S1", Status.DONE, "Description S1", Instant.parse("2023-01-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 2));
        Epic expectedEpic = new Epic(2, "E2", Status.DONE,"Description E2",Instant.parse("2023-01-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(
                new Subtask(3, "S1", Status.DONE, "Description S1", Instant.parse("2023-01-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "T1", Status.NEW, "Description T1", null, 0));
        List<Task> expectedHistoryList = List.of();

        assertEquals(expectedTasksMap, taskManagerLoaded.getAllTasks());
        assertEquals(expectedSubtasksMap, taskManagerLoaded.getAllSubtasks());
        assertEquals(expectedEpicsMap, taskManagerLoaded.getAllEpics());
        assertEquals(expectedHistoryList, taskManagerLoaded.getHistory());
        assertEquals(List.of(3), taskManagerLoaded.getEpicById(2).getSubtasksIdList());
        assertEquals(expectedPrioritizedSet, taskManagerLoaded.getPrioritizedTasks());
    }

    @Test
    public void savingAndLoadingEpicWithoutSubtasks() {
        taskManager.createEpic(new Epic(1, "E1", "Description E1"));

        HttpTaskManager taskManagerLoaded = new HttpTaskManager();
        taskManagerLoaded.resetAndLoadFromKVServer();

        Epic expectedEpic = new Epic(1, "E1", Status.NEW,"Description E1", null, 0);
        Epic actualllyLoadedEpic = taskManagerLoaded.getEpicById(1);
        assertEquals(expectedEpic, actualllyLoadedEpic, "Saved program state not correctly");
    }

}