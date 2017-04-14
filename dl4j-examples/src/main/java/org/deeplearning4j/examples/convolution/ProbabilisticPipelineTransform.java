package org.deeplearning4j.examples.convolution;

import org.bytedeco.javacpp.opencv_core;
import org.datavec.api.berkeley.Pair;
import org.datavec.image.data.ImageWritable;
import org.datavec.image.transform.BaseImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Allows creation of image transform pipelines, either sequentially or randomly.
 *
 * Pipelines are defined as a list of tuples, where each transform is assigned
 * a probability between 0.0-1.0 of being invoked. Pipeline can also be randomly
 * shuffled with each transform, further increasing the available dataset.
 *
 * @author Justin Long (@crockpotveggies)
 */
public class ProbabilisticPipelineTransform extends BaseImageTransform<opencv_core.Mat> {

    protected List<Pair<ImageTransform, Double>> transforms;
    protected boolean shuffle;
    protected org.nd4j.linalg.api.rng.Random rng;

    public ProbabilisticPipelineTransform(List<Pair<ImageTransform, Double>> transforms) {
        this(1234, transforms, false);
    }

    public ProbabilisticPipelineTransform(List<Pair<ImageTransform, Double>> transforms, boolean shuffle) {
        this(1234, transforms, shuffle);
    }

    public ProbabilisticPipelineTransform(long seed, List<Pair<ImageTransform, Double>> transforms) {
        this(seed, transforms, false);
    }

    public ProbabilisticPipelineTransform(long seed, List<Pair<ImageTransform, Double>> transforms, boolean shuffle) {
        this(null, seed, transforms, shuffle);
    }

    public ProbabilisticPipelineTransform(Random random, long seed, List<Pair<ImageTransform, Double>> transforms, boolean shuffle) {
        super(null); // for perf reasons we ignore java Random, create our own
        this.transforms = transforms;
        this.shuffle = shuffle;
        this.rng = Nd4j.getRandom();
        rng.setSeed(seed);
    }

    /**
     * Takes an image and executes a pipeline of combined transforms.
     *
     * @param image  to transform, null == end of stream
     * @param random object to use (or null for deterministic)
     * @return transformed image
     */
    @Override
    public ImageWritable transform(ImageWritable image, Random random) {
        if(shuffle) Collections.shuffle(transforms);

        // execute each item in the pipeline
        for(Pair<ImageTransform, Double> tuple : transforms) {
            if(rng.nextDouble() < tuple.getSecond()) { // probability of execution
                image = tuple.getFirst().transform(image);
            }
        }

        return image;
    }
}
