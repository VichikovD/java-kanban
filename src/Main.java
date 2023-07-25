/*
import model.Epic;
import model.Subtask;
import model.Task;
import service.TaskManager;
import model.Status;
import util.Managers;

public class Main {
    public static void main(String[] args) {

        TaskManager manager = Managers.getInMemoryTaskManager();


        Task task1 = new Task();
        task1.setDescription("Description T1");
        task1.setName("T1");
        task1.setStatus(Status.NEW);
        manager.createTask(task1);

        Task task2 = new Task();
        task2.setDescription("Description T2");
        task2.setName("T2");
        task2.setStatus(Status.DONE);
        manager.createTask(task2);

        Epic epic1 = new Epic();
        epic1.setDescription("Description E1");
        epic1.setName("E1");
        manager.createEpic(epic1);
        System.out.println(manager.getAllEpics());
        System.out.println();

        Subtask subTask1 = new Subtask();
        subTask1.setDescription("Description S1");
        subTask1.setName("S1");
        subTask1.setStatus(Status.DONE);
        subTask1.setEpicId(3);
        manager.createSubtask(subTask1);

        Subtask subTask2 = new Subtask();
        subTask2.setDescription("Description S2");
        subTask2.setName("S2");
        subTask2.setStatus(Status.NEW);
        subTask2.setEpicId(3);
        manager.createSubtask(subTask2);

        Subtask subTask3 = new Subtask();
        subTask3.setDescription("Description S3");
        subTask3.setName("S3");
        subTask3.setStatus(Status.IN_PROGRESS);
        subTask3.setEpicId(3);
        manager.createSubtask(subTask3);

        Epic epic2 = new Epic();
        epic2.setDescription("Description E2");
        epic2.setName("E2");
        manager.createEpic(epic2);

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllSubtasks());
        System.out.println(manager.getAllEpics());
        System.out.println();

        manager.getEpicById(3);
        manager.getEpicById(7);
        manager.getTaskById(1);
        manager.getSubtaskById(5);
        manager.getEpicById(3);
        manager.getEpicById(7);
        manager.getTaskById(2);
        manager.getSubtaskById(4);
        manager.getSubtaskById(6);
        manager.getSubtaskById(5);


        System.out.println(manager.getHistory());
        System.out.println("");


        manager.deleteAllSubtasks();
        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllSubtasks());
        System.out.println(manager.getAllEpics());
        System.out.println("");
        
        System.out.println(manager.getHistory());

    }
}*/
