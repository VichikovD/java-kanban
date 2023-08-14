package service;

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
import service.server.HttpTaskServer;
import service.server.InstantAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

public class HttpTaskServerTest {
    static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    HttpTaskServer httpTaskServer = new HttpTaskServer();
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
        httpTaskServer.stop(1);
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
        Task task1 = new Task(1, "name", Status.NEW, "description");

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(task1), DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(bodyPublisher)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void UpdateTask() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "task/");
        Task task1 = new Task(1, "name", Status.NEW, "description");

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(task1), DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(bodyPublisher)
                .build();
        HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response1.statusCode());

        HttpResponse<String> response2 = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode());
    }

    @Test
    public void createEpicAndSubtask() throws IOException, InterruptedException {
        URI urlEpic = URI.create(hostUrl + "epic/");
        Epic epic = new Epic(1, "name", "description");
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest requestEpic = HttpRequest.newBuilder()
                .uri(urlEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseEpic = client.send(requestEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseEpic.statusCode());

        URI urlSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = new Subtask(2, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes(), 1);
        HttpRequest.BodyPublisher bodyPublisherSubtask = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask), DEFAULT_CHARSET);
        HttpRequest requestSubtask = HttpRequest.newBuilder()
                .uri(urlSubtask)
                .POST(bodyPublisherSubtask)
                .build();
        HttpResponse<String> responseSubtask = client.send(requestSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseSubtask.statusCode());
    }

    @Test
    public void createEpicAndCreateUpdateSubask() throws IOException, InterruptedException {
        URI urlEpic = URI.create(hostUrl + "epic/");
        Epic epic = new Epic(1, "name", "description");
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest requestEpic = HttpRequest.newBuilder()
                .uri(urlEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseEpic = client.send(requestEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseEpic.statusCode());

        URI urlSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = new Subtask(2, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes(), 1);
        HttpRequest.BodyPublisher bodyPublisherSubtask = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask), DEFAULT_CHARSET);
        HttpRequest requestSubtask = HttpRequest.newBuilder()
                .uri(urlSubtask)
                .POST(bodyPublisherSubtask)
                .build();
        HttpResponse<String> responseSubtask = client.send(requestSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseSubtask.statusCode());

        Subtask updatedSubtask = new Subtask(2, "new name", Status.DONE, "new description",
                Instant.parse("2023-05-01T00:00:00.000Z"), Duration.ofDays(25).toMinutes(), 1);
        bodyPublisherSubtask = HttpRequest.BodyPublishers.ofString(gson.toJson(updatedSubtask), DEFAULT_CHARSET);
        HttpRequest requestUpdateSubtask = HttpRequest.newBuilder()
                .uri(urlSubtask)
                .POST(bodyPublisherSubtask)
                .build();
        HttpResponse<String> responseUpdateSubtask = client.send(requestUpdateSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseUpdateSubtask.statusCode());
    }

    @Test
    public void createAndUpdateEpic() throws IOException, InterruptedException {
        URI urlEpic = URI.create(hostUrl + "epic/");
        Epic epic = new Epic(1, "name", "description");
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest requestEpic = HttpRequest.newBuilder()
                .uri(urlEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseEpic = client.send(requestEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseEpic.statusCode());

        Epic updatedEpic = new Epic(1, "new name", "new description");
        bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(updatedEpic), DEFAULT_CHARSET);
        HttpRequest requestUpdateEpic = HttpRequest.newBuilder()
                .uri(urlEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseUpdateEpic = client.send(requestUpdateEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseUpdateEpic.statusCode());
    }

    @Test
    public void getAllTask() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "task/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getAllSubtask() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "subtask/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getAllEpic() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void deleteAllTask() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "task/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
    }
    @Test
    public void deleteAllSubtask() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "subtask/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
    }
    @Test
    public void deleteAllEpic() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl + "epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
    }

    @Test
    public void getPrioritized() throws IOException, InterruptedException {
        URI url = URI.create(hostUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getTaskById() throws IOException, InterruptedException {
        URI urlCreateTask = URI.create(hostUrl + "task/");
        Task task1 = new Task(1, "name", Status.NEW, "description");

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(task1), DEFAULT_CHARSET);
        HttpRequest requestCreate = HttpRequest.newBuilder()
                .uri(urlCreateTask)
                .POST(bodyPublisher)
                .build();
        HttpResponse<String> responseCreate = client.send(requestCreate, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreate.statusCode());

        URI urlGetById = URI.create(hostUrl + "task/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .GET()
                .build();
        HttpResponse<String> responseGetById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetById.statusCode());
    }

    @Test
    public void getSubtaskById() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = new Epic(1, "name", "description");
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(urlCreateEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseCreateEpic = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreateEpic.statusCode());

        URI urlCreateSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = new Subtask(2, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes(), 1);
        HttpRequest.BodyPublisher bodyPublisherSubtask = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask), DEFAULT_CHARSET);
        HttpRequest requestSubtask = HttpRequest.newBuilder()
                .uri(urlCreateSubtask)
                .POST(bodyPublisherSubtask)
                .build();
        HttpResponse<String> responseCreateSubtask = client.send(requestSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreateSubtask.statusCode());

        URI urlGetById = URI.create(hostUrl + "subtask/?id=2");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .GET()
                .build();
        HttpResponse<String> responseGetById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetById.statusCode());
    }

    @Test
    public void getEpicById() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = new Epic(1, "name", "description");
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(urlCreateEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseCreateEpic = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreateEpic.statusCode());

        URI urlGetById = URI.create(hostUrl + "epic/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .GET()
                .build();
        HttpResponse<String> responseGetById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetById.statusCode());
    }

    @Test
    public void getSubtaskListByEpicId() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = new Epic(1, "name", "description");
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(urlCreateEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseCreateEpic = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreateEpic.statusCode());

        URI urlCreateSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = new Subtask(2, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes(), 1);
        HttpRequest.BodyPublisher bodyPublisherSubtask = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask), DEFAULT_CHARSET);
        HttpRequest requestSubtask = HttpRequest.newBuilder()
                .uri(urlCreateSubtask)
                .POST(bodyPublisherSubtask)
                .build();
        HttpResponse<String> responseCreateSubtask = client.send(requestSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreateSubtask.statusCode());

        URI urlGetSubtaskListByEpicId = URI.create(hostUrl + "subtask/epic/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetSubtaskListByEpicId)
                .GET()
                .build();
        HttpResponse<String> responseGetSubtaskListByEpicId = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetSubtaskListByEpicId.statusCode());
    }

    @Test
    public void deleteTaskById() throws IOException, InterruptedException {
        URI urlCreateTask = URI.create(hostUrl + "task/");
        Task task1 = new Task(1, "name", Status.NEW, "description");

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(task1), DEFAULT_CHARSET);
        HttpRequest requestCreate = HttpRequest.newBuilder()
                .uri(urlCreateTask)
                .POST(bodyPublisher)
                .build();
        HttpResponse<String> responseCreate = client.send(requestCreate, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreate.statusCode());

        URI urlGetById = URI.create(hostUrl + "task/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .DELETE()
                .build();
        HttpResponse<String> responseDeleteTaskById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, responseDeleteTaskById.statusCode());
    }

    @Test
    public void deleteSubtaskById() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = new Epic(1, "name", "description");
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(urlCreateEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseCreateEpic = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreateEpic.statusCode());

        URI urlCreateSubtask = URI.create(hostUrl + "subtask/");
        Subtask subtask = new Subtask(2, "name", Status.NEW, "description", Instant.parse("2023-05-01T00:00:00.000Z"),
                Duration.ofDays(31).toMinutes(), 1);
        HttpRequest.BodyPublisher bodyPublisherSubtask = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask), DEFAULT_CHARSET);
        HttpRequest requestSubtask = HttpRequest.newBuilder()
                .uri(urlCreateSubtask)
                .POST(bodyPublisherSubtask)
                .build();
        HttpResponse<String> responseCreateSubtask = client.send(requestSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreateSubtask.statusCode());

        URI urlDeleteSubtaskById = URI.create(hostUrl + "subtask/?id=2");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlDeleteSubtaskById)
                .DELETE()
                .build();
        HttpResponse<String> responseDeleteSubtaskById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, responseDeleteSubtaskById.statusCode());
    }

    @Test
    public void deleteEpicById() throws IOException, InterruptedException {
        URI urlCreateEpic = URI.create(hostUrl + "epic/");
        Epic epic = new Epic(1, "name", "description");
        HttpRequest.BodyPublisher bodyPublisherEpic = HttpRequest.BodyPublishers.ofString(gson.toJson(epic), DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(urlCreateEpic)
                .POST(bodyPublisherEpic)
                .build();
        HttpResponse<String> responseCreateEpic = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCreateEpic.statusCode());

        URI urlGetById = URI.create(hostUrl + "epic/?id=1");
        HttpRequest requestGetById = HttpRequest.newBuilder()
                .uri(urlGetById)
                .DELETE()
                .build();
        HttpResponse<String> responseDeleteEpicById = client.send(requestGetById, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, responseDeleteEpicById.statusCode());
    }
}