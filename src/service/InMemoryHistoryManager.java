package service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.Task;

public class InMemoryHistoryManager implements HistoryManager {

    LinkedList<Task> history = new LinkedList<>();

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);    
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (history.size() == 10) {
            history.removeFirst();
            history.addLast(task);
            return;
        }

        history.addLast(task);
    }

}
