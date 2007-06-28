// 	TibrvMessageToMap.java

// 	Ross Paul, ross.paul@mlb.com, 12 Jul 2006
// 	Time-stamp: <2007-06-26 17:17:51 rpaul>
package org.mule.providers.tibrv.transformers;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.tibco.tibrv.*;
import java.util.*;
import java.io.*;

/**
 * Takes in a tibrvMessage and converts it to a Map.  Additionally tibco 
 * specific types will be converted into java types.
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revision: 1.3 $
 */
public class TibrvMsgToMap extends AbstractTransformer
{

    public TibrvMsgToMap()
    {
        super();
        registerSourceType( TibrvMsg.class );
    }

	// probably want a separate transformer for messsage to object

    public Object doTransform( Object src, String encoding ) 
        throws TransformerException
    {
        Map map = null;
        try{
            map = transform( (TibrvMsg)src );
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
