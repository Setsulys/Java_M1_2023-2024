package fr.uge.sed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class StreamEditor {

	@FunctionalInterface
	public interface Rule{
		Optional<String> rewrite(String str);

		static Rule andThen(Rule rule1, Rule rule2) {
			Objects.requireNonNull(rule1);
			Objects.requireNonNull(rule2);
			return (String str) -> rule1.rewrite(str).flatMap(rule2::rewrite);
		}
	}
	
	private final Rule rule;
	
	public StreamEditor(Rule rule) {
		Objects.requireNonNull(rule);
		this.rule = rule;
	}

	public void rewrite(BufferedReader reader, Writer writer) throws IOException{
		Objects.requireNonNull(reader);
		Objects.requireNonNull(writer);
		String line;
		while((line = reader.readLine())!= null) {
			var rew = rule.rewrite(line);
			if(!rew.isEmpty()) {
				writer.write(rew.get());
				writer.write("\n");
			}
		}

	}

	public void rewrite(Path input, Path output) throws IOException {
		Objects.requireNonNull(input);
		Objects.requireNonNull(output);
		try(var reader =Files.newBufferedReader(input)){
			try(var writer = Files.newBufferedWriter(output)){
				rewrite(reader,writer);
			}
		}
	}

	public static Rule createRules(String string) {
		Objects.requireNonNull(string);
		Rule rule =line ->Optional.of("");
		for(var c=0; c<string.length();c++) {
			Rule newRule = switch(String.valueOf(string.charAt(c))) {
			case "s" ->rule = line -> Optional.of(line.strip()); 
			case "u" ->rule = line -> Optional.of(line.toUpperCase(Locale.FRENCH));
			case "l" ->rule = line -> Optional.of(line.toLowerCase(Locale.FRENCH));
			case "d" ->rule = line ->Optional.empty();
			case "" -> rule =line ->Optional.of("");
			default -> throw new IllegalArgumentException();
			};
			rule = Rule.andThen(rule,newRule);
		}
		
		return rule;
	}
	
}
