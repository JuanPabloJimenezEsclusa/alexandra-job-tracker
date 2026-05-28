package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NotesTest {

  private static Stream<Arguments> validNotes() {
    return Stream.of(
      arguments(named("with text", "follow up"), "follow up"),
      arguments(named("null", null), null)
    );
  }

  private static Stream<Arguments> blankNotes() {
    return Stream.of(arguments(named("blank", " ")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validNotes")
  void shouldCreateNotes(final String value, final String expected) {
    assertThat(Notes.of(value)).extracting(Notes::value).isEqualTo(expected);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("blankNotes")
  void shouldRejectBlankNotes(final String value) {
    assertThatThrownBy(() -> Notes.of(value))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldCreateEmptyNotes() {
    assertThat(Notes.empty().isEmpty()).isTrue();
  }
}
