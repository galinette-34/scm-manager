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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.repository.BzrConfig;
import sonia.scm.repository.BzrRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.cgi.AbstractCGIServlet;
import sonia.scm.web.cgi.EnvList;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class BzrCGIServlet extends AbstractCGIServlet
{

  /** Field description */
  public static final String ENV_PYTHON_PATH = "SCM_PYTHON_PATH";

  /** Field description */
  public static final String ENV_REPOSITORY_NAME = "REPO_NAME";

  /** Field description */
  public static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";

  /** Field description */
  private static final long serialVersionUID = 7674689744455227632L;

  /** Field description */
  public static final Pattern PATTERN_REPOSITORYNAME =
    Pattern.compile("/[^/]+/([^/]+)(?:/.*)?");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   * @param handler
   */
  @Inject
  public BzrCGIServlet(RepositoryManager repositoryManager,
                       BzrRepositoryHandler handler)
  {
    this.repositoryManager = repositoryManager;
    this.handler = handler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException
  {
    command = BzrUtil.getCGI();
    super.init();
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param baseEnvironment
   *
   * @return
   *
   * @throws ServletException
   */
  @Override
  protected EnvList createRequestEnvironment(HttpServletRequest request,
          EnvList baseEnvironment)
          throws ServletException
  {
    EnvList list = new EnvList(baseEnvironment);
    Repository repository = getRepository(request);

    if (repository == null)
    {
      throw new ServletException("repository not found");
    }

    String name = repository.getName();
    File directory = handler.getDirectory(repository);

    list.set(ENV_REPOSITORY_PATH, directory.getAbsolutePath());
    list.set(ENV_REPOSITORY_NAME, name);

    String pythonPath = "";
    BzrConfig config = handler.getConfig();

    if (config != null)
    {
      pythonPath = config.getPythonPath();

      if (pythonPath == null)
      {
        pythonPath = "";
      }
    }

    list.set(ENV_PYTHON_PATH, pythonPath);

    return list;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @return
   *
   */
  @Override
  protected String getCmdPrefix()
  {
    BzrConfig config = handler.getConfig();

    AssertUtil.assertIsNotNull(config);

    return config.getPythonBinary();
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected File getCommand(HttpServletRequest request)
          throws ServletException, IOException
  {
    return command;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  private Repository getRepository(HttpServletRequest request)
  {
    Repository repository = null;
    String uri = request.getRequestURI();

    uri = uri.substring(request.getContextPath().length());

    Matcher m = PATTERN_REPOSITORYNAME.matcher(uri);

    if (m.matches())
    {
      String repositoryname = m.group(1);

      repository = getRepository(repositoryname);
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param repositoryname
   *
   * @return
   */
  private Repository getRepository(String repositoryname)
  {
    return repositoryManager.get(BzrRepositoryHandler.TYPE_NAME,
                                 repositoryname);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File command;

  /** Field description */
  private BzrRepositoryHandler handler;

  /** Field description */
  private RepositoryManager repositoryManager;
}
