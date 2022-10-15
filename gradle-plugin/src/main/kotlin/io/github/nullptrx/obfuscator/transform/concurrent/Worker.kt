package io.github.nullptrx.obfuscator.transform.concurrent

import java.io.IOException
import java.lang.Error
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.function.Consumer

class Worker(
  val executor: ExecutorService
) {
  protected val futures = LinkedList<Future<*>>()


  fun execute(runnable: Runnable) {
    futures.add(executor.submit(runnable))
  }

  fun <T> submit(callable: Callable<T>): Future<T> {
    val future = executor.submit(callable)
    futures.add(future)
    return future
  }

  fun await() {
    do {
      val future = futures.pollFirst() ?: break
      try {
        future.get()
        // } catch (e: ExecutionException, InterruptedException ) {
      } catch (e: Exception) {
        if (e is IOException || e is RuntimeException || e.cause is Error) {
          throw  e
        }
        throw RuntimeException(e)
      }
    } while (true)
  }

  fun <I> submitAndAwait(input: Collection<I>, consumer: Consumer<I>) {
    input.stream().map {
      Runnable {
        consumer.accept(it)
      }
    }.forEach(this::execute)
    await()
  }
}
