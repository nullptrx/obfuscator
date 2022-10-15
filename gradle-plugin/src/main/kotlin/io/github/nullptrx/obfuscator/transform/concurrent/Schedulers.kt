package io.github.nullptrx.obfuscator.transform.concurrent

import java.util.concurrent.*

object Schedulers {
  private val cpuCount = Runtime.getRuntime().availableProcessors()
  val IO_POOL = ThreadPoolExecutor(
    0, cpuCount * 3,
    30L, TimeUnit.SECONDS,
    LinkedBlockingQueue()
  )

  val COMPUTATION_POOL = Executors.newWorkStealingPool(cpuCount)

  val IO: Worker
    get() {
      return Worker(IO_POOL)
    }

  val COMPUTATION: Worker
    get() {
      return Worker(COMPUTATION_POOL)
    }

  val FORKJOINPOOL: ForkJoinPool
    get() {
      return COMPUTATION_POOL as ForkJoinPool
    }
}
