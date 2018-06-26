package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces(VndMediaType.USER)
public class UserResource {

  private final UserDtoToUserMapper dtoToUserMapper;
  private final UserToUserDtoMapper userToDtoMapper;

  private final ResourceManagerAdapter<User, UserDto, UserException> adapter;

  @Inject
  public UserResource(UserDtoToUserMapper dtoToUserMapper, UserToUserDtoMapper userToDtoMapper, UserManager manager) {
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
    this.adapter = new ResourceManagerAdapter<>(manager);
  }

  /**
   * Returns a user.
   *
   * <strong>Note:</strong> This method requires "user" privileges.
   *
   * @param request the current request
   * @param id the id/name of the user
   *
   */
  @GET
  @Path("")
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no privileges to read the user"),
    @ResponseCode(code = 404, condition = "not found, no user with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@Context Request request, @Context UriInfo uriInfo, @PathParam("id") String id) {
    return adapter.get(id, userToDtoMapper::map);
  }

  /**
   * Modifies the given user.
   *
   * <strong>Note:</strong> This method requires "user" privileges.
   *
   * @param name name of the user to be modified
   * @param userDto user object to modify
   */
  @PUT
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user does not have the \"user\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(@Context UriInfo uriInfo, @PathParam("id") String name, UserDto userDto) {
    return adapter.update(name, existing -> dtoToUserMapper.map(userDto, existing.getPassword()));
  }

  /**
   * Deletes a user.
   *
   * <strong>Note:</strong> This method requires "user" privileges.
   *
   * @param name the name of the user to delete.
   *
   */
  @DELETE
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user does not have the \"user\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response delete(@PathParam("id") String name) {
    return adapter.delete(name);
  }
}