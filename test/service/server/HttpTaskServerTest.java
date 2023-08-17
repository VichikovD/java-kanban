package service.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.mem.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class HttpTaskServerTest {
    static final boolean CHOOSE_CREATE = false;
    static final boolean CHOOSE_UPDATE = true;
    static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    HttpTaskServer httpTaskServer = new HttpTaskServer(new InMemoryTaskManager());
    HttpClient client = HttpClient.newHttpClient();
    String hostUrl = "http://localhost:8080/tasks/";
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public HttpTaskServerTest() throws IOException {
    }


    @BeforeEach
    public void startKVServerStartHttpTaskServerCreateHttpClient() throws IOException {
        httpTaskServer.start();
    }

    @AfterEach
    public void stopServer() {
        httpTaskServer.stop(0);
    }


    @Test
    public void getHistory() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    public void createTask() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "task/");
        Task task1 = getStandardTask(CHOOSE_CREATE);

        HttpResponse<String> response = SendRequestCreateRequestGetResponse(task1, url);

        assertEquals(List.of(new Task(1, "name", Status.NEW, "description")), httpTaskServer.httpTaskManager.getAllTasks());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void UpdateTask() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "task/");
        Task task1 = getStandardTask(CHOOSE_CREATE);
        HttpResponse<String> response1 = SendRequestCreateRequestGetResponse(task1, url);
        Task taskUpdated = new Task(1, "New name", Status.DONE, "New description");
        HttpRequest.BodyPublisher bodyPublisherUpdate = HttpRequest.BodyPublishers.ofString(gson.toJson(taskUpdated), DEFAULT_CHARSET);
        HttpRequest requestUpdate = HttpRequest.newBuilder()
                .uri(url)
                .POST(bodyPublisherUpdate)
                .build();

        HttpResponse<String> response2 = client.send(requestUpdate, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(taskUpdated), httpTaskServer.httpTaskManager.getAllTasks());
        assertEquals(200, response1.statusCode());
        assertEquals(200, response2.statusCode());
    }

    @Test
    public void createEpicAndSubtask() throws IOException, InterruptedException {
        URI urlEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        URI urlSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = getStandardSutbask(CHOOSE_CREATE, 2, 1);
        Epic expectedEpic = new Epic(1, "name", Status.NEW, "description",
                Instant.parse("2023-05-01T00:00:00.000Z"), Duration.ofDays(31).toMinutes());
        expectedEpic.addSubtaskId(2);
        expectedEpic.setEndTime(Instant.parse("2023-05-01T00:00:00.000Z").plus(Duration.ofDays(31)));

        HttpResponse<String> responseEpic = SendRequestCreateRequestGetResponse(epic, urlEpic);
        HttpResponse<String> responseSubtask = SendRequestCreateRequestGetResponse(epic, urlEpic);

        assertEquals(List.of(new Subtask(2, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes(), 1)), httpTaskServer.httpTaskManager.getAllSubtasks());
        assertEquals(List.of(expectedEpic), httpTaskServer.httpTaskManager.getAllEpics());
        assertEquals(200, responseEpic.statusCode());
        assertEquals(200, responseSubtask.statusCode());
    }

    @Test
    public void createEpicAndCreateUpdateSubask() throws IOException, InterruptedException {
        URI urlEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        URI urlSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = getStandardSutbask(CHOOSE_CREATE, 2, 1);
        Subtask updatedSubtask = new Subtask(2, "new name", Status.DONE, "NEW description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(11).toMinutes(), 1);
        HttpRequest.BodyPublisher bodyPublisherSubtask = HttpRequest.BodyPublishers.ofString(gson.toJson(updatedSubtask), DEFAULT_CHARSET);
        HttpRequest requestUpdateSubtask = HttpRequest.newBuilder()
                .uri(urlSubtask)
                .POST(bodyPublisherSubtask)
                .build();
        Epic expectedEpic = new Epic(1, "name", Status.DONE, "description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(11).toMinutes());
        expectedEpic.addSubtaskId(2);
        expectedEpic.setEndTime(Instant.parse("2023-01-01T00:00:00.000Z").plus(Duration.ofDays(11)));

        HttpResponse<String> responseEpic = SendRequestCreateRequestGetResponse(epic, urlEpic);
        HttpResponse<String> responseSubtask = SendRequestCreateRequestGetResponse(subtask, urlSubtask);
        HttpResponse<String> responseUpdateSubtask = client.send(requestUpdateSubtask, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(updatedSubtask), httpTaskServer.httpTaskManager.getAllSubtasks());
        assertEquals(List.of(expectedEpic), httpTaskServer.httpTaskManager.getAllEpics());
        assertEquals(200, responseEpic.statusCode());
        assertEquals(200, responseSubtask.statusCode());
        assertEquals(200, responseUpdateSubtask.statusCode());
    }

    @Test
    public void createAndUpdateEpic() throws IOException, InterruptedException {
        URI urlEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseEpic = SendRequestCreateRequestGetResponse(epic, urlEpic);
        Epic updatedEpic = new Epic(1, "new name", Status.DONE, "new description",
                Instant.parse("2023-01-01T00:00:00.000Z"), Duration.ofDays(11).toMinutes());
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(updatedEpic), DEFAULT_CHARSET);
        HttpRequest requestUpdateEpic = HttpRequest.newBuilder()
                .uri(urlEpic)
                .POST(bodyPublisherEpic)
                .build();
        Epic expectedEpic = new Epic(1, "new name", Status.NEW, "new description", null, 0);

        HttpResponse<String> responseUpdateEpic = client.send(requestUpdateEpic, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(expectedEpic), httpTaskServer.httpTaskManager.getAllEpics());
        assertEquals(200, responseEpic.statusCode());
        assertEquals(200, responseUpdateEpic.statusCode());
    }

    @Test
    public void getAllTask() throws IOException, InterruptedException {
        URI urlCreate = URI.create(hostUrl + "task/");
        Task task1 = getStandardTask(CHOOSE_CREATE);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(task1), DEFAULT_CHARSET);
        HttpRequest requestCreate = HttpRequest.newBuilder()
                .uri(urlCreate)
                .POST(bodyPublisher)
                .build();
        HttpResponse<String> responseCreate = SendRequestCreateRequestGetResponse(task1, urlCreate);
        URI urlGetTasks = URI.create(hostUrl + "task/");
        HttpRequest requestGetTasks = HttpRequest.newBuilder()
                .uri(urlGetTasks)
                .GET()
                .build();

        HttpResponse<String> responseGetTasks = client.send(requestGetTasks, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(
                new Task(1, "name", Status.NEW, "description")),
                httpTaskServer.httpTaskManager.getAllTasks(),
                "not 1 task added to server manager");
        assertEquals(200, responseGetTasks.statusCode());
    }

    @Test
    public void getAllSubtask() throws IOException, InterruptedException {
        URI urlEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest requestEpic = HttpRequest.newBuilder()
                .uri(urlEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseEpic = SendRequestCreateRequestGetResponse(epic, urlEpic);
        URI urlSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = getStandardSutbask(CHOOSE_CREATE, 2, 1);
        HttpRequest.BodyPublisher bodyPublisherSubtask = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask), DEFAULT_CHARSET);
        HttpRequest requestSubtask = HttpRequest.newBuilder()
                .uri(urlSubtask)
                .POST(bodyPublisherSubtask)
                .build();
        HttpResponse<String> responseSubtask = SendRequestCreateRequestGetResponse(subtask, urlSubtask);
        URI url = URI.create(hostUrl + "subtask/");
        HttpRequest requestGetSubtasks = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> responseGetTasks = client.send(requestGetSubtasks, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(
                new Subtask(2, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                        Duration.ofDays(31).toMinutes(), 1)),
                httpTaskServer.httpTaskManager.getAllSubtasks(), "not 1 subtask added to server manager");
        assertEquals(200, responseGetTasks.statusCode());
    }

    @Test
    public void getAllEpic() throws IOException, InterruptedException {
        URI urlCreate = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseCreate = SendRequestCreateRequestGetResponse(epic, urlCreate);
        URI urlGet = URI.create(hostUrl + "epic/");
        HttpRequest requestGet = HttpRequest.newBuilder()
                .uri(urlGet)
                .GET()
                .build();
        Epic expectedEpic = new Epic(1, "name", Status.NEW, "description", null, 0);

        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(expectedEpic), httpTaskServer.httpTaskManager.getAllEpics());
        assertEquals(200, responseGet.statusCode());
    }

    @Test
    public void deleteAllTask() throws IOException, InterruptedException {
        URI urlCreate = URI.create(hostUrl + "task/");
        Task task1 = getStandardTask(CHOOSE_CREATE);
        HttpResponse<String> responseCreate = SendRequestCreateRequestGetResponse(task1, urlCreate);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(task1), DEFAULT_CHARSET);
        HttpRequest requestCreate = HttpRequest.newBuilder()
                .uri(urlCreate)
                .POST(bodyPublisher)
                .build();
        URI url = URI.create(hostUrl + "task/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> responseDelete = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(), httpTaskServer.httpTaskManager.getAllTasks());
        assertEquals(204, responseDelete.statusCode());
    }

    @Test
    public void deleteAllSubtask() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseCreateEpic = SendRequestCreateRequestGetResponse(epic, urlCreateEpic);
        URI urlCreateSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = getStandardSutbask(CHOOSE_CREATE, 2, 1);
        HttpResponse<String> responseCreateSubtask = SendRequestCreateRequestGetResponse(subtask, urlCreateSubtask);
        URI urlDeleteAllSubtasks = URI.create(hostUrl + "subtask/");
        HttpRequest requestDeleteAllSubtasks = HttpRequest.newBuilder()
                .uri(urlDeleteAllSubtasks)
                .DELETE()
                .build();

        HttpResponse<String> responseDeleteSubtasks = client.send(requestDeleteAllSubtasks, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(), httpTaskServer.httpTaskManager.getAllSubtasks());
        assertEquals(204, responseDeleteSubtasks.statusCode());
    }

    @Test
    public void deleteAllEpic() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseCreateEpic = SendRequestCreateRequestGetResponse(epic, urlCreateEpic);
        URI url = URI.create(hostUrl + "epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(), httpTaskServer.httpTaskManager.getAllEpics());
        assertEquals(204, response.statusCode());
    }

    @Test
    public void getPrioritized() throws IOException, InterruptedException {
        URI urlCreate = URI.create(hostUrl + "task/");
        Task task1 = getStandardTask(CHOOSE_CREATE);
        HttpResponse<String> responseCreate = SendRequestCreateRequestGetResponse(task1, urlCreate);
        URI url = URI.create(hostUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(new Task(1, "name", Status.NEW, "description")),
                httpTaskServer.httpTaskManager.getPrioritizedTasks());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getTaskById() throws IOException, InterruptedException {
        URI urlCreateTask = URI.create(hostUrl + "task/");
        Task task1 = getStandardTask(CHOOSE_CREATE);
        HttpResponse<String> responseCreate = SendRequestCreateRequestGetResponse(task1, urlCreateTask);
        URI urlGetById = URI.create(hostUrl + "task/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .GET()
                .build();

        HttpResponse<String> responseGetById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());

        assertEquals(new Task(1, "name", Status.NEW, "description"), httpTaskServer.httpTaskManager.getTaskById(1));
        assertEquals(200, responseCreate.statusCode());
        assertEquals(200, responseGetById.statusCode());
    }

    @Test
    public void getSubtaskById() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseCreateEpic = SendRequestCreateRequestGetResponse(epic, urlCreateEpic);
        URI urlCreateSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = getStandardSutbask(CHOOSE_CREATE, 2, 1);
        HttpResponse<String> responseCreateSubtask = SendRequestCreateRequestGetResponse(subtask, urlCreateSubtask);
        URI urlGetById = URI.create(hostUrl + "subtask/?id=2");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .GET()
                .build();

        HttpResponse<String> responseGetById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseCreateEpic.statusCode());
        assertEquals(200, responseCreateSubtask.statusCode());
        assertEquals(200, responseGetById.statusCode());
    }

    @Test
    public void getEpicById() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseCreateEpic = SendRequestCreateRequestGetResponse(epic, urlCreateEpic);
        URI urlGetById = URI.create(hostUrl + "epic/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .GET()
                .build();

        HttpResponse<String> responseGetById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseCreateEpic.statusCode());
        assertEquals(200, responseGetById.statusCode());
    }

    @Test
    public void getSubtaskListByEpicId() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseCreateEpic = SendRequestCreateRequestGetResponse(epic, urlCreateEpic);
        URI urlCreateSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = getStandardSutbask(CHOOSE_CREATE, 2, 1);
        HttpResponse<String> responseCreateSubtask = SendRequestCreateRequestGetResponse(subtask, urlCreateSubtask);
        URI urlGetSubtaskListByEpicId = URI.create(hostUrl + "subtask/epic/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetSubtaskListByEpicId)
                .GET()
                .build();

        HttpResponse<String> responseGetSubtaskListByEpicId = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseCreateEpic.statusCode());
        assertEquals(200, responseCreateSubtask.statusCode());
        assertEquals(200, responseGetSubtaskListByEpicId.statusCode());
    }

    @Test
    public void deleteTaskById() throws IOException, InterruptedException {
        URI urlCreateTask = URI.create(hostUrl + "task/");
        Task task1 = getStandardTask(CHOOSE_CREATE);
        HttpResponse<String> responseCreate = SendRequestCreateRequestGetResponse(task1, urlCreateTask);
        URI urlGetById = URI.create(hostUrl + "task/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .DELETE()
                .build();

        HttpResponse<String> responseDeleteTaskById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(), httpTaskServer.httpTaskManager.getAllTasks());
        assertEquals(200, responseCreate.statusCode());
        assertEquals(204, responseDeleteTaskById.statusCode());
    }

    @Test
    public void deleteSubtaskById() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseCreateEpic = SendRequestCreateRequestGetResponse(epic, urlCreateEpic);
        URI urlCreateSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = getStandardSutbask(CHOOSE_CREATE, 2, 1);
        HttpResponse<String> responseCreateSubtask = SendRequestCreateRequestGetResponse(subtask, urlCreateSubtask);
        URI urlDeleteSubtaskById = URI.create(hostUrl + "subtask/?id=2");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlDeleteSubtaskById)
                .DELETE()
                .build();

        HttpResponse<String> responseDeleteSubtaskById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(), httpTaskServer.httpTaskManager.getAllSubtasks());
        assertEquals(200, responseCreateEpic.statusCode());
        assertEquals(200, responseCreateSubtask.statusCode());
        assertEquals(204, responseDeleteSubtaskById.statusCode());
    }

    @Test
    public void deleteEpicById() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = getStandardEpic(CHOOSE_CREATE);
        HttpResponse<String> responseCreateEpic = SendRequestCreateRequestGetResponse(epic, urlCreateEpic);
        URI urlGetById = URI.create(hostUrl + "epic/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .DELETE()
                .build();

        HttpResponse<String> responseDeleteEpicById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());

        assertEquals(List.of(), httpTaskServer.httpTaskManager.getAllEpics());
        assertEquals(200, responseCreateEpic.statusCode());
        assertEquals(204, responseDeleteEpicById.statusCode());
    }

    Task getStandardTask(boolean bool) {
        if (bool) {
            return new Task(1, "name", Status.NEW, "description");
        } else {
            return new Task(null, "name", Status.NEW, "description");
        }
    }


    Subtask getStandardSutbask(boolean bool, int id, int epicId) {
        if (bool) {
            return new Subtask(id, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                    Duration.ofDays(31).toMinutes(), epicId);
        } else {
            return new Subtask(null, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                    Duration.ofDays(31).toMinutes(), epicId);
        }
    }

    Epic getStandardEpic(boolean bool) {
        if (bool) {
            return new Epic(1, "name", "description");
        } else {
            return new Epic(null, "name", "description");
        }
    }

    public HttpResponse<String> SendRequestCreateRequestGetResponse(Task task, URI url) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(task), DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(bodyPublisherEpic)
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}