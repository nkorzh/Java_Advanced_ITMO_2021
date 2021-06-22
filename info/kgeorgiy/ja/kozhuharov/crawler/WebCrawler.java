package info.kgeorgiy.ja.kozhuharov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private static final int DEFAULT_ARG_VALUE = 1;

    private final Downloader downloader;
    private final ExecutorService extractors;
    private final ExecutorService downloaders;
    private final HostDownloadsManager hostDownloadsManager;

    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        this.downloader = downloader;
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        hostDownloadsManager = new HostDownloadsManager(perHost);
    }

    @Override
    public Result download(final String url, final int depth) {
        return new BreadthDownloader(url, depth).getResult();
    }

    @Override
    public void close() {
        shutdownAndAwait(extractors);
        shutdownAndAwait(downloaders);
    }

    public static void shutdownAndAwait(final ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (final InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private class BreadthDownloader {
        private final Set<String> downloaded = ConcurrentHashMap.newKeySet();
        private final Map<String, IOException> failed = new ConcurrentHashMap<>();
        private final Set<String> extracted = ConcurrentHashMap.newKeySet();
        private final Queue<String> awaits = new ConcurrentLinkedQueue<>();

        public BreadthDownloader(final String url, int depth) {
            awaits.add(url);

            final Phaser phaser = new Phaser(1);
            for (; depth > 0; depth--) {
                List<String> processing = new ArrayList<>(awaits);
                awaits.clear();

                boolean visitChildren = depth > 1;
                processing.stream()
                        .filter(extracted::add)
                        .forEach(currentUrl -> processPage(currentUrl, visitChildren, phaser));

                phaser.arriveAndAwaitAdvance();
            }
        }

        private void scheduleExtraction(final Document page, final Phaser phaser) {
            phaser.register();
            extractors.submit(() -> {
                List<String> links = Collections.emptyList();
                try {
                    links = page.extractLinks();
                } catch (final IOException ignored) {
                } finally {
                    awaits.addAll(links);
                    phaser.arriveAndDeregister();
                }
            });
        }

        private void processPage(final String url, final boolean visitChildren, final Phaser phaser) {
            final String host;
            try {
                host = URLUtils.getHost(url);
            } catch (final MalformedURLException e) {
                failed.put(url, e);
                return;
            }
            phaser.register();
            hostDownloadsManager.addTask(
                    host,
                    () -> {
                        try {
                            final Document page = downloader.download(url);
                            downloaded.add(url);
                            if (visitChildren) {
                                scheduleExtraction(page, phaser);
                            }
                        } catch (final IOException e) {
                            failed.put(url, e);
                        } finally {
                            phaser.arriveAndDeregister();
                        }
                    }
            );
        }

        Result getResult() {
            return new Result(new ArrayList<>(downloaded), failed);
        }
    }

    private class HostDownloadsManager {
        private final int perHost;
        private final Map<String, Semaphore> semaphores;

        public HostDownloadsManager(final int hostUseLimit) {
            perHost = hostUseLimit;
            semaphores = new ConcurrentHashMap<>();
        }

        public void addTask(final String hostName, final Runnable task) {
            final Semaphore semaphore = semaphores.computeIfAbsent(hostName, name -> new Semaphore(perHost));
            downloaders.submit(() -> {
                try {
                    semaphore.acquire();
                    task.run();
                    semaphore.release();
                } catch (final InterruptedException ignored) {
                }
            });
        }
    }

    private static int[] initArgs(String[] args) {
        int[] updatedArgs = new int[4];
        for (int i = 1; i <= 4; ++i) {
            try {
                updatedArgs[i - 1] = (i >= args.length ? DEFAULT_ARG_VALUE : Integer.parseInt(args[i]));
            } catch (final NumberFormatException e) {
                System.err.println("Invalid integer value: " + args[i]);
                System.err.println("Default value used: " + DEFAULT_ARG_VALUE);
                updatedArgs[i - 1] = DEFAULT_ARG_VALUE;
            }
        }
        return updatedArgs;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Url not specified. " +
                    "Expected usage: WebCrawler url [depth [downloaders [extractors [perHost]]]]");
            return;
        }
        int[] crawlerArgs = initArgs(args);
        int depth = crawlerArgs[0];
        int downloaders = crawlerArgs[1];
        int extractors = crawlerArgs[2];
        int maxPerHost = crawlerArgs[3];

        try (final Crawler crawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, maxPerHost)) {
            crawler.download(args[0], depth);
        } catch (final IOException e) {
            System.err.println("Error creating Downloader: " + e.getMessage());
        }
    }
}
