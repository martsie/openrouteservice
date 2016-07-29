/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.emergencyrouteservice;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;


import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.freeopenls.emergencyrouteservice.RespRouteXLSDoc;
import org.freeopenls.error.ServiceError;



/**
 * <p><b>Title: RequestOperator </b></p>
 * <p><b>Description:</b> After parsing the request through the doOperation() method, the
 * request is send up to the ERSListener </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-08-01
 */
public class RequestOperator {
    /** Logger, used to log errors(exceptions) and additionally information */
    protected static Logger mLogger = Logger.getLogger(RequestOperator.class.getName());

    /**
     * Method that parse the transfered String, check what instance it is and
     * send, if it is a XLSDoc, the doc to the ERSListener(). Return the response
     * of the ERSListener().
     * 
     * @param sRequest
     *			String that contains the XMLRequest
     * @return RespRouteXLSDoc
     * 			- Returns Response XLSDocument from the ERSListener()
     */
    public RespRouteXLSDoc doOperation(String sRequest) {
    	
        RespRouteXLSDoc response = null;
        XmlObject doc = null;

        try{
        	doc = XmlObject.Factory.parse(sRequest);
        } catch (XmlException xmle) {
        	mLogger.error("Request is NOT well-formed! "+xmle);
        	ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.OTHER_XML, null, xmle.getMessage());
            return new RespRouteXLSDoc(se.getErrorListXLSDocument(""));
        }
        
        //Check what Doc it is
        if (doc instanceof XLSDocument) {
            ERSListener routeListener = new ERSListener();
        	response = routeListener.receiveCompleteRequest(doc);
        }else{
        	mLogger.error("Request is NOT a XLSDoc!");
        }
        
        return response;
    }
}