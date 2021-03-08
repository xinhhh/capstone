//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.10 at 04:17:37 PM BST 
//


package uk.ac.cam.ceb.como.io.chem.file.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="0" minOccurs="0">
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}dictRef"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}dataType"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}units"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}multiplierToSI"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}constantToSI"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}title"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}unitType"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}id"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}convention"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "tableHeaderCell")
public class TableHeaderCell {

    @XmlAttribute(name = "dictRef")
    protected java.lang.String dictRef;
    @XmlAttribute(name = "dataType")
    protected java.lang.String dataType;
    @XmlAttribute(name = "units")
    protected java.lang.String units;
    @XmlAttribute(name = "multiplierToSI")
    protected Double multiplierToSI;
    @XmlAttribute(name = "constantToSI")
    protected Double constantToSI;
    @XmlAttribute(name = "title")
    protected java.lang.String title;
    @XmlAttribute(name = "unitType")
    protected java.lang.String unitType;
    @XmlAttribute(name = "id")
    protected java.lang.String id;
    @XmlAttribute(name = "convention")
    protected java.lang.String convention;

    /**
     * Gets the value of the dictRef property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getDictRef() {
        return dictRef;
    }

    /**
     * Sets the value of the dictRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setDictRef(java.lang.String value) {
        this.dictRef = value;
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setDataType(java.lang.String value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setUnits(java.lang.String value) {
        this.units = value;
    }

    /**
     * Gets the value of the multiplierToSI property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMultiplierToSI() {
        return multiplierToSI;
    }

    /**
     * Sets the value of the multiplierToSI property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMultiplierToSI(Double value) {
        this.multiplierToSI = value;
    }

    /**
     * Gets the value of the constantToSI property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getConstantToSI() {
        return constantToSI;
    }

    /**
     * Sets the value of the constantToSI property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setConstantToSI(Double value) {
        this.constantToSI = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setTitle(java.lang.String value) {
        this.title = value;
    }

    /**
     * Gets the value of the unitType property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getUnitType() {
        return unitType;
    }

    /**
     * Sets the value of the unitType property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setUnitType(java.lang.String value) {
        this.unitType = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

    /**
     * Gets the value of the convention property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getConvention() {
        return convention;
    }

    /**
     * Sets the value of the convention property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setConvention(java.lang.String value) {
        this.convention = value;
    }

}
