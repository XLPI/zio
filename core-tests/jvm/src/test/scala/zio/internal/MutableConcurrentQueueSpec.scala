package zio.internal

import zio.SerializableSpecHelpers._
import zio.ZIOBaseSpec
import zio.test._
import zio.test.Assertion._

/*
 * This spec is just a sanity check and tests RingBuffer correctness
 * in a single-threaded case.
 *
 * Concurrent tests are run via jcstress and are in [[RingBufferConcurrencyTests]].
 */
object MutableConcurrentQueueSpec2
    extends ZIOBaseSpec(
      suite("MutableConcurrentQueueSpec")(
        suite("Make a bounded MutableConcurrentQueue")(
          test("of capacity 1 return a queue of capacity 1") {
            val q = MutableConcurrentQueue.bounded(1)

            assert(q.capacity, equalTo(1))
          },
          test("of capacity 2 return a queue of capacity 2") {
            val q = MutableConcurrentQueue.bounded(2)

            assert(q.capacity, equalTo(2))
          },
          test("of capacity 3 return a queue of capacity 3") {
            val q = MutableConcurrentQueue.bounded(3)

            assert(q.capacity, equalTo(3))
          }
        ),
        suite("With a RingBuffer of capacity 2")(
          test("`offer` of 2 items succeeds, further offers fail") {
            val q = MutableConcurrentQueue.bounded[Int](2)

            (assert(q.offer(1), isTrue)
            && assert(q.size(), equalTo(1))
            && assert(q.offer(2), isTrue)
            && assert(q.size(), equalTo(2))
            && assert(q.offer(3), isFalse)
            && assert(q.isFull(), isTrue))
          },
          test(
            "`poll` of 2 items from full queue succeeds, further `poll`s return default value"
          ) {
            val q = MutableConcurrentQueue.bounded[Int](2)
            q.offer(1)
            q.offer(2)

            (assert(q.poll(-1), equalTo(1))
            && assert(q.poll(-1), equalTo(2))
            && assert(q.poll(-1), equalTo(-1))
            && assert(q.isEmpty(), isTrue))
          }
        ),
        suite("Serialization works for")(
          test("a one element queue") {
            val q = MutableConcurrentQueue.bounded[Int](1)
            q.offer(1)
            val returnQ = serializeAndDeserialize(q)
            returnQ.offer(2)

            (assert(returnQ.poll(-1), equalTo(1))
            && assert(returnQ.poll(-1), equalTo(-1)))
          },
          test("a pow 2 capacity ring buffer") {
            val q = MutableConcurrentQueue.bounded[Int](3)
            q.offer(1)
            val returnQ = serializeAndDeserialize(q)
            returnQ.offer(2)

            (assert(returnQ.poll(-1), equalTo(1))
            && assert(returnQ.poll(-1), equalTo(2))
            && assert(returnQ.poll(-1), equalTo(-1)))
          },
          test("an arbitrary capacity ring buffer") {
            val q = MutableConcurrentQueue.bounded[Int](2)
            q.offer(1)
            val returnQ = serializeAndDeserialize(q)
            returnQ.offer(2)

            (assert(returnQ.poll(-1), equalTo(1))
            && assert(returnQ.poll(-1), equalTo(2))
            && assert(returnQ.poll(-1), equalTo(-1)))
          }
        )
      )
    )
