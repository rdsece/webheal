package org.webheal.sniffer;

import java.io.File;
import java.util.List;

import org.webheal.util.IExecutor;

import pcap.reconst.beans.TcpConnection;
import pcap.reconst.output.HttpRequestResponse;

/** reloads http handler if file changes */ 
public class AutoReloadHttpHandler implements IHttpHandler
{
    private final IExecutor<File, IHttpHandler> factory;
    private final List<HostPortConf> confList;

    public AutoReloadHttpHandler(final List<HostPortConf> confList,IExecutor<File,IHttpHandler> factory) {
        this.factory = factory;
        this.confList = confList;
    }
    public void handleHttp(TcpConnection conn, HttpRequestResponse http) throws Exception {
        for ( HostPortConf conf : confList ) {
            conf.handleHttp(factory,conn, http);
        }
    }
}
