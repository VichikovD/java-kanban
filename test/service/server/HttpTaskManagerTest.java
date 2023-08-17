package service.server;

import model.*;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import service.TaskManager;
import service.TaskManagerTest;
import service.server.HttpTaskManager;
import service.server.KVServer;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    KVServer kvServer = new KVServer();

    public HttpTaskManagerTest() throws IOException {
    }

    @Override
    public HttpTaskManager getTaskManager() {
        return new HttpTaskManager();
    }

    @BeforeEach
    public void beforeEach() {
        kvServer.start();
        taskManager = getTaskManager();
        madeTask1 = taskManager.createTask(new Task(2, "name", Status.NEW, "description"));
        madeEpic1 = taskManager.createEpic(new Epic(3, "Epic1 name", "Epic1 description"));
        madeSubtask1 = taskManager.createSubtask(new Subtask(4, "Subtask1 name", Status.DONE,
                "Subtask1 description", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
    }

    @AfterEach
    public void stopKVServer() {
        kvServer.stop();
    }

    @Test
    public void savingAndLoadingNoTasks() {
        taskManager = new HttpTaskManager();
        List<Task> expectedTasksMap = List.of();
        List<Subtask> expectedSubtasksMap = List.of();
        List<Epic> expectedEpicsMap = List.of();
        List<Task> expectedPrioritizedSet = List.of();
        List<Task> expectedHistoryList = List.of();

        taskManager.save();
        HttpTaskManager taskManagerLoaded = new HttpTaskManager("http://localhost:8010/");

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
        Epic expectedEpic = new Epic(2, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(getStandardSubask(3, 2), getStandardTask());
        List<Task> expectedHistoryList = List.of(getStandardSubask(3, 2), getStandardTask());

        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);
        HttpTaskManager taskManagerLoaded = null;
        taskManagerLoaded = new HttpTaskManager("http://localhost:8010/");

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
        Epic expectedEpic = new Epic(2, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(getStandardSubask(3, 2), getStandardTask());
        List<Task> expectedHistoryList = List.of();

        HttpTaskManager taskManagerLoaded = new HttpTaskManager("http://localhost:8010/");

        assertEquals(expectedTasksMap, taskManagerLoaded.getAllTasks());
        assertEquals(expectedSubtasksMap, taskManagerLoaded.getAllSubtasks());
        assertEquals(expectedEpicsMap, taskManagerLoaded.getAllEpics());
        assertEquals(expectedHistoryList, taskManagerLoaded.getHistory());
        assertEquals(List.of(3), taskManagerLoaded.getEpicById(2).getSubtasksIdList());
        assertEquals(expectedPrioritizedSet, taskManagerLoaded.getPrioritizedTasks());
    }

    @Test
    public void savingAndLoadingEpicWithoutSubtasks() {
        taskManager = new HttpTaskManager();
        Epic expectedEpic = new Epic(1, "Epic1 name", "Epic1 description");

        taskManager.createEpic(getStandardEpic());
        HttpTaskManager taskManagerLoaded = null;
        taskManagerLoaded = new HttpTaskManager("http://localhost:8010/");
        Epic actualllyLoadedEpic = taskManagerLoaded.getEpicById(1);

        assertEquals(expectedEpic, actualllyLoadedEpic, "Saved program state not correctly");
    }

    Task getStandardTask() {
        return new Task(1, "name", Status.NEW, "description");
    }

    Subtask getStandardSubask(int subtaskId, int epicId) {
        return madeSubtask1 = new Subtask(subtaskId, "Subtask1 name", Status.DONE,
                "Subtask1 description", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), epicId);
    }

    Epic getStandardEpic() {
        return new Epic(2, "Epic1 name", "Epic1 description");
    }
}