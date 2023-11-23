package com.hsbc.statistic;

import static java.lang.String.format;

public interface ProbabilisticRandomGen {

    int nextFromSample();

    class NumAndProbability {
        private final int number;
        private final float probabilityOfSample;

        public NumAndProbability(int number, float probabilityOfSample) {
            this.number = number;
            this.probabilityOfSample = probabilityOfSample;
        }

        public int getNumber() {
            return number;
        }

        public float getProbabilityOfSample() {
            return probabilityOfSample;
        }

        @Override
        public String toString() {
            return format("{ %d -> %.3f }", number, probabilityOfSample);
        }
    }

    static NumAndProbability nbProb(int nb, float proba) {
        return new NumAndProbability(nb, proba);
    }
}