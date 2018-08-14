package sonia.scm.api.v2.resources;

import com.github.sdorra.spotter.ContentType;
import com.github.sdorra.spotter.ContentTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PathNotFoundException;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ContentResource {

  private static final Logger LOG = LoggerFactory.getLogger(ContentResource.class);

  private final RepositoryServiceFactory servicefactory;

  @Inject
  public ContentResource(RepositoryServiceFactory servicefactory) {
    this.servicefactory = servicefactory;
  }

  @GET
  @Path("{revision}/{path: .*}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    try (RepositoryService repositoryService = servicefactory.create(new NamespaceAndName(namespace, name))) {
      try {

        StreamingOutput stream = os -> {
          try {
            repositoryService.getCatCommand().setRevision(revision).retriveContent(os, path);
          } catch (PathNotFoundException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
          } catch (RepositoryException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
          }
          os.close();
        };

        Response.ResponseBuilder responseBuilder = Response.ok(stream);
        appendContentType(path, getHead(revision, path, repositoryService), responseBuilder);

        return responseBuilder.build();
      } catch (PathNotFoundException e) {
        return Response.status(Status.NOT_FOUND).build();
      } catch (IOException e) {
        LOG.error("error reading repository resource {} from {}/{}", path, namespace, name, e);
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      } catch (RepositoryException e) {
        LOG.error("error reading repository resource {} from {}/{}", path, namespace, name, e);
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    } catch (RepositoryNotFoundException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @HEAD
  @Path("{revision}/{path: .*}")
  public Response metadata(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    try (RepositoryService repositoryService = servicefactory.create(new NamespaceAndName(namespace, name))) {
      try {

        Response.ResponseBuilder responseBuilder = Response.ok();

        appendContentType(path, getHead(revision, path, repositoryService), responseBuilder);
        return responseBuilder.build();
      } catch (PathNotFoundException e) {
        return Response.status(Status.NOT_FOUND).build();
      } catch (IOException e) {
        LOG.error("error reading repository resource {} from {}/{}", path, namespace, name, e);
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      } catch (RepositoryException e) {
        LOG.error("error reading repository resource {} from {}/{}", path, namespace, name, e);
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    } catch (RepositoryNotFoundException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private void appendContentType(String path, byte[] head, Response.ResponseBuilder responseBuilder) {
    ContentType contentType = ContentTypes.detect(path, head);
    responseBuilder.header("Content-Type", contentType.getRaw());
    contentType.getLanguage().ifPresent(language -> responseBuilder.header("Language", language));
    responseBuilder.header("Content-Length", head.length);
  }

  private byte[] getHead(String revision, String path, RepositoryService repositoryService) throws IOException, RepositoryException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    repositoryService.getCatCommand().setRevision(revision).retriveContent(outputStream, path);
    return outputStream.toByteArray();
  }
}
