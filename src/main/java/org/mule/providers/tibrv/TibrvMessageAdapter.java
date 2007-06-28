// 	TibrvMessageAdapter.java

// 	Ross Paul, ross.paul@mlb.com,  5 Jul 2006
// 	Time-stamp: <2007-06-26 16:57:53 rpaul>
package org.mule.providers.tibrv;

import org.mule.umo.MessagingException;
import org.mule.umo.provider.*;
import org.mule.providers.AbstractMessageAdapter;
import com.tibco.tibrv.*;

/**
 * Gives a uniform view of incomming tibrv messages.  The Adapter expects a 
 * TibrvMsg.
 *
 * TODO find some good way of getting at the contents more easily
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
     * Converts the message implementation into a String representation
     * 
     * @param encoding
     *            The encoding to use when transforming the message (if
     *            necessary). The parameter is used when converting from a byte
     *            array
     * @return String representation of the message payload
     * @throws Exception
     *             Implementation may throw an endpoint specific exception
     */
    public String getPayloadAsString(String encoding) throws Exception
    {
        synchronized (this)
        {
            return new String(this.getPayloadAsBytes(), encoding);
        }
    }


    /**
     * whole message as bytes
     */
    public byte[] getPayloadAsBytes() throws Exception
    {
        return message.getAsBytes();
    }

    /**
     * @return the current message
     */
    public Object getPayload()
    {
        return message;
    }
}
