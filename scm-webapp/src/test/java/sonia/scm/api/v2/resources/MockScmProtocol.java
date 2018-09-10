package sonia.scm.api.v2.resources;

import sonia.scm.repository.api.ScmProtocol;

import java.net.URI;

class MockScmProtocol implements ScmProtocol {
  private final String type;
  private final String protocol;

  public MockScmProtocol(String type, String protocol) {
    this.type = type;
    this.protocol = protocol;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getUrl(URI baseUri) {
    return protocol;
  }
}
