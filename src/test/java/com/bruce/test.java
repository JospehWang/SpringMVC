package com.bruce;

import com.springmvc.xml.XmlPaser;
import org.junit.Test;

public class test {
    @Test
    public void testReader(){
        String basePackage = XmlPaser.getBasePackage("springmvc.xml");
        //System.out.println(basePackage);

    }
}
