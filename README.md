[![Build Status](https://travis-ci.com/lambig/Patterns-static-import-free
.svg?branch=main)](https://travis-ci.com/lambig/Patterns-static-import-free
)
# Patterns-static-import-free
easy implementation of pattern-matcher works as a function

```Java
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
```

```Java
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
```
