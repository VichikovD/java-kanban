package service.file;

import model.*;
import service.TaskManagerTest;

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

    @Test
    public void savingNoTasks() {
        FileBackedTasksManager tasksManager2 = new FileBackedTasksManager("test/TestFile.csv");
        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,epic,startTime,durationInMinutes"
                + lSeparator + lSeparator;

        tasksManager2.save();
        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("test/TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'savingNoTasks' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void savingThreeTasksAndHistory() {
        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);
        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,epic,startTime,durationInMinutes" + lSeparator
                + "1,TASK,name,NEW,description,null,null,0" + lSeparator
                + "2,EPIC,Epic1 name,DONE,Epic1 description,null,2023-01-01T00:00:00Z,44640" + lSeparator
                + "3,SUBTASK,Subtask1 name,DONE,Subtask1 description,2,2023-01-01T00:00:00Z,44640" + lSeparator
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
        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,epic,startTime,durationInMinutes" + lSeparator
                + "1,TASK,name,NEW,description,null,null,0" + lSeparator
                + "2,EPIC,Epic1 name,DONE,Epic1 description,null,2023-01-01T00:00:00Z,44640" + lSeparator
                + "3,SUBTASK,Subtask1 name,DONE,Subtask1 description,2,2023-01-01T00:00:00Z,44640" + lSeparator
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
        FileBackedTasksManager taskManager2 = new FileBackedTasksManager("test/TestFile.csv");
        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,epic,startTime,durationInMinutes" + lSeparator
                + "1,EPIC,E1,NEW,Description E1,null,null,0" + lSeparator
                + lSeparator;

        Epic madeEpic2 = taskManager2.createEpic(new Epic(1, "E1", "Description E1"));
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
        FileBackedTasksManager taskManager2 = new FileBackedTasksManager("test/TestFile.csv");
        List<Task> expectedTasksMap = List.of();
        List<Subtask> expectedSubtasksMap = List.of();
        Epic expectedEpic = new Epic(1, "E1", Status.NEW, "Description E1",null, 0);
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of();
        List<Task> expectedHistoryList = List.of();

        taskManager2.createEpic(new Epic(2, "E1", Status.NEW, "Description E1",null, 0));
        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.load("test/TestFile.csv");

        assertEquals(expectedTasksMap, tasksManagerLoaded.getAllTasks());
        assertEquals(expectedSubtasksMap, tasksManagerLoaded.getAllSubtasks());
        assertEquals(expectedEpicsMap, tasksManagerLoaded.getAllEpics());
        assertEquals(expectedHistoryList, tasksManagerLoaded.getHistory());
        assertEquals(List.of(), tasksManagerLoaded.getEpicById(1).getSubtasksIdList());
        assertEquals(expectedPrioritizedSet, tasksManagerLoaded.getPrioritizedTasks());
    }
    /*new Task(1, "name", Status.NEW, "description");
    new Epic(2, "Epic1 name", "Epic1 description");
    new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
    Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);*/
    @Test
    public void load3TasksWithoutHistory() {
        List<Task> expectedTasksMap = List.of(new Task(1, "name", Status.NEW, "description"));
        List<Subtask> expectedSubtasksMap = List.of(new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        Epic expectedEpic = new Epic(2, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(
                new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                        Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "name", Status.NEW, "description"));
        List<Task> expectedHistoryList = List.of();

        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.load("test/TestFile.csv");

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
        List<Task> expectedTasksMap = List.of(new Task(1, "name", Status.NEW, "description"));
        List<Subtask> expectedSubtasksMap = List.of(new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        Epic expectedEpic = new Epic(2, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));
        List<Epic> expectedEpicsMap = List.of(expectedEpic);
        List<Task> expectedPrioritizedSet = List.of(
                new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                        Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "name", Status.NEW, "description"));
        List<Task> expectedHistoryList = List.of(new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                        Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "name", Status.NEW, "description"));
        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);

        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.load("test/TestFile.csv");

        assertTrue(expectedTasksMap.equals(tasksManagerLoaded.getAllTasks())
                        && expectedSubtasksMap.equals(tasksManagerLoaded.getAllSubtasks())
                        && expectedEpicsMap.equals(tasksManagerLoaded.getAllEpics())
                        && expectedHistoryList.equals(tasksManagerLoaded.getHistory())
                        && List.of(3).equals(tasksManagerLoaded.getEpicById(2).getSubtasksIdList())
                        && expectedPrioritizedSet.equals(tasksManagerLoaded.getPrioritizedTasks()),
                "Loaded not 3 Tasks with prioritized tasks list but without History tasks list");
    }
}