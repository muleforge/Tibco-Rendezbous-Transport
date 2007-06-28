// 	TibrvMessageDispatcher.java

// 	Ross Paul, ross.paul@mlb.com, 22 Jun 2006
// 	Time-stamp: <2007-06-27 10:15:51 rpaul>
package org.mule.providers.tibrv;

import com.tibco.tibrv.*;
import org.mule.providers.VarSubDispatcher;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.*;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import java.util.*;

/**
 * Used for sending out transformed umo output over rendezvous.  By convention,
 * the output will be keyed with TibrvConnector.CONTENT_FIELD (content).  See
 * TibrvConnector for info on how to use certified messaging, 
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @author Ross Paul
 * @version $Revision: 1.9 $
 */
public class TibrvMessageDispatcher extends VarSubDispatcher
{    
    static Map cmTransportMap = new Hashtable();
    TibrvConnector connector;

    public TibrvMessageDispatcher( UMOImmutableEndpoint endpoint )
    {
        super( endpoint );
        this.connector = (TibrvConnector)endpoint.getConnector();
    }

    /** grabs the transformed message and sends it on its way */
    public void doDispatch( UMOEvent event ) throws Exception
    {
        try
        {
            //what we're sending and where to
            Object message = event.getTransformedMessage();
            String subject = event.getEndpoint().getEndpointURI().getAddress();
            String cmname = event.getEndpoint().getEndpointURI().getParams()
                .getProperty( "cmname" );

            subject = substituteVars( subject );
            
            TibrvMsg msg = null;
            if( !( message instanceof TibrvMsg ))
            {
                logger.warn( "Message isn't a TibrvMessage.  Converting it" );
                msg = new TibrvMsg();
                msg.add( TibrvConnector.CONTENT_FIELD, message );
            }
            else
            {
                msg = (TibrvMsg)message;
            }

            msg.setSendSubject( subject );

            if( logger.isDebugEnabled() )
            {
                logger.debug( "Sending to: " + subject + " - " + message );
            }


            if( cmname == null )
                ((TibrvConnector)connector).transport.send( msg );
            else
            {
                cmname = substituteVars( cmname );

                //fist send we'll need to set up the cmTransport
                TibrvCmTransport cmTransport = null;
                synchronized( cmTransportMap )
                {
                    cmTransport = (TibrvCmTransport)
                        cmTransportMap.get( cmname );
                    if( cmTransport == null )
                    {
                        logger.debug("first message, initializing cmTransport");
                        cmTransport = new TibrvCmTransport
                            ( (TibrvRvdTransport)((TibrvConnector)connector)
                              .transport, cmname, true );
                        cmTransportMap.put( cmname, cmTransport );
                    }
                }

                logger.debug( "sending certified, over: " + cmname );
                cmTransport.send( msg );
            }
        }catch( Exception e ){  getConnector().handleException( e ); }
    }


    public UMOMessage doSend( UMOEvent event ) throws Exception
    {
        doDispatch( event );
        return event.getMessage();
    }



    public Object getDelegateSession() throws UMOException
    {
        return null;
    }



    public UMOMessage doReceive( long timeout ) 
        throws Exception
    {
        throw new UnsupportedOperationException
            ("Receive not implemented until I figure out how it is used" );
    }

    /** Clear TibrvCmTransport */
    public void doDispose()
    {
        synchronized( cmTransportMap )
        {
            for( Iterator i = cmTransportMap.values().iterator(); i.hasNext(); )
                ((TibrvCmTransport)i.next()).destroy();
        }
    }

    protected void doConnect() throws Exception {
        // no op
    }

    protected void doDisconnect() throws Exception {
        // no op
    }

}
