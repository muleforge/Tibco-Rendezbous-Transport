// 	TibrvMessageReceiverReceiver.java

// 	Ross Paul, ross.paul@mlb.com, 22 Jun 2006
// 	Time-stamp: <2007-06-26 17:03:04 rpaul>
package org.mule.providers.tibrv;

import org.mule.providers.*;
import org.mule.impl.MuleMessage;
import org.mule.umo.provider.*;
import org.mule.umo.*;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.*;
import javax.resource.spi.work.Work;

import com.tibco.tibrv.*;

/**
 * Receives regular and certified messages from rendezvous.  See TibrvConnector
 * for more info
 *
 * @author <a href="mailto:rossapaul@gmail.com">Ross Paul</a>
 * @version $Revision: 1.6 $
 */
public class TibrvMessageReceiver extends AbstractMessageReceiver 
    implements TibrvMsgCallback
{
    private TibrvListener listener = null;
    private TibrvCmListener cmListener = null;
    private TibrvCmTransport cmTransport = null;
    private TibrvDispatcher tibrvDispatcher = null;
    


    public TibrvMessageReceiver( UMOConnector connector, 
                                 UMOComponent component, UMOEndpoint endpoint )
        throws InitialisationException
    {
        super(connector, component, endpoint);
        logger.debug( "receiver created" );
    }

    /** sets up the listener to and starts the dispatching thread */
    public void doConnect() throws Exception
    {
        TibrvTransport transport = ((TibrvConnector)connector).transport;
        String subject = endpoint.getEndpointURI().getAddress();
        String cmname = endpoint.getEndpointURI().getParams().getProperty
            ( "cmname" );

        logger.info( "Connecting on subject: " + subject );
        if( cmname != null )
        {
            logger.debug( "Using Certified Messagiging with cmname: " + cmname);
            TibrvConnector tibrvConnector = (TibrvConnector)connector;
            int workerWeight = tibrvConnector.getDefaultWorkerWeight();
            int workerTasks = tibrvConnector.getDefaultWorkerTasks();
            int schedulerWeight = tibrvConnector.getDefaultSchedulerWeight();
            double schedulerHeartbeat = 
                tibrvConnector.getDefaultSchedulerHeartbeat();
            double schedulerActivation = 
                tibrvConnector.getDefaultSchedulerActivation();

            logger.debug( "workerWeight: " + workerWeight );
            logger.debug( "workerTasks: " + workerTasks );
            logger.debug( "schedulerWeight: " + schedulerWeight );
            logger.debug( "schedulerHeartbeat: " + schedulerHeartbeat );
            logger.debug( "schedulerActivation: " + schedulerActivation );

            cmTransport = new TibrvCmQueueTransport
                ( (TibrvRvdTransport)transport, cmname, workerWeight, 
                  workerTasks, schedulerWeight, schedulerHeartbeat,
                  schedulerActivation );
            cmListener = new TibrvCmListener( Tibrv.defaultQueue(), this,
                                              cmTransport, subject, null );
        }
        else
        {
        listener = new TibrvListener
            ( Tibrv.defaultQueue(), this, transport, subject, null );
        }
    }

    /** Frees up the rendezvous resources */
    public void doDisconnect() throws Exception
    {
        if( listener != null )
            listener.destroy();
        if( cmListener != null )
            cmListener.destroy();
        if( cmTransport != null )
            cmTransport.destroy();
        doStop();
        listener = null;
        cmListener = null;
        cmTransport = null;
    }

    /** stops dispatching messages */
    protected void doStop() throws UMOException
    {
        try
        {
            if( tibrvDispatcher != null )
            {
                tibrvDispatcher.destroy();
                tibrvDispatcher = null;
            }
        }catch( Exception e ){ throw new LifecycleException( e, this );}
    }

    /** starts a dispatcher on the default message queue */
    protected void doStart() throws UMOException
    {
        try
        {
            if( tibrvDispatcher == null )
            {
                tibrvDispatcher = new TibrvDispatcher( "Bus", 
                                                       Tibrv.defaultQueue() );
            }
        }catch( Exception e ){ throw new LifecycleException( e, this );}
    }


    /** forwards the rendezvous messages to the umo */
    public void onMsg( TibrvListener rvListener, TibrvMsg msg )
    {
        try{
            if( logger.isInfoEnabled() )
            {
                logger.info("message received on: " + rvListener.getSubject() );
                logger.debug( msg );
            }
            
            getWorkManager().startWork( new Worker( msg ));
        }catch( Exception e ){ handleException( e );}
    }


    protected void doDispose()
    {
        // template method
    }

    private class Worker implements Work
    {
        private TibrvMsg message;
        
        public Worker( TibrvMsg message )
        {
            this.message = message;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            try
            {
                UMOMessageAdapter adapter = 
                    connector.getMessageAdapter( message );
                routeMessage( new MuleMessage( adapter ));
            }
            catch( Exception e )
            {
                handleException( e );
            }
        }

        public void release()
        {
            // no op
        }
    }
}
