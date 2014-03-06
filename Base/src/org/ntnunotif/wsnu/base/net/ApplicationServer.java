package org.ntnunotif.wsnu.base.net;

/**
 * Created by tormod on 3/3/14.
 */
public class ApplicationServer {

    private static ApplicationServer _singleton = null;

    /**
     * As this class is a singleton no external instantiation is allowed.
     */
    private ApplicationServer(){}

    /**
     * Function to return the singleton instance
     * @return
     */
    public static ApplicationServer getInstance(){
        if(_singleton == null){
            ApplicationServer server = new ApplicationServer();
            return _singleton;
        }else{
            return _singleton;
        }
    }
}
