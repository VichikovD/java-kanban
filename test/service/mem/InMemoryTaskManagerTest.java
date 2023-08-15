package service.mem;

import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import service.TaskManagerTest;
import service.mem.InMemoryTaskManager;

import java.util.List;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    public InMemoryTaskManager getTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    public void EpicStatusShouldUpdateToNEW() {
        Epic madeEpic = new Epic(1, "Epic1 name", Status.DONE, "Epic1 description", null, 0);
        Status expectedEpicStatus = Status.NEW;

        taskManager.createEpic(madeEpic);
        Status actualEpicStatus = taskManager.getEpicById(1).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }

    @Test
    public void EpicStatusShouldUpdateToNEW2() {
        Epic madeEpic = new Epic(1, "Epic1 name", Status.NEW, "Epic1 description", null, 0);
        Subtask madeSubtask1 = new Subtask(2, "Subtask1 name", Status.NEW, "Subtask1 description", null, 0, 1);
        Subtask madeSubtask2 = new Subtask(3, "Subtask2 name", Status.NEW, "Subtask2 description", null, 0, 1);
        Status expectedEpicStatus = Status.NEW;

        taskManager.createEpic(madeEpic);
        taskManager.createSubtask(madeSubtask1);
        taskManager.createSubtask(madeSubtask2);
        Status actualEpicStatus = taskManager.getEpicById(1).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }

    @Test
    public void EpicStatusShouldUpdateToDONE() {
        Epic madeEpic = new Epic(1, "Epic1 name", Status.NEW, "Epic1 description", null, 0);
        Subtask madeSubtask1 = new Subtask(2, "Subtask1 name", Status.DONE, "Subtask1 description", null, 0, 1);
        Subtask madeSubtask2 = new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description", null, 0, 1);
        Status expectedEpicStatus = Status.DONE;

        taskManager.createEpic(madeEpic);
        taskManager.createSubtask(madeSubtask1);
        taskManager.createSubtask(madeSubtask2);
        Status actualEpicStatus = taskManager.getEpicById(1).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }

    @Test
    public void EpicStatusShouldUpdateToIN_PROGRESS() {
        Epic madeEpic = new Epic(1, "Epic1 name", Status.NEW, "Epic1 description", null, 0);
        Subtask madeSubtask1 = new Subtask(2, "Subtask1 name", Status.IN_PROGRESS, "Subtask1 description", null, 0, 1);
        Subtask madeSubtask2 = new Subtask(3, "Subtask2 name", Status.IN_PROGRESS, "Subtask2 description", null, 0, 1);
        Status expectedEpicStatus = Status.IN_PROGRESS;

        taskManager.createEpic(madeEpic);
        taskManager.createSubtask(madeSubtask1);
        taskManager.createSubtask(madeSubtask2);
        Status actualEpicStatus = taskManager.getEpicById(1).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }

    @Test
    public void EpicStatusShouldUpdateToIN_PROGRESS2() {
        Epic madeEpic = new Epic(1, "Epic1 name", Status.NEW, "Epic1 description", null, 0);
        Subtask madeSubtask1 = new Subtask(2, "Subtask1 name", Status.NEW, "Subtask1 description", null, 0, 1);
        Subtask madeSubtask2 = new Subtask(3, "Subtask2 name", Status.DONE, "Subtask2 description", null, 0, 1);
        Status expectedEpicStatus = Status.IN_PROGRESS;

        taskManager.createEpic(madeEpic);
        taskManager.createSubtask(madeSubtask1);
        taskManager.createSubtask(madeSubtask2);
        Status actualEpicStatus = taskManager.getEpicById(1).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }
}