package fr.uge.sed;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamEditorTest {
  @Nested
  class Q1 {
    @Test
    public void ruleIsAnInterface() {
      assertTrue(StreamEditor.Rule.class.isInterface());
    }

    @Test
    public void ruleRewriteIsCorrectlyTyped() {
      StreamEditor.Rule rule = Optional::of;
      assertNotNull(rule);
    }

    @Test
    public void ruleRewriteIsCorrectlyTyped2() {
      StreamEditor.Rule rule = Optional::ofNullable;
      assertNotNull(rule);
    }

    @Test
    public void ruleIsDeclaredAsAFunctionalInterface() {
      assertTrue(StreamEditor.Rule.class.isAnnotationPresent(FunctionalInterface.class));
    }
  }

  @Nested
  class Q2 {
    @Test
    public void rewriteOneLine() throws IOException {
      StreamEditor.Rule rule = Optional::of;
      StreamEditor editor = new StreamEditor(rule);
      StringReader reader = new StringReader("hello\n");
      CharArrayWriter writer = new CharArrayWriter();
      try(BufferedReader bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("hello\n", writer.toString());
    }
    
    @Test
    public void duplicateOneLine() throws IOException {
      StreamEditor.Rule rule = line -> Optional.of(line+line);
      StreamEditor editor = new StreamEditor(rule);
      StringReader reader = new StringReader("hello\n");
      CharArrayWriter writer = new CharArrayWriter();
      try(BufferedReader bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("hellohello\n", writer.toString());
    }
    
    @Test
    public void deleteOneLine() throws IOException {
      StreamEditor.Rule rule = line -> Optional.empty();
      StreamEditor editor = new StreamEditor(rule);
      StringReader reader = new StringReader("hello\n");
      CharArrayWriter writer = new CharArrayWriter();
      try(BufferedReader bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("", writer.toString());
    }

    @Test
    public void rewriteSeveralLines() throws IOException {
      StreamEditor.Rule rule = Optional::of;
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          foo
          bar
          baz
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("""
          foo
          bar
          baz
          """, writer.toString());
    }
    
    @Test
    public void deleteSeveralLines() throws IOException {
      StreamEditor.Rule rule = line -> Optional.empty();
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          foo
          bar
          baz
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("", writer.toString());
    }

    @Test
    public void rewriteEmpty() throws IOException {
      StreamEditor.Rule rule = Optional::of;
      var editor = new StreamEditor(rule);
      var reader = Reader.nullReader();
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("", writer.toString());
    }

    @Test
    public void deleteEmpty() throws IOException {
      StreamEditor.Rule rule = line -> Optional.empty();
      var editor = new StreamEditor(rule);
      var reader = Reader.nullReader();
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("", writer.toString());
    }

    @Test
    public void rewriteALotOfLines() throws IOException {
      StreamEditor.Rule rule = Optional::of;
      var editor = new StreamEditor(rule);
      var text = IntStream.range(0, 100_000).mapToObj(i -> i + "\n").collect(joining(""));
      var reader = new StringReader(text);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals(text, writer.toString());
    }

    @Test
    public void rewriteAnUngodlyNumberOfLines() throws IOException {
      StreamEditor.Rule rule = Optional::of;
      var editor = new StreamEditor(rule);
      var reader = new Reader() {
        private int index;
        private boolean closed;

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
          Objects.checkFromIndexSize(off, len, cbuf.length);
          if (closed) {
            throw new IOException("closed");
          }
          var last = Math.min(1_000_000_000, index + len);
          var toRead = Math.min(last - index, len);
          for(var i = 0; i < toRead; i++) {
            cbuf[off + i] = (index + i) % 20 == 0 ? '\n' : 'A';
          }
          if (toRead == 0) {
            return -1;
          }
          index += toRead;
          return toRead;
        }

        @Override
        public void close() {
          closed = true;
        }
      };
      var writer = Writer.nullWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
    }

    @Test
    public void streamEditorClassIsFinal() {
      assertTrue(StreamEditor.class.accessFlags().contains(AccessFlag.FINAL));
    }

    @Test
    public void preconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> new StreamEditor(null)),
          () -> assertThrows(NullPointerException.class, () -> new StreamEditor(Optional::of).rewrite(null, Writer.nullWriter())),
          () -> assertThrows(NullPointerException.class, () -> new StreamEditor(Optional::of).rewrite(new BufferedReader(Reader.nullReader()), null))
      );
    }
  }


  @Nested
  class Q3 {
    private static String rewriteUsingFiles(StreamEditor editor, String text) throws IOException {
      var directory = Files.createTempDirectory("stream-editor-test");
      try {
        var inputPath = directory.resolve("input.txt");
        var outputPath = directory.resolve("output.txt");
        try {
          Files.writeString(inputPath, text);
          editor.rewrite(inputPath, outputPath);
          return Files.readString(outputPath);
        } finally {
          Files.deleteIfExists(outputPath);
          Files.deleteIfExists(inputPath);
        }
      } finally {
        Files.delete(directory);
      }
    }

    @Test
    public void rewriteOneLine() throws IOException {
      StreamEditor.Rule rule = Optional::of;
      var editor = new StreamEditor(rule);
      var text = """
          hello my name is Joey
          """;
      var result = rewriteUsingFiles(editor, text);
      assertEquals(text, result);
    }

    @Test
    public void rewriteALotOfLines() throws IOException {
      StreamEditor.Rule rule = Optional::of;
      var editor = new StreamEditor(rule);
      var text = IntStream.range(0, 100_000).mapToObj(i -> i + "\n").collect(joining(""));
      var result = rewriteUsingFiles(editor, text);
      assertEquals(text, result);
    }

    @Test
    public void deleteOneLine() throws IOException {
      StreamEditor.Rule rule = line -> Optional.empty();
      var editor = new StreamEditor(rule);
      var text = """
          hello my name is Joey
          """;
      var result = rewriteUsingFiles(editor, text);
      assertEquals("", result);
    }

    @Test
    public void deleteALotOfLines() throws IOException {
      StreamEditor.Rule rule = line -> Optional.empty();
      var editor = new StreamEditor(rule);
      var text = IntStream.range(0, 100_000).mapToObj(i -> i + "\n").collect(joining(""));
      var result = rewriteUsingFiles(editor, text);
      assertEquals("", result);
    }

    @Test
    public void preconditions() {
      var streamEditor = new StreamEditor(Optional::of);
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> streamEditor.rewrite(null, Path.of("."))),
          () -> assertThrows(NullPointerException.class, () -> streamEditor.rewrite(Path.of("."), null))
      );
    }
  }


  @Nested
  class Q4 {

    @Test
    public void createRulesStrip() {
      var rule = StreamEditor.createRules("s");
      assertAll(
          () -> assertEquals("foo", rule.rewrite("  foo  ").orElseThrow()),
          () -> assertEquals("bar", rule.rewrite("bar  ").orElseThrow()),
          () -> assertEquals("baz", rule.rewrite("  baz").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createRulesUppercase() {
      var rule = StreamEditor.createRules("u");
      assertAll(
          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
          () -> assertEquals("BAR", rule.rewrite("BAR").orElseThrow())
      );
    }

    @Test
    public void createRulesUppercaseDotlessI() {
      // see https://en.wikipedia.org/wiki/Dotless_I
      var rule = StreamEditor.createRules("u");
      var oldLocale = Locale.getDefault();
      Locale.setDefault(Locale.forLanguageTag("tr-tr"));
      try {
        assertEquals("ILL", rule.rewrite("ill").orElseThrow());
      } finally {
        Locale.setDefault(oldLocale);
      }
    }

    @Test
    public void createRulesLowercase() {
      var rule = StreamEditor.createRules("l");
      assertAll(
          () -> assertEquals("foo", rule.rewrite("FOO").orElseThrow()),
          () -> assertEquals("bar", rule.rewrite("bar").orElseThrow())
      );
    }

    @Test
    public void createRulesLowercaseDotlessI() {
      // see https://en.wikipedia.org/wiki/Dotless_I
      var rule = StreamEditor.createRules("l");
      var oldLocale = Locale.getDefault();
      Locale.setDefault(Locale.forLanguageTag("tr-tr"));
      try {
        assertEquals("ill", rule.rewrite("ILL").orElseThrow());
      } finally {
        Locale.setDefault(oldLocale);
      }
    }

    @Test
    public void createRulesUpperCaseNonLatin() {
      var rule = StreamEditor.createRules("u");
      assertEquals("\u039b", rule.rewrite("\u03bb").orElseThrow());
    }

    @Test
    public void createRulesMalformed() {
      assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("z"));
    }

    @Test
    public void deleteOneLine() throws IOException {
      var rule = StreamEditor.createRules("d");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("hello\n");
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("", writer.toString());
    }

    @Test
    public void stripSeveraLines() throws IOException {
      var rule = StreamEditor.createRules("s");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
            foo
           bar
          baz
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("""
          foo
          bar
          baz
          """, writer.toString());
    }

    @Test
    public void uppercaseSeveraLines() throws IOException {
      var rule = StreamEditor.createRules("u");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          foo
           bar  \s
          BaZ
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("""
          FOO
           BAR  \s
          BAZ
          """, writer.toString());
    }

    @Test
    public void lowercaseSeveraLines() throws IOException {
      var rule = StreamEditor.createRules("u");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          FOO
           bar  \s
          baz
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("""
          FOO
           BAR  \s
          BAZ
          """, writer.toString());
    }
  }


  @Nested
  class Q5 {
    @Test
    public void andThen() {
      var rule1 = StreamEditor.createRules("s");
      var rule2 = StreamEditor.createRules("l");
      var rule = StreamEditor.Rule.andThen(rule1, rule2);
      assertAll(
          () -> assertEquals("foo", rule.rewrite("FOO").orElseThrow()),
          () -> assertEquals("bar", rule.rewrite(" BaR ").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void andThenPrecondition() {
      var rule = StreamEditor.createRules("");
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> StreamEditor.Rule.andThen(null, rule)),
          () -> assertThrows(NullPointerException.class, () -> StreamEditor.Rule.andThen(rule, null))
      );
    }

    @Test
    public void createRulesStripUppercase() {
      var rule = StreamEditor.createRules("su");
      assertAll(
          () -> assertEquals("FOO", rule.rewrite("  foo  ").orElseThrow()),
          () -> assertEquals("BAR", rule.rewrite("bar  ").orElseThrow()),
          () -> assertEquals("BAZ", rule.rewrite("  baz").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createRulesUppercaseStrip() {
      var rule = StreamEditor.createRules("us");
      assertAll(
          () -> assertEquals("FOO", rule.rewrite("  foo  ").orElseThrow()),
          () -> assertEquals("BAR", rule.rewrite("bar  ").orElseThrow()),
          () -> assertEquals("BAZ", rule.rewrite("  baz").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createRulesDeleteUppercase() {
      var rule = StreamEditor.createRules("du");
      assertAll(
          () -> assertTrue(rule.rewrite("  foo  ").isEmpty()),
          () -> assertTrue(rule.rewrite("bar  ").isEmpty()),
          () -> assertTrue(rule.rewrite("  baz").isEmpty()),
          () -> assertTrue(rule.rewrite("").isEmpty())
      );
    }

    @Test
    public void createRulesEmpty() {
      var rule = StreamEditor.createRules("");
      assertAll(
          () ->  assertEquals("foo", rule.rewrite("foo").orElseThrow()),
          () ->  assertEquals("bAr", rule.rewrite("bAr").orElseThrow()),
          () ->  assertEquals(" ", rule.rewrite(" ").orElseThrow()),
          () ->  assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createVeryLongRules() {
      var rule = StreamEditor.createRules("uls".repeat(1_000_000));
      assertNotNull(rule);
    }

    @Test
    public void aLotOfRules() {
      var rule = StreamEditor.createRules("lu".repeat(100));
      assertAll(
          () -> assertEquals("FOO", rule.rewrite("FOO").orElseThrow()),
          () -> assertEquals("BAR", rule.rewrite("bar").orElseThrow()),
          () -> assertEquals("  FOO  ", rule.rewrite("  FOO  ").orElseThrow()),
          () -> assertEquals("  BAR  ", rule.rewrite("  bar  ").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void lowercaseStripSeveraLines() throws IOException {
      var rule = StreamEditor.createRules("ls");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          FOO
           bar  \s
          bAz
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("""
          foo
          bar
          baz
          """, writer.toString());
    }

    @Test
    public void deleteUppercaseSeveraLines() throws IOException {
      var rule = StreamEditor.createRules("du");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          FOO
           bar  \s
          bAz
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("", writer.toString());
    }

    @Test
    public void createRulesMalformed() {
      assertAll(
          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("uw")),
          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("wu"))
      );
    }
  }


  @Nested
  class Q6 {
    @Test
    public void andThen2() {
      var rule1 = StreamEditor.createRules("s");
      var rule2 = StreamEditor.createRules("l");
      var rule = rule1.andThen(rule2);
      assertAll(
          () -> assertEquals("foo", rule.rewrite("FOO").orElseThrow()),
          () -> assertEquals("bar", rule.rewrite(" BaR ").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void andThen2Precondition() {
      var rule = StreamEditor.createRules("");
      assertThrows(NullPointerException.class, () -> rule.andThen(null));
    }
  }


  @Nested
  class Q7 {
    @Test
    public void guard() {
      var uppercase = StreamEditor.createRules("u");
      var rule = StreamEditor.Rule.guard("foo"::equals, uppercase);
      assertAll(
          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
          () -> assertEquals("bar", rule.rewrite("bar").orElseThrow()),
          () -> assertEquals("  foo ", rule.rewrite("  foo ").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void guardDelete() {
      var delete = StreamEditor.createRules("d");
      var rule = StreamEditor.Rule.guard("foo"::equals, delete);
      assertAll(
          () -> assertTrue(rule.rewrite("foo").isEmpty()),
          () -> assertEquals("bar", rule.rewrite("bar").orElseThrow()),
          () -> assertEquals("  foo ", rule.rewrite("  foo ").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void guardEmptyDelete() {
      var delete = StreamEditor.createRules("d");
      var rule = StreamEditor.Rule.guard(String::isEmpty, delete);
      assertAll(
          () -> assertEquals("foo", rule.rewrite("foo").orElseThrow()),
          () -> assertTrue(rule.rewrite("").isEmpty())
      );
    }

    @Test
    public void guardPreconditions() {
      var rule = StreamEditor.createRules("");
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> StreamEditor.Rule.guard(null, rule)),
          () -> assertThrows(NullPointerException.class, () -> StreamEditor.Rule.guard("foo"::startsWith, null))
      );
    }

    @Test
    public void createRulesIfUpperCase() {
      var rule = StreamEditor.createRules("i=foo;u");
      assertAll(
          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
          () -> assertEquals("bar", rule.rewrite("bar").orElseThrow()),
          () -> assertEquals("  foo ", rule.rewrite("  foo ").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createRulesIfDelete() {
      var rule = StreamEditor.createRules("i=foo;d");
      assertAll(
          () -> assertTrue(rule.rewrite("foo").isEmpty()),
          () -> assertEquals("bar", rule.rewrite("bar").orElseThrow()),
          () -> assertEquals("  foo ", rule.rewrite("  foo ").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createRulesIfStripUppercase() {
      var rule = StreamEditor.createRules("i= foo ;su");
      assertAll(
          () -> assertEquals("FOO", rule.rewrite(" foo ").orElseThrow()),
          () -> assertEquals(" bar ", rule.rewrite(" bar ").orElseThrow()),
          () -> assertEquals("foo", rule.rewrite("foo").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createRulesIfEmpty() {
      var rule = StreamEditor.createRules("i=;");
      assertAll(
          () -> assertEquals("foo", rule.rewrite("foo").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createRulesUpperCaseThenIfLowerCase() {
      var rule = StreamEditor.createRules("ui=HELLO;l");
      assertAll(
          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
          () -> assertEquals("hello", rule.rewrite("HELLO").orElseThrow()),
          () -> assertEquals("hello", rule.rewrite("Hello").orElseThrow()),
          () -> assertEquals("hello", rule.rewrite("hello").orElseThrow()),
          () -> assertEquals("", rule.rewrite("").orElseThrow())
      );
    }

    @Test
    public void createRulesDeleteThenIfLowerCase() {
      var rule = StreamEditor.createRules("di=HELLO;l");
      assertAll(
          () -> assertTrue(rule.rewrite("foo").isEmpty()),
          () -> assertTrue(rule.rewrite("HELLO").isEmpty()),
          () -> assertTrue(rule.rewrite("").isEmpty())
      );
    }

    @Test
    public void ifDeleteSeveraLines() throws IOException {
      var rule = StreamEditor.createRules("i=foo;d");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          FOO
          foo
          bar
          foo
          BAZ
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("""
          FOO
          bar
          BAZ
          """, writer.toString());
    }

    @Test
    public void ifEmptyDeleteSeveraLines() throws IOException {
      var rule = StreamEditor.createRules("i=;d");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          
          foo
          bar
          
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("""
          foo
          bar
          """, writer.toString());
    }

    @Test
    public void ifLowercaseStripSeveraLines() throws IOException {
      var rule = StreamEditor.createRules("i= Bar;ls");
      var editor = new StreamEditor(rule);
      var reader = new StringReader("""
          FOO
           Bar
            baz  \s
          """);
      var writer = new CharArrayWriter();
      try(var bufferedReader = new BufferedReader(reader)) {
        editor.rewrite(bufferedReader, writer);
      }
      assertEquals("""
          FOO
          bar
            baz  \s
          """, writer.toString());
    }

    @Test
    public void createRulesMalformed() {
      assertAll(
          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("i")),
          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("i=")),
          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("i;=")),
          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("i;")),
          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("ia;=x"))
      );
    }
  }


//  @Nested
//  class Q8 {
//    @Test
//    public void createRulesPatternIfUpperCase() {
//      var rule = StreamEditor.createRules("i=f.*;u");
//      assertAll(
//          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
//          () -> assertEquals("FAR", rule.rewrite("far").orElseThrow()),
//          () -> assertEquals("  foo ", rule.rewrite("  foo ").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void createRulesPatternIfDelete() {
//      var rule = StreamEditor.createRules("i=[ab]+;d");
//      assertAll(
//          () -> assertTrue(rule.rewrite("a").isEmpty()),
//          () -> assertTrue(rule.rewrite("baba").isEmpty()),
//          () -> assertEquals("  foo ", rule.rewrite("  foo ").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void createRulesPatternIfStripUppercase() {
//      var rule = StreamEditor.createRules("i=[ ]*foo[ ]*;su");
//      assertAll(
//          () -> assertEquals("FOO", rule.rewrite(" foo ").orElseThrow()),
//          () -> assertEquals(" bar ", rule.rewrite(" bar ").orElseThrow()),
//          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void patternIfDeleteSeveraLines() throws IOException {
//      var rule = StreamEditor.createRules("i=f.*;d");
//      var editor = new StreamEditor(rule);
//      var reader = new StringReader("""
//          far
//          foo
//          bar
//          foo
//          BAZ
//          """);
//      var writer = new CharArrayWriter();
//      try(var bufferedReader = new BufferedReader(reader)) {
//        editor.rewrite(bufferedReader, writer);
//      }
//      assertEquals("""
//          bar
//          BAZ
//          """, writer.toString());
//    }
//
//    @Test
//    public void patternIfLowercaseStripSeveraLines() throws IOException {
//      var rule = StreamEditor.createRules("i=.*FOO.*;ls");
//      var editor = new StreamEditor(rule);
//      var reader = new StringReader("""
//          FOO
//           Bar
//            FOO  \s
//          """);
//      var writer = new CharArrayWriter();
//      try(var bufferedReader = new BufferedReader(reader)) {
//        editor.rewrite(bufferedReader, writer);
//      }
//      assertEquals("""
//          foo
//           Bar
//          foo
//          """, writer.toString());
//    }
//
//    @Test
//    public void createRulesMalformed() {
//      assertAll(
//          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("ia;=xu")),
//          () -> assertThrows(IllegalArgumentException.class, () -> StreamEditor.createRules("ia;=ux"))
//      );
//    }
//  }
//
//
//  @Nested
//  class Q9 {
//    @Test
//    public void withAsFilter() {
//      var strip = StreamEditor.createRules("s");
//      var uppercase = StreamEditor.createRules("u");
//      var rule = StreamEditor.Rule.guard(strip.withAsFilter("foo"::equals), uppercase);
//      assertAll(
//          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
//          () -> assertEquals("bar", rule.rewrite("bar").orElseThrow()),
//          () -> assertEquals("  FOO ", rule.rewrite("  foo ").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void withAsFilterPreconditions() {
//      var rule = StreamEditor.createRules("");
//      assertAll(
//          () -> assertThrows(NullPointerException.class, () -> rule.withAsFilter(null))
//      );
//    }
//
//    @Test
//    public void createRulesPatternStripIfUpperCaseString() {
//      var rule = StreamEditor.createRules("is=foo;u");
//      assertAll(
//          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
//          () -> assertEquals("far", rule.rewrite("far").orElseThrow()),
//          () -> assertEquals("boo", rule.rewrite("boo").orElseThrow()),
//          () -> assertEquals("  FOO ", rule.rewrite("  foo ").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void createRulesPatternStripIfUpperCasePattern() {
//      var rule = StreamEditor.createRules("is=f.*;u");
//      assertAll(
//          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
//          () -> assertEquals("FAR", rule.rewrite("far").orElseThrow()),
//          () -> assertEquals("boo", rule.rewrite("boo").orElseThrow()),
//          () -> assertEquals("  FOO ", rule.rewrite("  foo ").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void createRulesPatternLowercaseIfDelete() {
//      var rule = StreamEditor.createRules("il=[ab]+;d");
//      assertAll(
//          () -> assertTrue(rule.rewrite("a").isEmpty()),
//          () -> assertTrue(rule.rewrite("bABa").isEmpty()),
//          () -> assertEquals("  foo ", rule.rewrite("  foo ").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void createRulesPatternStripLowercaseIfStripUppercase() {
//      var rule = StreamEditor.createRules("isl=f.*;su");
//      assertAll(
//          () -> assertEquals("FOO", rule.rewrite(" fOo ").orElseThrow()),
//          () -> assertEquals(" bar ", rule.rewrite(" bar ").orElseThrow()),
//          () -> assertEquals("FOO", rule.rewrite("foo").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void createRulesPatternDeleteIfUppercase() {
//      var rule = StreamEditor.createRules("id=f;u");
//      assertAll(
//          () -> assertEquals("hello", rule.rewrite("hello").orElseThrow()),
//          () -> assertEquals("foo", rule.rewrite("foo").orElseThrow()),
//          () -> assertEquals("", rule.rewrite("").orElseThrow())
//      );
//    }
//
//    @Test
//    public void patternStripIfDeleteSeveraLines() throws IOException {
//      var rule = StreamEditor.createRules("is=f.*;d");
//      var editor = new StreamEditor(rule);
//      var reader = new StringReader("""
//          far
//          foo
//          bar
//            foo
//          BAZ
//          """);
//      var writer = new CharArrayWriter();
//      try (var bufferedReader = new BufferedReader(reader)) {
//        editor.rewrite(bufferedReader, writer);
//      }
//      assertEquals("""
//          bar
//          BAZ
//          """, writer.toString());
//    }
//
//    @Test
//    public void patternIfLowercaseStripSeveraLines() throws IOException {
//      var rule = StreamEditor.createRules("isl=f.*;su");
//      var editor = new StreamEditor(rule);
//      var reader = new StringReader("""
//          foo
//           Bar
//            fOo  \s
//          """);
//      var writer = new CharArrayWriter();
//      try (var bufferedReader = new BufferedReader(reader)) {
//        editor.rewrite(bufferedReader, writer);
//      }
//      assertEquals("""
//          FOO
//           Bar
//          FOO
//          """, writer.toString());
//    }
//  }
}