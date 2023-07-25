package service;

import java.util.List;

import model.Task;

public interface HistoryManager {

    public List<Task> getHistory();

    public void add(Task task);

    public void remove(int id);



}
