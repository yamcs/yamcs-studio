package org.yamcs.studio.ui.alphanum;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "parameter"
})
@XmlRootElement(name = "alphaNumeric")
public class AlphaNumericXml {
	
    @XmlElement(required = true)
    protected List<Parameter> parameter;
    
    
    public List<Parameter> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<Parameter>();
        }
        return this.parameter;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Parameter {

        @XmlAttribute(name = "qualifiedName", required = true)
        protected String qualifiedName;
        
        public String getQualifiedName() {
            return qualifiedName;
        }

        /**
         * Sets the value of the qualifiedName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setQualifiedName(String value) {
            this.qualifiedName = value;
        }
        
        
    }

}
