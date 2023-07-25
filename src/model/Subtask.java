package model;

public class Subtask extends Task {
    private int epicId;

    public int getEpicId() {
        return epicId;
    }

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

    @Override
    public String toString(Task task) {
        return super.toString(task) + "," + String.valueOf(epicId);

    }

    public static Subtask subtaskFromStringArray(String[] data) {
        Subtask subtask = new Subtask();

        subtask.setId(Integer.parseInt(data[0]));
        subtask.setName(data[2]);
        subtask.setStatus(Status.getStatusByString(data[3]));
        subtask.setDescription(data[4]);
        subtask.setEpicId(Integer.parseInt(data[5]));

        return subtask;
    }

    public Subtask() {
        super();
        tasksType = TasksType.SUBTASK;
    }
}
