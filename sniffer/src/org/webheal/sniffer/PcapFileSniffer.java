package org.webheal.sniffer;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import jpcap.JpcapCaptor;
import pcap.reconst.reconstructor.JpcapPacketReceiver;
import pcap.reconst.reconstructor.StreamReassembler;

public class PcapFileSniffer extends AbstractSniffer
{
    private final File src;

    public PcapFileSniffer(File src, Set<String> hostsFilter, Set<Integer> httpPort, Set<String> notExt, Set<String> notContentType, File tcpFlowDir, IHttpHandler handler,boolean verbose) throws IOException {
        super(new StreamReassembler(httpPort,verbose), false,hostsFilter, notExt, notContentType, tcpFlowDir, handler,-1,verbose);
        this.src = src;
    }

    @Override public void init() throws IOException
    {
        JpcapCaptor captor = JpcapCaptor.openFile(src.getAbsolutePath());
        captor.setFilter(getTcpFilter(), true);
        //captor.setFilter("tcp", true);
        JpcapPacketReceiver jpcapPacketProcessor = new JpcapPacketReceiver(pr,false,verbose);
        captor.processPacket(-1, jpcapPacketProcessor);
        captor.close();
    }
}
