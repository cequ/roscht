package de.morsepost.roscht;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

import org.json.JSONObject;

public class Initiator {

	private static CommandLineDisplayer displayer = new CommandLineDisplayer(Locale.ENGLISH.getLanguage());

	public static void main(String[] args) {
		displayer.print("roschtWelcome");
		try {
			JSONObject endpoints = readEndpoints();
			startRoschtServer(endpoints);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static JSONObject readEndpoints() throws IOException {
		displayer.print("loadingX");
		JSONObject endpointDataBase = new JSONObject(FileOperator.createOrLoad("/endpointDataBase.json", "{}"));
		displayer.print("endpointDataBase").print(".");
		return endpointDataBase;
	}

	private static void startRoschtServer(JSONObject endpoints) throws IOException {
		displayer.print("startingServer");
		boolean isRunningCommandLineInterface = true;

		try (RoschtServer server = new RoschtServer(endpoints);
			Scanner inputScanner = new Scanner(System.in)) {
			displayer.print("startedServer");

			String input = "";
			while (isRunningCommandLineInterface) {
				displayer.print("selectOption");
				input = inputScanner.nextLine();
				if ("0".equals(input)) {
					isRunningCommandLineInterface = false;
					displayer.print("goodbye");
					return;
				}
				if ("1".equals(input))
				{
					displayer.print("enterCharaters");
					input = inputScanner.nextLine();
					String hash = server.hash(input);
					displayer.printRaw(hash).printRaw("\n");
				}
			}
		}
	}

}
