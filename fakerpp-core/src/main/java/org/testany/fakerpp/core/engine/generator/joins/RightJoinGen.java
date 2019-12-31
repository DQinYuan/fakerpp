package org.testany.fakerpp.core.engine.generator.joins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.domain.ColExec;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RightJoinGen implements Generator {


    public static class Builder {

        private List<Dimension> fixedDimension = new ArrayList<>();
        private List<ColExec> fixedJoinColExec = new ArrayList<>();

        private List<Dimension> randomDimension = new ArrayList<>();
        private List<ColExec> randomJoinColExec = new ArrayList<>();

        public void appendDimension(JoinDepend joinDepend, boolean random) {
            if (random) {
                randomJoinColExec.addAll(joinDepend.getJoinColExecs());
                randomDimension.add(new Dimension(joinDepend.getDependColExecs()));
            } else {
                fixedJoinColExec.addAll(joinDepend.getJoinColExecs());
                fixedDimension.add(new Dimension(joinDepend.getDependColExecs()));
            }
        }

        public List<ColExec> colOrder() {
            // random dimension behind
            List<ColExec> res = new ArrayList<>(randomJoinColExec);
            res.addAll(fixedJoinColExec);
            return res;
        }

        public RightJoinGen build() {
            return new RightJoinGen(fixedDimension, randomDimension);
        }

    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * non random dimension
     */
    private final List<Dimension> fixedDimension;

    /**
     * random dimension
     */
    private final List<Dimension> randomDimension;

    // rowlist of random dimension
    private List<RowList> randomDimenRows;

    // cartesian product of fixedDimension
    // dimension row -> col
    private Iterator<List<List<String>>> fixedIter;
    private List<List<String>> currentFixed;

    // cartesian product of randomDimension
    private Iterator<List<List<String>>> randomIter;

    // <start, end>
    private Iterator<List<StartEnd>> randomPlanIter;

    private boolean initDataFlag;
    private int dataNum = 0;

    @Override
    public void init() throws ERMLException {
    }

    /**
     * initData before nextData and dataNum
     */
    private void initData() {
        //dimension row list
        this.randomDimenRows = getRowLists(randomDimension);
        List<RowList> fixedColsByRow =
                getRowLists(fixedDimension);

        // cartesian product
        List<List<List<String>>> fixedDimenRows = Lists.cartesianProduct(fixedColsByRow);
        this.fixedIter =
                fixedDimenRows.iterator();
        if (fixedIter.hasNext()) {
            currentFixed = fixedIter.next();
        }

        // do random plan first to get dataNum
        ArrayList<List<StartEnd>> plans = new ArrayList<>();
        for (int i = 0; i < fixedDimenRows.size(); i++) {
            int counter = 1;
            List<StartEnd> startEnds = new ArrayList<>();
            for (RowList dimenRows : randomDimenRows) {
                int randomStart = SeedableThreadLocalRandom.nextInt(dimenRows.size());
                int randomEnd = getRandomEnd(randomStart, dimenRows.size());
                counter *= (randomEnd - randomStart);
                startEnds.add(new StartEnd(randomStart, randomEnd));
            }
            plans.add(startEnds);
            dataNum += counter;
        }
        randomPlanIter = plans.iterator();

        resetRandomIter();
    }

    @Override
    public List<String> nextData() {
        if (!initDataFlag) {
            initData();
            initDataFlag = true;
        }
        if (randomIter.hasNext()) {
            List<List<String>> randomRow = randomIter.next();
            // generate random dimension first, then generate fixed dimension
            return flatten(ImmutableList.<List<String>>builder()
                    .addAll(randomRow)
                    .addAll(currentFixed)
                    .build());
        } else if (fixedIter.hasNext()) {
            currentFixed = fixedIter.next();
            resetRandomIter();
            return this.nextData();
        }

        return null;
    }

    @Override
    public long dataNum() {
        if (!initDataFlag) {
            initData();
            initDataFlag = true;
        }
        return dataNum;
    }

    private void resetRandomIter() {
        randomIter = cartesianRandomProduct().iterator();
    }

    private List<List<List<String>>> cartesianRandomProduct() {
        if (randomPlanIter.hasNext()) {
            List<List<List<String>>> randomPart =
                    Streams.zip(randomDimenRows.stream(), randomPlanIter.next().stream(),
                            (dimenRow, plan) ->
                                    dimenRow.subList(plan.start, plan.end))
                            .collect(Collectors.toList());

            return Lists.cartesianProduct(randomPart);
        } else {
            return null;
        }
    }

    private List<String> flatten(List<List<String>> folded) {
        return folded.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<ColExec> colOrder() {
        List<ColExec> res = new ArrayList<>();
        randomDimension.forEach(
                d -> res.addAll(d.getColExecs())
        );
        fixedDimension.forEach(
                d -> res.addAll(d.getColExecs())
        );
        return res;
    }


    private int getRandomEnd(int start, int bound) {
        if (start + 1 >= bound) {
            return bound;
        }

        return SeedableThreadLocalRandom
                .nextInt(start + 1, bound);
    }

    private List<RowList> getRowLists(List<Dimension> dimensions) {
        return dimensions.stream()
                .map(Dimension::toRowList)
                .collect(Collectors.toList());
    }


    private static class Dimension {

        private final List<ColExec> cols;

        public Dimension(List<ColExec> cols) {
            this.cols = cols;
        }

        public List<ColExec> getColExecs() {
            return cols;
        }

        public RowList toRowList() {
            return new RowList(cols);
        }

    }

    @RequiredArgsConstructor
    private static class StartEnd {
        private final int start;
        private final int end;
    }
}
