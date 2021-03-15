package com.redhat.example.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.dataformat.bindy.annotation.FixedLengthRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@FixedLengthRecord(ignoreTrailingChars = true)
public class FundsetEnquiry implements Serializable {

    @XmlAttribute
    @DataField(pos = 1, lengthPos = 25, trim = true) // 0-24
    private String username;

    @XmlAttribute
    @DataField(pos = 2, lengthPos = 40) // 25-64
    private String ReplyQueueName;

    @XmlAttribute
    @DataField(pos = 3, lengthPos = 5) // 65-69
    private String version;

    @XmlAttribute
    @DataField(pos = 4, lengthPos = 30) // 70-99
    private String TransactionId;

    @XmlAttribute
    @DataField(pos = 5, lengthPos = 11) // 100-111
    private String FundSetRef;

    @XmlAttribute
    @DataField(pos = 6, lengthPos = 10, length = 10, paddingChar = '0', align = "R")  // 112-121
    private String SchemaNumber;

    @DataField(pos = 7, lengthPos = 15) // 122-136
    private String ignored;

    @XmlAttribute
    @DataField(pos = 8, lengthPos = 2) // 137-138
    private String CallType;

    @XmlAttribute
    @DataField(pos = 9, lengthPos = 30, trim = false) // 139-168
    private String Fund;

    @XmlAttribute
    @DataField(pos = 10, lengthPos = 1) // 169
    private String Series;

    @XmlAttribute
    @DataField(pos = 11, lengthPos = 9) // 170-178
    private String EffectiveDate;

    @XmlAttribute
    @DataField(pos = 12, lengthPos = 13) // 179-191
    private String ProductVersion;

//    @XmlAttribute
//    public String getCombinedName() {
//        return firstName + " " + lastName;
//    }
}