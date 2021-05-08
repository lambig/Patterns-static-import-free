package io.github.lambig.patterns;

import static io.github.lambig.funcifextension.function.Functions.compositionOf;
import static io.github.lambig.funcifextension.function.Functions.sequenceOf;
import static io.github.lambig.patterns.Patterns.equalsTo;
import static io.github.lambig.patterns.Patterns.orElse;
import static io.github.lambig.patterns.Patterns.patterns;
import static io.github.lambig.patterns.Patterns.then;
import static io.github.lambig.patterns.Patterns.thenApply;
import static io.github.lambig.patterns.Patterns.thenSupply;
import static io.github.lambig.patterns.Patterns.when;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PatternsTest {

  @Nested
  class 設定と取得のテスト {
    @Test
    void 該当キーに対応する値または適用結果が取得できること() {
      //SetUp
      Patterns<Integer, String> target =
          patterns(
              when(equalsTo(3), then("b")),
              when(equalsTo(4), thenSupply(() -> "c")),
              when(i -> i > 0, thenApply(Object::toString)),
              when(i -> i < 0, thenApply(sequenceOf((UnaryOperator<Integer>) i -> i + 1, Number::longValue, Object::toString))),
              orElse(then("a")));

      //Exercise
      List<String> actual = Stream.of(-1, 0, 1, 2, 3, 4).map(target).collect(toList());
      //Verify
      assertThat(actual).containsExactly("0", "a", "1", "2", "b", "c");
    }

    @Test
    void 該当キーに対応する値がなければnullが取得できること() {
      //SetUp
      UnaryOperator<Integer> add1 = i -> i + 1;
      Patterns<Integer, String> target =
          patterns(
              when(equalsTo(3), then("b")),
              when(i -> i > 0, thenApply(Object::toString)),
              when(i -> i < 0, thenApply(compositionOf(Object::toString, i -> i, add1))));

      //Exercise
      List<String> actual = Stream.of(-1, 0, 1, 2, 3).map(target).collect(toList());
      //Verify
      assertThat(actual).containsExactly("0", null, "1", "2", "b");
    }
  }
}