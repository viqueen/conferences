package org.viqueen.conf;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitQuickcheck.class)
public class RandomTopicTest {
    // TODO use reflection or something to ensure these are resolved correctly from java.util.Random
    private final static long multiplier = 0x5DEECE66DL;
    private final static long addend = 0xBL;
    private final static long mask = (1L << 48) - 1;

    private Random random;
    private RandomIntGenerator randomIntGenerator;

    @Property
    public void testRandomIntGenerator(long seed) {
        random = new Random(seed);
        randomIntGenerator = new RandomIntGenerator(seed);
        assertThat(random.nextInt()).isEqualTo(randomIntGenerator.next());
        assertThat(random.nextInt()).isEqualTo(randomIntGenerator.next());
    }

    @Property
    public void testResolveSeeds(long seed) {
        System.out.println("\n*******");
        random = new Random(seed);
        randomIntGenerator = new RandomIntGenerator(seed);

        int first = randomIntGenerator.next();
        int second = randomIntGenerator.next();

        long seedMask = RandomIntGenerator.MASK(randomIntGenerator.initialSeed);

        Collection<Long> seeds = RandomIntGenerator.resolveSeeds(first, second);

        seeds.forEach(s -> System.out.println(Long.toBinaryString(s)));

        System.out.println("---");
        System.out.println(Long.toBinaryString(seedMask));
        System.out.println(seeds.contains(seedMask));

        assertThat(seeds).isNotEmpty();
        assertThat(seeds).contains(seedMask);
    }

    static class RandomIntGenerator {
        private final AtomicLong seed;
        private final long initialSeed;

        RandomIntGenerator(final long seed) {
            this.initialSeed = (seed ^ multiplier) & mask;
            this.seed = new AtomicLong(initialSeed);
        }

        int next() {
            long oldSeed = seed.get();
            seed.set(MASK(oldSeed));
            return (int) SHIFT(MASK(oldSeed));
        }

        static long MASK(final long value) {
            return (value * multiplier + addend) & mask;
        }

        static long SHIFT(final long value) {
            return (value >>> 16);
        }

        // TODO : actually explain all of this with words
        static Collection<Long> resolveSeeds(final long first, final long second) {
            final Collection<Long> seeds = new LinkedHashSet<>();
            for (int index = 0; index < 65536; index++) {
                long tempSeed = first * 65536L + index;
                if ((int) SHIFT(MASK(tempSeed)) == second) {
                    seeds.add(tempSeed);
                    // TODO : explain this with words
                    seeds.add((tempSeed << 16) >>> 16);
                }
            }
            return seeds;
        }
    }

}
