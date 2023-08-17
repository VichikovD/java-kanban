package service.server;

import service.server.exception.KVClientRegisterException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class KVTaskClient {
    public static void main(String[] args) throws ConnectException {
        KVTaskClient kvTaskClient = new KVTaskClient("http://localhost:8010/");
    }

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    HttpClient client;
    String url;
    long ApiToken;

    /*Если мы решили выкидывать исключеня внутри HttpTaskManager при ошибке в сохранении/загрузке на/с сервер(а),
    то при нынешней реализации возможных URL запросов к HttpTaskServer (без возможности повторной регистрации KVClient
    на KVServer) лучше сразу не дать создать HttpTaskManager, т.к. у него не будет токена
    для сохранения/загрузки с KVServer, даже если тот будет в последствии запущен, а значит не будут проходить
    запросы к HttpTaskServer по созданию/обновлению тасок. Решил при запуске HttpTaskServer явно указать ошибку в регистрации*/

    public KVTaskClient(String url) throws KVClientRegisterException {
        this.client = HttpClient.newHttpClient();
        this.url = url;
        register();
    }

    public void register() throws KVClientRegisterException {
        URI registerURL = URI.create(url + "register");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(registerURL)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ConnectException("In reply to register to KVServer request received status code: " + response.statusCode());
            } else {
                ApiToken = Long.parseLong(response.body());
                System.out.println("received API_TOKEN: " + ApiToken + "\n");
            }
        } catch (IOException | InterruptedException e) {
            throw new KVClientRegisterException("URL - " + registerURL, e);
        }
    }

    public void put(String key, String json) throws IOException {
        URI saveURL = URI.create(url + "save/" + key + "?API_TOKEN=" + ApiToken);

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(json, DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(saveURL)
                .POST(bodyPublisher)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ConnectException("In reply to save request to KVServer received status code: " + response.statusCode());
            } else {
                System.out.println("StatusCode = 200, information saved under key: " + key + "\n");
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            throw new IOException("URL - " + saveURL, e);
        }
    }

    public String load(String key) throws IOException {
        URI loadURL = URI.create(url + "load/" + key + "?API_TOKEN=" + ApiToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(loadURL)
                .GET()
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ConnectException("In reply to load request to KVServer received status code: " + response.statusCode());
            } else {
                System.out.println("StatusCode = 200, information loaded under key: " + key + "\n");
                return response.body();
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            throw new IOException("URL - " + loadURL, e);
        }
    }
}