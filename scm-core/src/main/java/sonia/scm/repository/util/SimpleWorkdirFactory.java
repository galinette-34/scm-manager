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

package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;

@Extension
public abstract class SimpleWorkdirFactory<R, W, C> implements WorkdirFactory<R, W, C>, ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleWorkdirFactory.class);

  private final CacheSupportingWorkdirProvider workdirProvider;

  public SimpleWorkdirFactory(CacheSupportingWorkdirProvider workdirProvider) {
    this.workdirProvider = workdirProvider;
  }

  @Override
  public WorkingCopy<R, W> createWorkingCopy(C context, String initialBranch) {
    try {
      CreateWorkdirContext<R, W, C> createWorkdirContext = new CreateWorkdirContext<>(
        getScmRepository(context),
        initialBranch,
        context,
        newFolder -> cloneRepository(context, newFolder, initialBranch),
        cachedFolder -> reclaimRepository(context, cachedFolder, initialBranch)
      );
      ParentAndClone<R, W> parentAndClone = workdirProvider.getWorkdir(createWorkdirContext);
      return new WorkingCopy<>(parentAndClone.getClone(), parentAndClone.getParent(), () -> this.close(createWorkdirContext, parentAndClone), parentAndClone.getDirectory());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new InternalRepositoryException(getScmRepository(context), "could not clone repository in temporary directory", e);
    }
  }

  private void close(CreateWorkdirContext<R, W, C> createWorkdirContext, ParentAndClone<R, W> parentAndClone) {
    try {
      closeRepository(parentAndClone.getParent());
    } catch (Exception e) {
      LOG.warn("could not close central repository for {}", createWorkdirContext.getScmRepository(), e);
    }
    try {
      closeWorkdir(parentAndClone.getClone());
    } catch (Exception e) {
      LOG.warn("could not close clone for {} in directory {}", createWorkdirContext.getScmRepository(), parentAndClone.getDirectory(), e);
    }
    try {
      workdirProvider.contextClosed(createWorkdirContext, parentAndClone.getDirectory());
    } catch (Exception e) {
      LOG.warn("could not close context for {} with directory {}", createWorkdirContext.getScmRepository(), parentAndClone.getDirectory(), e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    workdirProvider.shutdown();
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // nothing to do
  }

  @FunctionalInterface
  public interface WorkdirInitializer<R, W> {
    ParentAndClone<R, W> initialize(File target) throws IOException;
  }

  @FunctionalInterface
  public interface WorkdirReclaimer<R, W> {
    ParentAndClone<R, W> reclaim(File target) throws IOException, ReclaimFailedException;
  }

  protected abstract Repository getScmRepository(C context);

  @SuppressWarnings("squid:S00112")
  // We do allow implementations to throw arbitrary exceptions here, so that we can handle them in closeCentral
  protected abstract void closeRepository(R repository) throws Exception;
  @SuppressWarnings("squid:S00112")
  // We do allow implementations to throw arbitrary exceptions here, so that we can handle them in closeWorkdir
  protected abstract void closeWorkdirInternal(W workdir) throws Exception;

  protected abstract ParentAndClone<R, W> cloneRepository(C context, File target, String initialBranch) throws IOException;

  protected abstract ParentAndClone<R, W> reclaimRepository(C context, File target, String initialBranch) throws IOException, ReclaimFailedException;

  private void closeCentral(R repository) {
    try {
      closeRepository(repository);
    } catch (Exception e) {
      LOG.warn("could not close temporary repository clone", e);
    }
  }

  private void closeWorkdir(W repository) {
    try {
      closeWorkdirInternal(repository);
    } catch (Exception e) {
      LOG.warn("could not close temporary repository clone", e);
    }
  }

  protected static class ParentAndClone<R, W> {
    private final R parent;
    private final W clone;
    private final File directory;

    public ParentAndClone(R parent, W clone, File directory) {
      this.parent = parent;
      this.clone = clone;
      this.directory = directory;
    }

    public R getParent() {
      return parent;
    }

    public W getClone() {
      return clone;
    }

    public File getDirectory() {
      return directory;
    }
  }

  public static class ReclaimFailedException extends Exception {
    public ReclaimFailedException() {
    }

    public ReclaimFailedException(Throwable cause) {
      super(cause);
    }
  }
}
