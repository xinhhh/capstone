package uk.ac.cam.cares.jps.aws;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateFileWatcher extends Thread implements Watcher {
    private final File file;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private WatcherCallback callback;

    public CreateFileWatcher(File file) {
        this.file = file;
    }

    public boolean isStopped() {
        return stop.get();
    }

    public void stopThread() {
        stop.set(true);
    }

    public void doOnChange() {
        if (callback != null) {
            callback.call();
        }
        stopThread();
    }

    public void setCallback(WatcherCallback cb) {
        callback = cb;
    }

    @Override
    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path path = file.toPath().getParent();
            path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

            while (!isStopped()) {
                WatchKey key;
                try {
                    key = watcher.poll(25, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    return;
                }
                if (key == null) {
                    Thread.yield();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        Thread.yield();
                        continue;
                    } else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
                            && filename.toString().equals(file.getName())) {
                        doOnChange();
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
                Thread.yield();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}