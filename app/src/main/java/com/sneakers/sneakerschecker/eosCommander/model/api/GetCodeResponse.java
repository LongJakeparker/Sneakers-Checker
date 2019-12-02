package com.sneakers.sneakerschecker.eosCommander.model.api;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import com.sneakers.sneakerschecker.eosCommander.crypto.digest.Sha256;
import com.sneakers.sneakerschecker.eosCommander.util.StringUtils;

/**
 * Created by swapnibble on 2018-01-10.
 */

public class GetCodeResponse {
    @Expose
    private String account_name;

    @Expose
    private String wast;

    @Expose
    private String code_hash;

    @Expose
    private JsonObject abi;

    public JsonObject getAbi() { return abi; }

    public boolean isValidCode() {
        return ! ( StringUtils.isEmpty(code_hash) || Sha256.ZERO_HASH.toString().equals( code_hash ));
    }
}
