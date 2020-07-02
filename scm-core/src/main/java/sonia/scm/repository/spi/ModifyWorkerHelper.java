/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.repository.spi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

/**
 * This "interface" is not really intended to be used as an interface but rather as
 * a base class to reduce code redundancy in Worker instances.
 */
public interface ModifyWorkerHelper extends ModifyCommand.Worker {

  Logger LOG = LoggerFactory.getLogger(ModifyWorkerHelper.class);

  @Override
  default void delete(String toBeDeleted) throws IOException {
    Path fileToBeDeleted = new File(getWorkDir(), toBeDeleted).toPath();
    try {
      Files.delete(fileToBeDeleted);
    } catch (NoSuchFileException e) {
      throw notFound(createFileContext(toBeDeleted));
    }
    doScmDelete(toBeDeleted);
  }

  void doScmDelete(String toBeDeleted);

  @Override
  default void create(String toBeCreated, File file, boolean overwrite) throws IOException {
    Path targetFile = new File(getWorkDir(), toBeCreated).toPath();
    createDirectories(targetFile);
    if (overwrite) {
      Files.move(file.toPath(), targetFile, REPLACE_EXISTING);
    } else {
      try {
        Files.move(file.toPath(), targetFile);
      } catch (FileAlreadyExistsException e) {
        throw alreadyExists(createFileContext(toBeCreated));
      }
    }
    addFileToScm(toBeCreated, targetFile);
  }

  default void modify(String path, File file) throws IOException {
    Path targetFile = new File(getWorkDir(), path).toPath();
    createDirectories(targetFile);
    if (!targetFile.toFile().exists()) {
      throw notFound(createFileContext(path));
    }
    boolean executable = Files.isExecutable(targetFile);
    Files.move(file.toPath(), targetFile, REPLACE_EXISTING);
    if (targetFile.toFile().setExecutable(executable)) {
      LOG.warn("could not set executable flag for file {}", targetFile);
    }
    addFileToScm(path, targetFile);
  }

  void addFileToScm(String name, Path file);

  default ContextEntry.ContextBuilder createFileContext(String path) {
    ContextEntry.ContextBuilder contextBuilder = entity("file", path);
    if (!StringUtils.isEmpty(getBranch())) {
      contextBuilder.in("branch", getBranch());
    }
    contextBuilder.in(getRepository());
    return contextBuilder;
  }

  default void createDirectories(Path targetFile) throws IOException {
    try {
      Files.createDirectories(targetFile.getParent());
    } catch (FileAlreadyExistsException e) {
      throw alreadyExists(createFileContext(targetFile.toString()));
    }
  }

  File getWorkDir();

  Repository getRepository();

  String getBranch();
}