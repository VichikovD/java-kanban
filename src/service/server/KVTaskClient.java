package service.server;

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
        kvTaskClient.put("1", "value");
        System.out.println(kvTaskClient.load("1"));
        System.out.println(kvTaskClient.load("3"));
    }

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    HttpClient client;
    String url;
    long ApiToken;

    public KVTaskClient(String url) throws ConnectException {
        this.client = HttpClient.newHttpClient();
        this.url = url;
        register();
    }

    public void register() throws ConnectException {
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
        } catch (ConnectException e) { // обрабатываем ошибки отправки запроса
            throw new ConnectException(e.getMessage());
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            throw new ConnectException("Error occurred during registering to KVServer, check URL and try again");
        }
    }

    public void put(String key, String json) throws ConnectException {
        URI registerURL = URI.create(url + "save/" + key + "?API_TOKEN=" + ApiToken);

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(json, DEFAULT_CHARSET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(registerURL)
                .POST(bodyPublisher)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ConnectException("In reply to save request received status code: " + response.statusCode());
            } else {
                System.out.println("StatusCode = 200, information saved under key: " + key + "\n");
            }
        } catch (ConnectException e) { // обрабатываем ошибки отправки запроса
            throw new ConnectException(e.getMessage());
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            throw new ConnectException("Error occurred, check URL and try again");
        }
    }

    public String load(String key) throws ConnectException {
        URI registerURL = URI.create(url + "load/" + key + "?API_TOKEN=" + ApiToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(registerURL)
                .GET()
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ConnectException("In reply to load request received status code: " + response.statusCode());
            } else {
                System.out.println("StatusCode = 200, information loaded under key: " + key + "\n");
                return response.body();
            }
        } catch (ConnectException e) { // обрабатываем ошибки отправки запроса
            throw new ConnectException(e.getMessage());
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            throw new ConnectException("Error occurred, check URL and try again");
        }
    }
}