// 	TibrvMessageAdapter.java

// 	Ross Paul, ross.paul@mlb.com,  5 Jul 2006
// 	Time-stamp: <2009-10-12 16:25:12 rpaul>
package org.mule.transport.tibrv;

import com.tibco.tibrv.*;

import org.mule.api.MessagingException;
import org.mule.api.ThreadSafeAccess;
import org.mule.api.config.MuleProperties;
import org.mule.api.transport.MessageTypeNotSupportedException;
import org.mule.api.transport.PropertyScope;
import org.mule.transport.AbstractMessageAdapter;

/**
 * Gives a uniform view of incomming tibrv messages.  The Adapter expects a 
 * TibrvMsg.
 *
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revision: 3 $
 */
public class TibrvMessageAdapter extends AbstractMessageAdapter
{
    private TibrvMsg message = null;
    
    /**
     * @throws MessageTypeNotSupportedException if message is not a TibrvMsg 
     */
    public TibrvMessageAdapter( Object message ) throws MessagingException
    {
        if( message instanceof TibrvMsg )
            this.message = (TibrvMsg) message;
        else
            throw new MessageTypeNotSupportedException( message, getClass() );
    }

    /**
     * @return the current message
     */
    public Object getPayload()
    {
        return message;
    }
}
