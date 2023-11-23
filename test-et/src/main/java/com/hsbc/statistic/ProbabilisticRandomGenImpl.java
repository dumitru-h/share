package com.hsbc.statistic;

import lombok.NonNull;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * this class is not required to be thread-safe
 */
public class ProbabilisticRandomGenImpl implements ProbabilisticRandomGen {

	// some tolerance on input validation
	private final float EPSILON = 0.001F;

	public SortedMap<Integer, Float> getTargetProbabilities() {
		return new TreeMap<>(targetProbabilities);
	}

	private final SortedMap<Integer, Float> targetProbabilities;

	// Map<number, times it was generated>
	private final SortedMap<Integer, Integer> generated = new TreeMap<>();

	/**
	 * The choice of Constructor and final field is for simplicity and immutability
	 * of the generator params
	 * @param params
	 */
	public ProbabilisticRandomGenImpl(@NonNull List<NumAndProbability> params) {
		validateParams(params);

		this.targetProbabilities = new TreeMap<>(
				params.stream().collect(
						toMap(NumAndProbability::getNumber, NumAndProbability::getProbabilityOfSample))
		);
		params.forEach(np -> generated.put(np.getNumber(), 0));	// init generated
	}

	/*
	check that sum of probabilities is 1 or very close to 1
	 */
	private void validateParams(List<NumAndProbability> params) {
		float sum = params.stream().map(np -> np.getProbabilityOfSample())
				.reduce((p1, p2) -> p1 + p2)
				.orElse(0F);

		if (Math.abs(1F - sum) > EPSILON)
			throw new IllegalArgumentException("Sum of probabilities for sample should be close to 1 +/- " + EPSILON);
	}

	private int sampleSize() {
		return generated.values().stream().reduce((v1, v2) -> v1+v2).get();
	}

	/**
	 * current sample computed probabilities
	 */
	public SortedMap<Integer, Float> sampleProbabilities() {
		SortedMap<Integer, Float> sampleProbs = new TreeMap<>();
		generated.forEach((nb, count) -> sampleProbs.put(nb, (count==0) ? 0F : count*1F/sampleSize()));

		return sampleProbs;
	}

	private int generateNext() {
		Comparator<Map.Entry<Integer, Float>> distanceFromTarget =
				(entry1, entry2) -> Float.compare(
						targetProbabilities.get(entry1.getKey())-entry1.getValue(),
						targetProbabilities.get(entry2.getKey())-entry2.getValue()
						);

		int next =
			sampleProbabilities().entrySet().stream()
					.max(distanceFromTarget)
					.get().getKey();

		int nbOfOccurences = generated.getOrDefault(next, 0);
		generated.put(next, nbOfOccurences+1);

		return next;
	}



	@Override
	public int nextFromSample() {
		return generateNext();
	}
}
