package model;

public enum Status {
    NEW("NEW"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE");
    String status;
    Status(String status){
        this.status = status;
    }

    public static Status getStatusByString(String value) {
        for (Status status : values()) {
            if (status.toString().equals(value)) {
                return status;
            }
        }
        return null;
    }

}
