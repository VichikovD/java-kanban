package service;

import java.util.HashMap;
import java.util.List;

import model.Task;
import model.Epic;
import model.Subtask;

public interface TaskManager {
    public List<Task> getHistory();

    public void createTask(Task task);

    public void updateTask(Task task);

    public void createSubtask(Subtask thatSubtask);

    public void updateSubtask(Subtask thatSubtask);

    public void createEpic(Epic thatEpic);

    public void updateEpic(Epic thatEpic);

    public List<Subtask> getSubtasksListByEpicId(int epicId);

    public List<Task> getAllTasks();

    public List<Subtask> getAllSubtasks();

    public List<Epic> getAllEpics();

    public Task getTaskById(int taskId);

    public Subtask getSubtaskById(int subtaskId);

    public Epic getEpicById(int epicId);

    public void deleteTaskById(int taskId);

    public void deleteSubtaskById(int subtaskId);

    public void deleteEpicById(int epicId);

    public void deleteAllEpics();

    public void deleteAllSubtasks();

    public void deleteAllTasks();
    public List<Task> getPrioritizedTasks();
}
