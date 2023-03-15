package io.github.lambig.patterns;

import io.github.lambig.tuplite._2.Tuple2;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static io.github.lambig.tuplite._2.Tuple2._2mapWith;

/**
 * 疑似パターンマッチ定義クラス
 * パターンは定義順に評価します。
 *
 * @param <K> キー型
 * @param <V> 処理結果の戻り型
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Patterns<K, V> implements Function<K, V> {

    private final WhenValueIsNull<V> whenValueIsNull;
    private final List<Tuple2<Predicate<K>, Function<K, V>>> map;


    @SafeVarargs
    public static <S, O> Patterns<S, O> of(Tuple2<Predicate<S>, Function<S, O>>... patterns) {
        return of(WhenValueIsNull.throwRuntimeException("nullの場合の処理が定義されていません。"), patterns);
    }

    @SafeVarargs
    public static <S, O> Patterns<S, O> of(WhenValueIsNull<O> whenValueIsNull, Tuple2<Predicate<S>, Function<S, O>>... patterns) {
        return new Patterns<>(
                whenValueIsNull,
                Stream.of(patterns)
                        .collect(
                                Collector.<
                                        Tuple2<Predicate<S>, Function<S, O>>,
                                        List<Tuple2<Predicate<S>, Function<S, O>>>,
                                        List<Tuple2<Predicate<S>, Function<S, O>>>>of(
                                        ArrayList::new,
                                        List::add,
                                        (a, b) -> {
                                            a.addAll(b);
                                            return a;
                                        },
                                        Collections::unmodifiableList)));
    }

    public static <S, O> Patterns<S, O> of(List<Tuple2<Predicate<S>, Function<S, O>>> patterns) {
        return new Patterns<>(WhenValueIsNull.throwRuntimeException("nullの場合の処理が定義されていません。"), Collections.unmodifiableList(patterns));
    }

    public static <S, O> Patterns<S, O> of(WhenValueIsNull<O> whenValueIsNull, List<Tuple2<Predicate<S>, Function<S, O>>> patterns) {
        return new Patterns<>(whenValueIsNull, Collections.unmodifiableList(patterns));
    }

    /**
     * キーが最初に該当するパターンの返却値を取得します
     *
     * @param key キー
     * @return 該当するパターンがあればその返却値、なければnull
     */
    public V get(K key) {
        return this.map.stream()
                .filter(entry -> entry._1().test(key))
                .findFirst()
                .map(_2mapWith((ignored, then) -> then.apply(key)))
                .orElseGet(whenValueIsNull::get);
    }

    @Override
    public V apply(K k) {
        return this.get(k);
    }

}

