package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
@Setter
public class FilterContext<T> {

    private boolean finished;

    private Set<Integer> memberIds;

    Supplier<Stream<T>> streamSupplier;

    Supplier<Stream<FactMemberUnion>> memberUnionSupplier;


}
