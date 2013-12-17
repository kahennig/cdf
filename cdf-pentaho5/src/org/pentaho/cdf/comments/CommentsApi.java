/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pentaho.cdf.comments;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.pentaho.cdf.util.Parameter;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;

import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.PluginIOUtils;

/**
 * 
 * @author diogomariano
 */
@Path( "/pentaho-cdf/api/comments" )
public class CommentsApi {

  private static final Log logger = LogFactory.getLog( CommentsApi.class );

  @GET
  @Path( "/add" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  public void add( @DefaultValue( "" ) @QueryParam( Parameter.PAGE ) String page,
      @DefaultValue( "" ) @QueryParam( Parameter.COMMENT ) String comment,
      @Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest ) {

    JSONObject json;
    String result = "";

    try {
      json = CommentsEngine.getInstance().add( page, comment, getUserName() );
      result = json.toString( 2 );
    } catch ( Exception e ) {
      logger.error( "Error processing comment: " + e );
    }

    try {
      PluginIOUtils.writeOut( servletResponse.getOutputStream(), result );
    } catch ( IOException ex ) {
      logger.error( "Error while outputing result", ex );
    }
  }

  @GET
  @Path( "/list" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  public void list( @DefaultValue( "" ) @QueryParam( Parameter.PAGE ) String page,
      @DefaultValue( "0" ) @QueryParam( Parameter.FIRST_RESULT ) int firstResult,
      @DefaultValue( "20" ) @QueryParam( Parameter.MAX_RESULTS ) int maxResults,
      @DefaultValue( "false" ) @QueryParam( Parameter.DELETED ) boolean deleted,
      @DefaultValue( "false" ) @QueryParam( Parameter.MAX_RESULTS ) boolean archived,

      @Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest ) {

    JSONObject json;
    String result = "";

    boolean isAdministrator = SecurityHelper.isPentahoAdministrator( PentahoSessionHolder.getSession() );

    if ( deleted && !isAdministrator ) {
      deleted = false;
      logger.warn( "only admin users are allowed to see deleted comments" );
    }

    if ( archived && !isAdministrator ) {
      archived = false;
      logger.warn( "only admin users are allowed to see archived comments" );
    }

    try {
      json = CommentsEngine.getInstance().list( page, firstResult, maxResults, deleted, archived, getUserName() );
      result = json.toString( 2 );
    } catch ( Exception e ) {
      logger.error( "Error processing comment: " + e );
    }

    try {
      PluginIOUtils.writeOut( servletResponse.getOutputStream(), result );
    } catch ( IOException ex ) {
      logger.error( "Error while outputing result", ex );
    }
  }

  @GET
  @Path( "/archive" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  public void archive( @DefaultValue( "0" ) @QueryParam( Parameter.COMMENT_ID ) int commentId,
      @DefaultValue( "true" ) @QueryParam( Parameter.VALUE ) boolean value,
      @Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest ) {

    JSONObject json;
    String result = "";

    boolean isAdministrator = SecurityHelper.isPentahoAdministrator( PentahoSessionHolder.getSession() );
    boolean isAuthenticated = PentahoSessionHolder.getSession().isAuthenticated();

    if ( !isAdministrator || !isAuthenticated ) {
      String msg = "Operation not authorized: requires administrator priviledges";
      logger.error( msg );
      try {
        IOUtils.write( msg, servletResponse.getOutputStream(), CharsetHelper.getEncoding() );
      } catch ( IOException ex ) {
        logger.error( "Error while outputing result", ex );
      }
      return;
    }

    try {
      json = CommentsEngine.getInstance().archive( commentId, value, getUserName() );
      result = json.toString( 2 );
    } catch ( Exception e ) {
      logger.error( "Error processing comment: " + e );
    }

    try {
      PluginIOUtils.writeOut( servletResponse.getOutputStream(), result );
    } catch ( IOException ex ) {
      logger.error( "Error while outputing result", ex );
    }
  }

  @GET
  @Path( "/delete" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  public void delete( @DefaultValue( "0" ) @QueryParam( "commentId" ) int commentId,
      @DefaultValue( "true" ) @QueryParam( Parameter.VALUE ) boolean value,
      @Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest ) {

    JSONObject json;
    String result = "";

    boolean isAdministrator = SecurityHelper.isPentahoAdministrator( PentahoSessionHolder.getSession() );
    boolean isAuthenticated = PentahoSessionHolder.getSession().isAuthenticated();

    if ( !isAdministrator || !isAuthenticated ) {
      String msg = "Operation not authorized: requires administrator priviledges";
      logger.error( msg );
      try {
        PluginIOUtils.writeOut( servletResponse.getOutputStream(), msg );
      } catch ( IOException ex ) {
        logger.error( "Error while outputing result", ex );
      }
      return;
    }

    try {
      json = CommentsEngine.getInstance().delete( commentId, value, getUserName() );
      result = json.toString( 2 );
    } catch ( Exception ex ) {
      logger.error( "Error processing comment: " + ex );
    }

    try {
      PluginIOUtils.writeOut( servletResponse.getOutputStream(), result );
    } catch ( IOException ex ) {
      logger.error( "Error while outputing result", ex );
    }
  }

  private String getUserName() {
    return PentahoSessionHolder.getSession().getName();
  }
}
