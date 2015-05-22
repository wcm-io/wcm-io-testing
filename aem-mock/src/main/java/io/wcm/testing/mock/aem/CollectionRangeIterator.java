/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 - 2015 wcm.io
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
package io.wcm.testing.mock.aem;

import java.util.Collection;
import java.util.Iterator;

import com.day.cq.commons.RangeIterator;

/**
 * Implementation of {@link RangeIterator} through a Collection
 * @param <E> the type of elements returned by this iterator
 */
class CollectionRangeIterator<E> implements RangeIterator<E> {

  private final Collection<E> collection;
  private long index;
  private Iterator<E> iterator;

  public CollectionRangeIterator(Collection<E> collection) {
    this.collection = collection;
    index = 0;
    iterator = collection.iterator();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public E next() {
    ++index;
    return iterator.next();
  }

  @Override
  public void remove() {
    iterator.remove();
  }

  @Override
  public void skip(long skipNum) {
    long skip = Math.max(0, Math.min(skipNum, getSize()));

    // consume from the iterator
    while (skip-- > 0) {
      next();
    }
  }

  @Override
  public long getSize() {
    return collection.size() - index;
  }

  @Override
  public long getPosition() {
    return index;
  }
}
