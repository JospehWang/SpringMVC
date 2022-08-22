package com.springmvc.xml;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * 解析classpath:springmvc.xml
 */
public class XmlPaser {
    public static String getBasePackage(String xml){
        SAXReader saxReader = new SAXReader();
        InputStream resourceAsStream = XmlPaser.class.getClassLoader().getResourceAsStream(xml);
        //xml文档对象
        try {
            Document read = saxReader.read(resourceAsStream);
            Element rootElement = read.getRootElement();
            Element element = rootElement.element("component-scan");
            Attribute attribute = element.attribute("base-package");
            String BasePackage = attribute.getText();
            return BasePackage;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "";
    }
}
