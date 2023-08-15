package service.file;

import model.*;
import service.TaskManagerTest;
import service.file.FileBackedTasksManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    @Override
    public FileBackedTasksManager getTaskManager() {
        String path = "test/TestFile.csv";
        return new FileBackedTasksManager(path);
    }

    /*@Test
    public void ShouldThrowManagerSaveException(){
        String path = "notExisting.csv";
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            System.out.println("'Saving to not existing file' Test is not carried out.");
        }
        taskManager = new FileBackedTasksManager(path);
        assertThrows(
                ManagerSaveException.class,
                () -> taskManager.save());
    }*/

    @Test
    public void savingNoTasks() {
        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,epic,startTime,durationInMinutes"
                + lSeparator + lSeparator;
        taskManager.save();
        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("test/TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'Saving' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void savingThreeTasksAndHistory() {
        Task madeTask1 = taskManager.createTask(new Task(1, "T1", Status.NEW, "Description T1"));

        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "E2", "Description E2"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);

        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,epic,startTime,durationInMinutes" + lSeparator
                + "1,TASK,T1,NEW,Description T1,null,null,0" + lSeparator
                + "2,EPIC,E2,DONE,Description E2,null,2023-01-01T00:00:00Z,44640" + lSeparator
                + "3,SUBTASK,S1,DONE,Description S1,2,2023-01-01T00:00:00Z,44640" + lSeparator
                + lSeparator
                + "3,1";
        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("test/TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'Saving' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void saving3TasksWithoutHistory() {
        Task madeTask1 = taskManager.createTask(new Task(1, "T1", Status.NEW, "Description T1",
                Instant.parse("2023-03-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "E2", "Description E2"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,epic,startTime,durationInMinutes" + lSeparator
                + "1,TASK,T1,NEW,Description T1,null,2023-03-01T00:00:00Z,44640" + lSeparator
                + "2,EPIC,E2,DONE,Description E2,null,2023-01-01T00:00:00Z,44640" + lSeparator
                + "3,SUBTASK,S1,DONE,Description S1,2,2023-01-01T00:00:00Z,44640" + lSeparator
                + lSeparator;
        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("test/TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'Saving' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void savingEpicWithoutSubtasks() {
        Epic madeEpic2 = taskManager.createEpic(new Epic(1, "E1", "Description E1"));

        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,epic,startTime,durationInMinutes" + lSeparator
                + "1,EPIC,E1,NEW,Description E1,null,null,0" + lSeparator
                + lSeparator;
        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("test/TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'Saving' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void loadEpicWithoutSubtasks() {
        taskManager.createEpic(new Epic(2, "E1", Status.NEW, "Description E1",null, 0));

        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.load("test/TestFile.csv");

        List<Task> expectedTasksMap = List.of();
        List<Subtask> expectedSubtasksMap = List.of();
        Epic expectedEpic = new Epic(1, "E1", Status.NEW, "Description E1",null, 0);
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of();
        List<Task> expectedHistoryList = List.of();

        assertEquals(expectedTasksMap, tasksManagerLoaded.getAllTasks());
        assertEquals(expectedSubtasksMap, tasksManagerLoaded.getAllSubtasks());
        assertEquals(expectedEpicsMap, tasksManagerLoaded.getAllEpics());
        assertEquals(expectedHistoryList, tasksManagerLoaded.getHistory());
        assertEquals(List.of(), tasksManagerLoaded.getEpicById(1).getSubtasksIdList());
        assertEquals(expectedPrioritizedSet, tasksManagerLoaded.getPrioritizedTasks());
    }

    @Test
    public void load3TasksWithoutHistory() {
        Task madeTask1 = taskManager.createTask(new Task(1, "T1", Status.NEW, "Description T1"));

        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "E2", "Description E2"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.load("test/TestFile.csv");

        Epic expectedEpic = new Epic(2, "E2",Status.DONE, "Description E2",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);

        List<Task> expectedTasksMap = List.of(new Task(1, "T1", Status.NEW, "Description T1", null, 0));
        List<Subtask> expectedSubtasksMap = List.of(new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "T1", Status.NEW, "Description T1", null, 0));
        List<Task> expectedHistoryList = List.of();

        assertTrue(expectedTasksMap.equals(tasksManagerLoaded.getAllTasks())
                        && expectedSubtasksMap.equals(tasksManagerLoaded.getAllSubtasks())
                        && expectedEpicsMap.equals(tasksManagerLoaded.getAllEpics())
                        && expectedHistoryList.equals(tasksManagerLoaded.getHistory())
                        && List.of(3).equals(tasksManagerLoaded.getEpicById(2).getSubtasksIdList())
                        && expectedPrioritizedSet.equals(tasksManagerLoaded.getPrioritizedTasks()),
                "Loaded not 3 Tasks with prioritized tasks list but without History tasks list");
    }

    @Test
    public void load3TasksWithHistory() {
        Task madeTask1 = taskManager.createTask(new Task(1, "T1", Status.NEW, "Description T1"));

        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "E2", "Description E2"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);

        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.load("test/TestFile.csv");

        Epic expectedEpic = new Epic(2, "E2",Status.DONE, "Description E2",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);

        List<Task> expectedTasksMap = List.of(
                new Task(1, "T1", Status.NEW, "Description T1", null, 0));
        List<Subtask> expectedSubtasksMap = List.of(
                new Subtask(3, "S1", Status.DONE, "Description S1", Instant.parse("2023-01-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 2));
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(
                new Subtask(3, "S1", Status.DONE, "Description S1", Instant.parse("2023-01-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "T1", Status.NEW, "Description T1", null, 0));
        List<Task> expectedHistoryList = List.of(
                new Subtask(3, "S1", Status.DONE, "Description S1", Instant.parse("2023-01-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "T1", Status.NEW, "Description T1", null, 0));

        assertTrue(expectedTasksMap.equals(tasksManagerLoaded.getAllTasks())
                        && expectedSubtasksMap.equals(tasksManagerLoaded.getAllSubtasks())
                        && expectedPrioritizedSet.equals(tasksManagerLoaded.getPrioritizedTasks())
                        && expectedHistoryList.equals(tasksManagerLoaded.getHistory())
                        && expectedEpicsMap.equals(tasksManagerLoaded.getAllEpics())
                        && List.of(3).equals(tasksManagerLoaded.getEpicById(2).getSubtasksIdList()),
                "Loaded not 3 Tasks with History tasks list and prioritized  tasks list");
    }
}