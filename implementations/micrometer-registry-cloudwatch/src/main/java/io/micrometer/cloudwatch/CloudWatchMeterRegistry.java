/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.cloudwatch;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.model.*;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.util.TimeUtils;
import io.micrometer.core.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Dawid Kublik
 */
public class CloudWatchMeterRegistry extends StepMeterRegistry {
    private final CloudWatchConfig config;
    private final AmazonCloudWatchAsync amazonCloudWatchAsync;
    private final Logger logger = LoggerFactory.getLogger(CloudWatchMeterRegistry.class);

    public CloudWatchMeterRegistry(CloudWatchConfig config, Clock clock,
                                   AmazonCloudWatchAsync amazonCloudWatchAsync) {
        this(config, clock, amazonCloudWatchAsync, Executors.defaultThreadFactory());
    }

    public CloudWatchMeterRegistry(CloudWatchConfig config, Clock clock,
                                   AmazonCloudWatchAsync amazonCloudWatchAsync, ThreadFactory threadFactory) {
        super(config, clock);
        requireNonNull(config.namespace());

        this.amazonCloudWatchAsync = amazonCloudWatchAsync;
        this.config = config;
        config().namingConvention(NamingConvention.identity);
        start(threadFactory);
    }

    @Override
    public void start(ThreadFactory threadFactory) {
        if (config.enabled()) {
            logger.info("publishing metrics to cloudwatch every " + TimeUtils.format(config.step()));
        }
        super.start(threadFactory);
    }

    @Override
    protected void publish() {
        for (List<MetricDatum> batch : MetricDatumPartition.partition(metricData(), config.batchSize())) {
            sendMetricData(batch);
        }
    }

    private void sendMetricData(List<MetricDatum> metricData) {
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest()
                .withNamespace(config.namespace())
                .withMetricData(metricData);
        amazonCloudWatchAsync.putMetricDataAsync(putMetricDataRequest, new AsyncHandler<PutMetricDataRequest, PutMetricDataResult>() {
            @Override
            public void onError(Exception exception) {
                logger.error("Error sending metric data.", exception);
            }

            @Override
            public void onSuccess(PutMetricDataRequest request, PutMetricDataResult result) {
                logger.debug("published metric with namespace:{}", request.getNamespace());
            }
        });
    }

    private List<MetricDatum> metricData() {
        return getMeters().stream().flatMap(m -> m.apply(
                this::metricData,
                this::metricData,
                this::timerData,
                this::summaryData,
                this::metricData,
                this::metricData,
                this::metricData,
                this::functionTimerData,
                this::metricData)
        ).collect(toList());
    }

    private Stream<MetricDatum> functionTimerData(FunctionTimer timer) {
        long wallTime = clock.wallTime();

        // we can't know anything about max and percentiles originating from a function timer
        return Stream.of(
                metricDatum(timer.getId(), "count", wallTime, timer.count()),
                metricDatum(timer.getId(), "avg", wallTime, timer.mean(getBaseTimeUnit())));
    }

    private Stream<MetricDatum> timerData(Timer timer) {
        final long wallTime = clock.wallTime();
        final Stream.Builder<MetricDatum> metrics = Stream.builder();

        metrics.add(metricDatum(timer.getId(), "sum", getBaseTimeUnit().name(), wallTime, timer.totalTime(getBaseTimeUnit())));
        metrics.add(metricDatum(timer.getId(), "count", "count", wallTime, timer.count()));
        metrics.add(metricDatum(timer.getId(), "avg", getBaseTimeUnit().name(), wallTime, timer.mean(getBaseTimeUnit())));
        metrics.add(metricDatum(timer.getId(), "max", getBaseTimeUnit().name(), wallTime, timer.max(getBaseTimeUnit())));

        return metrics.build();
    }

    private Stream<MetricDatum> summaryData(DistributionSummary summary) {
        final long wallTime = clock.wallTime();
        final Stream.Builder<MetricDatum> metrics = Stream.builder();

        metrics.add(metricDatum(summary.getId(), "sum", wallTime, summary.totalAmount()));
        metrics.add(metricDatum(summary.getId(), "count", wallTime, summary.count()));
        metrics.add(metricDatum(summary.getId(), "avg", wallTime, summary.mean()));
        metrics.add(metricDatum(summary.getId(), "max", wallTime, summary.max()));

        return metrics.build();
    }

    // VisibleForTesting
    Stream<MetricDatum> metricData(Meter m) {
        long wallTime = clock.wallTime();
        return stream(m.measure().spliterator(), false)
                .map(ms -> metricDatum(m.getId().withTag(ms.getStatistic()), wallTime, ms.getValue()))
                .filter(Objects::nonNull);
    }

    @Nullable
    private MetricDatum metricDatum(Meter.Id id, long wallTime, double value) {
        return metricDatum(id, null, null, wallTime, value);
    }

    @Nullable
    private MetricDatum metricDatum(Meter.Id id, @Nullable String suffix, long wallTime, double value) {
        return metricDatum(id, suffix, null, wallTime, value);
    }

    @Nullable
    private MetricDatum metricDatum(Meter.Id id, @Nullable String suffix, @Nullable String unit, long wallTime, double value) {
        if (Double.isNaN(value)) {
            return null;
        }

        String metricName = config().namingConvention().name(id.getName() + "." + suffix, id.getType(), id.getBaseUnit());
        List<Tag> tags = id.getConventionTags(config().namingConvention());
        return new MetricDatum()
                .withMetricName(metricName)
                .withDimensions(toDimensions(tags))
                .withTimestamp(new Date(wallTime))
                .withValue(CloudWatchUtils.clampMetricValue(value))
                .withUnit(toStandardUnit(unit));
    }

    private StandardUnit toStandardUnit(@Nullable String unit) {
        if (unit == null) {
            return StandardUnit.None;
        }
        switch (unit.toLowerCase()) {
            case "bytes":
                return StandardUnit.Bytes;
            case "milliseconds":
                return StandardUnit.Milliseconds;
            case "count":
                return StandardUnit.Count;
        }
        return StandardUnit.None;
    }


    private List<Dimension> toDimensions(List<Tag> tags) {
        return tags.stream()
                .map(tag -> new Dimension().withName(tag.getKey()).withValue(tag.getValue()))
                .collect(toList());
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }
}
