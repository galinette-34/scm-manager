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



package sonia.scm.server;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmServerDaemon implements Daemon
{

  /** Field description */
  private static volatile ScmServer webserver = new ScmServer();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   */
  public static void main(String[] args)
  {
    webserver.run();
  }

  /**
   * Method description
   *
   *
   * @param args
   *
   * @throws Exception
   */
  public static void start(String[] args) throws Exception
  {
    webserver.start();
  }

  /**
   * Method description
   *
   *
   * @param args
   *
   * @throws Exception
   */
  public static void stop(String[] args) throws Exception
  {
    webserver.stopServer();
    webserver.join(2000l);
  }

  /**
   * Method description
   *
   */
  @Override
  public void destroy()
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param context
   *
   * @throws DaemonInitException
   * @throws Exception
   */
  @Override
  public void init(DaemonContext context) throws DaemonInitException, Exception
  {
    daemonArgs = context.getArguments();
    // initialize web server and open port. We have to do this in the init 
    // method, because this method is started by jsvc with super user privileges.
    webserver.init();
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  public void start() throws Exception
  {
    start(daemonArgs);
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  public void stop() throws Exception
  {
    stop(daemonArgs);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String[] daemonArgs;
}
