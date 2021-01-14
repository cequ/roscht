package de.morsepost.roscht;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class CommandLineDisplayer {

	private static final Map<String, Map<String, String>> textInLanguages = new HashMap<>();

	static {
		String en = Locale.ENGLISH.getLanguage();
		textInLanguages.put("roschtWelcome", Map.of(en, "---------------\n Roscht Server\n---------------\n"));
		textInLanguages.put("loadingX", Map.of(en, "Loading "));
		textInLanguages.put("endpointDataBase", Map.of(en, "Endpoint Database"));
		textInLanguages.put(".", Map.of(en, ". "));
		textInLanguages.put("startingServer", Map.of(en, "\nStarting Server ... "));
		textInLanguages.put("startedServer", Map.of(en, "Roscht is up.\n"));
		textInLanguages.put("selectOption", Map.of(en, "\nInput options are " +
														" ( 1 ) Hash a string, " +
														" ( 0 ) Shut down server" +
														"\nEnter option: "));
		textInLanguages.put("goodbye", Map.of(en, "\nShut down Server. Bye.\n"));
		textInLanguages.put("enterCharaters", Map.of(en, "\nEnter characters:"));
	}

	private final String language;

	CommandLineDisplayer(String language) {
		this.language = language;
	}

	CommandLineDisplayer print(String textIdentifier) {
		String text = textInLanguages.getOrDefault(textIdentifier, Map.of()).getOrDefault(language, "");
		System.out.print(text);

		return this;
	}

	CommandLineDisplayer printRaw(String text) {
		System.out.print(text);

		return this;
	}
}
