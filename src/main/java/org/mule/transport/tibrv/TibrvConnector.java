// 	TibrvConnector.java

// 	Ross Paul, rossapaul@gmail.com, 21 Jun 2006
// 	Time-stamp: <2009-10-21 11:41:32 rpaul>
package org.mule.transport.tibrv;

import com.tibco.tibrv.*;

import org.mule.api.MuleException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.service.Service;
import org.mule.api.transport.MessageReceiver;
import org.mule.transport.AbstractConnector;


/**
 * Allows mule to communicate over rendevoooooos!  Now mule 2.2.1 friendly.
 * Endpoing should be specified with a subject and optionally a cmname.  When
 * cmname is present, the Dispatcher will send certified, and the receiver will
 * both recieve certified and register itself as part of a distributed queue.
 * Subjects support wildcards and mule expressions. Remember, when using the 
 * wildcard, ">", make sure you uri encode it to %3E
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revision: 3 $
 */
public class TibrvConnector extends AbstractConnector
{
    public static final String TIBCORV = "tibcorv";
    public static final String CONTENT_FIELD = "content";

    public static final String SEND_SUBJECT = "__send__subject__";
    public static final String REPLY_SUBJECT = "__reply__subject__";

    //tibco connection params
    private String service;
    private String network;
    private String daemon;

    //default weights etc
    private int defaultWorkerWeight = 1;
    private int defaultWorkerTasks = 1;
    private int defaultSchedulerWeight = 1;
    private double defaultSchedulerHeartbeat = 1.0;
    private double defaultSchedulerActivation = 3.5;
    
    TibrvTransport transport = null;


    /** @return "tibcorv" */
    public String getProtocol()
    {
        return TIBCORV;
    }

    public void doStart() throws MuleException
    {
        //noop
    }

    protected void doInitialise() throws InitialisationException
    {
    }

    /** Connects to the rvd and initializes a transport */
    public void doConnect() throws Exception
    {
        if( transport == null )
        {
            Tibrv.open( Tibrv.IMPL_NATIVE );
            if( logger.isInfoEnabled() )
            {
                logger.info( "Service: " + service );
                logger.info( "Network: " + network );
                logger.info( "Daemon: " + daemon );
            }
            transport = new TibrvRvdTransport( service, network, daemon );
        }
    }
    
    /** Destroys transport and closes tibrv xs*/    
    public void doDisconnect() throws Exception
    {
        if( transport != null )
        {
            transport.destroy();
            transport = null;
            Tibrv.close();
        }
    }


    protected void doStop() throws MuleException
    {
        // template method
    }

    public void doDispose() 
    {
        try
        {
            doDisconnect();
        }catch( Exception e )
        {
            logger.error( e, e );
        }
    }

    //mmmmm.... beans.......
    public void setService( String service )
    {
        this.service = service;
    }

    public void setNetwork( String network )
    {
        this.network = network;
    }

    public void setDaemon( String daemon )
    {
        this.daemon = daemon;
    }

    public String getService()
    {
        return service;
    }

    public String getNetwork()
    {
        return network;
    } 

    public String getDaemon()
    {
        return daemon;
    }

    public int getDefaultWorkerWeight()
    {
        return defaultWorkerWeight;
    }

    public void setDefaultWorkerWeight( int defaultWorkerWeight )
    {
        this.defaultWorkerWeight = defaultWorkerWeight;
    }

    public int getDefaultWorkerTasks()
    {
        return defaultWorkerTasks;
    }

    public void setDefaultWorkerTasks( int defaultWorkerTasks )
    {
        this.defaultWorkerTasks = defaultWorkerTasks;
    }

    public int getDefaultSchedulerWeight()
    {
        return defaultSchedulerWeight;
    }

    public void setDefaultSchedulerWeight( int defaultSchedulerWeight )
    {
        this.defaultSchedulerWeight = defaultSchedulerWeight;
    }

    public double getDefaultSchedulerHeartbeat()
    {
        return defaultSchedulerHeartbeat;
    }

    public void setDefaultSchedulerHeartbeat( double defaultSchedulerHeartbeat )
    {
        this.defaultSchedulerHeartbeat = defaultSchedulerHeartbeat;
    }

    public double getDefaultSchedulerActivation()
    {
        return defaultSchedulerActivation;
    }

    public void setDefaultSchedulerActivation(double defaultSchedulerActivation)
    {
        this.defaultSchedulerActivation = defaultSchedulerActivation;
    }
}
