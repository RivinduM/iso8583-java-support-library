package org.wso2.iso8583;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

import java.util.Base64;

public class ISO8583Parser {

    public static void main(String[] args) {
        String msg = "0200B2384081080080100000000000000100201a45000000500000011152218012348945566612348888120610000a12a456789123USD006abc123021ABCDEFGHIJ 1234567890";
        try {
            OMElement omElement = unpackISOmessage(msg);
            System.out.println(omElement.toString());
        } catch (ISOException e) {
            e.printStackTrace();
        }
    }

    public static OMElement unpackISOmessage(String message) throws ISOException {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        GenericPackager packager = new GenericPackager(loader.getResourceAsStream(ISO8583Constant.PACKAGER_V_87));

        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(packager);
        isoMsg.unpack(message.getBytes());
        return messageBuilder(isoMsg);
    }

    /**
     * messageBuilder is used to build the xml iso messages.
     *
     * @param isomsg iso fields.
     */
    private static OMElement messageBuilder(ISOMsg isomsg) throws ISOException {
        OMFactory OMfactory = OMAbstractFactory.getOMFactory();
        OMElement parentElement = OMfactory.createOMElement(ISO8583Constant.TAG_MSG, null);
        if (isomsg.getHeader() != null) {
            OMElement header = OMfactory.createOMElement(ISO8583Constant.HEADER, null);
            header.setText(Base64.getEncoder().encodeToString(isomsg.getHeader()));
            parentElement.addChild(header);
        }

        String mti = isomsg.getString(0);
        String parentTag = getMessageType(mti);
        if (parentTag == null) {
            throw new ISOException("MTI not supported");
        }

        OMElement result = OMfactory.createOMElement(parentTag, null);
        for (int i = 0; i <= isomsg.getMaxField(); i++) {
            if (isomsg.hasField(i)) {
                String outputResult = isomsg.getString(i);
                OMElement messageElement = OMfactory.createOMElement(ISO8583Constant.iso8583idNames.get(String.valueOf(i)), null);
                messageElement.setText(outputResult);
                result.addChild(messageElement);
                parentElement.addChild(result);
            }
        }
        return parentElement;
    }

    private static String getMessageType(String mti) {

        if (mti.startsWith("02")) {
            return ISO8583Constant.ISO_O2XX;
        } else if (mti.startsWith("04")) {
            return ISO8583Constant.ISO_O4XX;
        } else if (mti.startsWith("08")) {
            return ISO8583Constant.ISO_O8XX;
        } else {
            return null;
        }
    }
}