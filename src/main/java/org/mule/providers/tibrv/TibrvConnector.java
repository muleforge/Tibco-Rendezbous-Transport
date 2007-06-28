// 	TibrvConnector.java

// 	Ross Paul, ross.paul@mlb.com, 21 Jun 2006
// 	Time-stamp: <2007-06-26 16:58:10 rpaul>
package org.mule.providers.tibrv;

import com.tibco.tibrv.*;
import org.mule.providers.*;
import org.mule.umo.*;
import org.mule.umo.lifecycle.LifecycleException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.config.i18n.*;


/**
 * Allows mule to communicate over rendevoooooos!  Endpoint may be specified 
 * with: tibrv://subjectName.  Or to use certified messaging, specify endpoints
 * with: tibrv://subjectName?cmname=yourCMName.
 * The connector is configured by specifiying the service, network, &&|| daemon
 * as properties.  The Receiver are set up to act as distributed queue receivers
 * whose default weights etc should be set here.
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revision: 3 $
 */
public class TibrvConnector extends AbstractConnector
{
    public static final String CONTENT_FIELD = "content";

    //tibco connectino params
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


    /** @return "tibrv" */
    public String getProtocol()
    {
        return "tibrv";
    }

    public void doStart() throws UMOException
    {
        //noop
    }

    protected void doInitialise() throws InitialisationException
    {
    }

    /** Connects to the rvd and initializes a transport */
    public void doConnect() throws ConnectException
    {
        try{
            if( transport == null )
            {
                Tibrv.open( Tibrv.IMPL_NATIVE );
                if( logger.isDebugEnabled() )
                {
                    logger.debug( "Service: " + service );
                    logger.debug( "Network: " + network );
                    logger.debug( "Daemon: " + daemon );
                }
                transport = new TibrvRvdTransport( service, network, daemon );
            }
        }catch( Exception e ){
            throw new ConnectException
                ( CoreMessages.failedToCreate( "Tibrv Connector" ), e, this );
        }
    }
    
    /** Destroys transport and closes tibrv xs*/    
    public void doDisconnect()
    {
        try{
            if( transport != null )
            {
                transport.destroy();
                transport = null;
                Tibrv.close();
            }
        }catch( Exception e ){ logger.error( e, e ); }
    }


    protected void doStop() throws UMOException
    {
        // template method
    }

    public void doDispose()
    {
        doDisconnect();
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
