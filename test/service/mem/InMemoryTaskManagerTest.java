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
        madeSubtask1.setStatus(Status.NEW);
        Status expectedEpicStatus = Status.NEW;

        taskManager.updateSubtask(madeSubtask1);
        Status actualEpicStatus = taskManager.getEpicById(2).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }

    @Test
    public void EpicStatusShouldUpdateToDONE() {
        Subtask madeSubtask2 = new Subtask(3, "Subtask1 name", Status.DONE, "Subtask1 description", null, 0, 2);
        Status expectedEpicStatus = Status.DONE;

        taskManager.createSubtask(madeSubtask2);
        Status actualEpicStatus = taskManager.getEpicById(2).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }

    @Test
    public void EpicStatusShouldUpdateToIN_PROGRESS() {
        Subtask madeSubtask2 = new Subtask(4, "Subtask2 name", Status.IN_PROGRESS, "Subtask2 description", null, 0, 2);
        Status expectedEpicStatus = Status.IN_PROGRESS;

        taskManager.createSubtask(madeSubtask2);
        Status actualEpicStatus = taskManager.getEpicById(2).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }

    @Test
    public void EpicStatusShouldUpdateToIN_PROGRESS2() {
        Subtask madeSubtask2 = new Subtask(4, "Subtask2 name", Status.NEW, "Subtask2 description", null, 0, 2);
        Status expectedEpicStatus = Status.IN_PROGRESS;

        taskManager.createSubtask(madeSubtask2);
        Status actualEpicStatus = taskManager.getEpicById(2).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus, "Status not correct");
    }
}