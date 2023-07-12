package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Task;

public class InMemoryHistoryManager implements HistoryManager {
    HashMap<Integer, Node<Task>> nodeMap = new HashMap<>();
    Integer size = 0;
    Node<Task> first;
    Node<Task> last;

    public void linkLast(Task task) {
        Node<Task> oldLast = last;
        Node<Task> newNode = new Node<>(oldLast, task, null);
        last = newNode;
        if (oldLast == null) {
            first = newNode;
        } else {
            oldLast.next = newNode;
        }
        size++;
    }

    public void removeNode(Node<Task> node){
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

        size--;
        node.data = null;
    }

    public List<Task> getTasks() {
        Node<Task> node = first;
        List<Task> tasksList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            tasksList.add(node.data);
            node = node.next;
        }
        return tasksList;
    }
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void add(Task task) {
        remove(task.getId());
        linkLast(task);
        nodeMap.put(task.getId(), last);
    }

    @Override
    public void remove(int id) {
        Node sameTaskNode = nodeMap.get(id);
        if(sameTaskNode != null) {
            removeNode(sameTaskNode);
        }
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
