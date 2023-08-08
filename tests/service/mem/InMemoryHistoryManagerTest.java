package service.mem;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    InMemoryTaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    public void getHistory() {
        List<Task> expectedHistory = List.of();
        List<Task> actualHistory = taskManager.getHistory();
        assertEquals(expectedHistory, actualHistory, "Not empty history returned");
    }

    @Test
    public void addAndGetHistorySameGoToTail() {
        Task madeTask1 = new Task(1, "name1", Status.NEW, "description1");
        taskManager.createTask(madeTask1);
        taskManager.getTaskById(1);

        List<Task> expectedHistory1 = List.of(new Task(1, "name1", Status.NEW, "description1", null, 0));
        List<Task> actualHistory1 = taskManager.getHistory();
        assertEquals(expectedHistory1, actualHistory1, "Task not added to history or Not correct history returned");

        Epic madeEpic2 = new Epic(2, "Epic2 name", "Epic2 description");
        taskManager.createEpic(madeEpic2);
        taskManager.getEpicById(2);

        List<Task> expectedHistory2 = List.of(
                new Task(1, "name1", Status.NEW, "description1", null, 0),
                new Epic(2, "Epic2 name", Status.NEW, "Epic2 description", null, 0, List.of()));
        List<Task> actualHistory2 = taskManager.getHistory();
        assertEquals(expectedHistory2, actualHistory2, "Task not added to end of history or Not correct history returned");

        taskManager.getTaskById(1);
        List<Task> expectedHistory3 = List.of(
                new Epic(2, "Epic2 name", Status.NEW, "Epic2 description", null, 0, List.of()),
                new Task(1, "name1", Status.NEW, "description1", null, 0));
        List<Task> actualHistory3 = taskManager.getHistory();
        assertEquals(expectedHistory3, actualHistory3, "Duplicated task not removed and added to end of history " +
                "or Not correct history returned");
    }

    @Test
    public void removeFromTail() {
        Task madeTask1 = new Task(1, "name1", Status.NEW, "description1");
        taskManager.createTask(madeTask1);
        taskManager.getTaskById(1);

        Epic madeEpic1 = new Epic(2, "Epic2 name", "Epic2 description");
        taskManager.createEpic(madeEpic1);
        taskManager.getEpicById(2);

        Subtask madeSubtask1 = new Subtask(3, "Subtask name1", Status.DONE, "Subtask description1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(),  2);
        taskManager.createSubtask(madeSubtask1);
        taskManager.getSubtaskById(3);

        taskManager.deleteSubtaskById(3);

        List<Task> expectedHistory1 = List.of(
                new Task(1, "name1", Status.NEW, "description1", null, 0),
                new Epic(2, "Epic2 name", Status.NEW, "Epic2 description", null, 0, List.of()));
        List<Task> actualHistory1 = taskManager.getHistory();
        assertEquals(expectedHistory1, actualHistory1, "Task not added to history or Not correct history returned");
    }

    @Test
    public void removeFromMid() {
        Task madeTask1 = new Task(1, "name1", Status.NEW, "description1",Instant.parse("2023-04-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes());
        taskManager.createTask(madeTask1);
        taskManager.getTaskById(1);

        Epic madeEpic1 = new Epic(2, "Epic2 name", "Epic2 description");
        taskManager.createEpic(madeEpic1);
        taskManager.getEpicById(2);

        Subtask madeSubtask1 = new Subtask(3, "Subtask name1", Status.DONE, "Subtask description1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);
        taskManager.createSubtask(madeSubtask1);
        taskManager.getSubtaskById(3);

        Task madeTask2 = new Task(4, "name2", Status.NEW, "description2");
        taskManager.createTask(madeTask2);
        taskManager.getTaskById(4);

        taskManager.deleteEpicById(2);

        List<Task> expectedHistory1 = List.of(
                new Task(1, "name1", Status.NEW, "description1", Instant.parse("2023-04-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes()),
                new Task(4, "name2", Status.NEW, "description2", null, 0));
        List<Task> actualHistory1 = taskManager.getHistory();
        assertEquals(expectedHistory1, actualHistory1, "History not updated or Not correct history returned");
    }

    @Test
    public void removeFromHead() {
        Task madeTask1 = new Task(1, "name1", Status.NEW, "description1");
        taskManager.createTask(madeTask1);
        taskManager.getTaskById(1);

        Epic madeEpic1 = new Epic(2, "Epic2 name", "Epic2 description");
        taskManager.createEpic(madeEpic1);
        taskManager.getEpicById(2);

        Subtask madeSubtask1 = new Subtask(3, "Subtask name1", Status.DONE, "Subtask description1",
                Instant.parse("2023-04-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);
        taskManager.createSubtask(madeSubtask1);
        taskManager.getSubtaskById(3);

        taskManager.deleteTaskById(1);

        List<Task> expectedHistory1 = List.of(
                new Epic(2, "Epic2 name", Status.DONE, "Epic2 description", Instant.parse("2023-04-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), List.of(3)),
                new Subtask(3, "Subtask name1", Status.DONE, "Subtask description1",
                        Instant.parse("2023-04-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        List<Task> actualHistory1 = taskManager.getHistory();
        assertEquals(expectedHistory1, actualHistory1, "Task not added to history or Not correct history returned");
    }
}

