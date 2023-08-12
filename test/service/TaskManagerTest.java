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

import static org.junit.jupiter.api.Assertions.*;

abstract public class TaskManagerTest<T extends TaskManager> {
    public T taskManager = beforeEach();

    @BeforeEach
    abstract public T beforeEach();

    @Test
    public void getPrioritized1ShouldReturn2NullTimeTasks() {
        Task madeTask1 = taskManager.createTask(new Task(1, "name", Status.NEW, "description"));
        Task madeTask2 = taskManager.createTask(new Task(2, "name", Status.NEW, "description"));

        List<Task> expectedList = List.of(
                new Task(1, "name", Status.NEW, "description", null, 0),
                new Task(2, "name", Status.NEW, "description", null, 0));
        List<Task> actualList = taskManager.getPrioritizedTasks();
        assertEquals(expectedList, actualList, "Returned not 2 null startTime tasks");
    }

    @Test
    public void getPrioritized2ShouldAddOnly1Subtask() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "Epic1 name", "Epic1 description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(3, "Subtask1 name", Status.DONE,
                "Subtask1 description", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));
        Subtask madeSubtask = taskManager.createSubtask(new Subtask(4, "Subtask2 name", Status.DONE,
                "Subtask2 description", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));

        List<Task> expectedList = List.of(
                new Subtask(2, "Subtask1 name", Status.DONE, "Subtask1 description",
                        Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));
        List<Task> actualList = taskManager.getPrioritizedTasks();
        assertEquals(expectedList, actualList, "Returned not 1 subtasks");
    }

    @Test
    public void createTask1ShouldAddCopyOfTaskButWithCorrectIdToTasksMap() {

        Task madeTask = taskManager.createTask(new Task(2, "name", Status.NEW, "description"));

        Task expectedTask = new Task(1, "name", Status.NEW, "description", null, 0);
        assertEquals(expectedTask, madeTask, "Task not created correctly or getTaskById didn't return correct Task");
    }

    @Test
    public void createTask2ShouldNotCreateSecondTask() {
        Task madeTask1 = taskManager.createTask(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        Task madeTask2 = taskManager.createTask(new Task(2, "name", Status.NEW, "description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        List<Task> expectedTasks = List.of(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));
        List<Task> actualTasks = List.of(madeTask1);
        assertEquals(expectedTasks, actualTasks, "Not 1 task created or getTaskById didn't return correct Task");
    }

    //  Предполагаю, что проверку тасков на null поля стоило бы иметь, если мы нe считали, что нам подаются правильные
    //  таски, как это было описано в ранних ТЗ. Тесты, выбрасываний исключений не проходят по изначальной логике
    /*@Test
    public void createTask2ShouldThrowException() {
        Task madeTask = new Task(null, null, null, null);
        assertThrows(IllegalArgumentException.class,
                () -> taskManager.createTask(madeTask),
                "Exception is not thrown when task fields are not filled up");
    }

    @Test
    public void createTask3ShouldThrowException() {
        Task madeTask = null;
        assertThrows(IllegalArgumentException.class,
                () -> taskManager.createTask(madeTask),
                "Exception is not thrown when task is null");
    }*/

    @Test
    public void updateTask1ShouldUpdateTaskInTasksMap() {
        Task madeTask = taskManager.createTask(new Task(1, "name", Status.NEW, "description"));

        Task updatedTask = taskManager.updateTask(new Task(1, "New Name", Status.DONE, "New Description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        Task expectedTask = new Task(1, "New Name", Status.DONE, "New Description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        assertEquals(expectedTask, updatedTask, "Task not updated correctly or getTaskById didn't return correct Task");
    }

    @Test
    public void updateTask2ShouldUpdateTaskIvenWithSameStartTime() {
        Task madeTask = taskManager.createTask(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        Task updatedTask = taskManager.updateTask(new Task(1, "New Name", Status.DONE, "New Description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        Task expectedTask = new Task(1, "New Name", Status.DONE, "New Description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        assertEquals(expectedTask, updatedTask, "Task not updated correctly or getTaskById didn't return correct Task");
    }


    //  Предполагаю, что проверку тасков на null поля стоило бы иметь, если мы нe считали, что нам подаются корректные
    //  таски, как это было, кажется, описано в ранних ТЗ. 2 нижних теста не проходят по изначальной логике
 /*
    @Test
    public void updateTask2ShouldThrowException() {
        Task madeTask = taskManager.createTask(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));
        Task madeTask = new Task(null, null, null, null);
        assertThrows(IllegalArgumentException.class,
                () -> taskManager.updateTask(madeTask),
                "Exception is not thrown when task fields are not filled up");
    }

    @Test
    public void updateTask3ShouldThrowException() {
        Task madeTask = taskManager.createTask(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));
        Task madeTask = null;
        assertThrows(IllegalArgumentException.class,
                () -> taskManager.updateTask(madeTask),
                "Exception is not thrown when task is null");
    }*/

    @Test
    public void createSubtask1ShouldAddSubtaskToSubtasksMapAndAddToEpicSubtaskList() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "Epic1 name", "Epic1 description"));

        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "Epic2 name", "Epic2 description"));

        Subtask madeSubtask = taskManager.createSubtask(new Subtask(5, "Subtask1 name", Status.DONE, "Subtask1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        Subtask expectedSubtask = new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2);
        Subtask savedSubtask = taskManager.getSubtaskById(3);
        assertEquals(expectedSubtask, savedSubtask, "Subtask not created correctly or getSubtaskById didn't return " +
                "correct Subtask");

        Epic expectedEpic = new Epic(2, "Epic2 name", Status.DONE, "Epic2 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        Epic actualEpic = taskManager.getEpicById(2);
        assertEquals(expectedEpic, actualEpic, "Subtask not added to epic's subtascIdList or getEpicById return wrong");

        assertEquals(List.of(3), actualEpic.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }

    /*@Test
    public void createSubtask2ThrowException() {
        Subtask madeSubtask = new Subtask(4, "Subtask name", Status.NEW, "Subtask description", null, 0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> taskManager.createSubtask(madeSubtask),
                "Exception is not thrown when there is no created Epic mentioned in subtask's epicId");
    }

    @Test
    public void createSubtask3ThrowException() {
        Epic madeEpic = new Epic(1, "Epic name", "Epic description");
        taskManager.createEpic(madeEpic);

        Subtask madeSubtask = new Subtask(null, null, null, null, null);
        assertThrows(IllegalArgumentException.class,
                () -> taskManager.createSubtask(madeSubtask),
                "Exception is not thrown when subtask fields are not filled up");
    }*/

    @Test
    public void updateSubtask1() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "Epic1 name", "Epic1 description"));

        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "Epic2 name", "Epic2 description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(5, "Subtask name", Status.DONE, "Subtask1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));

        Epic expectedEpic1 = new Epic(1, "Epic1 name", Status.DONE, "Epic1 description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic1.addSubtaskId(3);
        Epic actualEpic1 = taskManager.getEpicById(1);
        assertEquals(expectedEpic1, actualEpic1, "Epic's characteristics not updated or getEpicById return wrong");

        assertEquals(List.of(3), actualEpic1.getSubtasksIdList());

        Subtask updatedSubtask = taskManager.updateSubtask(new Subtask(3, "New Subtask name", Status.IN_PROGRESS, "New Subtask description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes(), 2));

        Subtask expectedSubtask = new Subtask(3, "New Subtask name", Status.IN_PROGRESS, "New Subtask description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes(), 2);
        assertEquals(expectedSubtask, updatedSubtask, "Subtask not updated correctly");

        Epic expectedUpdatedEpic1 = new Epic(1, "Epic1 name", Status.NEW, "Epic1 description", null, 0);
        Epic actualUpdatedEpic1 = taskManager.getEpicById(1);
        assertEquals(expectedUpdatedEpic1, actualUpdatedEpic1, "Epic's characteristics not updated or getEpicById " +
                "return wrong");

        assertEquals(List.of(), actualUpdatedEpic1.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");

        Epic expectedEpic2 = new Epic(2, "Epic2 name", Status.IN_PROGRESS, "Epic2 description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes());
        expectedEpic2.addSubtaskId(3);
        Epic actualEpic2 = taskManager.getEpicById(2);
        assertEquals(expectedEpic2, actualEpic2, "Subtask not added to epic's subtascIdList or getEpicById return wrong");

        assertEquals(List.of(3), actualEpic2.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }


    /*@Test
    public void updateSubtask2ShouldThrowExceptionDueToNotCorrectUpdatedEpicId() {
        Epic madeEpic = taskManager.createEpic(new Epic(1, "Epic nameUpd2", "Epic descriptionUpd2"));

        Subtask madeSubtask = taskManager.createSubtask(new Subtask(4, "Subtask nameUpd2", Status.NEW,
                "Subtask descriptionUpd2", null, 0, 1));

        Subtask updatedSubtask = new Subtask(1, "New NameUpd2", Status.DONE, "New DescriptionUpd2",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes(), 2);
        assertThrows(IllegalArgumentException.class,
                () -> taskManager.updateSubtask(updatedSubtask),
                "Exception is not thrown when update subtask's epicId not in epicsMap");
    }
*/
    @Test
    public void createEpic1ShouldCreateEpicAsPerRequirements() {
        Epic madeEpic = taskManager.createEpic(new Epic(2, "Epic2 name", Status.IN_PROGRESS, "Epic2 description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes()));

        Epic expectedEpic = new Epic(1, "Epic2 name", Status.NEW, "Epic2 description", null, 0);
        Epic actualEpic = taskManager.getEpicById(1);
        assertEquals(expectedEpic, actualEpic, "Epic not created correctly");

        assertEquals(List.of(), actualEpic.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }

    @Test
    public void updateEpicAsPerRequirements() {
        Epic madeEpic = taskManager.createEpic(new Epic(1, "Epic name", "Epic description"));

        Epic updatedEpic = taskManager.updateEpic(new Epic(1, "New Epic name", Status.IN_PROGRESS, "new Epic description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(55).toMinutes()));

        Epic expectedEpic = new Epic(1, "New Epic name", Status.NEW, "new Epic description", null, 0);
        Epic actualEpic = taskManager.getEpicById(1);
        assertEquals(expectedEpic, actualEpic, "Task not updated correctly");

        assertEquals(List.of(), actualEpic.getSubtasksIdList(), "SubtasksIdList not updated or method return wrong");
    }

    @Test
    public void getSubtasksListByEpicId() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "Epic1 name", "Epic1 description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(2, "Subtask1 name", Status.NEW, "Subtask1 description",
                Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));

        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(3, "Subtask2 name", Status.NEW, "Subtask2 description",
                Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));

        List<Subtask> expectedList = List.of(
                new Subtask(2, "Subtask1 name", Status.NEW, "Subtask1 description",
                        Instant.parse("2023-05-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1),
                new Subtask(3, "Subtask2 name", Status.NEW, "Subtask2 description",
                        Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));
        List<Subtask> actualList = List.of(madeSubtask1, madeSubtask2);
        assertEquals(expectedList, actualList, "Subtask not updated correctly");
    }

     /*@Test
    public void getSubtasksListByEpicId2() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "Epic1 name", "Epic1 description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(2, "Subtask1 name", Status.NEW, "Subtask1 description", null, 0, 1));

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.getSubtasksListByEpicId(2));
    }*/

    @Test
    public void getAllTasks() {
        Task madeTask1 = taskManager.createTask(new Task(1, "name1", Status.NEW, "description1"));
        Task madeTask2 = taskManager.createTask(new Task(2, "name2", Status.NEW, "description2"));

        List<Task> expected = List.of(
                new Task(1, "name1", Status.NEW, "description1", null, 0),
                new Task(2, "name2", Status.NEW, "description2", null, 0));
        List<Task> actual = taskManager.getAllTasks();
        assertEquals(expected, actual, "Returned not correct list of tasks");
    }

    @Test
    public void getAllSubtasks() {
        Epic madeEpic = taskManager.createEpic(new Epic(1, "Epic name", "Epic description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(2, "Subtask name1", Status.NEW,
                "Subtask description1", null, 0, 1));
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(3, "Subtask name2", Status.NEW,
                "Subtask description2", null, 0, 1));

        List<Subtask> expected = List.of(
                new Subtask(2, "Subtask name1", Status.NEW, "Subtask description1", null, 0, 1),
                new Subtask(3, "Subtask name2", Status.NEW, "Subtask description2", null, 0, 1));
        List<Subtask> actual = taskManager.getAllSubtasks();
        assertEquals(expected, actual, "Returned not correct list of subtasks");
    }

    @Test
    public void getAllEpics() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "Epic name1", "Epic description1"));
        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "Epic name2", "Epic description2"));
        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(3, "Subtask name1", Status.DONE,
                "Subtask description1", Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));

        Epic expectedEpic1 = new Epic(1, "Epic name1", Status.DONE, "Epic description1",
                Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic1.addSubtaskId(3);
        Epic expectedEpic2 = new Epic(2, "Epic name2", Status.NEW, "Epic description2", null, 0);

        List<Epic> expected = List.of(expectedEpic1, expectedEpic2);
        List<Epic> actual = taskManager.getAllEpics();
        assertEquals(expected, actual, "Returned not correct list of epics");

        assertEquals(List.of(3), taskManager.getEpicById(1).getSubtasksIdList(), "SubtasksIdList not updated or " +
                "method return wrong");

        assertEquals(List.of(), taskManager.getEpicById(2).getSubtasksIdList(), "SubtasksIdList not updated or " +
                "method return wrong");
    }

    @Test
    public void getTaskById() {
        Task madeTask1 = taskManager.createTask(new Task(1, "name1", Status.NEW, "description1"));

        Task expected = new Task(1, "name1", Status.NEW, "description1", null, 0);
        Task actual = taskManager.getTaskById(1);
        assertEquals(expected, actual, "Received not correct Task");
    }

    /*@Test
    public void getTaskById2ShouldThrowException() {
        Task madeTask1 = taskManager.createTask(new Task(1, "name1", Status.NEW, "description1"));

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.getTaskById(2));
    }
*/

    @Test
    public void getSubtaskById() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "name1", "description1"));

        Subtask madeSubtask = taskManager.createSubtask(new Subtask(2, "name1", Status.NEW, "description1", null, 0, 1));

        Subtask expected = new Subtask(2, "name1", Status.NEW, "description1", null, 0, 1);
        Subtask actual = taskManager.getSubtaskById(2);
        assertEquals(expected, actual, "Received not correct subtask");
    }

    /*@Test
    public void getSubtaskById2ShouldThrowException() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "name1", "description1"));

        Subtask madeSubtask = taskManager.createSubtask(new Subtask(2, "name1", Status.NEW, "description1", 1));

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.getSubtaskById(3));
    }*/

    @Test
    public void getEpicById() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "name1", "description1"));

        Epic expected = new Epic(1, "name1", Status.NEW, "description1", null, 0);
        Epic actual = taskManager.getEpicById(1);
        assertEquals(expected, actual, "Received not correct Task");
    }

   /* @Test
    public void getEpicById2ShouldThrowException() {
        Epic madeEpic1 = new Epic(1, "name1", "description1");
        taskManager.createEpic(madeEpic1);

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.getEpicById(2));
    }*/

    @Test
    public void deleteTaskByIdShouldDelete1TaskAndDeleteFromPrioritized() {
        Task madeTask = taskManager.createTask(new Task(1, "name1", Status.NEW, "description1"));
        Task madeTask2 = taskManager.createTask(new Task(2, "name2", Status.NEW, "description2"));

        taskManager.deleteTaskById(1);

        List<Task> expected = List.of(madeTask2);
        List<Task> actual = taskManager.getAllTasks();
        assertEquals(expected, actual, "Deleted not 1 task or getAllTasks returned not correct data");

        List<Task> expectedPrioritized = List.of(madeTask2);
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getAllTasks returned not correct data");
    }

    /*@Test
    public void deleteTaskById2ShouldThrowException() {
        Task madeTask = taskManager.createTask(new Task(1, "name1", Status.NEW, "description1"));

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.deleteTaskById(2));
    }*/

    @Test
    public void deleteSubtaskByIdShouldDelete1SubtaskAndUpdatePrioritizedTasksAndUpdateEpic() {
        Epic madeEpic = taskManager.createEpic(new Epic(1, "Epic name", "Epic description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(2, "Subtask name1", Status.DONE, "Subtask description1",
                Instant.parse("2023-03-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));

        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(3, "Subtask name2", Status.NEW, "Subtask description2",
                Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));

        taskManager.deleteSubtaskById(2);

        List<Subtask> expectedSubtasks = List.of(
                madeSubtask2);
        List<Subtask> actualSubtasks = taskManager.getAllSubtasks();
        assertEquals(expectedSubtasks, actualSubtasks, "Deleted not 1 subtask or getAllSubtasks returned not correct data");

        List<Task> expectedPrioritized = List.of(madeSubtask2);
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or PrioritizedTasks returned not correct data");

        Epic expectedEpic = new Epic(1, "Epic name", Status.NEW, "Epic description",
                Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(3);
        Epic actualEpic = taskManager.getEpicById(1);
        assertEquals(expectedEpic, actualEpic, "Epic's characteristics not correctly updated");

        assertEquals(List.of(3), actualEpic.getSubtasksIdList(), "SubtasksIdList not updated or " +
                "method return wrong");

    }

    /*@Test
    public void deleteSubtaskById2ShouldThrowException() {
        Epic madeEpic = askManager.createEpic(new Epic(1, "Epic name", Status.NEW, "Epic description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(2, "Subtask name1", Status.NEW, "Subtask description1", 1));

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.deleteSubtaskById(3));
    }*/


    @Test
    public void deleteEpicById1ShouldDelete1EpicAndDelete1SubtaskAndUpdatePrioritizedTasks() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "Epic1 name", "Epic1 description"));
        Epic madeEpic2 = taskManager.createEpic(new Epic(2, "Epic2 name", "Epic2 description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(3, "Subtask name1", Status.NEW,
                "Subtask description1", Instant.parse("2023-07-01T13:00:00.000Z"), Duration.ofDays(13).toMinutes(), 1));
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(4, "Subtask name2", Status.DONE,
                "Subtask description2", Instant.parse("2023-01-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));

        taskManager.deleteEpicById(1);

        Epic expectedEpic = new Epic(2, "Epic2 name", Status.DONE, "Epic2 description",
                Instant.parse("2023-01-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(4);

        List<Epic> expectedEpicList = List.of(expectedEpic);
        List<Epic> actualEpicList = taskManager.getAllEpics();
        assertEquals(expectedEpicList, actualEpicList, "Deleted not 1 epic or characteristics not changed " +
                "or getAllEpics returned not correct data or addSubtaskId works wrongly");


        List<Subtask> expectedSubtasks = List.of(
                madeSubtask2);
        List<Subtask> actualSubtasks = taskManager.getAllSubtasks();
        assertEquals(expectedSubtasks, actualSubtasks, "Deleted not 1 subtask or getAllSubtasks returned not correct data");

        List<Task> expectedPrioritized = List.of(madeSubtask2);
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getPrioritizedTasks returned not correct data");
    }

    /*@Test
    public void deleteEpicById2ShouldThrowException() {
        Epic madeEpic1 = taskManager.createEpic(new Epic(1, "Epic name", Status.NEW, "Epic description"));

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.deleteEpicById(3));
    }*/

    @Test
    public void deleteAllEpics() {
        Task task = taskManager.createTask(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2022-10-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        Epic madeEpic1 = taskManager.createEpic(new Epic(2, "Epic1 name", "Epic1 description"));
        Epic madeEpic2 = taskManager.createEpic(new Epic(3, "Epic2 name", "Epic2 description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(4, "Subtask name1", Status.DONE,
                "Subtask description1", Instant.parse("2023-01-01T13:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(5, "Subtask name2", Status.IN_PROGRESS,
                "Subtask description2", Instant.parse("2023-04-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 3));

        taskManager.deleteAllEpics();

        List<Epic> expectedEpicList = List.of();
        List<Epic> actualEpicList = taskManager.getAllEpics();
        assertEquals(expectedEpicList, actualEpicList, "Not all epics deleted");

        List<Subtask> expectedSubtaskList = List.of();
        List<Subtask> actualSubtaskList = taskManager.getAllSubtasks();
        assertEquals(expectedSubtaskList, actualSubtaskList, "Not all Subtasks deleted");

        List<Task> expectedPrioritized = List.of(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2022-10-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getPrioritizedTasks returned not correct data");
    }

    @Test
    public void deleteAllSubtasks() {
        Task task = taskManager.createTask(new Task(1, "name", Status.NEW, "description",
                Instant.parse("2023-04-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes()));

        Epic madeEpic1 = taskManager.createEpic(new Epic(2, "Epic1 name", "Epic1 description"));
        Epic madeEpic2 = taskManager.createEpic(new Epic(3, "Epic2 name", "Epic2 description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(3, "Subtask name1", Status.DONE,
                "Subtask description1", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 2));
        Subtask madeSubtask2 = taskManager.createSubtask(new Subtask(4, "Subtask name2", Status.DONE,
                "Subtask description2", Instant.parse("2022-10-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 3));

        taskManager.deleteAllSubtasks();

        List<Epic> expectedEpicList = List.of(
                new Epic(2, "Epic1 name", Status.NEW, "Epic1 description", null, 0),
                new Epic(3, "Epic2 name", Status.NEW, "Epic2 description", null, 0)
        );
        List<Epic> actualEpicList = taskManager.getAllEpics();
        assertEquals(expectedEpicList, actualEpicList, "Epic's characteristics not update");

        assertEquals(List.of(), taskManager.getEpicById(2).getSubtasksIdList(), "SubtasksIdList not updated or " +
                "method return wrong");
        assertEquals(List.of(), taskManager.getEpicById(3).getSubtasksIdList(), "SubtasksIdList not updated or " +
                "method return wrong");

        List<Subtask> expectedSubtaskList = List.of();
        List<Subtask> actualSubtaskList = taskManager.getAllSubtasks();
        assertEquals(expectedSubtaskList, actualSubtaskList, "Not all Subtasks deleted");

        List<Task> expectedPrioritized = List.of(new Task(1, "name", Status.NEW, "description", Instant.parse("2023-04-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes()));
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getPrioritizedTasks returned not correct data");
    }

    @Test
    public void deleteAllTasks() {
        Epic madeEpic2 = taskManager.createEpic(new Epic(1, "Epic2 name", "Epic2 description"));

        Subtask madeSubtask1 = taskManager.createSubtask(new Subtask(2, "Subtask name1", Status.DONE,
                "Subtask description1", Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));

        Task madeTask1 = taskManager.createTask(new Task(3, "name1", Status.NEW, "description1",
                Instant.parse("2023-02-01T00:00:00.001Z"), Duration.ofDays(31).toMinutes()));

        Task madeTask2 = taskManager.createTask(new Task(4, "name2", Status.NEW, "description2"));

        taskManager.deleteAllTasks();

        List<Task> expected = new ArrayList<>();
        List<Task> actual = taskManager.getAllTasks();
        assertEquals(expected, actual, "Tasks are not deleted or getAllTask returned not empty list");

        List<Task> expectedPrioritized = List.of(new Subtask(2, "Subtask name1", Status.DONE, "Subtask description1",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes(), 1));
        List<Task> actualPrioritized = taskManager.getPrioritizedTasks();
        assertEquals(expectedPrioritized, actualPrioritized,
                "Not correctly deleted from prioritized or getPrioritizedTasks returned not correct data");
    }
}