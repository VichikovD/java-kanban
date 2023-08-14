package service.server;

enum Endpoint {
    GET_HISTORY,
    CREATE_OR_UPDATE_TASK,
    CREATE_OR_UPDATE_SUBTASK,
    CREATE_OR_UPDATE_EPIC,
    GET_TASK_BY_ID,
    GET_SUBTASK_BY_ID,
    GET_EPIC_BY_ID,
    GET_ALL_TASKS,
    GET_ALL_SUBTASKS,
    GET_ALL_EPICS,
    DELETE_TASK_BY_ID,
    DELETE_SUBTASK_BY_ID,
    DELETE_EPIC_BY_ID,
    DELETE_ALL_TASKS,
    DELETE_ALL_SUBTASKS,
    DELETE_ALL_EPICS,
    GET_SUBTASKS_LIST_BY_EPIC_ID,
    GET_PRIORITIZED,
    UNKNOWN
}
