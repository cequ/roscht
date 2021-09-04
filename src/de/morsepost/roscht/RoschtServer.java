package de.morsepost.roscht;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class RoschtServer implements HttpHandler, Closeable {

	private final JSONObject endpoints;
	private final HttpServer httpServer;
	private final ExecutorService executor;

	RoschtServer(JSONObject endpoints) throws IOException {
		this.endpoints = endpoints;
		httpServer = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 80), 0);
		httpServer.createContext("/", this);
		executor = Executors.newFixedThreadPool(4);
		httpServer.setExecutor(executor);
		httpServer.start();
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();
		if ("GET".equals(method) || "HEAD".equals(method)) {
			read(exchange);
		} else {
			modify(exchange, "DELETE".equals(method));
		}
	}

	@Override
	public void close() {
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException ignore) {
		}
		httpServer.stop(0);
	}

	private void read(HttpExchange exchange) throws IOException {
		String fileName = exchange.getRequestURI().getPath();

		String file = "";
		try {
			file = FileOperator.read(fileName, "");
		} catch (IOException e) {
			sendJson(exchange, 500, new JSONObject(Map.of("Ressource caused Server error", fileName)));
			throw e;
		}
		if (file.isEmpty()) {
			exchange.sendResponseHeaders(404, -1);
			return;
		}

		if (file.startsWith("<!DOCTYPE html")) {
			exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
		} else if (file.startsWith("{")) {
			exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
		} else if (file.startsWith("<svg")) {
			exchange.getResponseHeaders().set("Content-Type", "image/svg+xml; charset=UTF-8");
		} else {
			exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
		}

		if ("HEAD".equals(exchange.getRequestMethod())) {
			exchange.sendResponseHeaders(200, -1);
			return;
		} else {
			exchange.sendResponseHeaders(200, file.length());
			try (OutputStream outputStream = exchange.getResponseBody()) {
				outputStream.write(file.getBytes(Charset.forName("UTF-8")));
			}
		}
	}

	private void modify(HttpExchange exchange, boolean isDelete) throws IOException {
		try {
			JSONObject request = readRequestJson(exchange);

			JSONObject credentials = request.getJSONObject("roscht");
			String endpoint = credentials.getString("endpoint");
			if (!endpoints.has(endpoint) || !endpoints.get(endpoint).equals(hash(credentials.getString("secret")))) {
				sendJson(exchange, 401, new JSONObject());
				return;
			}

			String fileName = exchange.getRequestURI().getPath();

			if (isDelete || !request.has("data")) {
				try {
					boolean isDeleted = FileOperator.delete(fileName);
					sendJson(exchange, 200, new JSONObject(Map.of("deleted", isDeleted)));
				} catch (IOException e) {
					sendJson(exchange, 500, new JSONObject(Map.of("deleted", "false")));
					throw e;
				}
			} else {
				try {
					FileOperator.update(fileName, request.getJSONObject("data").toString(4));
					sendJson(exchange, 201, new JSONObject(Map.of("modified", "true")));
				} catch (IOException e) {
					sendJson(exchange, 500, new JSONObject(Map.of("modified", "false")));
					throw e;
				}
			}

			return;
		} catch (IllegalArgumentException e) {
			JSONObject wellformedJson = new JSONObject();
			wellformedJson.put("roscht", Map.of("endpoint", "string", "secret", "string"));
			wellformedJson.put("data", Map.of());

			sendJson(exchange, 400, wellformedJson);
			return;
		}
	}

	private JSONObject readRequestJson(HttpExchange exchange) throws IllegalArgumentException {
		JSONObject jsonRequest = new JSONObject();

		try (Scanner scanner = new Scanner(exchange.getRequestBody(), Charset.forName("UTF-8"))) {
			scanner.useDelimiter("\\A");
			if (!scanner.hasNext()) {
				throw new IllegalArgumentException("No body");
			}
			String textRequest = scanner.next();
			if (textRequest.isEmpty()) {
				throw new IllegalArgumentException("Empty body");
			}
			jsonRequest = new JSONObject(textRequest);
			if (!jsonRequest.has("roscht")) {
				throw new IllegalArgumentException("No credentials");
			}
			JSONObject roscht = jsonRequest.getJSONObject("roscht");
			if (!roscht.has("endpoint")) {
				throw new IllegalArgumentException("No endpoint");
			}
			if (!roscht.has("secret")) {
				throw new IllegalArgumentException("No Secret");
			}
			Object endpointRaw = roscht.get("endpoint");
			Object secret = roscht.get("secret");
			if (!(endpointRaw instanceof String && secret instanceof String)) {
				throw new IllegalArgumentException("Wrong format of secret or endpoint");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		return jsonRequest;
	}

	private void sendJson(HttpExchange httpExchange, int httpCode, JSONObject jsonObject) throws IOException {
		String responseText = jsonObject.toString();

		httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
		httpExchange.sendResponseHeaders(httpCode, responseText.length());
		try (OutputStream outputStream = httpExchange.getResponseBody()) {
			outputStream.write(responseText.getBytes(Charset.forName("UTF-8")));
		}
	}

	String hash(String input) {
		if (input == null || input.isEmpty()) {
			return "";
		}

		StringBuilder hashBuilder = new StringBuilder();
		int value, action = 0;
		for (char character : input.toCharArray()) {
			value = Integer.valueOf(character).intValue();
			if (action == 0) {
				value = value + 19;
			}
			if (action == 1) {
				value = value * value;
			}
			if (action == 2) {
				value = value - 27;
			}
			if (action == 4) {
				value = value / 7;
				action = -1;
			}
			hashBuilder.append(Integer.toHexString(value));
			action++;
		}

		if (hashBuilder.length() < 40) {
			String string = hashBuilder.toString();
			return string + hash(string.substring(1) + string.substring(0, 1));
		}
		String hash = hashBuilder.toString().substring(1) + Integer.toHexString(action + 13);
		return hash;
	}

}
