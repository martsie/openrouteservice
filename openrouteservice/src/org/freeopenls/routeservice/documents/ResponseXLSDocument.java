

package org.freeopenls.routeservice.documents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.QName;

import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractHeaderType;
import net.opengis.xls.AbstractResponseParametersType;
import net.opengis.xls.DetermineRouteResponseType;
import net.opengis.xls.ResponseHeaderType;
import net.opengis.xls.ResponseType;
import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.freeopenls.constants.OpenLS;
import org.freeopenls.constants.RouteService;


/**
 * Class RespRouteXLSDoc - For response of a request to OpenLS Route Service.
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * @version 1.0 2006-06-05
 * @version 1.1 2008-04-23
 */

/**
 * <p><b>Title: ResponseXLSDocument</b></p>
 * <p><b>Description:</b> Class for ResponseXLSDocument - For response of a request to OpenLS Route Service.<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-06-05
 * @version 1.1 2008-04-23
 */
public class ResponseXLSDocument{
	/** XLSDocument */
	private XLSDocument mXLSDocument;
	/** XLSType from XLSDocOut */
	private XLSType mXLSType = null;
	/** ResponseHeaderType from xlsTypeOut */
	private ResponseHeaderType mResponseHeader = null;
	/** ResponseType from xlsTypeOut */
	private ResponseType mResponse = null;

    /**
     * Constructor
     */
	public ResponseXLSDocument(XLSDocument xlsDoc){
		mXLSDocument = xlsDoc;
	}
	
    /**
     * Constructor
     */
    public ResponseXLSDocument(String sessionID) {

		//Create New XLSDocument for Response
		mXLSDocument = XLSDocument.Factory.newInstance();
		mXLSDocument.documentProperties().setVersion("1.0");
		mXLSDocument.documentProperties().setEncoding("UTF-8");
		mXLSDocument.documentProperties().setSourceName("source");
		mXLSType = mXLSDocument.addNewXLS();
		mXLSType.setVersion(new BigDecimal("1.1"));

		//Header
		AbstractHeaderType ahTypeOut = mXLSType.addNewHeader();
		mResponseHeader = (ResponseHeaderType) ahTypeOut.changeType(ResponseHeaderType.type);
		
		if(sessionID != null)
			mResponseHeader.setSessionID(sessionID);

		//For well-formed XML-Doc
		XmlCursor cursorXLSDoc = mXLSDocument.newCursor();
		if (cursorXLSDoc.toFirstChild())
			cursorXLSDoc.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"), OpenLS.SCHEMA_LOCATION_OPENLS+" "+RouteService.SCHEMA_FILENAME_ROUTESERVICE);
		cursorXLSDoc.dispose();
		
		XmlCursor cursorXLSType = mXLSType.newCursor();
		if (cursorXLSType.toChild(new QName("http://www.opengis.net/xls", "_Header")))
			cursorXLSType.setName(new QName("http://www.opengis.net/xls","ResponseHeader"));
		cursorXLSType.dispose();
    }

    /**
     * Create Response in the document
     * 
     * @param requestID
     * @param version
     * @param numberofResponses
     */
    public void createResponse(String requestID, String version, BigInteger numberofResponses){
		AbstractBodyType abTypeOut = mXLSType.addNewBody();
		mResponse = (ResponseType) abTypeOut.changeType(ResponseType.type);
		mResponse.setRequestID(requestID);
		mResponse.setVersion(version);
		mResponse.setNumberOfResponses(numberofResponses);
    }

    /**
     * Add DeterminRouteResponseType to the document
     * 
     * @return DetermineRouteResponseType
     */
    public DetermineRouteResponseType addResponseParameters(){
		AbstractResponseParametersType arespparamType = mResponse.addNewResponseParameters();
		return (DetermineRouteResponseType) arespparamType.changeType(DetermineRouteResponseType.type);
    }

    public void doWellFormedDetermineRouteResponse(){
		//For well formed XML-Doc
		XmlCursor cursorXLS = mXLSType.newCursor();
		XmlCursor cursorReqType = mResponse.newCursor();

		if (cursorXLS.toChild(new QName("http://www.opengis.net/xls", "_Body")))
			cursorXLS.setName(new QName("http://www.opengis.net/xls","Response"));
		if (cursorReqType.toChild(new QName("http://www.opengis.net/xls", "_ResponseParameters")))
			cursorReqType.setName(new QName("http://www.opengis.net/xls","DetermineRouteResponse"));
		cursorXLS.dispose();
		cursorReqType.dispose();
    }
 
    /**
     * Method that returns length of the content in bytes.
     * 
     * @return int 
     * 			Returns the length of the content in bytes.
     * @throws IOException 
     * 			If the transformation of the XLSDocument into a byte[] failed
     */
    public int getContentLength() throws IOException {
        return getByteArray().length;
    }

    /**
     * Method that returns content type of this response
     * 
     * @return String
     * 			Returns the value of the constant OpenLS.CONTENT_TYPE_XML.
     */
    public String getContentType() {
        return OpenLS.CONTENT_TYPE_XML;
    }

    /**
     * Method that returns response as byte[].
     * 
     * @return byte[]
     * 			Returns the response as byte[]
     * @throws IOException
     * 			If the transformation of the XLSDocument into a byte[] failed
     */
    public byte[] getByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlOptions options = new XmlOptions();
        options.setSaveNamespacesFirst();
        options.setSaveAggressiveNamespaces();
        options.setSavePrettyPrint();
        mXLSDocument.save(baos,options);
        byte[] bytes = baos.toByteArray();
        
        return bytes;
    }
}