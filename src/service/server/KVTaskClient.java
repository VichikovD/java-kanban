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
    /*public static void main(String[] args) throws ConnectException {
        KVTaskClient kvTaskClient = new KVTaskClient("http://localhost:8010/");
    }*/

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    HttpClient client;
    String url;
    long ApiToken;

    public KVTaskClient(String url) {
        this.client = HttpClient.newHttpClient();
        this.url = url;
        try {
            register();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register() throws IOException {
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
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            throw new IOException("Error occurred during register request to KVServer, check URL and try again", e);
        }
    }

    public void put(String key, String json) throws IOException {
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
                throw new ConnectException("In reply to save request to KVServer received status code: " + response.statusCode());
            } else {
                System.out.println("StatusCode = 200, information saved under key: " + key + "\n");
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            throw new IOException("Error occurred during save request to KVServer, check URL and try again", e);
        }
    }

    public String load(String key) throws IOException {
        URI registerURL = URI.create(url + "load/" + key + "?API_TOKEN=" + ApiToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(registerURL)
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
            throw new IOException("Error occurred during load request to KVServer, check URL and try again", e);
        }
    }
}