/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.painless.api;

import java.lang.Integer;
import java.lang.Math;
import java.lang.Double;

/**
 * Encapsulation of operations related to IPv4 addresses written in CIDR notation.
 */
public class CIDR {
    private static String ip4addrPattern = "^(\\d{1,3}\\.){3}\\d{1,3}$";
    private static String cidrPattern = "^(\\d{1,3}\\.){3}\\d{1,3}\\/\\d\\d?$";

    private int upperBound;
    private int lowerBound;

    private int addressToInt(String ip4addr) throws IllegalArgumentException {
        String[] addr = ip4addr.split(".");
        for (String bit8num : addr) {
            if (Integer.parseUnsignedInt(bit8num) > 255) {
                throw new IllegalArgumentException("Can't parse " + ip4addr + " into IPv4 address, contains " + bit8num + " which is not an 8 bit number.");
            }
        }
        return Integer.parseUnsignedInt(String.join("",addr));
    }

    private double maskToDouble(String mask) throws IllegalArgumentException {
        if (Integer.parseUnsignedInt(mask) > 32) {
            throw new IllegalArgumentException("Subnet mask " + mask + " not within range 0-32.");
        }
        return Double.parseDouble(mask);
    }

    CIDR(String cidrBlock) throws IllegalArgumentException {
        if (!cidrBlock.matches(cidrPattern)) {
            throw new IllegalArgumentException(cidrBlock + " does not match CIDR block pattern " + cidrPattern);
        }
        String[] cidr = cidrBlock.split("/");
        lowerBound = addressToInt(cidr[0]);
        upperBound = lowerBound + (int)Math.pow(2.0, 32.0-maskToDouble(cidr[1]));
    }

    public boolean contains(String ip4addr) throws IllegalArgumentException {
        if (!ip4addr.matches(ip4addrPattern)) {
            throw new IllegalArgumentException(ip4addr + " does not match IPv4 address pattern " + ip4addrPattern);
        }
        int addr = addressToInt(ip4addr);
        return (Integer.compareUnsigned(lowerBound, addr) <= 0 && Integer.compareUnsigned(addr, upperBound) < 0);
    }
}
