package fr.uge.sed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class StreamEditor {

	@FunctionalInterface
	public interface Rule{
		Optional<String> rewrite(String str);

		static Rule andThen(Rule rule1, Rule rule2) {
			Objects.requireNonNull(rule1);
			Objects.requireNonNull(rule2);
			return (String line) -> rule1.rewrite(line).flatMap(rule2::rewrite);
		}
		default Rule andThen(Rule rule) {
			Objects.requireNonNull(rule);
			return (String line) -> rule.rewrite(this.rewrite(line).get());
		}
		static Rule guard(Predicate<String> function,Rule rule) {
			Objects.requireNonNull(function);
			Objects.requireNonNull(rule);
			return (String line) ->{
				if(function.test(line)) {
					return rule.rewrite(line);
				}
				else {
					return Optional.of(line);
				}
			};
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
	
	private static Rule switchOnRule(String string) {
		Objects.requireNonNull(string);
		Rule rule = line ->Optional.of(line);
		for(var c=0; c<string.length();c++) {
			Rule newRule = switch(String.valueOf(string.charAt(c))) {
			case "s" ->line -> Optional.of(line.strip());
			case "u" ->line -> Optional.of(line.toUpperCase(Locale.FRENCH));
			case "l" ->line -> Optional.of(line.toLowerCase(Locale.FRENCH));
			case "d" ->line -> Optional.empty();
			case "" -> line -> Optional.of(line);
			default -> throw new IllegalArgumentException();
			};
			rule = Rule.andThen(rule,newRule);
		}
		return rule;
	}

	public static Rule createRules(String string) {
		Objects.requireNonNull(string);
		Rule rule = line ->Optional.of(line);
		if(Pattern.compile(".*i=.*").matcher(string).matches()) {
			String strPred =string.split("i=")[1].split(";")[0];
			Predicate<String> pred = s -> s.matches(strPred);
			string = string.split("i=")[1].split(";")[1];
			rule= switchOnRule(string);
			Rule.guard(pred, rule);
		}
		else {
			rule = switchOnRule(string);
		}
		return rule;
	}
}