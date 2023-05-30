import model.Epic;
import model.SubTask;
import model.Task;
import service.TaskManager;
public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task();
        task1.setDescription("Description T1");
        task1.setName("T1");
        task1.setStatus("NEW");
        manager.createTask(task1);

        Task task2 = new Task();
        task2.setDescription("Description T2");
        task2.setName("T2");
        task2.setStatus("DONE");
        manager.createTask(task2);

        Epic epic1 = new Epic();
        epic1.setDescription("Description E1");
        epic1.setName("E1");
        manager.createTask(epic1);

        SubTask subTask1 = new SubTask();
        subTask1.setDescription("Description S1");
        subTask1.setName("S1");
        subTask1.setStatus("DONE");
        subTask1.setEpicId(3);
        manager.createTask(subTask1);

        SubTask subTask2 = new SubTask();
        subTask2.setDescription("Description S2");
        subTask2.setName("S2");
        subTask2.setStatus("IN_PROGRESS");
        subTask2.setEpicId(3);
        manager.createTask(subTask2);

        Epic epic2 = new Epic();
        epic2.setDescription("Description E2");
        epic2.setName("E2");
        manager.createTask(epic2);

        System.out.println(manager.getAllTasksList());

        task1.setStatus("DONE");
        subTask2.setStatus("DONE");
        epic1.setDescription("New description E1");

        manager.updateTask(task1);
        manager.updateTask(subTask2);
        manager.updateTask(epic1);

        System.out.println(manager.getAllTasksList());

        manager.deleteTaskById(5);
        System.out.println(manager.getAllTasksList());

        manager.deleteTaskById(3);
        System.out.println(manager.getAllTasksList());
    }
}