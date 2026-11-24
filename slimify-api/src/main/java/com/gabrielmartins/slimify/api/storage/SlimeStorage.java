package com.gabrielmartins.slimify.api.storage;

import java.nio.file.Path;
import java.util.List;

import com.gabrielmartins.slimify.api.exception.InvalidSlimeNameException;
import com.gabrielmartins.slimify.api.exception.StorageException;

public interface SlimeStorage {

    Path directory();

    List<SlimeFile> list() throws StorageException;

    boolean exists(String name) throws InvalidSlimeNameException;

    Path locate(String name) throws InvalidSlimeNameException;

    boolean delete(String name) throws InvalidSlimeNameException, StorageException;
}
