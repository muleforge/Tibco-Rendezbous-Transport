/*
 * $Id: NamespaceHandler.vm 10621 2008-01-30 12:15:16Z dirk.olmes $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.tibrv.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.TransformerDefinitionParser;
import org.mule.endpoint.URIBuilder;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import org.mule.transport.tibrv.*;
import org.mule.transport.tibrv.transformer.*;

/**
 * Registers a Bean Definition Parser for handling 
 * <code><tibrv:connector></code> elements and supporting endpoint elements.
 */
public class TibrvNamespaceHandler extends AbstractMuleNamespaceHandler
{
    public void init()
    {
        registerStandardTransportEndpoints( TibrvConnector.TIBRV, 
                                            URIBuilder.PATH_ATTRIBUTES);

        registerConnectorDefinitionParser(TibrvConnector.class);

        registerBeanDefinitionParser
            ( "tibrvmsg-to-map-transformer", 
              new TransformerDefinitionParser( TibrvMsgToMap.class ));
        registerBeanDefinitionParser
            ( "object-to-tibrvmsg", 
              new TransformerDefinitionParser( ObjectToTibrvMsg.class ));

    }
}
