// 	VarSubDispatcher.java
//
//  abstract dispatcher that provides variable substitution in uris.

package org.mule.providers;

import java.util.*;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.*;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.impl.RequestContext;

/**
 * Allows endpoint variable substitution with properties from the message's 
 * property map.  To specify a variable in the endpoint, surround it with "$"s
 *
 * @author Ben Grooters
 */
public abstract class VarSubDispatcher extends AbstractMessageDispatcher
{
    private static final String DELIMITER = "$";

    public VarSubDispatcher( UMOImmutableEndpoint endpoint )
    {
        super( endpoint );
    }

    /** 
     * Replaces any variable in the form "$VAR$" with values in the event's 
     * property map
     */
    protected String substituteVars( String template )
    {
        String key;
        Object value;
        UMOMessage message = RequestContext.getEvent().getMessage();
        
        StringTokenizer st = new StringTokenizer(template,DELIMITER,true);
        StringBuffer returnValue = new StringBuffer();
        int delimCount = 0;
        while (st.hasMoreTokens())
        {
            String nt = st.nextToken();
            if ( DELIMITER.equals(nt) )
            {
                delimCount++;
                continue;
            }
            
            if ( delimCount % 2 == 0 )
                returnValue.append( nt );
            else
            {
                Object prop = RequestContext.getEvent().getMessage().getProperty( nt );
                if ( prop == null )
                    returnValue.append(DELIMITER+nt+DELIMITER);
                else
                    returnValue.append(prop.toString());
            }
        }
		
        return returnValue.toString();
    }
}
