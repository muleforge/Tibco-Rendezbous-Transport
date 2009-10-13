// 	TibrvMessageDispatcher.java

// 	Ross Paul, rossapaul@gmail.com, 22 Jun 2006
// 	Time-stamp: <2009-10-12 17:43:35 rpaul>
package org.mule.transport.tibrv;

import com.tibco.tibrv.*;
import java.util.*;

import org.mule.transport.AbstractMessageDispatcher;
import org.mule.api.*;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.expression.*;


/**
 * Used for sending out transformed umo output over rendezvous.  By convention,
 * non map outputs will be keyed with TibrvConnector.CONTENT_FIELD (content).  
 * See TibrvConnector for info on how to use certified messaging, 
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @author Ross Paul
 * @version $Revision: 1.9 $
 */
public class TibrvMessageDispatcher extends AbstractMessageDispatcher
{    
    static Map cmTransportMap = new Hashtable();
    TibrvConnector connector;

    public TibrvMessageDispatcher( OutboundEndpoint endpoint )
    {
        super( endpoint );
        this.connector = (TibrvConnector)endpoint.getConnector();
    }


    /** grabs the transformed message and sends it on its way */
    protected void doDispatch( MuleEvent event ) throws Exception
    {
        ExpressionManager expressionManager = 
            event.getMuleContext().getExpressionManager();
        //what we're sending and where to
        Object message = event.transformMessage();
        String subject = event.getEndpoint().getEndpointURI().getAddress();
        String cmname = event.getEndpoint().getEndpointURI().getParams()
            .getProperty( "cmname" );
        
        subject = expressionManager.parse( subject, event.getMessage() );
        
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
            cmname = expressionManager.parse( cmname, event.getMessage() );
            
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
    }


    protected MuleMessage doSend( MuleEvent event ) throws Exception
    {
        doDispatch( event );
        return event.getMessage();
    }



    /** Clear TibrvCmTransport */
    protected void doDispose()
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
