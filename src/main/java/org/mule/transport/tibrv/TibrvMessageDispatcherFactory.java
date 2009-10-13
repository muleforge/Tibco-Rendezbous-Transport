// 	TibrvMessageDispatcherFactory.java

// 	Ross Paul, ross.paul@mlb.com, 22 Jun 2006
// 	Time-stamp: <2009-10-12 16:27:18 rpaul>
package org.mule.transport.tibrv;

import org.mule.api.MuleException;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.MessageDispatcher;
import org.mule.transport.AbstractMessageDispatcherFactory;

/**
 * Simply spins out a dispatcher
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revsion: $ 
 */
public class TibrvMessageDispatcherFactory 
    extends AbstractMessageDispatcherFactory
{
    
    public MessageDispatcher create( OutboundEndpoint endpoint ) 
        throws MuleException
    {
        return new TibrvMessageDispatcher( endpoint );
    }

}
