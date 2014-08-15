/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.testing.junit.rules.parameterized;

/*
 * (C) 2012 Jens Schauder http://blog.schauderhaft.de/
 * Code initially published here https://github.com/schauder/parameterizedTestsWithRules
 * Slightly simplified for wcm.io.
 */
import java.util.Iterator;

class SyncingIterable<T> implements Iterable<T> {

  private final Iterable<T> values;
  private final ValueContainer<T> valueContainer;

  public SyncingIterable(final Iterable<T> values,
      final ValueContainer<T> valueContainer) {
    this.values = values;
    this.valueContainer = valueContainer;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      private final Iterator<T> delegate = SyncingIterable.this.values.iterator();

      @Override
      public boolean hasNext() {
        return this.delegate.hasNext();
      }

      @Override
      public T next() {
        T next = this.delegate.next();
        SyncingIterable.this.valueContainer.set(next);
        return next;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException(
            "Can't remove from this iterator");
      }
    };
  }
}
