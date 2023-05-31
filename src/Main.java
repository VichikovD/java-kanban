import model.Epic;
import model.Subtask;
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
        manager.createEpic(epic1);
        System.out.println(manager.getAllEpics());

        Subtask subTask1 = new Subtask();
        subTask1.setDescription("Description S1");
        subTask1.setName("S1");
        subTask1.setStatus("DONE");
        subTask1.setEpicId(3);
        manager.createSubtask(subTask1);

        Subtask subTask2 = new Subtask();
        subTask2.setDescription("Description S2");
        subTask2.setName("S2");
        subTask2.setStatus("NEW");
        subTask2.setEpicId(3);
        manager.createSubtask(subTask2);

        Epic epic2 = new Epic();
        epic2.setDescription("Description E2");
        epic2.setName("E2");
        manager.createEpic(epic2);

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllSubtasks());
        System.out.println(manager.getAllEpics());
        System.out.println("");

        task1.setStatus("DONE");
        subTask2.setStatus("IN_PROGRESS");
        epic1.setDescription("New description E1");
        epic1.setStatus("NEW");
        epic1.clearSubtaskIdList();

        manager.updateTask(task1);
        manager.updateSubtask(subTask2);
        manager.updateEpic(epic1);


        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllSubtasks());
        System.out.println(manager.getAllEpics());
        System.out.println("");

        manager.deleteSubtaskById(subTask2.getId());

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllSubtasks());
        System.out.println(manager.getAllEpics());
        System.out.println("");

        manager.deleteAllEpics();

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllSubtasks());
        System.out.println(manager.getAllEpics());
        System.out.println("");


    }
}