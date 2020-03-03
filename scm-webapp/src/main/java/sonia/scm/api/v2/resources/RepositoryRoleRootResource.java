package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

/**
 *  RESTful web service resource to manage repository roles.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Repository role", description = "Repository role related endpoints")
})
@Path(RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2)
public class RepositoryRoleRootResource {

  static final String REPOSITORY_ROLES_PATH_V2 = "v2/repositoryRoles/";

  private final Provider<RepositoryRoleCollectionResource> repositoryRoleCollectionResource;
  private final Provider<RepositoryRoleResource> repositoryRoleResource;

  @Inject
  public RepositoryRoleRootResource(Provider<RepositoryRoleCollectionResource> repositoryRoleCollectionResource,
                                    Provider<RepositoryRoleResource> repositoryRoleResource) {
    this.repositoryRoleCollectionResource = repositoryRoleCollectionResource;
    this.repositoryRoleResource = repositoryRoleResource;
  }

  @Path("")
  public RepositoryRoleCollectionResource getRepositoryRoleCollectionResource() {
    return repositoryRoleCollectionResource.get();
  }

  @Path("{name}")
  public RepositoryRoleResource getRepositoryRoleResource() {
    return repositoryRoleResource.get();
  }
}
