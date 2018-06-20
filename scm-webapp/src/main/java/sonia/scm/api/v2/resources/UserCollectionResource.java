package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.PageResult;
import sonia.scm.api.rest.resources.AbstractManagerResource;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import static sonia.scm.api.v2.resources.ResourceLinks.user;

@Produces(VndMediaType.USER_COLLECTION)
public class UserCollectionResource extends AbstractManagerResource<User, UserException> {
  public static final int DEFAULT_PAGE_SIZE = 10;
  private final UserDtoToUserMapper dtoToUserMapper;
  private final UserToUserDtoMapper userToDtoMapper;
  private final UserCollectionToDtoMapper userCollectionToDtoMapper;

  @Inject
  public UserCollectionResource(UserManager manager, UserDtoToUserMapper dtoToUserMapper, UserToUserDtoMapper userToDtoMapper, UserCollectionToDtoMapper userCollectionToDtoMapper) {
    super(manager);
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
    this.userCollectionToDtoMapper = userCollectionToDtoMapper;
  }

  /**
   * Returns all users for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   * <strong>Note:</strong> This method requires admin privileges.
   *
   * @param request  the current request
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortby   sort parameter
   * @param desc     sort direction desc or aesc
   * @return
   */
  @GET
  @Path("")
  @TypeHint(UserDto[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@Context Request request,
    @DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
    @QueryParam("sortby") String sortby,
    @DefaultValue("false")
    @QueryParam("desc") boolean desc) {
    PageResult<User> pageResult = fetchPage(sortby, desc, page, pageSize);

    return Response.ok(userCollectionToDtoMapper.map(page, pageSize, pageResult)).build();
  }

  /**
   * Creates a new user.
   * @param userDto The user to be created.
   * @return A response with the link to the new user (if created successfully).
   */
  @POST
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 201, condition = "create success", additionalHeaders = {
      @ResponseHeader(name = "Location", description = "uri to the created group")
    }),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response create(@Context UriInfo uriInfo, UserDto userDto) throws IOException, UserException {
    if (userDto == null) {
      return Response.status(400).build();
    }
    User user = dtoToUserMapper.map(userDto, "");
    manager.create(user);
    return Response.created(URI.create(user(uriInfo).self(user.getName()))).build();
  }

  @Override
  protected GenericEntity<Collection<User>> createGenericEntity(Collection<User> items) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getId(User item) {
    return item.getName();
  }

  @Override
  protected String getPathPart() {
    throw new UnsupportedOperationException();
  }
}