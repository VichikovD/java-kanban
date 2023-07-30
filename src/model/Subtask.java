package model;

public class Subtask extends Task {
    public Subtask() {
        super();
        tasksType = TasksType.SUBTASK;
    }

    public Subtask(Integer id, String name, Status status, String description, Integer epicId) {
        super(id, name, status, description);
        this.tasksType = TasksType.SUBTASK;
        this.epicId = epicId;
    }

    @Override
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
