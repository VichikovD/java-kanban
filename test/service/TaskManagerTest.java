package service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.mem.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.*;

abstract public class TaskManagerTest<T extends TaskManager> {
    public T taskManager;
    public Task madeTask1;
    public Subtask madeSubtask1;
    public Epic madeEpic1;

    @BeforeEach
    public void beforeEach() {
        taskManager = getTaskManager();
        madeTask1 = taskManager.createTask(new Task(2, "name", Status.NEW, "description"));
        madeEpic1 = taskManager.createEpic(new Epic(3, "Epic1 name", "Epic1 description"));
        madeSubtask1 = taskManager.createSubtask(new Subtask(4, "Subtask1 name", Status.DONE,
                "Subtask1 description", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
    }

    abstract public T getTaskManager();

    @Test
    public void getPrioritized1ShouldReturn2NullTimeTasks() {
        List<Task> expectedList = List.of(
                new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                        Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "name", Status.NEW, "description", null, 0),
                new Task(4, "name", Status.NEW, "description", null, 0));

        Task madeTask2 = taskManager.createTask(new Task(2, "name", Status.NEW, "description"));
        List<Task> actualList = taskManager.getPrioritizedTasks();

        assertEquals(expectedList, actualList, "Returned not 2 null startTime tasks");
    }

    @Test
    public void getPrioritized2ShouldAddOnly2Subtask() {
        Subtask madeSubtask2 = new Subtask(4, "Subtask2 name", Status.DONE, "Subtask2 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1);
        List<Task> expectedList = List.of(
                new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                        Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "name", Status.NEW, "description"));
        List<Task> actualList = taskManager.getPrioritizedTasks();

        assertThrows(IllegalArgumentException.class,
                () -> taskManager.createTask(madeSubtask2),
                "Exception is not thrown when task start-time coincide");
        assertEquals(expectedList, actualList, "Returned not 1 subtasks");
    }

    @Test
    public void createTask1ShouldAddCopyOfTaskButWithCorrectIdToTasksMap() {
        Task expectedTask = new Task(1, "name", Status.NEW, "description", null, 0);

        assertEquals(expectedTask, madeTask1, "Task not created correctly or getTaskById didn't return correct Task");
    }

    @Test
    public void createTask2ShouldNotCreateSecondTask() {
        Task madeTask2 = new Task(2, "name", Status.NEW, "description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        List<Task> expectedTasks = List.of(new Task(1, "name", Status.NEW, "description",
                null, 0));
        List<Task> actualTasks = List.of(madeTask1);

        assertThrows(IllegalArgumentException.class,
                () -> taskManager.createTask(madeTask2),
                "Exception is not thrown when task start-time coincide");
        assertEquals(expectedTasks, actualTasks, "Not 1 task created or getTaskById didn't return correct Task");
    }

