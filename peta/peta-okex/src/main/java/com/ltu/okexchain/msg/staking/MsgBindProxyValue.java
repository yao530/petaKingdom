package com.ltu.okexchain.msg.staking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class MsgBindProxyValue {

    @JsonProperty("delegator_address")
    @SerializedName("delegator_address")
    private String delAddress;

    @JsonProperty("proxy_address")
    @SerializedName("proxy_address")
    private String proxyAddress;


    public void setDelAddress(String delAddress) {
        this.delAddress = delAddress;
    }
    public void setProxyAddress(String proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("proxy_address", this.delAddress)
                .append("reg", this.proxyAddress)
                .toString();
    }

}
