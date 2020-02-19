/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.repository.BrowserResult;

import java.util.function.Consumer;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class BrowseCommandRequest extends FileBaseCommandRequest
{

  public static final int DEFAULT_REQUEST_LIMIT = 100;

  private static final long serialVersionUID = 7956624623516803183L;
  private int offset;

  public BrowseCommandRequest() {
    this(null);
  }

  public BrowseCommandRequest(Consumer<BrowserResult> updater) {
    this.updater = updater;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public BrowseCommandRequest clone()
  {
    BrowseCommandRequest clone = null;

    try
    {
      clone = (BrowseCommandRequest) super.clone();
    }
    catch (CloneNotSupportedException e)
    {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError("CatCommandRequest seems not to be cloneable");
    }

    return clone;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final BrowseCommandRequest other = (BrowseCommandRequest) obj;

    return super.equals(obj) && Objects.equal(recursive, other.recursive)
      && Objects.equal(disableLastCommit, other.disableLastCommit)
      && Objects.equal(disableSubRepositoryDetection,
        other.disableSubRepositoryDetection);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(super.hashCode(), recursive, disableLastCommit,
      disableSubRepositoryDetection);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("path", getPath())
                  .add("revision", getRevision())
                  .add("recursive", recursive)
                  .add("disableLastCommit", disableLastCommit)
                  .add("disableSubRepositoryDetection", disableSubRepositoryDetection)
                  .toString();
    //J+
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * True to disable the last commit.
   *
   *
   * @param disableLastCommit true to disable the last commit
   *
   * @since 1.26
   */
  public void setDisableLastCommit(boolean disableLastCommit)
  {
    this.disableLastCommit = disableLastCommit;
  }

  /**
   * Enable or Disable sub repository detection. Default is enabled.
   *
   *
   * @param disableSubRepositoryDetection true to disable sub repository detection
   *
   * @since 1.26
   */
  public void setDisableSubRepositoryDetection(
    boolean disableSubRepositoryDetection)
  {
    this.disableSubRepositoryDetection = disableSubRepositoryDetection;
  }

  /**
   * True to enable recursive file object browsing.
   *
   *
   * @param recursive true to enable recursive browsing
   *
   * @since 1.26
   */
  public void setRecursive(boolean recursive)
  {
    this.recursive = recursive;
  }

  /**
   * Limit the number of result files to <code>limit</code> entries.
   *
   * @param limit The maximal number of files this request shall return.
   *
   * @since 2.0.0
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * Proceed the list from the given number on (zero based).
   *
   * @param offset The number of the entry, the result should start with (zero based).
   *               All preceding entries will be omitted.
   * @since 2.0.0
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns true if the last commit is disabled.
   *
   *
   * @return true if the last commit is disabled
   *
   * @since 1.26
   */
  public boolean isDisableLastCommit()
  {
    return disableLastCommit;
  }

  /**
   * Returns true if the detection of sub repositories is disabled.
   *
   *
   * @return true if sub repository detection is disabled.
   *
   * @since 1.26
   */
  public boolean isDisableSubRepositoryDetection()
  {
    return disableSubRepositoryDetection;
  }

  /**
   * Returns true if recursive file object browsing is enabled.
   *
   *
   * @return true recursive is enabled
   *
   * @since 1.26
   */
  public boolean isRecursive()
  {
    return recursive;
  }

  /**
   * Returns the limit for the number of result files.
   *
   * @since 2.0.0
   */
  public int getLimit() {
    return limit;
  }

  /**
   * The number of the entry, the result start with. All preceding entries will be omitted.
   *
   * @since 2.0.0
   */
  public int getOffset() {
    return offset;
  }

  public void updateCache(BrowserResult update) {
    if (updater != null) {
      updater.accept(update);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** disable last commit */
  private boolean disableLastCommit = false;

  /** disable detection of sub repositories */
  private boolean disableSubRepositoryDetection = false;

  /** browse file objects recursive */
  private boolean recursive = false;


  /** Limit the number of result files to <code>limit</code> entries. */
  private int limit = DEFAULT_REQUEST_LIMIT;

  // WARNING / TODO: This field creates a reverse channel from the implementation to the API. This will break
  // whenever the API runs in a different process than the SPI (for example to run explicit hosts for git repositories).
  private final transient Consumer<BrowserResult> updater;
}
