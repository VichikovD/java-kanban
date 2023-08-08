package service.file;

import model.*;
import service.mem.InMemoryTaskManager;
import service.file.exception.ManagerLoadException;
import service.file.exception.ManagerSaveException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.TaskManagerTest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import service.file.exception.ManagerSaveException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    @Override
    public FileBackedTasksManager beforeEach() {
        String path = "TestFile.csv";

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
        String expectedToBeSaved = "id,type,name,status,description,startTime,durationInMinutes,epic"
                + lSeparator + lSeparator;
        taskManager.save();
        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'Saving' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void savingThreeTasksAndHistory() {
        Task madeTask1 = new Task(1, "T1", Status.NEW, "Description T1");
        taskManager.createTask(madeTask1);

        Epic madeEpic2 = new Epic(2, "E2", "Description E2");
        taskManager.createEpic(madeEpic2);

        Subtask madeSubtask1 = new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);
        taskManager.createSubtask(madeSubtask1);

        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);

        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,startTime,durationInMinutes,epic" + lSeparator
                + "1,TASK,T1,NEW,Description T1,null,0," + lSeparator
                + "2,EPIC,E2,DONE,Description E2,2023-01-01T00:00:00Z,44640," + lSeparator
                + "3,SUBTASK,S1,DONE,Description S1,2023-01-01T00:00:00Z,44640,2" + lSeparator
                + lSeparator
                + "3,1";
        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'Saving' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void saving3TasksWithoutHistory() {
        Task madeTask1 = new Task(1, "T1", Status.NEW, "Description T1",Instant.parse("2023-03-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes());
        taskManager.createTask(madeTask1);

        Epic madeEpic2 = new Epic(2, "E2", "Description E2");
        taskManager.createEpic(madeEpic2);

        Subtask madeSubtask1 = new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);
        taskManager.createSubtask(madeSubtask1);

        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,startTime,durationInMinutes,epic" + lSeparator
                + "1,TASK,T1,NEW,Description T1,2023-03-01T00:00:00Z,44640," + lSeparator
                + "2,EPIC,E2,DONE,Description E2,2023-01-01T00:00:00Z,44640," + lSeparator
                + "3,SUBTASK,S1,DONE,Description S1,2023-01-01T00:00:00Z,44640,2" + lSeparator
                + lSeparator;

        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'Saving' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void savingEpicWithoutSubtasks() {
        Epic madeEpic2 = new Epic(1, "E1", "Description E1");
        taskManager.createEpic(madeEpic2);

        String lSeparator = System.lineSeparator();
        String expectedToBeSaved = "id,type,name,status,description,startTime,durationInMinutes,epic" + lSeparator
                + "1,EPIC,E1,NEW,Description E1,null,0," + lSeparator
                + lSeparator;
        String actuallySaved = null;
        try {
            actuallySaved = Files.readString(Path.of("TestFile.csv"));
        } catch (IOException e) {
            System.out.println("'Saving' Test is not carried out.");
        }

        assertEquals(expectedToBeSaved, actuallySaved, "Saved program state not correctly");
    }

    @Test
    public void loadEpicWithoutSubtasks() {
        taskManager.createEpic(new Epic(1, "E1", Status.NEW, "Description E1",null, 0, List.of()));

        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.loadFromFile("TestFile.csv");
        assertEquals(taskManager, tasksManagerLoaded, "Loaded not 1 Epic");
    }

    @Test
    public void load3TasksWithoutHistory() {
        Task madeTask1 = new Task(1, "T1", Status.NEW, "Description T1");
        taskManager.createTask(madeTask1);

        Epic madeEpic2 = new Epic(2, "E2", "Description E2");
        taskManager.createEpic(madeEpic2);

        Subtask madeSubtask1 = new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);
        taskManager.createSubtask(madeSubtask1);

        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.loadFromFile("TestFile.csv");
        assertEquals(taskManager, tasksManagerLoaded, "Loaded not 3 Tasks Without History");
    }

    @Test
    public void load3TasksWithHistory() {
        Task madeTask1 = new Task(1, "T1", Status.NEW, "Description T1");
        taskManager.createTask(madeTask1);

        Epic madeEpic2 = new Epic(2, "E2", "Description E2");
        taskManager.createEpic(madeEpic2);

        Subtask madeSubtask1 = new Subtask(3, "S1", Status.DONE, "Description S1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);
        taskManager.createSubtask(madeSubtask1);

        taskManager.getSubtaskById(3);
        taskManager.getTaskById(1);

        FileBackedTasksManager tasksManagerLoaded = FileBackedTasksManager.loadFromFile("TestFile.csv");
        assertEquals(taskManager, tasksManagerLoaded, "Loaded not 3 Tasks Without History");
    }
}