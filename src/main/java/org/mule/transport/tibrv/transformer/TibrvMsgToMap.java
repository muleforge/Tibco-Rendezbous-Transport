// 	TibrvMessageToMap.java

// 	Ross Paul, rossapaul@gmail.com, 12 Jul 2006
// 	Time-stamp: <2009-10-21 11:42:56 rpaul>
package org.mule.transport.tibrv.transformer;

import com.tibco.tibrv.*;
import java.util.*;
import java.io.*;

import org.mule.transport.tibrv.TibrvConnector;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.api.transformer.TransformerException;
import org.mule.api.MuleMessage;


/**
 * Takes in a tibrvMessage and converts it to a Map.  Additionally tibco 
 * specific types will be converted into java types.
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revision: 1.3 $
 */
public class TibrvMsgToMap extends AbstractMessageAwareTransformer
{

    public TibrvMsgToMap()
    {
        super();
        registerSourceType( TibrvMsg.class );
    }

	// probably want a separate transformer for messsage to object

    public Object transform( MuleMessage message, String encoding ) 
        throws TransformerException
    {
        Map map = null;
        try{
            map = transform( (TibrvMsg)message.getPayload() );
        }catch( Exception e ){ throw new TransformerException( this, e );}
        return map;
    }

    public static Map transform( TibrvMsg msg ) throws Exception
    {
        Map map = new HashMap();

        TibrvMsgField field = null;
        int fields = msg.getNumFields();
        for( int i = 0; i < fields; i++ )
        {
            field = msg.getFieldByIndex( i );
            Object data = null;
            switch ( field.type )
            {
            case TibrvMsg.OPAQUE:
                data = unByteMe( (byte[])field.data );
                break;
            case TibrvMsg.XML:
                data = new String(( (TibrvXml)field.data ).getBytes() );
                break;
            default:
                data = field.data;
            }
            map.put( field.name, data );
        }

        String subject = msg.getSendSubject();
        if( subject != null )
            map.put( TibrvConnector.SEND_SUBJECT, msg.getSendSubject() );

        subject = msg.getReplySubject();
        if( subject != null )
            map.put( TibrvConnector.REPLY_SUBJECT, msg.getReplySubject() );


        return map;
    }

	protected static Object unByteMe( byte[] ba ) throws Exception
	{
		ByteArrayInputStream bais = new ByteArrayInputStream( ba );
		ObjectInputStream ois = new ObjectInputStream( bais );
		Object ret = ois.readObject();
		ois.close();
		return ret;
	}
}
