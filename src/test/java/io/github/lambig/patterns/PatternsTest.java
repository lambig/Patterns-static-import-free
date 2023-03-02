package io.github.lambig.patterns;

import static io.github.lambig.funcifextension.function.Functions.compositionOf;
import static io.github.lambig.funcifextension.function.Functions.sequenceOf;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PatternsTest {

    @Nested
    class 設定と取得のテスト {
        @Test
        void 該当キーに対応する値または適用結果が取得できること() {
            //SetUp
            Patterns<Integer, String> target =
                    Patterns.of(
                            P.when(P.equalsTo(3), P.then("b")),
                            P.when(P.equalsTo(4), P.thenSupply(() -> "c")),
                            P.when(i -> i > 0, P.thenApply(Object::toString)),
                            P.when(i -> i < 0, P.thenApply(sequenceOf((UnaryOperator<Integer>) i -> i + 1, Number::longValue, Object::toString))),
                            P.orElse(P.then("a")));

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
                    Patterns.of(
                            P.when(P.equalsTo(3), P.then("b")),
                            P.when(i -> i.toString().length() > 10, P.thenApply(Object::toString)),
                            P.when(i -> i < 0, P.thenApply(compositionOf(Object::toString, i -> i, add1))));

            //Exercise
            List<String> actual = Stream.of(-1, 0, 1, 2, 3).map(target).collect(toList());
            //Verify
            assertThat(actual).containsExactly("0", null, null, null, "b");
        }

        @Test
        void 該当キーに対応する値がなければnullが取得できること_リスト引数版() {
            //SetUp
            UnaryOperator<Integer> add1 = i -> i + 1;
            Patterns<Integer, String> target =
                    Patterns.of(
                            Arrays.asList(
                                    P.when(P.equalsTo(3), P.then("b")),
                                    P.when(i -> i.toString().length() > 10, P.thenApply(Object::toString)),
                                    P.when(i -> i < 0, P.thenApply(compositionOf(Object::toString, i -> i, add1)))));

            //Exercise
            List<String> actual = Stream.of(-1, 0, 1, 2, 3).map(target).collect(toList());
            //Verify
            assertThat(actual).containsExactly("0", null, null, null, "b");
        }

        @Test
        void 該当キーに対応する値がなければnullが取得できること_型宣言なし() {
            //Exercise
            UnaryOperator<Integer> add1 = i -> i + 1;
            List<String> actual =
                    Stream.of(-1, 0, 1, 2, 3)
                            .map(Patterns.of(
                                    P.when(i -> i.toString().length() > 10, P.thenApply(Object::toString)),
                                    P.when(P.equalsTo(3), P.then("b")),
                                    P.when(i -> i < 0, P.thenApply(compositionOf(Object::toString, i -> i, add1)))))
                            .collect(toList());
            //Verify
            assertThat(actual).containsExactly("0", null, null, null, "b");
        }

        @Test
        void 該当キーに対応する値がなければ例外が送出されること() {
            //SetUp
            UnaryOperator<Integer> add1 = i -> i + 1;

            Patterns<Integer, String> target =
                    Patterns.of(
                            P.when(P.equalsTo(3),
                                    P.then("b")),
                            P.when(i -> i > 0,
                                    P.thenApply(Object::toString)),
                            P.when(i -> i < 0,
                                    P.thenApply(compositionOf(Object::toString, i -> i, add1))),
                            P.orElseThrow(v -> new RuntimeException("この値はパターンにありません: " + v)));

            //Exercise
            try {
                List<String> actual = Stream.of(0).map(target).collect(toList());
                //Verify
                fail();
            } catch (RuntimeException e) {
                assertThat(e).hasMessage("この値はパターンにありません: 0");
            }
        }

        //SetUp
        @RequiredArgsConstructor
        @Accessors(fluent = true)
        @Getter
        class A {
            final String value;
        }

        class B extends A {
            public B(String value) {
                super(value);
            }
        }

        class C extends A {
            public C(String value) {
                super(value);
            }

            public String say() {
                return "C here. I've got " + this.value() + ".";
            }
        }

        @Test
        void 型パターンマッチのテスト() {
            //SetUp
            Patterns<A, String> target =
                    Patterns.of(
                            P.whenMatch(
                                    B.class,
                                    P.thenApply(b -> "it's a B. value: " + b.value() + ".")),
                            P.whenMatch(
                                    C.class,
                                    c -> c.say().contains("here"),
                                    P.thenApply(C::say)),
                            P.orElse(P.then("it's a plain A.")));

            //Exercise
            List<String> actual = Stream.of(new A("aaa"), new B("bbb"), new C("ccc")).map(target).collect(toList());
            //Verify
            assertThat(actual).containsExactly("it's a plain A.", "it's a B. value: bbb.", "C here. I've got ccc.");
        }

        @Test
        void 型パターンマッチのテスト_型宣言なし() {
            //SetUp
            //Exercise
            List<String> actual =
                    Stream.of(new A("aaa"), new B("bbb"), new C("ccc"))
                            .map(Patterns.of(
                                    P.whenMatch(
                                            B.class,
                                            P.thenApply(b -> "it's a B. value: " + b.value() + ".")),
                                    P.whenMatch(
                                            C.class,
                                            c -> c.say().contains("here"),
                                            P.thenApply(C::say)),
                                    P.orElse(P.then("it's a plain A."))))
                            .collect(toList());
            //Verify
            assertThat(actual).containsExactly("it's a plain A.", "it's a B. value: bbb.", "C here. I've got ccc.");
        }
    }
}