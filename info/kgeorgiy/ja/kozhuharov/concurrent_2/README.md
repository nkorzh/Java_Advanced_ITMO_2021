## ParallelMapper

**Task**: 

1. Implement a class `ParallelMapperImpl`, implementing the `ParallelMapper` interface:

    ```
    public interface ParallelMapper extends AutoCloseable {
    
    <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException;

    @Override
    void close() throws InterruptedException;
    }
    ```

    - The `run` method must compute the function f on each of the specified arguments (`args`) in parallel.
    - The close method should stop all worker threads.
    - The `ParallelMapperImpl(int threads)` constructor creates threads of worker threads that can be used for parallelization.
    - A single `ParallelMapperImpl` can be accessed by multiple clients at the same time.
    - Tasks for execution must be accumulated in the queue and processed on a first-come, first-served basis.
    - There should be no active expectations in the implementation.
    
2. Modify the `IterativeParallelism` class so that it can use ParallelMapper.
    - Add the `IterativeParallelism(Parallel Mapper)` constructor.
    - The methods of the class should divide the work into threads of fragments and execute them using the Parallel Mapper.
    - If there is a `ParallelMapper`, `IterativeParallelism` instance should not create new threads.
    - It should be possible to simultaneously start and run multiple clients using the same `ParallelMapper`.