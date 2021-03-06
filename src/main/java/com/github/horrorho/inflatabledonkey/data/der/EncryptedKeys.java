/*
 * The MIT License
 *
 * Copyright 2016 Ahseya.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.horrorho.inflatabledonkey.data.der;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.util.encoders.Hex;

/**
 * Encrypted keys.
 *
 * @author Ahseya
 */
@Immutable
public final class EncryptedKeys extends ASN1Object {

    /*
        Template:
    
        SEQUENCE  (3)
         INTEGER            x
         SET  (1)           Set<EncryptedKey> encryptedKeySet
         cont [0] OPTIONAL  EXTERN 0x10068f1c
    
        EXTERN 0x10068f1c:
         SEQUENCE  (1)
          SEQUENCE  (2)
           INTEGER 
           OCTET_STRING
     */
    private static final int CONT0 = 0;

    private final int x;
    private final List<EncryptedKey> encryptedKeySet;
    private final Optional<byte[]> cont0;

    public EncryptedKeys(int x, List<EncryptedKey> encryptedKeySet, Optional<byte[]> cont0) {
        this.x = x;
        this.encryptedKeySet = new ArrayList<>(encryptedKeySet);
        this.cont0 = cont0.map(a -> Arrays.copyOf(a, a.length));
    }

    public EncryptedKeys(ASN1Primitive primitive) {
        DERIterator i = DER.asSequence(primitive);

        Map<Integer, ASN1Primitive> tagged = i.derTaggedObjects();

        x = DER.as(ASN1Integer.class, i)
                .getValue()
                .intValue();

        encryptedKeySet = DER.asSet(i, EncryptedKey::new);

        cont0 = Optional.ofNullable(tagged.get(CONT0))
                .map(DER.as(DEROctetString.class))
                .map(ASN1OctetString::getOctets);
    }

    public int x() {
        return x;
    }

    public List<EncryptedKey> encryptedKeySet() {
        return new ArrayList<>(encryptedKeySet);
    }

    public Optional<byte[]> cont0() {
        return cont0.map(a -> Arrays.copyOf(a, a.length));
    }

    @Override
    public ASN1Primitive toASN1Primitive() {
        DERTaggedObject cont0Encodable = cont0()
                .map(DEROctetString::new)
                .map(e -> new DERTaggedObject(CONT0, e))
                .orElseGet(null);

        ASN1EncodableVector vector = DER.vector(
                new ASN1Integer(x),
                DER.toSet(encryptedKeySet),
                cont0Encodable);

        return new DERSequence(vector);
    }

    @Override
    public String toString() {
        return "EncryptedKeys{"
                + "x=" + x
                + ", encryptedKeySet=" + encryptedKeySet
                + ", cont0=" + cont0.map(Hex::toHexString).orElse(null)
                + '}';
    }
}
