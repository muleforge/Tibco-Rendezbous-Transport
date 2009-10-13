// 	ObjectToTibrvMsg.java

// 	Ross Paul, rossapaul@gmail.com, 13 Jul 2006
// 	Time-stamp: <2009-10-12 17:47:45 rpaul>
package org.mule.transport.tibrv.transformer;

import java.util.*;
import java.io.*;
import com.tibco.tibrv.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mule.transport.tibrv.TibrvConnector;

import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.api.transformer.TransformerException;
import org.mule.api.MuleMessage;


/**
 * If the object is a map, the transformer will create a TibrvMsg which 
 * presevers the mapping.  Otherwise, it will add the input with key:
 * TibrvConnector.CONTENT_FIELD.  If the Object is not a valid value, 
 * the tranformer will attempt to convert it into a byte[] and send it as
 * type TibrvMsg.OPAQUE
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revision: 3 $
 */ 
public class ObjectToTibrvMsg extends AbstractMessageAwareTransformer
{
    private static transient Log logger = 
        LogFactory.getLog( ObjectToTibrvMsg.class );

    public Object transform( MuleMessage message, String encoding ) 
        throws TransformerException
    {
        Object src = message.getPayload();

        if( src instanceof TibrvMsg )
            return src;

        TibrvMsg msg = null;
        try
        {
            msg = new TibrvMsg();
            
            if( src instanceof Map )
            {
                logger.debug( "Src is map, adding key value pairs" );
                Map map = (Map)src;
                String key;
                for( Iterator i = map.keySet().iterator(); i.hasNext(); )
                {
                    key = i.next().toString();
                    try
                    {
                        msg.add( key, map.get( key ));
                    }
                    catch( TibrvException e)
                    {
                        try
                        {
                            msg.add( key, byteMe(map.get( key )), 
                                     TibrvMsg.OPAQUE );
                        }
                        catch( IOException io )
                        {
                            throw new TransformerException( this, e );
                        }
                    }
                }
            }
            else
            {
                logger.debug( "adding src with fieldname: "
                              + TibrvConnector.CONTENT_FIELD + " src: " + src );
                
                try
                {
                    msg.add( TibrvConnector.CONTENT_FIELD, src );
                }
                catch( TibrvException e )
                {
                    try
                    {
                        msg.add( TibrvConnector.CONTENT_FIELD, byteMe(src), 
                                 TibrvMsg.OPAQUE );
                    }
                    catch( IOException io )
                    {
                        throw new TransformerException( this, e );
                    }
                }
            }
        }catch( Exception e ){ throw new TransformerException( this, e );}
        return msg;
    }

	protected byte[] byteMe( Object obj ) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( obj );
		oos.flush();
		baos.flush();
		return baos.toByteArray();
	}
}
    
