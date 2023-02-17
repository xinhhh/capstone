package com.cmclinnovations.stack.clients.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public abstract class TempPath implements AutoCloseable {

    private final Path path;

    protected Path changePermissions(Path path) {
        try {
            Set<PosixFilePermission> perms = Files.readAttributes(path, PosixFileAttributes.class).permissions();

            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            return Files.setPosixFilePermissions(path, perms);
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Failed to change the permissions of the file/directory '" + path + "' to '777'.",
                    ex);
        }
    }

    protected TempPath(Path path) {

        changePermissions(path);

        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public abstract void close() throws RuntimeException;

}