    @Test
    public void updateTask1ShouldUpdateTaskInTasksMap() {
        Task updatedTask = taskManager.updateTask(new Task(1, "New Name", Status.DONE, "New Description",
                Instant.parse("2022-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));
        Task expectedTask = new Task(1, "New Name", Status.DONE, "New Description",
                Instant.parse("2022-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());

        assertEquals(expectedTask, updatedTask, "Task not updated correctly or getTaskById didn't return correct Task");
    }

    @Test
    public void updateTask2ShouldUpdateTaskIvenWithSameStartTime() {
        Task expectedTask = new Task(1, "New Name", Status.DONE, "New Description",
                Instant.parse("2022-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());

        taskManager.updateTask(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2022-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));
        Task updatedTask = taskManager.updateTask(new Task(1, "New Name", Status.DONE, "New Description",
                Instant.parse("2022-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        assertEquals(expectedTask, updatedTask, "Task not updated correctly or getTaskById didn't return correct Task");
    }

    @Test
    public void createSubtask1ShouldAddSubtaskToSubtasksMapAndAddToEpicSubtaskList() {
        Subtask expectedSubtask = new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);
        Subtask savedSubtask = taskManager.getSubtaskById(expectedSubtask.getId());
        assertEquals(expectedSubtask, savedSubtask, "Subtask not created correctly or getSubtaskById didn't return " +
                "correct Subtask");
        Epic expectedEpic = new Epic(2, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(expectedSubtask.getId());
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));
        Epic actualEpic = taskManager.getEpicById(expectedEpic.getId());

        assertEquals(expectedSubtask, savedSubtask, "Subtask not created correctly or getSubtaskById didn't return " +
                "correct Subtask");
        assertEquals(expectedEpic, actualEpic, "Subtask not added to epic's subtascIdList or getEpicById return wrong");
        assertEquals(List.of(madeSubtask1.getId()), actualEpic.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }

    @Test
    public void createSubtask2ThrowException() {
        Subtask madeSubtask = new Subtask(4, "Subtask name", Status.NEW, "Subtask description", null, 0, 1);
        assertThrows(NotFoundException.class,
                () -> taskManager.createSubtask(madeSubtask),
                "Exception is not thrown when there is no created Epic mentioned in subtask's epicId");
    }


    @Test
    public void updateSubtask1() {
        Epic madeEpic2 = taskManager.createEpic(new Epic(4, "Epic2 name", "Epic2 description"));
        Subtask expectedSubtask = new Subtask(3, "New Subtask name", Status.IN_PROGRESS, "New Subtask description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes(), 4);
        Epic expectedUpdatedEpic1 = new Epic(2, "Epic1 name", Status.NEW, "Epic1 description", null, 0);
        Epic expectedEpic2 = new Epic(4, "Epic2 name", Status.IN_PROGRESS, "Epic2 description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes());
        expectedEpic2.addSubtaskId(expectedSubtask.getId());
        Epic actualEpic2 = taskManager.getEpicById(expectedEpic2.getId());

        Subtask updatedSubtask = taskManager.updateSubtask(new Subtask(3, "New Subtask name", Status.IN_PROGRESS, "New Subtask description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes(), 4));
        Epic actualUpdatedEpic1 = taskManager.getEpicById(expectedUpdatedEpic1.getId());

        assertEquals(expectedSubtask, updatedSubtask, "Subtask not updated correctly");
        assertEquals(expectedUpdatedEpic1, actualUpdatedEpic1, "Epic's characteristics not updated or getEpicById return wrong");
        assertEquals(List.of(), actualUpdatedEpic1.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
        assertEquals(expectedEpic2, actualEpic2, "Subtask not added to epic's subtascIdList or getEpicById return wrong");
        assertEquals(List.of(madeSubtask1.getId()), actualEpic2.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }


    @Test
    public void updateSubtask2ShouldThrowExceptionDueToNotCorrectUpdatedEpicId() {
        Subtask updatedSubtask = new Subtask(1, "New NameUpd2", Status.DONE, "New DescriptionUpd2",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes(), 1);
        assertThrows(NotFoundException.class,
                () -> taskManager.updateSubtask(updatedSubtask),
                "Exception is not thrown when update subtask's epicId not in epicsMap");
    }

    @Test
    public void createEpic1ShouldCreateEpicAsPerRequirements() {
        Epic expectedEpic = new Epic(2, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));
        Epic actualEpic = taskManager.getEpicById(expectedEpic.getId());

        assertEquals(expectedEpic, actualEpic, "Epic not created correctly");
        assertEquals(List.of(madeSubtask1.getId()), actualEpic.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }

    @Test
    public void updateEpicAsPerRequirements() {
        Epic updatedEpic = taskManager.updateEpic(new Epic(2, "New Epic name", Status.IN_PROGRESS, "new Epic description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes()));
        Epic expectedEpic = new Epic(2, "New Epic name", Status.DONE, "new Epic description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(madeSubtask1.getId());
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));
        Epic actualEpic = taskManager.getEpicById(expectedEpic.getId());

        assertEquals(expectedEpic, actualEpic, "Task not updated correctly");
        assertEquals(List.of(madeSubtask1.getId()), actualEpic.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }

    @Test
    public void getSubtasksListByEpicId() {
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(4, "Subtask2 name", Status.NEW, "Subtask2 description",
                Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        List<Subtask> expectedList = List.of(
                new Subtask(3, "Subtask1 name", Status.DONE,
                        "Subtask1 description", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Subtask(4, "Subtask2 name", Status.NEW, "Subtask2 description",
                        Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        List<Subtask> actualList = List.of(madeSubtask1, madeSubtask2);

        assertEquals(expectedList, actualList, "Subtask not updated correctly");
    }

     @Test
    public void getSubtasksListByEpicId2() {
        assertThrows(
                NotFoundException.class,
                () -> taskManager.getSubtasksListByEpicId(3));
    }

    @Test
    public void getAllTasks1() {
        Task madeTask2 = taskManager.createTask(new Task(2, "name2", Status.NEW, "description2"));
        List<Task> expected = List.of(
                new Task(1, "name", Status.NEW, "description", null, 0),
                new Task(4, "name2", Status.NEW, "description2", null, 0));

        List<Task> actual = taskManager.getAllTasks();

        assertEquals(expected, actual, "Returned not correct list of tasks");
    }

    @Test
    public void getAllSubtasks1() {
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(3, "Subtask name2", Status.NEW,
                "Subtask description2", null, 0, 2));

        List<Subtask> expected = List.of(
                new Subtask(3, "Subtask1 name", Status.DONE,
                        "Subtask1 description", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Subtask(4, "Subtask name2", Status.NEW, "Subtask description2", null, 0, 2));

        List<Subtask> actual = taskManager.getAllSubtasks();
        assertEquals(expected, actual, "Returned not correct list of subtasks");
    }

    @Test
    public void getAllEpics1() {
        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "Epic name2", "Epic description2"));
        Epic expectedEpic1 = new Epic(2, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic1.addSubtaskId(3);
        expectedEpic1.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));
        Epic expectedEpic2 = new Epic(4, "Epic name2", Status.NEW, "Epic description2", null, 0);
        List<Epic> expected = List.of(expectedEpic1, expectedEpic2);

        List<Epic> actual = taskManager.getAllEpics();

        assertEquals(expected, actual, "Returned not correct list of epics");
        assertEquals(List.of(3), taskManager.getEpicById(2).getSubtasksIdList(), "SubtasksIdList not updated or " +
                "method return wrong");
        assertEquals(List.of(), taskManager.getEpicById(4).getSubtasksIdList(), "SubtasksIdList not updated or " +
                "method return wrong");
    }

    @Test
    public void getTaskById1() {
        Task expected = new Task(1, "name", Status.NEW, "description", null, 0);

        Task actual = taskManager.getTaskById(expected.getId());

        assertEquals(expected, actual, "Received not correct Task");
    }

    @Test
    public void getTaskById2ShouldThrowException() {
        assertThrows(
                NotFoundException.class,
                () -> taskManager.getTaskById(2));
    }


    @Test
    public void getSubtaskById1() {
        Subtask expected = new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);

        Subtask actual = taskManager.getSubtaskById(expected.getId());

        assertEquals(expected, actual, "Received not correct subtask");
    }

    @Test
    public void getSubtaskById2ShouldThrowException() {
        assertThrows(
                NotFoundException.class,
                () -> taskManager.getSubtaskById(2));
    }

    @Test
    public void getEpicById() {
        Epic expectedEpic = new Epic(2, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(31)));

        Epic actual = taskManager.getEpicById(expectedEpic.getId());

        assertEquals(expectedEpic, actual, "Received not correct Task");
    }

   @Test
    public void getEpicById2ShouldThrowException() {
        assertThrows(
                NotFoundException.class,
                () -> taskManager.getEpicById(1));
    }

    @Test
    public void deleteTaskByIdShouldDelete1TaskAndDeleteFromPrioritized() {
        Task madeTask2 = taskManager.createTask(new Task(4, "name2", Status.NEW, "description2"));
        List<Task> expected = List.of(new Task(4, "name2", Status.NEW, "description2"));
        List<Task> expectedPrioritized = List.of(
                new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                        Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Task(4, "name2", Status.NEW, "description2"));

        taskManager.deleteTaskById(madeTask1.getId());
        List<Task> actual = taskManager.getAllTasks();
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();

        assertEquals(expected, actual, "Deleted not 1 task or getAllTasks returned not correct data");
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getAllTasks returned not correct data");
    }

    @Test
    public void deleteTaskById2ShouldThrowException() {
        assertThrows(
                NotFoundException.class,
                () -> taskManager.deleteTaskById(2));
    }

    @Test
    public void deleteSubtaskByIdShouldDelete1SubtaskAndUpdatePrioritizedTasksAndUpdateEpic() {
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(4, "Subtask name2", Status.NEW, "Subtask description2",
                Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        List<Subtask> expectedSubtasks = List.of(
                new Subtask(4, "Subtask name2", Status.NEW, "Subtask description2",
                        Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        List<Task> expectedPrioritized = List.of(
                new Subtask(4, "Subtask name2", Status.NEW, "Subtask description2",
                        Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2),
                new Task(1, "name", Status.NEW, "description"));
        Epic expectedEpic = new Epic(2, "Epic1 name", Status.NEW, "Epic1 description",
                Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(4);
        expectedEpic.setEndTime(Instant.parse("2023-07-01T13:00:00.000Z").plus(Duration.ofDays(31)));

        taskManager.deleteSubtaskById(madeSubtask1.getId());
        List<Subtask> actualSubtasks = taskManager.getAllSubtasks();
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();
        Epic actualEpic = taskManager.getEpicById(expectedEpic.getId());

        assertEquals(expectedSubtasks, actualSubtasks, "Deleted not 1 subtask or getAllSubtasks returned not correct data");
        assertEquals(expectedPrioritized, actualPrioritized, "Not correctly deleted from prioritized or PrioritizedTasks returned not correct data");
        assertEquals(expectedEpic, actualEpic, "Epic's characteristics not correctly updated");
        assertEquals(List.of(4), actualEpic.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }

    @Test
    public void deleteSubtaskById2ShouldThrowException() {
        assertThrows(
                NotFoundException.class,
                () -> taskManager.deleteSubtaskById(1));
    }


    @Test
    public void deleteEpicById1ShouldDelete1EpicAndDelete1SubtaskAndUpdatePrioritizedTasks() {
        Epic madeEpic2 = taskManager.createEpic(new Epic(4, "Epic2 name", "Epic2 description"));
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(5, "Subtask name2", Status.DONE,
                "Subtask description2", Instant.parse("2023-03-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 4));
        Epic expectedEpic = new Epic(4, "Epic2 name", Status.DONE, "Epic2 description",
                Instant.parse("2023-03-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(5);
        expectedEpic.setEndTime(Instant.parse("2023-03-01T13:00:00.000Z").plus(Duration.ofDays(31)));
        List<Epic> expectedEpicList = List.of(expectedEpic);
        List<Subtask> expectedSubtasks = List.of(
                new Subtask(5, "Subtask name2", Status.DONE, "Subtask description2",
                        Instant.parse("2023-03-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 4));
        List<Task> expectedPrioritized = List.of(
                new Subtask(5, "Subtask name2", Status.DONE, "Subtask description2",
                        Instant.parse("2023-03-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 4),
                new Task(1, "name", Status.NEW, "description"));

        taskManager.deleteEpicById(madeEpic1.getId());
        List<Epic> actualEpicList = taskManager.getAllEpics();
        List<Subtask> actualSubtasks = taskManager.getAllSubtasks();
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();

        assertEquals(expectedEpicList, actualEpicList, "Deleted not 1 epic or characteristics not changed " +
                "or getAllEpics returned not correct data or addSubtaskId works wrongly");
        assertEquals(expectedSubtasks, actualSubtasks, "Deleted not 1 subtask or getAllSubtasks returned not correct data");
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getPrioritizedTasks returned not correct data");
    }

    @Test
    public void deleteEpicById2ShouldThrowException() {
        assertThrows(
                NotFoundException.class,
                () -> taskManager.deleteEpicById(3));
    }

    @Test
    public void deleteAllEpics() {
        Epic madeEpic2 = taskManager.createEpic(new Epic(4, "Epic2 name", "Epic2 description"));
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(5, "Subtask name2", Status.IN_PROGRESS,
                "Subtask description2", Instant.parse("2023-04-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 4));
        List<Epic> expectedEpicList = List.of();
        List<Subtask> expectedSubtaskList = List.of();
        List<Task> expectedPrioritized = List.of(new Task(1, "name", Status.NEW, "description", null, 0));

        taskManager.deleteAllEpics();
        List<Epic> actualEpicList = taskManager.getAllEpics();
        List<Subtask> actualSubtaskList = taskManager.getAllSubtasks();
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();

        assertEquals(expectedEpicList, actualEpicList, "Not all epics deleted");
        assertEquals(expectedSubtaskList, actualSubtaskList, "Not all Subtasks deleted");
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getPrioritizedTasks returned not correct data");
    }

    @Test
    public void deleteAllSubtasks() {
        Epic madeEpic2 = taskManager.createEpic(new Epic(44, "Epic2 name", "Epic2 description"));
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(5, "Subtask name2", Status.DONE,
                "Subtask description2", Instant.parse("2022-10-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 4));
        List<Epic> expectedEpicList = List.of(
                new Epic(2, "Epic1 name", Status.NEW, "Epic1 description", null, 0),
                new Epic(4, "Epic2 name", Status.NEW, "Epic2 description", null, 0));
        List<Subtask> expectedSubtaskList = List.of();
        List<Task> expectedPrioritized = List.of(new Task(1, "name", Status.NEW, "description", null, 0));

        taskManager.deleteAllSubtasks();
        List<Epic> actualEpicList = taskManager.getAllEpics();
        List<Subtask> actualSubtaskList = taskManager.getAllSubtasks();
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();

        assertEquals(expectedEpicList, actualEpicList, "Epic's characteristics not update");
        assertEquals(List.of(), taskManager.getEpicById(2).getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
        assertEquals(List.of(), taskManager.getEpicById(4).getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
        assertEquals(expectedSubtaskList, actualSubtaskList, "Not all Subtasks deleted");
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getPrioritizedTasks returned not correct data");
    }

    @Test
    public void deleteAllTasks() {
        Task madeTask2 = taskManager.createTask(new Task(4, "name2", Status.NEW, "description2"));
        List<Task> expected = new ArrayList<>();
        List<Task> expectedPrioritized = List.of(
                new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                        Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        taskManager.deleteAllTasks();
        List<Task> actual = taskManager.getAllTasks();
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();

        assertEquals(expected, actual, "Tasks are not deleted or getAllTask returned not empty list");
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getPrioritizedTasks returned not correct data");
    }
}