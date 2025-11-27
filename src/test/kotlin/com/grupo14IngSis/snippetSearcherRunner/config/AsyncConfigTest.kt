package com.grupo14IngSis.snippetSearcherRunner.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AsyncConfigTest {
    private val asyncConfig = AsyncConfig()

    @Test
    fun `taskExecutor should be configured with correct core pool size`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor

        assertEquals(2, executor.corePoolSize)
    }

    @Test
    fun `taskExecutor should be configured with correct max pool size`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor

        assertEquals(5, executor.maxPoolSize)
    }

    @Test
    fun `taskExecutor should be configured with correct queue capacity`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor

        assertEquals(100, executor.queueCapacity)
    }

    @Test
    fun `taskExecutor should have correct thread name prefix`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val latch = CountDownLatch(1)
        var threadName = ""

        executor.execute {
            threadName = Thread.currentThread().name
            latch.countDown()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(threadName.startsWith("formatting-async-"))
    }

    @Test
    fun `taskExecutor should execute tasks asynchronously`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val counter = AtomicInteger(0)
        val latch = CountDownLatch(3)

        repeat(3) {
            executor.execute {
                counter.incrementAndGet()
                latch.countDown()
            }
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(3, counter.get())
    }

    @Test
    fun `taskExecutor should handle multiple concurrent tasks`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val numberOfTasks = 10
        val latch = CountDownLatch(numberOfTasks)
        val results = mutableListOf<Int>()

        repeat(numberOfTasks) { i ->
            executor.execute {
                synchronized(results) {
                    results.add(i)
                }
                latch.countDown()
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        assertEquals(numberOfTasks, results.size)
        assertEquals((0 until numberOfTasks).toSet(), results.toSet())
    }

    @Test
    fun `taskExecutor should queue tasks when pool is full`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val numberOfTasks = 20
        val latch = CountDownLatch(numberOfTasks)
        val executedTasks = AtomicInteger(0)

        repeat(numberOfTasks) {
            executor.execute {
                Thread.sleep(50) // Simular trabajo
                executedTasks.incrementAndGet()
                latch.countDown()
            }
        }

        assertTrue(latch.await(15, TimeUnit.SECONDS))
        assertEquals(numberOfTasks, executedTasks.get())
    }

    @Test
    fun `taskExecutor should be initialized and active`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor

        assertTrue(executor.threadPoolExecutor.isShutdown.not())
        assertTrue(executor.threadPoolExecutor.isTerminated.not())
    }

    @Test
    fun `taskExecutor should wait for tasks to complete on shutdown`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val latch = CountDownLatch(1)
        var taskCompleted = false

        executor.execute {
            Thread.sleep(100)
            taskCompleted = true
            latch.countDown()
        }

        executor.shutdown()
        executor.threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS)

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(taskCompleted)
    }

    @Test
    fun `taskExecutor should handle task execution order with queue`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val executionOrder = mutableListOf<Int>()
        val latch = CountDownLatch(5)

        // Llenar el pool con tareas largas
        repeat(5) { i ->
            executor.execute {
                Thread.sleep(100)
                synchronized(executionOrder) {
                    executionOrder.add(i)
                }
                latch.countDown()
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        assertEquals(5, executionOrder.size)
    }

    @Test
    fun `taskExecutor should handle exceptions in tasks`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val successLatch = CountDownLatch(1)
        var successfulTaskExecuted = false

        // Tarea que lanza excepción
        executor.execute {
            throw RuntimeException("Test exception")
        }

        // Tarea exitosa después de la excepción
        executor.execute {
            successfulTaskExecuted = true
            successLatch.countDown()
        }

        assertTrue(successLatch.await(5, TimeUnit.SECONDS))
        assertTrue(successfulTaskExecuted)
    }

    @Test
    fun `taskExecutor should scale up to max pool size under load`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val numberOfTasks = 10
        val latch = CountDownLatch(numberOfTasks)
        val threadNames = mutableSetOf<String>()

        repeat(numberOfTasks) {
            executor.execute {
                synchronized(threadNames) {
                    threadNames.add(Thread.currentThread().name)
                }
                Thread.sleep(100)
                latch.countDown()
            }
        }

        assertTrue(latch.await(15, TimeUnit.SECONDS))
        // Verificar que se usaron múltiples threads
        assertTrue(threadNames.size > 1, "Should use multiple threads under load")
        assertTrue(threadNames.size <= 5, "Should not exceed max pool size")
    }

    @Test
    fun `taskExecutor should return ThreadPoolTaskExecutor instance`() {
        val executor = asyncConfig.taskExecutor()

        assertNotNull(executor)
        assertTrue(executor is ThreadPoolTaskExecutor)
    }

    @Test
    fun `taskExecutor should handle rapid task submission`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val numberOfTasks = 50
        val latch = CountDownLatch(numberOfTasks)
        val counter = AtomicInteger(0)

        repeat(numberOfTasks) {
            executor.execute {
                counter.incrementAndGet()
                latch.countDown()
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        assertEquals(numberOfTasks, counter.get())
    }

    @Test
    fun `taskExecutor should maintain thread pool efficiency`() {
        val executor = asyncConfig.taskExecutor() as ThreadPoolTaskExecutor
        val initialPoolSize = executor.poolSize

        // Ejecutar algunas tareas
        val latch = CountDownLatch(3)
        repeat(3) {
            executor.execute {
                Thread.sleep(50)
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)

        // El pool size debería estar dentro de los límites configurados
        assertTrue(executor.poolSize >= executor.corePoolSize)
        assertTrue(executor.poolSize <= executor.maxPoolSize)
    }
}
