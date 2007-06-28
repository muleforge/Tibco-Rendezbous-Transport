// 	TibrvMessageDispatcherFactory.java

// 	Ross Paul, ross.paul@mlb.com, 22 Jun 2006
// 	Time-stamp: <2007-06-26 17:02:09 rpaul>
package org.mule.providers.tibrv;

import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.providers.AbstractMessageDispatcherFactory;

/**
 * Simply spins out a dispatcher
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revsion: $ 
 */
public class TibrvMessageDispatcherFactory 
    extends AbstractMessageDispatcherFactory
{
    
    public UMOMessageDispatcher create( UMOImmutableEndpoint endpoint ) 
        throws UMOException
    {
        return new TibrvMessageDispatcher( endpoint );
    }

}
