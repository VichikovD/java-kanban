package service.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class KVTaskClient {
    public static void main(String[] args) {
        KVTaskClient kvTaskClient = new KVTaskClient("http://localhost:8010/");
        kvTaskClient.put("1", "value");
        System.out.println(kvTaskClient.load("1"));
        System.out.println(kvTaskClient.load("3"));
    }
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    HttpClient client;
    String url;
    long ApiToken;

    public KVTaskClient(String url) {
        this.client = HttpClient.newHttpClient();
        this.url = url;
        register();
    }

    public void register() {
        URI registerURL = URI.create(url + "register");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(registerURL)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("In reply to register request received status code: " + response.statusCode() + "\n");
            } else {
                ApiToken = Long.parseLong(response.body());
                System.out.println("received API_TOKEN: " + ApiToken + "\n");
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("Error occurred, check URL and try again" + "\n");
        }
    }

    public void put(String key, String json) {
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
                System.out.println("StatusCode = 200, information saved under key: " + key + "\n");;
            } else {
                System.out.println("In reply to save request received status code: " + response.statusCode() + "\n");
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("Error occurred, check URL and try again" + "\n");
        }
    }

    public String load(String key) {
        URI registerURL = URI.create(url + "load/" + key + "?API_TOKEN=" + ApiToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(registerURL)
                .GET()
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("In reply to load request received status code: " + response.statusCode() + "\n");
                return "";
            } else {
                System.out.println("StatusCode = 200, information loaded under key: " + key + "\n");;
                return response.body();
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("Error occurred, check URL and try again" + "\n");
            return "";
        }
    }

        /*URI url = URI.create("https://api.exchangerate.host/latest?base=RUB&symbols=USD,EUR");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // проверяем, успешно ли обработан запрос
            if (response.statusCode() == 200) {
                JsonElement jsonElement = JsonParser.parseString(response.body());
                if(!jsonElement.isJsonObject()) { // проверяем, точно ли мы получили JSON-объект
                    System.out.println("Ответ от сервера не соответствует ожидаемому.");
                    return;
                }
                JsonObject maneObject = jsonElement.getAsJsonObject();
                // получите курс доллара и евро и запишите в переменные rateUSD и rateEUR
                JsonElement jsonElementRates = maneObject.get("rates");
                if(!jsonElementRates.isJsonObject()) { // проверяем, точно ли мы получили JSON-объект
                    System.out.println("Формат курсов валют в ответе от сервера не соответствует ожидаемому.");
                    return;
                }

                JsonObject jsonObjectRates = jsonElementRates.getAsJsonObject();

                String rateUSD = jsonObjectRates.get("USD").getAsString();

                System.out.println("Стоимость рубля в долларах: " + rateUSD + " USD");
            } else {
                System.out.println("Что-то пошло не так. Сервер вернул код состояния: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }*/

    }
