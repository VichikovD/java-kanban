package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Task;

public class InMemoryHistoryManager implements HistoryManager {
    HashMap<Integer, Node<Task>> nodeMap = new HashMap<>();
    Node<Task> first;
    Node<Task> last;

    private void linkLast(Task task) {
        Node<Task> oldLast = last;
        Node<Task> newNode = new Node<>(oldLast, task, null);
        last = newNode;
        if (oldLast == null) {
            first = newNode;
        } else {
            oldLast.next = newNode;
        }
    }

    private void removeNode(int nodeId){
        Node<Task> node = nodeMap.remove(nodeId);
        if (node == null) {
            return;
        }
        Node<Task> prevNode = node.prev;
        Node<Task> nextNode = node.next;

        if (prevNode == null) {
            first = nextNode;
        } else {
            prevNode.next = nextNode;
            node.prev = null;
        }

        if (nextNode == null) {
            last = prevNode;
        } else {
            nextNode.prev = prevNode;
            node.next = null;
        }

        node.data = null;
    }

    public List<Task> getTasks() {
        return getHistory();
    }
    @Override
    public List<Task> getHistory() {
        Node<Task> node = first;
        List<Task> tasksList = new ArrayList<>();
        while (node != null){
            tasksList.add(node.data);
            node = node.next;
        }
        return tasksList;
    }

    @Override
    public void add(Task task) {
        removeNode(task.getId());
        linkLast(task);
        nodeMap.put(task.getId(), last);
    }

    @Override
    public void remove(int id) {
        removeNode(id);
    }

    class Node<E> {
        E data;
        Node<E> prev;
        Node<E> next;

        public Node(Node<E> prev, E data, Node<E> next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }

}
