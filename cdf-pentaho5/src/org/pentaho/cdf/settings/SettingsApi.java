/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pentaho.cdf.settings;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.cdf.util.Parameter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import pt.webdetails.cpf.utils.PluginIOUtils;

/**
 * 
 * @author diogomariano
 */
@Path( "/pentaho-cdf/api/settings" )
public class SettingsApi {

  IPentahoSession userSession;

  private static final Log logger = LogFactory.getLog( SettingsApi.class );

  public SettingsApi() {
  }

  @GET
  @Path( "/set" )
  public void set( @QueryParam( Parameter.KEY ) String key, @QueryParam( Parameter.VALUE ) String value ) {

    if ( StringUtils.isEmpty( key ) || StringUtils.isEmpty( value ) ) {
      logger.equals( "empty values not allowed -> key:" + key + " | value:" + value );
      return;
    }

    SettingsEngine.getInstance().setValue( key, value, PentahoSessionHolder.getSession() );

  }

  @GET
  @Path( "/get" )
  public void get( @QueryParam( Parameter.KEY ) String key, @Context HttpServletResponse servletResponse ) {

    if ( StringUtils.isEmpty( key ) ) {
      logger.equals( "empty key value not allowed" );
      return;
    }

    final Object value = SettingsEngine.getInstance().getValue( key, PentahoSessionHolder.getSession() );

    try {
      PluginIOUtils.writeOut( servletResponse.getOutputStream(), value.toString() );
    } catch ( Exception e ) {
      logger.error( e );
    }
  }
}
