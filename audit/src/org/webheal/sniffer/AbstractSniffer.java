package org.webheal.sniffer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pcap.reconst.beans.TcpConnection;
import pcap.reconst.output.HttpFlow;
import pcap.reconst.output.HttpRequestResponse;
import pcap.reconst.reconstructor.JpcapPacketProcessor;
import pcap.reconst.reconstructor.PacketReassembler;
import pcap.reconst.reconstructor.TcpReassembler;

public abstract class AbstractSniffer
{
    protected final PacketReassembler pr;
    protected final JpcapPacketProcessor jpcapProcessor;
    protected final File tcpFlowDir;
    protected final Set<String> notExt;
    protected final Set<String> notContentType;
    protected final Set<String> hostsFilter;
    protected final IHttpHandler handler;
    protected final boolean verbose;
    private static DateFormat TIME_FMT = new SimpleDateFormat("yyMMddHHss");
    private static NumberFormat SEQ_FMT = new DecimalFormat("000");

    public AbstractSniffer(PacketReassembler pr, Set<String> hostsFilter, Set<String> notExt, Set<String> notContentType, File tcpFlowDir, IHttpHandler handler, boolean verbose) throws IOException {
        this.notExt = notExt;
        this.notContentType = notContentType;
        this.hostsFilter = hostsFilter;
        this.tcpFlowDir = tcpFlowDir;
        this.pr = pr;
        jpcapProcessor = new JpcapPacketProcessor(pr,verbose);
        this.handler = handler;
        this.verbose = verbose;
    }

    public AbstractSniffer(PacketReassembler pr, IHttpHandler handler, boolean verbose) throws IOException {
        this.notExt = null;
        this.notContentType = null;
        this.hostsFilter = null;
        this.tcpFlowDir = null;
        this.pr = pr;
        jpcapProcessor = new JpcapPacketProcessor(pr,verbose);
        this.handler = handler;
        this.verbose = verbose;
    }

    public abstract void init() throws IOException; 
    
    public void drainPackets() throws IOException {
        Map<TcpConnection, TcpReassembler> map = pr.getReassembledPackets();
        if ( map.size() == 0 ) { 
            return;
        }
        processConnections(map);
    }
    
    protected void processConnections(Map<TcpConnection, TcpReassembler> map) throws IOException {
        if ( verbose ) {
            System.out.println("Connections to process = " + map.size());
        }
        Map<TcpConnection, List<HttpRequestResponse>> httpPackets = HttpFlow.packetize(map,hostsFilter,notExt,notContentType);
        System.out.println("httpstreams to process = " + httpPackets.size());
        String dt = TIME_FMT.format(new Date());
        for ( Map.Entry<TcpConnection, List<HttpRequestResponse>> entry : httpPackets.entrySet() ) {
            String conn = entry.getKey().toString();
            //System.out.println("Processing " + conn);
            int idx = 0;
            for ( HttpRequestResponse http : entry.getValue() ) {
                idx++;
                String seq = SEQ_FMT.format(idx);
                http.getRequest().decode();
                http.getResponse().decode();
                //System.out.println("Processing " + http.getHost()+", "+http.getRequestUri()+", inp:"+http.getRequest()+", out:"+http.getResponse());
                if ( tcpFlowDir != null ) {
                    http.writeToFile(tcpFlowDir,conn+"."+dt,seq);
                }
                try {
                    handler.handleHttp(entry.getKey(), http);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
