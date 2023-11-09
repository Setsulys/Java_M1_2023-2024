package fr.umlv.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Document <T>{
	public record Template(List<String> fragments) {
		
		public Template{
			Objects.requireNonNull(fragments);
			fragments.stream().forEach(Objects::requireNonNull);
			if(fragments.isEmpty()) {
				throw new IllegalArgumentException();
			}
			fragments = List.copyOf(fragments);
		}

		@Override
		public String toString() {
			return fragments.stream().map(e -> e.toString()).collect(Collectors.joining("@"));
		}

		public <E> String interpolate(List<E> list) {
			Objects.requireNonNull(list);
			if(list.isEmpty()|| list.size() != fragments().size()-1) {
				throw new IllegalArgumentException();
			}
//			StringBuilder builder = new StringBuilder();
//			builder.append(fragments().get(0));
//			IntStream.range(0, list.size())
//					.forEach(i -> {
//						builder.append(list.get(i)); 
//						builder.append(fragments.get(i+1));});
//			return builder.toString();
			return IntStream.range(0, list.size()).mapToObj(i -> fragments().get(i)+ String.valueOf(list.get(i))).collect(Collectors.joining("","",fragments.getLast()));
		}
		
		public static Template of(String str) {
			Objects.requireNonNull(str);
			var start =0;
			var newTemplate = new ArrayList<String>();
			for(var i = 0; i <str.length();i++) {
				var c = str.charAt(i);
				if(c=='@') {
					newTemplate.add(str.substring(start,i));
					start=i+1;
				}
			}
			newTemplate.add(str.substring(start));
			return new Template(newTemplate);
		}

		@SuppressWarnings("unchecked")
		public <T> Document<T> toDocument(Function<? super T,? extends Object> ... functions) {
			Objects.requireNonNull(functions);
			return new Document<T>(this,Arrays.asList(functions));
		}
	}
	
	private final Template template;
	private final List<? extends Function<? super T,? extends Object>> list;	
	Document(Template newTemplate,List<? extends Function<? super T,? extends Object>> newList){
		Objects.requireNonNull(newList);
		Objects.requireNonNull(newTemplate);
		if(newList.size() != newTemplate.fragments.size()-1) {
			throw new IllegalArgumentException();
		}
		template = newTemplate;
		list = newList;
	}
	
//	public String applyTemplate(Record rec) {
//		Objects.requireNonNull(rec);
//		template.interpolate(rec.);
//		return null;
//	}
}
