package service.server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.Subtask;
import model.Task;
import service.TaskManager;
import service.mem.exception.NotFoundException;
import service.server.exception.ValidateException;
import util.Managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class HttpTaskServer {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final int PORT_8080 = 8080;
    public final HttpServer httpTaskServer;
    final TaskManager httpTaskManager;

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
    }

    public HttpTaskServer() throws IOException {
        httpTaskManager = Managers.getDefaults();
        httpTaskServer = HttpServer.create();
        httpTaskServer.bind(new InetSocketAddress(PORT_8080), 0);
        httpTaskServer.createContext("/tasks", new TasksHandler());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        httpTaskManager = taskManager;
        httpTaskServer = HttpServer.create();
        httpTaskServer.bind(new InetSocketAddress(PORT_8080), 0);
        httpTaskServer.createContext("/tasks", new TasksHandler());
    }

    public void start() {
        httpTaskServer.start();
        System.out.println("HttpTaskServer now working. Port: " + PORT_8080);
    }

    public void stop(int time) {
        httpTaskServer.stop(time);
        System.out.println("HttpTaskServer stopped now.");
    }

    class TasksHandler implements HttpHandler {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();

        TasksHandler() {
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Endpoint endpoint = getEndpoint(exchange.getRequestURI().toString(), exchange.getRequestMethod());

                System.out.println(endpoint + " = " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
                switch (endpoint) {
                    case GET_HISTORY:
                        List<Task> history = httpTaskManager.getHistory();
                        writeResponse(exchange, gson.toJson(history), 200);
                        break;
                    case CREATE_OR_UPDATE_TASK:
                        handleCreateOrUpdateTask(exchange);
                        break;
                    case CREATE_OR_UPDATE_SUBTASK:
                        handleCreateOrUpdateSubtask(exchange);
                        break;
                    case CREATE_OR_UPDATE_EPIC:
                        handleCreateOrUpdateEpic(exchange);
                        break;
                    case GET_ALL_TASKS:
                        List<Task> taskList = httpTaskManager.getAllTasks();
                        writeResponse(exchange, gson.toJson(taskList), 200);
                        break;
                    case GET_ALL_SUBTASKS:
                        List<Subtask> subtaskList = httpTaskManager.getAllSubtasks();
                        writeResponse(exchange, gson.toJson(subtaskList), 200);
                        break;
                    case GET_ALL_EPICS:
                        List<Epic> epicList = httpTaskManager.getAllEpics();
                        writeResponse(exchange, gson.toJson(epicList), 200);
                        break;
                    case GET_TASK_BY_ID:
                        handleGetTaskById(exchange);
                        break;
                    case GET_SUBTASK_BY_ID:
                        handleGetSubtaskById(exchange);
                        break;
                    case GET_EPIC_BY_ID:
                        handleGetEpicById(exchange);
                        break;
                    case DELETE_TASK_BY_ID:
                        handleDeleteTaskById(exchange);
                        break;
                    case DELETE_SUBTASK_BY_ID:
                        handleDeleteSubtaskById(exchange);
                        break;
                    case DELETE_EPIC_BY_ID:
                        handleDeleteEpicById(exchange);
                        break;
                    case DELETE_ALL_TASKS:
                        httpTaskManager.deleteAllTasks();
                        writeResponse(exchange, "", 204);
                        break;
                    case DELETE_ALL_SUBTASKS:
                        httpTaskManager.deleteAllSubtasks();
                        writeResponse(exchange, "", 204);
                        break;
                    case DELETE_ALL_EPICS:
                        httpTaskManager.deleteAllEpics();
                        writeResponse(exchange, "", 204);
                        break;
                    case GET_SUBTASKS_LIST_BY_EPIC_ID:
                        handleGetSubtasksListByEpicId(exchange);
                        break;
                    case GET_PRIORITIZED:
                        List<Task> prioritizedList = httpTaskManager.getPrioritizedTasks();
                        writeResponse(exchange, gson.toJson(prioritizedList), 200);
                        break;
                    default:
                        writeResponse(exchange, "Wrong request", 404);
                }
            } catch (NotFoundException e) {
                writeResponse(exchange, e.getMessage(), 404);
            } catch (ValidateException | IllegalArgumentException exc) {
                writeResponse(exchange, exc.getMessage(), 400);
            } catch (JsonSyntaxException exc) {
                writeResponse(exchange, "Wrong JSON format received", 400);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                exchange.close();
            }
        }

        public void handleCreateOrUpdateTask(HttpExchange exchange) throws ValidateException, IOException, NotFoundException {
            String body = getRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);
            validate(task);

            String jsonAddedTask;
            if (task.getId() == null) {
                Task createdTask = httpTaskManager.createTask(task);
                jsonAddedTask = gson.toJson(createdTask);
                writeResponse(exchange, jsonAddedTask, 200);
            } else {
                Task updatedTask = httpTaskManager.updateTask(task);
                jsonAddedTask = gson.toJson(updatedTask);
                writeResponse(exchange, jsonAddedTask, 200);
            }

        }

        public void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException, ValidateException, NotFoundException {
            String body = getRequestBody(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            validate(subtask);

            String jsonAddedSubtask;
            if (subtask.getId() == null) {
                Subtask createdSubtask = httpTaskManager.createSubtask(subtask);
                jsonAddedSubtask = gson.toJson(createdSubtask);
                writeResponse(exchange, jsonAddedSubtask, 200);
            } else {
                Subtask updatedSubtask = httpTaskManager.updateSubtask(subtask);
                jsonAddedSubtask = gson.toJson(updatedSubtask);
                writeResponse(exchange, jsonAddedSubtask, 200);
            }
        }

        public void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException, ValidateException, NotFoundException {
            String body = getRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);
            validate(epic);

            String jsonAddedEpic;
            if (epic.getId() == null) {
                Epic createdEpic = httpTaskManager.createEpic(epic);
                jsonAddedEpic = gson.toJson(createdEpic);
                writeResponse(exchange, jsonAddedEpic, 200);
            } else {
                Epic updatedEpic = httpTaskManager.updateEpic(epic);
                jsonAddedEpic = gson.toJson(updatedEpic);
                writeResponse(exchange, jsonAddedEpic, 200);
            }
        }


        public void handleGetTaskById(HttpExchange exchange) throws IOException, NotFoundException,
                NumberFormatException, IndexOutOfBoundsException {
            int taskId = getTaskId(exchange);
            Task task = httpTaskManager.getTaskById(taskId);
            String jsonTask = gson.toJson(task);
            writeResponse(exchange, jsonTask, 200);

        }

        public void handleGetSubtaskById(HttpExchange exchange) throws IOException, NotFoundException,
                NumberFormatException, IndexOutOfBoundsException {
            int subtaskId = getTaskId(exchange);
            Subtask subtask = httpTaskManager.getSubtaskById(subtaskId);
            String jsonTask = gson.toJson(subtask);
            writeResponse(exchange, jsonTask, 200);

        }

        public void handleGetEpicById(HttpExchange exchange) throws IOException, NotFoundException,
                NumberFormatException, IndexOutOfBoundsException {
            int epicId = getTaskId(exchange);
            Epic epic = httpTaskManager.getEpicById(epicId);
            String jsonTask = gson.toJson(epic);
            writeResponse(exchange, jsonTask, 200);

        }

        public void handleDeleteTaskById(HttpExchange exchange) throws IOException, NotFoundException,
                NumberFormatException, IndexOutOfBoundsException {
            int taskId = getTaskId(exchange);
            httpTaskManager.deleteTaskById(taskId);
            writeResponse(exchange, "", 204);

        }

        public void handleDeleteSubtaskById(HttpExchange exchange) throws IOException, NotFoundException,
                NumberFormatException, IndexOutOfBoundsException {
            int subtaskId = getTaskId(exchange);

            httpTaskManager.deleteSubtaskById(subtaskId);
            writeResponse(exchange, "", 204);

        }

        public void handleDeleteEpicById(HttpExchange exchange) throws IOException, NotFoundException,
                NumberFormatException, IndexOutOfBoundsException {
            int epicId = getTaskId(exchange);
            httpTaskManager.deleteEpicById(epicId);
            writeResponse(exchange, "", 204);

        }

        public void handleGetSubtasksListByEpicId(HttpExchange exchange) throws IOException, NotFoundException,
                NumberFormatException, IndexOutOfBoundsException {
            int epicId = getTaskId(exchange);
            List<Subtask> subtaskList = httpTaskManager.getSubtasksListByEpicId(epicId);
            String jsonSubtaskList = gson.toJson(subtaskList);
            writeResponse(exchange, jsonSubtaskList, 200);

        }

        private int getTaskId(HttpExchange exchange) throws NumberFormatException, IndexOutOfBoundsException {
            String stringId = exchange.getRequestURI().getQuery().substring(3);
            return Integer.parseInt(stringId);

        }

        private void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
            OutputStream os = exchange.getResponseBody();
            if (responseString.isEmpty()) {
                exchange.sendResponseHeaders(responseCode, 0);
            } else {
                byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
                exchange.sendResponseHeaders(responseCode, bytes.length);
                os.write(bytes);
            }

        }

        private String getRequestBody(HttpExchange exchange) throws IOException {
            InputStream inputStream = exchange.getRequestBody();
            return new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
        }

        private void validate(Task task) throws ValidateException {
            if (task.getDescription().isBlank() || task.getName().isBlank() || task.getDurationInMinutes() < 0) {
                throw new ValidateException("Task's fields cannot be empty");
            }
        }

        private Endpoint getEndpoint(String requestPath, String requestMethod) {
            String[] pathParts = requestPath.split("/");

            if (requestMethod.equals("GET") && requestPath.equals("/tasks/task/")) {
                return Endpoint.GET_ALL_TASKS;
            }
            if (Objects.equals(requestMethod, "POST") && requestPath.equals("/tasks/task/")) {
                return Endpoint.CREATE_OR_UPDATE_TASK;
            }
            if (Objects.equals(requestMethod, "GET") && requestPath.startsWith("/tasks/task/?id=") && pathParts.length == 4) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (Objects.equals(requestMethod, "DELETE") && requestPath.equals("/tasks/task/")) {
                return Endpoint.DELETE_ALL_TASKS;
            }
            if (Objects.equals(requestMethod, "DELETE") && requestPath.startsWith("/tasks/task/?id=") && pathParts.length == 4) {
                return Endpoint.DELETE_TASK_BY_ID;
            }
            if (Objects.equals(requestMethod, "GET") && requestPath.equals("/tasks/subtask/")) {
                return Endpoint.GET_ALL_SUBTASKS;
            }
            if (Objects.equals(requestMethod, "GET") && requestPath.startsWith("/tasks/subtask/?id=") && pathParts.length == 4) {
                return Endpoint.GET_SUBTASK_BY_ID;
            }
            if (Objects.equals(requestMethod, "DELETE") && requestPath.equals("/tasks/subtask/")) {
                return Endpoint.DELETE_ALL_SUBTASKS;
            }
            if (Objects.equals(requestMethod, "DELETE") && requestPath.startsWith("/tasks/subtask/?id=") && pathParts.length == 4) {
                return Endpoint.DELETE_SUBTASK_BY_ID;
            }
            if (Objects.equals(requestMethod, "POST") && requestPath.equals("/tasks/subtask/")) {
                return Endpoint.CREATE_OR_UPDATE_SUBTASK;
            }
            if (Objects.equals(requestMethod, "GET") && requestPath.equals("/tasks/epic/")) {
                return Endpoint.GET_ALL_EPICS;
            }
            if (Objects.equals(requestMethod, "GET") && requestPath.startsWith("/tasks/epic/?id=") && pathParts.length == 4) {
                return Endpoint.GET_EPIC_BY_ID;
            }
            if (Objects.equals(requestMethod, "DELETE") && requestPath.equals("/tasks/epic/")) {
                return Endpoint.DELETE_ALL_EPICS;
            }
            if (Objects.equals(requestMethod, "DELETE") && requestPath.startsWith("/tasks/epic/?id=") && pathParts.length == 4) {
                return Endpoint.DELETE_EPIC_BY_ID;
            }
            if (Objects.equals(requestMethod, "POST") && requestPath.equals("/tasks/epic/")) {
                return Endpoint.CREATE_OR_UPDATE_EPIC;
            }
            if (Objects.equals(requestMethod, "GET") && requestPath.equals("/tasks/")) {
                return Endpoint.GET_PRIORITIZED;
            }
            if (requestMethod.equals("GET") && requestPath.equals("/tasks/history")) {
                return Endpoint.GET_HISTORY;
            }
            if (requestMethod.equals("GET") && requestPath.startsWith("/tasks/subtask/epic/?id=") && pathParts.length == 5) {
                return Endpoint.GET_SUBTASKS_LIST_BY_EPIC_ID;
            }
            return Endpoint.UNKNOWN;
        }
    }

    private static enum Endpoint {
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

}
