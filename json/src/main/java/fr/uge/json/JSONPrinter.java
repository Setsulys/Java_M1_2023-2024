package fr.uge.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JSONPrinter {
	
	private static final ClassValue<RecordComponent[]> CACHE = new ClassValue<>() {

		@Override
		protected RecordComponent[] computeValue(Class<?> type) {
			return type.getRecordComponents();
		}
		
	};
	
	static Object invoke(Method method, Object object) {
		Objects.requireNonNull(method);
		Objects.requireNonNull(object);
		try {
			var m = method.invoke(object);
			return m;
		} catch (IllegalAccessException e) {
			throw new IllegalAccessError();
		} catch(InvocationTargetException e) {
			var cause = e.getCause();
			switch(cause) {
			case RuntimeException rte -> throw rte;
			case Error error -> throw error;
			default -> throw new UndeclaredThrowableException(cause);
			}
		}
	}

	@SuppressWarnings("unused")
	private static String escape(Object o) {
		return o instanceof String s ? "\"" + s + "\"": "" + o;
	}

	public static String toJSON(Record record) {
		Objects.requireNonNull(record);

		return Arrays.stream(CACHE.get(record.getClass()))
				.map(e-> {
					var annote = e.isAnnotationPresent(JSONProperty.class)? e.getAnnotation(JSONProperty.class).value():e.getName();
					return "\""+annote+"\":" +escape(invoke(e.getAccessor(),record));
					}
				)
				.collect(Collectors.joining(",","{","}"));
	}
	
	public static String toJSON(List<? extends Record> list) {
		Objects.requireNonNull(list);
		list.forEach(Objects::requireNonNull);
		return list.stream().map(e -> toJSON(e)).collect(Collectors.joining(",","[","]"));
	}
}
