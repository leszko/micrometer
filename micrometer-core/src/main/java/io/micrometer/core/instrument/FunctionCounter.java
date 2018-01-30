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
package io.micrometer.core.instrument;

import io.micrometer.core.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * A counter that tracks a monotonically increasing function.
 *
 * @author Jon Schneider
 */
public interface FunctionCounter extends Meter {
    static <T> Builder<T> builder(String name, T obj, ToDoubleFunction<T> f) {
        return new Builder<>(name, obj, f);
    }

    /**
     * The cumulative count since this counter was created.
     */
    double count();

    @Override
    default Iterable<Measurement> measure() {
        return Collections.singletonList(new Measurement(this::count, Statistic.COUNT));
    }

    @Override
    default Meter.Type type() {
        return Meter.Type.Counter;
    }

    /**
     * Fluent builder for function counters.
     *
     * @param <T> The type of the state object from which the counter value is extracted.
     */
    class Builder<T> {
        private final String name;
        private final T obj;
        private final ToDoubleFunction<T> f;
        private final List<Tag> tags = new ArrayList<>();
        @Nullable
        private String description;
        @Nullable
        private String baseUnit;

        private Builder(String name, T obj, ToDoubleFunction<T> f) {
            this.name = name;
            this.obj = obj;
            this.f = f;
        }

        /**
         * @param tags Must be an even number of arguments representing key/value pairs of tags.
         */
        public Builder<T> tags(String... tags) {
            return tags(Tags.of(tags));
        }

        /**
         * @param tags Tags to add to the eventual meter.
         * @return The function counter builder with added tags.
         */
        public Builder<T> tags(Iterable<Tag> tags) {
            tags.forEach(this.tags::add);
            return this;
        }

        /**
         * @param key   The tag key.
         * @param value The tag value.
         * @return The function counter builder with a single added tag.
         */
        public Builder<T> tag(String key, String value) {
            tags.add(Tag.of(key, value));
            return this;
        }

        /**
         * @param description Description text of the eventual function counter.
         * @return The function counter builder with added description.
         */
        public Builder<T> description(@Nullable String description) {
            this.description = description;
            return this;
        }

        /**
         * @param unit Base unit of the eventual counter.
         * @return The counter builder with added base unit.
         */
        public Builder<T> baseUnit(@Nullable String unit) {
            this.baseUnit = unit;
            return this;
        }

        /**
         * Add the function counter to a single registry, or return an existing function counter in that registry. The returned
         * function counter will be unique for each registry, but each registry is guaranteed to only create one function counter
         * for the same combination of name and tags.
         *
         * @param registry A registry to add the function counter to, if it doesn't already exist.
         * @return A new or existing function counter.
         */
        public FunctionCounter register(MeterRegistry registry) {
            return registry.more().counter(new Meter.Id(name, tags, baseUnit, description, Type.Counter), obj, f);
        }
    }
}
