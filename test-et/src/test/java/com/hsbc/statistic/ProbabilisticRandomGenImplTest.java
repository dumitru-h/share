package com.hsbc.statistic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.hsbc.statistic.ProbabilisticRandomGen.nbProb;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class ProbabilisticRandomGenImplTest {

	final ProbabilisticRandomGen.NumAndProbability[] generatorParams = {
			nbProb(1, .25F),
			nbProb(2, .23F),
			nbProb(3, .17F),
			nbProb(4, .25F),
			nbProb(5, .1F)
	};

	private ProbabilisticRandomGenImpl generator() {
		return new ProbabilisticRandomGenImpl(asList(generatorParams));
	}

	@Test
	void sampleProbabilities() {
		ProbabilisticRandomGenImpl generator = generator();

		boolean targetReached = false;
		for (int i = 0; i < 2000 && !targetReached; i++) {
			log.info("next: {}", generator.nextFromSample());

			targetReached = generator.getTargetProbabilities().equals(generator.sampleProbabilities());
			if (targetReached) {
				log.info("target probabilities reached first time after {} generations", i);
			}
		}

		assertTrue(targetReached);

		log.info("sample probabilities: {}", generator.sampleProbabilities());
		log.info("target probabilities; {}", asList(generatorParams));
	}

	@Test
	void nextFromSample() {
		ProbabilisticRandomGenImpl generator = generator();

		assertEquals(1, generator.nextFromSample());
		assertEquals(4, generator.nextFromSample());
		assertEquals(2, generator.nextFromSample());
		assertEquals(3, generator.nextFromSample());
	}
}