// 	TibrvMessageReceiverReceiver.java

// 	Ross Paul, rossapaul@gmail.com, 22 Jun 2006
// 	Time-stamp: <2009-10-16 11:05:29 rpaul>
package org.mule.transport.tibrv;


import javax.resource.spi.work.Work;

import com.tibco.tibrv.*;


import org.mule.api.MuleException;
import org.mule.api.transport.MessageAdapter;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.lifecycle.LifecycleException;
import org.mule.api.service.Service;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;
import org.mule.api.transport.Connector;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.AbstractReceiverWorker;
import org.mule.transport.ConnectException;
import org.mule.DefaultMuleMessage;



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
    


    public TibrvMessageReceiver( Connector connector, 
                                 Service service, InboundEndpoint endpoint )
        throws CreateException
    {
        super(connector, service, endpoint);
        logger.debug( "receiver created" );
    }

    /** sets up the listener to and starts the dispatching thread */
    public void doConnect() throws Exception
    {
        TibrvTransport transport = ((TibrvConnector)connector).transport;

        String subject = endpoint.getEndpointURI().getAuthority();
        String cmname = (String)endpoint.getProperty( "cmname" );

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
    protected void doStop( boolean force ) throws MuleException
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
    protected void doStart() throws MuleException
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
            
            getWorkManager().doWork( new Worker( msg ));
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
                MessageAdapter adapter = 
                    connector.getMessageAdapter( message );
                routeMessage( new DefaultMuleMessage( adapter ));
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
