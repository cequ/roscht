package de.morsepost.roscht;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class FileOperator {

	private static final String rootDirectory = "./root";
	private static final Path rootPath = Paths.get(rootDirectory).normalize().toAbsolutePath();

	static String createOrLoad(String fileName, String defaultText) throws IOException {
		Path path = Paths.get(rootDirectory + fileName).normalize().toAbsolutePath();

		if (!isExsistingFile(path) && path.startsWith(rootPath)) {
			if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
					Files.createDirectory(rootPath);
			}

			Path parent = path.getParent();
			if(!Files.exists(parent)) {
				Files.createDirectories(parent);
			}

			Files.createFile(path);
			write(path, defaultText);

			return defaultText;
		}

		return load(path);
	}

	private static boolean isExsistingFile(Path path) {
		if (!path.normalize().toAbsolutePath().startsWith(rootPath)) {
			return false;
		}

		return Files.exists(path) && !Files.isDirectory(path);
	}

	static String read(String fileName, String defaultText) throws IOException {
		Path path = Paths.get(rootDirectory + fileName);

		if (!isExsistingFile(path)) {
			return defaultText;
		}

		return load(path);
	}

	private static String load(Path path) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
			reader.lines().forEach(stringBuilder::append);
			return stringBuilder.toString();
		}
	}

	static void update(String fileName, String text) throws IOException {
		Path path = Paths.get(rootDirectory + fileName);

		if (!isExsistingFile(path)) {
			createOrLoad(fileName, text);
		}

		write(path, text);
	}

	private static void write(Path path, String defaultText) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
			writer.write(defaultText);
		}
	}

	static boolean delete(String fileName) {
		Path path = Paths.get(rootDirectory + fileName);

		if (!isExsistingFile(path)) {
			return false;
		}

		try {
			Files.delete(path);
			return true;
		} catch (IOException ignore) {
			return false;
		}
	}
}
