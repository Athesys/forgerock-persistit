/**
 * Copyright © 2011-2012 Akiban Technologies, Inc.  All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, version 3 (only) of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * This program may also be available under different license terms. For more
 * information, see www.akiban.com or contact licensing@akiban.com.
 */

package com.persistit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.persistit.Exchange.Sequence;
import com.persistit.exception.PersistitException;
import com.persistit.policy.SplitPolicy;
import com.persistit.unit.PersistitUnitTestCase;

public class InsertSequenceTest extends PersistitUnitTestCase {

    Sequence sequence;
    int index;

    private class TestPolicy extends SplitPolicy {

        @Override
        public int splitFit(Buffer buffer, int kbOffset, int insertAt, boolean replace, int leftSize, int rightSize,
                int currentSize, int virtualSize, int capacity, int splitInfo, Sequence sequence) {
            if (buffer.isDataPage()) {
                InsertSequenceTest.this.sequence = sequence;
            }
            return SplitPolicy.PACK_BIAS.splitFit(buffer, kbOffset, insertAt, replace, leftSize, rightSize,
                    currentSize, virtualSize, capacity, splitInfo, sequence);
        }

    }

    @Test
    public void testPacked() throws PersistitException {
        // final Exchange exchange = _persistit.getExchange("persistit",
        // "InsertSequenceTest", true);
        // exchange.removeAll();
        // exchange.setSplitPolicy(new TestPolicy());
        // for (index = 1; index < 100000; index++) {
        // exchange.clear().append(index);
        // exchange.getValue().put("Record #" + index);
        // exchange.store();
        // if (sequence != null) {
        // assertEquals(Sequence.FORWARD, sequence);
        // }
        // sequence = null;
        // }
        // // -1 for the volume head page which isn't on the LRU queue.
        // int bufferCount = exchange.getVolume().getPool().getBufferCount() -
        // 1;
        // assertEquals(bufferCount,exchange.getVolume().getPool().countLruQueueEntries());
        // for (index = 10000; index < 100000; index += 10000) {
        // Buffer buffer = exchange.clear().append(index).fetchBufferCopy(0);
        // assertTrue(buffer.getAvailableSize() < buffer.getBufferSize() * .9);
        // }
        // assertEquals(bufferCount,exchange.getVolume().getPool().countLruQueueEntries());
    }

    @Test
    public void testForwardSequence() throws PersistitException {
        final Exchange exchange = _persistit.getExchange("persistit", "InsertSequenceTest", true);
        exchange.removeAll();
        exchange.setSplitPolicy(new TestPolicy());

        for (index = 1; index < 100000; index++) {
            String key = "a" + index;
            boolean expectForward = index % 10 != 0;
            exchange.clear().append(key);
            exchange.getValue().put("Record #" + index);
            exchange.store();
            if (sequence != null) {
                assertEquals(expectForward ? Sequence.FORWARD : Sequence.NONE, sequence);
            }
            sequence = null;
        }
    }

    @Test
    public void testReverseSequence() throws PersistitException {
        final Exchange exchange = _persistit.getExchange("persistit", "InsertSequenceTest", true);
        exchange.removeAll();
        exchange.setSplitPolicy(new TestPolicy());

        for (index = 1000000; index >= 0; index--) {
            String key = "a" + index;
            boolean expectReverse = index > 99999;
            exchange.clear().append(key);
            exchange.getValue().put("Record #" + index);
            exchange.store();
            if (sequence != null) {
                assertEquals(expectReverse ? Sequence.REVERSE : Sequence.NONE, sequence);
            }
            sequence = null;
        }
    }

    @Override
    public void runAllTests() throws Exception {
        testForwardSequence();
        testReverseSequence();
    }
}