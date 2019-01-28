package org.viqueen.conf;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class RandomScratchPad {
    // TODO use reflection or something to ensure these are resolved correctly from java.util.Random
    private final static long multiplier = 0x5DEECE66DL;
    private final static long addend = 0xBL;
    private final static long mask = (1L << 48) - 1;

    private final Random random;
    private final RandomIntGenerator randomIntGenerator;

    public RandomScratchPad(long seed) {
        this.random = new Random(seed);
        this.randomIntGenerator = new RandomIntGenerator(seed);
    }

    @Parameterized.Parameters
    public static Collection<Long> seeds() {
        return singletonList(
                2L
        );
    }

    @Ignore
    @Test
    public void testRandomIntGenerator() {
        assertThat(random.nextInt(), is(randomIntGenerator.next()));
    }

    static class RandomIntGenerator {
        private final AtomicLong seed;

        RandomIntGenerator(final long seed) {
            this.seed = new AtomicLong((seed ^ multiplier) & mask);
        }

        int next() {
            long oldSeed = seed.get();
            long nextSeed = MASK(oldSeed);
            seed.set(nextSeed);
            return SHIFT(nextSeed);
        }

        static long MASK(final long value) {
            return (value * multiplier + addend) & mask;
        }

        static int SHIFT(final long value) {
            return (int)(value >>> 16);
        }
    }

}
