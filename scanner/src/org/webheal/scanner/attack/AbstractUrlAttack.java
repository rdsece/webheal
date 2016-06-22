package org.webheal.scanner.attack;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.Header;
import org.webheal.scanner.UrlClient;
import org.webheal.scanner.UrlResponse;
import org.webheal.util.Logable;

public abstract class AbstractUrlAttack extends Logable
{
    protected UrlClient uc = new UrlClient();
    protected final Set<String> alreadyDone = new HashSet<String>();
    protected AttackAndResponseMatch conf;

    public void configure(AttackAndResponseMatch conf) {
        this.conf = conf;
    }

    public boolean attack(String url) throws Exception
    {
        if (isExclude(url)) {
            return false;
        }
        UrlParams src = new UrlParams(url);
        return attack(src);
    }

    protected abstract boolean attack(UrlParams src) throws Exception;

    protected boolean isExclude(String url)
    {
        int idx = url.indexOf('?');
        if (idx < 0) {
            return true;
        }
        return false;
    }

    public void done()
    {
        uc.close();
    }

    protected final String wget(String trgUrl)
    {
        try {
            UrlResponse resp = uc.wget(trgUrl);
            String data = resp.resultBody;
            //System.out.println("    "+trgUrl+" : "+data.length());
            return data;
        } catch (Throwable e) {
            uc.close();
            uc = new UrlClient();
            log().error("Could not wget : " + trgUrl, e);
            return null;
        }
    }
    protected final Header[] wgetHeaders(String trgUrl)
    {
        try {
            UrlResponse resp = uc.wget(trgUrl);
            return resp.allHeaders;
        } catch (Throwable e) {
            uc.close();
            uc = new UrlClient();
            log().error("Could not wget : " + trgUrl, e);
            return null;
        }
    }
}
