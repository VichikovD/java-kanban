import model.Epic;
import model.Subtask;
import model.Task;
import service.TaskManager;
import model.Status;
import service.file.FileBackedTasksManager;
import util.Managers;

public class Main {
    public static void main(String[] args) {

        TaskManager manager1 = Managers.getDefaults();

        System.out.println(manager1.getAllTasks());
        System.out.println(manager1.getAllSubtasks());
        System.out.println(manager1.getAllEpics());
        System.out.println(manager1.getHistory());
        System.out.println();

        Task task1 = new Task();
        task1.setDescription("Description T1");
        task1.setName("T1");
        task1.setStatus(Status.NEW);
        manager1.createTask(task1);

        Task task2 = new Task();
        task2.setDescription("Description T2");
        task2.setName("T2");
        task2.setStatus(Status.DONE);
        manager1.createTask(task2);

        Epic epic1 = new Epic();
        epic1.setDescription("Description E1");
        epic1.setName("E1");
        manager1.createEpic(epic1);

        manager1.getTaskById(2);

        Subtask subTask1 = new Subtask();
        subTask1.setDescription("Description S1");
        subTask1.setName("S1");
        subTask1.setStatus(Status.DONE);
        subTask1.setEpicId(3);
        manager1.createSubtask(subTask1);

        manager1.getSubtaskById(4);

        TaskManager manager2 = FileBackedTasksManager.loadFromFile("AutoSave.csv");

        System.out.println("Task maps are identical: " + manager1.getAllTasks().equals(manager2.getAllTasks()));
        System.out.println("Subtask maps are identical: " + manager1.getAllSubtasks().equals(manager2.getAllSubtasks()));
        System.out.println("Epic maps are identical: " + manager1.getAllEpics().equals(manager2.getAllEpics()));
        System.out.println("Histories are identical: " + manager1.getHistory().equals(manager2.getHistory()));
        System.out.println();


        Subtask subTask2 = new Subtask();
        subTask2.setDescription("Description S2");
        subTask2.setName("S2");
        subTask2.setStatus(Status.NEW);
        subTask2.setEpicId(3);
        manager1.createSubtask(subTask2);

        Subtask subTask3 = new Subtask();
        subTask3.setDescription("Description S3");
        subTask3.setName("S3");
        subTask3.setStatus(Status.IN_PROGRESS);
        subTask3.setEpicId(3);
        manager1.createSubtask(subTask3);

        Epic epic2 = new Epic();
        epic2.setDescription("Description E2");
        epic2.setName("E2");
        manager1.createEpic(epic2);

        manager1.getEpicById(3);
        manager1.getEpicById(7);
        manager1.getTaskById(1);
        manager1.getSubtaskById(5);
        manager1.getEpicById(3);
        manager1.getEpicById(7);
        manager1.getTaskById(2);
        manager1.getSubtaskById(4);
        manager1.getSubtaskById(6);
        manager1.getSubtaskById(5);

        System.out.println(manager1.getHistory());
        System.out.println("");

        manager2 = FileBackedTasksManager.loadFromFile("AutoSave.csv");

        System.out.println("Task maps are identical :" + manager1.getAllTasks().equals(manager2.getAllTasks()));
        System.out.println("Subtask maps are identical :" + manager1.getAllSubtasks().equals(manager2.getAllSubtasks()));
        System.out.println("Epic maps are identical :" + manager1.getAllEpics().equals(manager2.getAllEpics()));
        System.out.println("Histories are identical: " + manager1.getHistory().equals(manager2.getHistory()));

    }
}
