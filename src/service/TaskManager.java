package service;

import java.util.List;

import model.Task;
import model.Epic;
import model.Subtask;

public interface TaskManager {
    public List<Task> getHistory();

    public Task createTask(Task task);

    public Task updateTask(Task task);

    public Subtask createSubtask(Subtask thatSubtask);

    public Subtask updateSubtask(Subtask thatSubtask);

    public Epic createEpic(Epic thatEpic);

    public Epic updateEpic(Epic thatEpic);

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
    public boolean containsTask(int id);
    public boolean containsSubtask(int id);
    public boolean containsEpic(int id);
}
