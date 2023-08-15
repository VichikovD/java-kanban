package service.server;

import model.*;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import service.TaskManagerTest;
import service.server.HttpTaskManager;
import service.server.KVServer;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    KVServer kvServer= new KVServer();

    public HttpTaskManagerTest() throws IOException {
    }

    @Override
    public HttpTaskManager getTaskManager() {
        try {
            return new HttpTaskManager();
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        }
    }
    @BeforeEach
    public void beforeEach() {
        kvServer.start();
        taskManager = getTaskManager();
    }

    @AfterEach
    public void stopKVServer() {
        kvServer.stop();
    }

    @Test
    public void savingAndLoadingNoTasks() {
        List<Task> expectedTasksMap = List.of();
        List<Subtask> expectedSubtasksMap = List.of();
        List<Epic> expectedEpicsMap = List.of();
        List<Task> expectedPrioritizedSet = List.of();
        List<Task> expectedHistoryList = List.of();

        taskManager.save();
        HttpTaskManager taskManagerLoaded = null;
        try {
            taskManagerLoaded = new HttpTaskManager("http://localhost:8010/");
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        }

        assertTrue(expectedTasksMap.equals(taskManagerLoaded.getAllTasks())
                        && expectedSubtasksMap.equals(taskManagerLoaded.getAllSubtasks())
                        && expectedEpicsMap.equals(taskManagerLoaded.getAllEpics())
                        && expectedHistoryList.equals(taskManagerLoaded.getHistory())
                        && expectedPrioritizedSet.equals(taskManagerLoaded.getPrioritizedTasks()),
                "Loaded not empty HttpTaskManager");
    }

    @Test
    public void savingAndLoadingThreeTasksAndHistory() {
        List<Task> expectedTasksMap = List.of(getStandardTask());
        List<Subtask> expectedSubtasksMap = List.of(getStandardSubask(3, 2));
        Epic expectedEpic = new Epic(2, "epic name", Status.NEW,"epic description",Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(getStandardSubask(3, 2), getStandardTask());
        List<Task> expectedHistoryList = List.of(getStandardSubask(3, 2), getStandardTask());

        taskManager.createTask(getStandardTask());
        taskManager.createEpic(getStandardEpic());
        taskManager.createSubtask(getStandardSubask(2, 2));
        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);
        HttpTaskManager taskManagerLoaded = null;
        try {
            taskManagerLoaded = new HttpTaskManager("http://localhost:8010/");
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedTasksMap, taskManagerLoaded.getAllTasks());
        assertEquals(expectedSubtasksMap, taskManagerLoaded.getAllSubtasks());
        assertEquals(expectedEpicsMap, taskManagerLoaded.getAllEpics());
        assertEquals(expectedHistoryList, taskManagerLoaded.getHistory());
        assertEquals(List.of(3), taskManagerLoaded.getEpicById(2).getSubtasksIdList());
        assertEquals(expectedPrioritizedSet, taskManagerLoaded.getPrioritizedTasks());
    }

    @Test
    public void savingAndLoading3TasksWithoutHistory() {
        List<Task> expectedTasksMap = List.of(getStandardTask());
        List<Subtask> expectedSubtasksMap = List.of(getStandardSubask(3, 2));
        Epic expectedEpic = new Epic(2, "epic name", Status.NEW,"epic description",Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(getStandardSubask(3, 2), getStandardTask());
        List<Task> expectedHistoryList = List.of();

        taskManager.createTask(getStandardTask());
        taskManager.createEpic(getStandardEpic());
        taskManager.createSubtask(getStandardSubask(2, 2));
        HttpTaskManager taskManagerLoaded = null;
        try {
            taskManagerLoaded = new HttpTaskManager("http://localhost:8010/");
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedTasksMap, taskManagerLoaded.getAllTasks());
        assertEquals(expectedSubtasksMap, taskManagerLoaded.getAllSubtasks());
        assertEquals(expectedEpicsMap, taskManagerLoaded.getAllEpics());
        assertEquals(expectedHistoryList, taskManagerLoaded.getHistory());
        assertEquals(List.of(3), taskManagerLoaded.getEpicById(2).getSubtasksIdList());
        assertEquals(expectedPrioritizedSet, taskManagerLoaded.getPrioritizedTasks());
    }

    @Test
    public void savingAndLoadingEpicWithoutSubtasks() {
        Epic expectedEpic = getStandardEpic();

        taskManager.createEpic(getStandardEpic());
        HttpTaskManager taskManagerLoaded = null;
        try {
            taskManagerLoaded = new HttpTaskManager("http://localhost:8010/");
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        }
        Epic actualllyLoadedEpic = taskManagerLoaded.getEpicById(1);

        assertEquals(expectedEpic, actualllyLoadedEpic, "Saved program state not correctly");
    }

    Task getStandardTask() {
        return new Task(1, "task name", Status.NEW, "task description");
    }

    Subtask getStandardSubask(int subtaskId, int epicId) {
        return new Subtask(subtaskId, "subtask name", Status.NEW, "subtask description", Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes(), epicId);
    }

    Epic getStandardEpic() {
        return new Epic(1, "epic name", "epic description");
    }

}