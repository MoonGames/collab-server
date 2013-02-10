/*
 * Collab server - Server from Collab project
 * Copyright (C) 2012 Martin Indra <aktive@seznam.cz>
 *
 * This file is part of Collab server.
 *
 * Collab server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Collab server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Collab server.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.mgn.collabserver;

/**
 *
 * @author indy
 */
public class CSUtils {

    /**
     * Generates human-readable string from byte array.
     *
     * Example 1 (without binary): [11, 73, 110, 100, 123]
     *
     * Example 2 (with binary): [11 (00001011), 73 (01001001), 110 (01101110),
     * 100 (01100100), 121 (01111001)]
     *
     * @param b byte array to stringify
     * @param binary if true binary representation will be attached (in brackets
     * after each number)
     *
     * @return generated string
     */
    public static final String byteArrayToString(byte[] b, boolean binary) {
        String zeros = "00000000";
        StringBuilder string = new StringBuilder();
        string.append("[");
        for (int i = 0; i < b.length; i++) {
            int value = b[i] >= 0 ? b[i] : 256 + b[i];
            string.append("").append(value);
            if (binary) {
                String bv = Integer.toBinaryString(value);
                bv = zeros.substring(bv.length()) + bv;
                string.append(" (").append(bv).append(")");
            }
            if ((i + 1) < b.length) {
                string.append(", ");
            }
        }
        string.append("]");
        return string.toString();
    }

    /**
     * transform integer to four bytes
     *
     * @param value integer to transform
     * @return bytes of transfromed integer
     */
    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value};
    }

    /**
     * transform four bytes to integer
     *
     * @param b bytes to transform
     * @return integer from transformed bytes
     */
    public static final int byteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    public static final boolean byteArrayToBoolean(byte[] b) {
        return (b == null || b.length == 0) ? false : b[0] != 0x00;
    }

    public static final byte[] booleanToByteArray(boolean value) {
        return new byte[]{(byte) (value ? 0x01 : 0x00)};
    }

    public static String toCompatibleCharacters(String origin) {
        return origin.replaceAll("[^\\p{L}\\p{N}]", "_");
    }
}
